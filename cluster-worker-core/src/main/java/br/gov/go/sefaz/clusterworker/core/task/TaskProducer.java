package br.gov.go.sefaz.clusterworker.core.task;

import java.io.Serializable;
import java.util.Collection;

/**
 * Markup class to {@link TaskProducer} specializations.
 * @param <T> type of this type produce.
 */
public interface TaskProducer<T> extends Serializable {
	Collection<T> produce();
}
