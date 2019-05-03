package br.gov.go.sefaz.clusterworker.core.producer;


import java.util.Calendar;
import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceNotActiveException;
import com.hazelcast.core.IMap;
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
    private transient HazelcastMemberRoundRobin hazelcastMemberRoundRobin;

    /**
     * Constructor of HazelcastRunnableProducer
     * @param itemProducer ItemProducer client's implementation.
     * @param hazelcastInstance instance of hazelcast.
     * @param queueName queue name
     */
    public HazelcastRunnableProducer(ItemProducer<T> itemProducer, HazelcastInstance hazelcastInstance, String queueName) {
    	 super(hazelcastInstance, queueName);
         this.itemProducer = itemProducer;
         this.hazelcastMemberRoundRobin = new HazelcastMemberRoundRobin(hazelcastInstance, ClusterWorkerConstants.CW_ROUND_ROBIN_MEMBER);
    }

    @Override
    public void run() {

		String producerThreadName = getThreadName();
        logger.info(String.format("[%s] - Starting thread '%s'..", Thread.currentThread().getName(), producerThreadName));

        updateLastExecutionTime();
    	
    	// Get the next member 
		Member member = hazelcastMemberRoundRobin.select();
    	boolean isLocalMember = member.localMember();
    	
		if (isLocalMember) {
    		
    		try{
    			
    			// Forces this threads sleeps a while because of concurrence in distibuted producers in other JVM's
    			// This waits cause the other distibuted thread (producers) finish their proccess before this local member
    			Thread.sleep(100);
    			
				// Produces items from client's implementation
				Collection<T> items = itemProducer.produce();

				if (items != null && !items.isEmpty()){
					produce(items);
				}
				// Advances the round robin pivot
				hazelcastMemberRoundRobin.advance();
            } catch (InterruptedException|HazelcastInstanceNotActiveException e) {
				logger.error(String.format("Cannot produce to hazelcast '%s' queue! This thread will die! Reason: %s", queueName, e.getMessage()));
				Thread.currentThread().interrupt();
    		}catch (ItemProducerException e){
    			logger.error(String.format("Cannot produce on client's implementation! Reason: %s", e.getMessage()));
    		 }catch (Exception e){
     			logger.error(String.format("A general error occurs process on '%s'", producerThreadName), e);
             }
		}
    	
		logger.info(String.format("[%s] - Thread '%s' execution %s and finished on this member..", Thread.currentThread().getName(), producerThreadName, (isLocalMember ? "COMPLETED" : "IGNORED")));
    }

    /**
     * Updates the producer last execution time
     */
	private void updateLastExecutionTime() {
		IMap<String, Long> iMap = hazelcastInstance.getMap(ClusterWorkerConstants.CW_PRODUCER_SYNC_EXECUTION);
    	iMap.put(ClusterWorkerConstants.CW_PRODUCER_LAST_EXECUTION, Calendar.getInstance().getTimeInMillis());
	}
    
    /**
     * Retrieves the unique thread name for this producer
     * @return the unique producer thread name
     */
    private String getThreadName() {
    	return String.format("%s.producer[%s]", hazelcastInstance.getName(), itemProducer.getClass().getSimpleName());
    }
}
