package br.gov.go.sefaz.clusterworker.core.producer;


import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import org.junit.Test;

import br.gov.go.sefaz.clusterworker.core.constants.TestConstants;
import br.gov.go.sefaz.clusterworker.core.consumer.HazelcastQueueConsumer;
import br.gov.go.sefaz.clusterworker.core.factory.ClusterWorkerFactory;
import br.gov.go.sefaz.clusterworker.core.queue.QueueStrategy;

/**
 * HazelcastQueueProducer example of use
 * @author renato-rs
 * @since
 */
public class HazelcastQueueProducerTest {

    private static ClusterWorkerFactory cwFactory = ClusterWorkerFactory.getInstance();
	
    @Test
	public void testHazelcastQueueProducer() {

        // Creates an consumer to consumes the hazelcast queue
		HazelcastQueueConsumer<Integer> hazelcastQueueConsumer = cwFactory.getHazelcastQueueConsumer(TestConstants.CW_QUEUE_NAME, QueueStrategy.WAIT_ON_AVAILABLE, TestConstants.CW_QUEUEE_TIMEOUT);
        // Creates an producer to produces the hazelcast queue
		HazelcastQueueProducer<Integer> hazelcastQueueProducer = cwFactory.getHazelcastQueueProducer(TestConstants.CW_QUEUE_NAME);

		ArrayList<Integer> list = new ArrayList<Integer>();

		for (int i = 0; i < TestConstants.CW_ITEM_PRODUCER_QUANTITY; i++) {
			list.add(i);
		}

		hazelcastQueueProducer.produce(list);

		Integer result;

		for (int i = 0; i < TestConstants.CW_ITEM_PRODUCER_QUANTITY; i++) {
			result = hazelcastQueueConsumer.consume();
			assertEquals(result, list.get(i));
		}
        // Shutdown cluster worker factory internals
		cwFactory.shutdownHazelcastInstance();
	}
}