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

import org.junit.Test;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.SizeDimension;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow;
import com.graphhopper.jsprit.core.util.Coordinate;
import com.graphhopper.jsprit.core.util.TestUtils;

public class ShipmentJobTest {

    @Test
    public void whenTwoShipmentsHaveTheSameId_theyReferencesShouldBeUnEqual() {
        ShipmentJob one = new ShipmentJob.Builder("s").addSizeDimension(0, 10).setPickupLocation(Location.Builder.newInstance().setId("foo").build()).
                setDeliveryLocation(TestUtils.loc("foofoo")).setPickupServiceTime(10).setDeliveryServiceTime(20).build();
        ShipmentJob two = new ShipmentJob.Builder("s").addSizeDimension(0, 10).setPickupLocation(Location.Builder.newInstance().setId("foo").build()).
                setDeliveryLocation(TestUtils.loc("foofoo")).setPickupServiceTime(10).setDeliveryServiceTime(20).build();

        assertTrue(one != two);
    }

    @Test
    public void sizeAtStartAndEndShouldBeCorrect() {
        ShipmentJob one = new ShipmentJob.Builder("s").addSizeDimension(0, 10).addSizeDimension(1, 5).setPickupLocation(Location.Builder.newInstance().setId("foo").build()).
                setDeliveryLocation(TestUtils.loc("foofoo")).setPickupServiceTime(10).setDeliveryServiceTime(20).build();
        SizeDimension cap = SizeDimension.Builder.newInstance().addDimension(0, 0).addDimension(1, 0).build();
        assertTrue(one.getSizeAtStart().equals(cap));
        assertTrue(one.getSizeAtEnd().equals(cap));
    }

    @Test
    public void whenTwoShipmentsHaveTheSameId_theyShouldBeEqual() {
        ShipmentJob one = new ShipmentJob.Builder("s").addSizeDimension(0, 10).setPickupLocation(Location.Builder.newInstance().setId("foo").build()).
                setDeliveryLocation(TestUtils.loc("foofoo")).setPickupServiceTime(10).setDeliveryServiceTime(20).build();
        ShipmentJob two = new ShipmentJob.Builder("s").addSizeDimension(0, 10).setPickupLocation(Location.Builder.newInstance().setId("foo").build()).
                setDeliveryLocation(TestUtils.loc("foofoo")).setPickupServiceTime(10).setDeliveryServiceTime(20).build();

        assertTrue(one.equals(two));
    }

    @Test
    public void whenShipmentIsInstantiatedWithASizeOf10_theSizeShouldBe10() {
        ShipmentJob one = new ShipmentJob.Builder("s").addSizeDimension(0, 10).setPickupLocation(Location.Builder.newInstance().setId("foo").build()).
                setDeliveryLocation(TestUtils.loc("foofoo")).setPickupServiceTime(10).setDeliveryServiceTime(20).build();
        assertEquals(10, one.getSize().get(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenShipmentIsBuiltWithNegativeDemand_itShouldThrowException() {
        @SuppressWarnings("unused")
        ShipmentJob one = new ShipmentJob.Builder("s").addSizeDimension(0, -10)
        .setPickupLocation(Location.Builder.newInstance().setId("foo").build())
        .setDeliveryLocation(TestUtils.loc("foofoo")).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenShipmentIsBuiltWithNegativeDemand_itShouldThrowException_v2() {
        @SuppressWarnings("unused")
        ShipmentJob one = new ShipmentJob.Builder("s").addSizeDimension(0, -10)
        .setPickupLocation(Location.Builder.newInstance().setId("foo").build())
        .setDeliveryLocation(TestUtils.loc("foofoo")).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenIdIsNull_itShouldThrowException() {
        @SuppressWarnings("unused")
        ShipmentJob one = new ShipmentJob.Builder(null).addSizeDimension(0, 10)
        .setPickupLocation(Location.Builder.newInstance().setId("foo").build())
        .setDeliveryLocation(TestUtils.loc("foofoo")).build();
    }

    @Test
    public void whenCallingForANewBuilderInstance_itShouldReturnBuilderCorrectly() {
        ShipmentJob.Builder builder = new ShipmentJob.Builder("s");
        assertNotNull(builder);
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenNeitherPickupLocationIdNorPickupCoord_itThrowsException() {
        @SuppressWarnings("unused")
        ShipmentJob s = new ShipmentJob.Builder("s").setDeliveryLocation(TestUtils.loc("delLoc")).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenNeitherDeliveryLocationIdNorDeliveryCoord_itThrowsException() {
        @SuppressWarnings("unused")
        ShipmentJob s = new ShipmentJob.Builder("s").setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build()).build();
    }

    @Test
    public void whenPickupLocationIdIsSet_itShouldBeDoneCorrectly() {
        ShipmentJob s = new ShipmentJob.Builder("s").setDeliveryLocation(TestUtils.loc("delLoc")).setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build()).build();
        assertEquals("pickLoc", s.getPickupActivity().getLocation().getId());
        assertEquals("pickLoc", s.getPickupActivity().getLocation().getId());
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenPickupLocationIsNull_itThrowsException() {
        @SuppressWarnings("unused")
        ShipmentJob.Builder builder = new ShipmentJob.Builder("s").setPickupLocation(Location.Builder.newInstance().setId(null).build());
    }

    @Test
    public void whenPickupCoordIsSet_itShouldBeDoneCorrectly() {
        ShipmentJob s = new ShipmentJob.Builder("s")
                .setDeliveryLocation(TestUtils.loc("delLoc")).setPickupLocation(Location.Builder.newInstance().setId("pickLoc").setCoordinate(Coordinate.newInstance(1, 2)).build()).build();
        assertEquals(1.0, s.getPickupActivity().getLocation().getCoordinate().getX(), 0.01);
        assertEquals(2.0, s.getPickupActivity().getLocation().getCoordinate().getY(), 0.01);
        assertEquals(1.0, s.getPickupActivity().getLocation().getCoordinate().getX(), 0.01);
        assertEquals(2.0, s.getPickupActivity().getLocation().getCoordinate().getY(), 0.01);
    }


    @Test
    public void whenDeliveryLocationIdIsSet_itShouldBeDoneCorrectly() {
        ShipmentJob s = new ShipmentJob.Builder("s")
                .setDeliveryLocation(TestUtils.loc("delLoc")).setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build()).build();
        assertEquals("delLoc", s.getDeliveryActivity().getLocation().getId());
        assertEquals("delLoc", s.getDeliveryActivity().getLocation().getId());
    }


    @Test
    public void whenDeliveryCoordIsSet_itShouldBeDoneCorrectly() {
        ShipmentJob s = new ShipmentJob.Builder("s").setDeliveryLocation(TestUtils.loc("delLoc", Coordinate.newInstance(1, 2)))
                .setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build())
                .build();
        assertEquals(1.0, s.getDeliveryActivity().getLocation().getCoordinate().getX(), 0.01);
        assertEquals(2.0, s.getDeliveryActivity().getLocation().getCoordinate().getY(), 0.01);
        assertEquals(1.0, s.getDeliveryActivity().getLocation().getCoordinate().getX(), 0.01);
        assertEquals(2.0, s.getDeliveryActivity().getLocation().getCoordinate().getY(), 0.01);
    }

    @Test
    public void whenPickupServiceTimeIsNotSet_itShouldBeZero() {
        ShipmentJob s = new ShipmentJob.Builder("s")
                .setDeliveryLocation(TestUtils.loc("delLoc")).setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build()).build();
        assertEquals(0.0, s.getPickupActivity().getOperationTime(), 0.01);
    }

    @Test
    public void whenDeliveryServiceTimeIsNotSet_itShouldBeZero() {
        ShipmentJob s = new ShipmentJob.Builder("s")
                .setDeliveryLocation(TestUtils.loc("delLoc")).setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build()).build();
        assertEquals(0.0, s.getDeliveryActivity().getOperationTime(), 0.01);
    }

    @Test
    public void whenPickupServiceTimeIsSet_itShouldBeDoneCorrectly() {
        ShipmentJob s = new ShipmentJob.Builder("s")
                .setPickupServiceTime(2.0)
                .setDeliveryLocation(TestUtils.loc("delLoc")).setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build()).build();
        assertEquals(2.0, s.getPickupActivity().getOperationTime(), 0.01);
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenPickupServiceIsSmallerThanZero_itShouldThrowException() {
        @SuppressWarnings("unused")
        ShipmentJob s = new ShipmentJob.Builder("s").setPickupServiceTime(-2.0)
        .setDeliveryLocation(TestUtils.loc("delLoc")).setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build()).build();
    }

    @Test
    public void whenDeliveryServiceTimeIsSet_itShouldBeDoneCorrectly() {
        ShipmentJob s = new ShipmentJob.Builder("s").setDeliveryServiceTime(2.0)
                .setDeliveryLocation(TestUtils.loc("delLoc")).setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build()).build();
        assertEquals(2.0, s.getDeliveryActivity().getOperationTime(), 0.01);
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenDeliveryServiceIsSmallerThanZero_itShouldThrowException() {
        @SuppressWarnings("unused")
        ShipmentJob s = new ShipmentJob.Builder("s").setDeliveryServiceTime(-2.0).setDeliveryLocation(TestUtils.loc("delLoc")).setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build()).build();
    }

    @Test
    public void whenPickupTimeWindowIsNotSet_itShouldBeTheDefaultOne() {
        ShipmentJob s = new ShipmentJob.Builder("s").setDeliveryLocation(TestUtils.loc("delLoc")).setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build()).build();
        assertEquals(0.0, s.getPickupActivity().getTimeWindows().iterator().next().getStart(),
                0.01);
        assertEquals(Double.MAX_VALUE,
                s.getPickupActivity().getTimeWindows().iterator().next().getEnd(), 0.01);
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenPickupTimeWindowIsNull_itShouldThrowException() {
        @SuppressWarnings("unused")
        ShipmentJob s = new ShipmentJob.Builder("s").setPickupTimeWindow(null).setDeliveryLocation(TestUtils.loc("delLoc")).setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build()).build();
    }

    @Test
    public void whenPickupTimeWindowIsSet_itShouldBeDoneCorrectly() {
        ShipmentJob s = new ShipmentJob.Builder("s").setPickupTimeWindow(TimeWindow.newInstance(1, 2))
                .setDeliveryLocation(TestUtils.loc("delLoc")).setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build()).build();
        assertEquals(1.0, s.getPickupActivity().getTimeWindows().iterator().next().getStart(),
                0.01);
        assertEquals(2.0, s.getPickupActivity().getTimeWindows().iterator().next().getEnd(), 0.01);
    }

    @Test
    public void whenDeliveryTimeWindowIsNotSet_itShouldBeTheDefaultOne() {
        ShipmentJob s = new ShipmentJob.Builder("s").setDeliveryLocation(TestUtils.loc("delLoc")).setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build()).build();
        assertEquals(0.0, s.getDeliveryActivity().getTimeWindows().iterator().next().getStart(),
                0.01);
        assertEquals(Double.MAX_VALUE,
                s.getDeliveryActivity().getTimeWindows().iterator().next().getEnd(), 0.01);
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenDeliveryTimeWindowIsNull_itShouldThrowException() {
        @SuppressWarnings("unused")
        ShipmentJob s = new ShipmentJob.Builder("s").setDeliveryTimeWindow(null).setDeliveryLocation(TestUtils.loc("delLoc")).setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build()).build();
    }

    @Test
    public void whenDeliveryTimeWindowIsSet_itShouldBeDoneCorrectly() {
        ShipmentJob s = new ShipmentJob.Builder("s").setDeliveryTimeWindow(TimeWindow.newInstance(1, 2))
                .setDeliveryLocation(TestUtils.loc("delLoc")).setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build()).build();
        assertEquals(1.0, s.getDeliveryActivity().getTimeWindows().iterator().next().getStart(),
                0.01);
        assertEquals(2.0, s.getDeliveryActivity().getTimeWindows().iterator().next().getEnd(),
                0.01);
    }

    @Test
    public void whenUsingAddDeliveryTimeWindow_itShouldBeDoneCorrectly() {
        ShipmentJob s = new ShipmentJob.Builder("s").addDeliveryTimeWindow(TimeWindow.newInstance(1, 2))
                .setDeliveryLocation(TestUtils.loc("delLoc")).setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build()).build();
        assertEquals(1.0, s.getDeliveryActivity().getTimeWindows().iterator().next().getStart(),
                0.01);
        assertEquals(2.0, s.getDeliveryActivity().getTimeWindows().iterator().next().getEnd(),
                0.01);
    }

    @Test
    public void whenUsingAddDeliveryTimeWindow2_itShouldBeDoneCorrectly() {
        ShipmentJob s = new ShipmentJob.Builder("s").addDeliveryTimeWindow(1, 2)
                .setDeliveryLocation(TestUtils.loc("delLoc")).setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build()).build();
        assertEquals(1.0, s.getDeliveryActivity().getTimeWindows().iterator().next().getStart(),
                0.01);
        assertEquals(2.0, s.getDeliveryActivity().getTimeWindows().iterator().next().getEnd(),
                0.01);
    }

    @Test
    public void whenAddingMultipleDeliveryTimeWindows_itShouldBeDoneCorrectly() {
        TimeWindow tw1 = TimeWindow.newInstance(1, 2);
        TimeWindow tw2 = TimeWindow.newInstance(4, 5);
        ShipmentJob s = new ShipmentJob.Builder("s").addDeliveryTimeWindow(tw1).addDeliveryTimeWindow(tw2)
                .setDeliveryLocation(TestUtils.loc("delLoc")).setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build()).build();
        assertEquals(s.getDeliveryActivity().getTimeWindows().size(), 2);
        assertThat(s.getDeliveryActivity().getTimeWindows(), hasItem(is(tw1)));
        assertThat(s.getDeliveryActivity().getTimeWindows(), hasItem(is(tw2)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenAddingMultipleOverlappingDeliveryTimeWindows_itShouldThrowException() {
        ShipmentJob s = new ShipmentJob.Builder("s")
                .addDeliveryTimeWindow(1, 3)
                .addDeliveryTimeWindow(2, 5)
                .setDeliveryLocation(TestUtils.loc("delLoc"))
                .setPickupLocation(Location.newInstance("pickLoc"))
                .build();
    }


    @Test
    public void whenUsingAddPickupTimeWindow_itShouldBeDoneCorrectly() {
        ShipmentJob s = new ShipmentJob.Builder("s").addPickupTimeWindow(TimeWindow.newInstance(1, 2))
                .setDeliveryLocation(TestUtils.loc("delLoc")).setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build()).build();
        assertEquals(1.0, s.getPickupActivity().getTimeWindows().iterator().next().getStart(),
                0.01);
        assertEquals(2.0, s.getPickupActivity().getTimeWindows().iterator().next().getEnd(), 0.01);
    }

    @Test
    public void whenUsingAddPickupTimeWindow2_itShouldBeDoneCorrectly() {
        ShipmentJob s = new ShipmentJob.Builder("s").addPickupTimeWindow(1, 2)
                .setDeliveryLocation(TestUtils.loc("delLoc")).setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build()).build();
        assertEquals(1.0, s.getPickupActivity().getTimeWindows().iterator().next().getStart(),
                0.01);
        assertEquals(2.0, s.getPickupActivity().getTimeWindows().iterator().next().getEnd(), 0.01);
    }

    @Test
    public void whenAddingMultiplePickupTimeWindows_itShouldBeDoneCorrectly() {
        TimeWindow tw1 = TimeWindow.newInstance(1, 2);
        TimeWindow tw2 = TimeWindow.newInstance(4, 5);
        ShipmentJob s = new ShipmentJob.Builder("s").addPickupTimeWindow(tw1).addPickupTimeWindow(tw2)
                .setDeliveryLocation(TestUtils.loc("delLoc")).setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build()).build();
        assertEquals(s.getPickupActivity().getTimeWindows().size(), 2);
        assertThat(s.getPickupActivity().getTimeWindows(), hasItem(is(tw1)));
        assertThat(s.getPickupActivity().getTimeWindows(), hasItem(is(tw2)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenAddingMultipleOverlappingPickupTimeWindows_itShouldThrowException() {
        ShipmentJob s = new ShipmentJob.Builder("s")
                .addPickupTimeWindow(1, 3)
                .addPickupTimeWindow(2, 5)
                .setDeliveryLocation(TestUtils.loc("delLoc"))
                .setPickupLocation(Location.newInstance("pickLoc")).build();
    }


    @Test(expected = IllegalArgumentException.class)
    public void whenShipmentHasNegativeCapacityVal_throwIllegalStateExpception() {
        @SuppressWarnings("unused")
        ShipmentJob one = new ShipmentJob.Builder("s").setPickupLocation(Location.Builder.newInstance().setId("foo").build())
        .setDeliveryLocation(TestUtils.loc("foofoo"))
        .addSizeDimension(0, -2)
        .build();
    }

    @Test
    public void whenAddingTwoCapDimension_nuOfDimsShouldBeTwo() {
        ShipmentJob one = new ShipmentJob.Builder("s").setPickupLocation(Location.Builder.newInstance().setId("foo").build())
                .setDeliveryLocation(TestUtils.loc("foofoo"))
                .addSizeDimension(0, 2)
                .addSizeDimension(1, 4)
                .build();
        assertEquals(2, one.getSize().getNuOfDimensions());
    }

    @Test
    public void whenShipmentIsBuiltWithoutSpecifyingCapacity_itShouldHvCapWithOneDimAndDimValOfZero() {
        ShipmentJob one = new ShipmentJob.Builder("s")
                .setPickupLocation(Location.Builder.newInstance().setId("foo").setCoordinate(Coordinate.newInstance(0, 0)).build())
                .setDeliveryLocation(TestUtils.loc("foofoo")).build();
        assertEquals(1, one.getSize().getNuOfDimensions());
        assertEquals(0, one.getSize().get(0));
    }

    @Test
    public void whenShipmentIsBuiltWithConstructorWhereSizeIsSpecified_capacityShouldBeSetCorrectly() {
        ShipmentJob one = new ShipmentJob.Builder("s").addSizeDimension(0, 1)
                .setPickupLocation(Location.Builder.newInstance().setId("foo").setCoordinate(Coordinate.newInstance(0, 0)).build())
                .setDeliveryLocation(TestUtils.loc("foofoo")).build();
        assertEquals(1, one.getSize().getNuOfDimensions());
        assertEquals(1, one.getSize().get(0));
    }

    @Test
    public void whenAddingSkills_theyShouldBeAddedCorrectly() {
        ShipmentJob s = new ShipmentJob.Builder("s").setPickupLocation(Location.Builder.newInstance().setId("loc").build())
                .setDeliveryLocation(TestUtils.loc("delLoc"))
                .addRequiredSkill("drill").addRequiredSkill("screwdriver").build();
        assertTrue(s.getRequiredSkills().containsSkill("drill"));
        assertTrue(s.getRequiredSkills().containsSkill("drill"));
        assertTrue(s.getRequiredSkills().containsSkill("ScrewDriver"));
    }

    @Test
    public void whenAddingSkillsCaseSens_theyShouldBeAddedCorrectly() {
        ShipmentJob s = new ShipmentJob.Builder("s")
                .setPickupLocation(Location.Builder.newInstance().setId("pick").build())
                .setDeliveryLocation(TestUtils.loc("del"))
                .addRequiredSkill("DriLl").addRequiredSkill("screwDriver").build();
        assertTrue(s.getRequiredSkills().containsSkill("drill"));
        assertTrue(s.getRequiredSkills().containsSkill("drilL"));
    }

    @Test
    public void whenAddingSkillsCaseSensV2_theyShouldBeAddedCorrectly() {
        ShipmentJob s = new ShipmentJob.Builder("s").setPickupLocation(Location.Builder.newInstance().setId("loc").build())
                .setDeliveryLocation(TestUtils.loc("del"))
                .addRequiredSkill("screwDriver").build();
        assertFalse(s.getRequiredSkills().containsSkill("drill"));
        assertFalse(s.getRequiredSkills().containsSkill("drilL"));
    }

    @Test
    public void nameShouldBeAssigned() {
        ShipmentJob s = new ShipmentJob.Builder("s").setPickupLocation(Location.Builder.newInstance().setId("loc").build())
                .setDeliveryLocation(TestUtils.loc("del"))
                .setName("name").build();
        assertEquals("name", s.getName());
    }

    @Test
    public void whenSettingLocation_itShouldWork() {
        ShipmentJob s = new ShipmentJob.Builder("s").setPickupLocation(Location.Builder.newInstance().setId("loc").build())
                .setDeliveryLocation(Location.Builder.newInstance().setId("del").build()).build();
        assertEquals("loc", s.getPickupActivity().getLocation().getId());
        assertEquals("loc", s.getPickupActivity().getLocation().getId());
        assertEquals("del", s.getDeliveryActivity().getLocation().getId());
        assertEquals("del", s.getDeliveryActivity().getLocation().getId());
    }

    @Test
    public void whenSettingPriorities_itShouldBeSetCorrectly() {
        ShipmentJob s = new ShipmentJob.Builder("s").setPickupLocation(Location.newInstance("loc"))
                .setDeliveryLocation(Location.newInstance("loc"))
                .setPriority(1).build();
        assertEquals(1, s.getPriority());
    }

    @Test
    public void whenSettingPriorities_itShouldBeSetCorrectly2() {
        ShipmentJob s = new ShipmentJob.Builder("s").setPickupLocation(Location.newInstance("loc"))
                .setDeliveryLocation(Location.newInstance("loc"))
                .setPriority(3).build();
        assertEquals(3, s.getPriority());
    }

    @Test
    public void whenNotSettingPriorities_defaultShouldBe2() {
        ShipmentJob s = new ShipmentJob.Builder("s").setPickupLocation(Location.newInstance("loc"))
                .setDeliveryLocation(Location.newInstance("loc"))
                .build();
        assertEquals(2, s.getPriority());
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenSettingIncorrectPriorities_itShouldThrowException() {
        new ShipmentJob.Builder("s").setPickupLocation(Location.newInstance("loc"))
        .setDeliveryLocation(Location.newInstance("loc"))
        .setPriority(30).build();

    }

    @Test(expected = IllegalArgumentException.class)
    public void whenSettingIncorrectPriorities_itShouldThrowException2() {
        new ShipmentJob.Builder("s").setPickupLocation(Location.newInstance("loc"))
        .setDeliveryLocation(Location.newInstance("loc"))
        .setPriority(0).build();

    }

}
