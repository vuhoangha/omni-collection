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
public class IntRingNode<V extends WriteBytesMarshallable> implements IRingNode<Integer, V>, WriteBytesMarshallable {

    public int key;                                   // key phần tử hiện tại
    public V value;                                    // giá trị phần tử hiện tại
    public IRingNode<Integer, V> next;                    // tham chiếu phần tử đứng sau
    public IRingNode<Integer, V> prev;                    // tham chiếu phần tử đứng trước

    public IntRingNode(BytesIn<?> bytes, Function<BytesIn<?>, V> creator) {
        key = bytes.readInt();
        value = creator.apply(bytes);
    }

    @Override
    public void setKey(Integer key) {
        this.key = key;
    }

    @Override
    public Integer getKey() {
        return this.key;
    }

    @Override
    public void setNext(IRingNode<Integer, V> next) {     // Triển khai phương thức setNext
        this.next = next;
    }

    @Override
    public void setPrev(IRingNode<Integer, V> prev) {     // Triển khai phương thức setPrev
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
        bytes.writeInt(key);
        value.writeMarshallable(bytes);
    }

}
