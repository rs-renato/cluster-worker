package br.com.clusterworker.lock;

import java.io.Serializable;

/**
 * Consntats sufix to the consumer/producer locks.
 */
public enum LockType implements Serializable{

    PRODUCER(".lock.producer"),
    CONSUMER(".lock.consumer");

    private String baseLockName;

    LockType(String baseLockName) {
        this.baseLockName = baseLockName;
    }

    @Override
    public String toString() {
        return this.baseLockName;
    }
}
