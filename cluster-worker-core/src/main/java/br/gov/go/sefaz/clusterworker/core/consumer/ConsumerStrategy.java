package br.gov.go.sefaz.clusterworker.core.consumer;

/**
 * Consummer queue strategy to the client's {@link Consumer} implementation.
 */
public enum ConsumerStrategy {
    /**
     * Accept null element from the queue. This is a non-blocking strategy.
     */
    ACCEPT_NULL,

    /**
     * Wait on available item from the queue. This is a blocking strategy.
     */
    WAIT_ON_AVAILABLE;
}