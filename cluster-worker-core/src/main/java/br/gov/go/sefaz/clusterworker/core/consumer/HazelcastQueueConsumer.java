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

	private static final long serialVersionUID = 4384549432295630459L;

    private transient HazelcastInstance hazelcastInstance;

    private String queueName;
    private QueueStrategy queueStrategy = QueueStrategy.ACCEPT_NULL;
    private int timeout = 1;

    /**
     * Constructor for HazelcastQueueConsumer
     * @param hazelcastInstance instance of hazelcast.
     * @param queueName queue name
     */
    public HazelcastQueueConsumer(HazelcastInstance hazelcastInstance, String queueName) {
    	this.hazelcastInstance = hazelcastInstance;
		this.queueName = queueName;
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
	}
    
	/**
	 * Constructor for HazelcastQueueConsumer
	 * @param hazelcastInstance instance of hazelcast.
	 * @param queueName queue name
	 * @param queueStrategy Consummer queue strategy
	 * @param timeout Timeout of execution (in seconds) to the task processor before to return null on queue consumption.
	 */
	public HazelcastQueueConsumer(HazelcastInstance hazelcastInstance, String queueName, QueueStrategy queueStrategy, int timeout) {
		this.hazelcastInstance = hazelcastInstance;
		this.queueName = queueName;
		this.queueStrategy = queueStrategy;
		this.timeout = timeout;
	}

    @Override
    public T consume() {

        try {

        	//Return the hazelcast distributed queue
            IQueue<T> iQueue = hazelcastInstance.getQueue(queueName);

            //Blocking on take() only if strategy is {@link QueueStrategy#WAIT_ON_AVAILABLE}, otherwise, wait until timeout.
            boolean isNonBlocking = QueueStrategy.ACCEPT_NULL.equals(queueStrategy);
            
			T type = isNonBlocking ? iQueue.poll(timeout, TimeUnit.SECONDS) : iQueue.take();

            logger.debug(String.format("Consume type %s from hazelcast queue.", type));

            return type;

        } catch (InterruptedException e) {
            logger.error(String.format("Cannot consume the hazelcast %s queue!", queueName), e);
            Thread.currentThread().interrupt();
        }

        return null;
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
