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

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class VehicleTypeImplTest {

    @Test(expected = IllegalArgumentException.class)
    public void whenTypeHasNegativeCapacityVal_throwIllegalStateExpception() {
        @SuppressWarnings("unused")
        VehicleTypeImpl type = VehicleTypeImpl.Builder.newInstance("t").addCapacityDimension(0, -10).build();
    }

    @Test
    public void whenAddingTwoCapDimension_nuOfDimsShouldBeTwo() {
        VehicleTypeImpl type = VehicleTypeImpl.Builder.newInstance("t")
            .addCapacityDimension(0, 2)
            .addCapacityDimension(1, 4)
            .build();
        assertEquals(2, type.getCapacityDimensions().getNuOfDimensions());
    }

    @Test
    public void whenAddingTwoCapDimension_dimValuesMustBeCorrect() {
        VehicleTypeImpl type = VehicleTypeImpl.Builder.newInstance("t")
            .addCapacityDimension(0, 2)
            .addCapacityDimension(1, 4)
            .build();
        assertEquals(2, type.getCapacityDimensions().get(0));
        assertEquals(4, type.getCapacityDimensions().get(1));
    }

    @Test
    public void whenTypeIsBuiltWithoutSpecifyingCapacity_itShouldHvCapWithOneDim() {
        VehicleTypeImpl type = VehicleTypeImpl.Builder.newInstance("t").build();
        assertEquals(1, type.getCapacityDimensions().getNuOfDimensions());
    }

    @Test
    public void whenTypeIsBuiltWithoutSpecifyingCapacity_itShouldHvCapDimValOfZero() {
        VehicleTypeImpl type = VehicleTypeImpl.Builder.newInstance("t").build();
        assertEquals(0, type.getCapacityDimensions().get(0));
    }

    @Test
    public void whenCallingStaticNewBuilderInstance_itShouldReturnNewBuilderInstance() {
        VehicleTypeImpl.Builder builder = VehicleTypeImpl.Builder.newInstance("foo");
        assertNotNull(builder);
    }

    @Test
    public void whenBuildingTypeJustByCallingNewInstance_typeIdMustBeCorrect() {
        VehicleTypeImpl type = VehicleTypeImpl.Builder.newInstance("foo").build();
        assertEquals("foo", type.getTypeId());
    }

    @Test
    public void whenBuildingTypeJustByCallingNewInstance_capMustBeCorrect() {
        VehicleTypeImpl type = VehicleTypeImpl.Builder.newInstance("foo").build();
        assertEquals(0, type.getCapacityDimensions().get(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenBuildingTypeWithCapSmallerThanZero_throwIllegalStateException() {
        @SuppressWarnings("unused")
        VehicleTypeImpl type = VehicleTypeImpl.Builder.newInstance("foo").addCapacityDimension(0, -10).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenBuildingTypeWithNullId_throwIllegalStateException() {
        @SuppressWarnings("unused")
        VehicleTypeImpl type = VehicleTypeImpl.Builder.newInstance(null).addCapacityDimension(0, 10).build();
    }


    @Test
    public void whenSettingMaxVelocity_itShouldBeSetCorrectly() {
        VehicleTypeImpl type = VehicleTypeImpl.Builder.newInstance("type").setMaxVelocity(10).build();
        assertEquals(10, type.getMaxVelocity(), 0.0);
    }


    @Test(expected = IllegalArgumentException.class)
    public void whenMaxVelocitySmallerThanZero_itShouldThrowException() {
        @SuppressWarnings("unused")
        VehicleTypeImpl type = VehicleTypeImpl.Builder.newInstance("type").setMaxVelocity(-10).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenFixedCostsSmallerThanZero_itShouldThrowException() {
        @SuppressWarnings("unused")
        VehicleTypeImpl type = VehicleTypeImpl.Builder.newInstance("type").setFixedCost(-10).build();
    }

    public void whenSettingFixedCosts_itShouldBeSetCorrectly() {
        VehicleTypeImpl type = VehicleTypeImpl.Builder.newInstance("type").setFixedCost(10).build();
        assertEquals(10.0, type.getVehicleCostParams().fix, 0.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenPerDistanceCostsSmallerThanZero_itShouldThrowException() {
        @SuppressWarnings("unused")
        VehicleTypeImpl type = VehicleTypeImpl.Builder.newInstance("type").setCostPerDistance(-10).build();
    }

    public void whenSettingPerDistanceCosts_itShouldBeSetCorrectly() {
        VehicleTypeImpl type = VehicleTypeImpl.Builder.newInstance("type").setCostPerDistance(10).build();
        assertEquals(10.0, type.getVehicleCostParams().perDistanceUnit, 0.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenPerTimeCostsSmallerThanZero_itShouldThrowException() {
        @SuppressWarnings("unused")
        VehicleTypeImpl type = VehicleTypeImpl.Builder.newInstance("type").setCostPerTime(-10).build();
    }

    @Test
    public void whenSettingPerTimeCosts_itShouldBeSetCorrectly() {
        VehicleTypeImpl type = VehicleTypeImpl.Builder.newInstance("type").setCostPerTime(10).build();
        assertEquals(10.0, type.getVehicleCostParams().perTimeUnit, 0.0);
    }

    @Test
    public void whenHavingTwoTypesWithTheSameId_theyShouldBeEqual() {
        VehicleTypeImpl type = VehicleTypeImpl.Builder.newInstance("type").setCostPerTime(10).build();
        VehicleTypeImpl type2 = VehicleTypeImpl.Builder.newInstance("type").setCostPerTime(10).build();
        assertTrue(type.equals(type2));
    }

    @Test
    public void whenAddingProfile_itShouldBeCorrect() {
        VehicleTypeImpl type = VehicleTypeImpl.Builder.newInstance("type").setProfile("car").build();
        assertEquals("car", type.getProfile());
    }


    @Test
    public void whenSettingUserData_itIsAssociatedWithTheVehicleType() {
        VehicleType one = VehicleTypeImpl.Builder.newInstance("type").setUserData(new HashMap<String, Object>())
            .build();
        VehicleType two = VehicleTypeImpl.Builder.newInstance("type").setUserData(42).build();
        VehicleType three = VehicleTypeImpl.Builder.newInstance("type").build();

        assertTrue(one.getUserData() instanceof Map);
        assertEquals(42, two.getUserData());
        assertNull(three.getUserData());
    }
}
