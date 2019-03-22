package br.gov.go.sefaz.clusterworker.core.consumer.impl;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.hazelcast.core.HazelcastInstance;

import br.gov.go.sefaz.clusterworker.core.ClusterWorker;
import br.gov.go.sefaz.clusterworker.core.constants.TestConstants;
import br.gov.go.sefaz.clusterworker.core.consumer.ConsumerStrategy;
import br.gov.go.sefaz.clusterworker.core.consumer.HazelcastQueueeConsumer;
import br.gov.go.sefaz.clusterworker.core.factory.ClusterWorkerFactory;
import br.gov.go.sefaz.clusterworker.core.task.impl.MyTaskProducer;

/**
 * Created by renato-rs on 21/10/2016.
 */
public class BaseConsumerTest {

	private static ClusterWorkerFactory cwFactory = ClusterWorkerFactory.getInstance();
	private static HazelcastInstance hazelcastInstance = cwFactory.getHazelcastinstance();
	private static ClusterWorker<Integer> clusterWorker;

	@BeforeClass
	public static void setUp() {
		clusterWorker = cwFactory.getClusterWorker(Integer.class);
		clusterWorker.executeTaskProducer(new MyTaskProducer());
	}

	@AfterClass
	public static void tearDownClass() {
		clusterWorker.shutDownAll();
	}

	@Test
	public void testWaitOnAvailableBaseConsumer() {

		HazelcastQueueeConsumer<Integer> hazelcastQueueeConsumer =
				new HazelcastQueueeConsumer<Integer>(hazelcastInstance, TestConstants.TASK_QUEUE, ConsumerStrategy.WAIT_ON_AVAILABLE);

		Integer result;

		while ((result = hazelcastQueueeConsumer.consume()) != TestConstants.TASK_PRODUCE_QUANTITY) {
			assertNotNull(result);
		}
	}

	@Test
	public void testAcceptNullBaseConsumer() {

		HazelcastQueueeConsumer<Integer> hazelcastQueueeConsumer = 
				new HazelcastQueueeConsumer<Integer>(hazelcastInstance, TestConstants.TASK_QUEUE, ConsumerStrategy.ACCEPT_NULL,TestConstants.BASE_CONSUMER_TIMEOUT);
		;

		int loop = TestConstants.TASK_PRODUCE_QUANTITY * 5;

		int count = 0;

		List<Integer> resultList = new ArrayList<Integer>();

		while (count < loop) {
			resultList.add(hazelcastQueueeConsumer.consume());
			count++;
		}

		assertTrue(resultList.contains(null));
	}
}