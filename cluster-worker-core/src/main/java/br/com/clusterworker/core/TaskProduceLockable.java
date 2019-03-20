package br.com.clusterworker.core;

import java.util.Collection;

import br.com.clusterworker.lock.AtomicLock;


/**
 * Contract to task produce lockable on client's implementation.
 * @param <T> type of this task produce.
 */
public abstract class TaskProduceLockable<T> extends TaskProduce<T> {

    /**
     * Produce an specific types on client's implementation.
     * @param atomicLock
     * @return collection of T
     */
    protected abstract Collection<T> produce(AtomicLock atomicLock);

    @Override
    final Collection<T> accept(TaskVisitor<T> taskVisitor) {
        return taskVisitor.visit(this);
    }
}