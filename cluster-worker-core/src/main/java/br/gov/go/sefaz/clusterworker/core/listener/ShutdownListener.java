package br.gov.go.sefaz.clusterworker.core.listener;

/**
 * Listener used for handle a shutdown 
 * @author renato.rsilva
 * @since 1.0.0
 */
public interface ShutdownListener {

	/**
	 * Shutdown handle
	 * @since 1.0.0
	 */
	public void shutdown();
}
