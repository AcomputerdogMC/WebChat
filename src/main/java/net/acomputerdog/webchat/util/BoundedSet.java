package net.acomputerdog.webchat.util;

import java.util.Iterator;

/**
 * A circular linked list data structure.  When full, new items replace the oldest.
 */
public class BoundedSet<T> implements Iterable<T> {
    private final int maxSize;

    private int size = 0;
    //the "center" of the circular list
    //oldest.prev is youngest, oldest.next is second youngest
    private Entry oldest;

    public BoundedSet(int maxSize) {
        this.maxSize = maxSize;
    }

    public void add(T val) {
        if (size == 0) {
            size++;
            Entry entry = new Entry(val, null, null);
            entry.next = entry;
            entry.prev = entry;
            oldest = entry;
        } else if (size >= maxSize) {
            oldest.value = val;
            oldest = oldest.next;
        } else {
            size++;
            //insert before oldest
            Entry entry = new Entry(val, oldest, oldest.prev);
            oldest.prev.next = entry;
            oldest.prev = entry;
        }
    }

    public int getMaxSize() {
        return maxSize;
    }

    public int getSize() {
        return size;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            Entry start = oldest;
            Entry curr = start;

            @Override
            public boolean hasNext() {
                return curr != null;
            }

            @Override
            public T next() {
                if (curr == null) {
                    return null;
                }

                T val = curr.value;
                curr = curr.next;
                if (curr == oldest) {
                    curr = null;
                }
                return val;
            }
        };
    }

    private class Entry {
        T value;
        Entry next;
        Entry prev;

        public Entry(T value, Entry next, Entry prev) {
            this.value = value;
            this.next = next;
            this.prev = prev;
        }
    }
}
