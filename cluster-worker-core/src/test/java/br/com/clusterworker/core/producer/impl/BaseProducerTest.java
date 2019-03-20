package br.com.clusterworker.core.producer.impl;


import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import org.junit.Test;

import br.com.clusterworker.core.BaseConsumer;
import br.com.clusterworker.core.constants.TestConstants;
import br.com.clusterworker.core.consumer.impl.MyWaitOnAvailableBaseConsumer;

/**
 * Created by renato-rs on 21/10/2016.
 */
public class BaseProducerTest {

    private static BaseConsumer<Integer> baseConsumer;
    private static MyBaseProducer baseProducer;

    @Test
    public void testBaseProducer(){

        baseProducer = new MyBaseProducer();

        ArrayList<Integer> list = new ArrayList<Integer>();

        for (int i = 0; i < TestConstants.TASK_PRODUCE_QUANTITY; i++) {
            list.add(i);
        }

        baseProducer.produce(list);

        baseConsumer = new MyWaitOnAvailableBaseConsumer();

        Integer result;

        for (int i = 0; i < TestConstants.TASK_PRODUCE_QUANTITY; i++) {
            result = baseConsumer.consume();
            assertEquals(result, list.get(i));
        }

        baseConsumer.shutdown();
        baseProducer.shutdown();
    }
}