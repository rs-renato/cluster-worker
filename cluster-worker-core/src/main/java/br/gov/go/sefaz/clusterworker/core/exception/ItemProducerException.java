package br.gov.go.sefaz.clusterworker.core.exception;

public class ItemProducerException extends RuntimeException {

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
