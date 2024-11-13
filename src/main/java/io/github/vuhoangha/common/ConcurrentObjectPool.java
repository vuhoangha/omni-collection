package io.github.vuhoangha.common;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * Object Pool sử dụng trong môi trường đa luồng
 */
// TODO thử với ý tưởng sử dụng Node queue như mình hay dùng ấy, có head, tail. Nhưng ở đây thêm 1 biến nữa là max head ý là node cao nhất khả dụng để ghi vào, đỡ phải chứa node vào pool
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


    public boolean push(Iterable<T> objects) {
        synchronized (pool) {
            for (T t : objects) {
                if (index.get() + 1 >= capacity)
                    return false;
                else {
                    pool[index.incrementAndGet()] = t;
                }
            }
        }
        return true;
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