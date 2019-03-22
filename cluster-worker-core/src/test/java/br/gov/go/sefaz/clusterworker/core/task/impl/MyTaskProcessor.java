package br.gov.go.sefaz.clusterworker.core.task.impl;

import org.apache.log4j.Logger;

import br.gov.go.sefaz.clusterworker.core.constants.TestConstants;
import br.gov.go.sefaz.clusterworker.core.consumer.ConsumerStrategy;
import br.gov.go.sefaz.clusterworker.core.task.TaskProcessor;
import br.gov.go.sefaz.clusterworker.core.task.annotation.QueueeProcessor;

/**
 * Created by renato-rs on 13/10/2016.
 */
@QueueeProcessor(queueName = TestConstants.TASK_QUEUE, consumerStrategy = ConsumerStrategy.WAIT_ON_AVAILABLE, workers = TestConstants.TASK_PROCESS_WORKERS)
public class MyTaskProcessor implements TaskProcessor<Integer> {

	private static final long serialVersionUID = -546308003566028878L;
	private static final Logger logger = Logger.getLogger(MyTaskProcessor.class);

    @Override
    public void process(Integer type) {
        logger.info("Processing object " + type);
    }
}
