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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Shipment Test")
class ShipmentTest {

    @Test
    @DisplayName("When Two Shipments Have The Same Id _ they References Should Be Un Equal")
    void whenTwoShipmentsHaveTheSameId_theyReferencesShouldBeUnEqual() {
        Shipment one = Shipment.Builder.newInstance("s").addSizeDimension(0, 10).setPickupLocation(Location.Builder.newInstance().setId("foo").build()).setDeliveryLocation(TestUtils.loc("foofoo")).setPickupServiceTime(10).setDeliveryServiceTime(20).build();
        Shipment two = Shipment.Builder.newInstance("s").addSizeDimension(0, 10).setPickupLocation(Location.Builder.newInstance().setId("foo").build()).setDeliveryLocation(TestUtils.loc("foofoo")).setPickupServiceTime(10).setDeliveryServiceTime(20).build();
        assertTrue(one != two);
    }

    @Test
    @DisplayName("When Two Shipments Have The Same Id _ they Should Be Equal")
    void whenTwoShipmentsHaveTheSameId_theyShouldBeEqual() {
        Shipment one = Shipment.Builder.newInstance("s").addSizeDimension(0, 10).setPickupLocation(Location.Builder.newInstance().setId("foo").build()).setDeliveryLocation(TestUtils.loc("foofoo")).setPickupServiceTime(10).setDeliveryServiceTime(20).build();
        Shipment two = Shipment.Builder.newInstance("s").addSizeDimension(0, 10).setPickupLocation(Location.Builder.newInstance().setId("foo").build()).setDeliveryLocation(TestUtils.loc("foofoo")).setPickupServiceTime(10).setDeliveryServiceTime(20).build();
        assertTrue(one.equals(two));
    }

    @Test
    @DisplayName("When Shipment Is Instantiated With A Size Of 10 _ the Size Should Be 10")
    void whenShipmentIsInstantiatedWithASizeOf10_theSizeShouldBe10() {
        Shipment one = Shipment.Builder.newInstance("s").addSizeDimension(0, 10).setPickupLocation(Location.Builder.newInstance().setId("foo").build()).setDeliveryLocation(TestUtils.loc("foofoo")).setPickupServiceTime(10).setDeliveryServiceTime(20).build();
        assertEquals(10, one.getSize().get(0));
    }

    @Test
    @DisplayName("When Shipment Is Built With Negative Demand _ it Should Throw Exception")
    void whenShipmentIsBuiltWithNegativeDemand_itShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            @SuppressWarnings("unused")
            Shipment one = Shipment.Builder.newInstance("s").addSizeDimension(0, -10).setPickupLocation(Location.Builder.newInstance().setId("foo").build()).setDeliveryLocation(TestUtils.loc("foofoo")).build();
        });
    }

    @Test
    @DisplayName("When Shipment Is Built With Negative Demand _ it Should Throw Exception _ v 2")
    void whenShipmentIsBuiltWithNegativeDemand_itShouldThrowException_v2() {
        assertThrows(IllegalArgumentException.class, () -> {
            @SuppressWarnings("unused")
            Shipment one = Shipment.Builder.newInstance("s").addSizeDimension(0, -10).setPickupLocation(Location.Builder.newInstance().setId("foo").build()).setDeliveryLocation(TestUtils.loc("foofoo")).build();
        });
    }

    @Test
    @DisplayName("When Id Is Null _ it Should Throw Exception")
    void whenIdIsNull_itShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            @SuppressWarnings("unused")
            Shipment one = Shipment.Builder.newInstance(null).addSizeDimension(0, 10).setPickupLocation(Location.Builder.newInstance().setId("foo").build()).setDeliveryLocation(TestUtils.loc("foofoo")).build();
        });
    }

    @Test
    @DisplayName("When Calling For A New Builder Instance _ it Should Return Builder Correctly")
    void whenCallingForANewBuilderInstance_itShouldReturnBuilderCorrectly() {
        Shipment.Builder builder = Shipment.Builder.newInstance("s");
        assertNotNull(builder);
    }

    @Test
    @DisplayName("When Neither Pickup Location Id Nor Pickup Coord _ it Throws Exception")
    void whenNeitherPickupLocationIdNorPickupCoord_itThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            @SuppressWarnings("unused")
            Shipment s = Shipment.Builder.newInstance("s").setDeliveryLocation(TestUtils.loc("delLoc")).build();
        });
    }

    @Test
    @DisplayName("When Neither Delivery Location Id Nor Delivery Coord _ it Throws Exception")
    void whenNeitherDeliveryLocationIdNorDeliveryCoord_itThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            @SuppressWarnings("unused")
            Shipment s = Shipment.Builder.newInstance("s").setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build()).build();
        });
    }

    @Test
    @DisplayName("When Pickup Location Id Is Set _ it Should Be Done Correctly")
    void whenPickupLocationIdIsSet_itShouldBeDoneCorrectly() {
        Shipment s = Shipment.Builder.newInstance("s").setDeliveryLocation(TestUtils.loc("delLoc")).setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build()).build();
        assertEquals(s.getPickupLocation().getId(), "pickLoc");
        assertEquals(s.getPickupLocation().getId(), "pickLoc");
    }

    @Test
    @DisplayName("When Pickup Location Is Null _ it Throws Exception")
    void whenPickupLocationIsNull_itThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            @SuppressWarnings("unused")
            Shipment.Builder builder = Shipment.Builder.newInstance("s").setPickupLocation(Location.Builder.newInstance().setId(null).build());
        });
    }

    @Test
    @DisplayName("When Pickup Coord Is Set _ it Should Be Done Correctly")
    void whenPickupCoordIsSet_itShouldBeDoneCorrectly() {
        Shipment s = Shipment.Builder.newInstance("s").setDeliveryLocation(TestUtils.loc("delLoc")).setPickupLocation(Location.Builder.newInstance().setId("pickLoc").setCoordinate(Coordinate.newInstance(1, 2)).build()).build();
        assertEquals(1.0, s.getPickupLocation().getCoordinate().getX(), 0.01);
        assertEquals(2.0, s.getPickupLocation().getCoordinate().getY(), 0.01);
        assertEquals(1.0, s.getPickupLocation().getCoordinate().getX(), 0.01);
        assertEquals(2.0, s.getPickupLocation().getCoordinate().getY(), 0.01);
    }

    @Test
    @DisplayName("When Delivery Location Id Is Set _ it Should Be Done Correctly")
    void whenDeliveryLocationIdIsSet_itShouldBeDoneCorrectly() {
        Shipment s = Shipment.Builder.newInstance("s").setDeliveryLocation(TestUtils.loc("delLoc")).setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build()).build();
        assertEquals(s.getDeliveryLocation().getId(), "delLoc");
        assertEquals(s.getDeliveryLocation().getId(), "delLoc");
    }

    @Test
    @DisplayName("When Delivery Coord Is Set _ it Should Be Done Correctly")
    void whenDeliveryCoordIsSet_itShouldBeDoneCorrectly() {
        Shipment s = Shipment.Builder.newInstance("s").setDeliveryLocation(TestUtils.loc("delLoc", Coordinate.newInstance(1, 2))).setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build()).build();
        assertEquals(1.0, s.getDeliveryLocation().getCoordinate().getX(), 0.01);
        assertEquals(2.0, s.getDeliveryLocation().getCoordinate().getY(), 0.01);
        assertEquals(1.0, s.getDeliveryLocation().getCoordinate().getX(), 0.01);
        assertEquals(2.0, s.getDeliveryLocation().getCoordinate().getY(), 0.01);
    }

    @Test
    @DisplayName("When Pickup Service Time Is Not Set _ it Should Be Zero")
    void whenPickupServiceTimeIsNotSet_itShouldBeZero() {
        Shipment s = Shipment.Builder.newInstance("s").setDeliveryLocation(TestUtils.loc("delLoc")).setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build()).build();
        assertEquals(0.0, s.getPickupServiceTime(), 0.01);
    }

    @Test
    @DisplayName("When Delivery Service Time Is Not Set _ it Should Be Zero")
    void whenDeliveryServiceTimeIsNotSet_itShouldBeZero() {
        Shipment s = Shipment.Builder.newInstance("s").setDeliveryLocation(TestUtils.loc("delLoc")).setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build()).build();
        assertEquals(0.0, s.getDeliveryServiceTime(), 0.01);
    }

    @Test
    @DisplayName("When Pickup Service Time Is Set _ it Should Be Done Correctly")
    void whenPickupServiceTimeIsSet_itShouldBeDoneCorrectly() {
        Shipment s = Shipment.Builder.newInstance("s").setPickupServiceTime(2.0).setDeliveryLocation(TestUtils.loc("delLoc")).setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build()).build();
        assertEquals(2.0, s.getPickupServiceTime(), 0.01);
    }

    @Test
    @DisplayName("When Pickup Service Is Smaller Than Zero _ it Should Throw Exception")
    void whenPickupServiceIsSmallerThanZero_itShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            @SuppressWarnings("unused")
            Shipment s = Shipment.Builder.newInstance("s").setPickupServiceTime(-2.0).setDeliveryLocation(TestUtils.loc("delLoc")).setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build()).build();
        });
    }

    @Test
    @DisplayName("When Delivery Service Time Is Set _ it Should Be Done Correctly")
    void whenDeliveryServiceTimeIsSet_itShouldBeDoneCorrectly() {
        Shipment s = Shipment.Builder.newInstance("s").setDeliveryServiceTime(2.0).setDeliveryLocation(TestUtils.loc("delLoc")).setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build()).build();
        assertEquals(2.0, s.getDeliveryServiceTime(), 0.01);
    }

    @Test
    @DisplayName("When Delivery Service Is Smaller Than Zero _ it Should Throw Exception")
    void whenDeliveryServiceIsSmallerThanZero_itShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            @SuppressWarnings("unused")
            Shipment s = Shipment.Builder.newInstance("s").setDeliveryServiceTime(-2.0).setDeliveryLocation(TestUtils.loc("delLoc")).setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build()).build();
        });
    }

    @Test
    @DisplayName("When Pickup Time Window Is Not Set _ it Should Be The Default One")
    void whenPickupTimeWindowIsNotSet_itShouldBeTheDefaultOne() {
        Shipment s = Shipment.Builder.newInstance("s").setDeliveryLocation(TestUtils.loc("delLoc")).setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build()).build();
        assertEquals(0.0, s.getPickupTimeWindow().getStart(), 0.01);
        assertEquals(Double.MAX_VALUE, s.getPickupTimeWindow().getEnd(), 0.01);
    }

    @Test
    @DisplayName("When Pickup Time Window Is Null _ it Should Throw Exception")
    void whenPickupTimeWindowIsNull_itShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            @SuppressWarnings("unused")
            Shipment s = Shipment.Builder.newInstance("s").setPickupTimeWindow(null).setDeliveryLocation(TestUtils.loc("delLoc")).setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build()).build();
        });
    }

    @Test
    @DisplayName("When Pickup Time Window Is Set _ it Should Be Done Correctly")
    void whenPickupTimeWindowIsSet_itShouldBeDoneCorrectly() {
        Shipment s = Shipment.Builder.newInstance("s").setPickupTimeWindow(TimeWindow.newInstance(1, 2)).setDeliveryLocation(TestUtils.loc("delLoc")).setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build()).build();
        assertEquals(1.0, s.getPickupTimeWindow().getStart(), 0.01);
        assertEquals(2.0, s.getPickupTimeWindow().getEnd(), 0.01);
    }

    @Test
    @DisplayName("When Delivery Time Window Is Not Set _ it Should Be The Default One")
    void whenDeliveryTimeWindowIsNotSet_itShouldBeTheDefaultOne() {
        Shipment s = Shipment.Builder.newInstance("s").setDeliveryLocation(TestUtils.loc("delLoc")).setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build()).build();
        assertEquals(0.0, s.getDeliveryTimeWindow().getStart(), 0.01);
        assertEquals(Double.MAX_VALUE, s.getDeliveryTimeWindow().getEnd(), 0.01);
    }

    @Test
    @DisplayName("When Delivery Time Window Is Null _ it Should Throw Exception")
    void whenDeliveryTimeWindowIsNull_itShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            @SuppressWarnings("unused")
            Shipment s = Shipment.Builder.newInstance("s").setDeliveryTimeWindow(null).setDeliveryLocation(TestUtils.loc("delLoc")).setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build()).build();
        });
    }

    @Test
    @DisplayName("When Delivery Time Window Is Set _ it Should Be Done Correctly")
    void whenDeliveryTimeWindowIsSet_itShouldBeDoneCorrectly() {
        Shipment s = Shipment.Builder.newInstance("s").setDeliveryTimeWindow(TimeWindow.newInstance(1, 2)).setDeliveryLocation(TestUtils.loc("delLoc")).setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build()).build();
        assertEquals(1.0, s.getDeliveryTimeWindow().getStart(), 0.01);
        assertEquals(2.0, s.getDeliveryTimeWindow().getEnd(), 0.01);
    }

    @Test
    @DisplayName("When Using Add Delivery Time Window _ it Should Be Done Correctly")
    void whenUsingAddDeliveryTimeWindow_itShouldBeDoneCorrectly() {
        Shipment s = Shipment.Builder.newInstance("s").addDeliveryTimeWindow(TimeWindow.newInstance(1, 2)).setDeliveryLocation(TestUtils.loc("delLoc")).setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build()).build();
        assertEquals(1.0, s.getDeliveryTimeWindow().getStart(), 0.01);
        assertEquals(2.0, s.getDeliveryTimeWindow().getEnd(), 0.01);
    }

    @Test
    @DisplayName("When Using Add Delivery Time Window 2 _ it Should Be Done Correctly")
    void whenUsingAddDeliveryTimeWindow2_itShouldBeDoneCorrectly() {
        Shipment s = Shipment.Builder.newInstance("s").addDeliveryTimeWindow(1, 2).setDeliveryLocation(TestUtils.loc("delLoc")).setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build()).build();
        assertEquals(1.0, s.getDeliveryTimeWindow().getStart(), 0.01);
        assertEquals(2.0, s.getDeliveryTimeWindow().getEnd(), 0.01);
    }

    @Test
    @DisplayName("When Adding Multiple Delivery Time Windows _ it Should Be Done Correctly")
    void whenAddingMultipleDeliveryTimeWindows_itShouldBeDoneCorrectly() {
        TimeWindow tw1 = TimeWindow.newInstance(1, 2);
        TimeWindow tw2 = TimeWindow.newInstance(4, 5);
        Shipment s = Shipment.Builder.newInstance("s").addDeliveryTimeWindow(tw1).addDeliveryTimeWindow(tw2).setDeliveryLocation(TestUtils.loc("delLoc")).setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build()).build();
        assertEquals(s.getDeliveryTimeWindows().size(), 2);
        assertThat(s.getDeliveryTimeWindows(), hasItem(is(tw1)));
        assertThat(s.getDeliveryTimeWindows(), hasItem(is(tw2)));
    }

    @Test
    @DisplayName("When Adding Multiple Overlapping Delivery Time Windows _ it Should Throw Exception")
    void whenAddingMultipleOverlappingDeliveryTimeWindows_itShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            Shipment s = Shipment.Builder.newInstance("s").addDeliveryTimeWindow(1, 3).addDeliveryTimeWindow(2, 5).setDeliveryLocation(TestUtils.loc("delLoc")).setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build()).build();
            assertEquals(1.0, s.getDeliveryTimeWindow().getStart(), 0.01);
            assertEquals(2.0, s.getDeliveryTimeWindow().getEnd(), 0.01);
        });
    }

    @Test
    @DisplayName("When Using Add Pickup Time Window _ it Should Be Done Correctly")
    void whenUsingAddPickupTimeWindow_itShouldBeDoneCorrectly() {
        Shipment s = Shipment.Builder.newInstance("s").addPickupTimeWindow(TimeWindow.newInstance(1, 2)).setDeliveryLocation(TestUtils.loc("delLoc")).setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build()).build();
        assertEquals(1.0, s.getPickupTimeWindow().getStart(), 0.01);
        assertEquals(2.0, s.getPickupTimeWindow().getEnd(), 0.01);
    }

    @Test
    @DisplayName("When Using Add Pickup Time Window 2 _ it Should Be Done Correctly")
    void whenUsingAddPickupTimeWindow2_itShouldBeDoneCorrectly() {
        Shipment s = Shipment.Builder.newInstance("s").addPickupTimeWindow(1, 2).setDeliveryLocation(TestUtils.loc("delLoc")).setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build()).build();
        assertEquals(1.0, s.getPickupTimeWindow().getStart(), 0.01);
        assertEquals(2.0, s.getPickupTimeWindow().getEnd(), 0.01);
    }

    @Test
    @DisplayName("When Adding Multiple Pickup Time Windows _ it Should Be Done Correctly")
    void whenAddingMultiplePickupTimeWindows_itShouldBeDoneCorrectly() {
        TimeWindow tw1 = TimeWindow.newInstance(1, 2);
        TimeWindow tw2 = TimeWindow.newInstance(4, 5);
        Shipment s = Shipment.Builder.newInstance("s").addPickupTimeWindow(tw1).addPickupTimeWindow(tw2).setDeliveryLocation(TestUtils.loc("delLoc")).setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build()).build();
        assertEquals(s.getPickupTimeWindows().size(), 2);
        assertThat(s.getPickupTimeWindows(), hasItem(is(tw1)));
        assertThat(s.getPickupTimeWindows(), hasItem(is(tw2)));
    }

    @Test
    @DisplayName("When Adding Multiple Overlapping Pickup Time Windows _ it Should Throw Exception")
    void whenAddingMultipleOverlappingPickupTimeWindows_itShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            Shipment s = Shipment.Builder.newInstance("s").addPickupTimeWindow(1, 3).addPickupTimeWindow(2, 5).setDeliveryLocation(TestUtils.loc("delLoc")).setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build()).build();
            assertEquals(1.0, s.getPickupTimeWindow().getStart(), 0.01);
            assertEquals(2.0, s.getPickupTimeWindow().getEnd(), 0.01);
        });
    }

    @Test
    @DisplayName("When Shipment Has Negative Capacity Val _ throw Illegal State Expception")
    void whenShipmentHasNegativeCapacityVal_throwIllegalStateExpception() {
        assertThrows(IllegalArgumentException.class, () -> {
            @SuppressWarnings("unused")
            Shipment one = Shipment.Builder.newInstance("s").setPickupLocation(Location.Builder.newInstance().setId("foo").build()).setDeliveryLocation(TestUtils.loc("foofoo")).addSizeDimension(0, -2).build();
        });
    }

    @Test
    @DisplayName("When Adding Two Cap Dimension _ nu Of Dims Should Be Two")
    void whenAddingTwoCapDimension_nuOfDimsShouldBeTwo() {
        Shipment one = Shipment.Builder.newInstance("s").setPickupLocation(Location.Builder.newInstance().setId("foo").build()).setDeliveryLocation(TestUtils.loc("foofoo")).addSizeDimension(0, 2).addSizeDimension(1, 4).build();
        assertEquals(2, one.getSize().getNuOfDimensions());
    }

    @Test
    @DisplayName("When Shipment Is Built Without Specifying Capacity _ it Should Hv Cap With One Dim And Dim Val Of Zero")
    void whenShipmentIsBuiltWithoutSpecifyingCapacity_itShouldHvCapWithOneDimAndDimValOfZero() {
        Shipment one = Shipment.Builder.newInstance("s").setPickupLocation(Location.Builder.newInstance().setId("foo").setCoordinate(Coordinate.newInstance(0, 0)).build()).setDeliveryLocation(TestUtils.loc("foofoo")).build();
        assertEquals(1, one.getSize().getNuOfDimensions());
        assertEquals(0, one.getSize().get(0));
    }

    @Test
    @DisplayName("When Shipment Is Built With Constructor Where Size Is Specified _ capacity Should Be Set Correctly")
    void whenShipmentIsBuiltWithConstructorWhereSizeIsSpecified_capacityShouldBeSetCorrectly() {
        Shipment one = Shipment.Builder.newInstance("s").addSizeDimension(0, 1).setPickupLocation(Location.Builder.newInstance().setId("foo").setCoordinate(Coordinate.newInstance(0, 0)).build()).setDeliveryLocation(TestUtils.loc("foofoo")).build();
        assertEquals(1, one.getSize().getNuOfDimensions());
        assertEquals(1, one.getSize().get(0));
    }

    @Test
    @DisplayName("When Adding Skills _ they Should Be Added Correctly")
    void whenAddingSkills_theyShouldBeAddedCorrectly() {
        Shipment s = Shipment.Builder.newInstance("s").setPickupLocation(Location.Builder.newInstance().setId("loc").build()).setDeliveryLocation(TestUtils.loc("delLoc")).addRequiredSkill("drill").addRequiredSkill("screwdriver").build();
        assertTrue(s.getRequiredSkills().containsSkill("drill"));
        assertTrue(s.getRequiredSkills().containsSkill("drill"));
        assertTrue(s.getRequiredSkills().containsSkill("ScrewDriver"));
    }

    @Test
    @DisplayName("When Adding Skills Case Sens _ they Should Be Added Correctly")
    void whenAddingSkillsCaseSens_theyShouldBeAddedCorrectly() {
        Shipment s = Shipment.Builder.newInstance("s").setPickupLocation(Location.Builder.newInstance().setId("pick").build()).setDeliveryLocation(TestUtils.loc("del")).addRequiredSkill("DriLl").addRequiredSkill("screwDriver").build();
        assertTrue(s.getRequiredSkills().containsSkill("drill"));
        assertTrue(s.getRequiredSkills().containsSkill("drilL"));
    }

    @Test
    @DisplayName("When Adding Skills Case Sens V 2 _ they Should Be Added Correctly")
    void whenAddingSkillsCaseSensV2_theyShouldBeAddedCorrectly() {
        Shipment s = Shipment.Builder.newInstance("s").setPickupLocation(Location.Builder.newInstance().setId("loc").build()).setDeliveryLocation(TestUtils.loc("del")).addRequiredSkill("screwDriver").build();
        assertFalse(s.getRequiredSkills().containsSkill("drill"));
        assertFalse(s.getRequiredSkills().containsSkill("drilL"));
    }

    @Test
    @DisplayName("Name Should Be Assigned")
    void nameShouldBeAssigned() {
        Shipment s = Shipment.Builder.newInstance("s").setPickupLocation(Location.Builder.newInstance().setId("loc").build()).setDeliveryLocation(TestUtils.loc("del")).setName("name").build();
        assertEquals(s.getName(), "name");
    }

    @Test
    @DisplayName("When Setting Location _ it Should Work")
    void whenSettingLocation_itShouldWork() {
        Shipment s = Shipment.Builder.newInstance("s").setPickupLocation(Location.Builder.newInstance().setId("loc").build()).setDeliveryLocation(Location.Builder.newInstance().setId("del").build()).build();
        assertEquals(s.getPickupLocation().getId(), "loc");
        assertEquals(s.getPickupLocation().getId(), "loc");
        assertEquals(s.getDeliveryLocation().getId(), "del");
        assertEquals(s.getDeliveryLocation().getId(), "del");
    }

    @Test
    @DisplayName("When Setting Priorities _ it Should Be Set Correctly")
    void whenSettingPriorities_itShouldBeSetCorrectly() {
        Shipment s = Shipment.Builder.newInstance("s").setPickupLocation(Location.newInstance("loc")).setDeliveryLocation(Location.newInstance("loc")).setPriority(1).build();
        assertEquals(1, s.getPriority());
    }

    @Test
    @DisplayName("When Setting Priorities _ it Should Be Set Correctly 2")
    void whenSettingPriorities_itShouldBeSetCorrectly2() {
        Shipment s = Shipment.Builder.newInstance("s").setPickupLocation(Location.newInstance("loc")).setDeliveryLocation(Location.newInstance("loc")).setPriority(3).build();
        assertEquals(3, s.getPriority());
    }

    @Test
    @DisplayName("When Setting Priorities _ it Should Be Set Correctly 3")
    void whenSettingPriorities_itShouldBeSetCorrectly3() {
        Shipment s = Shipment.Builder.newInstance("s").setPickupLocation(Location.newInstance("loc")).setDeliveryLocation(Location.newInstance("loc")).setPriority(10).build();
        assertEquals(10, s.getPriority());
    }

    @Test
    @DisplayName("When Not Setting Priorities _ default Should Be 2")
    void whenNotSettingPriorities_defaultShouldBe2() {
        Shipment s = Shipment.Builder.newInstance("s").setPickupLocation(Location.newInstance("loc")).setDeliveryLocation(Location.newInstance("loc")).build();
        assertEquals(2, s.getPriority());
    }

    @Test
    @DisplayName("When Setting Incorrect Priorities _ it Should Throw Exception")
    void whenSettingIncorrectPriorities_itShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            Shipment s = Shipment.Builder.newInstance("s").setPickupLocation(Location.newInstance("loc")).setDeliveryLocation(Location.newInstance("loc")).setPriority(30).build();
        });
    }

    @Test
    @DisplayName("When Setting Incorrect Priorities _ it Should Throw Exception 2")
    void whenSettingIncorrectPriorities_itShouldThrowException2() {
        assertThrows(IllegalArgumentException.class, () -> {
            Shipment s = Shipment.Builder.newInstance("s").setPickupLocation(Location.newInstance("loc")).setDeliveryLocation(Location.newInstance("loc")).setPriority(0).build();
        });
    }

    @Test
    @DisplayName("When Setting User Data _ it Is Associated With The Job")
    void whenSettingUserData_itIsAssociatedWithTheJob() {
        Shipment one = Shipment.Builder.newInstance("s").setPickupLocation(Location.newInstance("loc")).setDeliveryLocation(Location.newInstance("loc")).setUserData(new HashMap<String, Object>()).build();
        Shipment two = Shipment.Builder.newInstance("s2").setPickupLocation(Location.newInstance("loc")).setDeliveryLocation(Location.newInstance("loc")).setUserData(42).build();
        Shipment three = Shipment.Builder.newInstance("s3").setPickupLocation(Location.newInstance("loc")).setDeliveryLocation(Location.newInstance("loc")).build();
        assertTrue(one.getUserData() instanceof Map);
        assertEquals(42, two.getUserData());
        assertNull(three.getUserData());
    }

    @Test
    @DisplayName("When Adding Max Time In Vehicle _ it Should Be Set")
    void whenAddingMaxTimeInVehicle_itShouldBeSet() {
        Shipment s = Shipment.Builder.newInstance("s").setPickupLocation(Location.newInstance("loc")).setDeliveryLocation(Location.newInstance("loc")).setMaxTimeInVehicle(10).build();
        assertEquals(10, s.getMaxTimeInVehicle(), 0.001);
    }

    @Test
    @DisplayName("When Not Adding Max Time In Vehicle _ it Should Be Default")
    void whenNotAddingMaxTimeInVehicle_itShouldBeDefault() {
        Shipment s = Shipment.Builder.newInstance("s").setPickupLocation(Location.newInstance("loc")).setDeliveryLocation(Location.newInstance("loc")).build();
        assertEquals(Double.MAX_VALUE, s.getMaxTimeInVehicle(), 0.001);
    }

    @Test
    @DisplayName("Test Shipment Activities")
    void testShipmentActivities() {
        Job job = Shipment.Builder.newInstance("s").setPickupLocation(Location.newInstance("loc")).setDeliveryLocation(Location.newInstance("loc")).build();
        assertEquals(2, job.getActivities().size());
        assertEquals(Activity.Type.PICKUP, job.getActivities().get(0).getActivityType());
        assertEquals(Activity.Type.DELIVERY, job.getActivities().get(1).getActivityType());
    }
}
