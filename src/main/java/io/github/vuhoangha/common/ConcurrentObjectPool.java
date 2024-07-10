package io.github.vuhoangha.common;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * Object Pool sử dụng trong môi trường đa luồng
 */
public class ConcurrentObjectPool<T> {

    private final T[] pool;
    private final int capacity;
    private final Supplier<T> factory;
    private final AtomicInteger index = new AtomicInteger(-1);

    public ConcurrentObjectPool(T[] pool, Supplier<T> factory) {
        this.capacity = pool.length;
        this.factory = factory;
        this.pool = pool;
    }


    public T pop() {
        synchronized (pool) {
            return index.get() <= -1
                    ? factory.get()
                    : pool[index.getAndDecrement()];
        }
    }


    public boolean push(T object) {
        synchronized (pool) {
            if (index.get() + 1 >= capacity)
                return false;
            else {
                pool[index.incrementAndGet()] = object;
                return true;
            }
        }
    }


    public int size() {
        return index.get() + 1;
    }

    public boolean isEmpty() {
        return index.get() == -1;
    }

    public boolean isFull() {
        return index.get() + 1 == capacity;
    }

}