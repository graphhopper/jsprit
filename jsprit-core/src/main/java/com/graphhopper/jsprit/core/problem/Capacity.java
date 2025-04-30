/*
 * Licensed to GraphHopper GmbH under one or more contributor
 * license agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * GraphHopper GmbH licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.graphhopper.jsprit.core.problem;

import java.util.Arrays;

/**
 * Capacity with an arbitrary number of capacity-dimension.
 * <p>
 * <p>Note that this assumes the the values of each capacity dimension can be added up and subtracted
 *
 * @author schroeder
 */
public class Capacity {

    // Pre-allocate a common instance for performance
    private static final Capacity ZERO = new Capacity(new int[1]);

    /**
     * Adds up two capacities, i.e. sums up each and every capacity dimension, and returns the resulting Capacity.
     * <p>
     * <p>Note that this assumes that capacity dimension can be added up.
     *
     * @param cap1 capacity to be added up
     * @param cap2 capacity to be added up
     * @return new capacity
     * @throws NullPointerException if one of the args is null
     */
    public static Capacity addup(Capacity cap1, Capacity cap2) {
        if (cap1 == null || cap2 == null) throw new NullPointerException("arguments must not be null");

        // Special case handling for better performance
        if (cap1.isZero()) return copyOf(cap2);
        if (cap2.isZero()) return copyOf(cap1);

        int maxDimension = Math.max(cap1.getNuOfDimensions(), cap2.getNuOfDimensions());
        int[] newDimensions = new int[maxDimension];

        // Process all dimensions in one loop
        for (int i = 0; i < maxDimension; i++) {
            newDimensions[i] = cap1.get(i) + cap2.get(i);
        }

        return new Capacity(newDimensions);
    }

    /**
     * Subtracts cap2subtract from cap and returns the resulting Capacity.
     *
     * @param cap          capacity to be subtracted from
     * @param cap2subtract capacity to subtract
     * @return new capacity
     * @throws NullPointerException  if one of the args is null
     */
    public static Capacity subtract(Capacity cap, Capacity cap2subtract) {
        if (cap == null || cap2subtract == null) throw new NullPointerException("arguments must not be null");

        // Special case handling for better performance
        if (cap2subtract.isZero()) return copyOf(cap);

        int maxDimension = Math.max(cap.getNuOfDimensions(), cap2subtract.getNuOfDimensions());
        int[] newDimensions = new int[maxDimension];

        // Process all dimensions in one loop
        for (int i = 0; i < maxDimension; i++) {
            newDimensions[i] = cap.get(i) - cap2subtract.get(i);
        }

        return new Capacity(newDimensions);
    }

    /**
     * Returns the inverted capacity, i.e. it multiplies all capacity dimensions with -1.
     *
     * @param cap2invert capacity to be inverted
     * @return inverted capacity
     * @throws NullPointerException if one of the args is null
     */
    public static Capacity invert(Capacity cap2invert) {
        if (cap2invert == null) throw new NullPointerException("arguments must not be null");

        // Special case handling for better performance
        if (cap2invert.isZero()) return ZERO;

        int[] newDimensions = new int[cap2invert.getNuOfDimensions()];

        // Process all dimensions in one loop
        for (int i = 0; i < cap2invert.getNuOfDimensions(); i++) {
            newDimensions[i] = -cap2invert.get(i);
        }

        return new Capacity(newDimensions);
    }

    /**
     * Divides every dimension of numerator capacity by the corresponding dimension of denominator capacity,
     * , and averages each quotient.
     * <p>
     * <p>If both nominator.get(i) and denominator.get(i) equal to 0, dimension i is ignored.
     * <p>If both capacities are have only dimensions with dimensionVal=0, it returns 0.0
     *
     * @param numerator   the numerator
     * @param denominator the denominator
     * @return quotient
     * @throws IllegalStateException if numerator.get(i) != 0 and denominator.get(i) == 0
     */
    public static double divide(Capacity numerator, Capacity denominator) {
        int nuOfDimensions = 0;
        double sumQuotients = 0.0;
        int maxDim = Math.max(numerator.getNuOfDimensions(), denominator.getNuOfDimensions());

        for (int index = 0; index < maxDim; index++) {
            int num = numerator.get(index);
            int denom = denominator.get(index);

            if (num != 0 && denom == 0) {
                throw new IllegalArgumentException("numerator > 0 and denominator = 0. cannot divide by 0");
            } else if (num == 0 && denom == 0) {
                continue;
            } else {
                nuOfDimensions++;
                sumQuotients += (double) num / (double) denom;
            }
        }
        if (nuOfDimensions > 0) return sumQuotients / (double) nuOfDimensions;
        return 0.0;
    }

    /**
     * Makes a deep copy of Capacity.
     *
     * @param capacity capacity to be copied
     * @return copy
     */
    public static Capacity copyOf(Capacity capacity) {
        if (capacity == null) return null;
        if (capacity.isZero()) return ZERO;

        int[] newDimensions = new int[capacity.getNuOfDimensions()];
        System.arraycopy(capacity.dimensions, 0, newDimensions, 0, capacity.getNuOfDimensions());
        return new Capacity(newDimensions);
    }

    /**
     * Builder that builds Capacity
     *
     * @author schroeder
     */
    public static class Builder {
        private static final int DEFAULT_CAPACITY = 10;
        private int[] dimensions;
        private int maxIndex = -1;

        /**
         * Returns a new instance of Capacity with one dimension and a value/size of 0
         *
         * @return this builder
         */
        public static Builder newInstance() {
            return new Builder();
        }

        Builder() {
            dimensions = new int[DEFAULT_CAPACITY];
        }

        /**
         * Sets initial capacity for more efficient building when dimension count is known
         *
         * @param capacity the initial capacity
         * @return this builder
         */
        public Builder withCapacity(int capacity) {
            this.dimensions = new int[capacity];
            return this;
        }

        /**
         * add capacity dimension
         * <p>
         * <p>Note that it automatically resizes dimensions according to index, i.e. if index=7 there are 8 dimensions.
         * New dimensions then are initialized with 0
         *
         * @param index    dimensionIndex
         * @param dimValue dimensionValue
         * @return this builder
         */
        public Builder addDimension(int index, int dimValue) {
            ensureCapacity(index + 1);
            dimensions[index] = dimValue;
            if (index > maxIndex) {
                maxIndex = index;
            }
            return this;
        }

        /**
         * Ensures the dimensions array has sufficient capacity
         * Grows by factor 1.5x for better amortized performance
         */
        private void ensureCapacity(int requiredSize) {
            if (requiredSize > dimensions.length) {
                int newSize = Math.max(requiredSize, dimensions.length + (dimensions.length >> 1));
                int[] newDimensions = new int[newSize];
                System.arraycopy(dimensions, 0, newDimensions, 0, dimensions.length);
                dimensions = newDimensions;
            }
        }

        /**
         * Builds an immutable Capacity and returns it.
         *
         * @return Capacity
         */
        public Capacity build() {
            // Special case for empty or zero-only capacity
            boolean isZero = true;
            for (int i = 0; i <= maxIndex; i++) {
                if (dimensions[i] != 0) {
                    isZero = false;
                    break;
                }
            }

            if (isZero && maxIndex < 0) {
                return ZERO;
            }

            // Create right-sized array for the final capacity
            int[] rightSizedDimensions = new int[maxIndex + 1];
            System.arraycopy(dimensions, 0, rightSizedDimensions, 0, maxIndex + 1);
            return new Capacity(rightSizedDimensions);
        }
    }

    private final int[] dimensions;
    private final boolean isZero; // Cache for quick zero checks

    /**
     * Private constructor that takes ownership of the provided array
     */
    private Capacity(int[] dimensions) {
        this.dimensions = dimensions;

        // Precompute if this capacity is all zeros
        boolean allZeros = true;
        for (int dim : dimensions) {
            if (dim != 0) {
                allZeros = false;
                break;
            }
        }
        this.isZero = allZeros;
    }

    /**
     * Returns the number of specified capacity dimensions.
     *
     * @return noDimensions
     */
    public int getNuOfDimensions() {
        return dimensions.length;
    }

    /**
     * Returns value of capacity-dimension with specified index.
     * <p>
     * <p>If capacity dimension does not exist, it returns 0 (rather than IndexOutOfBoundsException).
     *
     * @param index dimension index of the capacity value to be retrieved
     * @return the according dimension value
     */
    public int get(int index) {
        if (index < dimensions.length) return dimensions[index];
        return 0;
    }

    /**
     * Returns true if this capacity is less or equal than the capacity toCompare, i.e. if none of the capacity dimensions > than the corresponding dimension in toCompare.
     *
     * @param toCompare the capacity to compare
     * @return true if this capacity is less or equal than toCompare
     * @throws NullPointerException if one of the args is null
     */
    public boolean isLessOrEqual(Capacity toCompare) {
        if (toCompare == null) throw new NullPointerException();

        // We can't use isZero as a fast path since dimensions can be negative
        int maxDim = Math.max(this.getNuOfDimensions(), toCompare.getNuOfDimensions());
        for (int i = 0; i < maxDim; i++) {
            if (this.get(i) > toCompare.get(i)) return false;
        }
        return true;
    }

    /**
     * Returns true if this capacity is greater or equal than the capacity toCompare
     *
     * @param toCompare the capacity to compare
     * @return true if this capacity is greater or equal than toCompare
     * @throws NullPointerException if one of the args is null
     */
    public boolean isGreaterOrEqual(Capacity toCompare) {
        if (toCompare == null) throw new NullPointerException();

        // We can't use isZero as a fast path since dimensions can be negative
        int maxDim = Math.max(this.getNuOfDimensions(), toCompare.getNuOfDimensions());
        for (int i = 0; i < maxDim; i++) {
            if (this.get(i) < toCompare.get(i)) return false;
        }
        return true;
    }

    /**
     * Check if this is a zero capacity (all dimensions are zero)
     *
     * @return true if all dimensions are zero
     */
    public boolean isZero() {
        return isZero;
    }

    @Override
    public String toString() {
        StringBuilder string = new StringBuilder("[noDimensions=" + getNuOfDimensions() + "]");
        for (int i = 0; i < getNuOfDimensions(); i++) {
            string.append("[[dimIndex=").append(i).append("][dimValue=").append(dimensions[i]).append("]]");
        }
        return string.toString();
    }

    /**
     * Return the maximum, i.e. the maximum of each capacity dimension.
     *
     * @param cap1 first capacity to compare
     * @param cap2 second capacity to compare
     * @return capacity maximum of each capacity dimension
     */
    public static Capacity max(Capacity cap1, Capacity cap2) {
        if (cap1 == null || cap2 == null) throw new IllegalArgumentException("arg must not be null");

        int maxDim = Math.max(cap1.getNuOfDimensions(), cap2.getNuOfDimensions());
        int[] newDimensions = new int[maxDim];

        for (int i = 0; i < maxDim; i++) {
            newDimensions[i] = Math.max(cap1.get(i), cap2.get(i));
        }

        return new Capacity(newDimensions);
    }

    /**
     * Return the minimum, i.e. the minimum of each capacity dimension.
     *
     * @param cap1 first capacity to compare
     * @param cap2 second capacity to compare
     * @return capacity minimum of each capacity dimension
     */
    public static Capacity min(Capacity cap1, Capacity cap2) {
        if (cap1 == null || cap2 == null) throw new IllegalArgumentException("arg must not be null");

        int maxDim = Math.max(cap1.getNuOfDimensions(), cap2.getNuOfDimensions());
        int[] newDimensions = new int[maxDim];

        for (int i = 0; i < maxDim; i++) {
            newDimensions[i] = Math.min(cap1.get(i), cap2.get(i));
        }

        return new Capacity(newDimensions);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Capacity)) return false;

        Capacity capacity = (Capacity) o;

        return Arrays.equals(dimensions, capacity.dimensions);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(dimensions);
    }
}
