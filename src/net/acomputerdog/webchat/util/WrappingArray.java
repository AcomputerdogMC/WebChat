package net.acomputerdog.webchat.util;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;

public class WrappingArray<T> implements Iterable<T> {
    private final Object[] array;

    private int length;
    private int nextIndex;

    public WrappingArray(int size) {
        this.length = size;
        this.nextIndex = 0;
        this.array = new Object[size];
    }

    public int getLength() {
        return length;
    }

    public T get(int index) {
        int idx = calcIndex(index);
        checkBounds(idx);
        return (T) array[idx];
    }

    public void add(T value) {
        int idx = nextIndex % length;
        array[idx] = value;
        nextIndex++;
    }

    public void set(int index, T value) {
        int idx = calcIndex(index);
        checkBounds(idx);
        array[idx] = value;
    }

    private int calcIndex(int index) {
        if (index < 0 || index >= length) {
            return -1; //invalid index
        }
        int newIndex = nextIndex + index;
        newIndex %= length;
        return newIndex;
    }

    private void checkBounds(int idx) {
        if (idx < 0) {
            throw new IllegalArgumentException("Array index must be at least 0!");
        }
        if (idx >= length) {
            throw new IllegalArgumentException("Array index must be less than array length!");
        }
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private int index = 0;

            @Override
            public boolean hasNext() {
                return index < length;
            }

            @Override
            public T next() {
                T value = get(index);
                index++;
                return value;
            }
        };
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        for (int index = 0; index < length; index++) {
            action.accept(get(index));
        }
    }

    @Override
    public Spliterator<T> spliterator() {
        throw new UnsupportedOperationException("Not implemented.");
    }
}
