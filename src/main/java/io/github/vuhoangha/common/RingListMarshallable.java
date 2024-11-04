package io.github.vuhoangha.common;

import net.openhft.chronicle.bytes.BytesIn;
import net.openhft.chronicle.bytes.BytesOut;
import net.openhft.chronicle.bytes.WriteBytesMarshallable;

import java.util.LinkedList;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * 1 List có tối đa "maxSize" item. Nếu đầy nó sẽ tự xóa phần tử cũ nhất và thêm mới
 */
public class RingListMarshallable<T extends WriteBytesMarshallable> implements WriteBytesMarshallable {

    private final LinkedList<T> list = new LinkedList<>();
    private final int maxSize;

    public RingListMarshallable(int maxSize) {
        this.maxSize = maxSize;
    }

    public RingListMarshallable(final BytesIn bytes, final Function<BytesIn, T> creator) {
        this.maxSize = bytes.readInt();
        int size = bytes.readInt();
        for (int i = 0; i < size; i++) {
            list.addLast(creator.apply(bytes));
        }
    }

    public T popOldestIfFull() {
        return list.size() == maxSize
                ? list.removeFirst()
                : null;
    }

    public void add(T element) {
        list.addLast(element);
    }

    public T get(int index) {
        return list.get(index);
    }

    public int size() {
        return list.size();
    }

    // lấy phần tử thêm vào mới nhất
    // dùng cẩn thận vì nó đang tham chiếu đến thẳng dữ liệu
    public T last(){
        return list.getLast();
    }

    // lặp từ phần tử cũ nhất tới mới nhất
    public void foreach(Consumer<T> consumer) {
        for (T element : list) {
            consumer.accept(element);
        }
    }

    public void clear() {
        list.clear();
    }

    @Override
    public void writeMarshallable(BytesOut bytes) {
        bytes.writeInt(maxSize);
        bytes.writeInt(list.size());

        // lặp từ phần tử cũ nhất tới mới nhất
        for (T element : list) {
            element.writeMarshallable(bytes);
        }
    }

}
