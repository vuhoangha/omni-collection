package io.github.vuhoangha.common;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * Object pool hoạt động trong môi trường đa luồng
 * học tập cách Lmax Disruptor
 * TODO kiểm tra lại logic xem đúng ko nhé. Đang có nhiều case liên quan đến các luồng chưa chắc đã đúng đâu
 */
public class ConcurrencyRingObjectPool<T> {

    private final T[] pool;
    private final int capacity;
    private final int masking;
    private final Supplier<T> factory;
    private final AtomicInteger writeIndex = new AtomicInteger(0);      // vị trí viết tiếp theo
    private final AtomicInteger readIndex = new AtomicInteger(0);       // vị trí đọc tiếp theo

    public ConcurrencyRingObjectPool(T[] pool, Supplier<T> factory) {

        this.capacity = pool.length;
        this.masking = this.capacity - 1;
        this.factory = factory;
        this.pool = pool;

        if (Integer.bitCount(capacity) != 1)
            throw new IllegalArgumentException("Ring size must be a power of 2");

    }


    public T pop() {

        while (true) {

            int currentReadIndex = readIndex.get();
            int currentWriteIndex = writeIndex.get();

            if (currentReadIndex == currentWriteIndex)  // ko có item nào trong queue
                return factory.get();

            T object = pool[currentReadIndex];
            int newReadIndex = (currentReadIndex + 1) & masking;
            if (readIndex.compareAndSet(currentReadIndex, newReadIndex))
                return object;
        }
    }


    public boolean push(T object) {

        if (object == null)
            return false;

        while (true) {
            int currentWriteIndex = writeIndex.get();
            int nextWriteIndex = (currentWriteIndex + 1) & masking;
            if (nextWriteIndex == readIndex.get())    // Buffer is full
                return false;

            if (writeIndex.compareAndSet(currentWriteIndex, nextWriteIndex)) {
                pool[currentWriteIndex] = object;
                return true;
            }
        }
    }
}
