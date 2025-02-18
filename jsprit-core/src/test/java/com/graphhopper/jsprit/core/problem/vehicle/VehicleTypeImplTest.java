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
package com.graphhopper.jsprit.core.problem.vehicle;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Vehicle Type Impl Test")
class VehicleTypeImplTest {

    @Test
    @DisplayName("When Type Has Negative Capacity Val _ throw Illegal State Expception")
    void whenTypeHasNegativeCapacityVal_throwIllegalStateExpception() {
        assertThrows(IllegalArgumentException.class, () -> {
            @SuppressWarnings("unused")
            VehicleTypeImpl type = VehicleTypeImpl.Builder.newInstance("t").addCapacityDimension(0, -10).build();
        });
    }

    @Test
    @DisplayName("When Adding Two Cap Dimension _ nu Of Dims Should Be Two")
    void whenAddingTwoCapDimension_nuOfDimsShouldBeTwo() {
        VehicleTypeImpl type = VehicleTypeImpl.Builder.newInstance("t").addCapacityDimension(0, 2).addCapacityDimension(1, 4).build();
        assertEquals(2, type.getCapacityDimensions().getNuOfDimensions());
    }

    @Test
    @DisplayName("When Adding Two Cap Dimension _ dim Values Must Be Correct")
    void whenAddingTwoCapDimension_dimValuesMustBeCorrect() {
        VehicleTypeImpl type = VehicleTypeImpl.Builder.newInstance("t").addCapacityDimension(0, 2).addCapacityDimension(1, 4).build();
        assertEquals(2, type.getCapacityDimensions().get(0));
        assertEquals(4, type.getCapacityDimensions().get(1));
    }

    @Test
    @DisplayName("When Type Is Built Without Specifying Capacity _ it Should Hv Cap With One Dim")
    void whenTypeIsBuiltWithoutSpecifyingCapacity_itShouldHvCapWithOneDim() {
        VehicleTypeImpl type = VehicleTypeImpl.Builder.newInstance("t").build();
        assertEquals(1, type.getCapacityDimensions().getNuOfDimensions());
    }

    @Test
    @DisplayName("When Type Is Built Without Specifying Capacity _ it Should Hv Cap Dim Val Of Zero")
    void whenTypeIsBuiltWithoutSpecifyingCapacity_itShouldHvCapDimValOfZero() {
        VehicleTypeImpl type = VehicleTypeImpl.Builder.newInstance("t").build();
        assertEquals(0, type.getCapacityDimensions().get(0));
    }

    @Test
    @DisplayName("When Calling Static New Builder Instance _ it Should Return New Builder Instance")
    void whenCallingStaticNewBuilderInstance_itShouldReturnNewBuilderInstance() {
        VehicleTypeImpl.Builder builder = VehicleTypeImpl.Builder.newInstance("foo");
        assertNotNull(builder);
    }

    @Test
    @DisplayName("When Building Type Just By Calling New Instance _ type Id Must Be Correct")
    void whenBuildingTypeJustByCallingNewInstance_typeIdMustBeCorrect() {
        VehicleTypeImpl type = VehicleTypeImpl.Builder.newInstance("foo").build();
        assertEquals(type.getTypeId(), "foo");
    }

    @Test
    @DisplayName("When Building Type Just By Calling New Instance _ cap Must Be Correct")
    void whenBuildingTypeJustByCallingNewInstance_capMustBeCorrect() {
        VehicleTypeImpl type = VehicleTypeImpl.Builder.newInstance("foo").build();
        assertEquals(0, type.getCapacityDimensions().get(0));
    }

    @Test
    @DisplayName("When Building Type With Cap Smaller Than Zero _ throw Illegal State Exception")
    void whenBuildingTypeWithCapSmallerThanZero_throwIllegalStateException() {
        assertThrows(IllegalArgumentException.class, () -> {
            @SuppressWarnings("unused")
            VehicleTypeImpl type = VehicleTypeImpl.Builder.newInstance("foo").addCapacityDimension(0, -10).build();
        });
    }

    @Test
    @DisplayName("When Building Type With Null Id _ throw Illegal State Exception")
    void whenBuildingTypeWithNullId_throwIllegalStateException() {
        assertThrows(IllegalArgumentException.class, () -> {
            @SuppressWarnings("unused")
            VehicleTypeImpl type = VehicleTypeImpl.Builder.newInstance(null).addCapacityDimension(0, 10).build();
        });
    }

    @Test
    @DisplayName("When Setting Max Velocity _ it Should Be Set Correctly")
    void whenSettingMaxVelocity_itShouldBeSetCorrectly() {
        VehicleTypeImpl type = VehicleTypeImpl.Builder.newInstance("type").setMaxVelocity(10).build();
        assertEquals(10, type.getMaxVelocity(), 0.0);
    }

    @Test
    @DisplayName("When Max Velocity Smaller Than Zero _ it Should Throw Exception")
    void whenMaxVelocitySmallerThanZero_itShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            @SuppressWarnings("unused")
            VehicleTypeImpl type = VehicleTypeImpl.Builder.newInstance("type").setMaxVelocity(-10).build();
        });
    }

    @Test
    @DisplayName("When Fixed Costs Smaller Than Zero _ it Should Throw Exception")
    void whenFixedCostsSmallerThanZero_itShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            @SuppressWarnings("unused")
            VehicleTypeImpl type = VehicleTypeImpl.Builder.newInstance("type").setFixedCost(-10).build();
        });
    }

    public void whenSettingFixedCosts_itShouldBeSetCorrectly() {
        VehicleTypeImpl type = VehicleTypeImpl.Builder.newInstance("type").setFixedCost(10).build();
        assertEquals(10.0, type.getVehicleCostParams().fix, 0.0);
    }

    @Test
    @DisplayName("When Per Distance Costs Smaller Than Zero _ it Should Throw Exception")
    void whenPerDistanceCostsSmallerThanZero_itShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            @SuppressWarnings("unused")
            VehicleTypeImpl type = VehicleTypeImpl.Builder.newInstance("type").setCostPerDistance(-10).build();
        });
    }

    @Test
    @DisplayName("When Setting Per Distance Costs _ it Should Be Set Correctly")
    void whenSettingPerDistanceCosts_itShouldBeSetCorrectly() {
        VehicleTypeImpl type = VehicleTypeImpl.Builder.newInstance("type").setCostPerDistance(10).build();
        assertEquals(10.0, type.getVehicleCostParams().perDistanceUnit, 0.0);
    }

    @Test
    @DisplayName("When Per Time Costs Smaller Than Zero _ it Should Throw Exception")
    void whenPerTimeCostsSmallerThanZero_itShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            @SuppressWarnings("unused")
            VehicleTypeImpl type = VehicleTypeImpl.Builder.newInstance("type").setCostPerTime(-10).build();
        });
    }

    @Test
    @DisplayName("When Having Two Types With The Same Id _ they Should Be Equal")
    void whenHavingTwoTypesWithTheSameId_theyShouldBeEqual() {
        VehicleTypeImpl type = VehicleTypeImpl.Builder.newInstance("type").setCostPerTime(10).build();
        VehicleTypeImpl type2 = VehicleTypeImpl.Builder.newInstance("type").setCostPerTime(10).build();
        assertTrue(type.equals(type2));
    }

    @Test
    @DisplayName("When Adding Profile _ it Should Be Correct")
    void whenAddingProfile_itShouldBeCorrect() {
        VehicleTypeImpl type = VehicleTypeImpl.Builder.newInstance("type").setProfile("car").build();
        assertEquals(type.getProfile(), "car");
    }

    @Test
    @DisplayName("When Setting User Data _ it Is Associated With The Vehicle Type")
    void whenSettingUserData_itIsAssociatedWithTheVehicleType() {
        VehicleType one = VehicleTypeImpl.Builder.newInstance("type").setUserData(new HashMap<String, Object>()).build();
        VehicleType two = VehicleTypeImpl.Builder.newInstance("type").setUserData(42).build();
        VehicleType three = VehicleTypeImpl.Builder.newInstance("type").build();
        assertTrue(one.getUserData() instanceof Map);
        assertEquals(42, two.getUserData());
        assertNull(three.getUserData());
    }

    @Test
    @DisplayName("Types Should Be Equal")
    void typesShouldBeEqual() {
        VehicleType one = VehicleTypeImpl.Builder.newInstance("type").setFixedCost(100).build();
        VehicleType two = VehicleTypeImpl.Builder.newInstance("type").setFixedCost(100).build();
        assertTrue(one.equals(two));
    }

    @Test
    @DisplayName("Types Should Be Not Equal")
    void typesShouldBeNotEqual() {
        VehicleType one = VehicleTypeImpl.Builder.newInstance("type").build();
        VehicleType two = VehicleTypeImpl.Builder.newInstance("type").setFixedCost(100).build();
        assertFalse(one.equals(two));
    }

    @Test
    @DisplayName("Types Should Be Not Equal 2")
    void typesShouldBeNotEqual2() {
        VehicleType one = VehicleTypeImpl.Builder.newInstance("type").addCapacityDimension(0, 10).build();
        VehicleType two = VehicleTypeImpl.Builder.newInstance("type").addCapacityDimension(0, 20).build();
        assertFalse(one.equals(two));
    }

    @Test
    @DisplayName("Types Should Be Equal 2")
    void typesShouldBeEqual2() {
        VehicleType one = VehicleTypeImpl.Builder.newInstance("type").addCapacityDimension(0, 10).build();
        VehicleType two = VehicleTypeImpl.Builder.newInstance("type").addCapacityDimension(0, 10).build();
        assertTrue(one.equals(two));
    }
}
