package org.com.clusterworker.core.consumer;

import static org.junit.Assert.assertNotNull;

import java.util.Timer;
import java.util.TimerTask;
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
 * HazelcastQueueConsumer example of use in blocking way
 * @author rs-renato
 * @since 1.0.0
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class HazelcastQueueBlockingConsumerTest {

	private static ClusterWorkerFactory cwFactory = ClusterWorkerFactory.getInstance(TestConstants.CW_NAME);
	private static ClusterWorker<Integer> clusterWorker = cwFactory.getClusterWorker(Integer.class);
	private static Timer timerItemProducer;
	
	@BeforeClass
	public static void setUp() {
		timerItemProducer = new Timer();
		int TIMER_DELAY = 10_000;

		timerItemProducer.schedule(
			new TimerTask() {
				@Override
				public void run() {
					// Execute the item produder on cluster worker
					clusterWorker.executeItemProducer(new IntegerItemProducer());
				}
			}, TIMER_DELAY);
	}

	@AfterClass
	public static void tearDownClass() {
        // Shutdown cluster worker internals
		cwFactory.shutdown(clusterWorker);
		timerItemProducer.cancel();
	}

	@Test
	public void CW_T01_shouldExecuteHazelcastQueueConsumerWaitOnAvailable() throws InterruptedException {
        // Creates an consumer to consumes the hazelcast queue
		HazelcastQueueConsumer<Integer> hazelcastQueueConsumer =  cwFactory.getHazelcastQueueConsumer(TestConstants.CW_INTEGER_QUEUE_NAME, ConsumerStrategy.WAIT_ON_AVAILABLE, TestConstants.CW_QUEUEE_TIMEOUT, TimeUnit.SECONDS); 
				
		Integer result;

		while ((result = hazelcastQueueConsumer.consume()) != TestConstants.CW_ITEM_PRODUCER_QUANTITY) {
			assertNotNull(result);
		}
	}
}