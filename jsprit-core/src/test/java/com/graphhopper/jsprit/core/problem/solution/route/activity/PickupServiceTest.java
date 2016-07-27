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

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.job.Service;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PickupServiceTest {

    private Service service;

    private PickupService pickup;

    @Before
    public void doBefore() {
        service = Service.Builder.newInstance("service").setLocation(Location.newInstance("loc")).
            setTimeWindow(TimeWindow.newInstance(1., 2.)).
            addSizeDimension(0, 10).addSizeDimension(1, 100).addSizeDimension(2, 1000).build();
        pickup = new PickupService(service);
        pickup.setTheoreticalEarliestOperationStartTime(service.getTimeWindow().getStart());
        pickup.setTheoreticalLatestOperationStartTime(service.getTimeWindow().getEnd());
    }

    @Test
    public void whenCallingCapacity_itShouldReturnCorrectCapacity() {
        assertEquals(10, pickup.getSize().get(0));
        assertEquals(100, pickup.getSize().get(1));
        assertEquals(1000, pickup.getSize().get(2));
    }


    @Test
    public void whenStartIsIniWithEarliestStart_itShouldBeSetCorrectly() {
        assertEquals(1., pickup.getTheoreticalEarliestOperationStartTime(), 0.01);
    }

    @Test
    public void whenStartIsIniWithLatestStart_itShouldBeSetCorrectly() {
        assertEquals(2., pickup.getTheoreticalLatestOperationStartTime(), 0.01);
    }

    @Test
    public void whenSettingArrTime_itShouldBeSetCorrectly() {
        pickup.setArrTime(4.0);
        assertEquals(4., pickup.getArrTime(), 0.01);
    }

    @Test
    public void whenSettingEndTime_itShouldBeSetCorrectly() {
        pickup.setEndTime(5.0);
        assertEquals(5., pickup.getEndTime(), 0.01);
    }

    @Test
    public void whenIniLocationId_itShouldBeSetCorrectly() {
        assertEquals("loc", pickup.getLocation().getId());
    }

    @Test
    public void whenCopyingStart_itShouldBeDoneCorrectly() {
        PickupService copy = (PickupService) pickup.duplicate();
        assertEquals(1., copy.getTheoreticalEarliestOperationStartTime(), 0.01);
        assertEquals(2., copy.getTheoreticalLatestOperationStartTime(), 0.01);
        assertEquals("loc", copy.getLocation().getId());
        assertEquals(10, copy.getSize().get(0));
        assertEquals(100, copy.getSize().get(1));
        assertEquals(1000, copy.getSize().get(2));
        assertTrue(copy != pickup);
    }

}
