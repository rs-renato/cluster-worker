package br.com.clusterworker.core;

import java.util.Collection;

/**
 * Contract to task produce on client's implementation.
 * @param <T> type of this task produce.
 */
public abstract class TaskProduceUnlockable<T> extends TaskProduce<T> {

    /**
     * Produce an specific types on client's implementation.
     * @return collection of T
     */
    protected abstract Collection<T> produce();

    @Override
    final Collection<T> accept(TaskVisitor<T> taskVisitor) {
        return taskVisitor.visit(this);
    }
}