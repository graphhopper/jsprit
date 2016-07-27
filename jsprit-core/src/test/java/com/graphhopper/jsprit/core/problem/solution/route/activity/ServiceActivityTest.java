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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;


public class ServiceActivityTest {

    private Service service;

    private ServiceActivity serviceActivity;

    @Before
    public void doBefore() {
        service = Service.Builder.newInstance("service").setLocation(Location.newInstance("loc")).
            setTimeWindow(TimeWindow.newInstance(1., 2.)).
            addSizeDimension(0, 10).addSizeDimension(1, 100).addSizeDimension(2, 1000).build();
        serviceActivity = ServiceActivity.newInstance(service);
        serviceActivity.setTheoreticalEarliestOperationStartTime(service.getTimeWindow().getStart());
        serviceActivity.setTheoreticalLatestOperationStartTime(service.getTimeWindow().getEnd());
    }

    @Test
    public void whenCallingCapacity_itShouldReturnCorrectCapacity() {
        Assert.assertEquals(10, serviceActivity.getSize().get(0));
        Assert.assertEquals(100, serviceActivity.getSize().get(1));
        Assert.assertEquals(1000, serviceActivity.getSize().get(2));
    }


    @Test
    public void whenStartIsIniWithEarliestStart_itShouldBeSetCorrectly() {
        assertEquals(1., serviceActivity.getTheoreticalEarliestOperationStartTime(), 0.01);
    }

    @Test
    public void whenStartIsIniWithLatestStart_itShouldBeSetCorrectly() {
        assertEquals(2., serviceActivity.getTheoreticalLatestOperationStartTime(), 0.01);
    }

    @Test
    public void whenSettingArrTime_itShouldBeSetCorrectly() {
        serviceActivity.setArrTime(4.0);
        assertEquals(4., serviceActivity.getArrTime(), 0.01);
    }

    @Test
    public void whenSettingEndTime_itShouldBeSetCorrectly() {
        serviceActivity.setEndTime(5.0);
        assertEquals(5., serviceActivity.getEndTime(), 0.01);
    }

    @Test
    public void whenIniLocationId_itShouldBeSetCorrectly() {
        assertEquals("loc", serviceActivity.getLocation().getId());
    }

    @Test
    public void whenCopyingStart_itShouldBeDoneCorrectly() {
        ServiceActivity copy = (ServiceActivity) serviceActivity.duplicate();
        assertEquals(1., copy.getTheoreticalEarliestOperationStartTime(), 0.01);
        assertEquals(2., copy.getTheoreticalLatestOperationStartTime(), 0.01);
        assertEquals("loc", copy.getLocation().getId());
        assertTrue(copy != serviceActivity);
    }


    @Test
    public void whenTwoDeliveriesHaveTheSameUnderlyingJob_theyAreEqual() {
        Service s1 = Service.Builder.newInstance("s").setLocation(Location.newInstance("loc")).build();
        Service s2 = Service.Builder.newInstance("s").setLocation(Location.newInstance("loc")).build();

        ServiceActivity d1 = ServiceActivity.newInstance(s1);
        ServiceActivity d2 = ServiceActivity.newInstance(s2);

        assertTrue(d1.equals(d2));
    }

    @Test
    public void whenTwoDeliveriesHaveTheDifferentUnderlyingJob_theyAreNotEqual() {
        Service s1 = Service.Builder.newInstance("s").setLocation(Location.newInstance("loc")).build();
        Service s2 = Service.Builder.newInstance("s1").setLocation(Location.newInstance("loc")).build();

        ServiceActivity d1 = ServiceActivity.newInstance(s1);
        ServiceActivity d2 = ServiceActivity.newInstance(s2);

        assertFalse(d1.equals(d2));
    }
}
