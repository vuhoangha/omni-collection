package io.github.vuhoangha.common;

import java.util.Arrays;
import java.util.function.Consumer;

/**
 * Pool chứa các object mà khi lấy ra sẽ trả lần lượt theo thuật toán Round Robin
 * Không thread-safe
 * Các phần tử phải tự thread-safe
 * Không có hàm push, các phần tử phải khởi tạo hoàn toàn ngay từ đầu
 */
public class RoundRobinObjectPool<T> {

    private T[] pool;
    private long capacity;
    private long sequence = -1;

    public RoundRobinObjectPool(T[] pool) {
        this.pool = pool;
        this.capacity = pool.length;
    }


    public T get() {
        int nextIndex = (int) (++sequence % capacity);
        if (sequence == Long.MAX_VALUE) {
            sequence = -1;
        }
        return pool[nextIndex];
    }


    public void foreach(Consumer<T> action) {
        for (T t : pool) {
            action.accept(t);
        }
    }


    public void clear() {
        Arrays.fill(pool, null);
        sequence = -1;
        pool = null;
        capacity = 0;
    }

}
