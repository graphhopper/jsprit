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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.job.Service;

public class PickupActivityTest {

    private Service service;

    private PickupActivityNEW pickup;

    @Before
    public void doBefore() {
        service = new Service.Builder("service").setLocation(Location.newInstance("loc")).
                        setTimeWindow(TimeWindow.newInstance(1., 2.)).
                        setServiceTime(20d).
                        addSizeDimension(0, 10).addSizeDimension(1, 100).addSizeDimension(2, 1000).build();
        pickup = (PickupActivityNEW) service.getServiceActivity();
        pickup.setTheoreticalEarliestOperationStartTime(
                        pickup.getTimeWindows().iterator().next().getStart());
        pickup.setTheoreticalLatestOperationStartTime(
                        pickup.getTimeWindows().iterator().next().getEnd());
    }

    @Test
    public void whenCallingCapacity_itShouldReturnCorrectCapacity() {
        assertEquals(10, pickup.getLoadChange().get(0));
        assertEquals(100, pickup.getLoadChange().get(1));
        assertEquals(1000, pickup.getLoadChange().get(2));
    }


    @Test
    public void whenCallingJob_itShouldReturnCorrectJob() {
        assertEquals(service, pickup.getJob());
    }

    @Test
    public void whenCallingOperationTime_itShouldReturnCorrectValue() {
        assertEquals(20d, pickup.getOperationTime(), 0.01);
    }

    @Test
    public void whenCallingOrderNumber_itShouldReturnCorrectValue() {
        assertEquals(1d, pickup.getOrderNumber(), 0.01);
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
        PickupActivityNEW copy = (PickupActivityNEW) pickup.duplicate();
        assertEquals(pickup.getJob(), copy.getJob());
        assertEquals(pickup.getOrderNumber(), copy.getOrderNumber());
        assertEquals(20d, copy.getOperationTime(), 0.01);
        assertEquals(pickup.getType(), copy.getType());
        assertEquals(1., copy.getTheoreticalEarliestOperationStartTime(), 0.01);
        assertEquals(2., copy.getTheoreticalLatestOperationStartTime(), 0.01);
        assertEquals("loc", copy.getLocation().getId());
        assertEquals(10, copy.getLoadChange().get(0));
        assertEquals(100, copy.getLoadChange().get(1));
        assertEquals(1000, copy.getLoadChange().get(2));
        assertTrue(copy != pickup);
    }

}
