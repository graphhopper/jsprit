/*******************************************************************************
 * Copyright (C) 2014  Stefan Schroeder
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package jsprit.core.problem.job;

import jsprit.core.problem.Location;
import jsprit.core.problem.solution.route.activity.TimeWindow;
import jsprit.core.util.Coordinate;
import jsprit.core.util.TestUtils;
import org.junit.Test;

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

    @Test(expected = IllegalStateException.class)
    public void whenNeitherPickupLocationIdNorPickupCoord_itThrowsException() {
        @SuppressWarnings("unused")
        Shipment s = Shipment.Builder.newInstance("s").setDeliveryLocation(TestUtils.loc("delLoc")).build();
    }

    @Test(expected = IllegalStateException.class)
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

    @Test(expected = IllegalStateException.class)
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
}
