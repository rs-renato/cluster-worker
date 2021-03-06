package org.com.clusterworker.core.exception;

/**
 * General exception for Cluster Worker
 * @author rs-renato
 * @since 1.0.0
 */
public class ClusterWorkerException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	/**
	 * 
	 * @param cause
	 * @since 1.0.0
	 */
	public ClusterWorkerException(Throwable cause) {
		super(cause);
	}
	
	/**
	 * 
	 * @param message
	 * @since 1.0.0
	 */
	public ClusterWorkerException(String message) {
		super(message);
	}
	
	/**
	 * 
	 * @param message
	 * @param cause
	 * @since 1.0.0
	 */
	public ClusterWorkerException(String message, Throwable cause) {
		super(message, cause);
	}
}