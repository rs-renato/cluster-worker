package br.gov.go.sefaz.clusterworker.core.exception;

import br.gov.go.sefaz.clusterworker.core.item.ItemProducer;

/**
 * Exception used in {@link ItemProducer} implementations
 * @author renato.rsilva
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
