package br.com.clusterworker.core;

import java.io.Serializable;

import org.apache.log4j.Logger;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IQueue;

import br.com.clusterworker.annotations.BaseConsumerConfig;
import br.com.clusterworker.consumer.Consumer;
import br.com.clusterworker.lock.AtomicLock;
import br.com.clusterworker.lock.LockType;
import br.com.clusterworker.utils.ClusterWorkerUtils;
import br.com.clusterworker.utils.HazelcastUtils;
import br.com.clusterworker.utils.QueueStrategy;

/**
 * Base class for {@link Consumer} implementation.
 * @param <T> type of this base consumer.
 */
public class BaseConsumer<T> implements Consumer<T>, Serializable{

    private static final transient Logger logger = Logger.getLogger(BaseConsumer.class);

    transient HazelcastInstance hazelcastInstance = HazelcastUtils.getInstance().getHazelcastInstance();

    private String queueName;
    private QueueStrategy queueStrategy;
    private int timeout;

    AtomicLock atomicLock;

    /**
     * Creates an BaseConsumer instance. This constructor requires an {@link BaseConsumerConfig} annotation.
     */
    public BaseConsumer() {
        BaseConsumerConfig baseConsumerConfig = ClusterWorkerUtils.verifyMandatoryAnotation(this, BaseConsumerConfig.class);
        init(baseConsumerConfig.queueName(), baseConsumerConfig.strategy(), baseConsumerConfig.timeout());
    }

    public BaseConsumer(String queueName, QueueStrategy queueStrategy, int timeout) {
        init(queueName, queueStrategy, timeout);
    }

    private void init(String queueName, QueueStrategy queueStrategy, int timeout) {
        this.queueName = queueName;
        this.queueStrategy = queueStrategy;
        this.timeout = timeout;
        this.atomicLock = new AtomicLock(hazelcastInstance, queueName, LockType.CONSUMER);
    }

    @Override
    public T consume() {

        try {

            IQueue<T> iQueue = hazelcastInstance.getQueue(queueName);

            //Waits on take() only if strategy is {@link QueueStrategy#WAIT_ON_AVAILABLE}.
            Thread.sleep(QueueStrategy.ACCEPT_NULL.equals(queueStrategy) && iQueue.isEmpty() ? timeout * 1000 : 0);

            T type = QueueStrategy.ACCEPT_NULL.equals(queueStrategy) ? iQueue.poll() : iQueue.take();

            logger.debug(String.format("Consume type %s from hazelcast queue.", type));

            return type;

        } catch (InterruptedException e) {
            logger.error(String.format("Cannot consume the hazelcast %s queue!", queueName), e);
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
    public QueueStrategy getQueueStrategy() {
        return queueStrategy;
    }

    /**
     * Return the timeout of this base consumer.
     * @return timeout
     */
    public int getTimeout() {
        return timeout;
    }

    /**
     * Locks the atomic lock of this base consumer.
     * @return atomicLock
     */
    public void lock() {
        atomicLock.lock();
    }

    /**
     * Unlocks the atomic lock of this base consumer.
     * @return atomicLock
     */
    public void unlock() {
        atomicLock.unlock();
    }

    public void shutdown(){
        if (hazelcastInstance.getLifecycleService().isRunning()){
            hazelcastInstance.shutdown();
        }
    }
}
