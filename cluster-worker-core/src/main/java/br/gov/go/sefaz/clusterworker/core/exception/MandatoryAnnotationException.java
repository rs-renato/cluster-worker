package br.gov.go.sefaz.clusterworker.core.exception;

/**
 * Exception throwed for mandatory annotations.
 * @author renato-rs
 * @since 1.0
 */
public class MandatoryAnnotationException extends RuntimeException {

	private static final long serialVersionUID = -556398177959796631L;

	/**
	 * Constructor of MandatoryAnnotationException
	 * @param message the message
	 */
	public MandatoryAnnotationException(String message) {
        super(message);
    }
}