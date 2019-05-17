package br.gov.go.sefaz.clusterworker.core.exception;

import br.gov.go.sefaz.clusterworker.core.item.ItemProcessor;

/**
 * Exception used in {@link ItemProcessor} implementations
 * @author renato-rs
 * @since 1.0
 */
public class ItemProcessorException extends ClusterWorkerException {

	private static final long serialVersionUID = 1L;

	/**
	 * 
	 * @param cause
	 * @since 1.0
	 */
	public ItemProcessorException(Throwable cause) {
		super(cause);
	}
	
	/**
	 * 
	 * @param message
	 * @since 1.0
	 */
	public ItemProcessorException(String message) {
		super(message);
	}
	
	/**
	 * 
	 * @param message
	 * @since 1.0
	 * @param cause
	 */
	public ItemProcessorException(String message, Throwable cause) {
		super(message, cause);
	}
}
