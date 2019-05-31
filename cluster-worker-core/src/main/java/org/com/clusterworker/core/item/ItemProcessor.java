package org.com.clusterworker.core.item;

import java.io.Serializable;

/**
 * Contract to item processor implementations.
 * @author rs-renato
 * @since 1.0.0
 * @param <T> type of data to be processed by this processor.
 */
public interface ItemProcessor<T> extends Serializable{
	
	/**
	 * Process the item from the queue
	 * @param item to be processed
	 * @since 1.0.0
	 */
	void process(T item);
}
