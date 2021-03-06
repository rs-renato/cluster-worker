package org.com.clusterworker.core;

import java.util.concurrent.TimeUnit;

import org.com.clusterworker.core.ClusterWorker;
import org.com.clusterworker.core.factory.ClusterWorkerFactory;
import org.com.clusterworker.core.item.processor.StringItemProcessor;
import org.com.clusterworker.core.item.producer.StringItemProducer;
import org.com.clusterworker.core.support.TestConstants;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

/**
 * Cluster Worker example of use
 * @author rs-renato
 * @since 1.0.0
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ClusterWorkerTest {

	private static ClusterWorkerFactory cwFactory = ClusterWorkerFactory.getInstance(TestConstants.CW_NAME);
	private static ClusterWorker<String> stringClusterWorker = cwFactory.getClusterWorker(String.class);

    private static StringItemProducer stringItemProducer = new StringItemProducer();
    private static StringItemProcessor stringItemProcessor = new StringItemProcessor();

    @AfterClass
	public static void tearDownClass() {
        cwFactory.shutdown();
	}

    @Test
    public void CW_T01_shouldExecuteItemProducer() {
        // Execute the item produder on cluster worker
        stringClusterWorker.executeItemProducer(stringItemProducer);
    }

    @Test
    public void CW_T02_shouldExecuteItemProcessor() throws InterruptedException {
        // Execute the item processor on cluster worker
        stringClusterWorker.executeItemProccessor(stringItemProcessor);
        // Just process for a while
        TimeUnit.SECONDS.sleep(TestConstants.CW_EXECUTION_TIME);
    }

    @Test
    public void CW_T03_shouldShutdownItemProducer(){
	    stringClusterWorker.shutdown(stringItemProducer);
	    Assert.assertTrue(stringClusterWorker.isDone(stringItemProducer));
    }

    @Test
    public void CW_T04_shouldShutdownItemProcessor(){
        stringClusterWorker.shutdown(stringItemProcessor);
        Assert.assertTrue(stringClusterWorker.isDone(stringItemProcessor));
    }

    @Test
    public void CW_T05_shouldReExecuteItemProducer() {
        // Execute the item produder on cluster worker
        stringClusterWorker.executeItemProducer(stringItemProducer);
    }

    @Test
    public void CW_T06_shouldReExecuteItemProcessor() throws InterruptedException {
        // Execute the item processor on cluster worker
        stringClusterWorker.executeItemProccessor(stringItemProcessor);
        // Just process for a while
        TimeUnit.SECONDS.sleep(TestConstants.CW_EXECUTION_TIME);
    }
}