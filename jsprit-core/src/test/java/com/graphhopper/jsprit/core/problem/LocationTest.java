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
package com.graphhopper.jsprit.core.problem;

import com.graphhopper.jsprit.core.util.Coordinate;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by schroeder on 16.12.14.
 */
@DisplayName("Location Test")
class LocationTest {

    @Test
    @DisplayName("When Index Set _ build Location")
    void whenIndexSet_buildLocation() {
        Location l = Location.Builder.newInstance().setIndex(1).build();
        Assertions.assertEquals(1, l.getIndex());
        Assertions.assertTrue(true);
    }

    @Test
    @DisplayName("When Name Set _ build Location")
    void whenNameSet_buildLocation() {
        Location l = Location.Builder.newInstance().setName("mystreet 6a").setIndex(1).build();
        Assertions.assertEquals(l.getName(), "mystreet 6a");
    }

    @Test
    @DisplayName("When Index Set Wit Factory _ return Correct Location")
    void whenIndexSetWitFactory_returnCorrectLocation() {
        Location l = Location.newInstance(1);
        Assertions.assertEquals(1, l.getIndex());
        Assertions.assertTrue(true);
    }

    @Test
    @DisplayName("When Index Smaller Zero _ throw Exception")
    void whenIndexSmallerZero_throwException() {
        assertThrows(IllegalArgumentException.class, () -> {
            Location l = Location.Builder.newInstance().setIndex(-1).build();
        });
    }

    @Test
    @DisplayName("When Coordinate And Id And Index Not Set _ throw Exception")
    void whenCoordinateAndIdAndIndexNotSet_throwException() {
        assertThrows(IllegalArgumentException.class, () -> {
            Location l = Location.Builder.newInstance().build();
        });
    }

    @Test
    @DisplayName("When Id Set _ build")
    void whenIdSet_build() {
        Location l = Location.Builder.newInstance().setId("id").build();
        Assertions.assertEquals(l.getId(), "id");
        Assertions.assertTrue(true);
    }

    @Test
    @DisplayName("When Id Set With Factory _ return Correct Location")
    void whenIdSetWithFactory_returnCorrectLocation() {
        Location l = Location.newInstance("id");
        Assertions.assertEquals(l.getId(), "id");
        Assertions.assertTrue(true);
    }

    @Test
    @DisplayName("When Coordinate Set _ build")
    void whenCoordinateSet_build() {
        Location l = Location.Builder.newInstance().setCoordinate(Coordinate.newInstance(10, 20)).build();
        Assertions.assertEquals(10., l.getCoordinate().getX(), 0.001);
        Assertions.assertEquals(20., l.getCoordinate().getY(), 0.001);
        Assertions.assertTrue(true);
    }

    @Test
    @DisplayName("When Coordinate Set With Factory _ return Correct Location")
    void whenCoordinateSetWithFactory_returnCorrectLocation() {
        // Location l = Location.Builder.newInstance().setCoordinate(Coordinate.newInstance(10,20)).build();
        Location l = Location.newInstance(10, 20);
        Assertions.assertEquals(10., l.getCoordinate().getX(), 0.001);
        Assertions.assertEquals(20., l.getCoordinate().getY(), 0.001);
        Assertions.assertTrue(true);
    }

    @Test
    @DisplayName("When Setting User Data _ it Is Associated With The Location")
    void whenSettingUserData_itIsAssociatedWithTheLocation() {
        Location one = Location.Builder.newInstance().setCoordinate(Coordinate.newInstance(10, 20)).setUserData(new HashMap<String, Object>()).build();
        Location two = Location.Builder.newInstance().setIndex(1).setUserData(42).build();
        Location three = Location.Builder.newInstance().setIndex(2).build();
        Assertions.assertTrue(one.getUserData() instanceof Map);
        Assertions.assertEquals(42, two.getUserData());
        assertNull(three.getUserData());
    }
}
