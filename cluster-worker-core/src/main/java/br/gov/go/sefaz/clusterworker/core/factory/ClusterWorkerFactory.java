package br.gov.go.sefaz.clusterworker.core.factory;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import br.gov.go.sefaz.clusterworker.core.support.ItemSupport;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.hazelcast.config.Config;
import com.hazelcast.config.ExecutorConfig;
import com.hazelcast.config.QueueConfig;
import com.hazelcast.core.HazelcastInstance;

import br.gov.go.sefaz.clusterworker.core.ClusterWorker;
import br.gov.go.sefaz.clusterworker.core.annotation.ConsumeFromQueue;
import br.gov.go.sefaz.clusterworker.core.annotation.ProduceToQueue;
import br.gov.go.sefaz.clusterworker.core.constants.ClusterWorkerConstants;
import br.gov.go.sefaz.clusterworker.core.consumer.Consumer;
import br.gov.go.sefaz.clusterworker.core.consumer.ConsumerStrategy;
import br.gov.go.sefaz.clusterworker.core.consumer.HazelcastQueueConsumer;
import br.gov.go.sefaz.clusterworker.core.consumer.HazelcastCallableConsumer;
import br.gov.go.sefaz.clusterworker.core.item.ItemProcessor;
import br.gov.go.sefaz.clusterworker.core.item.ItemProducer;
import br.gov.go.sefaz.clusterworker.core.producer.HazelcastQueueProducer;
import br.gov.go.sefaz.clusterworker.core.producer.HazelcastCallableProducer;
import br.gov.go.sefaz.clusterworker.core.producer.Producer;
import br.gov.go.sefaz.clusterworker.core.support.AnnotationSupport;
import br.gov.go.sefaz.clusterworker.core.support.HazelcastSupport;
import br.gov.go.sefaz.clusterworker.core.support.ParameterizedTypeReference;

/**
 * Factory for create {@link Consumer}'s and {@link Producer}'s implementations
 * @author renato.rsilva
 * @since 1.0.0
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
     * @since 1.0.0
     */
    private ClusterWorkerFactory(HazelcastInstance hazelcastInstance, boolean isDefaultHazelcastInstance) {
    	this.isDefaultHazelcastInstance = isDefaultHazelcastInstance;
    	this.hazelcastInstance = hazelcastInstance;
    }
    
    /**
     * Creates a new ClusterWorkerFactory with the given from hazelcast instance name.
     * The {@link HazelcastInstance} is mantained into this factory, that means all
     * objects created from this factory ({@link ClusterWorker}, {@link HazelcastCallableConsumer}, {@link HazelcastCallableProducer},
     * {@link HazelcastQueueProducer}, {@link HazelcastQueueConsumer}), will keep its reference.
     * @return a ClusterWorkerFactory instance
     * @since 1.0.0
     */
    public static ClusterWorkerFactory getInstance(String hazelcastInstanceName) {
    	logger.debug(String.format("Creating ClusterWorkerFactory instance with hazelcast instance name '%s'..", hazelcastInstanceName));
    	return new ClusterWorkerFactory(HazelcastSupport.getOrcreateDefaultHazelcastInstance(hazelcastInstanceName), true);
    }
    
    /**
     * Creates a new ClusterWorkerFactory with the given from hazelcast instance.
     * The {@link HazelcastInstance} is mantained into this factory, that means all
     * objects created from this factory ({@link ClusterWorker}, {@link HazelcastCallableConsumer}, {@link HazelcastCallableProducer},
     * {@link HazelcastQueueProducer}, {@link HazelcastQueueConsumer}), will keep its reference.
     * @return a ClusterWorkerFactory instance
     * @since 1.0.0
     */
    public static ClusterWorkerFactory getInstance(HazelcastInstance hazelcastInstance) {
    	logger.debug(String.format("Creating ClusterWorkerFactory instance with hazelcast instance name '%s'..", hazelcastInstance.getName()));
    	return new ClusterWorkerFactory(hazelcastInstance, false);
    }

    /**
     * Create a new {@link ClusterWorker} instance of T type.
     * @param type which this ClusterWorker will handle.
     * @return {@link ClusterWorker} instance
     * @since 1.0.0
     */
    public <T> ClusterWorker<T> getClusterWorker(Class<T> type){
		return addAndReturn(new ClusterWorker<>(hazelcastInstance));
    }

    /**
     * Create a new {@link ClusterWorker} instance of T type.
     * @param type which this ClusterWorker will handle.
     * @return {@link ClusterWorker} instance
     * @since 1.0.0
     */
    public <T> ClusterWorker<T> getClusterWorker(ParameterizedTypeReference<T> type){
		return addAndReturn(new ClusterWorker<>(hazelcastInstance));
    }

    /**
     * Create a new {@link HazelcastCallableConsumer} instance of T type.
     * @param itemProcessor the item process that will be executed by this {@link HazelcastCallableConsumer}.
     * @return {@link HazelcastCallableConsumer} instance
     * @since 1.0.0
     */
    public <T> HazelcastCallableConsumer<T> getHazelcastCallableConsumer(ItemProcessor<T> itemProcessor){
    	//Assert mandatory exception to create an HazelcastCallableConsumer
        ConsumeFromQueue consumeFromQueue = AnnotationSupport.assertMandatoryAnnotation(itemProcessor, ConsumeFromQueue.class);

        // Configures the executor service size if this configuration wasn't set
        Config config = this.hazelcastInstance.getConfig();

        String executorServiceName = ItemSupport.getExecutorServiceNameFor(itemProcessor);

		int poolSize = consumeFromQueue.workers();

        if(!config.getExecutorConfigs().containsKey(executorServiceName)){
            logger.debug(String.format("Updating executor service '%s' pool site to '%s' ", executorServiceName, poolSize));
            ExecutorConfig executorConfig = new ExecutorConfig(executorServiceName, poolSize);
            executorConfig.setQueueCapacity(ClusterWorkerConstants.CW_EXECUTOR_SERVICE_MAX_QUEUE_CAPACITY_DEFAULT);
            executorConfig.setStatisticsEnabled(ClusterWorkerConstants.CW_EXECUTOR_SERVICE_STATISTICS_ENABLED_DEFAULT);
            config.addExecutorConfig(executorConfig);
        }

        HazelcastCallableConsumer<T> hazelcastCallableConsumer = new HazelcastCallableConsumer<>(itemProcessor, hazelcastInstance, consumeFromQueue.queueName(), consumeFromQueue.strategy(), consumeFromQueue.timeout(), consumeFromQueue.timeUnit());
		logger.debug(String.format("Created HazelcastCallableProducer: %s", hazelcastCallableConsumer));
		return hazelcastCallableConsumer;
    }

    /**
     * Create a new {@link HazelcastCallableProducer} instance of T type.
     * @param itemProducer the item process that will be executed by this {@link HazelcastCallableProducer}.
     * @return {@link HazelcastCallableProducer} instance
     * @since 1.0.0
     */
    public <T> HazelcastCallableProducer<T> getHazelcastCallableProducer(ItemProducer<T> itemProducer){
    	//Assert mandatory exception to create an HazelcastCallableProducer
        ProduceToQueue produceToQueue = AnnotationSupport.assertMandatoryAnnotation(itemProducer, ProduceToQueue.class);
        
        String queueName = produceToQueue.queueName();
        int maxSize = produceToQueue.maxSize();
        
        // Configures the queue size if this configuration wasn't set
        Config config = this.hazelcastInstance.getConfig();
        
		if (!config.getQueueConfigs().containsKey(queueName)) {
            logger.debug(String.format("Updating queue '%s' size to %s", queueName, maxSize));
            QueueConfig queueConfig = new QueueConfig(queueName);
            queueConfig.setMaxSize(maxSize);
            config.addQueueConfig(queueConfig);
        }
		
		HazelcastCallableProducer<T> hazelcastCallableProducer = new HazelcastCallableProducer<>(itemProducer, hazelcastInstance, queueName);
        logger.debug(String.format("Created HazelcastCallableProducer: %s", hazelcastCallableProducer));
		return hazelcastCallableProducer;
    }

    /**
     * Create a new {@link HazelcastQueueProducer} instance of T type.
     * @param queueName queue name
     * @return {@link HazelcastQueueProducer} instance
     * @since 1.0.0
     */
    public <T> HazelcastQueueProducer<T> getHazelcastQueueProducer(String queueName){
    	HazelcastQueueProducer<T> hazelcastQueueProducer = new HazelcastQueueProducer<>(hazelcastInstance, queueName);
        logger.debug(String.format("Created HazelcastQueueProducer: %s", hazelcastQueueProducer));
		return hazelcastQueueProducer;
    }
    
    /**
     * Create a new {@link HazelcastQueueConsumer} instance of T type, using the default configuration for timeout and non-blocking strategy.
     * @param queueName queue name
     * @return {@link HazelcastQueueConsumer} instance
     * @since 1.0.0
     * @see {@link ConsumerStrategy#ACCEPT_NULL}
     */
    public <T> HazelcastQueueConsumer<T> getHazelcastQueueConsumer(String queueName){
    	HazelcastQueueConsumer<T> hazelcastQueueConsumer = new HazelcastQueueConsumer<>(hazelcastInstance, queueName);
    	logger.debug(String.format("Created HazelcastQueueConsumer: %s", hazelcastQueueConsumer));
		return hazelcastQueueConsumer;
    }
    
    /**
     * Create a new {@link HazelcastQueueConsumer} instance of T type.
     * @param queueName queue name
     * @param consumerStrategy Consumer queue strategy
     * @param timeout Timeout of execution to the item processor before to return null on queue consumption.
     * @param timeUnit Time duration of timeout
     * @return {@link HazelcastQueueConsumer} instance
     * @since 1.0.0
     */
    public <T> HazelcastQueueConsumer<T> getHazelcastQueueConsumer(String queueName, ConsumerStrategy consumerStrategy, int timeout, TimeUnit timeUnit){
    	HazelcastQueueConsumer<T> hazelcastQueueConsumer = new HazelcastQueueConsumer<>(hazelcastInstance, queueName, consumerStrategy, timeout, timeUnit);
    	logger.debug(String.format("Created HazelcastQueueConsumer: %s", hazelcastQueueConsumer));
		return hazelcastQueueConsumer;
    }
    
    
    /**
     * Shutdown all clusterWorker instances created by this factory. However, the hazelcast will be shutted down <b>if and only if</b>, 
     * the hazelcast was created by this factory. That is, if the hazelcast instance 
     * was provided to this factory when it was created, this method won't shutdown the hazelcast.
     * </br></br><i>Note:</i> Any other dependency of internal hazelcast instance will be affected! Eg.: Another clusterworker instance.
     * @since 1.0.0
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
     * @since 1.0.0
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
	 * @since 1.0.0
	 */
	public void shutdown(boolean shutdownHazelcast) {
		
		logger.warn(String.format("Shutting down ClusterWorkerFactory and its instances! Shutdown inner hazelcast instance: %s", shutdownHazelcast));
		
    	for (ClusterWorker<?> clusterWorker : cwInstances) {
			shutdown(clusterWorker, shutdownHazelcast);
		}
		
    	this.cwInstances.clear();
    	
    	logger.warn("ClusterWorkerFactory shutdown completed!");
	}
	
	/**
	 * Shutdown this clusterWorker and its core (listeners, futures, etc).
	 * </br></br><i>Note:</i> If <code>shutdownHazelcast</code> was set to true, any other dependency of this hazelcast instance
	 * will be affected! Eg.: Another clusterworker instance.
	 * @param clusterWorker the clusterWorker to be shutted down
	 * @param shutdownHazelcast <code>true</code> if this method should shutdown its internal hazelcast instance,
	 * <code>false</code> otherwise.
	 * @since 1.0.0
	 */
	public void shutdown(ClusterWorker<?> clusterWorker, boolean shutdownHazelcast) {

    	logger.warn(String.format("Shutdown Clusterworker! Shutdown inner hazelcast instance: %s", shutdownHazelcast));
    	
    	clusterWorker.shutdown(shutdownHazelcast);
		
		// Shutdown the hazelcast if it was created by this factory
    	if (shutdownHazelcast) {
    		shutdownHazelcast();
		}
	}
	
	/**
	 * Adds this clusterWorker instance into collection and return the element.
	 * @param clusterWorker to add into collection
	 * @return clusterWorker
	 * @since 1.0.0
	 */
	private <T> ClusterWorker<T> addAndReturn(ClusterWorker<T> clusterWorker) {
		cwInstances.add(clusterWorker);
		return clusterWorker;
	}
	
	/**
	 * Shutdown the hazelcast intance if its running
	 * @since 1.0.0
	 */
	private void shutdownHazelcast() {
		try {
			if (HazelcastSupport.isHazelcastInstanceRunning(hazelcastInstance)) {
		    	logger.warn("Hazelcast instance will be shutted down ..");
					hazelcastInstance.getLifecycleService().shutdown();
			}
		} catch (Exception e) {
	    	logger.error("Could not shutdown hazelcast instance!", e);
		}
	}
}