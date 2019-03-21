package br.gov.go.sefaz.clusterworker.core.factory;

import br.gov.go.sefaz.clusterworker.core.ClusterWorker;
import br.gov.go.sefaz.clusterworker.core.consumer.HazelcastRunnableConsumer;
import br.gov.go.sefaz.clusterworker.core.producer.HazelcastRunnableProducer;
import br.gov.go.sefaz.clusterworker.core.task.TaskProcessor;
import br.gov.go.sefaz.clusterworker.core.task.TaskProducer;
import br.gov.go.sefaz.clusterworker.core.task.annotation.TaskProcessConfig;
import br.gov.go.sefaz.clusterworker.core.task.annotation.TaskProduceConfig;
import br.gov.go.sefaz.clusterworker.core.utils.ClusterWorkerUtils;

/**
 * Factory for create Workers and Standalones objects.
 */
public class ClusterWorkerFactory {

    private static final ClusterWorkerFactory instance = new ClusterWorkerFactory();

    private ClusterWorkerFactory() {}

    public static ClusterWorkerFactory getInstance() {
        return instance;
    }

    /**
     * Create a new {@link ClusterWorker} instance of T type.
     * @param t type of this ClusterWorker.
     * @return ClusterWorker
     */
    public <T> ClusterWorker<T> getClusterWorker(Class<T> t){
        return new ClusterWorker<T>();
    }

    /**
     * Create a new {@link HazelcastRunnableConsumer} instance of T type.
     * @param taskProcess the task process that will be executed by this WorkerConsumer.
     * @return WorkerConsumer
     */
    public <T> HazelcastRunnableConsumer<T> getWorkerConsumer(TaskProcessor<T> taskProcess){

        TaskProcessConfig taskProduceConfig = ClusterWorkerUtils.verifyMandatoryAnotation(taskProcess, TaskProcessConfig.class);

        return new HazelcastRunnableConsumer<T>(taskProcess, taskProduceConfig.queueName(), taskProduceConfig.strategy(), taskProduceConfig.timeout());
    }

    /**
     * Create a new {@link HazelcastRunnableProducer} instance of T type.
     * @param taskProduce the task process that will be executed by this WorkerProducer.
     * @return WorkerProducer
     */
    public <T> HazelcastRunnableProducer<T> getWorkerProducer(TaskProducer<T> taskProduce){

        TaskProduceConfig standaloneProducerConfig = ClusterWorkerUtils.verifyMandatoryAnotation(taskProduce, TaskProduceConfig.class);

        return new HazelcastRunnableProducer<T>(taskProduce, standaloneProducerConfig.queueName());
    }
}