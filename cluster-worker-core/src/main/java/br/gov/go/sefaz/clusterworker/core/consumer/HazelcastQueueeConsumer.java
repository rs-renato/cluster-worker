package br.gov.go.sefaz.clusterworker.core.consumer;

import java.io.Serializable;

import org.apache.log4j.Logger;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;
import com.hazelcast.core.IQueue;

/**
 * Base class for {@link Consumer} implementation.
 * @param <T> type of this base consumer.
 */
public class HazelcastQueueeConsumer<T> implements Consumer<T>, Serializable, HazelcastInstanceAware{

	private static final transient Logger logger = Logger.getLogger(HazelcastQueueeConsumer.class);

	private static final long serialVersionUID = 4384549432295630459L;

    protected transient HazelcastInstance hazelcastInstance;

    private String queueName;
    private ConsumerStrategy consumerStrategy = ConsumerStrategy.ACCEPT_NULL;
    private int timeout = 1;

    public HazelcastQueueeConsumer(HazelcastInstance hazelcastInstance, String queueName) {
    	this.hazelcastInstance = hazelcastInstance;
		this.queueName = queueName;
	}
    
	public HazelcastQueueeConsumer(HazelcastInstance hazelcastInstance, String queueName, ConsumerStrategy consumerStrategy) {
		this.hazelcastInstance = hazelcastInstance;
		this.queueName = queueName;
		this.consumerStrategy = consumerStrategy;
	}
    
	public HazelcastQueueeConsumer(HazelcastInstance hazelcastInstance, String queueName, ConsumerStrategy consumerStrategy, int timeout) {
		this.hazelcastInstance = hazelcastInstance;
		this.queueName = queueName;
		this.consumerStrategy = consumerStrategy;
		this.timeout = timeout;
	}

    @Override
    public T consume() {

        try {

            IQueue<T> iQueue = hazelcastInstance.getQueue(queueName);

            //Waits on take() only if strategy is {@link QueueStrategy#WAIT_ON_AVAILABLE}.
            Thread.sleep(ConsumerStrategy.ACCEPT_NULL.equals(consumerStrategy) && iQueue.isEmpty() ? timeout * 1000 : 0);

            T type = ConsumerStrategy.ACCEPT_NULL.equals(consumerStrategy) ? iQueue.poll() : iQueue.take();

            logger.debug(String.format("Consume type %s from hazelcast queue.", type));

            return type;

        } catch (InterruptedException e) {
            logger.error(String.format("Cannot consume the hazelcast %s queue!", queueName), e);
            Thread.currentThread().interrupt();
        }

        return null;
    }

    /**
     * Return the queue's name of this base consumer.
     * @return queueName
     */
    public String getQueueName() {
        return queueName;
    }

    /**
     * Return the queueStrategy of this base consumer.
     * @return queueStrategy
     */
    public ConsumerStrategy getQueueStrategy() {
        return consumerStrategy;
    }

    /**
     * Return the timeout of this base consumer.
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
