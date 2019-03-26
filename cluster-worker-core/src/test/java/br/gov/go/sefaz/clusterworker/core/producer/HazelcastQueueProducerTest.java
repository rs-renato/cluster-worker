package br.gov.go.sefaz.clusterworker.core.producer;


import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import org.junit.Test;

import br.gov.go.sefaz.clusterworker.core.ClusterWorker;
import br.gov.go.sefaz.clusterworker.core.constants.TestConstants;
import br.gov.go.sefaz.clusterworker.core.consumer.HazelcastQueueConsumer;
import br.gov.go.sefaz.clusterworker.core.factory.ClusterWorkerFactory;
import br.gov.go.sefaz.clusterworker.core.queue.QueueStrategy;

/**
 * Created by renato-rs on 21/10/2016.
 */
public class HazelcastQueueProducerTest {

    private static ClusterWorkerFactory cwFactory = ClusterWorkerFactory.getInstance();
    private static ClusterWorker<Integer> clusterWorker = cwFactory.getClusterWorker(Integer.class);
	
    @Test
	public void testHazelcastQueueProducer() {

		HazelcastQueueConsumer<Integer> hazelcastQueueConsumer = cwFactory.getHazelcastQueueConsumer(TestConstants.CW_QUEUE_NAME, QueueStrategy.WAIT_ON_AVAILABLE, TestConstants.CW_QUEUEE_TIMEOUT);
		HazelcastQueueProducer<Integer> hazelcastQueueProducer = cwFactory.getHazelcastQueueProducer(TestConstants.CW_QUEUE_NAME);

		ArrayList<Integer> list = new ArrayList<Integer>();

		for (int i = 0; i < TestConstants.CW_TASK_PRODUCER_QUANTITY; i++) {
			list.add(i);
		}

		hazelcastQueueProducer.produce(list);

		Integer result;

		for (int i = 0; i < TestConstants.CW_TASK_PRODUCER_QUANTITY; i++) {
			result = hazelcastQueueConsumer.consume();
			assertEquals(result, list.get(i));
		}

		clusterWorker.shutdown();
	}
}