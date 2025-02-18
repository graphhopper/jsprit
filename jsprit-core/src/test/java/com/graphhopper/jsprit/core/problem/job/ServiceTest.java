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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Service Test")
class ServiceTest {

    @Test
    @DisplayName("When Two Services Have The Same Id _ their References Should Be Un Equal")
    void whenTwoServicesHaveTheSameId_theirReferencesShouldBeUnEqual() {
        Service one = Service.Builder.newInstance("service").addSizeDimension(0, 10).setLocation(Location.newInstance("foo")).build();
        Service two = Service.Builder.newInstance("service").addSizeDimension(0, 10).setLocation(Location.newInstance("fo")).build();
        assertTrue(one != two);
    }

    @Test
    @DisplayName("When Two Services Have The Same Id _ they Should Be Equal")
    void whenTwoServicesHaveTheSameId_theyShouldBeEqual() {
        Service one = Service.Builder.newInstance("service").addSizeDimension(0, 10).setLocation(Location.newInstance("foo")).build();
        Service two = Service.Builder.newInstance("service").addSizeDimension(0, 10).setLocation(Location.newInstance("fo")).build();
        assertTrue(one.equals(two));
    }

    @Test
    @DisplayName("No Name")
    void noName() {
        Set<Service> serviceSet = new HashSet<Service>();
        Service one = Service.Builder.newInstance("service").addSizeDimension(0, 10).setLocation(Location.newInstance("foo")).build();
        Service two = Service.Builder.newInstance("service").addSizeDimension(0, 10).setLocation(Location.newInstance("fo")).build();
        serviceSet.add(one);
        // assertTrue(serviceSet.contains(two));
        serviceSet.remove(two);
        assertTrue(serviceSet.isEmpty());
    }

    @Test
    @DisplayName("When Capacity Dim Value Is Negative _ throw Illegal State Expception")
    void whenCapacityDimValueIsNegative_throwIllegalStateExpception() {
        assertThrows(IllegalArgumentException.class, () -> {
            @SuppressWarnings("unused")
            Service s = Service.Builder.newInstance("s").setLocation(Location.newInstance("foo")).addSizeDimension(0, -10).build();
        });
    }

    @Test
    @DisplayName("When Adding Two Cap Dimension _ nu Of Dims Should Be Two")
    void whenAddingTwoCapDimension_nuOfDimsShouldBeTwo() {
        Service one = Service.Builder.newInstance("s").setLocation(Location.newInstance("foofoo")).addSizeDimension(0, 2).addSizeDimension(1, 4).build();
        assertEquals(2, one.getSize().getNuOfDimensions());
    }

    @Test
    @DisplayName("When Shipment Is Built Without Specifying Capacity _ it Should Hv Cap With One Dim And Dim Val Of Zero")
    void whenShipmentIsBuiltWithoutSpecifyingCapacity_itShouldHvCapWithOneDimAndDimValOfZero() {
        Service one = Service.Builder.newInstance("s").setLocation(Location.newInstance("foofoo")).build();
        assertEquals(1, one.getSize().getNuOfDimensions());
        assertEquals(0, one.getSize().get(0));
    }

    @Test
    @DisplayName("When Shipment Is Built With Constructor Where Size Is Specified _ capacity Should Be Set Correctly")
    void whenShipmentIsBuiltWithConstructorWhereSizeIsSpecified_capacityShouldBeSetCorrectly() {
        Service one = Service.Builder.newInstance("s").addSizeDimension(0, 1).setLocation(Location.newInstance("foofoo")).build();
        assertEquals(1, one.getSize().getNuOfDimensions());
        assertEquals(1, one.getSize().get(0));
    }

    @Test
    @DisplayName("When Calling For New Instance Of Builder _ it Should Return Builder Correctly")
    void whenCallingForNewInstanceOfBuilder_itShouldReturnBuilderCorrectly() {
        Service.Builder builder = Service.Builder.newInstance("s");
        assertNotNull(builder);
    }

    @Test
    @DisplayName("When Setting No Type _ it Should Return _ service")
    void whenSettingNoType_itShouldReturn_service() {
        Service s = Service.Builder.newInstance("s").setLocation(Location.newInstance("loc")).build();
        assertEquals(s.getType(), "service");
    }

    @Test
    @DisplayName("When Setting Location _ it Should Be Set Correctly")
    void whenSettingLocation_itShouldBeSetCorrectly() {
        Service s = Service.Builder.newInstance("s").setLocation(Location.newInstance("loc")).build();
        assertEquals(s.getLocation().getId(), "loc");
        assertEquals(s.getLocation().getId(), "loc");
    }

    @Test
    @DisplayName("When Setting Location _ it Should Work")
    void whenSettingLocation_itShouldWork() {
        Service s = Service.Builder.newInstance("s").setLocation(Location.Builder.newInstance().setId("loc").build()).build();
        assertEquals(s.getLocation().getId(), "loc");
        assertEquals(s.getLocation().getId(), "loc");
    }

    @Test
    @DisplayName("When Setting Location Coord _ it Should Be Set Correctly")
    void whenSettingLocationCoord_itShouldBeSetCorrectly() {
        Service s = Service.Builder.newInstance("s").setLocation(Location.newInstance(1, 2)).build();
        assertEquals(1.0, s.getLocation().getCoordinate().getX(), 0.01);
        assertEquals(2.0, s.getLocation().getCoordinate().getY(), 0.01);
        assertEquals(1.0, s.getLocation().getCoordinate().getX(), 0.01);
        assertEquals(2.0, s.getLocation().getCoordinate().getY(), 0.01);
    }

    // @Test(expected = IllegalArgumentException.class)
    // public void whenSettingNeitherLocationIdNorCoord_throwsException() {
    // @SuppressWarnings("unused")
    // Service s = Service.Builder.newInstance("s").build();
    // }
    @Test
    @DisplayName("When Service Time Smaller Zero _ throw Illegal State Exception")
    void whenServiceTimeSmallerZero_throwIllegalStateException() {
        assertThrows(IllegalArgumentException.class, () -> {
            @SuppressWarnings("unused")
            Service s = Service.Builder.newInstance("s").setLocation(Location.newInstance("loc")).setServiceTime(-1).build();
        });
    }

    @Test
    @DisplayName("When Setting Service Time _ it Should Be Set Correctly")
    void whenSettingServiceTime_itShouldBeSetCorrectly() {
        Service s = Service.Builder.newInstance("s").setLocation(Location.newInstance("loc")).setServiceTime(1).build();
        assertEquals(1.0, s.getServiceDuration(), 0.01);
    }

    @Test
    @DisplayName("When Time Window Is Null _ throw Exception")
    void whenTimeWindowIsNull_throwException() {
        assertThrows(IllegalArgumentException.class, () -> {
            @SuppressWarnings("unused")
            Service s = Service.Builder.newInstance("s").setLocation(Location.newInstance("loc")).setTimeWindow(null).build();
        });
    }

    @Test
    @DisplayName("When Setting Time Window _ it Should Be Set Correctly")
    void whenSettingTimeWindow_itShouldBeSetCorrectly() {
        Service s = Service.Builder.newInstance("s").setLocation(Location.newInstance("loc")).setTimeWindow(TimeWindow.newInstance(1.0, 2.0)).build();
        assertEquals(1.0, s.getTimeWindow().getStart(), 0.01);
        assertEquals(2.0, s.getTimeWindow().getEnd(), 0.01);
    }

    @Test
    @DisplayName("When Adding Skills _ they Should Be Added Correctly")
    void whenAddingSkills_theyShouldBeAddedCorrectly() {
        Service s = Service.Builder.newInstance("s").setLocation(Location.newInstance("loc")).addRequiredSkill("drill").addRequiredSkill("screwdriver").build();
        assertTrue(s.getRequiredSkills().containsSkill("drill"));
        assertTrue(s.getRequiredSkills().containsSkill("drill"));
        assertTrue(s.getRequiredSkills().containsSkill("ScrewDriver"));
    }

    @Test
    @DisplayName("When Adding Skills Case Sens _ they Should Be Added Correctly")
    void whenAddingSkillsCaseSens_theyShouldBeAddedCorrectly() {
        Service s = Service.Builder.newInstance("s").setLocation(Location.newInstance("loc")).addRequiredSkill("DriLl").addRequiredSkill("screwDriver").build();
        assertTrue(s.getRequiredSkills().containsSkill("drill"));
        assertTrue(s.getRequiredSkills().containsSkill("drilL"));
    }

    @Test
    @DisplayName("When Adding Several Time Windows _ it Should Be Set Correctly")
    void whenAddingSeveralTimeWindows_itShouldBeSetCorrectly() {
        TimeWindow tw1 = TimeWindow.newInstance(1.0, 2.0);
        TimeWindow tw2 = TimeWindow.newInstance(3.0, 5.0);
        Service s = Service.Builder.newInstance("s").setLocation(Location.newInstance("loc")).addTimeWindow(tw1).addTimeWindow(tw2).build();
        assertEquals(2, s.getTimeWindows().size());
        assertThat(s.getTimeWindows(), hasItem(is(tw1)));
        assertThat(s.getTimeWindows(), hasItem(is(tw2)));
    }

    @Test
    @DisplayName("When Adding Time Window _ it Should Be Set Correctly")
    void whenAddingTimeWindow_itShouldBeSetCorrectly() {
        Service s = Service.Builder.newInstance("s").setLocation(Location.newInstance("loc")).addTimeWindow(TimeWindow.newInstance(1.0, 2.0)).build();
        assertEquals(1.0, s.getTimeWindow().getStart(), 0.01);
        assertEquals(2.0, s.getTimeWindow().getEnd(), 0.01);
    }

    @Test
    @DisplayName("When Adding Skills Case Sens V 2 _ they Should Be Added Correctly")
    void whenAddingSkillsCaseSensV2_theyShouldBeAddedCorrectly() {
        Service s = Service.Builder.newInstance("s").setLocation(Location.newInstance("loc")).addRequiredSkill("screwDriver").build();
        assertFalse(s.getRequiredSkills().containsSkill("drill"));
        assertFalse(s.getRequiredSkills().containsSkill("drilL"));
    }

    @Test
    @DisplayName("Name Should Be Assigned")
    void nameShouldBeAssigned() {
        Service s = Service.Builder.newInstance("s").setLocation(Location.newInstance("loc")).setName("name").build();
        assertEquals(s.getName(), "name");
    }

    @Test
    @DisplayName("Should Know Multiple Time Windows")
    void shouldKnowMultipleTimeWindows() {
        Service s = Service.Builder.newInstance("s").setLocation(Location.newInstance("loc")).addTimeWindow(TimeWindow.newInstance(0., 10.)).addTimeWindow(TimeWindow.newInstance(20., 30.)).setName("name").build();
        assertEquals(2, s.getTimeWindows().size());
    }

    @Test
    @DisplayName("When Multiple TW Overlap _ throw Ex")
    void whenMultipleTWOverlap_throwEx() {
        assertThrows(IllegalArgumentException.class, () -> {
            Service s = Service.Builder.newInstance("s").setLocation(Location.newInstance("loc")).addTimeWindow(TimeWindow.newInstance(0., 10.)).addTimeWindow(TimeWindow.newInstance(5., 30.)).setName("name").build();
        });
    }

    @Test
    @DisplayName("When Multiple TW Overlap 2 _ throw Ex")
    void whenMultipleTWOverlap2_throwEx() {
        assertThrows(IllegalArgumentException.class, () -> {
            Service s = Service.Builder.newInstance("s").setLocation(Location.newInstance("loc")).addTimeWindow(TimeWindow.newInstance(20., 30.)).addTimeWindow(TimeWindow.newInstance(0., 25.)).setName("name").build();
        });
    }

    @Test
    @DisplayName("When Setting Priorities _ it Should Be Set Correctly")
    void whenSettingPriorities_itShouldBeSetCorrectly() {
        Service s = Service.Builder.newInstance("s").setLocation(Location.newInstance("loc")).setPriority(1).build();
        assertEquals(1, s.getPriority());
    }

    @Test
    @DisplayName("When Setting Priorities _ it Should Be Set Correctly 2")
    void whenSettingPriorities_itShouldBeSetCorrectly2() {
        Service s = Service.Builder.newInstance("s").setLocation(Location.newInstance("loc")).setPriority(3).build();
        assertEquals(3, s.getPriority());
    }

    @Test
    @DisplayName("When Setting Priorities _ it Should Be Set Correctly 3")
    void whenSettingPriorities_itShouldBeSetCorrectly3() {
        Service s = Service.Builder.newInstance("s").setLocation(Location.newInstance("loc")).setPriority(10).build();
        assertEquals(10, s.getPriority());
    }

    @Test
    @DisplayName("When Not Setting Priorities _ default Should Be 2")
    void whenNotSettingPriorities_defaultShouldBe2() {
        Service s = Service.Builder.newInstance("s").setLocation(Location.newInstance("loc")).build();
        assertEquals(2, s.getPriority());
    }

    @Test
    @DisplayName("When Setting Incorrect Priorities _ it Should Throw Exception")
    void whenSettingIncorrectPriorities_itShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            Service s = Service.Builder.newInstance("s").setLocation(Location.newInstance("loc")).setPriority(30).build();
        });
    }

    @Test
    @DisplayName("When Setting Incorrect Priorities _ it Should Throw Exception 2")
    void whenSettingIncorrectPriorities_itShouldThrowException2() {
        assertThrows(IllegalArgumentException.class, () -> {
            Service s = Service.Builder.newInstance("s").setLocation(Location.newInstance("loc")).setPriority(0).build();
        });
    }

    @Test
    @DisplayName("When Adding Max Time In Vehicle _ it Should Throw Ex")
    void whenAddingMaxTimeInVehicle_itShouldThrowEx() {
        assertThrows(UnsupportedOperationException.class, () -> {
            Service s = Service.Builder.newInstance("s").setLocation(Location.newInstance("loc")).setMaxTimeInVehicle(10).build();
        });
    }

    @Test
    @DisplayName("When Not Adding Max Time In Vehicle _ it Should Be Default")
    void whenNotAddingMaxTimeInVehicle_itShouldBeDefault() {
        Service s = Service.Builder.newInstance("s").setLocation(Location.newInstance("loc")).build();
        assertEquals(Double.MAX_VALUE, s.getMaxTimeInVehicle(), 0.001);
    }

    @Test
    @DisplayName("When Setting User Data _ it Is Associated With The Job")
    void whenSettingUserData_itIsAssociatedWithTheJob() {
        Service one = Service.Builder.newInstance("s").setLocation(Location.newInstance("loc")).setUserData(new HashMap<String, Object>()).build();
        Service two = Service.Builder.newInstance("s2").setLocation(Location.newInstance("loc")).setUserData(42).build();
        Service three = Service.Builder.newInstance("s3").setLocation(Location.newInstance("loc")).build();
        assertTrue(one.getUserData() instanceof Map);
        assertEquals(42, two.getUserData());
        assertNull(three.getUserData());
    }

    @Test
    @DisplayName("Test Service Activity")
    void testServiceActivity() {
        Service one = Service.Builder.newInstance("s").setLocation(Location.newInstance("loc")).build();
        assertEquals(1, one.getActivities().size());
        assertEquals(Activity.Type.SERVICE, one.getActivities().get(0).getActivityType());
    }
}
