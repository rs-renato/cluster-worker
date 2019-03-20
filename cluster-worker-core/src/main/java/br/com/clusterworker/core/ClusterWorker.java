package br.com.clusterworker.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.Member;

import br.com.clusterworker.annotations.TaskProcessConfig;
import br.com.clusterworker.annotations.TaskProduceConfig;
import br.com.clusterworker.utils.ClusterWorkerUtils;
import br.com.clusterworker.utils.HazelcastUtils;
import br.com.clusterworker.utils.PropertyUtils;

/**
 * Central core to manage hazelcast executor services and it's lifecycle.
 * @param <T> type of T
 */
public final class ClusterWorker<T> {

    private static final Logger logger = Logger.getLogger(ClusterWorker.class);

    private HazelcastUtils hazelcastUtils = HazelcastUtils.getInstance();
    private HazelcastInstance hazelcastInstance = hazelcastUtils.getHazelcastInstance();

    private final IExecutorService executorService = hazelcastInstance.getExecutorService(PropertyUtils.getProperties("cw-config").getProperty("cw.service.task"));
    private final ClusterWorkerFactory clusterWorkerFactory = ClusterWorkerFactory.getInstance();
    private Map<TaskProduce<T>, Timer> timerMap = new HashMap<TaskProduce<T>, Timer>();

    /**
     * Execute a {@link TaskProcess} client's implementation on hazelcast executor service.
     * @param taskProcess implementation of client task.
     */
    public void executeTaskProccess(TaskProcess<T> taskProcess){

        TaskProcessConfig taskProcessConfig = ClusterWorkerUtils.verifyMandatoryAnotation(taskProcess, TaskProcessConfig.class);

        int workers = taskProcessConfig.workers();

        logger.info(String.format("Executing task process (%s core(s)) implentation on hazelcast executor service.", workers));

        for (int i = 0; i <  workers; i++) {

            try {
                executorService.executeOnMember(clusterWorkerFactory.getWorkerConsumer(taskProcess), getLocalMember());
            }catch (Exception e){
                logger.error(String.format("Cannot execute a TaskProcess on hazelcast executor service! %s", e.getMessage()));
            }
        }
    }

    /**
     * Execute a {@link TaskProduce} client's implementation on hazelcast executor service.
     * @param taskProduce implementation of client task.
     */
    public void executeTaskProduce(final TaskProduce<T> taskProduce){

        final WorkerProducer<T> workerProducer = clusterWorkerFactory.getWorkerProducer(taskProduce);

        final TaskProduceConfig taskProduceConfig = ClusterWorkerUtils.verifyMandatoryAnotation(taskProduce, TaskProduceConfig.class);

        int frequency = taskProduceConfig.frequency();

        Timer timerTask = new Timer();

        logger.info(String.format("Executing task produce implentation on hazelcast executor service with frequency %s second.", frequency));

        timerTask.scheduleAtFixedRate(

                new TimerTask() {
                    @Override
                    public void run() {

                        String queueName = taskProduceConfig.queueName();

                        logger.info(String.format("Hazelcast queue %s size: %s", queueName, hazelcastInstance.getQueue(queueName).size()));

                        if (hazelcastInstance.getQueue(queueName).isEmpty()) {

                            boolean isLocalMember = isLocalMember();

                            if (isLocalMember) {

                                try {

                                    logger.debug("Executing task produce implementation on hazelcast executor service.");

                                    executorService.execute(workerProducer);

                                } catch (Exception e) {
                                    logger.error(String.format("Cannot execute a TaskProduce on hazelcast executor service! %s", e.getMessage()));
                                }
                            }
                        }
                    }
                },
                0,
                TimeUnit.MILLISECONDS.convert(frequency, TimeUnit.SECONDS)
        );

        timerMap.put(taskProduce, timerTask);
    }

    /**
     * Verifies if this member is local member
     * @return true is local member
     */
    private boolean isLocalMember() {
        return hazelcastInstance.getCluster().getMembers().iterator().next().localMember();
    }

    /**
     * Returns the local cluster member
     * @return member
     */
    private Member getLocalMember() {
        return hazelcastInstance.getCluster().getLocalMember();
    }

    /**
     * Shutdown an specified timer {@link TaskProduce}
     * @param taskProduce to be canceled.
     */
    public void shutDownTask(TaskProduce<T> taskProduce){

        if (timerMap.containsKey(taskProduce)){
            logger.warn("Shuttingdown TaskProduce!");
            timerMap.get(taskProduce).cancel();
        }
    }

    /**
     * Shutdown entyre clusterworker core and all hazelcast instances.
     */
    public void shutDownAll(){

        logger.warn("Shuttingdown entire ClusterWorker!");

        for (Timer timer: timerMap.values()){
            timer.cancel();
            logger.warn("Timer canceled: " + timer.toString());
        }

        WorkerConsumer.stop = true;

        if (!executorService.isShutdown()){
            executorService.shutdownNow();
            logger.warn("ExecutorService finished!");
        }

        if (hazelcastInstance.getLifecycleService().isRunning()){
            hazelcastInstance.getLifecycleService().shutdown();
            logger.warn("Hazelcast LifecycleService finished!");
        }
    }
}
