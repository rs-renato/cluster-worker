package br.gov.go.sefaz.clusterworker.core.queue;

import br.gov.go.sefaz.clusterworker.core.consumer.Consumer;

/**
 * Consummer queue strategy to the general client's {@link Consumer} implementation.
 * @author renato-rs
 * @since 1.0
 */
public enum QueueStrategy {
    /**
     * Accept a null element from the queue even if a timeout ocurrs. This is a non-blocking strategy.
     */
    ACCEPT_NULL,

    /**
     * Waits till an element become available from the queue. This is a blocking strategy.
     */
    WAIT_ON_AVAILABLE;
}