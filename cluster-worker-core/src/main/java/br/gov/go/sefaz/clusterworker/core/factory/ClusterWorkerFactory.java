package br.gov.go.sefaz.clusterworker.core.factory;

import java.util.HashMap;
import java.util.Map;

import com.hazelcast.core.HazelcastInstance;

import br.gov.go.sefaz.clusterworker.core.ClusterWorker;
import br.gov.go.sefaz.clusterworker.core.annotation.ConsumeFromQueue;
import br.gov.go.sefaz.clusterworker.core.annotation.ProduceToQueue;
import br.gov.go.sefaz.clusterworker.core.consumer.Consumer;
import br.gov.go.sefaz.clusterworker.core.consumer.HazelcastQueueConsumer;
import br.gov.go.sefaz.clusterworker.core.consumer.HazelcastRunnableConsumer;
import br.gov.go.sefaz.clusterworker.core.item.ItemProcessor;
import br.gov.go.sefaz.clusterworker.core.item.ItemProducer;
import br.gov.go.sefaz.clusterworker.core.producer.HazelcastQueueProducer;
import br.gov.go.sefaz.clusterworker.core.producer.HazelcastRunnableProducer;
import br.gov.go.sefaz.clusterworker.core.producer.Producer;
import br.gov.go.sefaz.clusterworker.core.queue.QueueStrategy;
import br.gov.go.sefaz.clusterworker.core.support.AnnotationSupport;
import br.gov.go.sefaz.clusterworker.core.support.HazelcastDefaultConfigurationSupport;
import br.gov.go.sefaz.clusterworker.core.support.ParameterizedTypeReference;

/**
 * Factory for create {@link Consumer}'s and {@link Producer}'s 
 * @author renato-rs
 * @since 1.0
 */
public class ClusterWorkerFactory {

    private final HazelcastInstance hazelcastInstance;
    
    private static final Map<String, ClusterWorkerFactory> factoryInstances = new HashMap<>();

    private ClusterWorkerFactory(HazelcastInstance hazelcastInstance) {
    	this.hazelcastInstance = hazelcastInstance;
    }

    /**
     * Creates a new ClusterWorkerFactory instance from default hazelcast instance configuration.
     * @return a ClusterWorkerFactory instance
     */
    public static synchronized ClusterWorkerFactory getInstance() {
    	return getInstance(HazelcastDefaultConfigurationSupport.getDefaultHazelcastInstance());
    }
    
    /**
     * Creates a new ClusterWorkerFactory instance from hazelcast instance.
     * @return a ClusterWorkerFactory instance
     */

    public static synchronized ClusterWorkerFactory getInstance(HazelcastInstance hazelcastInstance) {

    	String hazelcastInstanceName = hazelcastInstance.getName();
    	boolean containsInstance = factoryInstances.containsKey(hazelcastInstanceName);
    	
    	hazelcastInstance = isHazelcastInstanceRunning(hazelcastInstance) ? hazelcastInstance : HazelcastDefaultConfigurationSupport.createDefaultHazelcastInstance();
    	
    	if (!containsInstance || !isHazelcastInstanceRunning(factoryInstances.get(hazelcastInstanceName).hazelcastInstance)) {
    		factoryInstances.put(hazelcastInstanceName, new ClusterWorkerFactory(hazelcastInstance));
		}
    	
    	return factoryInstances.get(hazelcastInstanceName);
    }

    /**
     * Create a new {@link ClusterWorker} instance of T type.
     * @param type which this ClusterWorker will handle.
     * @return {@link ClusterWorker} instance
     */
    public <T> ClusterWorker<T> getClusterWorker(Class<T> type){
        return new ClusterWorker<>(hazelcastInstance);
    }
    
    /**
     * Create a new {@link ClusterWorker} instance of T type.
     * @param type which this ClusterWorker will handle.
     * @return {@link ClusterWorker} instance
     */
    public <T> ClusterWorker<T> getClusterWorker(ParameterizedTypeReference<T> type){
        return new ClusterWorker<>(hazelcastInstance);
    }

    /**
     * Create a new {@link HazelcastRunnableConsumer} instance of T type.
     * @param itemProcessor the item process that will be executed by this {@link HazelcastRunnableConsumer}.
     * @return {@link HazelcastRunnableConsumer} instance
     */
    public <T> HazelcastRunnableConsumer<T> getHazelcastRunnableConsumer(ItemProcessor<T> itemProcessor){
    	//Assert mandatory exception to create an HazelcastRunnableConsumer
        ConsumeFromQueue consumeFromQueue = AnnotationSupport.assertMandatoryAnnotation(itemProcessor, ConsumeFromQueue.class);
        return new HazelcastRunnableConsumer<>(itemProcessor, hazelcastInstance, consumeFromQueue.queueName(), consumeFromQueue.strategy(), consumeFromQueue.timeout());
    }

    /**
     * Create a new {@link HazelcastRunnableProducer} instance of T type.
     * @param itemProducer the item process that will be executed by this {@link HazelcastRunnableProducer}.
     * @return {@link HazelcastRunnableProducer} instance
     */
    public <T> HazelcastRunnableProducer<T> getHazelcastRunnableProducer(ItemProducer<T> itemProducer){
    	//Assert mandatory exception to create an HazelcastRunnableProducer
        ProduceToQueue produceToQueue = AnnotationSupport.assertMandatoryAnnotation(itemProducer, ProduceToQueue.class);
        return new HazelcastRunnableProducer<>(itemProducer, hazelcastInstance, produceToQueue.queueName());
    }

    /**
     * Create a new {@link HazelcastQueueProducer} instance of T type.
     * @param queueName queue name
     * @return {@link HazelcastQueueProducer} instance
     */
    public <T> HazelcastQueueProducer<T> getHazelcastQueueProducer(String queueName){
    	return new HazelcastQueueProducer<>(hazelcastInstance, queueName);
    }
    
    /**
     * Create a new {@link HazelcastQueueConsumer} instance of T type.
     * @param queueName queue name
     * @param queueStrategy Consummer queue strategy
     * @param timeout Timeout of execution (in seconds) to the item processor before to return null on queue consumption.
     * @return {@link HazelcastQueueConsumer} instance
     */
    public <T> HazelcastQueueConsumer<T> getHazelcastQueueConsumer(String queueName, QueueStrategy queueStrategy, int timeout){
    	return new HazelcastQueueConsumer<>(hazelcastInstance, queueName, queueStrategy, timeout);
    }
    
    /**
     * Shutdown hazelcast instance.
     */
	public synchronized void shutdownHazelcastInstance() {

		hazelcastInstance.shutdown();
		
		if (hazelcastInstance.getLifecycleService().isRunning()) {
			hazelcastInstance.getLifecycleService().shutdown();
		}
		
		factoryInstances.remove(hazelcastInstance.getName());
	}
	
	private static boolean isHazelcastInstanceRunning(HazelcastInstance hazelcastInstance) {
		return hazelcastInstance.getLifecycleService().isRunning();
	}
}