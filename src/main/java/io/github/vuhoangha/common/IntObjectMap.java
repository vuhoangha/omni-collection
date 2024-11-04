package io.github.vuhoangha.common;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * dùng cho các Map có key nhỏ, giới hạn 1 khoảng nhất định. Key chính là index trong 1 mảng
 */
public class IntObjectMap<T> {

    private final List<Integer> keys = new ArrayList<>();
    private final T[] values;

    public IntObjectMap(int size) {
        values = (T[]) new Object[size];
    }

    public void put(int key, T value) {
        if (keys.contains(key)) return;
        keys.add(key);
        values[key] = value;
    }

    public T get(int key) {
        return values[key];
    }

    public T getIfAbsentPut(int key, Supplier<T> supplier) {
        if (values[key] == null) {
            keys.add(key);
            values[key] = supplier.get();
        }
        return values[key];
    }

    public int size() {
        return keys.size();
    }

    public boolean containsKey(int key) {
        return values[key] != null;
    }

    public void forEachKeyValue(BiConsumer<Integer, T> consumer) {
        keys.forEach(key -> consumer.accept(key, values[key]));
    }

    public void forEachValue(Consumer<T> consumer) {
        keys.forEach(key -> consumer.accept(values[key]));
    }

    public void clear() {
        keys.forEach(key -> values[key] = null);
        keys.clear();
    }

}
