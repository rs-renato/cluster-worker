package br.gov.go.sefaz.clusterworker.core.consumer;

import static org.junit.Assert.assertNotNull;

import java.util.Timer;
import java.util.TimerTask;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import br.gov.go.sefaz.clusterworker.core.ClusterWorker;
import br.gov.go.sefaz.clusterworker.core.factory.ClusterWorkerFactory;
import br.gov.go.sefaz.clusterworker.core.item.producer.IntegerItemProducer;
import br.gov.go.sefaz.clusterworker.core.support.TestConstants;

/**
 * HazelcastQueueConsumer example of use in blocking way
 * @author renato.rsilva
 * @since 1.0.0
 */
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
	public void shouldExecuteHazelcastQueueConsumerWaitOnAvailable() throws InterruptedException {
        // Creates an consumer to consumes the hazelcast queue
		HazelcastQueueConsumer<Integer> hazelcastQueueConsumer =  cwFactory.getHazelcastQueueConsumer(TestConstants.CW_INTEGER_QUEUE_NAME, ConsumerStrategy.WAIT_ON_AVAILABLE, TestConstants.CW_QUEUEE_TIMEOUT); 
				
		Integer result;

		while ((result = hazelcastQueueConsumer.consume()) != TestConstants.CW_ITEM_PRODUCER_QUANTITY) {
			assertNotNull(result);
		}
	}
}