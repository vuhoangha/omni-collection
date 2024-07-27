package io.github.vuhoangha.common;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BaseQueueNode<K, V> implements IQueueNode<K, V> {

    public K key;                                    // key phần tử hiện tại
    public V value;                                  // giá trị phần tử hiện tại
    public IQueueNode<K, V> next;                    // tham chiếu phần tử đứng sau
    public IQueueNode<K, V> prev;                    // tham chiếu phần tử đứng trước

    @Override
    public void setNext(IQueueNode<K, V> next) {     // Triển khai phương thức setNext
        this.next = next;
    }

    @Override
    public void setPrev(IQueueNode<K, V> prev) {     // Triển khai phương thức setPrev
        this.prev = prev;
    }

    @Override
    public void clear() {
        key = null;
        value = null;
        next = null;
        prev = null;
    }

}
