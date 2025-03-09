package com.es.phoneshop.utils;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

public class ReadWriteLockWrapper {
    private final Lock readLock;
    private final Lock writeLock;

    public ReadWriteLockWrapper() {
        ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
        readLock = readWriteLock.readLock();
        writeLock = readWriteLock.writeLock();
    }

    public <T> T read(Supplier<T> function) {
        readLock.lock();
        try {
            return function.get();
        } finally {
            readLock.unlock();
        }
    }

    public void write(Writer writer) {
        writeLock.lock();
        try {
            writer.execute();
        } finally {
            writeLock.unlock();
        }
    }
}
