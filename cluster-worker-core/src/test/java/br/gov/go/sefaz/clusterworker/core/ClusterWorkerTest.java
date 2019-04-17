package br.gov.go.sefaz.clusterworker.core;

import org.junit.AfterClass;
import org.junit.Test;

import br.gov.go.sefaz.clusterworker.core.ClusterWorker;
import br.gov.go.sefaz.clusterworker.core.factory.ClusterWorkerFactory;
import br.gov.go.sefaz.clusterworker.core.item.IntegerItemProcessor;
import br.gov.go.sefaz.clusterworker.core.item.IntegerItemProducer;
import br.gov.go.sefaz.clusterworker.core.support.TestConstants;

/**
 * Cluster Worker example of use
 * @author renato-rs
 * @since 1.0
 */
public class ClusterWorkerTest {

	private static ClusterWorkerFactory cwFactory = ClusterWorkerFactory.getInstance(TestConstants.CW_NAME);
	private static ClusterWorker<Integer> clusterWorker = cwFactory.getClusterWorker(Integer.class);
	
	@AfterClass
	public static void tearDownClass() {
		cwFactory.shutdown(clusterWorker);
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

        //Just wait some seconds to execute this test
        Thread.sleep(TestConstants.CW_EXECUTION_TIME);
    }
}