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
package com.graphhopper.jsprit.core.problem.solution.route.activity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Start Test")
class StartTest {

    @Test
    @DisplayName("When Calling Capacity _ it Should Return Empty Capacity")
    void whenCallingCapacity_itShouldReturnEmptyCapacity() {
        Start start = Start.newInstance("loc", 0., 0.);
        assertEquals(0, start.getSize().get(0));
    }

    @Test
    @DisplayName("When Start Is Ini With Earliest Start _ it Should Be Set Correctly")
    void whenStartIsIniWithEarliestStart_itShouldBeSetCorrectly() {
        Start start = Start.newInstance("loc", 1., 2.);
        assertEquals(1., start.getTheoreticalEarliestOperationStartTime(), 0.01);
    }

    @Test
    @DisplayName("When Start Is Ini With Latest Start _ it Should Be Set Correctly")
    void whenStartIsIniWithLatestStart_itShouldBeSetCorrectly() {
        Start start = Start.newInstance("loc", 1., 2.);
        assertEquals(2., start.getTheoreticalLatestOperationStartTime(), 0.01);
    }

    @Test
    @DisplayName("When Setting Start End Time _ it Should Be Set Correctly")
    void whenSettingStartEndTime_itShouldBeSetCorrectly() {
        Start start = Start.newInstance("loc", 1., 2.);
        start.setEndTime(4.0);
        assertEquals(4., start.getEndTime(), 0.01);
    }

    @Test
    @DisplayName("When Setting Earliest Start _ it Should Be Set Correctly")
    void whenSettingEarliestStart_itShouldBeSetCorrectly() {
        Start start = Start.newInstance("loc", 1., 2.);
        start.setTheoreticalEarliestOperationStartTime(5.);
        assertEquals(5., start.getTheoreticalEarliestOperationStartTime(), 0.01);
    }

    @Test
    @DisplayName("When Setting Latest Start _ it Should Be Set Correctly")
    void whenSettingLatestStart_itShouldBeSetCorrectly() {
        Start start = Start.newInstance("loc", 1., 2.);
        start.setTheoreticalLatestOperationStartTime(5.);
        assertEquals(5., start.getTheoreticalLatestOperationStartTime(), 0.01);
    }

    @Test
    @DisplayName("When Copying Start _ it Should Be Done Correctly")
    void whenCopyingStart_itShouldBeDoneCorrectly() {
        Start start = Start.newInstance("loc", 1., 2.);
        start.setTheoreticalEarliestOperationStartTime(3.);
        start.setTheoreticalLatestOperationStartTime(5.);
        Start copy = Start.copyOf(start);
        assertEquals(3., copy.getTheoreticalEarliestOperationStartTime(), 0.01);
        assertEquals(5., copy.getTheoreticalLatestOperationStartTime(), 0.01);
        assertEquals(copy.getLocation().getId(), "loc");
        assertTrue(copy != start);
    }
}
