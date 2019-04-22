package br.gov.go.sefaz.clusterworker.core.factory;

import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.hazelcast.config.Config;
import com.hazelcast.core.HazelcastInstance;

import br.gov.go.sefaz.clusterworker.core.ClusterWorker;
import br.gov.go.sefaz.clusterworker.core.annotation.ConsumeFromQueue;
import br.gov.go.sefaz.clusterworker.core.annotation.ProduceToQueue;
import br.gov.go.sefaz.clusterworker.core.consumer.Consumer;
import br.gov.go.sefaz.clusterworker.core.consumer.ConsumerStrategy;
import br.gov.go.sefaz.clusterworker.core.consumer.HazelcastQueueConsumer;
import br.gov.go.sefaz.clusterworker.core.consumer.HazelcastRunnableConsumer;
import br.gov.go.sefaz.clusterworker.core.item.ItemProcessor;
import br.gov.go.sefaz.clusterworker.core.item.ItemProducer;
import br.gov.go.sefaz.clusterworker.core.producer.HazelcastQueueProducer;
import br.gov.go.sefaz.clusterworker.core.producer.HazelcastRunnableProducer;
import br.gov.go.sefaz.clusterworker.core.producer.Producer;
import br.gov.go.sefaz.clusterworker.core.support.AnnotationSupport;
import br.gov.go.sefaz.clusterworker.core.support.HazelcastSupport;
import br.gov.go.sefaz.clusterworker.core.support.ParameterizedTypeReference;

/**
 * Factory for create {@link Consumer}'s and {@link Producer}'s implementations
 * @author renato-rs
 * @since 1.0
 */
public class ClusterWorkerFactory {

    private static final Logger logger = LogManager.getLogger(ClusterWorkerFactory.class);

    private final HazelcastInstance hazelcastInstance;
    private final boolean isDefaultHazelcastInstance;
    private final Set<ClusterWorker<?>> cwInstances = new HashSet<>();
    
    /**
     * Creates a ClusterWorkerFactory
     * @param hazelcastInstance hazelcast instance
     * @param isDefaultHazelcastInstance <code>true</code> if this instance was created by this factory, <code>false</code> otherwise.
     */
    private ClusterWorkerFactory(HazelcastInstance hazelcastInstance, boolean isDefaultHazelcastInstance) {
    	this.isDefaultHazelcastInstance = isDefaultHazelcastInstance;
    	this.hazelcastInstance = hazelcastInstance;
    }
    
    public static ClusterWorkerFactory getInstance(String hazelcastInstanceName) {
    	logger.debug(String.format("Creating ClusterWorkerFactory instance with hazelcast instance name '%s'..", hazelcastInstanceName));
    	return new ClusterWorkerFactory(HazelcastSupport.getOrcreateDefaultHazelcastInstance(hazelcastInstanceName), true);
    }
    
    /**
     * Creates a new ClusterWorkerFactory with the given from hazelcast instance.
     * The {@link HazelcastInstance} is mantained into this factory, that means all
     * objects created from this factory ({@link ClusterWorker}, {@link HazelcastRunnableConsumer}, {@link HazelcastRunnableProducer},
     * {@link HazelcastQueueProducer}, {@link HazelcastQueueConsumer}), will keep its reference.
     * @return a ClusterWorkerFactory instance
     */
    public static ClusterWorkerFactory getInstance(HazelcastInstance hazelcastInstance) {
    	logger.debug(String.format("Creating ClusterWorkerFactory instance with hazelcast instance name '%s'..", hazelcastInstance.getName()));
    	return new ClusterWorkerFactory(hazelcastInstance, false);
    }

    /**
     * Create a new {@link ClusterWorker} instance of T type.
     * @param type which this ClusterWorker will handle.
     * @return {@link ClusterWorker} instance
     */
    public <T> ClusterWorker<T> getClusterWorker(Class<T> type){
		return addAndReturn(new ClusterWorker<>(hazelcastInstance));
    }

    /**
     * Create a new {@link ClusterWorker} instance of T type.
     * @param type which this ClusterWorker will handle.
     * @return {@link ClusterWorker} instance
     */
    public <T> ClusterWorker<T> getClusterWorker(ParameterizedTypeReference<T> type){
		return addAndReturn(new ClusterWorker<>(hazelcastInstance));
    }

    /**
     * Create a new {@link HazelcastRunnableConsumer} instance of T type.
     * @param itemProcessor the item process that will be executed by this {@link HazelcastRunnableConsumer}.
     * @return {@link HazelcastRunnableConsumer} instance
     */
    public <T> HazelcastRunnableConsumer<T> getHazelcastRunnableConsumer(ItemProcessor<T> itemProcessor){
    	//Assert mandatory exception to create an HazelcastRunnableConsumer
        ConsumeFromQueue consumeFromQueue = AnnotationSupport.assertMandatoryAnnotation(itemProcessor, ConsumeFromQueue.class);
        HazelcastRunnableConsumer<T> hazelcastRunnableConsumer = new HazelcastRunnableConsumer<>(itemProcessor, hazelcastInstance, consumeFromQueue.queueName(), consumeFromQueue.strategy(), consumeFromQueue.timeout());
		logger.debug(String.format("Created HazelcastRunnableProducer: %s", hazelcastRunnableConsumer));
		return hazelcastRunnableConsumer;
    }

    /**
     * Create a new {@link HazelcastRunnableProducer} instance of T type.
     * @param itemProducer the item process that will be executed by this {@link HazelcastRunnableProducer}.
     * @return {@link HazelcastRunnableProducer} instance
     */
    public <T> HazelcastRunnableProducer<T> getHazelcastRunnableProducer(ItemProducer<T> itemProducer){
    	//Assert mandatory exception to create an HazelcastRunnableProducer
        ProduceToQueue produceToQueue = AnnotationSupport.assertMandatoryAnnotation(itemProducer, ProduceToQueue.class);
        
        String queueName = produceToQueue.queueName();
        
        // Configures the queue size if this configuration wasn't set
        Config config = this.hazelcastInstance.getConfig();
		if (!config.getQueueConfigs().containsKey(queueName)) {
			config.getQueueConfig(queueName)
				.setMaxSize(produceToQueue.maxSize());
		}
		
		HazelcastRunnableProducer<T> hazelcastRunnableProducer = new HazelcastRunnableProducer<>(itemProducer, hazelcastInstance, queueName);
        logger.debug(String.format("Created HazelcastRunnableProducer: %s", hazelcastRunnableProducer));
		return hazelcastRunnableProducer;
    }

    /**
     * Create a new {@link HazelcastQueueProducer} instance of T type.
     * @param queueName queue name
     * @return {@link HazelcastQueueProducer} instance
     */
    public <T> HazelcastQueueProducer<T> getHazelcastQueueProducer(String queueName){
    	HazelcastQueueProducer<T> hazelcastQueueProducer = new HazelcastQueueProducer<>(hazelcastInstance, queueName);
        logger.debug(String.format("Created HazelcastQueueProducer: %s", hazelcastQueueProducer));
		return hazelcastQueueProducer;
    }
    
    /**
     * Create a new {@link HazelcastQueueConsumer} instance of T type.
     * @param queueName queue name
     * @param consumerStrategy Consumer queue strategy
     * @param timeout Timeout of execution (in seconds) to the item processor before to return null on queue consumption.
     * @return {@link HazelcastQueueConsumer} instance
     */
    public <T> HazelcastQueueConsumer<T> getHazelcastQueueConsumer(String queueName, ConsumerStrategy consumerStrategy, int timeout){
    	HazelcastQueueConsumer<T> hazelcastQueueConsumer = new HazelcastQueueConsumer<>(hazelcastInstance, queueName, consumerStrategy, timeout);
    	logger.debug(String.format("Created HazelcastQueueConsumer: %s", hazelcastQueueConsumer));
		return hazelcastQueueConsumer;
    }
    
    /**
     * Shutdown all clusterWorker instances created by this factory. However, the hazelcast will be shutted down <b>if and only if</b>, 
     * the hazelcast was created by this factory. That is, if the hazelcast instance 
     * was provided to this factory when it was created, this method won't shutdown the hazelcast.
     * </br></br><i>Note:</i> Any other dependency of internal hazelcast instance will be affected! Eg.: Another clusterworker instance.
     * @see ClusterWorkerFactory#shutdown(boolean)
     */
	public void shutdown() {
		shutdown(isDefaultHazelcastInstance);
	}
	
	/**
     * Shutdown this clusterWorker and its core (listeners, futures, etc). However, the hazelcast will be shutted down <b>if and only if</b>, 
     * the hazelcast was created by this factory. That is, if the hazelcast instance 
     * was provided to this factory when it was created, this method won't shutdown the hazelcast.
     * </br></br><i>Note:</i> Any other dependency of internal hazelcast instance will be affected! Eg.: Another clusterworker instance.
     * @param clusterWorker the clusterWorker to be shutted down
     * @see ClusterWorkerFactory#shutdown(ClusterWorker, boolean)
     */
	public void shutdown(ClusterWorker<?> clusterWorker) {
		shutdown(clusterWorker, isDefaultHazelcastInstance);
	}
	
	/**
	 * Shutdown all clusterWorker instances created by this factory.
	 * </br></br><i>Note:</i> If <code>shutdownHazelcast</code> was set to true, any other dependency of this hazelcast instance
	 * will be affected! Eg.: Another clusterworker instance.
	 * @param shutdownHazelcast <code>true</code> if this method should shutdown its internal hazelcast instance,
	 * <code>false</code> otherwise.
	 */
	public void shutdown(boolean shutdownHazelcast) {
		
		logger.warn("Shutting down ClusterWorkerFactory and its instances..");
		
    	for (ClusterWorker<?> clusterWorker : cwInstances) {
			shutdown(clusterWorker, shutdownHazelcast);
		}
		
    	logger.warn("ClusterWorkerFactory shutdown completed!");
	}
	
	/**
	 * Shutdown this clusterWorker and its core (listeners, futures, etc).
	 * </br></br><i>Note:</i> If <code>shutdownHazelcast</code> was set to true, any other dependency of this hazelcast instance
	 * will be affected! Eg.: Another clusterworker instance.
	 * @param clusterWorker the clusterWorker to be shutted down
	 * @param shutdownHazelcast <code>true</code> if this method should shutdown its internal hazelcast instance,
	 * <code>false</code> otherwise.
	 */
	public void shutdown(ClusterWorker<?> clusterWorker, boolean shutdownHazelcast) {

		ClusterWorker<?> cwInstance = 
				cwInstances.stream()
					.filter(cw -> cw.equals(clusterWorker))
					.findFirst().orElseGet(null);
		
		// Shutdown the clusterWorker, preserving its internal hazelcast instance
		if (cwInstance != null) {
			cwInstance.shutdown();
			cwInstances.remove(clusterWorker);
		}
		
		// Shutdown the hazelcast if it was created by this factory
    	if (shutdownHazelcast) {
    		shutdownHazelcast();
		}
	}
	
	/**
	 * Adds this clusterWorker instance into collection and return the element.
	 * @param clusterWorker to add into collection
	 * @return clusterWorker
	 */
	private <T> ClusterWorker<T> addAndReturn(ClusterWorker<T> clusterWorker) {
		cwInstances.add(clusterWorker);
		return clusterWorker;
	}
	
	/**
	 * Shutdown the hazelcast intance if its running
	 */
	private void shutdownHazelcast() {
		if (HazelcastSupport.isHazelcastInstanceRunning(hazelcastInstance)) {
	    	logger.warn("Hazelcast instance will be shutted down ..");
				hazelcastInstance.getLifecycleService().shutdown();
		}
	}
}