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
import br.gov.go.sefaz.clusterworker.core.item.IntegerItemProducer;
import br.gov.go.sefaz.clusterworker.core.queue.QueueStrategy;

/**
 * HazelcastQueueConsumer example of use
 * @author renato-rs
 * @since 1.0
 */
public class HazelcastQueueConsumerTest {

	private static ClusterWorkerFactory cwFactory = ClusterWorkerFactory.getInstance();
	private static ClusterWorker<Integer> clusterWorker;

	@BeforeClass
	public static void setUp() {
    	// Instantiate a Cluster Worker to handle integer objects (produce and consume to/from hazelcast queue)
		clusterWorker = cwFactory.getClusterWorker(Integer.class);
        // Execute the item produder on cluster worker
		clusterWorker.executeItemProducer(new IntegerItemProducer());
	}

	@AfterClass
	public static void tearDownClass() {
        // Shutdown cluster worker internals
		clusterWorker.shutdown();
	}

	@Test
	public void testHazelcastQueueConsumerWaitOnAvailable() {
        // Creates an consumer to consumes the hazelcast queue
		HazelcastQueueConsumer<Integer> hazelcastQueueConsumer =  cwFactory.getHazelcastQueueConsumer(TestConstants.CW_QUEUE_NAME, QueueStrategy.WAIT_ON_AVAILABLE, TestConstants.CW_QUEUEE_TIMEOUT); 
				
		Integer result;

		while ((result = hazelcastQueueConsumer.consume()) != TestConstants.CW_ITEM_PRODUCER_QUANTITY) {
			assertNotNull(result);
		}
	}

	@Test
	public void testHazelcastQueueConsumerAcceptNull() {

        // Creates an consumer to consumes the hazelcast queue
		HazelcastQueueConsumer<Integer> hazelcastQueueConsumer = cwFactory.getHazelcastQueueConsumer(TestConstants.CW_QUEUE_NAME, QueueStrategy.ACCEPT_NULL, TestConstants.CW_QUEUEE_TIMEOUT);

		int loop = TestConstants.CW_ITEM_PRODUCER_QUANTITY * 5;

		int count = 0;

		List<Integer> resultList = new ArrayList<Integer>();

		while (count < loop) {
			resultList.add(hazelcastQueueConsumer.consume());
			count++;
		}

		assertTrue(resultList.contains(null));
	}
}