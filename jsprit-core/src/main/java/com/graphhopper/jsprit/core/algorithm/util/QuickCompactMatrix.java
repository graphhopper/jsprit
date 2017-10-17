package com.graphhopper.jsprit.core.algorithm.util;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;

import com.graphhopper.jsprit.core.problem.HasIndex;

/**
 * A quick, compact, but flexible int array based matrix.
 *
 * @author Balage
 *
 * @param <T>
 *            The type of the items.
 */
public class QuickCompactMatrix<T extends HasIndex> {

    // The shift in redirection array indexing: the minimum index of the items
    private int shift;
    // The size of the redirection array: the difference of the minimum and
    // maximum index of the items
    private int redirectionSize;
    // The matrix size: the number of items
    private int matrixSize;
    // The redirection array: maps the item index to matrix index
    private int[] redirection;
    // The matrix
    private int[][] matrix;

    /**
     * Wrapper class to avoid repeated calculation.
     *
     * @author Balage
     *
     * @param <S>
     *            The item type.
     */
    private static class CopyWrapper<S extends HasIndex> implements BiFunction<S, S, Integer> {

        private QuickCompactMatrix<S> source;
        private BiFunction<S, S, Integer> valueCalculator;

        public CopyWrapper(QuickCompactMatrix<S> source, BiFunction<S, S, Integer> valueCalculator) {
            this.source = source;
            this.valueCalculator = valueCalculator;
        }

        @Override
        public Integer apply(S item1, S item2) {
            if (source.contains(item1) && source.contains(item2))
                return source.getValue(item1, item2);
            else
                return valueCalculator.apply(item1, item2);
        }
    }

    /**
     * Wraps the calculator to use an other instance of the matrix for
     * calculating values whenever it is possible.
     * <p>
     * The wrapper is helpful, when creating an extended matrix from an old one
     * and the cost of calculating the value is high.
     * </p>
     * <p>
     * This class wraps the original value calculator. When it is called to
     * calculate a value, first checks if it is available in the source matrix
     * and returns the value from the source. Otherwise it delegates the
     * calculation to the wrapped calculator.
     * </p>
     *
     * @param source
     *            The source matrix.
     * @param valueCalculator
     *            The calculator.
     * @return A wrapped calculator.
     */
    public static <T extends HasIndex> BiFunction<T, T, Integer> getCopyWrapper(QuickCompactMatrix<T> source,
            BiFunction<T, T, Integer> valueCalculator) {
        return new CopyWrapper<>(source, valueCalculator);
    }

    /**
     * Plain constructor.
     * <p>
     * It gets no other matrix to extend.
     * </p>
     *
     * @param items
     *            The list of items.
     * @param valueCalculator
     *            The function to calculate matrix cell values.
     */
    public QuickCompactMatrix(List<T> items, BiFunction<T, T, Integer> valueCalculator) {
        calculateDimensions(items);
        initializeRedirectionArray(items);
        initializeMatrix();
        calculateValues(items, valueCalculator);
    }

    /**
     * Calculates the shift value and redirection array and the matrix size.
     *
     * @param items
     *            The list of items.
     */
    private void calculateDimensions(List<T> items) {
        shift = 0;
        int min = Integer.MAX_VALUE;
        int max = 0;
        for (T i : items) {
            int idx = i.getIndex();
            if (idx < min) {
                min = idx;
            }
            if (idx > max) {
                max = idx;
            }
        }
        shift = min;
        redirectionSize = max - shift + 1;
        matrixSize = items.size();
    }

    /**
     * Initialize the redirection matrix.
     *
     * @param items
     *            The list of items.
     */
    private void initializeRedirectionArray(List<T> items) {
        // Initialize the array and fill up with -1. (Filling the values with -1
        // ensures that IndexOutOfBounds is thrown when calling getValue with
        // items not in this original list.)
        redirection = new int[redirectionSize];
        Arrays.fill(redirection, -1);

        // Determine the matrix indexing for each item index
        int counter = 0;
        for (T i : items) {
            redirection[i.getIndex() - shift] = counter++;
        }
    }

    /**
     * Initialize the matrix.
     */
    private void initializeMatrix() {
        matrix = new int[matrixSize][matrixSize];
    }

    /**
     * Calculates the matrix values.
     * <p>
     * It only calculates the values above the <code>from</code> index.
     * </p>
     *
     * @param items
     *            The list of items.
     * @param valueCalculator
     *            The value calculator to use.
     */
    private void calculateValues(List<T> items, BiFunction<T, T, Integer> valueCalculator) {
        for (int row = 0; row < matrixSize; row++) {
            T rowItem = items.get(row);
            int rowIndex = redirection[rowItem.getIndex() - shift];
            for (int col = 0; col < matrixSize; col++) {
                T colItem = items.get(col);
                int colIndex = redirection[colItem.getIndex() - shift];
                matrix[rowIndex][colIndex] = valueCalculator.apply(rowItem, colItem);
            }
        }
    }

    /**
     * Returns the value from the matrix.
     *
     * @param rowItem
     *            The row item.
     * @param colItem
     *            The column item.
     * @return The value from the matrix.
     * @throws IndexOutOfBoundsException
     *             When any of the items passed are not mapped.
     */
    public int getValue(T rowItem, T colItem) {
        int rowIndex = redirection[rowItem.getIndex() - shift];
        int colIndex = redirection[colItem.getIndex() - shift];
        return matrix[rowIndex][colIndex];
    }

    /**
     * Returns the internal (matrix) index of the item.
     *
     * @param item
     *            The item to get the index of.
     * @return The matrix index or -1 if the item is not mapped.
     */
    public int getMatrixIndex(T item) {
        int rowItemIndex = item.getIndex() - shift;
        if (rowItemIndex < 0 || rowItemIndex >= redirectionSize)
            return -1;
        else
            return redirection[rowItemIndex];
    }

    /**
     * Checks if the item is part of the matrix.
     *
     * @param item
     *            The item to check.
     * @return True if the item is mapped in the matrix.
     */
    public boolean contains(T item) {
        return getMatrixIndex(item) != -1;
    }

    /**
     * @return The shift in index used when addressing the redirection array by
     *         item index.
     */
    public int getShift() {
        return shift;
    }

    /**
     * @return The size of the redirection array.
     */
    public int getRedirectionSize() {
        return redirectionSize;
    }

    /**
     * @return The matrix size.
     */
    public int getMatrixSize() {
        return matrixSize;
    }

}
