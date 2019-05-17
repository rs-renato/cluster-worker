package br.gov.go.sefaz.clusterworker.core.exception;

/**
 * General exception for Cluster Worker
 * @author renato-rs
 * @since 1.0
 */
public class ClusterWorkerException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	/**
	 * 
	 * @param cause
	 * @since 1.0
	 */
	public ClusterWorkerException(Throwable cause) {
		super(cause);
	}
	
	/**
	 * 
	 * @param message
	 * @since 1.0
	 */
	public ClusterWorkerException(String message) {
		super(message);
	}
	
	/**
	 * 
	 * @param message
	 * @param cause
	 * @since 1.0
	 */
	public ClusterWorkerException(String message, Throwable cause) {
		super(message, cause);
	}
}