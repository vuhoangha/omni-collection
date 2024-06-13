package io.github.vuhoangha.common;

import java.util.function.Supplier;

/**
 * Object pool chỉ hoạt động trong môi trường đơn luồng
 */
public class SynchronizeObjectPool<T> {

    private final T[] pool;
    private final Supplier<T> factory;
    private final int poolSize;
    private int currentIndex = -1; // Chỉ số của phần tử tiếp theo sẽ được trả về


    public SynchronizeObjectPool(T[] pool, Supplier<T> factory) {
        this.factory = factory;
        this.pool = pool;
        this.poolSize = pool.length;
    }


    public T pop() {
        if (currentIndex == -1) {
            return factory.get();
        }

        T object = pool[currentIndex];
        pool[currentIndex] = null;
        currentIndex--;
        return object;
    }


    public void push(T object) {
        if (object != null && currentIndex < poolSize - 1) {
            currentIndex++;
            pool[currentIndex] = object;
        }
    }


    public void clear() {
        currentIndex = -1;
        for (int i = 0; i < poolSize; i++) {
            pool[i] = null;
        }
    }
}
