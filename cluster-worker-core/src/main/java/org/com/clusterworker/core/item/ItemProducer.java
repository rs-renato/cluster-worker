package org.com.clusterworker.core.item;

import java.io.Serializable;
import java.util.Collection;

/**
 * Contract to item producer implementations.
 * @author rs-renato
 * @since 1.0.0
 * @param <T> type of data to be produced by this producer.
 */
public interface ItemProducer<T> extends Serializable {
	
	/**
	 * Produces the items to the queue.
	 * <br/><br/>
	 * <i>Note: This production ignores duplicated items. However, the queue's consumption happens in cuncurrent environment and consumers
	 * never rest, that is, if the collection has two duplicated items, lets say, at collection's position 10 and 500, and one consumer
	 * get the item at position 10 BEFORE the item at position 500 being put into the queue, that means another consumer will get that item and also process it.</i> 
	 * @return collection of items produced
	 * @since 1.0.0
	 */
	Collection<T> produce();
}