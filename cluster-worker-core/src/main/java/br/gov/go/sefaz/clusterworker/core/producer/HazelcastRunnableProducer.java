package br.gov.go.sefaz.clusterworker.core.producer;


import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceNotActiveException;
import com.hazelcast.core.IQueue;
import com.hazelcast.core.Member;

import br.gov.go.sefaz.clusterworker.core.constants.ClusterWorkerConstants;
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

    	// Get the next member 
		Member member = HazelcastMemberRoundRobin.next(hazelcastInstance, ClusterWorkerConstants.CW_ROUND_ROBIN_MEMBER);
    	boolean isLocalMember = member.localMember();
    	
    	if (isLocalMember) {
    		
    		try{
    			IQueue<Object> iQueue = hazelcastInstance.getQueue(queueName);
    			logger.debug(String.format("Hazelcast queue %s size: %s", queueName, iQueue.size()));
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
    	
		logger.info(String.format("HazelcastRunnableProducer execution %s on this member.", (isLocalMember ? "completed" : "ignored")));
    }
}
