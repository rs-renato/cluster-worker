package br.gov.go.sefaz.clusterworker.core.consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceNotActiveException;

import br.gov.go.sefaz.clusterworker.core.exception.ItemProcessorException;
import br.gov.go.sefaz.clusterworker.core.item.ItemProcessor;

import java.util.concurrent.Callable;

/**
 * Callable of {@link HazelcastQueueConsumer}, responsible for process {@link ItemProcessor} client's implementation.
 * @author renato.rsilva
 * @since 1.0.0
 * @param <T> type which this callable will handle.
 */
public final class HazelcastCallableConsumer<T> extends HazelcastQueueConsumer<T> implements Callable<Void>{

	private static final transient long serialVersionUID = 5404415194904610053L;
	private static final transient Logger logger = LogManager.getLogger(HazelcastCallableConsumer.class);

    private ItemProcessor<T> itemProcessor;

    /**
     * Constructor of HazelcastCallableConsumer
     * @param itemProcessor ItemProcessor client's implementation.
     * @param hazelcastInstance instance of hazelcast.
     * @param queueName queue name
     * @param consumerStrategy Consumer queue strategy
     * @param timeout Timeout of execution (in seconds) to the item processor before to return null on queue consumption.
     * @since 1.0.0
     */
    public HazelcastCallableConsumer(ItemProcessor<T> itemProcessor, HazelcastInstance hazelcastInstance, String queueName, ConsumerStrategy consumerStrategy, int timeout) {
        super(hazelcastInstance, queueName, consumerStrategy, timeout);
        this.itemProcessor = itemProcessor;
    }

    @Override
    public Void call() {

		String consumerThreadName = getCallableConsumerdName();

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
                Thread.currentThread().interrupt();
            }catch (ItemProcessorException e){
    			logger.error(String.format("Cannot process on client's implementation! Reason: %s", e.getMessage()));
            }catch (Exception e){
    			logger.error(String.format("A general error occurs process on '%s'", consumerThreadName), e);
            }
        }

        logger.warn(String.format("Thread execution '%s' FINISHED!", consumerThreadName));
        return null;
    }
	
    /**
     * Verifies if this callable is running.
     * @return <code>true</code> if thread is running, <code>false</code> otherwise.
     * @since 1.0.0
     */
    private boolean isRunning() {
    	return !Thread.currentThread().isInterrupted();
    }

    /**
     * Builds a unique thread name for this consumer
     * @return the unique consumer thread name
     * @since 1.0.0
     */
    @SuppressWarnings("deprecation")
	private String getCallableConsumerdName() {
    	String threadName = String.format("%s.consumer[%s]-", hazelcastInstance.getName(), itemProcessor.getClass().getSimpleName());
		long threadCount = hazelcastInstance.getAtomicLong(threadName).getAndIncrement();
		return threadName + threadCount;
    }
}