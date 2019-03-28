package br.gov.go.sefaz.clusterworker.core.producer;

import java.io.Serializable;
import java.util.Collection;

import org.apache.log4j.Logger;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;
import com.hazelcast.core.IQueue;

/**
 * Implementation for Hazelcast Queue Produces.
 * @author renato-rs
 * @since 1.0
 * @param <T> type of data to be produced by this queue.
 */
public class HazelcastQueueProducer<T> implements Producer<T>, Serializable, HazelcastInstanceAware {

	private static final transient long serialVersionUID = -3706506746207926465L;
	private static final transient Logger logger = Logger.getLogger(HazelcastQueueProducer.class);

    private transient HazelcastInstance hazelcastInstance;
  
    private String queueName;

    /**
     * Constructor for HazelcastQueueProducer
     * @param hazelcastInstance instance of hazelcast.
     * @param queueName queue name
     */
    public HazelcastQueueProducer(HazelcastInstance hazelcastInstance, String queueName) {
    	this.hazelcastInstance = hazelcastInstance;
    	this.queueName = queueName;
    }

    @Override
    public void produce(Collection<T> items) {

    	logger.debug(String.format("Producing %s items to %s queue.", items.size(), queueName));
    	
    	//Return the hazelcast distributed queue
        IQueue<T> iQueue = hazelcastInstance.getQueue(queueName);

        for (T item : items) {

            try {
                //Put a new item to the hazelcast queue
                iQueue.put(item);
            } catch (InterruptedException e) {
                logger.error(String.format("Cannot produce to hazelcast %s queue!", queueName), e);
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Return the queue's name of this producer.
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
