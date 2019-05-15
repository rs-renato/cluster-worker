package br.gov.go.sefaz.clusterworker.core.consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceNotActiveException;

import br.gov.go.sefaz.clusterworker.core.exception.ItemProcessorException;
import br.gov.go.sefaz.clusterworker.core.item.ItemProcessor;
import br.gov.go.sefaz.clusterworker.core.listener.ShutdownListener;

/**
 * Runnable of {@link HazelcastQueueConsumer}, responsible for process {@link ItemProcessor} client's implementation.
 * @author renato-rs
 * @since 1.0
 * @param <T> type which this runnable will handle.
 */
public final class HazelcastRunnableConsumer<T> extends HazelcastQueueConsumer<T> implements Runnable, ShutdownListener{

	private static final transient long serialVersionUID = 5404415194904610053L;
	private static final transient Logger logger = LogManager.getLogger(HazelcastRunnableConsumer.class);
    
	private volatile boolean stopped;

    private ItemProcessor<T> itemProcessor;

    /**
     * Constructor of HazelcastRunnableConsumer
     * @param itemProcessor ItemProcessor client's implementation.
     * @param hazelcastInstance instance of hazelcast.
     * @param queueName queue name
     * @param consumerStrategy Consumer queue strategy
     * @param timeout Timeout of execution (in seconds) to the item processor before to return null on queue consumption.
     */
    public HazelcastRunnableConsumer(ItemProcessor<T> itemProcessor, HazelcastInstance hazelcastInstance, String queueName, ConsumerStrategy consumerStrategy, int timeout) {
        super(hazelcastInstance, queueName, consumerStrategy, timeout);
        this.itemProcessor = itemProcessor;
    }

    @Override
    public void run() {

		String consumerThreadName = getRunnableConsumerdName();

		logger.info(String.format("Starting thread '%s'..", consumerThreadName));

        // Run this thread untill shutdown is called
        while(isRunning()) {

            try {
            	// Consumes from hazelcast queue
            	T item = consume();
            	
            	// Process the item if it exists, or if the strategy is non-blocking, independently if the item exists 
                if (item != null || !isBlocking()){
                	logger.trace(String.format("Processing item (%s) on the client's implementation. Strategy defined to %s.", item, getQueueStrategy()));
                	itemProcessor.process(item);
                }
                
            } catch (InterruptedException|HazelcastInstanceNotActiveException e) {
                logger.error(String.format("Cannot consume from hazelcast '%s' queue! This thread will die! Reason: %s", queueName, e));
                shutdown();
                Thread.currentThread().interrupt();
            }catch (ItemProcessorException e){
    			logger.error(String.format("Cannot process on client's implementation! Reason: %s", e.getMessage()));
            }catch (Exception e){
    			logger.error(String.format("A general error occurs process on '%s'", consumerThreadName), e);
            }
        }

        logger.warn(String.format("Thread execution '%s' FINISHED on this member..", consumerThreadName));
    }
    
	@Override
	public void shutdown() {
		logger.warn(String.format("Shutting down '%s'..", Thread.currentThread().getName()));
		this.stopped = true;
	}
	
    /**
     * Verifies if this runnable is running.
     * @return <code>true</code> if thread is running, <code>false</code> otherwise.
     */
    public boolean isRunning() {
    	return !this.stopped;
    }

    /**
     * Builds a unique thread name for this consumer
     * @return the unique consumer thread name
     */
    @SuppressWarnings("deprecation")
	private String getRunnableConsumerdName() {
    	String threadName = String.format("%s.consumer[%s]-", hazelcastInstance.getName(), itemProcessor.getClass().getSimpleName());
		long threadCount = hazelcastInstance.getAtomicLong(threadName).getAndIncrement();
		return threadName + threadCount;
    }
}