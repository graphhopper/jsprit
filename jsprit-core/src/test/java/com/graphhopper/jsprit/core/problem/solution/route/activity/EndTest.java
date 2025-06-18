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

import org.junit.jupiter.api.Assertions;

@DisplayName("End Test")
class EndTest {

    @Test
    @DisplayName("When Calling Capacity _ it Should Return Empty Capacity")
    void whenCallingCapacity_itShouldReturnEmptyCapacity() {
        End end = End.newInstance("loc", 0., 0.);
        Assertions.assertEquals(0, end.getSize().get(0));
    }

    @Test
    @DisplayName("When Start Is Ini With Earliest Start _ it Should Be Set Correctly")
    void whenStartIsIniWithEarliestStart_itShouldBeSetCorrectly() {
        End end = End.newInstance("loc", 1., 2.);
        Assertions.assertEquals(1., end.getTheoreticalEarliestOperationStartTime(), 0.01);
    }

    @Test
    @DisplayName("When Start Is Ini With Latest Start _ it Should Be Set Correctly")
    void whenStartIsIniWithLatestStart_itShouldBeSetCorrectly() {
        End end = End.newInstance("loc", 1., 2.);
        Assertions.assertEquals(2., end.getTheoreticalLatestOperationStartTime(), 0.01);
    }

    @Test
    @DisplayName("When Setting End Time _ it Should Be Set Correctly")
    void whenSettingEndTime_itShouldBeSetCorrectly() {
        End end = End.newInstance("loc", 1., 2.);
        end.setEndTime(4.0);
        Assertions.assertEquals(4., end.getEndTime(), 0.01);
    }

    @Test
    @DisplayName("When Setting Earliest Start _ it Should Be Set Correctly")
    void whenSettingEarliestStart_itShouldBeSetCorrectly() {
        End end = End.newInstance("loc", 1., 2.);
        end.setTheoreticalEarliestOperationStartTime(5.);
        Assertions.assertEquals(5., end.getTheoreticalEarliestOperationStartTime(), 0.01);
    }

    @Test
    @DisplayName("When Setting Latest Start _ it Should Be Set Correctly")
    void whenSettingLatestStart_itShouldBeSetCorrectly() {
        End end = End.newInstance("loc", 1., 2.);
        end.setTheoreticalLatestOperationStartTime(5.);
        Assertions.assertEquals(5., end.getTheoreticalLatestOperationStartTime(), 0.01);
    }

    @Test
    @DisplayName("When Copying End _ it Should Be Done Correctly")
    void whenCopyingEnd_itShouldBeDoneCorrectly() {
        End end = End.newInstance("loc", 1., 2.);
        end.setTheoreticalEarliestOperationStartTime(3.);
        end.setTheoreticalLatestOperationStartTime(5.);
        End copy = End.copyOf(end);
        Assertions.assertEquals(3., copy.getTheoreticalEarliestOperationStartTime(), 0.01);
        Assertions.assertEquals(5., copy.getTheoreticalLatestOperationStartTime(), 0.01);
        Assertions.assertEquals(copy.getLocation().getId(), "loc");
        Assertions.assertTrue(copy != end);
    }
}
