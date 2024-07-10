package io.github.vuhoangha.common;

public class RingHashMap<K, V> extends AbstractRingHashMap<K, V> {

    public RingHashMap(int capacity) {
        super(capacity, BaseRingNode::new);
    }

}
