package br.gov.go.sefaz.clusterworker.core.exception;

/**
 * Exception throwed for mandatory annotations.
 */
public class MandatoryAnnotationException extends RuntimeException {

    public MandatoryAnnotationException(String message) {
        super(message);
    }
}
