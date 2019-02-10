package net.acomputerdog.mc.webchat.test;

import net.acomputerdog.webchat.util.BoundedSet;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BoundedSetTest {

    private BoundedSet<String> set;

    private void fill(int size) {
        for (int i = 0; i < size; i++) {
            set.add(String.valueOf(i));
        }
    }

    @Before
    public void init() {
        set = new BoundedSet<>(50);
    }

    @Test
    public void basicTest() {
        fill(15);

        int idx = 0;
        for (String str : set) {
            assertEquals(String.valueOf(idx), str);
            idx++;
        }
    }

    @Test
    public void fullTest() {
        fill(set.getMaxSize());

        int idx = 0;
        for (String str : set) {
            assertEquals(String.valueOf(idx), str);
            idx++;
        }
    }

    @Test
    public void fullWwrapTest() {
        fill(set.getMaxSize() * 2);

        int idx = 50;
        for (String str : set) {
            assertEquals(String.valueOf(idx), str);
            idx++;
        }
    }
}
