package br.gov.go.sefaz.clusterworker.core.factory;

import com.hazelcast.core.HazelcastInstance;

import br.gov.go.sefaz.clusterworker.core.ClusterWorker;
import br.gov.go.sefaz.clusterworker.core.annotation.ConsumeFromQueue;
import br.gov.go.sefaz.clusterworker.core.annotation.ProduceToQueue;
import br.gov.go.sefaz.clusterworker.core.consumer.Consumer;
import br.gov.go.sefaz.clusterworker.core.consumer.ConsumerStrategy;
import br.gov.go.sefaz.clusterworker.core.consumer.HazelcastQueueConsumer;
import br.gov.go.sefaz.clusterworker.core.consumer.HazelcastRunnableConsumer;
import br.gov.go.sefaz.clusterworker.core.producer.HazelcastQueueProducer;
import br.gov.go.sefaz.clusterworker.core.producer.HazelcastRunnableProducer;
import br.gov.go.sefaz.clusterworker.core.producer.Producer;
import br.gov.go.sefaz.clusterworker.core.support.AnnotationSupport;
import br.gov.go.sefaz.clusterworker.core.support.HazelcastSupport;
import br.gov.go.sefaz.clusterworker.core.task.TaskProcessor;
import br.gov.go.sefaz.clusterworker.core.task.TaskProducer;

/**
 * Factory for create {@link Consumer}'s and {@link Producer}'s 
 * @author renato-rs
 * @since 1.0
 */
public class ClusterWorkerFactory {

    private static final ClusterWorkerFactory instance = new ClusterWorkerFactory();
    private static final HazelcastInstance hazelcastInstance = HazelcastSupport.getInstance().getHazelcastInstance();

    private ClusterWorkerFactory() {}

    public static ClusterWorkerFactory getInstance() {
        return instance;
    }

    /**
     * Create a new {@link ClusterWorker} instance of T type.
     * @param type type which this ClusterWorker will handle.
     * @return {@link ClusterWorker} instance
     */
    public <T> ClusterWorker<T> getClusterWorker(Class<T> type){
        return new ClusterWorker<>(hazelcastInstance);
    }

    /**
     * Create a new {@link HazelcastRunnableConsumer} instance of T type.
     * @param taskProcessor the task process that will be executed by this {@link HazelcastRunnableConsumer}.
     * @return {@link HazelcastRunnableConsumer} instance
     */
    public <T> HazelcastRunnableConsumer<T> getHazelcastRunnableConsumer(TaskProcessor<T> taskProcessor){
    	//Assert mandatory exception to create an HazelcastRunnableConsumer
        ConsumeFromQueue consumeFromQueue = AnnotationSupport.assertMandatoryAnnotation(taskProcessor, ConsumeFromQueue.class);
        return new HazelcastRunnableConsumer<>(taskProcessor, hazelcastInstance, consumeFromQueue.queueName(), consumeFromQueue.strategy(), consumeFromQueue.timeout());
    }

    /**
     * Create a new {@link HazelcastRunnableProducer} instance of T type.
     * @param taskProducer the task process that will be executed by this {@link HazelcastRunnableProducer}.
     * @return {@link HazelcastRunnableProducer} instance
     */
    public <T> HazelcastRunnableProducer<T> getHazelcastRunnableProducer(TaskProducer<T> taskProducer){
    	//Assert mandatory exception to create an HazelcastRunnableProducer
        ProduceToQueue produceToQueue = AnnotationSupport.assertMandatoryAnnotation(taskProducer, ProduceToQueue.class);
        return new HazelcastRunnableProducer<>(taskProducer, hazelcastInstance, produceToQueue.queueName());
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
     * @param consumerStrategy Consummer queue strategy
     * @param timeout Timeout of execution (in seconds) to the task processor before to return null on queue consumption.
     * @return {@link HazelcastQueueConsumer} instance
     */
    public <T> HazelcastQueueConsumer<T> getHazelcastQueueConsumer(String queueName, ConsumerStrategy consumerStrategy, int timeout){
    	return new HazelcastQueueConsumer<>(hazelcastInstance, queueName, consumerStrategy, timeout);
    }

    /**
     * Retrieve the hazelcast instance used used by this factory
     * @return hazelcast instance
     */
    public HazelcastInstance getHazelcastinstance() {
		return hazelcastInstance;
	}
}