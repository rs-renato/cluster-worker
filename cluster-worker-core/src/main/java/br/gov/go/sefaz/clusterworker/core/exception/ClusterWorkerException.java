package br.gov.go.sefaz.clusterworker.core.exception;

public class ClusterWorkerException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ClusterWorkerException(Throwable cause) {
		super(cause);
	}
	
	public ClusterWorkerException(String message) {
		super(message);
	}
	
	public ClusterWorkerException(String message, Throwable cause) {
		super(message, cause);
	}
}