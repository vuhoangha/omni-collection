package io.github.vuhoangha.common;

import lombok.Getter;

import java.util.HashMap;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * Hoạt động đơn luồng
 * Quản lý một danh sách phần tử theo thứ tự chèn vào
 * Có thể lấy phần tử cũ nhất, mới nhất, tổng số phần tử, giá trị phần tử theo key với O(1)
 * Lấy phần tử trong khoảng với O(n)
 * Insert, delete ở đầu, cuối danh sách O(1)
 * Delete ở giữa danh sách O(1)
 */
@SuppressWarnings("UnusedReturnValue")
public abstract class AbstractQueueHashMap<K, V> {

    @Getter
    protected IQueueNode<K, V> head;                                     // phần tử mới nhất chèn vào
    @Getter
    protected IQueueNode<K, V> tail;                                     // phần tử cũ nhất chèn vào
    protected final HashMap<K, IQueueNode<K, V>> map;                    // dùng để chứa Key và tham chiếu tới giá trị
    @Getter
    protected int size;                                                  // tổng số phần tử hiện tại
    protected final SynchronizeObjectPool<IQueueNode<K, V>> itemPool;    // pool dự trữ các item trong danh sách, tránh việc khởi tạo


    public AbstractQueueHashMap(int nodePoolSize, Supplier<IQueueNode<K, V>> nodeFactory) {
        this.map = new HashMap<>();
        this.itemPool = new SynchronizeObjectPool<>(new IQueueNode[nodePoolSize], nodeFactory);
    }


    /**
     * thêm giá trị mới vào
     * nếu đã có thì bỏ qua
     */
    public boolean put(K key, V value) {

        if (map.containsKey(key)) return false;

        // lấy object để chứa phần tử mới từ pool và cập nhật giá trị
        IQueueNode<K, V> newHead = itemPool.pop();
        newHead.setKey(key);
        newHead.setValue(value);

        // cập nhật head, tail
        if (size == 0) {
            head = newHead;
            tail = newHead;
        } else {
            head.setNext(newHead);
            newHead.setPrev(head);
            head = newHead;
        }

        // cập nhật size
        size++;

        // thêm vào map
        map.put(key, newHead);

        return true;
    }


    // xóa 1 phần tử
    public V remove(K key) {

        if (size == 0 || !map.containsKey(key)) return null;

        IQueueNode<K, V> removedNode = map.remove(key);
        size--;

        if (size == 0) {        // ko có phần tử thì head, tail bằng null
            head = null;
            tail = null;
        } else {
            // cập nhật node phía trước
            if (removedNode.getNext() != null) removedNode.getNext().setPrev(removedNode.getPrev());
            // cập nhật node phía sau
            if (removedNode.getPrev() != null) removedNode.getPrev().setNext(removedNode.getNext());
        }

        // lấy giá trị của phần tử cũ nhất
        V value = removedNode.getValue();

        // reset phần tử cũ nhất và đẩy vào pool
        removedNode.clear();
        itemPool.push(removedNode);

        return value;
    }


    public boolean exist(K key) {
        return map.containsKey(key);
    }

    public IQueueNode<K, V> get(K key) {
        return map.get(key);
    }

    public V getValue(K key) {
        IQueueNode<K, V> item = map.get(key);
        return item == null ? null : item.getValue();
    }

    public V getHeadValue() {
        return head == null ? null : head.getValue();
    }

    public V getTailValue() {
        return tail == null ? null : tail.getValue();
    }


    // di chuyển đến vị trí chính xác trong List nơi chứa key
    public IQueueNode<K, V> moveTo(K key) {
        if (map.containsKey(key)) {
            return map.get(key);
        }
        return null;
    }


    // lặp từ phần tử cũ nhất tới mới nhất
    public void foreach(BiConsumer<K, V> consumer) {
        if (size == 0) return;

        IQueueNode<K, V> cursor = tail;

        do {
            consumer.accept(cursor.getKey(), cursor.getValue());
            cursor = cursor.getNext();
        } while (cursor != null);
    }


    public void clear() {
        head = null;
        tail = null;
        map.clear();
        size = 0;
        itemPool.clear();
    }

}
