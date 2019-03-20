package br.gov.go.sefaz.clusterworker.core.utils;

import br.gov.go.sefaz.clusterworker.core.consumer.Consumer;

/**
 * Queue strategy to the client's base {@link Consumer} implementation.
 */
public enum QueueStrategy {
    /**
     * Accept null element from the queue. This is a non-blocking strategy.
     */
    ACCEPT_NULL,

    /**
     * Wait on available item from the queue. This is a blocking strategy.
     */
    WAIT_ON_AVAILABLE;
}