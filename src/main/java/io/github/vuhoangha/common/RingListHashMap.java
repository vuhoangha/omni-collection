package io.github.vuhoangha.common;

import lombok.Getter;

import java.util.HashMap;
import java.util.function.BiConsumer;

/**
 * Quản lý một danh sách phần tử theo thứ tự chèn vào
 * Giới hạn số phần tử tối đa, khi đầy sẽ tự động xóa phần tử cũ nhất và chèn phần tử mới vào
 * Có thể lấy phần tử cũ nhất, mới nhất, tổng số phần tử, giá trị phần tử theo key với O(1)
 * Lấy phần tử trong khoảng với O(n)
 * Insert, delete ở đầu, cuối danh sách O(1)
 * Insert, delete ở giữa danh sách O(n)
 */
public class RingListHashMap<K, V> {

    private RingListHashMapItem<K, V> head;                                     // phần tử mới nhất chèn vào
    private RingListHashMapItem<K, V> tail;                                     // phần tử cũ nhất chèn vào
    private final HashMap<K, RingListHashMapItem<K, V>> map;                    // dùng để chứa Key và tham chiếu tới giá trị
    @Getter
    private int size;                                                           // tổng số phần tử hiện tại
    private final int capacity;                                                 // tổng số phần tử tối đa
    private final SynchronizeObjectPool<RingListHashMapItem<K, V>> itemPool;    // pool dự trữ các item trong danh sách, tránh việc khởi tạo


    public RingListHashMap(int capacity) {
        this.map = new HashMap<>();
        this.capacity = capacity;
        this.itemPool = new SynchronizeObjectPool<>(new RingListHashMapItem[capacity], RingListHashMapItem::new);
    }


    // thêm giá trị mới vào. Nếu đầy sẽ tự động xóa phần tử cũ nhất và thêm mới
    public boolean push(K key, V value) {
        if (size == capacity) return false;
        if (map.containsKey(key)) return false;

        // nếu đầy, xóa phần tử cũ nhất
        popIfFull();

        // lấy object để chứa phần tử mới từ pool và cập nhật giá trị
        RingListHashMapItem<K, V> newHead = itemPool.pop();
        newHead.key = key;
        newHead.value = value;

        // cập nhật head, tail
        if (size == 0) {
            head = newHead;
            tail = newHead;
        } else {
            head.next = newHead;
            newHead.prev = head;
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

        RingListHashMapItem<K, V> oldTail = tail;

        if (size == 1) {        // có 1 phần tử thì xóa đi thì head, tail bằng null
            head = null;
            tail = null;
        } else {                // update tail mới
            tail = tail.next;
            tail.prev = null;
        }

        // cập nhật số lượng phần tử
        size--;

        // xóa khỏi map
        map.remove(oldTail.key);

        // lấy giá trị của phần tử cũ nhất
        V value = oldTail.value;

        // reset phần tử cũ nhất và đẩy vào pool
        oldTail.clear();
        itemPool.push(oldTail);

        return value;
    }


    public boolean exist(K key) {
        return map.containsKey(key);
    }

    public RingListHashMapItem<K, V> get(K key) {
        return map.get(key);
    }

    public V getValue(K key) {
        return map.get(key).value;
    }

    public V getHeadValue() {
        return head == null ? null : head.value;
    }

    public V getTailValue() {
        return tail == null ? null : tail.value;
    }


    // di chuyển đến vị trí chính xác trong List nơi chứa key
    public RingListHashMapItem<K, V> moveTo(K key) {
        if (map.containsKey(key)) {
            return map.get(key);
        }
        return null;
    }


    // lặp từ phần tử cũ nhất tới mới nhất
    public void foreach(BiConsumer<K, V> consumer) {
        if (size == 0) return;

        RingListHashMapItem<K, V> cursor = tail;

        do {
            consumer.accept(cursor.key, cursor.value);
            cursor = cursor.next;
        } while (cursor != null);
    }


}
