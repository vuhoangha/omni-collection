package io.github.vuhoangha.common;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.openhft.chronicle.bytes.BytesIn;
import net.openhft.chronicle.bytes.BytesOut;
import net.openhft.chronicle.bytes.WriteBytesMarshallable;

import java.util.function.Function;

@Getter
@Setter
@NoArgsConstructor
public class LongRingNode<V extends WriteBytesMarshallable> implements IRingNode<Long, V>, WriteBytesMarshallable {

    public long key;                                   // key phần tử hiện tại
    public V value;                                    // giá trị phần tử hiện tại
    public IRingNode<Long, V> next;                    // tham chiếu phần tử đứng sau
    public IRingNode<Long, V> prev;                    // tham chiếu phần tử đứng trước

    public LongRingNode(BytesIn<?> bytes, Function<BytesIn<?>, V> creator) {
        key = bytes.readLong();
        value = creator.apply(bytes);
    }

    @Override
    public void setKey(Long key) {
        this.key = key;
    }

    @Override
    public Long getKey() {
        return this.key;
    }

    @Override
    public void setNext(IRingNode<Long, V> next) {     // Triển khai phương thức setNext
        this.next = next;
    }

    @Override
    public void setPrev(IRingNode<Long, V> prev) {     // Triển khai phương thức setPrev
        this.prev = prev;
    }

    @Override
    public void clear() {
        key = 0;
        value = null;
        next = null;
        prev = null;
    }

    @Override
    public void writeMarshallable(BytesOut bytes) {
        bytes.writeLong(key);
        value.writeMarshallable(bytes);
    }

}
