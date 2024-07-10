package io.github.vuhoangha.common;

import net.openhft.chronicle.bytes.BytesIn;
import net.openhft.chronicle.bytes.BytesOut;
import net.openhft.chronicle.bytes.WriteBytesMarshallable;

import java.util.function.Function;

public class IntRingHashMap<V extends WriteBytesMarshallable> extends AbstractRingHashMap<Integer, V> implements WriteBytesMarshallable {

    public IntRingHashMap(int capacity) {
        super(capacity, IntRingNode::new);
    }


    public IntRingHashMap(BytesIn<?> bytes, Function<BytesIn<?>, V> creator) {
        super(bytes.readInt(), IntRingNode::new);

        // các item sẽ được đọc từ tail --> head
        int snapshotSize = bytes.readInt();
        for (int i = 0; i < snapshotSize; i++) {
            IntRingNode<V> node = new IntRingNode<V>(bytes, creator);
            put(node.key, node.value);
        }
    }


    @Override
    public void writeMarshallable(BytesOut bytes) {
        bytes.writeInt(capacity);
        bytes.writeInt(size);

        // các item sẽ được viết từ tail --> head
        IntRingNode<V> cursor = (IntRingNode<V>) tail;
        while (cursor != null) {
            cursor.writeMarshallable(bytes);
            cursor = (IntRingNode<V>) cursor.next;
        }
    }


}
