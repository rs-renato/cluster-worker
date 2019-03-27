package br.gov.go.sefaz.clusterworker.core.listener;

/**
 * Listener used for handle a shutdown 
 * @author renato-rs
 * @since 1.0
 */
public interface ShutdownListener {

	/**
	 * Shutdown handle 
	 */
	public void shutdown();
}
