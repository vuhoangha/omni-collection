package io.github.vuhoangha.common;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BaseRingNode<K, V> implements IRingNode<K, V> {

    public K key;                                   // key phần tử hiện tại
    public V value;                                 // giá trị phần tử hiện tại
    public IRingNode<K, V> next;                    // tham chiếu phần tử đứng sau
    public IRingNode<K, V> prev;                    // tham chiếu phần tử đứng trước

    @Override
    public void setNext(IRingNode<K, V> next) {     // Triển khai phương thức setNext
        this.next = next;
    }

    @Override
    public void setPrev(IRingNode<K, V> prev) {     // Triển khai phương thức setPrev
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
