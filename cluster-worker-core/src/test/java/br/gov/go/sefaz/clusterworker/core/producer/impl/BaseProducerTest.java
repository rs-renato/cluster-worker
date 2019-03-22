package br.gov.go.sefaz.clusterworker.core.producer.impl;


import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import org.junit.Test;

import com.hazelcast.core.HazelcastInstance;

import br.gov.go.sefaz.clusterworker.core.constants.TestConstants;
import br.gov.go.sefaz.clusterworker.core.consumer.ConsumerStrategy;
import br.gov.go.sefaz.clusterworker.core.consumer.HazelcastQueueeConsumer;
import br.gov.go.sefaz.clusterworker.core.factory.ClusterWorkerFactory;
import br.gov.go.sefaz.clusterworker.core.producer.HazelcastQueueeProducer;

/**
 * Created by renato-rs on 21/10/2016.
 */
public class BaseProducerTest {

    private static HazelcastQueueeConsumer<Integer> hazelcastQueueeConsumer;
    private static HazelcastQueueeProducer<Integer> baseProducer;

    private static ClusterWorkerFactory cwFactory = ClusterWorkerFactory.getInstance();
	private static HazelcastInstance hazelcastInstance = cwFactory.getHazelcastinstance();
	
    @Test
    public void testBaseProducer(){

        baseProducer = new HazelcastQueueeProducer<Integer>(hazelcastInstance, TestConstants.TASK_QUEUE);

        ArrayList<Integer> list = new ArrayList<Integer>();

        for (int i = 0; i < TestConstants.TASK_PRODUCE_QUANTITY; i++) {
            list.add(i);
        }

        baseProducer.produce(list);

        hazelcastQueueeConsumer = new HazelcastQueueeConsumer<Integer>(hazelcastInstance, TestConstants.TASK_QUEUE, ConsumerStrategy.WAIT_ON_AVAILABLE);

        Integer result;

        for (int i = 0; i < TestConstants.TASK_PRODUCE_QUANTITY; i++) {
            result = hazelcastQueueeConsumer.consume();
            assertEquals(result, list.get(i));
        }

        hazelcastInstance.shutdown();
    }
}