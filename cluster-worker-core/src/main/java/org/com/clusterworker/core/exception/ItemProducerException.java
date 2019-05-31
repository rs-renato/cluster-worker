package org.com.clusterworker.core.exception;

import org.com.clusterworker.core.item.ItemProducer;

/**
 * Exception used in {@link ItemProducer} implementations
 * @author rs-renato
 * @since 1.0.0
 */
public class ItemProducerException extends ClusterWorkerException {

	private static final long serialVersionUID = 1L;

	/**
	 * 
	 * @param cause
	 * @since 1.0.0
	 */
	public ItemProducerException(Throwable cause) {
		super(cause);
	}
	
	/**
	 * 
	 * @param message
	 * @since 1.0.0
	 */
	public ItemProducerException(String message) {
		super(message);
	}
	
	/**
	 * 
	 * @param message
	 * @since 1.0.0
	 * @param cause
	 */
	public ItemProducerException(String message, Throwable cause) {
		super(message, cause);
	}
}
