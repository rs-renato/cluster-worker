package br.gov.go.sefaz.clusterworker.core.producer;


import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceNotActiveException;
import com.hazelcast.core.Member;

import br.gov.go.sefaz.clusterworker.core.exception.ItemProducerException;
import br.gov.go.sefaz.clusterworker.core.item.ItemProducer;
import br.gov.go.sefaz.clusterworker.core.roundrobin.HazelcastMemberRoundRobin;

/**
 * Runnable of {@link HazelcastQueueProducer}, responsible for produces {@link ItemProducer} client's implementation.
 * @author renato-rs
 * @since 1.0
 * @param <T> type which this runnable will handle.
 */
public final class HazelcastRunnableProducer<T>  extends HazelcastQueueProducer<T> implements Runnable{

	private static final transient long serialVersionUID = 2538609461091747126L;

	private static final transient Logger logger = LogManager.getLogger(HazelcastRunnableProducer.class);
	
    private ItemProducer<T> itemProducer;
    
    private long producerId;

    /**
     * Constructor of HazelcastRunnableProducer
     * @param itemProducer ItemProducer client's implementation.
     * @param hazelcastInstance instance of hazelcast.
     * @param queueName queue name
     */
    public HazelcastRunnableProducer(ItemProducer<T> itemProducer, HazelcastInstance hazelcastInstance, String queueName) {
        super(hazelcastInstance, queueName);
        this.itemProducer = itemProducer;
        this.producerId = hazelcastInstance.getAtomicLong("producer.id").getAndIncrement();
    }

    @Override
    public void run() {
    	
    	// Unique roundrobin key BY HazelcastRunnableProducer
    	String roundRobinKey = String.format("roundrobin.producer.%s", producerId);
    	
    	// Get the next member 
		Member member = HazelcastMemberRoundRobin.next(hazelcastInstance, roundRobinKey);
    	boolean isLocalMember = member.localMember();
    	
    	if (isLocalMember) {
    		
    		try{
    			
   				Collection<T> items = itemProducer.produce();
    				
				if (items!= null){
					produce(items);
				}
				
            } catch (InterruptedException|HazelcastInstanceNotActiveException e) {
				logger.error(String.format("Cannot produce to hazelcast %s queue! This thread will die! Reason: %s", queueName, e.getMessage()));
				Thread.currentThread().interrupt();
    		}catch (ItemProducerException e){
    			logger.error(String.format("Cannot produce on client's implementation! Error: %s", e.getMessage()));
    		 }catch (Exception e){
     			logger.error("A general error occurs process on HazelcastRunnableProducer", e);
             }
		}
    	
		logger.debug(String.format("HazelcastRunnableProducer execution %s on member: '%s'.", (isLocalMember ? "completed" : "ignored"), member.getUuid()));
    }
}
