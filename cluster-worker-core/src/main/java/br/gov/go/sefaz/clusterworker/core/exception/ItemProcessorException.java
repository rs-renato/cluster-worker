package br.gov.go.sefaz.clusterworker.core.exception;

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
