package br.gov.go.sefaz.clusterworker.core.factory;

import com.hazelcast.core.HazelcastInstance;

import br.gov.go.sefaz.clusterworker.core.ClusterWorker;
import br.gov.go.sefaz.clusterworker.core.annotation.ConsumeFromQueue;
import br.gov.go.sefaz.clusterworker.core.annotation.ProduceToQueue;
import br.gov.go.sefaz.clusterworker.core.consumer.ConsumerStrategy;
import br.gov.go.sefaz.clusterworker.core.consumer.HazelcastQueueConsumer;
import br.gov.go.sefaz.clusterworker.core.consumer.HazelcastRunnableConsumer;
import br.gov.go.sefaz.clusterworker.core.producer.HazelcastQueueProducer;
import br.gov.go.sefaz.clusterworker.core.producer.HazelcastRunnableProducer;
import br.gov.go.sefaz.clusterworker.core.support.AnnotationSupport;
import br.gov.go.sefaz.clusterworker.core.support.HazelcastSupport;
import br.gov.go.sefaz.clusterworker.core.task.TaskProcessor;
import br.gov.go.sefaz.clusterworker.core.task.TaskProducer;

/**
 * Factory for create Workers and Standalones objects.
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
     * @param type type of this ClusterWorker.
     * @return ClusterWorker
     */
    public <T> ClusterWorker<T> getClusterWorker(Class<T> type){
        return new ClusterWorker<T>(hazelcastInstance);
    }

    /**
     * Create a new {@link HazelcastRunnableConsumer} instance of T type.
     * @param taskProcessor the task process that will be executed by this WorkerConsumer.
     * @return WorkerConsumer
     */
    public <T> HazelcastRunnableConsumer<T> getHazelcastRunnableConsumer(TaskProcessor<T> taskProcessor){

        ConsumeFromQueue consumeFromQueue = AnnotationSupport.assertMandatoryAnnotation(taskProcessor, ConsumeFromQueue.class);

        return new HazelcastRunnableConsumer<T>(taskProcessor, hazelcastInstance, consumeFromQueue.queueName(), consumeFromQueue.strategy(), consumeFromQueue.timeout());
    }

    /**
     * Create a new {@link HazelcastRunnableProducer} instance of T type.
     * @param taskProducer the task process that will be executed by this WorkerProducer.
     * @return WorkerProducer
     */
    public <T> HazelcastRunnableProducer<T> getHazelcastRunnableProducer(TaskProducer<T> taskProducer){

        ProduceToQueue produceToQueue = AnnotationSupport.assertMandatoryAnnotation(taskProducer, ProduceToQueue.class);

        return new HazelcastRunnableProducer<T>(taskProducer, hazelcastInstance, produceToQueue.queueName());
    }

    public <T> HazelcastQueueProducer<T> getHazelcastQueueProducer(String queueName){
    	return new HazelcastQueueProducer<>(hazelcastInstance, queueName);
    }
    
    public <T> HazelcastQueueConsumer<T> getHazelcastQueueConsumer(String queueName, ConsumerStrategy consumerStrategy, int timeout){
    	return new HazelcastQueueConsumer<>(hazelcastInstance, queueName, consumerStrategy, timeout);
    }

    public HazelcastInstance getHazelcastinstance() {
		return hazelcastInstance;
	}
}