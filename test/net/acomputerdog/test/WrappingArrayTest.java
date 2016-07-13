package net.acomputerdog.test;

import net.acomputerdog.webchat.util.WrappingArray;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class WrappingArrayTest {

    private WrappingArray<String> arr;

    @Before
    public void init() {
        arr = new WrappingArray<>(30);
    }

    @Test
    public void testSimple() {
        fill(30);
        for (int i = 0; i < 30; i++) {
            assertEquals(String.valueOf(i), arr.get(i));
        }
    }

    @Test
    public void testWrapOnce() {
        fill(45);
        assertEquals("15", arr.get(0));
        assertEquals("16", arr.get(1));
        assertEquals("29", arr.get(14));
        assertEquals("30", arr.get(15));
        assertEquals("31", arr.get(16));
        assertEquals("44", arr.get(29));
    }

    @Test
    public void testWrapMulti() {
        fill(75);
        assertEquals("45", arr.get(0));
        assertEquals("59", arr.get(14));
        assertEquals("74", arr.get(29));
    }

    private void fill(int num) {
        for (int count = 0; count < num; count++) {
            arr.add(String.valueOf(count));
        }
    }
}
