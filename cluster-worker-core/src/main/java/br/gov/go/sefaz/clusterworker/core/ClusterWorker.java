package br.gov.go.sefaz.clusterworker.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.IQueue;
import com.hazelcast.core.Member;

import br.gov.go.sefaz.clusterworker.core.annotation.ConsumeFromQueue;
import br.gov.go.sefaz.clusterworker.core.annotation.ProduceToQueue;
import br.gov.go.sefaz.clusterworker.core.constants.ClusterWorkerConstants;
import br.gov.go.sefaz.clusterworker.core.consumer.HazelcastRunnableConsumer;
import br.gov.go.sefaz.clusterworker.core.factory.ClusterWorkerFactory;
import br.gov.go.sefaz.clusterworker.core.item.ItemProcessor;
import br.gov.go.sefaz.clusterworker.core.item.ItemProducer;
import br.gov.go.sefaz.clusterworker.core.listener.ShutdownListener;
import br.gov.go.sefaz.clusterworker.core.producer.HazelcastRunnableProducer;
import br.gov.go.sefaz.clusterworker.core.support.AnnotationSupport;

/**
 * Central core to manage hazelcast executor services and it's lifecycle.
 * @author renato-rs
 * @since 1.0
 * @param <T> type which ClusterWorker will handle.
 */
public final class ClusterWorker<T> {

    private static final Logger logger = Logger.getLogger(ClusterWorker.class);

    private final HazelcastInstance hazelcastInstance;
    private final Map<ItemProducer<T>, Timer> itemProducerTimerMap = new HashMap<>();
    private final ArrayList<ShutdownListener> shutdownListeners = new ArrayList<>();

    /**
     * Constructor for ClusterWorker.
     * @param hazelcastInstance instance of hazelcast.
     */
    public ClusterWorker(HazelcastInstance hazelcastInstance) {
    	this.hazelcastInstance = hazelcastInstance;
    }
    
    /**
     * Execute a {@link ItemProcessor} client's implementation on hazelcast executor service.
     * @param itemProcessor implementation of client item.
     */
    public void executeItemProccessor(ItemProcessor<T> itemProcessor){
    	//Assert mandatory exception to create an ItemProcessor
        ConsumeFromQueue consumeFromQueue = AnnotationSupport.assertMandatoryAnnotation(itemProcessor, ConsumeFromQueue.class);

        // Number of workers (threads)
        int workers = consumeFromQueue.workers();

        logger.info(String.format("Executing ItemProcessor (%s worker(s)) implementation on hazelcast executor service.", workers));

        // Creates worker (HazelcastRunnableConsumer)
        HazelcastRunnableConsumer<T> hazelcastRunnableConsumer = ClusterWorkerFactory.getInstance(hazelcastInstance).getHazelcastRunnableConsumer(itemProcessor);
        for (int i = 1; i <=  workers; i++) {

            try {
                logger.debug(String.format("Adding listener for worker %s..", i));
            	this.shutdownListeners.add(hazelcastRunnableConsumer);
            	// Executes the consumer on hazelcast local member
				hazelcastInstance
            		.getExecutorService(ClusterWorkerConstants.CW_EXECUTOR_SERVICE_NAME)
            		.executeOnMember(hazelcastRunnableConsumer, getLocalMember());
            }catch (Exception e){
                logger.error(String.format("Cannot execute a ItemProcessor on hazelcast executor service! %s", e.getMessage()));
            }
        }
    }

    /**
     * Execute a {@link ItemProducer} client's implementation on hazelcast executor service.
     * @param itemProducer implementation of client item.
     */
    public void executeItemProducer(final ItemProducer<T> itemProducer){
    	//Assert mandatory exception to create an ItemProducer
    	final ProduceToQueue produceToQueue = AnnotationSupport.assertMandatoryAnnotation(itemProducer, ProduceToQueue.class);
    	
    	final HazelcastRunnableProducer<T> hazelcastRunnableProducer = ClusterWorkerFactory.getInstance(hazelcastInstance).getHazelcastRunnableProducer(itemProducer);

    	// Frequency of produce's execution 
        int frequency = produceToQueue.frequency();
        
        Timer timerItemProducer = new Timer();

        logger.info(String.format("Executing ItemProducer implementation on hazelcast executor service with frequency of %s second.", frequency));

        final int TIMER_DELAY = 0;
        
        // Create a fixed rate timer 
		timerItemProducer.scheduleAtFixedRate(

                new TimerTask() {
                    @Override
                    public void run() {

                    	String queueName = produceToQueue.queueName();
                    	IQueue<Object> iQueue = hazelcastInstance.getQueue(queueName);
						logger.info(String.format("Hazelcast queue %s size: %s", queueName, iQueue.size()));
                        
                        // Execute item producer only if the queue has none elements to be processed
						// The execution will be activated only on hazelcast local member
                        if (iQueue.isEmpty() && isLocalMember()) {
                        	
                            try {
                                logger.debug("Executing ItemProducer implementation on hazelcast executor service.");
                                hazelcastInstance
                                	.getExecutorService(ClusterWorkerConstants.CW_EXECUTOR_SERVICE_NAME)
                                	.execute(hazelcastRunnableProducer);

                            } catch (Exception e) {
                                logger.error(String.format("Cannot execute a ItemProducer on hazelcast executor service! %s", e.getMessage()));
                            }
                        }
                    }
                },
                TIMER_DELAY,
                TimeUnit.MILLISECONDS.convert(frequency, TimeUnit.SECONDS)
        );

        itemProducerTimerMap.put(itemProducer, timerItemProducer);
    }

    /**
     * Verifies if this member is local member.
     * @return true is local member.
     */
    private boolean isLocalMember() {
        return hazelcastInstance.getCluster().getMembers().iterator().next().localMember();
    }

    /**
     * Returns the local cluster member.
     * @return member
     */
    private Member getLocalMember() {
        return hazelcastInstance.getCluster().getLocalMember();
    }

    /**
     * Shutdown entyre ClusterWorker core and all hazelcast instances.
     */
	public void shutdown() {

		logger.warn("Shutting down entire ClusterWorker!");

		logger.debug("Cancelling item timers ..");
		this.itemProducerTimerMap.values().forEach(Timer::cancel);

		logger.debug("Shutting down listeners ..");
		this.shutdownListeners.forEach(ShutdownListener::shutdown);

		IExecutorService executorService = hazelcastInstance.getExecutorService(ClusterWorkerConstants.CW_EXECUTOR_SERVICE_NAME);

		if (!executorService.isShutdown()) {
			executorService.shutdownNow();
			logger.warn("ExecutorService finished!");
		}

		if (hazelcastInstance.getLifecycleService().isRunning()) {
			hazelcastInstance.getLifecycleService().shutdown();
			logger.warn("Hazelcast LifecycleService finished!");
		}
	}
}
