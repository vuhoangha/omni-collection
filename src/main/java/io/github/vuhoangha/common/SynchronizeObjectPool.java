package io.github.vuhoangha.common;

import lombok.Getter;

import java.util.function.Supplier;

/**
 * Object pool chỉ hoạt động trong môi trường đơn luồng
 */
public class SynchronizeObjectPool<T> {

    private final T[] pool;
    private final Supplier<T> factory;
    @Getter
    private final int capacity;
    private int currentIndex = -1; // Chỉ số của phần tử tiếp theo sẽ được trả về


    public SynchronizeObjectPool(T[] pool, Supplier<T> factory) {
        this.factory = factory;
        this.pool = pool;
        this.capacity = pool.length;
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


    public boolean push(T object) {
        if (object != null && currentIndex < capacity - 1) {
            currentIndex++;
            pool[currentIndex] = object;
            return true;
        }
        return false;
    }


    public void clear() {
        currentIndex = -1;
        for (int i = 0; i < capacity; i++) {
            pool[i] = null;
        }
    }

    public int getSize() {
        return currentIndex + 1;
    }

    public boolean isFull() {
        return currentIndex == capacity - 1;
    }

}
