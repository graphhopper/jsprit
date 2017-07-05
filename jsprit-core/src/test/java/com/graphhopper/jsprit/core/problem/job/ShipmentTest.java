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
package com.graphhopper.jsprit.core.problem.job;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow;
import com.graphhopper.jsprit.core.util.Coordinate;
import com.graphhopper.jsprit.core.util.TestUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.*;

public class ShipmentTest {

    @Test
    public void whenTwoShipmentsHaveTheSameId_theyReferencesShouldBeUnEqual() {
        Shipment one = Shipment.Builder.newInstance("s").addSizeDimension(0, 10).setPickupLocation(Location.Builder.newInstance().setId("foo").build()).
            setDeliveryLocation(TestUtils.loc("foofoo")).setPickupServiceTime(10).setDeliveryServiceTime(20).build();
        Shipment two = Shipment.Builder.newInstance("s").addSizeDimension(0, 10).setPickupLocation(Location.Builder.newInstance().setId("foo").build()).
            setDeliveryLocation(TestUtils.loc("foofoo")).setPickupServiceTime(10).setDeliveryServiceTime(20).build();

        assertTrue(one != two);
    }

    @Test
    public void whenTwoShipmentsHaveTheSameId_theyShouldBeEqual() {
        Shipment one = Shipment.Builder.newInstance("s").addSizeDimension(0, 10).setPickupLocation(Location.Builder.newInstance().setId("foo").build()).
            setDeliveryLocation(TestUtils.loc("foofoo")).setPickupServiceTime(10).setDeliveryServiceTime(20).build();
        Shipment two = Shipment.Builder.newInstance("s").addSizeDimension(0, 10).setPickupLocation(Location.Builder.newInstance().setId("foo").build()).
            setDeliveryLocation(TestUtils.loc("foofoo")).setPickupServiceTime(10).setDeliveryServiceTime(20).build();

        assertTrue(one.equals(two));
    }

    @Test
    public void whenShipmentIsInstantiatedWithASizeOf10_theSizeShouldBe10() {
        Shipment one = Shipment.Builder.newInstance("s").addSizeDimension(0, 10).setPickupLocation(Location.Builder.newInstance().setId("foo").build()).
            setDeliveryLocation(TestUtils.loc("foofoo")).setPickupServiceTime(10).setDeliveryServiceTime(20).build();
        assertEquals(10, one.getSize().get(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenShipmentIsBuiltWithNegativeDemand_itShouldThrowException() {
        @SuppressWarnings("unused")
        Shipment one = Shipment.Builder.newInstance("s").addSizeDimension(0, -10)
            .setPickupLocation(Location.Builder.newInstance().setId("foo").build())
            .setDeliveryLocation(TestUtils.loc("foofoo")).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenShipmentIsBuiltWithNegativeDemand_itShouldThrowException_v2() {
        @SuppressWarnings("unused")
        Shipment one = Shipment.Builder.newInstance("s").addSizeDimension(0, -10)
            .setPickupLocation(Location.Builder.newInstance().setId("foo").build())
            .setDeliveryLocation(TestUtils.loc("foofoo")).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenIdIsNull_itShouldThrowException() {
        @SuppressWarnings("unused")
        Shipment one = Shipment.Builder.newInstance(null).addSizeDimension(0, 10)
            .setPickupLocation(Location.Builder.newInstance().setId("foo").build())
            .setDeliveryLocation(TestUtils.loc("foofoo")).build();
    }

    @Test
    public void whenCallingForANewBuilderInstance_itShouldReturnBuilderCorrectly() {
        Shipment.Builder builder = Shipment.Builder.newInstance("s");
        assertNotNull(builder);
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenNeitherPickupLocationIdNorPickupCoord_itThrowsException() {
        @SuppressWarnings("unused")
        Shipment s = Shipment.Builder.newInstance("s").setDeliveryLocation(TestUtils.loc("delLoc")).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenNeitherDeliveryLocationIdNorDeliveryCoord_itThrowsException() {
        @SuppressWarnings("unused")
        Shipment s = Shipment.Builder.newInstance("s").setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build()).build();
    }

    @Test
    public void whenPickupLocationIdIsSet_itShouldBeDoneCorrectly() {
        Shipment s = Shipment.Builder.newInstance("s").setDeliveryLocation(TestUtils.loc("delLoc")).setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build()).build();
        assertEquals("pickLoc", s.getPickupLocation().getId());
        assertEquals("pickLoc", s.getPickupLocation().getId());
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenPickupLocationIsNull_itThrowsException() {
        @SuppressWarnings("unused")
        Shipment.Builder builder = Shipment.Builder.newInstance("s").setPickupLocation(Location.Builder.newInstance().setId(null).build());
    }

    @Test
    public void whenPickupCoordIsSet_itShouldBeDoneCorrectly() {
        Shipment s = Shipment.Builder.newInstance("s")
            .setDeliveryLocation(TestUtils.loc("delLoc")).setPickupLocation(Location.Builder.newInstance().setId("pickLoc").setCoordinate(Coordinate.newInstance(1, 2)).build()).build();
        assertEquals(1.0, s.getPickupLocation().getCoordinate().getX(), 0.01);
        assertEquals(2.0, s.getPickupLocation().getCoordinate().getY(), 0.01);
        assertEquals(1.0, s.getPickupLocation().getCoordinate().getX(), 0.01);
        assertEquals(2.0, s.getPickupLocation().getCoordinate().getY(), 0.01);
    }


    @Test
    public void whenDeliveryLocationIdIsSet_itShouldBeDoneCorrectly() {
        Shipment s = Shipment.Builder.newInstance("s")
            .setDeliveryLocation(TestUtils.loc("delLoc")).setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build()).build();
        assertEquals("delLoc", s.getDeliveryLocation().getId());
        assertEquals("delLoc", s.getDeliveryLocation().getId());
    }


    @Test
    public void whenDeliveryCoordIsSet_itShouldBeDoneCorrectly() {
        Shipment s = Shipment.Builder.newInstance("s").setDeliveryLocation(TestUtils.loc("delLoc", Coordinate.newInstance(1, 2)))
            .setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build())
            .build();
        assertEquals(1.0, s.getDeliveryLocation().getCoordinate().getX(), 0.01);
        assertEquals(2.0, s.getDeliveryLocation().getCoordinate().getY(), 0.01);
        assertEquals(1.0, s.getDeliveryLocation().getCoordinate().getX(), 0.01);
        assertEquals(2.0, s.getDeliveryLocation().getCoordinate().getY(), 0.01);
    }

    @Test
    public void whenPickupServiceTimeIsNotSet_itShouldBeZero() {
        Shipment s = Shipment.Builder.newInstance("s")
            .setDeliveryLocation(TestUtils.loc("delLoc")).setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build()).build();
        assertEquals(0.0, s.getPickupServiceTime(), 0.01);
    }

    @Test
    public void whenDeliveryServiceTimeIsNotSet_itShouldBeZero() {
        Shipment s = Shipment.Builder.newInstance("s")
            .setDeliveryLocation(TestUtils.loc("delLoc")).setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build()).build();
        assertEquals(0.0, s.getDeliveryServiceTime(), 0.01);
    }

    @Test
    public void whenPickupServiceTimeIsSet_itShouldBeDoneCorrectly() {
        Shipment s = Shipment.Builder.newInstance("s")
            .setPickupServiceTime(2.0)
            .setDeliveryLocation(TestUtils.loc("delLoc")).setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build()).build();
        assertEquals(2.0, s.getPickupServiceTime(), 0.01);
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenPickupServiceIsSmallerThanZero_itShouldThrowException() {
        @SuppressWarnings("unused")
        Shipment s = Shipment.Builder.newInstance("s").setPickupServiceTime(-2.0)
            .setDeliveryLocation(TestUtils.loc("delLoc")).setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build()).build();
    }

    @Test
    public void whenDeliveryServiceTimeIsSet_itShouldBeDoneCorrectly() {
        Shipment s = Shipment.Builder.newInstance("s").setDeliveryServiceTime(2.0)
            .setDeliveryLocation(TestUtils.loc("delLoc")).setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build()).build();
        assertEquals(2.0, s.getDeliveryServiceTime(), 0.01);
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenDeliveryServiceIsSmallerThanZero_itShouldThrowException() {
        @SuppressWarnings("unused")
        Shipment s = Shipment.Builder.newInstance("s").setDeliveryServiceTime(-2.0).setDeliveryLocation(TestUtils.loc("delLoc")).setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build()).build();
    }

    @Test
    public void whenPickupTimeWindowIsNotSet_itShouldBeTheDefaultOne() {
        Shipment s = Shipment.Builder.newInstance("s").setDeliveryLocation(TestUtils.loc("delLoc")).setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build()).build();
        assertEquals(0.0, s.getPickupTimeWindow().getStart(), 0.01);
        assertEquals(Double.MAX_VALUE, s.getPickupTimeWindow().getEnd(), 0.01);
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenPickupTimeWindowIsNull_itShouldThrowException() {
        @SuppressWarnings("unused")
        Shipment s = Shipment.Builder.newInstance("s").setPickupTimeWindow(null).setDeliveryLocation(TestUtils.loc("delLoc")).setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build()).build();
    }

    @Test
    public void whenPickupTimeWindowIsSet_itShouldBeDoneCorrectly() {
        Shipment s = Shipment.Builder.newInstance("s").setPickupTimeWindow(TimeWindow.newInstance(1, 2))
            .setDeliveryLocation(TestUtils.loc("delLoc")).setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build()).build();
        assertEquals(1.0, s.getPickupTimeWindow().getStart(), 0.01);
        assertEquals(2.0, s.getPickupTimeWindow().getEnd(), 0.01);
    }

    @Test
    public void whenDeliveryTimeWindowIsNotSet_itShouldBeTheDefaultOne() {
        Shipment s = Shipment.Builder.newInstance("s").setDeliveryLocation(TestUtils.loc("delLoc")).setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build()).build();
        assertEquals(0.0, s.getDeliveryTimeWindow().getStart(), 0.01);
        assertEquals(Double.MAX_VALUE, s.getDeliveryTimeWindow().getEnd(), 0.01);
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenDeliveryTimeWindowIsNull_itShouldThrowException() {
        @SuppressWarnings("unused")
        Shipment s = Shipment.Builder.newInstance("s").setDeliveryTimeWindow(null).setDeliveryLocation(TestUtils.loc("delLoc")).setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build()).build();
    }

    @Test
    public void whenDeliveryTimeWindowIsSet_itShouldBeDoneCorrectly() {
        Shipment s = Shipment.Builder.newInstance("s").setDeliveryTimeWindow(TimeWindow.newInstance(1, 2))
            .setDeliveryLocation(TestUtils.loc("delLoc")).setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build()).build();
        assertEquals(1.0, s.getDeliveryTimeWindow().getStart(), 0.01);
        assertEquals(2.0, s.getDeliveryTimeWindow().getEnd(), 0.01);
    }

    @Test
    public void whenUsingAddDeliveryTimeWindow_itShouldBeDoneCorrectly() {
        Shipment s = Shipment.Builder.newInstance("s").addDeliveryTimeWindow(TimeWindow.newInstance(1, 2))
            .setDeliveryLocation(TestUtils.loc("delLoc")).setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build()).build();
        assertEquals(1.0, s.getDeliveryTimeWindow().getStart(), 0.01);
        assertEquals(2.0, s.getDeliveryTimeWindow().getEnd(), 0.01);
    }

    @Test
    public void whenUsingAddDeliveryTimeWindow2_itShouldBeDoneCorrectly() {
        Shipment s = Shipment.Builder.newInstance("s").addDeliveryTimeWindow(1, 2)
            .setDeliveryLocation(TestUtils.loc("delLoc")).setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build()).build();
        assertEquals(1.0, s.getDeliveryTimeWindow().getStart(), 0.01);
        assertEquals(2.0, s.getDeliveryTimeWindow().getEnd(), 0.01);
    }

    @Test
    public void whenAddingMultipleDeliveryTimeWindows_itShouldBeDoneCorrectly() {
        TimeWindow tw1 = TimeWindow.newInstance(1,2);
        TimeWindow tw2 = TimeWindow.newInstance(4,5);
        Shipment s = Shipment.Builder.newInstance("s").addDeliveryTimeWindow(tw1).addDeliveryTimeWindow(tw2)
            .setDeliveryLocation(TestUtils.loc("delLoc")).setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build()).build();
        assertEquals(s.getDeliveryTimeWindows().size(),2);
        assertThat(s.getDeliveryTimeWindows(),hasItem(is(tw1)));
        assertThat(s.getDeliveryTimeWindows(),hasItem(is(tw2)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenAddingMultipleOverlappingDeliveryTimeWindows_itShouldThrowException() {
        Shipment s = Shipment.Builder.newInstance("s").addDeliveryTimeWindow(1, 3).addDeliveryTimeWindow(2,5)
            .setDeliveryLocation(TestUtils.loc("delLoc")).setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build()).build();
        assertEquals(1.0, s.getDeliveryTimeWindow().getStart(), 0.01);
        assertEquals(2.0, s.getDeliveryTimeWindow().getEnd(), 0.01);
    }



    @Test
    public void whenUsingAddPickupTimeWindow_itShouldBeDoneCorrectly() {
        Shipment s = Shipment.Builder.newInstance("s").addPickupTimeWindow(TimeWindow.newInstance(1, 2))
            .setDeliveryLocation(TestUtils.loc("delLoc")).setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build()).build();
        assertEquals(1.0, s.getPickupTimeWindow().getStart(), 0.01);
        assertEquals(2.0, s.getPickupTimeWindow().getEnd(), 0.01);
    }

    @Test
    public void whenUsingAddPickupTimeWindow2_itShouldBeDoneCorrectly() {
        Shipment s = Shipment.Builder.newInstance("s").addPickupTimeWindow(1, 2)
            .setDeliveryLocation(TestUtils.loc("delLoc")).setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build()).build();
        assertEquals(1.0, s.getPickupTimeWindow().getStart(), 0.01);
        assertEquals(2.0, s.getPickupTimeWindow().getEnd(), 0.01);
    }

    @Test
    public void whenAddingMultiplePickupTimeWindows_itShouldBeDoneCorrectly() {
        TimeWindow tw1 = TimeWindow.newInstance(1,2);
        TimeWindow tw2 = TimeWindow.newInstance(4,5);
        Shipment s = Shipment.Builder.newInstance("s").addPickupTimeWindow(tw1).addPickupTimeWindow(tw2)
            .setDeliveryLocation(TestUtils.loc("delLoc")).setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build()).build();
        assertEquals(s.getPickupTimeWindows().size(),2);
        assertThat(s.getPickupTimeWindows(), hasItem(is(tw1)));
        assertThat(s.getPickupTimeWindows(), hasItem(is(tw2)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenAddingMultipleOverlappingPickupTimeWindows_itShouldThrowException() {
        Shipment s = Shipment.Builder.newInstance("s").addPickupTimeWindow(1, 3).addPickupTimeWindow(2,5)
            .setDeliveryLocation(TestUtils.loc("delLoc")).setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build()).build();
        assertEquals(1.0, s.getPickupTimeWindow().getStart(), 0.01);
        assertEquals(2.0, s.getPickupTimeWindow().getEnd(), 0.01);
    }



    @Test(expected = IllegalArgumentException.class)
    public void whenShipmentHasNegativeCapacityVal_throwIllegalStateExpception() {
        @SuppressWarnings("unused")
        Shipment one = Shipment.Builder.newInstance("s").setPickupLocation(Location.Builder.newInstance().setId("foo").build())
            .setDeliveryLocation(TestUtils.loc("foofoo"))
            .addSizeDimension(0, -2)
            .build();
    }

    @Test
    public void whenAddingTwoCapDimension_nuOfDimsShouldBeTwo() {
        Shipment one = Shipment.Builder.newInstance("s").setPickupLocation(Location.Builder.newInstance().setId("foo").build())
            .setDeliveryLocation(TestUtils.loc("foofoo"))
            .addSizeDimension(0, 2)
            .addSizeDimension(1, 4)
            .build();
        assertEquals(2, one.getSize().getNuOfDimensions());
    }

    @Test
    public void whenShipmentIsBuiltWithoutSpecifyingCapacity_itShouldHvCapWithOneDimAndDimValOfZero() {
        Shipment one = Shipment.Builder.newInstance("s")
            .setPickupLocation(Location.Builder.newInstance().setId("foo").setCoordinate(Coordinate.newInstance(0, 0)).build())
            .setDeliveryLocation(TestUtils.loc("foofoo")).build();
        assertEquals(1, one.getSize().getNuOfDimensions());
        assertEquals(0, one.getSize().get(0));
    }

    @Test
    public void whenShipmentIsBuiltWithConstructorWhereSizeIsSpecified_capacityShouldBeSetCorrectly() {
        Shipment one = Shipment.Builder.newInstance("s").addSizeDimension(0, 1)
            .setPickupLocation(Location.Builder.newInstance().setId("foo").setCoordinate(Coordinate.newInstance(0, 0)).build())
            .setDeliveryLocation(TestUtils.loc("foofoo")).build();
        assertEquals(1, one.getSize().getNuOfDimensions());
        assertEquals(1, one.getSize().get(0));
    }

    @Test
    public void whenAddingSkills_theyShouldBeAddedCorrectly() {
        Shipment s = Shipment.Builder.newInstance("s").setPickupLocation(Location.Builder.newInstance().setId("loc").build())
            .setDeliveryLocation(TestUtils.loc("delLoc"))
            .addRequiredSkill("drill").addRequiredSkill("screwdriver").build();
        assertTrue(s.getRequiredSkills().containsSkill("drill"));
        assertTrue(s.getRequiredSkills().containsSkill("drill"));
        assertTrue(s.getRequiredSkills().containsSkill("ScrewDriver"));
    }

    @Test
    public void whenAddingSkillsCaseSens_theyShouldBeAddedCorrectly() {
        Shipment s = Shipment.Builder.newInstance("s")
            .setPickupLocation(Location.Builder.newInstance().setId("pick").build())
            .setDeliveryLocation(TestUtils.loc("del"))
            .addRequiredSkill("DriLl").addRequiredSkill("screwDriver").build();
        assertTrue(s.getRequiredSkills().containsSkill("drill"));
        assertTrue(s.getRequiredSkills().containsSkill("drilL"));
    }

    @Test
    public void whenAddingSkillsCaseSensV2_theyShouldBeAddedCorrectly() {
        Shipment s = Shipment.Builder.newInstance("s").setPickupLocation(Location.Builder.newInstance().setId("loc").build())
            .setDeliveryLocation(TestUtils.loc("del"))
            .addRequiredSkill("screwDriver").build();
        assertFalse(s.getRequiredSkills().containsSkill("drill"));
        assertFalse(s.getRequiredSkills().containsSkill("drilL"));
    }

    @Test
    public void nameShouldBeAssigned() {
        Shipment s = Shipment.Builder.newInstance("s").setPickupLocation(Location.Builder.newInstance().setId("loc").build())
            .setDeliveryLocation(TestUtils.loc("del"))
            .setName("name").build();
        assertEquals("name", s.getName());
    }

    @Test
    public void whenSettingLocation_itShouldWork() {
        Shipment s = Shipment.Builder.newInstance("s").setPickupLocation(Location.Builder.newInstance().setId("loc").build())
            .setDeliveryLocation(Location.Builder.newInstance().setId("del").build()).build();
        assertEquals("loc", s.getPickupLocation().getId());
        assertEquals("loc", s.getPickupLocation().getId());
        assertEquals("del", s.getDeliveryLocation().getId());
        assertEquals("del", s.getDeliveryLocation().getId());
    }

    @Test
    public void whenSettingPriorities_itShouldBeSetCorrectly(){
        Shipment s = Shipment.Builder.newInstance("s").setPickupLocation(Location.newInstance("loc"))
            .setDeliveryLocation(Location.newInstance("loc"))
            .setPriority(1).build();
        Assert.assertEquals(1, s.getPriority());
    }

    @Test
    public void whenSettingPriorities_itShouldBeSetCorrectly2(){
        Shipment s = Shipment.Builder.newInstance("s").setPickupLocation(Location.newInstance("loc"))
            .setDeliveryLocation(Location.newInstance("loc"))
            .setPriority(3).build();
        Assert.assertEquals(3, s.getPriority());
    }

    @Test
    public void whenSettingPriorities_itShouldBeSetCorrectly3() {
        Shipment s = Shipment.Builder.newInstance("s").setPickupLocation(Location.newInstance("loc"))
            .setDeliveryLocation(Location.newInstance("loc"))
            .setPriority(10).build();
        Assert.assertEquals(10, s.getPriority());
    }

    @Test
    public void whenNotSettingPriorities_defaultShouldBe2(){
        Shipment s = Shipment.Builder.newInstance("s").setPickupLocation(Location.newInstance("loc"))
            .setDeliveryLocation(Location.newInstance("loc"))
            .build();
        Assert.assertEquals(2, s.getPriority());
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenSettingIncorrectPriorities_itShouldThrowException(){
        Shipment s = Shipment.Builder.newInstance("s").setPickupLocation(Location.newInstance("loc"))
            .setDeliveryLocation(Location.newInstance("loc"))
            .setPriority(30).build();

    }

    @Test(expected = IllegalArgumentException.class)
    public void whenSettingIncorrectPriorities_itShouldThrowException2(){
        Shipment s = Shipment.Builder.newInstance("s").setPickupLocation(Location.newInstance("loc"))
            .setDeliveryLocation(Location.newInstance("loc"))
            .setPriority(0).build();

    }

    @Test
    public void whenSettingUserData_itIsAssociatedWithTheJob() {
        Shipment one = Shipment.Builder.newInstance("s").setPickupLocation(Location.newInstance("loc"))
            .setDeliveryLocation(Location.newInstance("loc")).setUserData(new HashMap<String, Object>()).build();
        Shipment two = Shipment.Builder.newInstance("s2").setPickupLocation(Location.newInstance("loc"))
            .setDeliveryLocation(Location.newInstance("loc")).setUserData(42).build();
        Shipment three = Shipment.Builder.newInstance("s3").setPickupLocation(Location.newInstance("loc"))
            .setDeliveryLocation(Location.newInstance("loc")).build();

        assertTrue(one.getUserData() instanceof Map);
        assertEquals(42, two.getUserData());
        assertNull(three.getUserData());
    }
    @Test
    public void whenAddingMaxTimeInVehicle_itShouldBeSet(){
        Shipment s = Shipment.Builder.newInstance("s").setPickupLocation(Location.newInstance("loc")).setDeliveryLocation(Location.newInstance("loc"))
            .setMaxTimeInVehicle(10)
            .build();
        Assert.assertEquals(10, s.getMaxTimeInVehicle(),0.001);
    }

    @Test
    public void whenNotAddingMaxTimeInVehicle_itShouldBeDefault(){
        Shipment s = Shipment.Builder.newInstance("s").setPickupLocation(Location.newInstance("loc")).setDeliveryLocation(Location.newInstance("loc"))
            .build();
        Assert.assertEquals(Double.MAX_VALUE, s.getMaxTimeInVehicle(),0.001);
    }

}
