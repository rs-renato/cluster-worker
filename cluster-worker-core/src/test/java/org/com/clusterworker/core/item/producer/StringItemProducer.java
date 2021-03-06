package org.com.clusterworker.core.item.producer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.com.clusterworker.core.annotation.ProduceToQueue;
import org.com.clusterworker.core.item.ItemProducer;
import org.com.clusterworker.core.support.RandomStringSupport;
import org.com.clusterworker.core.support.TestConstants;

/**
 * Example of implementation of {@link ItemProducer}
 * @author rs-renato
 * @since 1.0.0
 */
@ProduceToQueue(queueName = TestConstants.CW_STRING_QUEUE_NAME, cronExpression = TestConstants.CW_ITEM_PRODUCER_CRON_EXPRESSION, maxSize = TestConstants.CW_ITEM_PRODUCER_MAX_QUANTITY)
public class StringItemProducer implements ItemProducer<String> {

	private static final long serialVersionUID = 3936335122592593550L;
	private static final Logger logger = LogManager.getLogger(StringItemProducer.class);

    @Override
    public Collection<String> produce() {

        List<String> items = new ArrayList<String>();

        for (int i = 1; i <= TestConstants.CW_ITEM_PRODUCER_QUANTITY; i++){
            items.add(RandomStringSupport.generate(5));
        }

        logger.info("Producing items: " + items);
    	// Produces these items to hazelcast distributed queue
        return items;
    }
}
