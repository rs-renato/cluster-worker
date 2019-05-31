package org.com.clusterworker.core.support;

import org.com.clusterworker.core.constants.ClusterWorkerConstants;
import org.com.clusterworker.core.item.ItemProcessor;

/**
 * ItemProducer support class
 * @author rs-renato
 * @since 1.0.0
 */
public class ItemSupport {

    private ItemSupport() {
    }

    /**
	 * Creates an executor service name to be associated to this itemProcessor
	 * @param itemProcessor item processor
	 * @return executor service name for this itemProcessor
	 * @since 1.0.0
	 */
    public static <T> String getExecutorServiceNameFor(ItemProcessor<T> itemProcessor){
        return String.format("%s.%s", ClusterWorkerConstants.CW_EXECUTOR_SERVICE_NAME, itemProcessor.getClass().getSimpleName());
    }
}
