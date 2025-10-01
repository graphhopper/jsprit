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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Capacity Test")
class CapacityTest {

    @Test
    @DisplayName("When Setting Simply One Cap Dimension _ nu Of Dimension Must Be Correct")
    void whenSettingSimplyOneCapDimension_nuOfDimensionMustBeCorrect() {
        Capacity.Builder capBuilder = Capacity.Builder.newInstance();
        capBuilder.addDimension(0, 4);
        Capacity cap = capBuilder.build();
        Assertions.assertEquals(1, cap.getNuOfDimensions());
    }

    @Test
    @DisplayName("When Setting Two Cap Dimension _ nu Of Dimension Must Be Correct")
    void whenSettingTwoCapDimension_nuOfDimensionMustBeCorrect() {
        Capacity.Builder capBuilder = Capacity.Builder.newInstance();
        capBuilder.addDimension(0, 4);
        capBuilder.addDimension(1, 10);
        Capacity cap = capBuilder.build();
        Assertions.assertEquals(2, cap.getNuOfDimensions());
    }

    @Test
    @DisplayName("When Setting Random Nu Of Cap Dimension _ nu Of Dimension Must Be Correct")
    void whenSettingRandomNuOfCapDimension_nuOfDimensionMustBeCorrect() {
        Random rand = new Random();
        int nuOfCapDimensions = 1 + rand.nextInt(100);
        Capacity.Builder capBuilder = Capacity.Builder.newInstance();
        capBuilder.addDimension(nuOfCapDimensions - 1, 4);
        Capacity cap = capBuilder.build();
        Assertions.assertEquals(nuOfCapDimensions, cap.getNuOfDimensions());
    }

    @Test
    @DisplayName("When Setting One Dim Value _ value Must Be Correct")
    void whenSettingOneDimValue_valueMustBeCorrect() {
        Capacity.Builder capBuilder = Capacity.Builder.newInstance();
        capBuilder.addDimension(0, 4);
        Capacity cap = capBuilder.build();
        Assertions.assertEquals(4, cap.get(0));
    }

    @Test
    @DisplayName("When Getting Index Which Is Higher Than Nu Of Cap Dimensions _ it Should Return 0")
    void whenGettingIndexWhichIsHigherThanNuOfCapDimensions_itShouldReturn0() {
        Capacity.Builder capBuilder = Capacity.Builder.newInstance();
        capBuilder.addDimension(0, 4);
        Capacity cap = capBuilder.build();
        Assertions.assertEquals(0, cap.get(2));
    }

    @Test
    @DisplayName("When Setting No Dim _ Default Is One Dim With Dim Value Of Zero")
    void whenSettingNoDim_DefaultIsOneDimWithDimValueOfZero() {
        Capacity.Builder capBuilder = Capacity.Builder.newInstance();
        Capacity cap = capBuilder.build();
        Assertions.assertEquals(1, cap.getNuOfDimensions());
        Assertions.assertEquals(0, cap.get(0));
    }

    @Test
    @DisplayName("When Copying Capacity With Two Cap Dim _ copied Obj Should Hv Same Nu Of Dims")
    void whenCopyingCapacityWithTwoCapDim_copiedObjShouldHvSameNuOfDims() {
        Capacity.Builder capBuilder = Capacity.Builder.newInstance();
        capBuilder.addDimension(0, 4);
        capBuilder.addDimension(1, 10);
        Capacity cap = capBuilder.build();
        Capacity copiedCapacity = Capacity.copyOf(cap);
        Assertions.assertEquals(2, copiedCapacity.getNuOfDimensions());
    }

    @Test
    @DisplayName("When Copying Capacity With Two Cap Dim _ copied Obj Should Hv Same Values")
    void whenCopyingCapacityWithTwoCapDim_copiedObjShouldHvSameValues() {
        Capacity.Builder capBuilder = Capacity.Builder.newInstance();
        capBuilder.addDimension(0, 4);
        capBuilder.addDimension(1, 10);
        Capacity cap = capBuilder.build();
        Capacity copiedCapacity = Capacity.copyOf(cap);
        Assertions.assertEquals(4, copiedCapacity.get(0));
        Assertions.assertEquals(10, copiedCapacity.get(1));
    }

    @Test
    @DisplayName("When Copying Null _ it Should Return Null")
    void whenCopyingNull_itShouldReturnNull() {
        Capacity nullCap = Capacity.copyOf(null);
        Assertions.assertTrue(nullCap == null);
    }

    @Test
    @DisplayName("When Adding Up Two One Dimensional Capacities _ it Should Return Correct Capacity Values")
    void whenAddingUpTwoOneDimensionalCapacities_itShouldReturnCorrectCapacityValues() {
        Capacity cap1 = Capacity.Builder.newInstance().addDimension(0, 1).build();
        Capacity cap2 = Capacity.Builder.newInstance().addDimension(0, 2).build();
        Capacity result = Capacity.addup(cap1, cap2);
        Assertions.assertEquals(3, result.get(0));
    }

    @Test
    @DisplayName("When Adding Up Two One Dimensional Capacities _ it Should Return Correct Nu Of Dimensions")
    void whenAddingUpTwoOneDimensionalCapacities_itShouldReturnCorrectNuOfDimensions() {
        Capacity cap1 = Capacity.Builder.newInstance().addDimension(0, 1).build();
        Capacity cap2 = Capacity.Builder.newInstance().addDimension(0, 2).build();
        Capacity result = Capacity.addup(cap1, cap2);
        Assertions.assertEquals(1, result.getNuOfDimensions());
    }

    @Test
    @DisplayName("When Adding Up Two Three Dimensional Capacities _ it Should Return Correct Nu Of Dimensions")
    void whenAddingUpTwoThreeDimensionalCapacities_itShouldReturnCorrectNuOfDimensions() {
        Capacity cap1 = Capacity.Builder.newInstance().addDimension(0, 1).addDimension(1, 2).addDimension(2, 3).build();
        Capacity cap2 = Capacity.Builder.newInstance().addDimension(0, 2).addDimension(1, 3).addDimension(2, 4).build();
        Capacity result = Capacity.addup(cap1, cap2);
        Assertions.assertEquals(3, result.getNuOfDimensions());
    }

    @Test
    @DisplayName("When Adding Up Two Three Dimensional Capacities _ it Should Return Correct Cap Values")
    void whenAddingUpTwoThreeDimensionalCapacities_itShouldReturnCorrectCapValues() {
        Capacity cap1 = Capacity.Builder.newInstance().addDimension(0, 1).addDimension(1, 2).addDimension(2, 3).build();
        Capacity cap2 = Capacity.Builder.newInstance().addDimension(0, 2).addDimension(1, 3).addDimension(2, 4).build();
        Capacity result = Capacity.addup(cap1, cap2);
        Assertions.assertEquals(3, result.get(0));
        Assertions.assertEquals(5, result.get(1));
        Assertions.assertEquals(7, result.get(2));
    }

    public void whenAddingUpTwoCapacitiesWithDifferentNuOfDimensions_itShouldAddThemCorrectly() {
        Capacity cap1 = Capacity.Builder.newInstance().addDimension(0, 1).addDimension(1, 2).build();
        Capacity cap2 = Capacity.Builder.newInstance().addDimension(0, 2).build();
        Capacity result = Capacity.addup(cap1, cap2);
        Assertions.assertEquals(3, result.get(0));
        Assertions.assertEquals(2, result.get(1));
    }

    @Test
    @DisplayName("When One Of Args Is Null When Adding _ it Should Throw Exception")
    void whenOneOfArgsIsNullWhenAdding_itShouldThrowException() {
        assertThrows(NullPointerException.class, () -> {
            Capacity cap1 = Capacity.Builder.newInstance().addDimension(0, 1).addDimension(1, 2).build();
            @SuppressWarnings("unused")
            Capacity result = Capacity.addup(cap1, null);
        });
    }

    @Test
    @DisplayName("When Subtracting Two One Dimensional Capacities _ it Should Return Correct Capacity Values")
    void whenSubtractingTwoOneDimensionalCapacities_itShouldReturnCorrectCapacityValues() {
        Capacity cap1 = Capacity.Builder.newInstance().addDimension(0, 1).build();
        Capacity cap2 = Capacity.Builder.newInstance().addDimension(0, 2).build();
        Capacity result = Capacity.subtract(cap2, cap1);
        Assertions.assertEquals(1, result.get(0));
    }

    @Test
    @DisplayName("When Subtracting Two One Dimensional Capacities _ it Should Return Correct Nu Of Dimensions")
    void whenSubtractingTwoOneDimensionalCapacities_itShouldReturnCorrectNuOfDimensions() {
        Capacity cap1 = Capacity.Builder.newInstance().addDimension(0, 1).build();
        Capacity cap2 = Capacity.Builder.newInstance().addDimension(0, 2).build();
        Capacity result = Capacity.subtract(cap2, cap1);
        Assertions.assertEquals(1, result.getNuOfDimensions());
    }

    @Test
    @DisplayName("When Subtracting Two Three Dimensional Capacities _ it Should Return Correct Nu Of Dimensions")
    void whenSubtractingTwoThreeDimensionalCapacities_itShouldReturnCorrectNuOfDimensions() {
        Capacity cap1 = Capacity.Builder.newInstance().addDimension(0, 1).addDimension(1, 2).addDimension(2, 3).build();
        Capacity cap2 = Capacity.Builder.newInstance().addDimension(0, 2).addDimension(1, 3).addDimension(2, 4).build();
        Capacity result = Capacity.subtract(cap2, cap1);
        Assertions.assertEquals(3, result.getNuOfDimensions());
    }

    @Test
    @DisplayName("When Subtracting Two Three Dimensional Capacities _ it Should Return Correct Cap Values")
    void whenSubtractingTwoThreeDimensionalCapacities_itShouldReturnCorrectCapValues() {
        Capacity cap1 = Capacity.Builder.newInstance().addDimension(0, 1).addDimension(1, 2).addDimension(2, 3).build();
        Capacity cap2 = Capacity.Builder.newInstance().addDimension(0, 2).addDimension(1, 3).addDimension(2, 4).build();
        Capacity result = Capacity.subtract(cap2, cap1);
        Assertions.assertEquals(1, result.get(0));
        Assertions.assertEquals(1, result.get(1));
        Assertions.assertEquals(1, result.get(2));
    }

    @Test
    @DisplayName("When Subtracting Two Capacities With Different Nu Of Dimensions _ it Should Subtract Correctly")
    void whenSubtractingTwoCapacitiesWithDifferentNuOfDimensions_itShouldSubtractCorrectly() {
        Capacity cap1 = Capacity.Builder.newInstance().addDimension(0, 1).addDimension(1, 2).build();
        Capacity cap2 = Capacity.Builder.newInstance().addDimension(0, 2).build();
        Capacity result = Capacity.subtract(cap2, cap1);
        Assertions.assertEquals(1, result.get(0));
        Assertions.assertEquals(-2, result.get(1));
    }

    @Test
    @DisplayName("When One Of Args Is Null When Subtracting _ it Should Throw Exception")
    void whenOneOfArgsIsNullWhenSubtracting_itShouldThrowException() {
        assertThrows(NullPointerException.class, () -> {
            Capacity cap1 = Capacity.Builder.newInstance().addDimension(0, 1).addDimension(1, 2).build();
            @SuppressWarnings("unused")
            Capacity result = Capacity.subtract(cap1, null);
        });
    }

    @Test
    @DisplayName("When Subtracting Bigger From Lower _ it Should Subtract Correctly")
    void whenSubtractingBiggerFromLower_itShouldSubtractCorrectly() {
        Capacity cap1 = Capacity.Builder.newInstance().addDimension(0, 1).addDimension(1, 2).addDimension(2, 3).build();
        Capacity cap2 = Capacity.Builder.newInstance().addDimension(0, 2).addDimension(1, 3).addDimension(2, 4).build();
        Capacity result = Capacity.subtract(cap1, cap2);
        Assertions.assertEquals(-1, result.get(0));
        Assertions.assertEquals(-1, result.get(1));
        Assertions.assertEquals(-1, result.get(2));
    }

    @Test
    @DisplayName("When One Cap Is Less Than Another _ it Should Return Correct Boolean")
    void whenOneCapIsLessThanAnother_itShouldReturnCorrectBoolean() {
        Capacity cap1 = Capacity.Builder.newInstance().addDimension(0, 1).addDimension(1, 2).addDimension(2, 3).build();
        Capacity cap2 = Capacity.Builder.newInstance().addDimension(0, 2).addDimension(1, 3).addDimension(2, 4).build();
        Assertions.assertTrue(cap1.isLessOrEqual(cap2));
    }

    @Test
    @DisplayName("When One Cap Is Less Than Another _ it Should Return Correct Boolean _ v 2")
    void whenOneCapIsLessThanAnother_itShouldReturnCorrectBoolean_v2() {
        Capacity cap1 = Capacity.Builder.newInstance().addDimension(0, 2).addDimension(1, 2).addDimension(2, 4).build();
        Capacity cap2 = Capacity.Builder.newInstance().addDimension(0, 2).addDimension(1, 3).addDimension(2, 4).build();
        Assertions.assertTrue(cap1.isLessOrEqual(cap2));
    }

    @Test
    @DisplayName("When One Cap Is Less Than Another _ it Should Return Correct Boolean _ v 3")
    void whenOneCapIsLessThanAnother_itShouldReturnCorrectBoolean_v3() {
        Capacity cap1 = Capacity.Builder.newInstance().addDimension(0, 2).addDimension(1, 3).addDimension(2, 4).build();
        Capacity cap2 = Capacity.Builder.newInstance().addDimension(0, 2).addDimension(1, 3).addDimension(2, 4).build();
        Assertions.assertTrue(cap1.isLessOrEqual(cap2));
    }

    @Test
    @DisplayName("When One Cap Is Bigger Than Another _ it Should Return Correct Boolean")
    void whenOneCapIsBiggerThanAnother_itShouldReturnCorrectBoolean() {
        Capacity cap1 = Capacity.Builder.newInstance().addDimension(0, 2).addDimension(1, 3).addDimension(2, 4).build();
        Capacity cap2 = Capacity.Builder.newInstance().addDimension(0, 2).addDimension(1, 4).addDimension(2, 4).build();
        Assertions.assertFalse(cap2.isLessOrEqual(cap1));
    }

    @Test
    @DisplayName("When One Cap Is Bigger Than Another _ greater Or Equal Should Return True")
    void whenOneCapIsBiggerThanAnother_greaterOrEqualShouldReturnTrue() {
        Capacity cap1 = Capacity.Builder.newInstance().addDimension(0, 2).addDimension(1, 3).addDimension(2, 4).build();
        Capacity cap2 = Capacity.Builder.newInstance().addDimension(0, 2).addDimension(1, 4).addDimension(2, 4).build();
        Assertions.assertTrue(cap2.isGreaterOrEqual(cap1));
    }

    @Test
    @DisplayName("When One Cap Is Bigger Than Another _ greater Or Equal Should Return True _ v 2")
    void whenOneCapIsBiggerThanAnother_greaterOrEqualShouldReturnTrue_v2() {
        Capacity cap1 = Capacity.Builder.newInstance().addDimension(0, 2).addDimension(1, 3).addDimension(2, 4).build();
        // which is zero-cap
        Capacity cap2 = Capacity.Builder.newInstance().build();
        Assertions.assertTrue(cap1.isGreaterOrEqual(cap2));
    }

    @Test
    @DisplayName("When One Cap Is Equal To Another _ greater Or Equal Should Return True")
    void whenOneCapIsEqualToAnother_greaterOrEqualShouldReturnTrue() {
        Capacity cap1 = Capacity.Builder.newInstance().addDimension(0, 2).addDimension(1, 3).addDimension(2, 4).build();
        Capacity cap2 = Capacity.Builder.newInstance().addDimension(0, 2).addDimension(1, 3).addDimension(2, 4).build();
        Assertions.assertTrue(cap2.isGreaterOrEqual(cap1));
    }

    @Test
    @DisplayName("When Adding Two _ it Should Return Correct Cap")
    void whenAddingTwo_itShouldReturnCorrectCap() {
        int wheelChairSpace = 0;
        int passengerSeats = 1;
        Capacity cap1 = Capacity.Builder.newInstance().addDimension(wheelChairSpace, 2).addDimension(passengerSeats, 10).build();
        Capacity wheelChair = Capacity.Builder.newInstance().addDimension(wheelChairSpace, 1).build();
        Capacity passenger = Capacity.Builder.newInstance().addDimension(passengerSeats, 1).build();
        Capacity wheelChair_plus_passenger = Capacity.addup(wheelChair, passenger);
        Assertions.assertEquals(1, wheelChair_plus_passenger.get(wheelChairSpace));
        Assertions.assertEquals(1, wheelChair_plus_passenger.get(passengerSeats));
        Assertions.assertTrue(wheelChair_plus_passenger.isLessOrEqual(cap1));
    }

    @Test
    @DisplayName("When Adding Two _ it Should Return Correct Cap _ v 2")
    void whenAddingTwo_itShouldReturnCorrectCap_v2() {
        int wheelChairSpace = 0;
        int passengerSeats = 1;
        int weight = 2;
        Capacity cap1 = Capacity.Builder.newInstance().addDimension(wheelChairSpace, 2).addDimension(passengerSeats, 10).addDimension(2, 100).build();
        Capacity wheelChair = Capacity.Builder.newInstance().addDimension(wheelChairSpace, 1).addDimension(weight, 80).build();
        Capacity passenger = Capacity.Builder.newInstance().addDimension(passengerSeats, 1).addDimension(weight, 30).build();
        Capacity wheelChair_plus_passenger = Capacity.addup(wheelChair, passenger);
        Assertions.assertEquals(1, wheelChair_plus_passenger.get(wheelChairSpace));
        Assertions.assertEquals(1, wheelChair_plus_passenger.get(passengerSeats));
        Assertions.assertEquals(110, wheelChair_plus_passenger.get(weight));
        Assertions.assertFalse(wheelChair_plus_passenger.isLessOrEqual(cap1));
    }

    @Test
    @DisplayName("When Inverting Cap _ it Should Be Done Correctly")
    void whenInvertingCap_itShouldBeDoneCorrectly() {
        Capacity cap = Capacity.Builder.newInstance().addDimension(0, 2).addDimension(1, 3).addDimension(2, 4).build();
        Capacity inverted = Capacity.invert(cap);
        Assertions.assertEquals(-2, inverted.get(0));
        Assertions.assertEquals(-3, inverted.get(1));
        Assertions.assertEquals(-4, inverted.get(2));
    }

    @Test
    @DisplayName("When Determining The Maximum Of Two Capacities _ it Should Return Cap With Max Of Each Dimension")
    void whenDeterminingTheMaximumOfTwoCapacities_itShouldReturnCapWithMaxOfEachDimension() {
        Capacity cap1 = Capacity.Builder.newInstance().addDimension(0, 3).addDimension(1, 3).build();
        Capacity cap2 = Capacity.Builder.newInstance().addDimension(0, 2).addDimension(1, 4).build();
        Assertions.assertEquals(3, Capacity.max(cap1, cap2).get(0));
        Assertions.assertEquals(4, Capacity.max(cap1, cap2).get(1));
    }

    @Test
    @DisplayName("When Determining The Maximum Of Two Capacities _ it Should Return Cap With Max Of Each Dimension _ v 2")
    void whenDeterminingTheMaximumOfTwoCapacities_itShouldReturnCapWithMaxOfEachDimension_v2() {
        Capacity cap1 = Capacity.Builder.newInstance().addDimension(0, 1).addDimension(1, 3).build();
        Capacity cap2 = Capacity.Builder.newInstance().addDimension(0, 2).addDimension(1, 4).build();
        Assertions.assertEquals(2, Capacity.max(cap1, cap2).get(0));
        Assertions.assertEquals(4, Capacity.max(cap1, cap2).get(1));
    }

    @Test
    @DisplayName("When Determining The Maximum Of Two Capacities _ it Should Return Cap With Max Of Each Dimension _ v 3")
    void whenDeterminingTheMaximumOfTwoCapacities_itShouldReturnCapWithMaxOfEachDimension_v3() {
        Capacity cap1 = Capacity.Builder.newInstance().addDimension(0, 1).addDimension(1, 3).addDimension(2, 3).build();
        Capacity cap2 = Capacity.Builder.newInstance().addDimension(0, 2).addDimension(1, 4).build();
        Assertions.assertEquals(2, Capacity.max(cap1, cap2).get(0));
        Assertions.assertEquals(4, Capacity.max(cap1, cap2).get(1));
        Assertions.assertEquals(3, Capacity.max(cap1, cap2).get(2));
    }

    @Test
    @DisplayName("When Dividing Two Capacities _ it Should Return 05")
    void whenDividingTwoCapacities_itShouldReturn05() {
        Capacity cap1 = Capacity.Builder.newInstance().addDimension(0, 1).addDimension(1, 2).build();
        Capacity cap2 = Capacity.Builder.newInstance().addDimension(0, 2).addDimension(1, 4).build();
        Assertions.assertEquals(0.5, Capacity.divide(cap1, cap2), 0.001);
    }

    @Test
    @DisplayName("When Dividing Two Equal Capacities _ it Should Return 10")
    void whenDividingTwoEqualCapacities_itShouldReturn10() {
        Capacity cap1 = Capacity.Builder.newInstance().addDimension(0, 2).addDimension(1, 4).build();
        Capacity cap2 = Capacity.Builder.newInstance().addDimension(0, 2).addDimension(1, 4).build();
        Assertions.assertEquals(1.0, Capacity.divide(cap1, cap2), 0.001);
    }

    @Test
    @DisplayName("When Dividing Two Capacities _ it Should Return 00")
    void whenDividingTwoCapacities_itShouldReturn00() {
        Capacity cap1 = Capacity.Builder.newInstance().addDimension(0, 0).addDimension(1, 0).build();
        Capacity cap2 = Capacity.Builder.newInstance().addDimension(0, 2).addDimension(1, 4).build();
        Assertions.assertEquals(0.0, Capacity.divide(cap1, cap2), 0.001);
    }

    @Test
    @DisplayName("When Dividing By A Zero Dim _ it Should Throw Exception")
    void whenDividingByAZeroDim_itShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            Capacity cap1 = Capacity.Builder.newInstance().addDimension(0, 1).addDimension(1, 2).build();
            Capacity cap2 = Capacity.Builder.newInstance().addDimension(0, 2).addDimension(1, 0).build();
            Capacity.divide(cap1, cap2);
        });
    }

    @Test
    @DisplayName("When Both Dim Of Nominator And Denominator Are Zero _ division Should Ignore This Dim")
    void whenBothDimOfNominatorAndDenominatorAreZero_divisionShouldIgnoreThisDim() {
        Capacity cap1 = Capacity.Builder.newInstance().addDimension(0, 1).addDimension(1, 2).addDimension(3, 0).build();
        Capacity cap2 = Capacity.Builder.newInstance().addDimension(0, 2).addDimension(1, 4).addDimension(3, 0).build();
        Assertions.assertEquals(0.5, Capacity.divide(cap1, cap2), 0.001);
    }

    @Test
    @DisplayName("When Dividing Zero Caps _ it Should Return Zero")
    void whenDividingZeroCaps_itShouldReturnZero() {
        Capacity cap1 = Capacity.Builder.newInstance().build();
        Capacity cap2 = Capacity.Builder.newInstance().build();
        Assertions.assertEquals(0.0, Capacity.divide(cap1, cap2), 0.001);
    }

    @Test
    @DisplayName("Should Be Equal")
    void shouldBeEqual() {
        Capacity cap1 = Capacity.Builder.newInstance().build();
        Capacity cap2 = Capacity.Builder.newInstance().build();
        Assertions.assertTrue(cap1.equals(cap2));
    }

    @Test
    @DisplayName("Should Be Equal 2")
    void shouldBeEqual2() {
        Capacity cap1 = Capacity.Builder.newInstance().addDimension(0, 10).addDimension(1, 100).addDimension(2, 1000).build();
        Capacity cap2 = Capacity.Builder.newInstance().addDimension(0, 10).addDimension(2, 1000).addDimension(1, 100).build();
        Assertions.assertTrue(cap1.equals(cap2));
    }
}
