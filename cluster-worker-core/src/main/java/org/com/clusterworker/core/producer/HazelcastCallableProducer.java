package org.com.clusterworker.core.producer;


import java.util.Collection;
import java.util.concurrent.Callable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.com.clusterworker.core.constants.ClusterWorkerConstants;
import org.com.clusterworker.core.exception.ItemProducerException;
import org.com.clusterworker.core.item.ItemProducer;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceNotActiveException;
import com.hazelcast.core.IMap;

/**
 * Callable of {@link HazelcastQueueProducer}, responsible for produces {@link ItemProducer} client's implementation.
 * @author rs-renato
 * @since 1.0.0
 * @param <T> type which this callable will handle.
 */
public final class HazelcastCallableProducer<T>  extends HazelcastQueueProducer<T> implements Callable<Void> {

	private static final transient long serialVersionUID = 2538609461091747126L;
	private static final transient Logger logger = LogManager.getLogger(HazelcastCallableProducer.class);

    private ItemProducer<T> itemProducer;

    /**
     * Constructor of HazelcastCallableProducer
     * @param itemProducer ItemProducer client's implementation.
     * @param hazelcastInstance instance of hazelcast.
     * @param queueName queue name
     * @since 1.0.0
     */
    public HazelcastCallableProducer(ItemProducer<T> itemProducer, HazelcastInstance hazelcastInstance, String queueName) {
    	 super(hazelcastInstance, queueName);
         this.itemProducer = itemProducer;
    }

    @Override
    public Void call() {

    	String producerThreadName = getCallableProducerName();
    	
		try{
			
			setRunning(true);
			
			logger.info(String.format("[%s] - Starting thread '%s'..", Thread.currentThread().getName(), producerThreadName));
			
			// Produces items from client's implementation
			Collection<T> items = itemProducer.produce();

			if (items != null && !items.isEmpty()){
				produce(items);
			}
			
        } catch (InterruptedException|HazelcastInstanceNotActiveException e) {
			logger.error(String.format("Cannot produce to hazelcast '%s' queue! This thread will die! Reason: %s", queueName, e));
			Thread.currentThread().interrupt();
		}catch (ItemProducerException e){
			logger.error("Cannot produce on client's implementation!", e);
		}catch (Exception e){
			logger.error(String.format("A general error occurs process on '%s'", producerThreadName), e);
        }finally {
        	logger.info(String.format("[%s] - Thread '%s' execution FINISHED!", Thread.currentThread().getName(), producerThreadName));
        	setRunning(false);
        }
    	
		// This Callable<Void>
		return null;
    }
    
    /**
     * Retrieves the unique thread name for this producer
     * @return the unique producer thread name
     * @since 1.0.0
     */
    public String getCallableProducerName() {
    	return String.format("%s.producer[%s]", hazelcastInstance.getName(), itemProducer.getClass().getSimpleName());
    }
    
    /**
     * Returns the state running of this producer
     * @return <code>true</code> if this producer is running <code>false</code> otherwise
     */
    public boolean isRunning() {
    	return getRunningProducers().getOrDefault(getCallableProducerName(), false);
    }

    /**
     * Updates running status for this producer
     * @param running indicates if this producer is running
     */
    private void setRunning(boolean running) {
    	getRunningProducers().put(getCallableProducerName(), running);
    }
    
    /**
     * Retrieves all state running producers
     * @return Map with state running producers 
     */
    private IMap<String, Boolean> getRunningProducers(){
    	return hazelcastInstance.getMap(ClusterWorkerConstants.CW_RUNNING_PRODUCER);
    }
}
