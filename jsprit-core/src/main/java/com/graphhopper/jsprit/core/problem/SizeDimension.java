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
import java.util.EnumSet;

/**
 * SizeDimension with an arbitrary number of size dimension-dimension.
 * <p>
 * <p>
 * Note that this assumes the the values of each size dimension dimension can be
 * added up and subtracted
 *
 * @author schroeder
 * @author balage
 */
public class SizeDimension {

    public static final SizeDimension EMPTY = SizeDimension.Builder.newInstance().build();


    /**
     * Divides every dimension of numerator size dimension by the corresponding
     * dimension of denominator size dimension, , and averages each quotient.
     * <p>
     * <p>
     * If both nominator.get(i) and denominator.get(i) equal to 0, dimension i
     * is ignored.
     * <p>
     * If both sizeacities are have only dimensions with dimensionVal=0, it
     * returns 0.0
     *
     * @param numerator
     *            the numerator
     * @param denominator
     *            the denominator
     * @return quotient
     * @throws IllegalStateException
     *             if numerator.get(i) != 0 and denominator.get(i) == 0
     */
    public static double divide(SizeDimension numerator, SizeDimension denominator) {
        return numerator.divide(denominator);
    }

    /**
     * Makes a deep copy of SizeDimension.
     *
     * @param size dimension size dimension to be copied
     * @return copy
     */
    public static SizeDimension copyOf(SizeDimension sizeDimension) {
        if (sizeDimension == null) {
            return null;
        }
        return new SizeDimension(sizeDimension);
    }

    /**
     * Builder that builds SizeDimension
     *
     * @author schroeder
     * @author balage
     */
    public static class Builder {

        /**
         * default is 1 dimension with size of zero
         */
        private int[] dimensions = new int[1];

        /**
         * Returns a new instance of SizeDimension with one dimension and a value/size of 0
         *
         * @return this builder
         */
        public static Builder newInstance() {
            return new Builder();
        }

        Builder() {
        }

        /**
         * add size dimension dimension
         * <p>
         * <p>Note that it automatically resizes dimensions according to index, i.e. if index=7 there are 8 dimensions.
         * New dimensions then are initialized with 0
         *
         * @param index    dimensionIndex
         * @param dimValue dimensionValue
         * @return this builder
         */
        public Builder addDimension(int index, int dimValue) {
            if (index >= dimensions.length) {
                int requiredSize = index + 1;
                dimensions = Arrays.copyOf(dimensions, requiredSize);
            }
            dimensions[index] = dimValue;
            return this;
        }

        /**
         * Sets (overwrites) all dimensions from the parameter.
         *
         * <p>
         * The dimension size will be extended if the other has higher sizes.
         * </p>
         *
         * @param other
         *            The other size dimension object to copy the dimension
         *            values from
         * @return this builder
         */
        public Builder setDimensions(SizeDimension other) {
            if (other.getNuOfDimensions() >= dimensions.length) {
                dimensions = Arrays.copyOf(other.dimensions, other.dimensions.length);
            } else {
                for(int i = 0; i < other.getNuOfDimensions(); i++) {
                    dimensions[i] = other.dimensions[i];
                }
            }
            return this;
        }

        /**
         * Builds an immutable SizeDimension and returns it.
         *
         * @return SizeDimension
         */
        public SizeDimension build() {
            return new SizeDimension(this);
        }


    }

    private int[] dimensions;

    /**
     * copy constructor
     *
     * @param source size dimension to be copied
     */
    protected SizeDimension(SizeDimension source) {
        dimensions = Arrays.copyOf(source.dimensions, source.dimensions.length);
    }

    protected SizeDimension(Builder builder) {
        dimensions = builder.dimensions;
    }

    protected SizeDimension(int numberOfDimensions) {
        dimensions = new int[numberOfDimensions];
    }

    /**
     * Returns the number of specified size dimension dimensions.
     *
     * @return noDimensions
     */
    public int getNuOfDimensions() {
        return dimensions.length;
    }


    /**
     * Returns value of size dimension-dimension with specified index.
     * <p>
     * <p>
     * If size dimension dimension does not exist, it returns 0 (rather than
     * IndexOutOfBoundsException).
     *
     * @param index
     *            dimension index of the size dimension value to be retrieved
     * @return the according dimension value
     */
    public int get(int index) {
        if (index < dimensions.length) {
            return dimensions[index];
        }
        return 0;
    }

    /**
     * Returns true if this size dimension is less or equal than the size
     * dimension toCompare, i.e. if none of the size dimension dimensions > than
     * the corresponding dimension in toCompare.
     *
     * @param toCompare
     *            the size dimension to compare
     * @return true if this size dimension is less or equal than toCompare
     * @throws NullPointerException
     *             if one of the args is null
     */
    public boolean isLessOrEqual(SizeDimension toCompare) {
        if (toCompare == null) {
            throw new NullPointerException();
        }
        for (int i = 0; i < Math.max(getNuOfDimensions(), toCompare.getNuOfDimensions()); i++) {
            if (get(i) > toCompare.get(i)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns true if this size dimension is greater or equal than the size
     * dimension toCompare
     *
     * @param toCompare
     *            the size dimension to compare
     * @return true if this size dimension is greater or equal than toCompare
     * @throws NullPointerException
     *             if one of the args is null
     */
    public boolean isGreaterOrEqual(SizeDimension toCompare) {
        if (toCompare == null) {
            throw new NullPointerException();
        }
        for (int i = 0; i < Math.max(getNuOfDimensions(), toCompare.getNuOfDimensions()); i++) {
            if (get(i) < toCompare.get(i)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("SizeDimension[").append("[noDimensions=").append(getNuOfDimensions()).append(']');
        for (int i = 0; i < getNuOfDimensions(); i++) {
            sb.append('[').append(i).append('=').append(dimensions[i]).append(']');
        }
        sb.append(']');
        return sb.toString();
    }

    /**
     * Return the maximum, i.e. the maximum of each size dimension dimension.
     *
     * @param size1
     *            first size dimension to compare
     * @param size2
     *            second size dimension to compare
     * @return size dimension maximum of each size dimension dimension
     */
    public static SizeDimension max(SizeDimension size1, SizeDimension size2) {
        if (size1 == null || size2 == null) {
            throw new IllegalArgumentException("arg must not be null");
        }
        SizeDimension res = new SizeDimension(Math.max(size1.getNuOfDimensions(), size2.getNuOfDimensions()));
        for (int i = 0; i < Math.max(size1.getNuOfDimensions(), size2.getNuOfDimensions()); i++) {
            res.dimensions[i] = Math.max(size1.get(i), size2.get(i));
        }

        return res;
    }

    public static SizeDimension min(SizeDimension size1, SizeDimension size2) {
        if (size1 == null || size2 == null) {
            throw new IllegalArgumentException("arg must not be null");
        }
        SizeDimension res = new SizeDimension(Math.max(size1.getNuOfDimensions(), size2.getNuOfDimensions()));
        for (int i = 0; i < Math.max(size1.getNuOfDimensions(), size2.getNuOfDimensions()); i++) {
            res.dimensions[i] = Math.min(size1.get(i), size2.get(i));
        }
        return res;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SizeDimension)) {
            return false;
        }

        SizeDimension sizeDimension = (SizeDimension) o;

        if (!Arrays.equals(dimensions, sizeDimension.dimensions)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(dimensions);
    }


    public enum SizeDimensionSign {
        POSITIVE, ZERO, NEGATIVE, MIXED
    }

    /**
     * Returns the sign of the size dimension.
     * <p>
     * <ul>
     * <li>{@linkplain SizeDimensionSign#ZERO} when all dimension values are
     * zero.</li>
     * <li>{@linkplain SizeDimensionSign#POSITIVE} when all dimension values are
     * non-negative.</li>
     * <li>{@linkplain SizeDimensionSign#NEGATIVE} when all dimension values are
     * non-positive.</li>
     * <li>{@linkplain SizeDimensionSign#MIXED} when there are both negative or
     * positive dimensions.</li>
     * </ul>
     * </p>
     *
     * @return The sign of the size dimension.
     */
    public SizeDimensionSign sign() {
        EnumSet<SizeDimensionSign> possibleSigns = EnumSet.of(SizeDimensionSign.POSITIVE, SizeDimensionSign.NEGATIVE, SizeDimensionSign.ZERO);
        for (int i = 0; i < getNuOfDimensions(); i++) {
            if (get(i) < 0) {
                possibleSigns.remove(SizeDimensionSign.POSITIVE);
                possibleSigns.remove(SizeDimensionSign.ZERO);
            } else if (get(i) > 0) {
                possibleSigns.remove(SizeDimensionSign.NEGATIVE);
                possibleSigns.remove(SizeDimensionSign.ZERO);
            }
            if (possibleSigns.size() <= 1) {
                break;
            }
        }
        if (possibleSigns.isEmpty()) {
            return SizeDimensionSign.MIXED;
        }
        if (possibleSigns.contains(SizeDimensionSign.ZERO)) {
            return SizeDimensionSign.ZERO;
        } else {
            return possibleSigns.iterator().next();
        }
    }

    /**
     * Returns a new {@linkplain SizeDimension} object containing the sum of the
     * calling object and the <code>sizeToAdd</code>.
     * <p>
     * The dimension count of the result size is the max of the two operands.
     * </p>
     *
     * @param sizeToAdd
     *            size dimension to be added up
     * @return A sum of the two operands.
     * @throws NullPointerException
     *             if the <code>sizeToAdd</code> is null
     */
    public SizeDimension add(SizeDimension sizeToAdd) {
        if (sizeToAdd == null) {
            throw new NullPointerException("size dimension must not be null");
        }
        SizeDimension res = new SizeDimension(
                        Math.max(getNuOfDimensions(), sizeToAdd.getNuOfDimensions()));
        for (int i = 0; i < Math.max(getNuOfDimensions(),
                        sizeToAdd.getNuOfDimensions()); i++) {
            res.dimensions[i] = get(i) + sizeToAdd.get(i);
        }

        return res;
    }

    /**
     * Returns a new {@linkplain SizeDimension} object containing the difference
     * of the calling object and the <code>sizeToSubtract</code>.
     * <p>
     * The dimension count of the result size is the max of the two operands.
     * </p>
     *
     * @param sizeToSubtract
     *            size dimension to be subtracted
     * @return A difference of the two operands.
     * @throws NullPointerException
     *             if the <code>sizeToSubtract</code> is null
     */
    public SizeDimension subtract(SizeDimension sizeToSubstract) {
        if (sizeToSubstract == null) {
            throw new NullPointerException("size dimension must not be null");
        }
        SizeDimension res = new SizeDimension(Math.max(getNuOfDimensions(), sizeToSubstract.getNuOfDimensions()));
        for (int i = 0; i < Math.max(getNuOfDimensions(), sizeToSubstract.getNuOfDimensions()); i++) {
            res.dimensions[i] = get(i) - sizeToSubstract.get(i);
        }
        return res;
    }

    /**
     * Returns a new {@linkplain SizeDimension} object containing the inverted
     * value of the calling object. Each dimension is negated individually.
     *
     * @return The inverted value of calling object.
     */
    public SizeDimension invert() {
        SizeDimension res = new SizeDimension(getNuOfDimensions());
        for (int i = 0; i < getNuOfDimensions(); i++) {
            res.dimensions[i] = -get(i);
        }
        return res;
    }

    /**
     * Returns a new {@linkplain SizeDimension} object containing the absulute
     * value of the calling object. Each dimension is negated if it was negative
     * individually.
     *
     * @return The absolute value of calling object.
     */
    public SizeDimension abs() {
        SizeDimension res = new SizeDimension(getNuOfDimensions());
        for (int i = 0; i < getNuOfDimensions(); i++) {
            res.dimensions[i] = Math.abs(get(i));
        }
        return res;
    }

    /**
     * Divides every dimension of the calling size dimension by the
     * corresponding dimension of denominator size dimension, and averages each
     * quotient.
     * <p>
     * If both nominator.get(i) and denominator.get(i) equal to 0, dimension i
     * is ignored.
     * </p>
     * <p>
     * If both object are have only dimensions with value of 0, it returns 0.0
     * </p>
     *
     * @param denominator
     *            the denominator
     * @return averaged quotient value
     * @throws IllegalStateException
     *             if a size dimension is not 0, but the denominator has a 0
     *             value for the same dimension.
     */
    public double divide(SizeDimension denominator) {
        int nuOfDimensions = 0;
        double sumQuotients = 0.0;
        for (int index = 0; index < Math.max(getNuOfDimensions(),
                        denominator.getNuOfDimensions()); index++) {
            if (get(index) != 0 && denominator.get(index) == 0) {
                throw new IllegalArgumentException(
                                "numerator > 0 and denominator = 0. cannot divide by 0");
            } else if (get(index) == 0 && denominator.get(index) == 0) {
                continue;
            } else {
                nuOfDimensions++;
                sumQuotients += (double) get(index) / (double) denominator.get(index);
            }
        }
        if (nuOfDimensions > 0) {
            return sumQuotients / nuOfDimensions;
        }
        return 0.0;

    }

}
