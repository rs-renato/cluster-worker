package org.com.clusterworker.core.consumer;

/**
 * Consumer queue strategy to the general client's {@link Consumer} implementation.
 * @author rs-renato
 * @since 1.0.0
 */
public enum ConsumerStrategy {
    /**
     * Accept a null element from the queue even if a timeout ocurrs. This is a non-blocking strategy.
     * @since 1.0.0
     */
    ACCEPT_NULL,

    /**
     * Waits until an element become available from the queue. This is a blocking strategy.
     * @since 1.0.0
     */
    WAIT_ON_AVAILABLE;
}