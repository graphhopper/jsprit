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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Capacity with an arbitrary number of capacity-dimension.
 * <p>
 * <p>Note that this assumes the the values of each capacity dimension can be added up and subtracted
 *
 * @author schroeder
 */
public class Capacity {

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
        Capacity.Builder capacityBuilder = Capacity.Builder.newInstance();
        for(String i : getAllKeys(cap1, cap2)) {
            capacityBuilder.addDimension(i, cap1.get(i) + cap2.get(i));
        }
        return capacityBuilder.build();
    }

    /**
     * Subtracts cap2subtract from cap and returns the resulting Capacity.
     *
     * @param cap          capacity to be subtracted from
     * @param cap2subtract capacity to subtract
     * @return new capacity
     * @throws NullPointerException  if one of the args is null
     * @throws IllegalStateException if number of capacityDimensions of cap1 and cap2 are different (i.e. <code>cap1.getNuOfDimension() != cap2.getNuOfDimension()</code>).
     */
    public static Capacity subtract(Capacity cap, Capacity cap2subtract) {
        if (cap == null || cap2subtract == null) throw new NullPointerException("arguments must not be null");
        Capacity.Builder capacityBuilder = Capacity.Builder.newInstance();
        for(String i : getAllKeys(cap, cap2subtract)) {
            int dimValue = cap.get(i) - cap2subtract.get(i);
            capacityBuilder.addDimension(i, dimValue);
        }
        return capacityBuilder.build();
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
        Capacity.Builder capacityBuilder = Capacity.Builder.newInstance();
        for (String i : cap2invert.dimensions.keySet()) {
            int dimValue = cap2invert.get(i) * -1;
            capacityBuilder.addDimension(i, dimValue);
        }
        return capacityBuilder.build();
    }

    /**
     * Divides every dimension of numerator capacity by the corresponding dimension of denominator capacity,
     * , and averages each quotient.
     * <p>
     * <p>If both nominator.get(i) and denominator.get(i) equal to 0, dimension i is ignored.
     * <p>If both capacities are have only dimensions with dimensionVal=0, it returns 0.0
     *
     * @param numeratorCap   the numerator
     * @param denominatorCap the denominator
     * @return quotient
     * @throws IllegalStateException if numerator.get(i) != 0 and denominator.get(i) == 0
     */
    public static double divide(Capacity numeratorCap, Capacity denominatorCap) {
        int nuOfDimensions = 0;
        double sumQuotients = 0.0;

        for (String k : getAllKeys(numeratorCap, denominatorCap)) {
            Integer numerator = numeratorCap.get(k);
            Integer denominator = denominatorCap.get(k);

            if (numerator != 0 && denominator == 0)
                throw new IllegalArgumentException("numerator > 0 and denominator = 0. cannot divide by 0");

            if (numerator == 0 && denominator == 0)
                continue;

            nuOfDimensions++;
            sumQuotients += (double) numerator / (double) denominator;
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
        return new Capacity(capacity);
    }

    /**
     * Builder that builds Capacity
     *
     * @author schroeder
     */
    public static class Builder {

        /**
         * default is 1 dimension with size of zero
         */
        private Map<String, Integer> dimensions = new HashMap();

        /**
         * Returns a new instance of Capacity with one dimension and a value/size of 0
         *
         * @return this builder
         */
        public static Builder newInstance() {
            return new Builder();
        }

        Builder() {
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
        public Builder addDimension(String index, int dimValue) {
            dimensions.put(index, dimValue);
            return this;
        }

        public Builder addDimension(int index, int dimValue) {
            return addDimension(String.valueOf(index), dimValue);
        }


        /**
         * Builds an immutable Capacity and returns it.
         *
         * @return Capacity
         */
        public Capacity build() {
            return new Capacity(this);
        }


    }

    private Map<String, Integer> dimensions = new HashMap();

    /**
     * copy constructor
     *
     * @param capacity capacity to be copied
     */
    Capacity(Capacity capacity) {
        this.dimensions = new HashMap<>(capacity.dimensions);
    }

    Capacity(Builder builder) {
        dimensions = builder.dimensions;
    }

    /**
     * Returns the number of specified capacity dimensions.
     *
     * @return noDimensions
     */
    public int getNuOfDimensions() {
        //The previous array based implementation always had at least a size of 1
        //This is to keep it consistent with that implementation
        //But should probably be considered a bug and fixed
        if (dimensions.isEmpty()) return 1;

        return dimensions.size();
    }

    public Set<String> getDimensionNames() {
        return this.dimensions.keySet();
    }


    /**
     * Returns value of capacity-dimension with specified index.
     * <p>
     * <p>If capacity dimension does not exist, it returns 0 (rather than IndexOutOfBoundsException).
     *
     * @param index dimension index of the capacity value to be retrieved
     * @return the according dimension value
     */
    public int get(String index) {
        Integer v = dimensions.get(index);
        return v != null ? v.intValue() : 0;
    }

    public int get(int index) {
        return get(String.valueOf(index));
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
        for (String i : this.dimensions.keySet()) {
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
        for (String i : getAllKeys(this, toCompare)) {
            if (this.get(i) < toCompare.get(i)) return false;
        }
        return true;
    }

    @Override
    public String toString() {
        String string = "[noDimensions=" + getNuOfDimensions() + "]";
        for (String i : this.dimensions.keySet()) {
            string += "[[dimIndex=" + i + "][dimValue=" + dimensions.get(i) + "]]";
        }
        return string;
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
        Capacity.Builder toReturnBuilder = Capacity.Builder.newInstance();
        for (String i : getAllKeys(cap1, cap2)) {
            toReturnBuilder.addDimension(i, Math.max(cap1.get(i), cap2.get(i)));
        }
        return toReturnBuilder.build();
    }

    public static Capacity min(Capacity cap1, Capacity cap2) {
        if (cap1 == null || cap2 == null) throw new IllegalArgumentException("arg must not be null");
        Capacity.Builder toReturnBuilder = Capacity.Builder.newInstance();
        for (String i : getAllKeys(cap1, cap2)) {
            toReturnBuilder.addDimension(i, Math.min(cap1.get(i), cap2.get(i)));
        }
        return toReturnBuilder.build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Capacity capacity = (Capacity) o;

        return dimensions.equals(capacity.dimensions);
    }

    @Override
    public int hashCode() {
        return dimensions.hashCode();
    }

    private static Set<String> getAllKeys(Capacity capacity1, Capacity capacity2) {
        Set<String> allKeys = new HashSet();
        allKeys.addAll(capacity1.dimensions.keySet());
        allKeys.addAll(capacity2.dimensions.keySet());
        return allKeys;
    }
}
