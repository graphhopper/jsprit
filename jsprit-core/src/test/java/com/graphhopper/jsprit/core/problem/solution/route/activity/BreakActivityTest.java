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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.SizeDimension;
import com.graphhopper.jsprit.core.problem.job.Break;
import com.graphhopper.jsprit.core.problem.job.Break.Builder;
import com.graphhopper.jsprit.core.problem.job.ServiceJob;


public class BreakActivityTest {

    private Break service;

    private BreakActivity serviceActivity;

    @Before
    public void doBefore() {
        Builder breakBuilder = new Break.Builder("service")
                .setTimeWindow(TimeWindow.newInstance(1., 2.)).setServiceTime(3);
        service = breakBuilder.build();
        serviceActivity = BreakActivity.newInstance(service, breakBuilder);
        serviceActivity.setTheoreticalEarliestOperationStartTime(
                service.getActivity().getBreakTimeWindow().getStart());
        serviceActivity.setTheoreticalLatestOperationStartTime(
                service.getActivity().getBreakTimeWindow().getEnd());
    }

    @Test
    public void whenCallingCapacity_itShouldReturnCorrectCapacity() {
        assertEquals(0, serviceActivity.getLoadChange().get(0));
    }

    @Test
    public void hasVariableLocationShouldBeTrue() {
        Break aBreak = serviceActivity.getJob();
        assertTrue(aBreak.hasVariableLocation());
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
    public void whenCopyingStart_itShouldBeDoneCorrectly() {
        BreakActivity copy = (BreakActivity) serviceActivity.duplicate();
        assertEquals(1., copy.getTheoreticalEarliestOperationStartTime(), 0.01);
        assertEquals(2., copy.getTheoreticalLatestOperationStartTime(), 0.01);
        assertTrue(copy != serviceActivity);
    }


    @Test
    public void whenTwoDeliveriesHaveTheSameUnderlyingJob_theyAreEqual() {
        Location loc = Location.newInstance("loc");
        ServiceJob s1 = new ServiceJob.Builder("s").setLocation(loc).build();
        ServiceJob s2 = new ServiceJob.Builder("s").setLocation(loc).build();
        ServiceActivity d1 = new ServiceActivity(s1, "s1",
                loc, 0d, SizeDimension.EMPTY,
                TimeWindows.ANY_TIME.getTimeWindows());
        ServiceActivity d2 = new ServiceActivity(s2, "s2",
                loc, 0d, SizeDimension.EMPTY,
                TimeWindows.ANY_TIME.getTimeWindows());

        assertTrue(d1.equals(d2));
    }

    @Test
    public void whenTwoDeliveriesHaveTheDifferentUnderlyingJob_theyAreNotEqual() {
        Location loc = Location.newInstance("loc");
        ServiceJob s1 = new ServiceJob.Builder("s").setLocation(loc).build();
        ServiceJob s2 = new ServiceJob.Builder("s2").setLocation(loc).build();
        ServiceActivity d1 = new ServiceActivity(s1, "s1",
                loc, 0d, SizeDimension.EMPTY,
                TimeWindows.ANY_TIME.getTimeWindows());
        ServiceActivity d2 = new ServiceActivity(s2, "s2",
                loc, 0d, SizeDimension.EMPTY,
                TimeWindows.ANY_TIME.getTimeWindows());

        assertFalse(d1.equals(d2));
    }
}
