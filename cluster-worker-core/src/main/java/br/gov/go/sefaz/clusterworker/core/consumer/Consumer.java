package br.gov.go.sefaz.clusterworker.core.consumer;

/**
 * Contract to consumer implementations.
 * @param <T> type of data to be consummed by this consumer.
 */
public interface Consumer<T> {

    /**
     * Consume the type of T.
     * @return T
     */
    T consume();
}
