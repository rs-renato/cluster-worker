package br.gov.go.sefaz.clusterworker.core.producer;


import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import br.gov.go.sefaz.clusterworker.core.consumer.ConsumerStrategy;
import br.gov.go.sefaz.clusterworker.core.consumer.HazelcastQueueConsumer;
import br.gov.go.sefaz.clusterworker.core.factory.ClusterWorkerFactory;
import br.gov.go.sefaz.clusterworker.core.support.TestConstants;

/**
 * HazelcastQueueProducer example of use
 * @author renato-rs
 * @since 1.0
 */
public class HazelcastQueueProducerTest {

    private static ClusterWorkerFactory cwFactory;
	private static ArrayList<Integer> itemProduced = new ArrayList<Integer>();
	
	@BeforeClass
	public static void setUp() {
		cwFactory = ClusterWorkerFactory.getInstance(TestConstants.CW_NAME);
	}
	
	@AfterClass
	public static void tearDownClass() {
		cwFactory.shutdown();
	}
	
    @Test
	public void shouldExecuteHazelcastQueueProducer() throws InterruptedException {

        // Creates an producer to produces into hazelcast queue
		HazelcastQueueProducer<Integer> hazelcastQueueProducer = cwFactory.getHazelcastQueueProducer(TestConstants.CW_INTEGER_QUEUE_NAME);

		for (int i = 0; i < TestConstants.CW_ITEM_PRODUCER_QUANTITY; i++) {
			itemProduced.add(i);
		}

		hazelcastQueueProducer.produce(itemProduced);
	}
    
    @Test
   	public void shouldExecuteHazelcastQueueConsumer() throws InterruptedException {
        // Creates an consumer to consumes from hazelcast queue
   		HazelcastQueueConsumer<Integer> hazelcastQueueConsumer = cwFactory.getHazelcastQueueConsumer(TestConstants.CW_INTEGER_QUEUE_NAME, ConsumerStrategy.WAIT_ON_AVAILABLE, TestConstants.CW_QUEUEE_TIMEOUT);

   		Integer result;

   		for (int i = 0; i < TestConstants.CW_ITEM_PRODUCER_QUANTITY; i++) {
   			result = hazelcastQueueConsumer.consume();
   			assertEquals(result, itemProduced.get(i));
   		}
   	}
}