package br.gov.go.sefaz.clusterworker.core.producer;


import java.util.Collection;
import java.util.concurrent.Callable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceNotActiveException;

import br.gov.go.sefaz.clusterworker.core.exception.ItemProducerException;
import br.gov.go.sefaz.clusterworker.core.item.ItemProducer;

/**
 * Callable of {@link HazelcastQueueProducer}, responsible for produces {@link ItemProducer} client's implementation.
 * @author renato.rsilva
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

		return null;
    }

    /**
     * Retrieves the unique thread name for this producer
     * @return the unique producer thread name
     * @since 1.0.0
     */
    private String getCallableProducerName() {
    	return String.format("%s.producer[%s]", hazelcastInstance.getName(), itemProducer.getClass().getSimpleName());
    }
}
