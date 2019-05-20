package br.gov.go.sefaz.clusterworker.core.consumer;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import br.gov.go.sefaz.clusterworker.core.ClusterWorker;
import br.gov.go.sefaz.clusterworker.core.factory.ClusterWorkerFactory;
import br.gov.go.sefaz.clusterworker.core.item.producer.IntegerItemProducer;
import br.gov.go.sefaz.clusterworker.core.support.TestConstants;

/**
 * HazelcastQueueConsumer example of use in non-blocking way
 * @author renato.rsilva
 * @since 1.0.0
 */
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
	public void shouldExecuteHazelcastQueueConsumerAcceptNull() throws InterruptedException {

        // Creates an consumer to consumes the hazelcast queue
		HazelcastQueueConsumer<Integer> hazelcastQueueConsumer = cwFactory.getHazelcastQueueConsumer(TestConstants.CW_INTEGER_QUEUE_NAME, ConsumerStrategy.ACCEPT_NULL, TestConstants.CW_QUEUEE_TIMEOUT);

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