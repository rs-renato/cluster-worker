package org.com.clusterworker.core.item.processor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.com.clusterworker.core.annotation.ConsumeFromQueue;
import org.com.clusterworker.core.consumer.ConsumerStrategy;
import org.com.clusterworker.core.item.ItemProcessor;
import org.com.clusterworker.core.support.TestConstants;

/**
 * Example of implementation of {@link ItemProcessor}
 * @author rs-renato
 * @since 1.0.0
 */
@ConsumeFromQueue(queueName = TestConstants.CW_STRING_QUEUE_NAME, strategy = ConsumerStrategy.WAIT_ON_AVAILABLE, workers = TestConstants.CW_ITEM_PROCESSOR_WORKERS)
public class StringItemProcessor implements ItemProcessor<String> {

	private static final Logger logger = LogManager.getLogger(StringItemProcessor.class);
	private static final long serialVersionUID = -546308003566028878L;

    @Override
    public void process(String item) {
    	// Process the item obtained from hazelcast distributed queue 
        logger.info("Processing item: " + item);
    }
}