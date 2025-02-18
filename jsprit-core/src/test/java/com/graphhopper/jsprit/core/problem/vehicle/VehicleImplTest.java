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

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.job.Break;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Vehicle Impl Test")
class VehicleImplTest {

    @Test
    @DisplayName("When Vehicle Is Built Without Setting Neither Location Nor Coord _ it Throws An Illegal State Exception")
    void whenVehicleIsBuiltWithoutSettingNeitherLocationNorCoord_itThrowsAnIllegalStateException() {
        assertThrows(IllegalArgumentException.class, () -> {
            @SuppressWarnings("unused")
            Vehicle v = VehicleImpl.Builder.newInstance("v").build();
        });
    }

    @Test
    @DisplayName("When Adding Driver Break _ it Should Be Added Correctly")
    void whenAddingDriverBreak_itShouldBeAddedCorrectly() {
        VehicleTypeImpl type1 = VehicleTypeImpl.Builder.newInstance("type").build();
        Break aBreak = Break.Builder.newInstance("break").setTimeWindow(TimeWindow.newInstance(100, 200)).setServiceTime(30).build();
        Vehicle v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance("start")).setType(type1).setEndLocation(Location.newInstance("start")).setBreak(aBreak).build();
        assertNotNull(v.getBreak());
        assertEquals(100., v.getBreak().getTimeWindow().getStart(), 0.1);
        assertEquals(200., v.getBreak().getTimeWindow().getEnd(), 0.1);
        assertEquals(30., v.getBreak().getServiceDuration(), 0.1);
    }

    @Test
    @DisplayName("Building A New Vehicle Based On Another One Should Work")
    void buildingANewVehicleBasedOnAnotherOneShouldWork() {
        VehicleTypeImpl type1 = VehicleTypeImpl.Builder.newInstance("type").build();
        Break aBreak = Break.Builder.newInstance("break").setTimeWindow(TimeWindow.newInstance(100, 200)).setServiceTime(30).build();
        Vehicle v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance("start")).setType(type1).setEndLocation(Location.newInstance("start")).setBreak(aBreak).build();
        Vehicle newVehicle = VehicleImpl.Builder.newInstance(v).setStartLocation(Location.newInstance("newStartLocation")).build();
        assertNotNull(newVehicle.getBreak());
        assertEquals(100., newVehicle.getBreak().getTimeWindow().getStart(), 0.1);
        assertEquals(200., newVehicle.getBreak().getTimeWindow().getEnd(), 0.1);
        assertEquals(30., newVehicle.getBreak().getServiceDuration(), 0.1);
        assertEquals(newVehicle.getStartLocation().getId(), "newStartLocation");
        assertEquals(type1, newVehicle.getType());
    }

    @Test
    @DisplayName("When Adding Skills _ they Should Be Added Correctly")
    void whenAddingSkills_theyShouldBeAddedCorrectly() {
        VehicleTypeImpl type1 = VehicleTypeImpl.Builder.newInstance("type").build();
        Vehicle v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance("start")).setType(type1).setEndLocation(Location.newInstance("start")).addSkill("drill").addSkill("screwdriver").build();
        assertTrue(v.getSkills().containsSkill("drill"));
        assertTrue(v.getSkills().containsSkill("drill"));
        assertTrue(v.getSkills().containsSkill("screwdriver"));
    }

    @Test
    @DisplayName("When Adding Skills Case Sens _ they Should Be Added Correctly")
    void whenAddingSkillsCaseSens_theyShouldBeAddedCorrectly() {
        VehicleTypeImpl type1 = VehicleTypeImpl.Builder.newInstance("type").build();
        Vehicle v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance("start")).setType(type1).setEndLocation(Location.newInstance("start")).addSkill("drill").addSkill("screwdriver").build();
        assertTrue(v.getSkills().containsSkill("drill"));
        assertTrue(v.getSkills().containsSkill("dRill"));
        assertTrue(v.getSkills().containsSkill("ScrewDriver"));
    }

    @Test
    @DisplayName("When Vehicle Is Built To Return To Depot _ it Should Return To Depot")
    void whenVehicleIsBuiltToReturnToDepot_itShouldReturnToDepot() {
        Vehicle v = VehicleImpl.Builder.newInstance("v").setReturnToDepot(true).setStartLocation(Location.newInstance("loc")).build();
        assertTrue(v.isReturnToDepot());
    }

    @Test
    @DisplayName("When Vehicle Is Built To Not Return To Depot _ it Should Not Return To Depot")
    void whenVehicleIsBuiltToNotReturnToDepot_itShouldNotReturnToDepot() {
        Vehicle v = VehicleImpl.Builder.newInstance("v").setReturnToDepot(false).setStartLocation(Location.newInstance("loc")).build();
        assertFalse(v.isReturnToDepot());
    }

    @Test
    @DisplayName("When Vehicle Is Built With Location _ it Should Hv The Correct Location")
    void whenVehicleIsBuiltWithLocation_itShouldHvTheCorrectLocation() {
        Vehicle v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance("loc")).build();
        assertEquals(v.getStartLocation().getId(), "loc");
    }

    @Test
    @DisplayName("When Vehicle Is Built With Coord _ it Should Hv The Correct Coord")
    void whenVehicleIsBuiltWithCoord_itShouldHvTheCorrectCoord() {
        Vehicle v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance(1, 2)).build();
        assertEquals(1.0, v.getStartLocation().getCoordinate().getX(), 0.01);
        assertEquals(2.0, v.getStartLocation().getCoordinate().getY(), 0.01);
    }

    @Test
    @DisplayName("When Vehicle Is Built And Earliest Start Is Not Set _ it Should Set The Default Of Zero")
    void whenVehicleIsBuiltAndEarliestStartIsNotSet_itShouldSetTheDefaultOfZero() {
        Vehicle v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance(1, 2)).build();
        assertEquals(0.0, v.getEarliestDeparture(), 0.01);
    }

    @Test
    @DisplayName("When Vehicle Is Built And Earliest Start Set _ it Should Be Set Correctly")
    void whenVehicleIsBuiltAndEarliestStartSet_itShouldBeSetCorrectly() {
        Vehicle v = VehicleImpl.Builder.newInstance("v").setEarliestStart(10.0).setStartLocation(Location.newInstance(1, 2)).build();
        assertEquals(10.0, v.getEarliestDeparture(), 0.01);
    }

    @Test
    @DisplayName("When Vehicle Is Built And Latest Arrival Is Not Set _ it Should Set Default Of Double Max Value")
    void whenVehicleIsBuiltAndLatestArrivalIsNotSet_itShouldSetDefaultOfDoubleMaxValue() {
        Vehicle v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance(1, 2)).build();
        assertEquals(Double.MAX_VALUE, v.getLatestArrival(), 0.01);
    }

    @Test
    @DisplayName("When Vehicle Is Built And Latest Arrival Is Set _ it Should Be Set Correctly")
    void whenVehicleIsBuiltAndLatestArrivalIsSet_itShouldBeSetCorrectly() {
        Vehicle v = VehicleImpl.Builder.newInstance("v").setLatestArrival(30.0).setStartLocation(Location.newInstance(1, 2)).build();
        assertEquals(30.0, v.getLatestArrival(), 0.01);
    }

    @Test
    @DisplayName("When No Vehicle Is Create _ it Should Hv The Correct Id")
    void whenNoVehicleIsCreate_itShouldHvTheCorrectId() {
        Vehicle v = VehicleImpl.createNoVehicle();
        assertEquals(v.getId(), "noVehicle");
    }

    @Test
    @DisplayName("When Start Location Is Set _ it Is Done Correctly")
    void whenStartLocationIsSet_itIsDoneCorrectly() {
        Vehicle v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance("startLoc")).build();
        assertEquals(v.getStartLocation().getId(), "startLoc");
    }

    @Test
    @DisplayName("When Start Location Is Null _ it Throws Exception")
    void whenStartLocationIsNull_itThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            @SuppressWarnings("unused")
            Vehicle v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance(null)).build();
        });
    }

    @Test
    @DisplayName("When Start Location Coord Is Set _ it Is Done Correctly")
    void whenStartLocationCoordIsSet_itIsDoneCorrectly() {
        Vehicle v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance(1, 2)).build();
        assertEquals(1.0, v.getStartLocation().getCoordinate().getX(), 0.01);
        assertEquals(2.0, v.getStartLocation().getCoordinate().getY(), 0.01);
    }

    @Test
    @DisplayName("When End Location Is Set _ it Is Done Correctly")
    void whenEndLocationIsSet_itIsDoneCorrectly() {
        Vehicle v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance("startLoc")).setEndLocation(Location.newInstance("endLoc")).build();
        assertEquals(v.getStartLocation().getId(), "startLoc");
        assertEquals(v.getEndLocation().getId(), "endLoc");
    }

    @Test
    @DisplayName("When End Location Coord Is Set _ it Is Done Correctly")
    void whenEndLocationCoordIsSet_itIsDoneCorrectly() {
        Vehicle v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance("startLoc")).setEndLocation(Location.newInstance(1, 2)).build();
        assertEquals(1.0, v.getEndLocation().getCoordinate().getX(), 0.01);
        assertEquals(2.0, v.getEndLocation().getCoordinate().getY(), 0.01);
    }

    @Test
    @DisplayName("When Neither End Location Id Nor End Location Coord Are Set _ end Location Id Must Be Equal To Start Location Id")
    void whenNeitherEndLocationIdNorEndLocationCoordAreSet_endLocationIdMustBeEqualToStartLocationId() {
        Vehicle v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance("startLoc")).build();
        assertEquals(v.getEndLocation().getId(), "startLoc");
    }

    @Test
    @DisplayName("When Neither End Location Id Nor End Location Coord Are Set _ end Location Coord Must Be Equal To Start Location Coord")
    void whenNeitherEndLocationIdNorEndLocationCoordAreSet_endLocationCoordMustBeEqualToStartLocationCoord() {
        Vehicle v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance("startLoc")).build();
        assertEquals(v.getEndLocation().getCoordinate(), v.getStartLocation().getCoordinate());
    }

    @Test
    @DisplayName("When Neither End Location Id Nor End Location Coord Are Set _ end Location Coord Must Be Equal To Start Location Coord V 2")
    void whenNeitherEndLocationIdNorEndLocationCoordAreSet_endLocationCoordMustBeEqualToStartLocationCoordV2() {
        Vehicle v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance(1.0, 2.0)).build();
        assertEquals(v.getEndLocation().getCoordinate(), v.getStartLocation().getCoordinate());
    }

    @Test
    @DisplayName("When End Location Coordinate Is Set But No Id _ id Must Be Coord To String")
    void whenEndLocationCoordinateIsSetButNoId_idMustBeCoordToString() {
        Vehicle v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance(1.0, 2.0)).setEndLocation(Location.newInstance(3.0, 4.0)).build();
        assertEquals(v.getEndLocation().getCoordinate().toString(), v.getEndLocation().getId());
    }

    @Test
    @DisplayName("When End Location Id Is Specified AND Return To Depot Is False _ it Should Throw Exception")
    void whenEndLocationIdIsSpecifiedANDReturnToDepotIsFalse_itShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            @SuppressWarnings("unused")
            Vehicle v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance(1.0, 2.0)).setEndLocation(Location.newInstance("endLoc")).setReturnToDepot(false).build();
        });
    }

    @Test
    @DisplayName("When End Location Coord Is Specified AND Return To Depot Is False _ it Should Throw Exception")
    void whenEndLocationCoordIsSpecifiedANDReturnToDepotIsFalse_itShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            @SuppressWarnings("unused")
            Vehicle v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance(1.0, 2.0)).setEndLocation(Location.newInstance(3, 4)).setReturnToDepot(false).build();
        });
    }

    @Test
    @DisplayName("When End Location Coord Is Not Specified AND Return To Depot Is False _ end Location Coord Must Be Start Location Coord")
    void whenEndLocationCoordIsNotSpecifiedANDReturnToDepotIsFalse_endLocationCoordMustBeStartLocationCoord() {
        Vehicle v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance(1.0, 2.0)).setReturnToDepot(false).build();
        assertEquals(v.getStartLocation().getCoordinate(), v.getEndLocation().getCoordinate());
    }

    @Test
    @DisplayName("When End Location Id Is Not Specified AND Return To Depot Is False _ end Location Id Must Be Start Location Id")
    void whenEndLocationIdIsNotSpecifiedANDReturnToDepotIsFalse_endLocationIdMustBeStartLocationId() {
        Vehicle v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance(1.0, 2.0)).setReturnToDepot(false).build();
        assertEquals(v.getStartLocation().getCoordinate().toString(), v.getEndLocation().getId());
    }

    @Test
    @DisplayName("When Start And End Are Unequal AND Return To Depot Is False _ it Should Throw Exception")
    void whenStartAndEndAreUnequalANDReturnToDepotIsFalse_itShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            @SuppressWarnings("unused")
            Vehicle v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance("start")).setEndLocation(Location.newInstance("end")).setReturnToDepot(false).build();
        });
    }

    @Test
    @DisplayName("When Start And End Are Equal AND Return To Depot Is False _ it Should Throw Exception")
    void whenStartAndEndAreEqualANDReturnToDepotIsFalse_itShouldThrowException() {
        @SuppressWarnings("unused")
        Vehicle v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance("start")).setEndLocation(Location.newInstance("start")).setReturnToDepot(false).build();
        assertTrue(true);
    }

    @Test
    @DisplayName("When Two Vehicles Have The Same Id _ they Should Be Equal")
    void whenTwoVehiclesHaveTheSameId_theyShouldBeEqual() {
        Vehicle v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance("start")).setEndLocation(Location.newInstance("start")).setReturnToDepot(false).build();
        Vehicle v2 = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance("start")).setEndLocation(Location.newInstance("start")).setReturnToDepot(false).build();
        assertTrue(v.equals(v2));
    }

    @Test
    @DisplayName("When Adding Skills Case Sens V 2 _ they Should Be Added Correctly")
    void whenAddingSkillsCaseSensV2_theyShouldBeAddedCorrectly() {
        VehicleTypeImpl type1 = VehicleTypeImpl.Builder.newInstance("type").build();
        Vehicle v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance("start")).setType(type1).setEndLocation(Location.newInstance("start")).addSkill("drill").build();
        assertFalse(v.getSkills().containsSkill("ScrewDriver"));
    }

    @Test
    @DisplayName("When Setting User Data _ it Is Associated With The Vehicle")
    void whenSettingUserData_itIsAssociatedWithTheVehicle() {
        VehicleTypeImpl type1 = VehicleTypeImpl.Builder.newInstance("type").build();
        Vehicle one = VehicleImpl.Builder.newInstance("v").setType(type1).setStartLocation(Location.newInstance("start")).setUserData(new HashMap<String, Object>()).build();
        Vehicle two = VehicleImpl.Builder.newInstance("v").setType(type1).setStartLocation(Location.newInstance("start")).setUserData(42).build();
        Vehicle three = VehicleImpl.Builder.newInstance("v").setType(type1).setStartLocation(Location.newInstance("start")).build();
        assertTrue(one.getUserData() instanceof Map);
        assertEquals(42, two.getUserData());
        assertNull(three.getUserData());
    }
}
