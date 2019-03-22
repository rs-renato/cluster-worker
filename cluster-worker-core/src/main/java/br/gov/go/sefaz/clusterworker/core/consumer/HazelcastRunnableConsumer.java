package br.gov.go.sefaz.clusterworker.core.consumer;

import org.apache.log4j.Logger;

import com.hazelcast.core.HazelcastInstance;

import br.gov.go.sefaz.clusterworker.core.ClusterWorker;
import br.gov.go.sefaz.clusterworker.core.listener.ShutdownListener;
import br.gov.go.sefaz.clusterworker.core.task.TaskProcessor;

/**
 * Worker consumer implementation. This class consume from {@link TaskProcessor} client's implementation.
 * The role cycle of this core is controled by {@link ClusterWorker}.
 * @param <T> Type of this core consumer.
 */
public final class HazelcastRunnableConsumer<T> extends HazelcastQueueConsumer<T> implements Runnable, ShutdownListener{

	private static final long serialVersionUID = 5404415194904610053L;
	private static final transient Logger logger = Logger.getLogger(HazelcastRunnableConsumer.class);
    
	private boolean stopped;

    private TaskProcessor<T> taskProcessor;

    public HazelcastRunnableConsumer(TaskProcessor<T> taskProcessor, HazelcastInstance hazelcastInstance, String queueName, ConsumerStrategy consumerStrategy, int timeout) {
        super(hazelcastInstance, queueName, consumerStrategy, timeout);
        this.taskProcessor = taskProcessor;
    }

    @Override
    public void run() {

        logger.info("Starting WorkerConsumer!");

        while(isRunning()) {

            logger.debug(String.format("Processing on the client's implementation. Strategy defined to %s.", getQueueStrategy()));

            T type = consume();

            if (type!= null){

                try{

                	taskProcessor.process(type);

                }catch (Exception e){
                    logger.error("Cannot process on client's implementation!", e);
                }
            }
        }

        logger.warn("Finishing WorkerConsumer!");
    }
    
    public boolean isRunning() {
    	return !this.stopped;
    }

	@Override
	public void shutdown() {
		logger.warn(String.format("Shutting down HazelcastRunnableConsumer - Thread '%s'", Thread.currentThread().getName()));
		this.stopped = true;
	}
}