package br.gov.go.sefaz.clusterworker.core.producer.impl;


import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import org.junit.Test;

import br.gov.go.sefaz.clusterworker.core.constants.TestConstants;
import br.gov.go.sefaz.clusterworker.core.consumer.HazelcastQueueeConsumer;
import br.gov.go.sefaz.clusterworker.core.consumer.impl.MyWaitOnAvailableBaseConsumer;

/**
 * Created by renato-rs on 21/10/2016.
 */
public class BaseProducerTest {

    private static HazelcastQueueeConsumer<Integer> hazelcastQueueeConsumer;
    private static MyBaseProducer baseProducer;

    @Test
    public void testBaseProducer(){

        baseProducer = new MyBaseProducer();

        ArrayList<Integer> list = new ArrayList<Integer>();

        for (int i = 0; i < TestConstants.TASK_PRODUCE_QUANTITY; i++) {
            list.add(i);
        }

        baseProducer.produce(list);

        hazelcastQueueeConsumer = new MyWaitOnAvailableBaseConsumer();

        Integer result;

        for (int i = 0; i < TestConstants.TASK_PRODUCE_QUANTITY; i++) {
            result = hazelcastQueueeConsumer.consume();
            assertEquals(result, list.get(i));
        }

        hazelcastQueueeConsumer.shutdown();
        baseProducer.shutdown();
    }
}