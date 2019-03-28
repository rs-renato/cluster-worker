package br.gov.go.sefaz.clusterworker.core;

import org.junit.Test;

import br.gov.go.sefaz.clusterworker.core.factory.ClusterWorkerFactory;
import br.gov.go.sefaz.clusterworker.core.item.IntegerItemProcessor;
import br.gov.go.sefaz.clusterworker.core.item.IntegerItemProducer;

/**
 * Cluster Worker example of use
 * @author renato-rs
 * @since 1.0
 */
public class ClusterWorkerTest {

    @Test
    public void testClusterWorker() throws InterruptedException {

    	// Instantiate a Cluster Worker to handle integer objects (produce and consume to/from hazelcast queue)
        ClusterWorker<Integer> clusterWorker = ClusterWorkerFactory.getInstance().getClusterWorker(Integer.class);
        
        // Execute the item produder on cluster worker
        clusterWorker.executeItemProducer(new IntegerItemProducer());
        
        // Execute the item processor on cluster worker
        clusterWorker.executeItemProccessor(new IntegerItemProcessor());

        //Just wait for 30s to execute this test
        Thread.sleep(30 * 1000);

        // Shutdown cluster worker internals
        clusterWorker.shutdown();
    }
}