package br.gov.go.sefaz.clusterworker.core.producer;

import java.io.Serializable;
import java.util.Collection;

import org.apache.log4j.Logger;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;
import com.hazelcast.core.IQueue;

import br.gov.go.sefaz.clusterworker.core.queue.HazelcastQueueNameRoundRobin;

/**
 * Implementation for Hazelcast Queue Produces.
 * @author renato-rs
 * @since 1.0
 * @param <T> type of data to be produced by this queue.
 */
public class HazelcastQueueProducer<T> implements Producer<T>, Serializable, HazelcastInstanceAware {

	private static final long serialVersionUID = -3706506746207926465L;
	private static final transient Logger logger = Logger.getLogger(HazelcastQueueProducer.class);
	private HazelcastQueueNameRoundRobin hazelcastQueueNameRoundRobin;

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
    	this.hazelcastQueueNameRoundRobin = new HazelcastQueueNameRoundRobin(queueName);
    }

    @Override
    public void produce(Collection<T> types) {

    	//Return the next hazelcast distributed queue name
    	String queueName = this.hazelcastQueueNameRoundRobin.nextQueue();

    	logger.debug(String.format("Requested the next queue name from round robin: %s. Ignored if the queue is empty", queueName));
    	
        IQueue<T> iQueue = hazelcastInstance.getQueue(queueName);

        for (T type : types) {

            try {
                logger.debug(String.format("Producing type %s to %s queue on base base producer.", type, queueName));
                //Put a new item to the hazelcast queue
                iQueue.put(type);
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
