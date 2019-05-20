package br.gov.go.sefaz.clusterworker.core;

import org.junit.AfterClass;
import org.junit.Test;

import br.gov.go.sefaz.clusterworker.core.factory.ClusterWorkerFactory;
import br.gov.go.sefaz.clusterworker.core.item.processor.IntegerItemProcessor;
import br.gov.go.sefaz.clusterworker.core.item.processor.StringItemProcessor;
import br.gov.go.sefaz.clusterworker.core.item.producer.IntegerItemProducer;
import br.gov.go.sefaz.clusterworker.core.item.producer.StringItemProducer;
import br.gov.go.sefaz.clusterworker.core.support.TestConstants;

/**
 * Cluster Worker example of use
 * @author renato-rs
 * @since 1.0.0
 */
public class ClusterWorkerTest {

	private static ClusterWorkerFactory cwFactory = ClusterWorkerFactory.getInstance(TestConstants.CW_NAME);
	private static ClusterWorker<Integer> integerClusterWorker = cwFactory.getClusterWorker(Integer.class);
	private static ClusterWorker<String> stringClusterWorker = cwFactory.getClusterWorker(String.class);

	@AfterClass
	public static void tearDownClass() {
		cwFactory.shutdown(integerClusterWorker);
		cwFactory.shutdown(stringClusterWorker);
	}
	
    @Test
    public void shouldExecuteItemProducer() throws InterruptedException {
        // Execute the item produder on cluster worker
        integerClusterWorker.executeItemProducer(new IntegerItemProducer());
        stringClusterWorker.executeItemProducer(new StringItemProducer());
    }
    
    @Test
    public void shouldExecuteItemProcessor() throws InterruptedException {
        // Execute the item processor on cluster worker
        integerClusterWorker.executeItemProccessor(new IntegerItemProcessor());
        stringClusterWorker.executeItemProccessor(new StringItemProcessor());

        //Just wait some seconds to execute this test
        Thread.sleep(TestConstants.CW_EXECUTION_TIME);
    }
}