package br.gov.go.sefaz.clusterworker.core.factory;

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
    
    private ClusterWorkerFactory(HazelcastInstance hazelcastInstance) {
    	this.hazelcastInstance = hazelcastInstance;
    }
    
    public static ClusterWorkerFactory getInstance(String hazelcastInstanceName) {
    	return getInstance(HazelcastSupport.getOrcreateDefaultHazelcastInstance(hazelcastInstanceName));
    }
    
    /**
     * Creates a new ClusterWorkerFactory with the given from hazelcast instance.
     * The {@link HazelcastInstance} is mantained into this factory, that means all
     * objects created from this factory ({@link ClusterWorker}, {@link HazelcastRunnableConsumer}, {@link HazelcastRunnableProducer},
     * {@link HazelcastQueueProducer}, {@link HazelcastQueueConsumer}), will keep its reference.
     * @return a ClusterWorkerFactory instance
     */
    public static ClusterWorkerFactory getInstance(HazelcastInstance hazelcastInstance) {
    	logger.debug(String.format("Creating ClusterWorkerFactory instance with hazelcast instance name '%s'.", hazelcastInstance.getName()));
    	return new ClusterWorkerFactory(hazelcastInstance);
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
		logger.debug("Creating new HazelcastRunnableProducer");
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
     * Shutdown hazelcast instance from this factory.
     */
	public void shutdown() {

    	logger.warn("Shuttingdown ClusterWorkerFactory and its hazelcast instance..");
		
		if (HazelcastSupport.isHazelcastInstanceRunning(hazelcastInstance)) {
			hazelcastInstance.getLifecycleService().shutdown();
		}
		
    	logger.warn("ClusterWorkerFactory shutdown completed!");
	}
}