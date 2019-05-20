package br.gov.go.sefaz.clusterworker.core.consumer;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;
import com.hazelcast.core.IQueue;

/**
 * Implementation for Hazelcast Queue Consumer.
 * @author renato-rs
 * @since 1.0.0
 * @param <T> type of data to be consummed by this queue.
 */
public class HazelcastQueueConsumer<T> implements Consumer<T>, Serializable, HazelcastInstanceAware{

	private static final transient Logger logger = LogManager.getLogger(HazelcastQueueConsumer.class);

	private static final transient long serialVersionUID = 4384549432295630459L;

    protected transient HazelcastInstance hazelcastInstance;

    protected String queueName;
    private ConsumerStrategy consumerStrategy = ConsumerStrategy.ACCEPT_NULL;
    private boolean isBlocking = false;
    private int timeout = 1;
    

    /**
     * Constructor for HazelcastQueueConsumer
     * @param hazelcastInstance instance of hazelcast.
     * @param queueName queue name
     * @since 1.0.0
     */
    public HazelcastQueueConsumer(HazelcastInstance hazelcastInstance, String queueName) {
    	this.hazelcastInstance = hazelcastInstance;
		this.queueName = queueName;
		this.isBlocking = ConsumerStrategy.WAIT_ON_AVAILABLE.equals(consumerStrategy);
	}
    
    /**
     * Constructor for HazelcastQueueConsumer
     * @param hazelcastInstance instance of hazelcast.
     * @param queueName queue name
     * @param consumerStrategy Consumer queue strategy
     * @since 1.0.0
     */
	public HazelcastQueueConsumer(HazelcastInstance hazelcastInstance, String queueName, ConsumerStrategy consumerStrategy) {
		this.hazelcastInstance = hazelcastInstance;
		this.queueName = queueName;
		this.consumerStrategy = consumerStrategy;
		this.isBlocking = ConsumerStrategy.WAIT_ON_AVAILABLE.equals(consumerStrategy);
	}
    
	/**
	 * Constructor for HazelcastQueueConsumer
	 * @param hazelcastInstance instance of hazelcast.
	 * @param queueName queue name
	 * @param consumerStrategy Consumer queue strategy
	 * @param timeout Timeout of execution (in seconds) to the item processor before to return null on queue consumption.
	 * @since 1.0.0
	 */
	public HazelcastQueueConsumer(HazelcastInstance hazelcastInstance, String queueName, ConsumerStrategy consumerStrategy, int timeout) {
		this.hazelcastInstance = hazelcastInstance;
		this.queueName = queueName;
		this.consumerStrategy = consumerStrategy;
		this.timeout = timeout;
		this.isBlocking = ConsumerStrategy.WAIT_ON_AVAILABLE.equals(consumerStrategy);
	}

    @Override
    public T consume() throws InterruptedException {

    	// Return the hazelcast distributed queue
        IQueue<T> iQueue = hazelcastInstance.getQueue(queueName);

        logger.trace(String.format("Trying to consume item from hazelcast '%s' queue. Is Blocking: %s - Timeout (case of non-blocking): %s seconds", queueName, isBlocking(), timeout));
        
        // Blocking on take() only if strategy is {@link QueueStrategy#WAIT_ON_AVAILABLE}.
        // Otherwise, wait until timeout and return null if there is no item to process.
		T item = isBlocking() ? iQueue.take() : iQueue.poll(timeout, TimeUnit.SECONDS);

        logger.trace(String.format("Consumed item '%s' from hazelcast queue.", item));

        return item;
    }
    
    /**
     * Verifies if this consumer has blocking strategy
     * @see ConsumerStrategy
     * @return <code>true</code> if is blocking, <code>false</code> otherwise
     * @since 1.0.0
     */
    public boolean isBlocking() {
		return isBlocking;
	}

    /**
     * Return the queue's name of this consumer.
     * @return queueName
     * @since 1.0.0
     */
    public String getQueueName() {
        return queueName;
    }

    /**
     * Return the queueStrategy of this consumer.
     * @return queueStrategy
     * @since 1.0.0
     */
    public ConsumerStrategy getQueueStrategy() {
        return consumerStrategy;
    }

    /**
     * Return the timeout of this consumer.
     * @return timeout
     * @since 1.0.0
     */
    public int getTimeout() {
        return timeout;
    }
    
    @Override
    public void setHazelcastInstance(HazelcastInstance hazelcastInstance) {
        this.hazelcastInstance = hazelcastInstance;
    }

	@Override
	public String toString() {
		return "HazelcastQueueConsumer [queueName=" + queueName + ", consumerStrategy=" + consumerStrategy + ", isBlocking="
				+ isBlocking + ", timeout=" + timeout + "]";
	}
}
