package io.github.vuhoangha.common;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * dùng cho các Map có key nhỏ, giới hạn 1 khoảng nhất định
 */
public class ByteObjectMap<T> {

    private final List<Byte> keys = new ArrayList<>();
    private final T[] values;

    public ByteObjectMap(int size) {
        values = (T[]) new Object[size];
    }

    public void put(byte key, T value) {
        if (keys.contains(key)) return;
        keys.add(key);
        values[key] = value;
    }

    public T get(byte key) {
        return values[key];
    }

    public T getIfAbsentPut(byte key, Supplier<T> supplier) {
        if (values[key] == null) put(key, supplier.get());
        return get(key);
    }

    public int size() {
        return keys.size();
    }

    public boolean containsKey(byte key) {
        return values[key] != null;
    }

    public void forEachKeyValue(BiConsumer<Byte, T> consumer) {
        keys.forEach(key -> consumer.accept(key, values[key]));
    }

    public void forEachValue(Consumer<T> consumer) {
        keys.forEach(key -> consumer.accept(values[key]));
    }

}
