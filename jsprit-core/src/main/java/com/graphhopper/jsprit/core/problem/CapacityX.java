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
 * SizeDimensionX with an arbitrary number of SizeDimension-dimension.
 * <p>
 * <p>
 * Note that this assumes the the values of each SizeDimension dimension can be
 * added up and subtracted
 *
 * @author schroeder
 */
public class CapacityX {


    @Deprecated
    public static CapacityX addup(CapacityX cap1, CapacityX cap2) {
        return cap1.add(cap2);
    }

    /**
     * Subtracts cap2subtract from cap and returns the resulting SizeDimensionX.
     *
     * @param cap
     *            SizeDimension to be subtracted from
     * @param cap2subtract
     *            SizeDimension to subtract
     * @return new SizeDimension
     * @throws NullPointerException
     *             if one of the args is null
     * @throws IllegalStateException
     *             if number of SizeDimensionDimensions of cap1 and cap2 are
     *             different (i.e.
     *             <code>cap1.getNuOfDimension() != cap2.getNuOfDimension()</code>
     *             ).
     * @deprecated Use <code>cap1.subtract(cap2)</code> instead.
     */
    @Deprecated
    public static CapacityX subtract(CapacityX cap, CapacityX cap2subtract) {
        return cap.subtract(cap2subtract);
    }

    /**
     * Returns the inverted SizeDimension, i.e. it multiplies all SizeDimension
     * dimensions with -1.
     *
     * @param cap2invert
     *            SizeDimension to be inverted
     * @return inverted SizeDimension
     * @throws NullPointerException
     *             if one of the args is null
     * @deprecated Use <code>cap2invert.invert()</code> instead.
     */
    @Deprecated
    public static CapacityX invert(CapacityX cap2invert) {
        return cap2invert.invert();
    }

    /**
     * Divides every dimension of numerator SizeDimension by the corresponding
     * dimension of denominator SizeDimension, , and averages each quotient.
     * <p>
     * <p>
     * If both nominator.get(i) and denominator.get(i) equal to 0, dimension i
     * is ignored.
     * <p>
     * If both capacities are have only dimensions with dimensionVal=0, it
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
    public static double divide(CapacityX numerator, CapacityX denominator) {
        return numerator.divide(denominator);
    }

    /**
     * Makes a deep copy of SizeDimensionX.
     *
     * @param SizeDimension
     *            SizeDimension to be copied
     * @return copy
     */
    public static CapacityX copyOf(CapacityX SizeDimension) {
        if (SizeDimension == null) {
            return null;
        }
        return new CapacityX(SizeDimension);
    }

    /**
     * Builder that builds SizeDimensionX
     *
     * @author schroeder
     */
    public static class Builder {

        /**
         * default is 1 dimension with size of zero
         */
        private int[] dimensions = new int[1];

        /**
         * Returns a new instance of SizeDimensionX with one dimension and a
         * value/size of 0
         *
         * @return this builder
         */
        public static Builder newInstance() {
            return new Builder();
        }

        Builder() {
        }

        /**
         * add SizeDimension dimension
         * <p>
         * <p>
         * Note that it automatically resizes dimensions according to index,
         * i.e. if index=7 there are 8 dimensions. New dimensions then are
         * initialized with 0
         *
         * @param index
         *            dimensionIndex
         * @param dimValue
         *            dimensionValue
         * @return this builder
         */
        public Builder addDimension(int index, int dimValue) {
            if (index < dimensions.length) {
                dimensions[index] = dimValue;
            } else {
                int requiredSize = index + 1;
                int[] newDimensions = new int[requiredSize];
                copy(dimensions, newDimensions);
                newDimensions[index] = dimValue;
                dimensions = newDimensions;
            }
            return this;
        }

        private void copy(int[] from, int[] to) {
            for (int i = 0; i < dimensions.length; i++) {
                to[i] = from[i];
            }
        }

        /**
         * Builds an immutable SizeDimensionX and returns it.
         *
         * @return SizeDimensionX
         */
        public CapacityX build() {
            return new CapacityX(this);
        }


    }

    private int[] dimensions;

    /**
     * copy constructor
     *
     * @param SizeDimension
     *            SizeDimension to be copied
     */
    CapacityX(CapacityX SizeDimension) {
        dimensions = new int[SizeDimension.getNuOfDimensions()];
        for (int i = 0; i < SizeDimension.getNuOfDimensions(); i++) {
            dimensions[i] = SizeDimension.get(i);
        }
    }

    CapacityX(Builder builder) {
        dimensions = builder.dimensions;
    }

    private CapacityX(int numberOfDimensions) {
        dimensions = new int[numberOfDimensions];
        // Arrays.fill(dimensions, 0); // Just to be safe, not needed
    }

    /**
     * Returns the number of specified SizeDimension dimensions.
     *
     * @return noDimensions
     */
    public int getNuOfDimensions() {
        return dimensions.length;
    }


    /**
     * Returns value of SizeDimension-dimension with specified index.
     * <p>
     * <p>
     * If SizeDimension dimension does not exist, it returns 0 (rather than
     * IndexOutOfBoundsException).
     *
     * @param index
     *            dimension index of the SizeDimension value to be retrieved
     * @return the according dimension value
     */
    public int get(int index) {
        if (index < dimensions.length) {
            return dimensions[index];
        }
        return 0;
    }

    /**
     * Returns true if this SizeDimension is less or equal than the
     * SizeDimension toCompare, i.e. if none of the SizeDimension dimensions >
     * than the corresponding dimension in toCompare.
     *
     * @param toCompare
     *            the SizeDimension to compare
     * @return true if this SizeDimension is less or equal than toCompare
     * @throws NullPointerException
     *             if one of the args is null
     */
    public boolean isLessOrEqual(CapacityX toCompare) {
        if (toCompare == null) {
            throw new NullPointerException();
        }
        for (int i = 0; i < getNuOfDimensions(); i++) {
            if (get(i) > toCompare.get(i)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns true if this SizeDimension is greater or equal than the
     * SizeDimension toCompare
     *
     * @param toCompare
     *            the SizeDimension to compare
     * @return true if this SizeDimension is greater or equal than toCompare
     * @throws NullPointerException
     *             if one of the args is null
     */
    public boolean isGreaterOrEqual(CapacityX toCompare) {
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
        String string = "[noDimensions=" + getNuOfDimensions() + "]";
        for (int i = 0; i < getNuOfDimensions(); i++) {
            string += "[[dimIndex=" + i + "][dimValue=" + dimensions[i] + "]]";
        }
        return string;
    }

    /**
     * Return the maximum, i.e. the maximum of each SizeDimension dimension.
     *
     * @param cap1
     *            first SizeDimension to compare
     * @param cap2
     *            second SizeDimension to compare
     * @return SizeDimension maximum of each SizeDimension dimension
     */
    public static CapacityX max(CapacityX cap1, CapacityX cap2) {
        if (cap1 == null || cap2 == null) {
            throw new IllegalArgumentException("arg must not be null");
        }
        CapacityX.Builder toReturnBuilder = CapacityX.Builder.newInstance();
        for (int i = 0; i < Math.max(cap1.getNuOfDimensions(), cap2.getNuOfDimensions()); i++) {
            toReturnBuilder.addDimension(i, Math.max(cap1.get(i), cap2.get(i)));
        }
        return toReturnBuilder.build();
    }

    public static CapacityX min(CapacityX cap1, CapacityX cap2) {
        if (cap1 == null || cap2 == null) {
            throw new IllegalArgumentException("arg must not be null");
        }
        CapacityX.Builder toReturnBuilder = CapacityX.Builder.newInstance();
        for (int i = 0; i < Math.max(cap1.getNuOfDimensions(), cap2.getNuOfDimensions()); i++) {
            toReturnBuilder.addDimension(i, Math.min(cap1.get(i), cap2.get(i)));
        }
        return toReturnBuilder.build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CapacityX)) {
            return false;
        }

        CapacityX SizeDimension = (CapacityX) o;

        if (!Arrays.equals(dimensions, SizeDimension.dimensions)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(dimensions);
    }

    public boolean isPositive() {
        for (int i = 0; i < getNuOfDimensions(); i++) {
            if (get(i) < 0) {
                return false;
            }
        }
        return true;
    }

    public CapacityX add(CapacityX capToAdd) {
        if (capToAdd == null) {
            throw new NullPointerException("SizeDimension must not be null");
        }
        CapacityX res = new CapacityX(
                        Math.max(getNuOfDimensions(), capToAdd.getNuOfDimensions()));
        for (int i = 0; i < Math.max(getNuOfDimensions(),
                        capToAdd.getNuOfDimensions()); i++) {
            res.dimensions[i] = get(i) + capToAdd.get(i);
        }

        return res;
    }

    public CapacityX subtract(CapacityX capToSubstract) {
        if (capToSubstract == null) {
            throw new NullPointerException("SizeDimension must not be null");
        }
        CapacityX res = new CapacityX(
                        Math.max(getNuOfDimensions(), capToSubstract.getNuOfDimensions()));
        for (int i = 0; i < Math.max(getNuOfDimensions(),
                        capToSubstract.getNuOfDimensions()); i++) {
            res.dimensions[i] = get(i) - capToSubstract.get(i);
        }
        return res;
    }

    public CapacityX invert() {
        CapacityX res = new CapacityX(getNuOfDimensions());
        for (int i = 0; i < getNuOfDimensions(); i++) {
            res.dimensions[i] = -get(i);
        }
        return res;
    }

    public CapacityX abs() {
        CapacityX res = new CapacityX(getNuOfDimensions());
        for (int i = 0; i < getNuOfDimensions(); i++) {
            res.dimensions[i] = Math.abs(get(i));
        }
        return res;
    }

    public double divide(CapacityX denominator) {
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
                sumQuotients += get(index) / (double) denominator.get(index);
            }
        }
        if (nuOfDimensions > 0) {
            return sumQuotients / nuOfDimensions;
        }
        return 0.0;

    }

}
