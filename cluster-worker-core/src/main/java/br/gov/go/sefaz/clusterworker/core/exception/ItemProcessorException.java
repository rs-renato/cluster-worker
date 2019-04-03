package br.gov.go.sefaz.clusterworker.core.exception;

import br.gov.go.sefaz.clusterworker.core.item.ItemProcessor;

/**
 * Exception used in {@link ItemProcessor} implementations
 * @author renato-rs
 * @since 1.0
 */
public class ItemProcessorException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ItemProcessorException(Throwable cause) {
		super(cause);
	}
	
	public ItemProcessorException(String message) {
		super(message);
	}
	
	public ItemProcessorException(String message, Throwable cause) {
		super(message, cause);
	}
}
