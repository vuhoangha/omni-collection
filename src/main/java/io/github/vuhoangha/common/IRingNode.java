package io.github.vuhoangha.common;

/**
 * Một node sẽ bao gồm
 * - key: unique key của node này
 * - value: giá trị node này
 * - next: node tiếp theo
 * - prev: node phía trước
 */
public interface IRingNode<K, V> {

    K getKey();

    void setKey(K key);

    V getValue();

    void setValue(V value);

    IRingNode<K, V> getNext();

    void setNext(IRingNode<K, V> next);

    IRingNode<K, V> getPrev();

    void setPrev(IRingNode<K, V> prev);

    void clear();

}