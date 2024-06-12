package io.github.vuhoangha.common;

import java.util.concurrent.locks.LockSupport;

/**
 * dùng để chờ đợi kết quả trả về từ một hàm bất đồng bộ. Có thể tái sử dụng
 */
public class Promise<T> {

    private T value;

    private Exception ex;


    // reset kết quả
    public void clear() {
        value = null;
        ex = null;
    }


    // có kết quá
    public void complete(T value) {
        this.value = value;
    }


    // có kết quá
    public void completeWithException(Exception ex) {
        this.ex = ex;
    }


    /**
     * @param intervalTime thời gian mỗi lần kiểm tra kết quả (nanoseconds)
     */
    public T get(long intervalTime) throws Exception {

        // kiểm tra luôn xem có kết quả ko
        if (value != null) return value;
        if (ex != null) throw ex;

        // chờ 10 lần rồi mới đưa vào vòng lặp
        
        LockSupport.parkNanos(intervalTime);
        if (value != null) return value;
        if (ex != null) throw ex;

        LockSupport.parkNanos(intervalTime);
        if (value != null) return value;
        if (ex != null) throw ex;

        LockSupport.parkNanos(intervalTime);
        if (value != null) return value;
        if (ex != null) throw ex;

        LockSupport.parkNanos(intervalTime);
        if (value != null) return value;
        if (ex != null) throw ex;

        LockSupport.parkNanos(intervalTime);
        if (value != null) return value;
        if (ex != null) throw ex;

        LockSupport.parkNanos(intervalTime);
        if (value != null) return value;
        if (ex != null) throw ex;

        LockSupport.parkNanos(intervalTime);
        if (value != null) return value;
        if (ex != null) throw ex;

        LockSupport.parkNanos(intervalTime);
        if (value != null) return value;
        if (ex != null) throw ex;

        LockSupport.parkNanos(intervalTime);
        if (value != null) return value;
        if (ex != null) throw ex;

        LockSupport.parkNanos(intervalTime);
        if (value != null) return value;
        if (ex != null) throw ex;
        
        // chờ quá lâu thì đưa vào vòng lặp 
        do {
            LockSupport.parkNanos(intervalTime);
            if (value != null) return value;
            if (ex != null) throw ex;
        } while (true);

    }

}
