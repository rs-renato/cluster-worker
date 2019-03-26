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
import com.hazelcast.core.Member;

import br.gov.go.sefaz.clusterworker.core.annotation.ConsumeFromQueue;
import br.gov.go.sefaz.clusterworker.core.annotation.ProduceToQueue;
import br.gov.go.sefaz.clusterworker.core.constants.ClusterWorkerConstants;
import br.gov.go.sefaz.clusterworker.core.consumer.HazelcastRunnableConsumer;
import br.gov.go.sefaz.clusterworker.core.factory.ClusterWorkerFactory;
import br.gov.go.sefaz.clusterworker.core.listener.ShutdownListener;
import br.gov.go.sefaz.clusterworker.core.producer.HazelcastRunnableProducer;
import br.gov.go.sefaz.clusterworker.core.queue.HazelcastQueueNameRoundRobin;
import br.gov.go.sefaz.clusterworker.core.support.AnnotationSupport;
import br.gov.go.sefaz.clusterworker.core.task.TaskProcessor;
import br.gov.go.sefaz.clusterworker.core.task.TaskProducer;

/**
 * Central core to manage hazelcast executor services and it's lifecycle.
 * @author renato-rs
 * @since 1.0
 * @param <T> type which ClusterWorker will handle.
 */
public final class ClusterWorker<T> {

    private static final Logger logger = Logger.getLogger(ClusterWorker.class);

    private final HazelcastInstance hazelcastInstance;
    private final Map<TaskProducer<T>, Timer> taskProducerTimerMap = new HashMap<>();
    private final ArrayList<ShutdownListener> shutdownListeners = new ArrayList<>();

    /**
     * Constructor for ClusterWorker.
     * @param hazelcastInstance instance of hazelcast.
     */
    public ClusterWorker(HazelcastInstance hazelcastInstance) {
    	this.hazelcastInstance = hazelcastInstance;
    }
    
    /**
     * Execute a {@link TaskProcessor} client's implementation on hazelcast executor service.
     * @param taskProcessor implementation of client task.
     */
    public void executeTaskProccessor(TaskProcessor<T> taskProcessor){
    	//Assert mandatory exception to create an TaskProcessor
        ConsumeFromQueue consumeFromQueue = AnnotationSupport.assertMandatoryAnnotation(taskProcessor, ConsumeFromQueue.class);

        int workers = consumeFromQueue.workers();

        logger.info(String.format("Executing TaskProcessor (%s worker(s)) implementation on hazelcast executor service.", workers));

        // Creates worker (HazelcastRunnableConsumer)
        for (int i = 1; i <=  workers; i++) {

            try {
            	HazelcastRunnableConsumer<T> hazelcastRunnableConsumer = ClusterWorkerFactory.getInstance(hazelcastInstance).getHazelcastRunnableConsumer(taskProcessor);
                logger.debug(String.format("Adding listener for worker %s..", i));
            	this.shutdownListeners.add(hazelcastRunnableConsumer);
				hazelcastInstance
            		.getExecutorService(ClusterWorkerConstants.CW_SERVICE_TASK_NAME)
            		.executeOnMember(hazelcastRunnableConsumer, getLocalMember());
            }catch (Exception e){
                logger.error(String.format("Cannot execute a TaskProcessor on hazelcast executor service! %s", e.getMessage()));
            }
        }
    }

    /**
     * Execute a {@link TaskProducer} client's implementation on hazelcast executor service.
     * @param taskProducer implementation of client task.
     */
    public void executeTaskProducer(final TaskProducer<T> taskProducer){
    	//Assert mandatory exception to create an TaskProducer
    	final ProduceToQueue produceToQueue = AnnotationSupport.assertMandatoryAnnotation(taskProducer, ProduceToQueue.class);

    	final HazelcastRunnableProducer<T> hazelcastRunnableProducer = ClusterWorkerFactory.getInstance(hazelcastInstance).getHazelcastRunnableProducer(taskProducer);

        int frequency = produceToQueue.frequency();
        
        String queueName = produceToQueue.queueName();
        
        HazelcastQueueNameRoundRobin hazelcastQueueNameRoundRobin = new HazelcastQueueNameRoundRobin(queueName);

        Timer timerTaskProducer = new Timer();

        logger.info(String.format("Executing TaskProducer implementation on hazelcast executor service with frequency of %s second.", frequency));

        final int TIMER_DELAY = 0;
        // Create a fixed rate timer 
		timerTaskProducer.scheduleAtFixedRate(

                new TimerTask() {
                    @Override
                    public void run() {

                        String nextEmptyQueue = hazelcastQueueNameRoundRobin.nextHazelcastQueue(hazelcastInstance, HazelcastQueueNameRoundRobin.STRATEGY.ACCEPT_ONLY_EMPTY);
                    	logger.debug(String.format("Requested the next queue name from round robin: %s. Accepted only empty queue.", nextEmptyQueue));
                        
                        // Execute task producer only the queue has none more elements to be processed (its empty) 
                        if (nextEmptyQueue != null) {
                        	logger.info(String.format("Hazelcast queue %s size: %s", nextEmptyQueue, hazelcastInstance.getQueue(nextEmptyQueue).size()));
                            boolean isLocalMember = isLocalMember();

                            // Execute the task just on the local members
                            if (isLocalMember) {

                                try {
                                    logger.debug("Executing TaskProducer implementation on hazelcast executor service.");
                                    hazelcastInstance
                                    	.getExecutorService(ClusterWorkerConstants.CW_SERVICE_TASK_NAME)
                                    	.execute(hazelcastRunnableProducer);

                                } catch (Exception e) {
                                    logger.error(String.format("Cannot execute a TaskProducer on hazelcast executor service! %s", e.getMessage()));
                                }
                            }
                        }
                    }
                },
                TIMER_DELAY,
                TimeUnit.MILLISECONDS.convert(frequency, TimeUnit.SECONDS)
        );

        taskProducerTimerMap.put(taskProducer, timerTaskProducer);
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

		logger.debug("Cancelling task timers ..");
		this.taskProducerTimerMap.values().forEach(Timer::cancel);

		logger.debug("Shutting down listeners ..");
		this.shutdownListeners.forEach(ShutdownListener::shutdown);

		IExecutorService executorService = hazelcastInstance.getExecutorService(ClusterWorkerConstants.CW_SERVICE_TASK_NAME);

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
