package br.gov.go.sefaz.clusterworker.core.consumer;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;
import com.hazelcast.core.IQueue;

import br.gov.go.sefaz.clusterworker.core.queue.QueueStrategy;

/**
 * Implementation for Hazelcast Queue Consumer.
 * @author renato-rs
 * @since 1.0
 * @param <T> type of data to be consummed by this queue.
 */
public class HazelcastQueueConsumer<T> implements Consumer<T>, Serializable, HazelcastInstanceAware{

	private static final transient Logger logger = Logger.getLogger(HazelcastQueueConsumer.class);

	private static final transient long serialVersionUID = 4384549432295630459L;

    private transient HazelcastInstance hazelcastInstance;

    private String queueName;
    private QueueStrategy queueStrategy = QueueStrategy.ACCEPT_NULL;
    private boolean isBlocking = false;
    private int timeout = 1;
    

    /**
     * Constructor for HazelcastQueueConsumer
     * @param hazelcastInstance instance of hazelcast.
     * @param queueName queue name
     */
    public HazelcastQueueConsumer(HazelcastInstance hazelcastInstance, String queueName) {
    	this.hazelcastInstance = hazelcastInstance;
		this.queueName = queueName;
		this.isBlocking = QueueStrategy.WAIT_ON_AVAILABLE.equals(queueStrategy);
	}
    
    /**
     * Constructor for HazelcastQueueConsumer
     * @param hazelcastInstance instance of hazelcast.
     * @param queueName queue name
     * @param queueStrategy Consummer queue strategy
     */
	public HazelcastQueueConsumer(HazelcastInstance hazelcastInstance, String queueName, QueueStrategy queueStrategy) {
		this.hazelcastInstance = hazelcastInstance;
		this.queueName = queueName;
		this.queueStrategy = queueStrategy;
		this.isBlocking = QueueStrategy.WAIT_ON_AVAILABLE.equals(queueStrategy);
	}
    
	/**
	 * Constructor for HazelcastQueueConsumer
	 * @param hazelcastInstance instance of hazelcast.
	 * @param queueName queue name
	 * @param queueStrategy Consummer queue strategy
	 * @param timeout Timeout of execution (in seconds) to the item processor before to return null on queue consumption.
	 */
	public HazelcastQueueConsumer(HazelcastInstance hazelcastInstance, String queueName, QueueStrategy queueStrategy, int timeout) {
		this.hazelcastInstance = hazelcastInstance;
		this.queueName = queueName;
		this.queueStrategy = queueStrategy;
		this.timeout = timeout;
		this.isBlocking = QueueStrategy.WAIT_ON_AVAILABLE.equals(queueStrategy);
	}

    @Override
    public T consume() {

        try {

        	// Return the hazelcast distributed queue
            IQueue<T> iQueue = hazelcastInstance.getQueue(queueName);

            logger.debug(String.format("Trying to consume item from hazelcast %s queue. Is Blocking: %s - Timeout (case of non-blocking): %s", queueName, isBlocking(), timeout));
            
            // Blocking on take() only if strategy is {@link QueueStrategy#WAIT_ON_AVAILABLE}.
            // Otherwise, wait until timeout and return null if there is no item to process.
			T item = isBlocking() ? iQueue.take() : iQueue.poll(timeout, TimeUnit.SECONDS);

            logger.debug(String.format("Consumed item %s from hazelcast queue.", item));

            return item;

        } catch (InterruptedException e) {
            logger.error(String.format("Cannot consume from hazelcast %s queue!", queueName), e);
            Thread.currentThread().interrupt();
        }

        return null;
    }
    
    /**
     * Verifies if this consumer has blocking strategy
     * @see QueueStrategy
     * @return
     */
    public boolean isBlocking() {
		return isBlocking;
	}

    /**
     * Return the queue's name of this consumer.
     * @return queueName
     */
    public String getQueueName() {
        return queueName;
    }

    /**
     * Return the queueStrategy of this consumer.
     * @return queueStrategy
     */
    public QueueStrategy getQueueStrategy() {
        return queueStrategy;
    }

    /**
     * Return the timeout of this consumer.
     * @return timeout
     */
    public int getTimeout() {
        return timeout;
    }
    
    @Override
    public void setHazelcastInstance(HazelcastInstance hazelcastInstance) {
        this.hazelcastInstance = hazelcastInstance;
    }
}
