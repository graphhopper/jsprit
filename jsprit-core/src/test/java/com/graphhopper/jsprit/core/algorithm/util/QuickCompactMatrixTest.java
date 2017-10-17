package com.graphhopper.jsprit.core.algorithm.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

import org.junit.Test;

import com.graphhopper.jsprit.core.problem.HasIndex;

public class QuickCompactMatrixTest {

    public static class Item implements HasIndex {
        private int index;

        public Item(int index) {
            this.index = index;
        }

        @Override
        public int getIndex() {
            return index;
        }
    }

    private static final BiFunction<Item, Item, Integer> INDEX_DIST = (i1, i2) -> Math
            .abs(i1.getIndex() - i2.getIndex());

    private List<Item> create(int... indexes) {
        List<Item> res = new ArrayList<>(indexes.length);
        for (int indexe : indexes) {
            res.add(new Item(indexe));
        }
        return res;
    }

    @Test
    public void whenConstructed_correctSizesCalculated() {
        List<Item> items = create(0, 1, 2, 3, 4);
        QuickCompactMatrix<Item> matrix = new QuickCompactMatrix<>(items, INDEX_DIST);
        assertEquals(0, matrix.getShift());
        assertEquals(5, matrix.getRedirectionSize());
        assertEquals(5, matrix.getMatrixSize());
    }

    @Test
    public void whenConstructed_correctShiftIsCorrect() {
        List<Item> items = create(1, 3, 5, 7, 9);
        QuickCompactMatrix<Item> matrix = new QuickCompactMatrix<>(items, INDEX_DIST);
        assertEquals(1, matrix.getShift());
        assertEquals(9, matrix.getRedirectionSize());
        assertEquals(5, matrix.getMatrixSize());
    }

    @Test
    public void whenReadingMatrixIndex_correctValueIsReturned() {
        List<Item> items = create(1, 3, 5, 7, 9);
        QuickCompactMatrix<Item> matrix = new QuickCompactMatrix<>(items, INDEX_DIST);
        assertEquals(0, matrix.getMatrixIndex(items.get(0)));
        assertEquals(4, matrix.getMatrixIndex(items.get(4)));
        assertEquals(-1, matrix.getMatrixIndex(new Item(0)));
        assertEquals(-1, matrix.getMatrixIndex(new Item(10)));
        assertEquals(-1, matrix.getMatrixIndex(new Item(2)));
    }

    @Test
    public void whenCallingContains_correctAnswerIsReturned() {
        List<Item> items = create(1, 3, 5, 7, 9);
        QuickCompactMatrix<Item> matrix = new QuickCompactMatrix<>(items, INDEX_DIST);
        assertTrue(matrix.contains(items.get(0)));
        assertTrue(matrix.contains(items.get(4)));
        assertFalse(matrix.contains(new Item(0)));
        assertFalse(matrix.contains(new Item(10)));
        assertFalse(matrix.contains(new Item(2)));
    }

    @Test
    public void whenReadingValue_correctValueIsReturned() {
        List<Item> items = create(1, 3, 5, 7, 9);
        QuickCompactMatrix<Item> matrix = new QuickCompactMatrix<>(items, INDEX_DIST);
        assertEquals(2, matrix.getValue(items.get(0), items.get(1)));
        assertEquals(6, matrix.getValue(items.get(0), items.get(3)));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void whenReadingValueOfWrongItem_exceptionIsThrown() {
        List<Item> items = create(1, 3, 5, 7, 9);
        QuickCompactMatrix<Item> matrix = new QuickCompactMatrix<>(items, INDEX_DIST);
        assertEquals(2, matrix.getValue(items.get(0), new Item(2)));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void whenReadingValueOfWrongItem_exceptionIsThrown_underflow() {
        List<Item> items = create(1, 3, 5, 7, 9);
        QuickCompactMatrix<Item> matrix = new QuickCompactMatrix<>(items, INDEX_DIST);
        assertEquals(2, matrix.getValue(items.get(0), new Item(0)));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void whenReadingValueOfWrongItem_exceptionIsThrown_overflow() {
        List<Item> items = create(1, 3, 5, 7, 9);
        QuickCompactMatrix<Item> matrix = new QuickCompactMatrix<>(items, INDEX_DIST);
        assertEquals(2, matrix.getValue(items.get(0), new Item(10)));
    }

    @Test
    public void whenConstructedWithCopy_theValuesAreCorrect() {
        List<Item> sourceItems = create(0, 1, 2, 3, 4);
        QuickCompactMatrix<Item> sourceMatrix = new QuickCompactMatrix<>(sourceItems, INDEX_DIST);
        List<Item> items = create(1, 2, 5);
        QuickCompactMatrix<Item> matrix = new QuickCompactMatrix<>(items,
                QuickCompactMatrix.getCopyWrapper(sourceMatrix, INDEX_DIST));

        assertEquals(1, matrix.getValue(items.get(0), items.get(1)));
        assertEquals(4, matrix.getValue(items.get(0), items.get(2)));
    }

}
