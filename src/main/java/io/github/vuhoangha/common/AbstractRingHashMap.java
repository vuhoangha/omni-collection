package io.github.vuhoangha.common;

import lombok.Getter;

import java.util.HashMap;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * Hoạt động đơn luồng
 * Quản lý một danh sách phần tử theo thứ tự chèn vào
 * Giới hạn số phần tử tối đa, khi đầy sẽ tự động xóa phần tử cũ nhất và chèn phần tử mới vào
 * Có thể lấy phần tử cũ nhất, mới nhất, tổng số phần tử, giá trị phần tử theo key với O(1)
 * Lấy phần tử trong khoảng với O(n)
 * Insert, delete ở đầu, cuối danh sách O(1)
 * Insert, delete ở giữa danh sách O(n)
 */
@SuppressWarnings("UnusedReturnValue")
public abstract class AbstractRingHashMap<K, V> {

    @Getter
    protected IRingNode<K, V> head;                                     // phần tử mới nhất chèn vào
    @Getter
    protected IRingNode<K, V> tail;                                     // phần tử cũ nhất chèn vào
    protected final HashMap<K, IRingNode<K, V>> map;                    // dùng để chứa Key và tham chiếu tới giá trị
    @Getter
    protected int size;                                                 // tổng số phần tử hiện tại
    protected final int capacity;                                       // tổng số phần tử tối đa
    protected final SynchronizeObjectPool<IRingNode<K, V>> itemPool;    // pool dự trữ các item trong danh sách, tránh việc khởi tạo


    public AbstractRingHashMap(int capacity, Supplier<IRingNode<K, V>> nodeFactory) {
        this.map = new HashMap<>();
        this.capacity = capacity;
        this.itemPool = new SynchronizeObjectPool<>(new IRingNode[capacity], nodeFactory);
    }


    /**
     * thêm giá trị mới vào
     * nếu đã có thì bỏ qua
     * nếu đầy sẽ tự động xóa phần tử cũ nhất và thêm mới
     */
    public boolean put(K key, V value) {

        if (map.containsKey(key)) return false;

        // nếu đầy, xóa phần tử cũ nhất
        popIfFull();

        // lấy object để chứa phần tử mới từ pool và cập nhật giá trị
        IRingNode<K, V> newHead = itemPool.pop();
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


    // lấy giá trị của phần tử cũ nhất nếu list đã đầy
    public V popIfFull() {

        if (size < capacity) return null;

        IRingNode<K, V> oldTail = tail;

        if (size == 1) {        // có 1 phần tử thì xóa đi thì head, tail bằng null
            head = null;
            tail = null;
        } else {                // update tail mới
            tail = tail.getNext();
            tail.setPrev(null);
        }

        // cập nhật số lượng phần tử
        size--;

        // xóa khỏi map
        map.remove(oldTail.getKey());

        // lấy giá trị của phần tử cũ nhất
        V value = oldTail.getValue();

        // reset phần tử cũ nhất và đẩy vào pool
        oldTail.clear();
        itemPool.push(oldTail);

        return value;
    }


    public boolean exist(K key) {
        return map.containsKey(key);
    }

    public IRingNode<K, V> get(K key) {
        return map.get(key);
    }

    public V getValue(K key) {
        IRingNode<K, V> item = map.get(key);
        return item == null ? null : item.getValue();
    }

    public V getHeadValue() {
        return head == null ? null : head.getValue();
    }

    public V getTailValue() {
        return tail == null ? null : tail.getValue();
    }


    // di chuyển đến vị trí chính xác trong List nơi chứa key
    public IRingNode<K, V> moveTo(K key) {
        if (map.containsKey(key)) {
            return map.get(key);
        }
        return null;
    }


    // lặp từ phần tử cũ nhất tới mới nhất
    public void foreach(BiConsumer<K, V> consumer) {
        if (size == 0) return;

        IRingNode<K, V> cursor = tail;

        do {
            consumer.accept(cursor.getKey(), cursor.getValue());
            cursor = cursor.getNext();
        } while (cursor != null);
    }


    public boolean isFull() {
        return size == capacity;
    }


    public void clear() {
        if (size == 0) return;

        // clean chain
        IRingNode<K, V> cursor = tail;
        do {
            IRingNode<K, V> oldCursor = cursor;
            cursor = cursor.getNext();
            oldCursor.clear();
            itemPool.push(oldCursor);
        } while (cursor != null);

        // reset info
        head = null;
        tail = null;
        map.clear();
        size = 0;
    }

}
