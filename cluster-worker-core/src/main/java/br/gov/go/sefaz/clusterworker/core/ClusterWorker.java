package br.gov.go.sefaz.clusterworker.core;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.Member;
import com.hazelcast.scheduledexecutor.IScheduledExecutorService;
import com.hazelcast.scheduledexecutor.IScheduledFuture;

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

	private static final Logger logger = LogManager.getLogger(ClusterWorker.class);

    private final HazelcastInstance hazelcastInstance;
    private final IScheduledExecutorService scheduledExecutorService;
    private final ArrayList<ShutdownListener> shutdownListeners = new ArrayList<>();
    private final ArrayList<IScheduledFuture<?>> scheduledFutures = new ArrayList<>();
    
    /**
     * Constructor for ClusterWorker.
     * @param hazelcastInstance instance of hazelcast.
     */
    public ClusterWorker(HazelcastInstance hazelcastInstance) {
    	this.hazelcastInstance = hazelcastInstance;
    	this.scheduledExecutorService = hazelcastInstance.getScheduledExecutorService(ClusterWorkerConstants.CW_EXECUTOR_SERVICE_NAME);
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

        logger.info(String.format("Configuring ItemProcessor (%s worker(s)) implementation on hazelcast executor service.", workers));

        // Creates worker (HazelcastRunnableConsumer)
        HazelcastRunnableConsumer<T> hazelcastRunnableConsumer = ClusterWorkerFactory.getInstance(this.hazelcastInstance).getHazelcastRunnableConsumer(itemProcessor);
        
        for (int i = 1; i <=  workers; i++) {

            try {
            	// Executes the consumer on hazelcast local member
				IScheduledFuture<?> scheduledFuture = this.scheduledExecutorService.scheduleOnMember(hazelcastRunnableConsumer,getLocalMember(),0, TimeUnit.SECONDS);
                
				logger.debug(String.format("Adding listener for worker %s..", i));
				this.shutdownListeners.add(hazelcastRunnableConsumer);
				logger.debug(String.format("Adding ScheduledFutures for worker %s..", i));
                this.scheduledFutures.add(scheduledFuture);
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
    	
    	final HazelcastRunnableProducer<T> hazelcastRunnableProducer = ClusterWorkerFactory.getInstance(this.hazelcastInstance).getHazelcastRunnableProducer(itemProducer);

    	IMap<String, Long> iMap = hazelcastInstance.getMap(ClusterWorkerConstants.CW_PRODUCER_SYNC_EXECUTION);
    	
    	// Frequency of produce's execution (converted to milleseconds to grant more sync) 
        long frequency = TimeUnit.SECONDS.toMillis(produceToQueue.frequency());
        
        // Try to syncronize the initial execution always on second 0
        Calendar calendar = Calendar.getInstance();
		long initialDelay = TimeUnit.SECONDS.toMillis(60L - calendar.get(Calendar.SECOND)) - calendar.get(Calendar.MILLISECOND);
		
		// Retrieve the last execution timestamp
        if (iMap.containsKey(ClusterWorkerConstants.CW_PRODUCER_LAST_EXECUTION)) {
        	// Check the difference from now 
        	long difference = Calendar.getInstance().getTimeInMillis() - iMap.get(ClusterWorkerConstants.CW_PRODUCER_LAST_EXECUTION);
        	// Calculate the initial delay to this execution
        	initialDelay = frequency - difference;
        }
        
        logger.info(String.format("Configuring ItemProducer implementation on hazelcast executor service with frequency of %s seconds and initial delay of %s seconds (aproximated).", TimeUnit.MILLISECONDS.toSeconds(frequency), TimeUnit.MILLISECONDS.toSeconds(initialDelay)));

        // Schedule the execution to execute into local member
        this.scheduledExecutorService.scheduleOnMemberAtFixedRate(hazelcastRunnableProducer, getLocalMember(), initialDelay, frequency, TimeUnit.MILLISECONDS);
    }

    /**
     * Returns the local cluster member.
     * @return member
     */
    private Member getLocalMember() {
        return this.hazelcastInstance.getCluster().getLocalMember();
    }

    /**
     * Shutdown the ClusterWorker core and its hazelcast instance, listeners, futures, etc.
     */
	public void shutdown() {

		logger.info("Shuttingdown ClusterWorker!");

		logger.info("Shuttingdown ScheduledFutures!");
		this.scheduledFutures.forEach(future -> {
			
			if (!future.isCancelled()) {
				future.cancel(false);
			}
		});
		
		logger.info("Shuttingdown Listeners ..");
		this.shutdownListeners.forEach(ShutdownListener::shutdown);
		
		if (this.hazelcastInstance.getLifecycleService().isRunning()) {
			
			this.hazelcastInstance.getLifecycleService().shutdown();
			logger.info("Hazelcast LifecycleService finished!");
		}
		
		logger.info("ClusterWorker shutdown completed!");
	}
}
