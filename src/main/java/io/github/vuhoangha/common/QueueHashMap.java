package io.github.vuhoangha.common;

public class QueueHashMap<K, V> extends AbstractQueueHashMap<K, V> {

    public QueueHashMap(int nodePoolSize) {
        super(nodePoolSize, BaseQueueNode::new);
    }

}
