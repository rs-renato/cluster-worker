package br.gov.go.sefaz.clusterworker.core.task;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;

import br.gov.go.sefaz.clusterworker.core.annotation.ProduceToQueue;
import br.gov.go.sefaz.clusterworker.core.constants.TestConstants;

/**
 * Created by renato-rs on 11/10/2016.
 */
@ProduceToQueue(queueName = TestConstants.CW_QUEUE_NAME, frequency = TestConstants.CW_TASK_PRODUCER_FREQUENCY)
public class IntegerTaskProducer implements TaskProducer<Integer> {

	private static final long serialVersionUID = 3936335122592593550L;
	private static final Logger logger = Logger.getLogger(IntegerTaskProducer.class);

    @Override
    public Collection<Integer> produce() {

        List<Integer> list = new ArrayList<Integer>();

        for (int i = 1; i <= TestConstants.CW_TASK_PRODUCER_QUANTITY; i++){
            list.add(i);
        }

        logger.info("Producing objects... " + list);


        return list;
    }
}
