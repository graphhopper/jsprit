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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

public class SizeDimensionTest {

    @Test
    public void signShouldBePositive() {
        SizeDimension sd = SizeDimension.Builder.newInstance().addDimension(0, 1).addDimension(1, 2).build();
        assertEquals(SizeDimension.SizeDimensionSign.POSITIVE, sd.sign());
    }

    @Test
    public void signShouldBePositive2() {
        SizeDimension sd = SizeDimension.Builder.newInstance().addDimension(0, 1).addDimension(1, 0).build();
        assertEquals(SizeDimension.SizeDimensionSign.POSITIVE, sd.sign());
    }

    @Test
    public void signShouldBeNegative() {
        SizeDimension sd = SizeDimension.Builder.newInstance().addDimension(0, -1).addDimension(1, -2).build();
        assertEquals(SizeDimension.SizeDimensionSign.NEGATIVE, sd.sign());
    }

    @Test
    public void signShouldBeNegative2() {
        SizeDimension sd = SizeDimension.Builder.newInstance().addDimension(0, 0).addDimension(1, -2).build();
        assertEquals(SizeDimension.SizeDimensionSign.NEGATIVE, sd.sign());
    }

    @Test
    public void signShouldBeMixed() {
        SizeDimension sd = SizeDimension.Builder.newInstance().addDimension(0, -1).addDimension(1, 2).build();
        assertEquals(SizeDimension.SizeDimensionSign.MIXED, sd.sign());
    }

    @Test
    public void signShouldBeZero() {
        SizeDimension sd = SizeDimension.Builder.newInstance().addDimension(0, 0).addDimension(1, 0).build();
        assertEquals(SizeDimension.SizeDimensionSign.ZERO, sd.sign());
    }

    @Test
    public void getNegativeShouldReturnCorrectCapacity() {
        SizeDimension cap = SizeDimension.Builder.newInstance().addDimension(0, 2).addDimension(1, -3).build();
        SizeDimension neg = cap.getNegativeDimensions();
        assertEquals(2, neg.getNuOfDimensions());
        assertEquals(0, neg.get(0));
        assertEquals(-3, neg.get(1));
    }

    @Test
    public void getPositiveShouldReturnCorrectCapacity() {
        SizeDimension cap = SizeDimension.Builder.newInstance().addDimension(0, 2).addDimension(1, -3).build();
        SizeDimension pos = cap.getPositiveDimensions();
        assertEquals(2, pos.getNuOfDimensions());
        assertEquals(2, pos.get(0));
        assertEquals(0, pos.get(1));
    }

    @Test
    public void whenSettingSimplyOneCapDimension_nuOfDimensionMustBeCorrect() {
        SizeDimension.Builder capBuilder = SizeDimension.Builder.newInstance();
        capBuilder.addDimension(0, 4);
        SizeDimension cap = capBuilder.build();
        assertEquals(1, cap.getNuOfDimensions());
    }

    @Test
    public void whenSettingTwoCapDimension_nuOfDimensionMustBeCorrect() {
        SizeDimension.Builder capBuilder = SizeDimension.Builder.newInstance();
        capBuilder.addDimension(0, 4);
        capBuilder.addDimension(1, 10);
        SizeDimension cap = capBuilder.build();
        assertEquals(2, cap.getNuOfDimensions());
    }

    @Test
    public void whenSettingRandomNuOfCapDimension_nuOfDimensionMustBeCorrect() {
        Random rand = new Random();
        int nuOfCapDimensions = 1 + rand.nextInt(100);
        SizeDimension.Builder capBuilder = SizeDimension.Builder.newInstance();
        capBuilder.addDimension(nuOfCapDimensions - 1, 4);
        SizeDimension cap = capBuilder.build();
        assertEquals(nuOfCapDimensions, cap.getNuOfDimensions());
    }

    @Test
    public void whenSettingOneDimValue_valueMustBeCorrect() {
        SizeDimension.Builder capBuilder = SizeDimension.Builder.newInstance();
        capBuilder.addDimension(0, 4);
        SizeDimension cap = capBuilder.build();
        assertEquals(4, cap.get(0));
    }

    @Test
    public void whenGettingIndexWhichIsHigherThanNuOfCapDimensions_itShouldReturn0() {
        SizeDimension.Builder capBuilder = SizeDimension.Builder.newInstance();
        capBuilder.addDimension(0, 4);
        SizeDimension cap = capBuilder.build();
        assertEquals(0, cap.get(2));
    }

    @Test
    public void whenSettingNoDim_DefaultIsOneDimWithDimValueOfZero() {
        SizeDimension.Builder capBuilder = SizeDimension.Builder.newInstance();
        SizeDimension cap = capBuilder.build();
        assertEquals(1, cap.getNuOfDimensions());
        assertEquals(0, cap.get(0));
    }

    @Test
    public void whenCopyingCapacityWithTwoCapDim_copiedObjShouldHvSameNuOfDims() {
        SizeDimension.Builder capBuilder = SizeDimension.Builder.newInstance();
        capBuilder.addDimension(0, 4);
        capBuilder.addDimension(1, 10);
        SizeDimension cap = capBuilder.build();

        SizeDimension copiedCapacity = SizeDimension.copyOf(cap);
        assertEquals(2, copiedCapacity.getNuOfDimensions());
    }

    @Test
    public void whenCopyingCapacityWithTwoCapDim_copiedObjShouldHvSameValues() {
        SizeDimension.Builder capBuilder = SizeDimension.Builder.newInstance();
        capBuilder.addDimension(0, 4);
        capBuilder.addDimension(1, 10);
        SizeDimension cap = capBuilder.build();

        SizeDimension copiedCapacity = SizeDimension.copyOf(cap);
        assertEquals(4, copiedCapacity.get(0));
        assertEquals(10, copiedCapacity.get(1));
    }

    @Test
    public void whenCopyingNull_itShouldReturnNull() {
        SizeDimension nullCap = SizeDimension.copyOf(null);
        assertTrue(nullCap == null);
    }

    @Test
    public void whenAddingUpTwoOneDimensionalCapacities_itShouldReturnCorrectCapacityValues() {
        SizeDimension cap1 = SizeDimension.Builder.newInstance().addDimension(0, 1).build();
        SizeDimension cap2 = SizeDimension.Builder.newInstance().addDimension(0, 2).build();
        SizeDimension result = cap1.add(cap2);
        assertEquals(3, result.get(0));
    }

    @Test
    public void whenAddingUpTwoOneDimensionalCapacities_itShouldReturnCorrectNuOfDimensions() {
        SizeDimension cap1 = SizeDimension.Builder.newInstance().addDimension(0, 1).build();
        SizeDimension cap2 = SizeDimension.Builder.newInstance().addDimension(0, 2).build();
        SizeDimension result = cap1.add(cap2);
        assertEquals(1, result.getNuOfDimensions());
    }

    @Test
    public void whenAddingUpTwoThreeDimensionalCapacities_itShouldReturnCorrectNuOfDimensions() {
        SizeDimension cap1 = SizeDimension.Builder.newInstance().addDimension(0, 1).addDimension(1, 2).addDimension(2, 3).build();
        SizeDimension cap2 = SizeDimension.Builder.newInstance().addDimension(0, 2).addDimension(1, 3).addDimension(2, 4).build();
        SizeDimension result = cap1.add(cap2);
        assertEquals(3, result.getNuOfDimensions());
    }

    @Test
    public void whenAddingUpTwoThreeDimensionalCapacities_itShouldReturnCorrectCapValues() {
        SizeDimension cap1 = SizeDimension.Builder.newInstance().addDimension(0, 1).addDimension(1, 2).addDimension(2, 3).build();
        SizeDimension cap2 = SizeDimension.Builder.newInstance().addDimension(0, 2).addDimension(1, 3).addDimension(2, 4).build();
        SizeDimension result = cap1.add(cap2);
        assertEquals(3, result.get(0));
        assertEquals(5, result.get(1));
        assertEquals(7, result.get(2));
    }

    public void whenAddingUpTwoCapacitiesWithDifferentNuOfDimensions_itShouldAddThemCorrectly() {
        SizeDimension cap1 = SizeDimension.Builder.newInstance().addDimension(0, 1).addDimension(1, 2).build();
        SizeDimension cap2 = SizeDimension.Builder.newInstance().addDimension(0, 2).build();
        SizeDimension result = cap1.add(cap2);
        assertEquals(3, result.get(0));
        assertEquals(2, result.get(1));
    }

    @Test(expected = NullPointerException.class)
    public void whenOneOfArgsIsNullWhenAdding_itShouldThrowException() {
        SizeDimension cap1 = SizeDimension.Builder.newInstance().addDimension(0, 1).addDimension(1, 2).build();
        @SuppressWarnings("unused")
        SizeDimension result = cap1.add(null);
    }


    @Test
    public void whenSubtractingTwoOneDimensionalCapacities_itShouldReturnCorrectCapacityValues() {
        SizeDimension cap1 = SizeDimension.Builder.newInstance().addDimension(0, 1).build();
        SizeDimension cap2 = SizeDimension.Builder.newInstance().addDimension(0, 2).build();
        SizeDimension result = cap2.subtract(cap1);
        assertEquals(1, result.get(0));
    }

    @Test
    public void whenSubtractingTwoOneDimensionalCapacities_itShouldReturnCorrectNuOfDimensions() {
        SizeDimension cap1 = SizeDimension.Builder.newInstance().addDimension(0, 1).build();
        SizeDimension cap2 = SizeDimension.Builder.newInstance().addDimension(0, 2).build();
        SizeDimension result = cap2.subtract(cap1);
        assertEquals(1, result.getNuOfDimensions());
    }

    @Test
    public void whenSubtractingTwoThreeDimensionalCapacities_itShouldReturnCorrectNuOfDimensions() {
        SizeDimension cap1 = SizeDimension.Builder.newInstance().addDimension(0, 1).addDimension(1, 2).addDimension(2, 3).build();
        SizeDimension cap2 = SizeDimension.Builder.newInstance().addDimension(0, 2).addDimension(1, 3).addDimension(2, 4).build();
        SizeDimension result = cap2.subtract(cap1);
        assertEquals(3, result.getNuOfDimensions());
    }

    @Test
    public void whenSubtractingTwoThreeDimensionalCapacities_itShouldReturnCorrectCapValues() {
        SizeDimension cap1 = SizeDimension.Builder.newInstance().addDimension(0, 1).addDimension(1, 2).addDimension(2, 3).build();
        SizeDimension cap2 = SizeDimension.Builder.newInstance().addDimension(0, 2).addDimension(1, 3).addDimension(2, 4).build();
        SizeDimension result = cap2.subtract(cap1);
        assertEquals(1, result.get(0));
        assertEquals(1, result.get(1));
        assertEquals(1, result.get(2));
    }

    @Test
    public void whenSubtractingTwoCapacitiesWithDifferentNuOfDimensions_itShouldSubtractCorrectly() {
        SizeDimension cap1 = SizeDimension.Builder.newInstance().addDimension(0, 1).addDimension(1, 2).build();
        SizeDimension cap2 = SizeDimension.Builder.newInstance().addDimension(0, 2).build();
        SizeDimension result = cap2.subtract(cap1);
        assertEquals(1, result.get(0));
        assertEquals(-2, result.get(1));
    }

    @Test(expected = NullPointerException.class)
    public void whenOneOfArgsIsNullWhenSubtracting_itShouldThrowException() {
        SizeDimension cap1 = SizeDimension.Builder.newInstance().addDimension(0, 1).addDimension(1, 2).build();
        @SuppressWarnings("unused")
        SizeDimension result = cap1.subtract(null);
    }

    @Test
    public void whenSubtractingBiggerFromLower_itShouldSubtractCorrectly() {
        SizeDimension cap1 = SizeDimension.Builder.newInstance().addDimension(0, 1).addDimension(1, 2).addDimension(2, 3).build();
        SizeDimension cap2 = SizeDimension.Builder.newInstance().addDimension(0, 2).addDimension(1, 3).addDimension(2, 4).build();
        SizeDimension result = cap1.subtract(cap2);
        assertEquals(-1, result.get(0));
        assertEquals(-1, result.get(1));
        assertEquals(-1, result.get(2));
    }

    @Test
    public void whenOneCapIsLessThanAnother_itShouldReturnCorrectBoolean() {
        SizeDimension cap1 = SizeDimension.Builder.newInstance().addDimension(0, 1).addDimension(1, 2).addDimension(2, 3).build();
        SizeDimension cap2 = SizeDimension.Builder.newInstance().addDimension(0, 2).addDimension(1, 3).addDimension(2, 4).build();
        assertTrue(cap1.isLessOrEqual(cap2));
    }

    @Test
    public void whenOneCapIsLessThanAnother_itShouldReturnCorrectBoolean_v2() {
        SizeDimension cap1 = SizeDimension.Builder.newInstance().addDimension(0, 2).addDimension(1, 2).addDimension(2, 4).build();
        SizeDimension cap2 = SizeDimension.Builder.newInstance().addDimension(0, 2).addDimension(1, 3).addDimension(2, 4).build();
        assertTrue(cap1.isLessOrEqual(cap2));
    }

    @Test
    public void whenOneCapIsLessThanAnother_itShouldReturnCorrectBoolean_v3() {
        SizeDimension cap1 = SizeDimension.Builder.newInstance().addDimension(0, 2).addDimension(1, 3).addDimension(2, 4).build();
        SizeDimension cap2 = SizeDimension.Builder.newInstance().addDimension(0, 2).addDimension(1, 3).addDimension(2, 4).build();
        assertTrue(cap1.isLessOrEqual(cap2));
    }

    @Test
    public void whenOneCapIsBiggerThanAnother_itShouldReturnCorrectBoolean() {
        SizeDimension cap1 = SizeDimension.Builder.newInstance().addDimension(0, 2).addDimension(1, 3).addDimension(2, 4).build();
        SizeDimension cap2 = SizeDimension.Builder.newInstance().addDimension(0, 2).addDimension(1, 4).addDimension(2, 4).build();
        assertFalse(cap2.isLessOrEqual(cap1));
    }

    @Test
    public void whenOneCapIsBiggerThanAnother_greaterOrEqualShouldReturnTrue() {
        SizeDimension cap1 = SizeDimension.Builder.newInstance().addDimension(0, 2).addDimension(1, 3).addDimension(2, 4).build();
        SizeDimension cap2 = SizeDimension.Builder.newInstance().addDimension(0, 2).addDimension(1, 4).addDimension(2, 4).build();
        assertTrue(cap2.isGreaterOrEqual(cap1));
    }

    @Test
    public void whenOneCapIsBiggerThanAnother_greaterOrEqualShouldReturnTrue_v2() {
        SizeDimension cap1 = SizeDimension.Builder.newInstance().addDimension(0, 2).addDimension(1, 3).addDimension(2, 4).build();
        //which is zero-cap
        SizeDimension cap2 = SizeDimension.Builder.newInstance().build();
        assertTrue(cap1.isGreaterOrEqual(cap2));
    }

    @Test
    public void whenOneCapIsEqualToAnother_greaterOrEqualShouldReturnTrue() {
        SizeDimension cap1 = SizeDimension.Builder.newInstance().addDimension(0, 2).addDimension(1, 3).addDimension(2, 4).build();
        SizeDimension cap2 = SizeDimension.Builder.newInstance().addDimension(0, 2).addDimension(1, 3).addDimension(2, 4).build();
        assertTrue(cap2.isGreaterOrEqual(cap1));
    }

    @Test
    public void whenAddingTwo_itShouldReturnCorrectCap() {
        int wheelChairSpace = 0;
        int passengerSeats = 1;
        SizeDimension cap1 = SizeDimension.Builder.newInstance().addDimension(wheelChairSpace, 2).addDimension(passengerSeats, 10).build();
        SizeDimension wheelChair = SizeDimension.Builder.newInstance().addDimension(wheelChairSpace, 1).build();
        SizeDimension passenger = SizeDimension.Builder.newInstance().addDimension(passengerSeats, 1).build();
        SizeDimension wheelChair_plus_passenger = wheelChair.add(passenger);
        assertEquals(1, wheelChair_plus_passenger.get(wheelChairSpace));
        assertEquals(1, wheelChair_plus_passenger.get(passengerSeats));
        assertTrue(wheelChair_plus_passenger.isLessOrEqual(cap1));
    }

    @Test
    public void whenAddingTwo_itShouldReturnCorrectCap_v2() {
        int wheelChairSpace = 0;
        int passengerSeats = 1;
        int weight = 2;
        SizeDimension cap1 = SizeDimension.Builder.newInstance().addDimension(wheelChairSpace, 2).addDimension(passengerSeats, 10).addDimension(2, 100).build();
        SizeDimension wheelChair = SizeDimension.Builder.newInstance().addDimension(wheelChairSpace, 1).addDimension(weight, 80).build();
        SizeDimension passenger = SizeDimension.Builder.newInstance().addDimension(passengerSeats, 1).addDimension(weight, 30).build();
        SizeDimension wheelChair_plus_passenger = wheelChair.add(passenger);
        assertEquals(1, wheelChair_plus_passenger.get(wheelChairSpace));
        assertEquals(1, wheelChair_plus_passenger.get(passengerSeats));
        assertEquals(110, wheelChair_plus_passenger.get(weight));
        assertFalse(wheelChair_plus_passenger.isLessOrEqual(cap1));
    }

    @Test
    public void whenInvertingCap_itShouldBeDoneCorrectly() {
        SizeDimension cap = SizeDimension.Builder.newInstance().addDimension(0, 2).addDimension(1, 3).addDimension(2, 4).build();
        SizeDimension inverted = cap.invert();
        assertEquals(-2, inverted.get(0));
        assertEquals(-3, inverted.get(1));
        assertEquals(-4, inverted.get(2));
    }

    @Test
    public void whenDeterminingTheMaximumOfTwoCapacities_itShouldReturnCapWithMaxOfEachDimension() {
        SizeDimension cap1 = SizeDimension.Builder.newInstance().addDimension(0, 3).addDimension(1, 3).build();
        SizeDimension cap2 = SizeDimension.Builder.newInstance().addDimension(0, 2).addDimension(1, 4).build();
        assertEquals(3, SizeDimension.max(cap1, cap2).get(0));
        assertEquals(4, SizeDimension.max(cap1, cap2).get(1));
    }

    @Test
    public void whenDeterminingTheMaximumOfTwoCapacities_itShouldReturnCapWithMaxOfEachDimension_v2() {
        SizeDimension cap1 = SizeDimension.Builder.newInstance().addDimension(0, 1).addDimension(1, 3).build();
        SizeDimension cap2 = SizeDimension.Builder.newInstance().addDimension(0, 2).addDimension(1, 4).build();
        assertEquals(2, SizeDimension.max(cap1, cap2).get(0));
        assertEquals(4, SizeDimension.max(cap1, cap2).get(1));
    }

    @Test
    public void whenDeterminingTheMaximumOfTwoCapacities_itShouldReturnCapWithMaxOfEachDimension_v3() {
        SizeDimension cap1 = SizeDimension.Builder.newInstance().addDimension(0, 1).addDimension(1, 3).addDimension(2, 3).build();
        SizeDimension cap2 = SizeDimension.Builder.newInstance().addDimension(0, 2).addDimension(1, 4).build();
        assertEquals(2, SizeDimension.max(cap1, cap2).get(0));
        assertEquals(4, SizeDimension.max(cap1, cap2).get(1));
        assertEquals(3, SizeDimension.max(cap1, cap2).get(2));
    }

    @Test
    public void whenDividingTwoCapacities_itShouldReturn05() {
        SizeDimension cap1 = SizeDimension.Builder.newInstance().addDimension(0, 1).addDimension(1, 2).build();
        SizeDimension cap2 = SizeDimension.Builder.newInstance().addDimension(0, 2).addDimension(1, 4).build();
        assertEquals(0.5, SizeDimension.divide(cap1, cap2), 0.001);
    }

    @Test
    public void whenDividingTwoEqualCapacities_itShouldReturn10() {
        SizeDimension cap1 = SizeDimension.Builder.newInstance().addDimension(0, 2).addDimension(1, 4).build();
        SizeDimension cap2 = SizeDimension.Builder.newInstance().addDimension(0, 2).addDimension(1, 4).build();
        assertEquals(1.0, SizeDimension.divide(cap1, cap2), 0.001);
    }

    @Test
    public void whenDividingTwoCapacities_itShouldReturn00() {
        SizeDimension cap1 = SizeDimension.Builder.newInstance().addDimension(0, 0).addDimension(1, 0).build();
        SizeDimension cap2 = SizeDimension.Builder.newInstance().addDimension(0, 2).addDimension(1, 4).build();
        assertEquals(0.0, SizeDimension.divide(cap1, cap2), 0.001);
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenDividingByAZeroDim_itShouldThrowException() {
        SizeDimension cap1 = SizeDimension.Builder.newInstance().addDimension(0, 1).addDimension(1, 2).build();
        SizeDimension cap2 = SizeDimension.Builder.newInstance().addDimension(0, 2).addDimension(1, 0).build();
        SizeDimension.divide(cap1, cap2);
    }

    @Test
    public void whenBothDimOfNominatorAndDenominatorAreZero_divisionShouldIgnoreThisDim() {
        SizeDimension cap1 = SizeDimension.Builder.newInstance().addDimension(0, 1).addDimension(1, 2).addDimension(3, 0).build();
        SizeDimension cap2 = SizeDimension.Builder.newInstance().addDimension(0, 2).addDimension(1, 4).addDimension(3, 0).build();
        assertEquals(0.5, SizeDimension.divide(cap1, cap2), 0.001);
    }

    @Test
    public void whenDividingZeroCaps_itShouldReturnZero() {
        SizeDimension cap1 = SizeDimension.Builder.newInstance().build();
        SizeDimension cap2 = SizeDimension.Builder.newInstance().build();
        assertEquals(0.0, SizeDimension.divide(cap1, cap2), 0.001);
    }

    @Test
    public void shouldBeEqual() {
        SizeDimension cap1 = SizeDimension.Builder.newInstance().build();
        SizeDimension cap2 = SizeDimension.Builder.newInstance().build();
        Assert.assertTrue(cap1.equals(cap2));
    }

    @Test
    public void shouldBeEqual2() {
        SizeDimension cap1 = SizeDimension.Builder.newInstance().addDimension(0, 10).addDimension(1, 100).addDimension(2, 1000).build();
        SizeDimension cap2 = SizeDimension.Builder.newInstance().addDimension(0, 10).addDimension(2, 1000).addDimension(1, 100).build();
        Assert.assertTrue(cap1.equals(cap2));
    }

    @SuppressWarnings("deprecation")
    @Test
    public void legacyTest() {
        Capacity.Builder builder = Capacity.Builder.newInstance();
        Assert.assertTrue(builder instanceof SizeDimension.Builder);
        Assert.assertTrue(builder.build() instanceof SizeDimension);
    }
}
