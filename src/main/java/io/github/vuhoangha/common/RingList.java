package io.github.vuhoangha.common;

import java.util.LinkedList;

/**
 * 1 List có tối đa "maxSize" item. Nếu đầy nó sẽ tự xóa phần tử cũ nhất và thêm mới
 * khác "RingListObject" ở chỗ nó ko cần phần tử phải "WriteBytesMarshallable"
 */
public class RingList<T> {

    private final LinkedList<T> list = new LinkedList<>();
    private final int maxSize;

    public RingList(int maxSize) {
        this.maxSize = maxSize;
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


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("RingList{");
        sb.append("maxSize=").append(maxSize).append(", currentSize=").append(list.size()).append(", items=[");

        for (T t : list) {
            sb.append(t.toString()).append(", ");
        }

        if (!list.isEmpty()) {
            sb.deleteCharAt(sb.length() - 2); // Remove the trailing comma
        }

        sb.append("]");
        return sb.toString();
    }

}
