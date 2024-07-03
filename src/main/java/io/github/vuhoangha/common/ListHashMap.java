package io.github.vuhoangha.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Hoạt động đơn luồng
 * Quản lý một danh sách phần tử theo mảng và map
 * Truy cập trực tiếp toàn bộ phần tử, theo key với độ phức tạp O(1)
 * Hạn chế sử dụng remove vì độ phức tạp O(n)
 */
public class ListHashMap<K, V> {

    private final List<V> array = new ArrayList<>();
    private final HashMap<K, V> map = new HashMap<>();


    // thêm 1 phần tử
    public void add(K key, V value) {
        if (map.containsKey(key)) return;
        map.put(key, value);
        array.add(value);
    }


    // xóa 1 phần tử
    public void remove(K key) {
        if (!map.containsKey(key)) return;

        map.remove(key);

        array.clear();
        map.forEach((k, v) -> array.add(v));
    }


    // lấy all (lưu ý là sẽ truy cập trực tiếp chứ ko clone rồi trả ra đâu)
    public List<V> getAll() {
        return array;
    }


    // lấy bằng key
    public V get(K key) {
        return map.get(key);
    }


    // thêm 1 phần tử nếu "key" chưa tồn tại
    public void computeIfAbsent(K key, V value) {
        map.computeIfAbsent(key, k -> {
            array.add(value);
            return value;
        });
    }


    // lấy 1 giá trị bởi key. Nếu key ko tồn tại thì thêm "value" vào key này và trả ra kết quả
    public V getIfAbsent(K key, V value) {
        if (!map.containsKey(key))
            add(key, value);
        return get(key);
    }

}
