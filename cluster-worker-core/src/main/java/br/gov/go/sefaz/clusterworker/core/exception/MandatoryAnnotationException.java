package br.gov.go.sefaz.clusterworker.core.exception;

/**
 * Exception throwed for mandatory annotations.
 */
public class MandatoryAnnotationException extends RuntimeException {

	private static final long serialVersionUID = -556398177959796631L;

	public MandatoryAnnotationException(String message) {
        super(message);
    }
}
