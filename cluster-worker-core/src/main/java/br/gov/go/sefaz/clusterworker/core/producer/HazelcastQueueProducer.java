package br.gov.go.sefaz.clusterworker.core.producer;

import java.io.Serializable;
import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
	private static final transient Logger logger = LogManager.getLogger(HazelcastQueueProducer.class);

    protected transient HazelcastInstance hazelcastInstance;
  
    protected String queueName;

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
    public void produce(Collection<T> items) throws InterruptedException {

		//Return the hazelcast distributed queue
		IQueue<T> iQueue = hazelcastInstance.getQueue(queueName);

		logger.debug(String.format("Producing %s items to %s queue. The queue has %s itens", items.size(), queueName, iQueue.size()));
		
		//Put a new item to the hazelcast queue
		for (T item : items) {
			iQueue.put(item);
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

	@Override
	public String toString() {
		return "HazelcastQueueProducer [queueName=" + queueName + "]";
	}
}
