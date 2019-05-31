package org.com.clusterworker.core.exception;

/**
 * Exception used validations of mandatory annotations.
 * @author rs-renato
 * @since 1.0.0
 */
public class MandatoryAnnotationException extends ClusterWorkerException {

	private static final long serialVersionUID = -556398177959796631L;

	/**
	 * 
	 * @param message
	 * @since 1.0.0
	 */
	public MandatoryAnnotationException(String message) {
        super(message);
    }
}