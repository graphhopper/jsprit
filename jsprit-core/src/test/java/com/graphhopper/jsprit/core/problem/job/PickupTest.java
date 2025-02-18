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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pickup Test")
class PickupTest {

    @Test
    @DisplayName("When Neither Location Id Nor Coord Is Set _ it Throws Exception")
    void whenNeitherLocationIdNorCoordIsSet_itThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            Pickup.Builder.newInstance("p").build();
        });
    }

    @Test
    @DisplayName("When Adding Two Cap Dimension _ nu Of Dims Should Be Two")
    void whenAddingTwoCapDimension_nuOfDimsShouldBeTwo() {
        Pickup one = Pickup.Builder.newInstance("s").setLocation(Location.newInstance("foofoo")).addSizeDimension(0, 2).addSizeDimension(1, 4).build();
        assertEquals(2, one.getSize().getNuOfDimensions());
        assertEquals(2, one.getSize().get(0));
        assertEquals(4, one.getSize().get(1));
    }

    @Test
    @DisplayName("When Pickup Is Built Without Specifying Capacity _ it Should Hv Cap With One Dim And Dim Val Of Zero")
    void whenPickupIsBuiltWithoutSpecifyingCapacity_itShouldHvCapWithOneDimAndDimValOfZero() {
        Pickup one = Pickup.Builder.newInstance("s").setLocation(Location.newInstance("foofoo")).build();
        assertEquals(1, one.getSize().getNuOfDimensions());
        assertEquals(0, one.getSize().get(0));
    }

    @Test
    @DisplayName("When Pickup Is Built With Constructor Where Size Is Specified _ capacity Should Be Set Correctly")
    void whenPickupIsBuiltWithConstructorWhereSizeIsSpecified_capacityShouldBeSetCorrectly() {
        Pickup one = Pickup.Builder.newInstance("s").addSizeDimension(0, 1).setLocation(Location.newInstance("foofoo")).build();
        assertEquals(1, one.getSize().getNuOfDimensions());
        assertEquals(1, one.getSize().get(0));
    }

    @Test
    @DisplayName("When Adding Skills _ they Should Be Added Correctly")
    void whenAddingSkills_theyShouldBeAddedCorrectly() {
        Pickup s = Pickup.Builder.newInstance("s").setLocation(Location.newInstance("loc")).addRequiredSkill("drill").addRequiredSkill("screwdriver").build();
        assertTrue(s.getRequiredSkills().containsSkill("drill"));
        assertTrue(s.getRequiredSkills().containsSkill("drill"));
        assertTrue(s.getRequiredSkills().containsSkill("ScrewDriver"));
    }

    @Test
    @DisplayName("When Adding Skills Case Sens _ they Should Be Added Correctly")
    void whenAddingSkillsCaseSens_theyShouldBeAddedCorrectly() {
        Pickup s = Pickup.Builder.newInstance("s").setLocation(Location.newInstance("loc")).addRequiredSkill("DriLl").addRequiredSkill("screwDriver").build();
        assertTrue(s.getRequiredSkills().containsSkill("drill"));
        assertTrue(s.getRequiredSkills().containsSkill("drilL"));
    }

    @Test
    @DisplayName("When Adding Skills Case Sens V 2 _ they Should Be Added Correctly")
    void whenAddingSkillsCaseSensV2_theyShouldBeAddedCorrectly() {
        Pickup s = Pickup.Builder.newInstance("s").setLocation(Location.newInstance("loc")).addRequiredSkill("screwDriver").build();
        assertFalse(s.getRequiredSkills().containsSkill("drill"));
        assertFalse(s.getRequiredSkills().containsSkill("drilL"));
    }

    @Test
    @DisplayName("Name Should Be Assigned")
    void nameShouldBeAssigned() {
        Pickup s = Pickup.Builder.newInstance("s").setLocation(Location.newInstance("loc")).setName("name").build();
        assertEquals(s.getName(), "name");
    }

    @Test
    @DisplayName("When Setting Priorities _ it Should Be Set Correctly")
    void whenSettingPriorities_itShouldBeSetCorrectly() {
        Pickup s = Pickup.Builder.newInstance("s").setLocation(Location.newInstance("loc")).setPriority(3).build();
        assertEquals(3, s.getPriority());
    }

    @Test
    @DisplayName("When Not Setting Priorities _ default Should Be")
    void whenNotSettingPriorities_defaultShouldBe() {
        Pickup s = Pickup.Builder.newInstance("s").setLocation(Location.newInstance("loc")).build();
        assertEquals(2, s.getPriority());
    }

    @Test
    @DisplayName("When Setting User Data _ it Is Associated With The Job")
    void whenSettingUserData_itIsAssociatedWithTheJob() {
        Pickup one = Pickup.Builder.newInstance("s").setLocation(Location.newInstance("loc")).setUserData(new HashMap<String, Object>()).build();
        Pickup two = Pickup.Builder.newInstance("s2").setLocation(Location.newInstance("loc")).setUserData(42).build();
        Pickup three = Pickup.Builder.newInstance("s3").setLocation(Location.newInstance("loc")).build();
        assertTrue(one.getUserData() instanceof Map);
        assertEquals(42, two.getUserData());
        assertNull(three.getUserData());
    }

    @Test
    @DisplayName("When Adding Max Time In Vehicle _ it Should Throw Ex")
    void whenAddingMaxTimeInVehicle_itShouldThrowEx() {
        assertThrows(UnsupportedOperationException.class, () -> {
            Pickup s = Pickup.Builder.newInstance("s").setLocation(Location.newInstance("loc")).setMaxTimeInVehicle(10).build();
        });
    }

    @Test
    @DisplayName("When Not Adding Max Time In Vehicle _ it Should Be Default")
    void whenNotAddingMaxTimeInVehicle_itShouldBeDefault() {
        Pickup s = Pickup.Builder.newInstance("s").setLocation(Location.newInstance("loc")).build();
        assertEquals(Double.MAX_VALUE, s.getMaxTimeInVehicle(), 0.001);
    }
}
