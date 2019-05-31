package org.com.clusterworker.core.consumer;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.com.clusterworker.core.exception.ItemProcessorException;
import org.com.clusterworker.core.item.ItemProcessor;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceNotActiveException;

/**
 * Callable of {@link HazelcastQueueConsumer}, responsible for process {@link ItemProcessor} client's implementation.
 * @author rs-renato
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
     * @param timeout Timeout of execution to the item processor before to return null on queue consumption.
     * @param timeUnit Time duration of timeout
     * @since 1.0.0
     */
    public HazelcastCallableConsumer(ItemProcessor<T> itemProcessor, HazelcastInstance hazelcastInstance, String queueName, ConsumerStrategy consumerStrategy, int timeout, TimeUnit timeUnit) {
        super(hazelcastInstance, queueName, consumerStrategy, timeout, timeUnit);
        this.itemProcessor = itemProcessor;
    }

    @Override
    public Void call() {

		String consumerThreadName = getCallableConsumerdName();

		logger.info(String.format("[%s] - Starting thread '%s'..", Thread.currentThread().getName(),  consumerThreadName));
		
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
    			logger.error("Cannot process on client's implementation!", e);
            }catch (Exception e){
    			logger.error(String.format("A general error occurs process on '%s'", consumerThreadName), e);
            }
        }

        logger.warn(String.format("[%s] - Thread execution '%s' FINISHED!", Thread.currentThread().getName(), consumerThreadName));
        
    	// This Callable<Void>
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
	public String getCallableConsumerdName() {
    	String threadName = String.format("%s.consumer[%s]-", hazelcastInstance.getName(), itemProcessor.getClass().getSimpleName());
		long threadCount = hazelcastInstance.getAtomicLong(threadName).getAndIncrement();
		return threadName + threadCount;
    }
}