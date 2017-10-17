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


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.job.Break;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow;


public class VehicleImplTest {


    @Test(expected = IllegalArgumentException.class)
    public void whenVehicleIsBuiltWithoutSettingNeitherLocationNorCoord_itThrowsAnIllegalStateException() {
        @SuppressWarnings("unused")
        Vehicle v = VehicleImpl.Builder.newInstance("v").build();
    }


    @Test
    public void whenAddingDriverBreak_itShouldBeAddedCorrectly() {
        VehicleTypeImpl type1 = VehicleTypeImpl.Builder.newInstance("type").build();
        Break aBreak = new Break.Builder("break").setTimeWindow(TimeWindow.newInstance(100, 200)).setServiceTime(30).build();
        Vehicle v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance("start"))
                .setType(type1).setEndLocation(Location.newInstance("start"))
                .setBreak(aBreak).build();
        assertNotNull(v.getBreak());
        TimeWindow timeWindow = v.getBreak().getActivity().getBreakTimeWindow();
        assertEquals(100., timeWindow.getStart(), 0.1);
        assertEquals(200., timeWindow.getEnd(), 0.1);
        assertEquals(30., v.getBreak().getActivity().getOperationTime(), 0.1);
    }


    @Test
    public void whenAddingSkills_theyShouldBeAddedCorrectly() {
        VehicleTypeImpl type1 = VehicleTypeImpl.Builder.newInstance("type").build();
        Vehicle v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance("start")).setType(type1).setEndLocation(Location.newInstance("start"))
                .addSkill("drill").addSkill("screwdriver").build();
        assertTrue(v.getSkills().containsSkill("drill"));
        assertTrue(v.getSkills().containsSkill("drill"));
        assertTrue(v.getSkills().containsSkill("screwdriver"));
    }

    @Test
    public void whenAddingSkillsCaseSens_theyShouldBeAddedCorrectly() {
        VehicleTypeImpl type1 = VehicleTypeImpl.Builder.newInstance("type").build();
        Vehicle v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance("start")).setType(type1).setEndLocation(Location.newInstance("start"))
                .addSkill("drill").addSkill("screwdriver").build();
        assertTrue(v.getSkills().containsSkill("drill"));
        assertTrue(v.getSkills().containsSkill("dRill"));
        assertTrue(v.getSkills().containsSkill("ScrewDriver"));
    }


    @Test
    public void whenVehicleIsBuiltToReturnToDepot_itShouldReturnToDepot() {
        Vehicle v = VehicleImpl.Builder.newInstance("v").setReturnToDepot(true).setStartLocation(Location.newInstance("loc")).build();
        assertTrue(v.isReturnToDepot());
    }

    @Test
    public void whenVehicleIsBuiltToNotReturnToDepot_itShouldNotReturnToDepot() {
        Vehicle v = VehicleImpl.Builder.newInstance("v").setReturnToDepot(false).setStartLocation(Location.newInstance("loc")).build();
        assertFalse(v.isReturnToDepot());
    }

    @Test
    public void whenVehicleIsBuiltWithLocation_itShouldHvTheCorrectLocation() {
        Vehicle v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance("loc")).build();
        assertEquals("loc", v.getStartLocation().getId());
    }

    @Test
    public void whenVehicleIsBuiltWithCoord_itShouldHvTheCorrectCoord() {
        Vehicle v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance(1, 2)).build();
        assertEquals(1.0, v.getStartLocation().getCoordinate().getX(), 0.01);
        assertEquals(2.0, v.getStartLocation().getCoordinate().getY(), 0.01);
    }

    @Test
    public void whenVehicleIsBuiltAndEarliestStartIsNotSet_itShouldSetTheDefaultOfZero() {
        Vehicle v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance(1, 2)).build();
        assertEquals(0.0, v.getEarliestDeparture(), 0.01);
    }

    @Test
    public void whenVehicleIsBuiltAndEarliestStartSet_itShouldBeSetCorrectly() {
        Vehicle v = VehicleImpl.Builder.newInstance("v").setEarliestStart(10.0).setStartLocation(Location.newInstance(1, 2)).build();
        assertEquals(10.0, v.getEarliestDeparture(), 0.01);
    }

    @Test
    public void whenVehicleIsBuiltAndLatestArrivalIsNotSet_itShouldSetDefaultOfDoubleMaxValue() {
        Vehicle v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance(1, 2)).build();
        assertEquals(Double.MAX_VALUE, v.getLatestArrival(), 0.01);
    }

    @Test
    public void whenVehicleIsBuiltAndLatestArrivalIsSet_itShouldBeSetCorrectly() {
        Vehicle v = VehicleImpl.Builder.newInstance("v").setLatestArrival(30.0).setStartLocation(Location.newInstance(1, 2)).build();
        assertEquals(30.0, v.getLatestArrival(), 0.01);
    }

    @Test
    public void whenNoVehicleIsCreate_itShouldHvTheCorrectId() {
        Vehicle v = VehicleImpl.createNoVehicle();
        assertEquals("noVehicle", v.getId());
    }

    @Test
    public void whenStartLocationIsSet_itIsDoneCorrectly() {
        Vehicle v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance("startLoc")).build();
        assertEquals("startLoc", v.getStartLocation().getId());
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenStartLocationIsNull_itThrowsException() {
        @SuppressWarnings("unused")
        Vehicle v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance(null)).build();
    }

    @Test
    public void whenStartLocationCoordIsSet_itIsDoneCorrectly() {
        Vehicle v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance(1, 2)).build();
        assertEquals(1.0, v.getStartLocation().getCoordinate().getX(), 0.01);
        assertEquals(2.0, v.getStartLocation().getCoordinate().getY(), 0.01);
    }

    @Test
    public void whenEndLocationIsSet_itIsDoneCorrectly() {
        Vehicle v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance("startLoc")).setEndLocation(Location.newInstance("endLoc")).build();
        assertEquals("startLoc", v.getStartLocation().getId());
        assertEquals("endLoc", v.getEndLocation().getId());
    }

    @Test
    public void whenEndLocationCoordIsSet_itIsDoneCorrectly() {
        Vehicle v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance("startLoc")).setEndLocation(Location.newInstance(1, 2)).build();
        assertEquals(1.0, v.getEndLocation().getCoordinate().getX(), 0.01);
        assertEquals(2.0, v.getEndLocation().getCoordinate().getY(), 0.01);
    }


    @Test
    public void whenNeitherEndLocationIdNorEndLocationCoordAreSet_endLocationIdMustBeEqualToStartLocationId() {
        Vehicle v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance("startLoc")).build();
        assertEquals("startLoc", v.getEndLocation().getId());
    }

    @Test
    public void whenNeitherEndLocationIdNorEndLocationCoordAreSet_endLocationCoordMustBeEqualToStartLocationCoord() {
        Vehicle v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance("startLoc")).build();
        assertEquals(v.getEndLocation().getCoordinate(), v.getStartLocation().getCoordinate());
    }

    @Test
    public void whenNeitherEndLocationIdNorEndLocationCoordAreSet_endLocationCoordMustBeEqualToStartLocationCoordV2() {
        Vehicle v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance(1.0, 2.0)).build();
        assertEquals(v.getEndLocation().getCoordinate(), v.getStartLocation().getCoordinate());
    }

    @Test
    public void whenEndLocationCoordinateIsSetButNoId_idMustBeCoordToString() {
        Vehicle v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance(1.0, 2.0)).setEndLocation(Location.newInstance(3.0, 4.0)).build();
        assertEquals(v.getEndLocation().getCoordinate().toString(), v.getEndLocation().getId());
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenEndLocationIdIsSpecifiedANDReturnToDepotIsFalse_itShouldThrowException() {
        @SuppressWarnings("unused")
        Vehicle v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance(1.0, 2.0)).setEndLocation(Location.newInstance("endLoc")).setReturnToDepot(false).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenEndLocationCoordIsSpecifiedANDReturnToDepotIsFalse_itShouldThrowException() {
        @SuppressWarnings("unused")
        Vehicle v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance(1.0, 2.0)).setEndLocation(Location.newInstance(3, 4)).setReturnToDepot(false).build();
    }

    @Test
    public void whenEndLocationCoordIsNotSpecifiedANDReturnToDepotIsFalse_endLocationCoordMustBeStartLocationCoord() {
        Vehicle v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance(1.0, 2.0)).setReturnToDepot(false).build();
        assertEquals(v.getStartLocation().getCoordinate(), v.getEndLocation().getCoordinate());
    }

    @Test
    public void whenEndLocationIdIsNotSpecifiedANDReturnToDepotIsFalse_endLocationIdMustBeStartLocationId() {
        Vehicle v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance(1.0, 2.0)).setReturnToDepot(false).build();
        assertEquals(v.getStartLocation().getCoordinate().toString(), v.getEndLocation().getId());
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenStartAndEndAreUnequalANDReturnToDepotIsFalse_itShouldThrowException() {
        @SuppressWarnings("unused")
        Vehicle v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance("start")).setEndLocation(Location.newInstance("end")).setReturnToDepot(false).build();
    }

    @Test
    public void whenStartAndEndAreEqualANDReturnToDepotIsFalse_itShouldThrowException() {
        @SuppressWarnings("unused")
        Vehicle v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance("start")).setEndLocation(Location.newInstance("start")).setReturnToDepot(false).build();
        assertTrue(true);
    }

    @Test
    public void whenTwoVehiclesHaveTheSameId_theyShouldBeEqual() {
        Vehicle v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance("start")).setEndLocation(Location.newInstance("start")).setReturnToDepot(false).build();
        Vehicle v2 = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance("start")).setEndLocation(Location.newInstance("start")).setReturnToDepot(false).build();
        assertTrue(v.equals(v2));
    }


    @Test
    public void whenAddingSkillsCaseSensV2_theyShouldBeAddedCorrectly() {
        VehicleTypeImpl type1 = VehicleTypeImpl.Builder.newInstance("type").build();
        Vehicle v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance("start")).setType(type1).setEndLocation(Location.newInstance("start"))
                .addSkill("drill").build();
        assertFalse(v.getSkills().containsSkill("ScrewDriver"));
    }


}
