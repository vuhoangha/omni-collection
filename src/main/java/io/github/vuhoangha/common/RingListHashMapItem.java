package io.github.vuhoangha.common;

import lombok.Getter;

@Getter
public class RingListHashMapItem<K, V> {
    protected K key;                                   // key phần tử hiện tại
    protected V value;                                 // giá trị phần tử hiện tại
    protected RingListHashMapItem<K, V> next;          // tham chiếu phần tử đứng sau
    protected RingListHashMapItem<K, V> prev;          // tham chiếu phần tử đứng trước

    protected void clear() {
        key = null;
        value = null;
        next = null;
        prev = null;
    }

}