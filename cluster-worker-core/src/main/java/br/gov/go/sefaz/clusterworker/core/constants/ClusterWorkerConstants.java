package br.gov.go.sefaz.clusterworker.core.constants;

/**
 * Constants for global definitions of ClusterWorker API
 * @author renato-rs
 * @since 1.0
 *
 */
public class ClusterWorkerConstants {

	private ClusterWorkerConstants() {
	}
	
	/**
	 * Defines the default Service Task Name
	 */
    public static final String CW_SERVICE_TASK_NAME = "cw.service.task";
    
    /**
     * Defines the default queue config name
     */
    public static final String CW_QUEUE_CONFIG_DEFAULT = "cw.queue.config.default";
    
    
    /**
     * Defines the quantity of pieces the queue will be splited. This is used for task equity distrution reasons
     */
    public static final int CW_QUEUE_SPLIT_SIZE = 4;
}
