package io.github.vuhoangha.common;

import java.util.List;
import java.util.function.Supplier;

/**
 * object pool kết hợp cả single thread và multithread
 * lấy object sẽ là single thread nên nhanh hơn, trả object sẽ multithread nên chậm hơn
 * khi việc lấy object mà object pool đồng bộ bị hết thì nó sẽ lấy từ object pool bất đồng bộ để đưa vào
 * việc đồng bộ giữa 2 object pool nội bộ sẽ khá tốn CPU nên class này nên sử dụng khi số lượng phần tử trong object pool lớn hơn nhiều so với số phần tử lấy ra
 */
public class HybridObjectPool<T> {

    private final SynchronizeObjectPool<T> supplyPool;      // pool để lấy object
    private final ConcurrentObjectPool<T> consumePool;      // pool để trả object
    private int capacity;
    private final Supplier<T> factory;


    public HybridObjectPool(T[] pool, Supplier<T> factory) {
        this.capacity = pool.length;
        this.factory = factory;
        supplyPool = new SynchronizeObjectPool<>(pool.clone(), factory);
        consumePool = new ConcurrentObjectPool<>(pool.clone(), factory);
    }


    public T pop() {
        // nếu pool sync hết object thì lấy hết từ pool async chuyển qua
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

    public void push(Iterable<T> items) {
        consumePool.push(items);
    }

}
