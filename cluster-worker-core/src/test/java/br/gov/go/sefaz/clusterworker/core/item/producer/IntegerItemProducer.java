package br.gov.go.sefaz.clusterworker.core.item.producer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import br.gov.go.sefaz.clusterworker.core.annotation.ProduceToQueue;
import br.gov.go.sefaz.clusterworker.core.item.ItemProducer;
import br.gov.go.sefaz.clusterworker.core.support.TestConstants;

/**
 * Example of implementation of {@link ItemProducer}
 * @author renato.rsilva
 * @since 1.0.0
 */
@ProduceToQueue(queueName = TestConstants.CW_INTEGER_QUEUE_NAME, cronExpression = TestConstants.CW_ITEM_PRODUCER_CRON_EXPRESSION, maxSize = TestConstants.CW_ITEM_PRODUCER_MAX_QUANTITY)
public class IntegerItemProducer implements ItemProducer<Integer> {

	private static final long serialVersionUID = 3936335122592593550L;
	private static final Logger logger = LogManager.getLogger(IntegerItemProducer.class);

    @Override
    public Collection<Integer> produce() {

        List<Integer> items = new ArrayList<Integer>();

        for (int i = 1; i <= TestConstants.CW_ITEM_PRODUCER_QUANTITY; i++){
            items.add(i);
        }
    
        logger.info("Producing items: " + items);
    	// Produces these items to hazelcast distributed queue
        return items;
    }
}
