package br.gov.go.sefaz.clusterworker.core.producer;


import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceNotActiveException;
import com.hazelcast.core.IQueue;
import com.hazelcast.core.Member;

import br.gov.go.sefaz.clusterworker.core.exception.ItemProducerException;
import br.gov.go.sefaz.clusterworker.core.item.ItemProducer;

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

    /**
     * Constructor of HazelcastRunnableProducer
     * @param itemProducer ItemProducer client's implementation.
     * @param hazelcastInstance instance of hazelcast.
     * @param queueName queue name
     */
    public HazelcastRunnableProducer(ItemProducer<T> itemProducer, HazelcastInstance hazelcastInstance, String queueName) {
        super(hazelcastInstance, queueName);
        this.itemProducer = itemProducer;
    }

    @Override
    public void run() {

    	Member member = getFirstClusterMember();
    	boolean isLocalMember = member.localMember();
    	
		logger.debug(String.format("Trying to execute HazelcastRunnableProducer on first Cluster Member: '%s'. Is Local Member: %s", member.getUuid(), isLocalMember));
    	
    	if (isLocalMember) {
    		try{
    			
    			IQueue<Object> iQueue = hazelcastInstance.getQueue(queueName);
    			logger.info(String.format("Hazelcast queue %s size: %s", queueName, iQueue.size()));
    			// Execute item producer only if the queue has none elements to be processed
    			if (iQueue.isEmpty()) {
    				// Produces items from client's implementation
    				Collection<T> items = itemProducer.produce();
    				
    				if (items!= null){
    					produce(items);
    				}
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
    
    /**
     * Returns the first cluster member
     * @return the first cluster member 
     */
    private Member getFirstClusterMember() {
        return hazelcastInstance.getCluster().getMembers().iterator().next();
    }
}
