package br.gov.go.sefaz.clusterworker.core;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import br.gov.go.sefaz.clusterworker.core.constants.TestConstants;
import br.gov.go.sefaz.clusterworker.core.factory.ClusterWorkerFactory;
import br.gov.go.sefaz.clusterworker.core.item.IntegerItemProcessor;
import br.gov.go.sefaz.clusterworker.core.item.IntegerItemProducer;

/**
 * Cluster Worker example of use
 * @author renato-rs
 * @since 1.0
 */
public class ClusterWorkerTest {

	private static ClusterWorker<Integer> clusterWorker;
	
	@BeforeClass
	public static void setUp() {
		clusterWorker = ClusterWorkerFactory.getInstance(TestConstants.CW_NAME).getClusterWorker(Integer.class);
	}
	
	@AfterClass
	public static void tearDownClass() {
		clusterWorker.shutdown();
	}
	
    @Test
    public void shouldExecuteItemProducer() throws InterruptedException {
        // Execute the item produder on cluster worker
        clusterWorker.executeItemProducer(new IntegerItemProducer());
    }
    
    @Test
    public void shouldExecuteItemProcessor() throws InterruptedException {
        // Execute the item processor on cluster worker
        clusterWorker.executeItemProccessor(new IntegerItemProcessor());

        //Just wait for 20s to execute this test
        Thread.sleep(20 * 1000);
    }
}