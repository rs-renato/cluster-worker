package br.gov.go.sefaz.clusterworker.core.exception;

import br.gov.go.sefaz.clusterworker.core.item.ItemProducer;

/**
 * Exception used in {@link ItemProducer} implementations
 * @author renato-rs
 * @since 1.0
 */
public class ItemProducerException extends ClusterWorkerException {

	private static final long serialVersionUID = 1L;

	public ItemProducerException(Throwable cause) {
		super(cause);
	}
	
	public ItemProducerException(String message) {
		super(message);
	}
	
	public ItemProducerException(String message, Throwable cause) {
		super(message, cause);
	}
}
