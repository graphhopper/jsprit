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

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class StartTest {

    @Test
    public void whenCallingCapacity_itShouldReturnEmptyCapacity() {
        Start start = Start.newInstance("loc", 0., 0.);
        assertEquals(0, start.getSize().get(0));
    }

    @Test
    public void whenStartIsIniWithEarliestStart_itShouldBeSetCorrectly() {
        Start start = Start.newInstance("loc", 1., 2.);
        assertEquals(1., start.getTheoreticalEarliestOperationStartTime(), 0.01);
    }

    @Test
    public void whenStartIsIniWithLatestStart_itShouldBeSetCorrectly() {
        Start start = Start.newInstance("loc", 1., 2.);
        assertEquals(2., start.getTheoreticalLatestOperationStartTime(), 0.01);
    }

    @Test
    public void whenSettingStartEndTime_itShouldBeSetCorrectly() {
        Start start = Start.newInstance("loc", 1., 2.);
        start.setEndTime(4.0);
        assertEquals(4., start.getEndTime(), 0.01);
    }

    @Test
    public void whenSettingEarliestStart_itShouldBeSetCorrectly() {
        Start start = Start.newInstance("loc", 1., 2.);
        start.setTheoreticalEarliestOperationStartTime(5.);
        assertEquals(5., start.getTheoreticalEarliestOperationStartTime(), 0.01);
    }

    @Test
    public void whenSettingLatestStart_itShouldBeSetCorrectly() {
        Start start = Start.newInstance("loc", 1., 2.);
        start.setTheoreticalLatestOperationStartTime(5.);
        assertEquals(5., start.getTheoreticalLatestOperationStartTime(), 0.01);
    }

    @Test
    public void whenCopyingStart_itShouldBeDoneCorrectly() {
        Start start = Start.newInstance("loc", 1., 2.);
        start.setTheoreticalEarliestOperationStartTime(3.);
        start.setTheoreticalLatestOperationStartTime(5.);

        Start copy = Start.copyOf(start);
        assertEquals(3., copy.getTheoreticalEarliestOperationStartTime(), 0.01);
        assertEquals(5., copy.getTheoreticalLatestOperationStartTime(), 0.01);
        assertEquals("loc", copy.getLocation().getId());
        assertTrue(copy != start);
    }

}
