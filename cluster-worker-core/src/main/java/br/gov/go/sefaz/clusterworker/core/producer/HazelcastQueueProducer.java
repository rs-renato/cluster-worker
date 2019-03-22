package br.gov.go.sefaz.clusterworker.core.producer;

import java.io.Serializable;
import java.util.Collection;

import org.apache.log4j.Logger;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;
import com.hazelcast.core.IQueue;

/**
 * Base class for {@Producer} implementation.
 * @param <T> type of this base producer.
 */
public class HazelcastQueueProducer<T> implements Producer<T>, Serializable, HazelcastInstanceAware {

	private static final long serialVersionUID = -3706506746207926465L;
	private static final transient Logger logger = Logger.getLogger(HazelcastQueueProducer.class);

    protected transient HazelcastInstance hazelcastInstance;
    private String queueName;

    public HazelcastQueueProducer(HazelcastInstance hazelcastInstance, String queueName) {
    	this.hazelcastInstance = hazelcastInstance;
    	this.queueName = queueName;
    }

    @Override
    public void produce(Collection<T> types) {

        IQueue<T> iQueue = hazelcastInstance.getQueue(queueName);

        for (T type : types) {

            try {

                logger.debug(String.format("Producing type %s to %s queue on base base producer.", type, queueName));

                iQueue.put(type);

            } catch (InterruptedException e) {
                logger.error(String.format("Cannot produce to hazelcast %s queue!", queueName), e);
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Return the queueName of this base producer
     *
     * @return queueName
     */
    public String getQueueName() {
        return queueName;
    }
    
    @Override
    public void setHazelcastInstance(HazelcastInstance hazelcastInstance) {
        this.hazelcastInstance = hazelcastInstance;
    }
}
