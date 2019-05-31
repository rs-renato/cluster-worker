package org.com.clusterworker.core.consumer;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.com.clusterworker.core.ClusterWorker;
import org.com.clusterworker.core.consumer.ConsumerStrategy;
import org.com.clusterworker.core.consumer.HazelcastQueueConsumer;
import org.com.clusterworker.core.factory.ClusterWorkerFactory;
import org.com.clusterworker.core.item.producer.IntegerItemProducer;
import org.com.clusterworker.core.support.TestConstants;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

/**
 * HazelcastQueueConsumer example of use in non-blocking way
 * @author rs-renato
 * @since 1.0.0
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class HazelcastQueueNonBlockingConsumerTest {

	private static ClusterWorkerFactory cwFactory = ClusterWorkerFactory.getInstance(TestConstants.CW_NAME);
	private static ClusterWorker<Integer> clusterWorker = cwFactory.getClusterWorker(Integer.class);

	@BeforeClass
	public static void setUp() {
    	// Instantiate a Cluster Worker to handle integer objects (produce and consume to/from hazelcast queue)
		clusterWorker.executeItemProducer(new IntegerItemProducer());	
	}

	@AfterClass
	public static void tearDownClass() {
        // Shutdown cluster worker internals
		cwFactory.shutdown(clusterWorker);
	}

	@Test
	public void CW_T01_shouldExecuteHazelcastQueueConsumerAcceptNull() throws InterruptedException {

        // Creates an consumer to consumes the hazelcast queue
		HazelcastQueueConsumer<Integer> hazelcastQueueConsumer = cwFactory.getHazelcastQueueConsumer(TestConstants.CW_INTEGER_QUEUE_NAME, ConsumerStrategy.ACCEPT_NULL, TestConstants.CW_QUEUEE_TIMEOUT, TimeUnit.SECONDS);

		int loop = TestConstants.CW_ITEM_PRODUCER_QUANTITY * 3;

		int count = 0;

		List<Integer> resultList = new ArrayList<Integer>();

		while (count < loop) {
			resultList.add(hazelcastQueueConsumer.consume());
			count++;
		}

		assertTrue(resultList.contains(null));
	}
}