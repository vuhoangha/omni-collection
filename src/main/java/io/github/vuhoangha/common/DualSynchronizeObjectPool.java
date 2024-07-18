package io.github.vuhoangha.common;

import java.util.function.Supplier;

/**
 * object pool cho phép luồng lấy và nhận object ở 2 thread độc lập
 * lấy và trả object đều là single thread nên nó nhanh tương đương nhau
 * khi lấy object mà supply pool bị hết thì sẽ lấy từ consume pool đưa vào
 * việc đồng bộ giữa 2 object pool nội bộ khá tốn CPU nên số lượng phần tử phải lớn 1 chút để hạn chế việc chuyển object giữa 2 pool
 */
public class DualSynchronizeObjectPool<T> {

    private final SynchronizeObjectPool<T> supplyPool;      // pool để lấy object
    private final SynchronizeObjectPool<T> consumePool;      // pool để trả object
    private final int capacity;
    private final Supplier<T> factory;


    public DualSynchronizeObjectPool(T[] pool, Supplier<T> factory) {
        this.capacity = pool.length;
        this.factory = factory;
        supplyPool = new SynchronizeObjectPool<>(pool.clone(), factory);
        consumePool = new SynchronizeObjectPool<>(pool.clone(), factory);
    }


    public T pop() {
        // nếu supply pool hết object thì lấy từ consume pool chuyển qua
        if (supplyPool.isEmpty()) {
            synchronized (consumePool) {
                if (consumePool.isFull()) {      // phải chờ cho pool này full mới lấy tại vì tránh trường hợp tổng số lượng phần tử ít hơn kích thước pool nhưng cứ lấy qua lại sẽ ko hiệu quả. Ví dụ pool có capacity 1_000_000 nhưng tối đa chỉ có 100 phần tử thành ra cứ tới ngưỡng 100 nó đã chuyển rồi, ko hiệu quả
                    while (!consumePool.isEmpty()) {
                        supplyPool.push(consumePool.pop());
                    }
                }
            }
        }
        return supplyPool.pop();
    }

    public void push(T item) {
        consumePool.push(item);
    }

}
