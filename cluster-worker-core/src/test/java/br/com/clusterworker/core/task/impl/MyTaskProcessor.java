package br.com.clusterworker.core.task.impl;

import org.apache.log4j.Logger;

import br.com.clusterworker.annotations.TaskProcessConfig;
import br.com.clusterworker.core.TaskProcessUnlockable;
import br.com.clusterworker.core.constants.TestConstants;
import br.com.clusterworker.utils.QueueStrategy;

/**
 * Created by renato-rs on 13/10/2016.
 */
@TaskProcessConfig(queueName = TestConstants.TASK_QUEUE, strategy = QueueStrategy.WAIT_ON_AVAILABLE, workers = TestConstants.TASK_PROCESS_WORKERS)
public class MyTaskProcessor extends TaskProcessUnlockable<Integer> {

    private static final Logger logger = Logger.getLogger(MyTaskProcessor.class);

    @Override
    public void process(Integer type) {
        logger.info("Processing object " + type);
    }
}
