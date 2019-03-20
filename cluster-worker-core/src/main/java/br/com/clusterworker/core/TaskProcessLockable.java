package br.com.clusterworker.core;

import br.com.clusterworker.lock.AtomicLock;

import java.util.Collection;

/**
 * Contract to task process lockable on client's implementation.
 * @param <T> type of this task process.
 */
public abstract class TaskProcessLockable<T> extends TaskProcess<T> {

    /**
     * Process an specific type on client's implementation.
     * @param type type to be processed.
     * @param atomicLock
     */
    protected abstract void process(T type, AtomicLock atomicLock);

    @Override
    final Collection<T> accept(TaskVisitor<T> taskVisitor) {
        taskVisitor.visit(this);
        return null;
    }
}
