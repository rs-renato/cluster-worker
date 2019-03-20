package br.gov.go.sefaz.clusterworker.core.task.impl;

import org.apache.log4j.Logger;

import br.gov.go.sefaz.clusterworker.core.TaskProcessUnlockable;
import br.gov.go.sefaz.clusterworker.core.annotations.TaskProcessConfig;
import br.gov.go.sefaz.clusterworker.core.constants.TestConstants;
import br.gov.go.sefaz.clusterworker.core.utils.QueueStrategy;

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
