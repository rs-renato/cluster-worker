package br.gov.go.sefaz.clusterworker.core.consumer;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;
import com.hazelcast.core.IQueue;

import br.gov.go.sefaz.clusterworker.core.constants.ClusterWorkerConstants;

/**
 * Implementation for Hazelcast Queue Consumer.
 * <br><br> Note: The default strategy configuration for this consumer is <code>non-blocking</code>
 * @author renato.rsilva
 * @param <T> type of data to be consummed by this queue.
 * @since 1.0.0
 * @see {@link ConsumerStrategy#ACCEPT_NULL}
 */
public class HazelcastQueueConsumer<T> implements Consumer<T>, Serializable, HazelcastInstanceAware{

	private static final transient Logger logger = LogManager.getLogger(HazelcastQueueConsumer.class);

	private static final transient long serialVersionUID = 4384549432295630459L;

    protected transient HazelcastInstance hazelcastInstance;

    protected String queueName;
    private ConsumerStrategy consumerStrategy = ClusterWorkerConstants.CW_QUEUE_CONSUMER_STRATEGY;
    private int timeout = ClusterWorkerConstants.CW_QUEUE_TIMEOUT;
    private TimeUnit timeUnit = ClusterWorkerConstants.CW_QUEUE_TIMEOUT_TIMEUNIT;
    private boolean isBlocking;
    
    /**
     * Constructor for HazelcastQueueConsumer
     * @param hazelcastInstance instance of hazelcast.
     * @param queueName queue name
     * @since 1.0.0
     */
    public HazelcastQueueConsumer(HazelcastInstance hazelcastInstance, String queueName) {
    	this.hazelcastInstance = hazelcastInstance;
		this.queueName = queueName;
	}
    
	/**
	 * Constructor for HazelcastQueueConsumer
	 * @param hazelcastInstance instance of hazelcast.
	 * @param queueName queue name
	 * @param consumerStrategy Consumer queue strategy
	 * @param timeout Timeout of execution to the item processor before to return null on queue consumption.
	 * @param timeUnit Time duration of timeout
	 * @since 1.0.0
	 */
	public HazelcastQueueConsumer(HazelcastInstance hazelcastInstance, String queueName, ConsumerStrategy consumerStrategy, int timeout, TimeUnit timeUnit) {
		this.hazelcastInstance = hazelcastInstance;
		this.queueName = queueName;
		this.consumerStrategy = consumerStrategy;
		this.timeout = timeout;
		this.timeUnit = timeUnit;
		this.isBlocking = ConsumerStrategy.WAIT_ON_AVAILABLE.equals(consumerStrategy);
	}

    @Override
    public T consume() throws InterruptedException {

    	// Return the hazelcast distributed queue
        IQueue<T> iQueue = hazelcastInstance.getQueue(queueName);

        logger.trace(String.format("Trying to consume item from hazelcast '%s' queue. Is Blocking: %s - Timeout (case of non-blocking): '%s %s'", queueName, isBlocking(), timeout, timeUnit));
        
        // Blocking on take() only if strategy is {@link QueueStrategy#WAIT_ON_AVAILABLE}.
        // Otherwise, wait until timeout and return null if there is no item to process.
		T item = isBlocking() ? iQueue.take() : iQueue.poll(timeout, timeUnit);

        logger.debug(String.format("Consumed item '%s' from hazelcast queue.", item));

        return item;
    }
    
    /**
     * Verifies if this consumer has blocking strategy
     * @return <code>true</code> if is blocking, <code>false</code> otherwise
     * @since 1.0.0
     * @see ConsumerStrategy
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
				+ isBlocking + ", timeout=" + timeout + ", timeUnit=" + timeUnit + "]";
	}
}
