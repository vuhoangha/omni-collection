package io.github.vuhoangha.common;

/**
 * Một node sẽ bao gồm
 * - key: unique key của node này
 * - value: giá trị node này
 * - next: node tiếp theo
 * - prev: node phía trước
 */
public interface IQueueNode<K, V> {

    K getKey();

    void setKey(K key);

    V getValue();

    void setValue(V value);

    IQueueNode<K, V> getNext();

    void setNext(IQueueNode<K, V> next);

    IQueueNode<K, V> getPrev();

    void setPrev(IQueueNode<K, V> prev);

    void clear();

}