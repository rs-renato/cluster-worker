package br.gov.go.sefaz.clusterworker.core.support;

import br.gov.go.sefaz.clusterworker.core.constants.ClusterWorkerConstants;
import br.gov.go.sefaz.clusterworker.core.item.ItemProcessor;

/**
 * ItemProducer support class
 * @author renato.rsilva
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
