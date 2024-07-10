package io.github.vuhoangha.common;

import net.openhft.chronicle.bytes.BytesIn;
import net.openhft.chronicle.bytes.BytesOut;
import net.openhft.chronicle.bytes.WriteBytesMarshallable;

import java.util.function.Function;

public class LongRingHashMap<V extends WriteBytesMarshallable> extends AbstractRingHashMap<Long, V> implements WriteBytesMarshallable {

    public LongRingHashMap(int capacity) {
        super(capacity, LongRingNode::new);
    }


    public LongRingHashMap(BytesIn<?> bytes, Function<BytesIn<?>, V> creator) {
        super(bytes.readInt(), LongRingNode::new);

        // các item sẽ được đọc từ tail --> head
        int snapshotSize = bytes.readInt();
        for (int i = 0; i < snapshotSize; i++) {
            LongRingNode<V> node = new LongRingNode<V>(bytes, creator);
            put(node.key, node.value);
        }
    }


    @Override
    public void writeMarshallable(BytesOut bytes) {
        bytes.writeInt(capacity);
        bytes.writeInt(size);

        // các item sẽ được viết từ tail --> head
        LongRingNode<V> cursor = (LongRingNode<V>) tail;
        while (cursor != null) {
            cursor.writeMarshallable(bytes);
            cursor = (LongRingNode<V>) cursor.next;
        }
    }


}
