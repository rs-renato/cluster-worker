package br.gov.go.sefaz.clusterworker.core.consumer;

import org.apache.log4j.Logger;

import com.hazelcast.core.HazelcastInstance;

import br.gov.go.sefaz.clusterworker.core.listener.ShutdownListener;
import br.gov.go.sefaz.clusterworker.core.queue.QueueStrategy;
import br.gov.go.sefaz.clusterworker.core.task.TaskProcessor;

/**
 * Runnable of {@link HazelcastQueueConsumer}, responsible for process {@link TaskProcessor} client's implementation.
 * @author renato-rs
 * @since 1.0
 * @param <T> type which this runnable will handle.
 */
public final class HazelcastRunnableConsumer<T> extends HazelcastQueueConsumer<T> implements Runnable, ShutdownListener{

	private static final transient long serialVersionUID = 5404415194904610053L;
	private static final transient Logger logger = Logger.getLogger(HazelcastRunnableConsumer.class);
    
	private boolean stopped;

    private TaskProcessor<T> taskProcessor;

    /**
     * Constructor of HazelcastRunnableConsumer
     * @param taskProcessor TaskProcessor client's implementation.
     * @param hazelcastInstance instance of hazelcast.
     * @param queueName queue name
     * @param queueStrategy Consummer queue strategy
     * @param timeout Timeout of execution (in seconds) to the task processor before to return null on queue consumption.
     */
    public HazelcastRunnableConsumer(TaskProcessor<T> taskProcessor, HazelcastInstance hazelcastInstance, String queueName, QueueStrategy queueStrategy, int timeout) {
        super(hazelcastInstance, queueName, queueStrategy, timeout);
        this.taskProcessor = taskProcessor;
    }

    @Override
    public void run() {

        logger.info("Starting WorkerConsumer!");

        // Run this thread untill shutdown is called
        while(isRunning()) {

            logger.debug(String.format("Processing on the client's implementation. Strategy defined to %s.", getQueueStrategy()));

            // Consumes from hazecast queue
            T type = consume();

            //TODO: Verifies if should call process a null type
            if (type!= null){

                try{
                	// Proccess item on client's implementation
                	taskProcessor.process(type);

                }catch (Exception e){
                    logger.error("Cannot process on client's implementation!", e);
                }
            }
        }

        logger.warn("Finishing WorkerConsumer!");
    }
    
    /**
     * Verifies if this runnable is running.
     * @return <code>true</code> if thread is running, <code>false</code> otherwise.
     */
    public boolean isRunning() {
    	return !this.stopped;
    }

	@Override
	public void shutdown() {
		logger.warn(String.format("Shutting down HazelcastRunnableConsumer - Thread '%s'", Thread.currentThread().getName()));
		this.stopped = true;
	}
}