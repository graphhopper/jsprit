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

public class EndTest {

    @Test
    public void whenCallingCapacity_itShouldReturnEmptyCapacity() {
        End end = End.newInstance("loc", 0., 0.);
        assertEquals(0, end.getSize().get(0));
    }

    @Test
    public void whenStartIsIniWithEarliestStart_itShouldBeSetCorrectly() {
        End end = End.newInstance("loc", 1., 2.);
        assertEquals(1., end.getTheoreticalEarliestOperationStartTime(), 0.01);
    }

    @Test
    public void whenStartIsIniWithLatestStart_itShouldBeSetCorrectly() {
        End end = End.newInstance("loc", 1., 2.);
        assertEquals(2., end.getTheoreticalLatestOperationStartTime(), 0.01);
    }

    @Test
    public void whenSettingEndTime_itShouldBeSetCorrectly() {
        End end = End.newInstance("loc", 1., 2.);
        end.setEndTime(4.0);
        assertEquals(4., end.getEndTime(), 0.01);
    }


    @Test
    public void whenSettingEarliestStart_itShouldBeSetCorrectly() {
        End end = End.newInstance("loc", 1., 2.);
        end.setTheoreticalEarliestOperationStartTime(5.);
        assertEquals(5., end.getTheoreticalEarliestOperationStartTime(), 0.01);
    }

    @Test
    public void whenSettingLatestStart_itShouldBeSetCorrectly() {
        End end = End.newInstance("loc", 1., 2.);
        end.setTheoreticalLatestOperationStartTime(5.);
        assertEquals(5., end.getTheoreticalLatestOperationStartTime(), 0.01);
    }

    @Test
    public void whenCopyingEnd_itShouldBeDoneCorrectly() {
        End end = End.newInstance("loc", 1., 2.);
        end.setTheoreticalEarliestOperationStartTime(3.);
        end.setTheoreticalLatestOperationStartTime(5.);

        End copy = End.copyOf(end);
        assertEquals(3., copy.getTheoreticalEarliestOperationStartTime(), 0.01);
        assertEquals(5., copy.getTheoreticalLatestOperationStartTime(), 0.01);
        assertEquals("loc", copy.getLocation().getId());
        assertTrue(copy != end);
    }

}
