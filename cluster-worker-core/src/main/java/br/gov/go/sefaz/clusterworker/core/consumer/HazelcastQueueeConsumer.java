package br.gov.go.sefaz.clusterworker.core.consumer;

import java.io.Serializable;

import org.apache.log4j.Logger;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IQueue;

import br.gov.go.sefaz.clusterworker.core.utils.HazelcastUtils;
import br.gov.go.sefaz.clusterworker.core.utils.QueueStrategy;

/**
 * Base class for {@link Consumer} implementation.
 * @param <T> type of this base consumer.
 */
public class HazelcastQueueeConsumer<T> implements Consumer<T>, Serializable{

	private static final transient Logger logger = Logger.getLogger(HazelcastQueueeConsumer.class);

	private static final long serialVersionUID = 4384549432295630459L;

    protected transient HazelcastInstance hazelcastInstance = HazelcastUtils.getInstance().getHazelcastInstance();

    private String queueName;
    private QueueStrategy queueStrategy = QueueStrategy.ACCEPT_NULL;
    private int timeout = 1;

    public HazelcastQueueeConsumer(String queueName) {
		this.queueName = queueName;
	}
    
	public HazelcastQueueeConsumer(String queueName, QueueStrategy queueStrategy) {
		this.queueName = queueName;
		this.queueStrategy = queueStrategy;
	}
    
	public HazelcastQueueeConsumer(String queueName, QueueStrategy queueStrategy, int timeout) {
		this.queueName = queueName;
		this.queueStrategy = queueStrategy;
		this.timeout = timeout;
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

    public void shutdown(){
        if (hazelcastInstance.getLifecycleService().isRunning()){
            hazelcastInstance.shutdown();
        }
    }
}
