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

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.box.Jsprit;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.SizeDimension;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem.FleetSize;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleType;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import com.graphhopper.jsprit.core.reporting.SolutionPrinter;
import com.graphhopper.jsprit.core.util.Coordinate;
import com.graphhopper.jsprit.core.util.Solutions;
import com.graphhopper.jsprit.core.util.TestUtils;

public class ReturnedShipmentTest {

    @Test
    public void whenTwoReturnedShipmentsHaveTheSameId_theyReferencesShouldBeUnEqual() {
        ReturnedShipment one = ReturnedShipment.Builder.newInstance("s").addSizeDimension(0, 10)
                        .setPickupLocation(Location.Builder.newInstance().setId("foo").build()).
                        setDeliveryLocation(TestUtils.loc("foofoo")).setPickupServiceTime(10).setDeliveryServiceTime(20).build();
        ReturnedShipment two = ReturnedShipment.Builder.newInstance("s").addSizeDimension(0, 10)
                        .setPickupLocation(Location.Builder.newInstance().setId("foo").build()).
                        setDeliveryLocation(TestUtils.loc("foofoo")).setPickupServiceTime(10).setDeliveryServiceTime(20).build();

        assertTrue(one != two);
    }

    @Test
    public void sizeAtStartAndEndShouldBeCorrect() {
        ReturnedShipment one = ReturnedShipment.Builder.newInstance("s").addSizeDimension(0, 10)
                        .addSizeDimension(1, 5)
                        .setPickupLocation(Location.Builder.newInstance().setId("foo").build()).
                        setDeliveryLocation(TestUtils.loc("foofoo")).setPickupServiceTime(10).setDeliveryServiceTime(20).build();
        SizeDimension cap = SizeDimension.Builder.newInstance().addDimension(0, 0).addDimension(1, 0).build();
        assertTrue(one.getSizeAtStart().equals(cap));
        assertTrue(one.getSizeAtEnd().equals(cap));
    }

    @Test
    public void whenTwoReturnedShipmentsHaveTheSameId_theyShouldBeEqual() {
        ReturnedShipment one = ReturnedShipment.Builder.newInstance("s").addSizeDimension(0, 10)
                        .setPickupLocation(Location.Builder.newInstance().setId("foo").build()).
                        setDeliveryLocation(TestUtils.loc("foofoo")).setPickupServiceTime(10).setDeliveryServiceTime(20).build();
        ReturnedShipment two = ReturnedShipment.Builder.newInstance("s").addSizeDimension(0, 10)
                        .setPickupLocation(Location.Builder.newInstance().setId("foo").build()).
                        setDeliveryLocation(TestUtils.loc("foofoo")).setPickupServiceTime(10).setDeliveryServiceTime(20).build();

        assertTrue(one.equals(two));
    }

    @Test
    public void whenReturnedShipmentIsInstantiatedWithASizeOf10_theSizeShouldBe10() {
        ReturnedShipment one = ReturnedShipment.Builder.newInstance("s").addSizeDimension(0, 10)
                        .setPickupLocation(Location.Builder.newInstance().setId("foo").build()).
                        setDeliveryLocation(TestUtils.loc("foofoo")).setPickupServiceTime(10).setDeliveryServiceTime(20).build();
        assertEquals(10, one.getSize().get(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenReturnedShipmentIsBuiltWithNegativeDemand_itShouldThrowException() {
        @SuppressWarnings("unused")
        ReturnedShipment one = ReturnedShipment.Builder.newInstance("s").addSizeDimension(0, -10)
        .setPickupLocation(Location.Builder.newInstance().setId("foo").build())
        .setDeliveryLocation(TestUtils.loc("foofoo")).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenReturnedShipmentIsBuiltWithNegativeDemand_itShouldThrowException_v2() {
        @SuppressWarnings("unused")
        ReturnedShipment one = ReturnedShipment.Builder.newInstance("s").addSizeDimension(0, -10)
        .setPickupLocation(Location.Builder.newInstance().setId("foo").build())
        .setDeliveryLocation(TestUtils.loc("foofoo")).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenIdIsNull_itShouldThrowException() {
        @SuppressWarnings("unused")
        ReturnedShipment one = ReturnedShipment.Builder.newInstance(null).addSizeDimension(0, 10)
        .setPickupLocation(Location.Builder.newInstance().setId("foo").build())
        .setDeliveryLocation(TestUtils.loc("foofoo")).build();
    }

    @Test
    public void whenCallingForANewBuilderInstance_itShouldReturnBuilderCorrectly() {
        ReturnedShipment.Builder builder = ReturnedShipment.Builder.newInstance("s");
        assertNotNull(builder);
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenNeitherPickupLocationIdNorPickupCoord_itThrowsException() {
        @SuppressWarnings("unused")
        ReturnedShipment s = ReturnedShipment.Builder.newInstance("s")
        .setDeliveryLocation(TestUtils.loc("delLoc")).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenNeitherDeliveryLocationIdNorDeliveryCoord_itThrowsException() {
        @SuppressWarnings("unused")
        ReturnedShipment s = ReturnedShipment.Builder.newInstance("s")
        .setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build())
        .build();
    }

    @Test
    public void whenPickupLocationIdIsSet_itShouldBeDoneCorrectly() {
        ReturnedShipment s = ReturnedShipment.Builder.newInstance("s")
                        .setDeliveryLocation(TestUtils.loc("delLoc"))
                        .setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build())
                        .build();
        assertEquals("pickLoc", s.getPickupActivity().getLocation().getId());
        assertEquals("pickLoc", s.getPickupActivity().getLocation().getId());
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenPickupLocationIsNull_itThrowsException() {
        @SuppressWarnings("unused")
        ReturnedShipment.Builder builder = ReturnedShipment.Builder.newInstance("s")
        .setPickupLocation(Location.Builder.newInstance().setId(null).build());
    }

    @Test
    public void whenPickupCoordIsSet_itShouldBeDoneCorrectly() {
        ReturnedShipment s = ReturnedShipment.Builder.newInstance("s")
                        .setDeliveryLocation(TestUtils.loc("delLoc")).setPickupLocation(Location.Builder.newInstance().setId("pickLoc").setCoordinate(Coordinate.newInstance(1, 2)).build()).build();
        assertEquals(1.0, s.getPickupActivity().getLocation().getCoordinate().getX(), 0.01);
        assertEquals(2.0, s.getPickupActivity().getLocation().getCoordinate().getY(), 0.01);
        assertEquals(1.0, s.getPickupActivity().getLocation().getCoordinate().getX(), 0.01);
        assertEquals(2.0, s.getPickupActivity().getLocation().getCoordinate().getY(), 0.01);
    }


    @Test
    public void whenDeliveryLocationIdIsSet_itShouldBeDoneCorrectly() {
        ReturnedShipment s = ReturnedShipment.Builder.newInstance("s")
                        .setDeliveryLocation(TestUtils.loc("delLoc")).setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build()).build();
        assertEquals("delLoc", s.getExchangeActivity().getLocation().getId());
        assertEquals("delLoc", s.getExchangeActivity().getLocation().getId());
    }


    @Test
    public void whenDeliveryCoordIsSet_itShouldBeDoneCorrectly() {
        ReturnedShipment s = ReturnedShipment.Builder.newInstance("s")
                        .setDeliveryLocation(TestUtils.loc("delLoc", Coordinate.newInstance(1, 2)))
                        .setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build())
                        .build();
        assertEquals(1.0, s.getExchangeActivity().getLocation().getCoordinate().getX(), 0.01);
        assertEquals(2.0, s.getExchangeActivity().getLocation().getCoordinate().getY(), 0.01);
        assertEquals(1.0, s.getExchangeActivity().getLocation().getCoordinate().getX(), 0.01);
        assertEquals(2.0, s.getExchangeActivity().getLocation().getCoordinate().getY(), 0.01);
    }

    @Test
    public void whenPickupServiceTimeIsNotSet_itShouldBeZero() {
        ReturnedShipment s = ReturnedShipment.Builder.newInstance("s")
                        .setDeliveryLocation(TestUtils.loc("delLoc")).setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build()).build();
        assertEquals(0.0, s.getPickupActivity().getOperationTime(), 0.01);
    }

    @Test
    public void whenDeliveryServiceTimeIsNotSet_itShouldBeZero() {
        ReturnedShipment s = ReturnedShipment.Builder.newInstance("s")
                        .setDeliveryLocation(TestUtils.loc("delLoc")).setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build()).build();
        assertEquals(0.0, s.getExchangeActivity().getOperationTime(), 0.01);
    }

    @Test
    public void whenPickupServiceTimeIsSet_itShouldBeDoneCorrectly() {
        ReturnedShipment s = ReturnedShipment.Builder.newInstance("s")
                        .setPickupServiceTime(2.0)
                        .setDeliveryLocation(TestUtils.loc("delLoc")).setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build()).build();
        assertEquals(2.0, s.getPickupActivity().getOperationTime(), 0.01);
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenPickupServiceIsSmallerThanZero_itShouldThrowException() {
        @SuppressWarnings("unused")
        ReturnedShipment s = ReturnedShipment.Builder.newInstance("s").setPickupServiceTime(-2.0)
        .setDeliveryLocation(TestUtils.loc("delLoc")).setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build()).build();
    }

    @Test
    public void whenDeliveryServiceTimeIsSet_itShouldBeDoneCorrectly() {
        ReturnedShipment s = ReturnedShipment.Builder.newInstance("s").setDeliveryServiceTime(2.0)
                        .setDeliveryLocation(TestUtils.loc("delLoc")).setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build()).build();
        assertEquals(2.0, s.getExchangeActivity().getOperationTime(), 0.01);
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenDeliveryServiceIsSmallerThanZero_itShouldThrowException() {
        @SuppressWarnings("unused")
        ReturnedShipment s = ReturnedShipment.Builder.newInstance("s").setDeliveryServiceTime(-2.0)
        .setDeliveryLocation(TestUtils.loc("delLoc"))
        .setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build())
        .build();
    }

    @Test
    public void whenPickupTimeWindowIsNotSet_itShouldBeTheDefaultOne() {
        ReturnedShipment s = ReturnedShipment.Builder.newInstance("s")
                        .setDeliveryLocation(TestUtils.loc("delLoc"))
                        .setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build())
                        .build();
        assertEquals(0.0, s.getPickupActivity().getSingleTimeWindow().getStart(),
                        0.01);
        assertEquals(Double.MAX_VALUE,
                        s.getPickupActivity().getSingleTimeWindow().getEnd(), 0.01);
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenPickupTimeWindowIsNull_itShouldThrowException() {
        @SuppressWarnings("unused")
        ReturnedShipment s = ReturnedShipment.Builder.newInstance("s").setPickupTimeWindow(null)
        .setDeliveryLocation(TestUtils.loc("delLoc"))
        .setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build())
        .build();
    }

    @Test
    public void whenPickupTimeWindowIsSet_itShouldBeDoneCorrectly() {
        ReturnedShipment s = ReturnedShipment.Builder.newInstance("s")
                        .setPickupTimeWindow(TimeWindow.newInstance(1, 2))
                        .setDeliveryLocation(TestUtils.loc("delLoc")).setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build()).build();
        assertEquals(1.0, s.getPickupActivity().getSingleTimeWindow().getStart(),
                        0.01);
        assertEquals(2.0, s.getPickupActivity().getSingleTimeWindow().getEnd(), 0.01);
    }

    @Test
    public void whenDeliveryTimeWindowIsNotSet_itShouldBeTheDefaultOne() {
        ReturnedShipment s = ReturnedShipment.Builder.newInstance("s")
                        .setDeliveryLocation(TestUtils.loc("delLoc"))
                        .setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build())
                        .build();
        assertEquals(0.0, s.getExchangeActivity().getSingleTimeWindow().getStart(),
                        0.01);
        assertEquals(Double.MAX_VALUE,
                        s.getExchangeActivity().getSingleTimeWindow().getEnd(), 0.01);
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenDeliveryTimeWindowIsNull_itShouldThrowException() {
        @SuppressWarnings("unused")
        ReturnedShipment s = ReturnedShipment.Builder.newInstance("s").setDeliveryTimeWindow(null)
        .setDeliveryLocation(TestUtils.loc("delLoc"))
        .setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build())
        .build();
    }

    @Test
    public void whenDeliveryTimeWindowIsSet_itShouldBeDoneCorrectly() {
        ReturnedShipment s = ReturnedShipment.Builder.newInstance("s")
                        .setDeliveryTimeWindow(TimeWindow.newInstance(1, 2))
                        .setDeliveryLocation(TestUtils.loc("delLoc")).setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build()).build();
        assertEquals(1.0, s.getExchangeActivity().getSingleTimeWindow().getStart(),
                        0.01);
        assertEquals(2.0, s.getExchangeActivity().getSingleTimeWindow().getEnd(),
                        0.01);
    }

    @Test
    public void whenUsingAddDeliveryTimeWindow_itShouldBeDoneCorrectly() {
        ReturnedShipment s = ReturnedShipment.Builder.newInstance("s")
                        .addDeliveryTimeWindow(TimeWindow.newInstance(1, 2))
                        .setDeliveryLocation(TestUtils.loc("delLoc")).setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build()).build();
        assertEquals(1.0, s.getExchangeActivity().getSingleTimeWindow().getStart(),
                        0.01);
        assertEquals(2.0, s.getExchangeActivity().getSingleTimeWindow().getEnd(),
                        0.01);
    }

    @Test
    public void whenUsingAddDeliveryTimeWindow2_itShouldBeDoneCorrectly() {
        ReturnedShipment s = ReturnedShipment.Builder.newInstance("s").addDeliveryTimeWindow(1, 2)
                        .setDeliveryLocation(TestUtils.loc("delLoc")).setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build()).build();
        assertEquals(1.0, s.getExchangeActivity().getSingleTimeWindow().getStart(),
                        0.01);
        assertEquals(2.0, s.getExchangeActivity().getSingleTimeWindow().getEnd(),
                        0.01);
    }

    @Test
    public void whenAddingMultipleDeliveryTimeWindows_itShouldBeDoneCorrectly() {
        TimeWindow tw1 = TimeWindow.newInstance(1, 2);
        TimeWindow tw2 = TimeWindow.newInstance(4, 5);
        ReturnedShipment s = ReturnedShipment.Builder.newInstance("s").addDeliveryTimeWindow(tw1)
                        .addDeliveryTimeWindow(tw2)
                        .setDeliveryLocation(TestUtils.loc("delLoc")).setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build()).build();
        assertEquals(s.getExchangeActivity().getTimeWindows().size(), 2);
        assertThat(s.getExchangeActivity().getTimeWindows(), hasItem(is(tw1)));
        assertThat(s.getExchangeActivity().getTimeWindows(), hasItem(is(tw2)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenAddingMultipleOverlappingDeliveryTimeWindows_itShouldThrowException() {
        ReturnedShipment s = ReturnedShipment.Builder.newInstance("s").addDeliveryTimeWindow(1, 3)
                        .addDeliveryTimeWindow(2, 5)
                        .setDeliveryLocation(TestUtils.loc("delLoc")).setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build()).build();
        assertEquals(1.0, s.getExchangeActivity().getSingleTimeWindow().getStart(),
                        0.01);
        assertEquals(2.0, s.getExchangeActivity().getSingleTimeWindow().getEnd(),
                        0.01);
    }


    @Test
    public void whenUsingAddPickupTimeWindow_itShouldBeDoneCorrectly() {
        ReturnedShipment s = ReturnedShipment.Builder.newInstance("s")
                        .addPickupTimeWindow(TimeWindow.newInstance(1, 2))
                        .setDeliveryLocation(TestUtils.loc("delLoc")).setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build()).build();
        assertEquals(1.0, s.getPickupActivity().getSingleTimeWindow().getStart(),
                        0.01);
        assertEquals(2.0, s.getPickupActivity().getSingleTimeWindow().getEnd(), 0.01);
    }

    @Test
    public void whenUsingAddPickupTimeWindow2_itShouldBeDoneCorrectly() {
        ReturnedShipment s = ReturnedShipment.Builder.newInstance("s").addPickupTimeWindow(1, 2)
                        .setDeliveryLocation(TestUtils.loc("delLoc")).setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build()).build();
        assertEquals(1.0, s.getPickupActivity().getSingleTimeWindow().getStart(),
                        0.01);
        assertEquals(2.0, s.getPickupActivity().getSingleTimeWindow().getEnd(), 0.01);
    }

    @Test
    public void whenAddingMultiplePickupTimeWindows_itShouldBeDoneCorrectly() {
        TimeWindow tw1 = TimeWindow.newInstance(1, 2);
        TimeWindow tw2 = TimeWindow.newInstance(4, 5);
        ReturnedShipment s = ReturnedShipment.Builder.newInstance("s").addPickupTimeWindow(tw1)
                        .addPickupTimeWindow(tw2)
                        .setDeliveryLocation(TestUtils.loc("delLoc")).setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build()).build();
        assertEquals(s.getPickupActivity().getTimeWindows().size(), 2);
        assertThat(s.getPickupActivity().getTimeWindows(), hasItem(is(tw1)));
        assertThat(s.getPickupActivity().getTimeWindows(), hasItem(is(tw2)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenAddingMultipleOverlappingPickupTimeWindows_itShouldThrowException() {
        ReturnedShipment s = ReturnedShipment.Builder.newInstance("s").addPickupTimeWindow(1, 3)
                        .addPickupTimeWindow(2, 5)
                        .setDeliveryLocation(TestUtils.loc("delLoc")).setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build()).build();
        assertEquals(1.0, s.getPickupActivity().getSingleTimeWindow().getStart(), 0.01);
        assertEquals(2.0, s.getPickupActivity().getSingleTimeWindow().getEnd(), 0.01);
    }


    @Test(expected = IllegalArgumentException.class)
    public void whenReturnedShipmentHasNegativeCapacityVal_throwIllegalStateExpception() {
        @SuppressWarnings("unused")
        ReturnedShipment one = ReturnedShipment.Builder.newInstance("s")
        .setPickupLocation(Location.Builder.newInstance().setId("foo").build())
        .setDeliveryLocation(TestUtils.loc("foofoo"))
        .addSizeDimension(0, -2)
        .build();
    }

    @Test
    public void whenAddingTwoCapDimension_nuOfDimsShouldBeTwo() {
        ReturnedShipment one = ReturnedShipment.Builder.newInstance("s")
                        .setPickupLocation(Location.Builder.newInstance().setId("foo").build())
                        .setDeliveryLocation(TestUtils.loc("foofoo"))
                        .addSizeDimension(0, 2)
                        .addSizeDimension(1, 4)
                        .addBackhaulSizeDimension(0, 3)
                        .addBackhaulSizeDimension(1, 5)
                        .build();
        assertEquals(2, one.getSize().getNuOfDimensions());
        assertEquals(2, one.getBackhaulActivity().getLoadChange().getNuOfDimensions());
    }

    @Test
    public void whenReturnedShipmentIsBuiltWithoutSpecifyingCapacity_itShouldHvCapWithOneDimAndDimValOfZero() {
        ReturnedShipment one = ReturnedShipment.Builder.newInstance("s")
                        .setPickupLocation(Location.Builder.newInstance().setId("foo").setCoordinate(Coordinate.newInstance(0, 0)).build())
                        .setDeliveryLocation(TestUtils.loc("foofoo")).build();
        assertEquals(1, one.getSize().getNuOfDimensions());
        assertEquals(0, one.getSize().get(0));
        assertEquals(1, one.getBackhaulActivity().getLoadChange().getNuOfDimensions());
    }

    @Test
    public void whenReturnedShipmentIsBuiltWithConstructorWhereSizeIsSpecified_capacityShouldBeSetCorrectly() {
        ReturnedShipment one = ReturnedShipment.Builder.newInstance("s").addSizeDimension(0, 1)
                        .setPickupLocation(Location.Builder.newInstance().setId("foo").setCoordinate(Coordinate.newInstance(0, 0)).build())
                        .setDeliveryLocation(TestUtils.loc("foofoo")).build();
        assertEquals(1, one.getSize().getNuOfDimensions());
        assertEquals(1, one.getSize().get(0));
    }

    @Test
    public void whenAddingSkills_theyShouldBeAddedCorrectly() {
        ReturnedShipment s = ReturnedShipment.Builder.newInstance("s")
                        .setPickupLocation(Location.Builder.newInstance().setId("loc").build())
                        .setDeliveryLocation(TestUtils.loc("delLoc"))
                        .addRequiredSkill("drill").addRequiredSkill("screwdriver").build();
        assertTrue(s.getRequiredSkills().containsSkill("drill"));
        assertTrue(s.getRequiredSkills().containsSkill("drill"));
        assertTrue(s.getRequiredSkills().containsSkill("ScrewDriver"));
    }

    @Test
    public void whenAddingSkillsCaseSens_theyShouldBeAddedCorrectly() {
        ReturnedShipment s = ReturnedShipment.Builder.newInstance("s")
                        .setPickupLocation(Location.Builder.newInstance().setId("pick").build())
                        .setDeliveryLocation(TestUtils.loc("del"))
                        .addRequiredSkill("DriLl").addRequiredSkill("screwDriver").build();
        assertTrue(s.getRequiredSkills().containsSkill("drill"));
        assertTrue(s.getRequiredSkills().containsSkill("drilL"));
    }

    @Test
    public void whenAddingSkillsCaseSensV2_theyShouldBeAddedCorrectly() {
        ReturnedShipment s = ReturnedShipment.Builder.newInstance("s")
                        .setPickupLocation(Location.Builder.newInstance().setId("loc").build())
                        .setDeliveryLocation(TestUtils.loc("del"))
                        .addRequiredSkill("screwDriver").build();
        assertFalse(s.getRequiredSkills().containsSkill("drill"));
        assertFalse(s.getRequiredSkills().containsSkill("drilL"));
    }

    @Test
    public void nameShouldBeAssigned() {
        ReturnedShipment s = ReturnedShipment.Builder.newInstance("s")
                        .setPickupLocation(Location.Builder.newInstance().setId("loc").build())
                        .setDeliveryLocation(TestUtils.loc("del"))
                        .setName("name").build();
        assertEquals("name", s.getName());
    }

    @Test
    public void whenSettingLocation_itShouldWork() {
        ReturnedShipment s = ReturnedShipment.Builder.newInstance("s")
                        .setPickupLocation(Location.Builder.newInstance().setId("loc").build())
                        .setDeliveryLocation(Location.Builder.newInstance().setId("del").build())
                        .setBackhaulLocation(Location.Builder.newInstance().setId("back").build())
                        .build();
        assertEquals("loc", s.getPickupActivity().getLocation().getId());
        assertEquals("del", s.getExchangeActivity().getLocation().getId());
        assertEquals("back", s.getBackhaulActivity().getLocation().getId());
    }

    @Test
    public void whenSettingPriorities_itShouldBeSetCorrectly() {
        ReturnedShipment s = ReturnedShipment.Builder.newInstance("s")
                        .setPickupLocation(Location.newInstance("loc"))
                        .setDeliveryLocation(Location.newInstance("loc"))
                        .setPriority(1).build();
        assertEquals(1, s.getPriority());
    }

    @Test
    public void whenSettingPriorities_itShouldBeSetCorrectly2() {
        ReturnedShipment s = ReturnedShipment.Builder.newInstance("s")
                        .setPickupLocation(Location.newInstance("loc"))
                        .setDeliveryLocation(Location.newInstance("loc"))
                        .setPriority(3).build();
        assertEquals(3, s.getPriority());
    }

    @Test
    public void whenNotSettingPriorities_defaultShouldBe2() {
        ReturnedShipment s = ReturnedShipment.Builder.newInstance("s")
                        .setPickupLocation(Location.newInstance("loc"))
                        .setDeliveryLocation(Location.newInstance("loc"))
                        .build();
        assertEquals(2, s.getPriority());
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenSettingIncorrectPriorities_itShouldThrowException() {
        ReturnedShipment.Builder.newInstance("s").setPickupLocation(Location.newInstance("loc"))
        .setDeliveryLocation(Location.newInstance("loc"))
        .setPriority(30).build();

    }

    @Test(expected = IllegalArgumentException.class)
    public void whenSettingIncorrectPriorities_itShouldThrowException2() {
        ReturnedShipment.Builder.newInstance("s").setPickupLocation(Location.newInstance("loc"))
        .setDeliveryLocation(Location.newInstance("loc"))
        .setPriority(0).build();

    }

    @Test
    public void firstTest() {
        Set<Job> jobs = new HashSet<>();
        jobs.add(CustomJob.Builder.newInstance("job")
                        .addPickup(Location.newInstance(10, 0), SizeDimension.of(1), 0d, TimeWindow.newInstance(0, 30))
                        .addExchange(Location.newInstance(5, 30)).addDelivery(Location.newInstance(10, 0), SizeDimension.of(1))
                        .build());
        jobs.add(CustomJob.Builder.newInstance("job2")
                        .addPickup(Location.newInstance(20, 0), SizeDimension.of(1))
                        .addExchange(Location.newInstance(20, 30), SizeDimension.EMPTY)
                        .addDelivery(Location.newInstance(20, 0), SizeDimension.of(1))
                        .build());
        jobs.add(CustomJob.Builder.newInstance("job3")
                        .addPickup(Location.newInstance(20, 30), SizeDimension.of(1))
                        .addExchange(Location.newInstance(40, 30), SizeDimension.EMPTY)
                        .addDelivery(Location.newInstance(20, 30), SizeDimension.of(1))
                        .build());
        jobs.add(CustomJob.Builder.newInstance("job4")
                        .addPickup(Location.newInstance(20, 30), SizeDimension.of(1))
                        .addExchange(Location.newInstance(40, 30), SizeDimension.EMPTY)
                        .addDelivery(Location.newInstance(20, 30), SizeDimension.of(1))
                        .build());
        VehicleType type = VehicleTypeImpl.Builder.newInstance("type").addCapacityDimension(0, 2)
                        .build();
        Vehicle v = VehicleImpl.Builder.newInstance("v").setType(type)
                        .setStartLocation(Location.newInstance(0, 0)).build();
        Vehicle v2 = VehicleImpl.Builder.newInstance("v2").setType(type)
                        .setStartLocation(Location.newInstance(0, 0)).build();
        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance()
                        .setFleetSize(FleetSize.FINITE)
                        .addAllJobs(jobs).addVehicle(v).addVehicle(v2).build();
        VehicleRoutingAlgorithm vra = Jsprit.createAlgorithm(vrp);
        vra.setMaxIterations(10);
        VehicleRoutingProblemSolution solution = Solutions.bestOf(vra.searchSolutions());
        SolutionPrinter.print(vrp, solution, SolutionPrinter.Print.VERBOSE);
        Assert.assertTrue(solution.getUnassignedJobs().isEmpty());
    }
}
