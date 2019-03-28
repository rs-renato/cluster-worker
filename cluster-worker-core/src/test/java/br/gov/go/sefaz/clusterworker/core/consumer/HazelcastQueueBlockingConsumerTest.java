package br.gov.go.sefaz.clusterworker.core.consumer;

import static org.junit.Assert.assertNotNull;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import br.gov.go.sefaz.clusterworker.core.ClusterWorker;
import br.gov.go.sefaz.clusterworker.core.constants.TestConstants;
import br.gov.go.sefaz.clusterworker.core.factory.ClusterWorkerFactory;
import br.gov.go.sefaz.clusterworker.core.item.IntegerItemProducer;
import br.gov.go.sefaz.clusterworker.core.queue.QueueStrategy;

/**
 * HazelcastQueueConsumer example of use in blocking way
 * @author renato-rs
 * @since 1.0
 */
public class HazelcastQueueBlockingConsumerTest {

	private static ClusterWorkerFactory cwFactory = ClusterWorkerFactory.getInstance();
	private static ClusterWorker<Integer> clusterWorker;

	@BeforeClass
	public static void setUp() {
    	// Instantiate a Cluster Worker to handle integer objects (produce and consume to/from hazelcast queue)
		clusterWorker = cwFactory.getClusterWorker(Integer.class);
		 
		Timer timerItemProducer = new Timer();
		 int TIMER_DELAY = 10_000;
	        
			timerItemProducer.schedule(

	                new TimerTask() {
	                    @Override
	                    public void run() {
	                    	// Execute the item produder on cluster worker
	                		clusterWorker.executeItemProducer(new IntegerItemProducer());	
	                    }
	                },
	                TIMER_DELAY,
	                TimeUnit.MILLISECONDS.convert(TestConstants.CW_ITEM_PRODUCER_FREQUENCY, TimeUnit.SECONDS)
	        );
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
}