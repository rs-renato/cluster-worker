package br.gov.go.sefaz.clusterworker.core.consumer;

/**
 * Contract to consumers implementations.
 * @author renato.rsilva
 * @since 1.0.0
 * @param <T> type of data to be consummed by this consumer.
 */
public interface Consumer<T> {

    /**
     * Consumes the type of T.
     * @return T type
     * @throws InterruptedException if the thread is interrupted
     * @since 1.0.0
     */
    T consume() throws InterruptedException;
}
