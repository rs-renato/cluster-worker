package br.gov.go.sefaz.clusterworker.core.item;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import br.gov.go.sefaz.clusterworker.core.annotation.ConsumeFromQueue;
import br.gov.go.sefaz.clusterworker.core.consumer.ConsumerStrategy;
import br.gov.go.sefaz.clusterworker.core.support.TestConstants;

/**
 * Example of implementation of {@link ItemProcessor}
 * @author renato-rs
 * @since 1.0
 */
@ConsumeFromQueue(queueName = TestConstants.CW_INTEGER_QUEUE_NAME, strategy = ConsumerStrategy.WAIT_ON_AVAILABLE, workers = TestConstants.CW_ITEM_PROCESSOR_WORKERS)
public class IntegerItemProcessor implements ItemProcessor<Integer> {

	private static final Logger logger = LogManager.getLogger(IntegerItemProcessor.class);
	private static final long serialVersionUID = -546308003566028878L;

    @Override
    public void process(Integer item) {
    	// Process the item obtained from hazelcast distributed queue 
        logger.info("Processing item: " + item);
    }
}