package br.gov.go.sefaz.clusterworker.core.task;

import org.apache.log4j.Logger;

import br.gov.go.sefaz.clusterworker.core.annotation.ConsumeFromQueue;
import br.gov.go.sefaz.clusterworker.core.constants.TestConstants;
import br.gov.go.sefaz.clusterworker.core.consumer.ConsumerStrategy;

/**
 * Created by renato-rs on 13/10/2016.
 */
@ConsumeFromQueue(queueName = TestConstants.CW_QUEUE_NAME, strategy = ConsumerStrategy.WAIT_ON_AVAILABLE, workers = TestConstants.CW_TASK_PROCESSOR_WORKERS)
public class IntegerTaskProcessor implements TaskProcessor<Integer> {

	private static final long serialVersionUID = -546308003566028878L;
	private static final Logger logger = Logger.getLogger(IntegerTaskProcessor.class);

    @Override
    public void process(Integer type) {
        logger.info("Processing object " + type);
    }
}