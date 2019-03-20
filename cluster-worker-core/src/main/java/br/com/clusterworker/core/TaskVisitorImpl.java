package br.com.clusterworker.core;

import br.com.clusterworker.lock.AtomicLock;

import java.util.Collection;

/**
 * Created by renato-rs on 04/11/2016.
 */
class TaskVisitorImpl<T> implements TaskVisitor<T> {

    private AtomicLock atomicLock;
    private T type;

    public TaskVisitorImpl(AtomicLock atomicLock) {
        this.atomicLock = atomicLock;
    }

    @Override
    public Collection<T> visit(TaskProduceLockable<T> taskProduceLockable) {
        return taskProduceLockable.produce(atomicLock);
    }

    @Override
    public Collection<T> visit(TaskProduceUnlockable<T> taskProduceUnlockable) {
        return taskProduceUnlockable.produce();
    }

    @Override
    public void visit(TaskProcessUnlockable<T> taskProcessUnlockable) {
        taskProcessUnlockable.process(type);
    }

    @Override
    public void visit(TaskProcessLockable<T> taskProcessLockable) {
        taskProcessLockable.process(type, atomicLock);
    }

    public void setType(T type) {
        this.type = type;
    }
}
