package br.gov.go.sefaz.clusterworker.core.producer;


import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceNotActiveException;

import br.gov.go.sefaz.clusterworker.core.exception.ItemProducerException;
import br.gov.go.sefaz.clusterworker.core.item.ItemProducer;

/**
 * Runnable of {@link HazelcastQueueProducer}, responsible for produces {@link ItemProducer} client's implementation.
 * @author renato.rsilva
 * @since 1.0.0
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
     * @since 1.0.0
     */
    public HazelcastRunnableProducer(ItemProducer<T> itemProducer, HazelcastInstance hazelcastInstance, String queueName) {
    	 super(hazelcastInstance, queueName);
         this.itemProducer = itemProducer;
    }

    @Override
    public void run() {

		String producerThreadName = getRunnableProducerName();
		logger.info(String.format("Starting thread '%s'..", producerThreadName));
		
		try{
			
			// Produces items from client's implementation
			Collection<T> items = itemProducer.produce();

			if (items != null && !items.isEmpty()){
				produce(items);
			}
			
        } catch (InterruptedException|HazelcastInstanceNotActiveException e) {
			logger.error(String.format("Cannot produce to hazelcast '%s' queue! This thread will die! Reason: %s", queueName, e));
			Thread.currentThread().interrupt();
		}catch (ItemProducerException e){
			logger.error(String.format("Cannot produce on client's implementation! Reason: %s", e.getMessage()));
		}catch (Exception e){
			logger.error(String.format("A general error occurs process on '%s'", producerThreadName), e);
        }
    	
		logger.info(String.format("[%s] - Thread '%s' execution FINISHED!", Thread.currentThread().getName(), producerThreadName));
    }
    
    /**
     * Retrieves the unique thread name for this producer
     * @return the unique producer thread name
     * @since 1.0.0
     */
    private String getRunnableProducerName() {
    	return String.format("%s.producer[%s]", hazelcastInstance.getName(), itemProducer.getClass().getSimpleName());
    }
}
