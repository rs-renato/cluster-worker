package br.gov.go.sefaz.clusterworker.core.consumer;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import br.gov.go.sefaz.clusterworker.core.ClusterWorker;
import br.gov.go.sefaz.clusterworker.core.constants.TestConstants;
import br.gov.go.sefaz.clusterworker.core.factory.ClusterWorkerFactory;
import br.gov.go.sefaz.clusterworker.core.queue.QueueStrategy;
import br.gov.go.sefaz.clusterworker.core.task.IntegerTaskProducer;

/**
 * Created by renato-rs on 21/10/2016.
 */
public class HazelcastQueueConsumerTest {

	private static ClusterWorkerFactory cwFactory = ClusterWorkerFactory.getInstance();
	private static ClusterWorker<Integer> clusterWorker;

	@BeforeClass
	public static void setUp() {
		clusterWorker = cwFactory.getClusterWorker(Integer.class);
		clusterWorker.executeTaskProducer(new IntegerTaskProducer());
	}

	@AfterClass
	public static void tearDownClass() {
		clusterWorker.shutdown();
	}

	@Test
	public void testHazelcastQueueConsumerWaitOnAvailable() {

		HazelcastQueueConsumer<Integer> hazelcastQueueConsumer = 
				cwFactory.getHazelcastQueueConsumer(TestConstants.CW_QUEUE_NAME, QueueStrategy.WAIT_ON_AVAILABLE, TestConstants.CW_QUEUEE_TIMEOUT); 
				
		Integer result;

		while ((result = hazelcastQueueConsumer.consume()) != TestConstants.CW_TASK_PRODUCER_QUANTITY) {
			assertNotNull(result);
		}
	}

	@Test
	public void testHazelcastQueueConsumerAcceptNull() {

		HazelcastQueueConsumer<Integer> hazelcastQueueConsumer = 
				cwFactory.getHazelcastQueueConsumer(TestConstants.CW_QUEUE_NAME, QueueStrategy.ACCEPT_NULL, TestConstants.CW_QUEUEE_TIMEOUT);

		int loop = TestConstants.CW_TASK_PRODUCER_QUANTITY * 5;

		int count = 0;

		List<Integer> resultList = new ArrayList<Integer>();

		while (count < loop) {
			resultList.add(hazelcastQueueConsumer.consume());
			count++;
		}

		assertTrue(resultList.contains(null));
	}
}