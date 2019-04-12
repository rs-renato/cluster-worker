package br.gov.go.sefaz.clusterworker.core.item;

import java.io.Serializable;

/**
 * Contract to item processor implementations.
 * @author renato-rs
 * @since 1.0
 * @param <T> type of data to be processed by this processor.
 */
public interface ItemProcessor<T> extends Serializable{
	
	/**
	 * Process the item from the queue
	 * @param item to be processed
	 */
	void process(T item);
}
