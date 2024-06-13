package io.github.vuhoangha.common;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntBinaryOperator;
import java.util.function.Supplier;

/**
 * Object Pool sử dụng trong môi trường đa luồng
 */
public class ConcurrentObjectPool<T> {

//    cho tôi ví dụ sử dụng Array để tạo object pool có kích thước cố định. Khi lấy phần tử trong pool, nếu có sẵn thì trả ra luôn, nếu ko thì tạo mới phần tử rồi trả. Khi thêm vào pool, nếu đầy rồi thì thôi không thêm

    private final ConcurrentLinkedQueue<T> pool = new ConcurrentLinkedQueue<>();
    private final AtomicInteger currentSize = new AtomicInteger(0);      // ko trực tiếp lấy size từ pool vì độ phức tạp thuật toán O(n)
    private final Supplier<T> factory;
    private final int poolSize;
    private final IntBinaryOperator accumulatorFunc;


    public ConcurrentObjectPool(int poolSize, Supplier<T> factory) {
        this.poolSize = poolSize;
        this.factory = factory;
        accumulatorFunc = (current, unuse) -> current < poolSize ? current + 1 : current;        // hàm này khi được gọi sẽ trả về giá trị cũ trước khi cập nhật
    }


    public T pop() {
        T object = pool.poll();

        if (object == null) {
            return factory.get();
        }

        currentSize.decrementAndGet();
        return object;
    }


    // pool chưa đầy mới thêm vào
    public void push(T object) {
        if (object != null && currentSize.getAndAccumulate(1, accumulatorFunc) < poolSize) {
            pool.offer(object);
        }
    }

}