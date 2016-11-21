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

import org.junit.Test;

import com.graphhopper.jsprit.core.problem.job.AbstractSingleActivityJob;

public abstract class JobActivityTest {

    protected AbstractSingleActivityJob<?> service;

    protected JobActivity activity;

    public void createActivity(AbstractSingleActivityJob<?> service) {
        this.service = service;
        activity = service.getActivity();
        activity.setTheoreticalEarliestOperationStartTime(
                        activity.getTimeWindows().iterator().next().getStart());
        activity.setTheoreticalLatestOperationStartTime(
                        activity.getTimeWindows().iterator().next().getEnd());
    }

    @Test
    public abstract void whenCallingCapacity_itShouldReturnCorrectCapacity();


    @Test
    public void whenCallingJob_itShouldReturnCorrectJob() {
        assertEquals(service, activity.getJob());
    }

    @Test
    public void whenCallingOperationTime_itShouldReturnCorrectValue() {
        assertEquals(20d, activity.getOperationTime(), 0.01);
    }

    @Test
    public void whenCallingOrderNumber_itShouldReturnCorrectValue() {
        assertEquals(1d, activity.getOrderNumber(), 0.01);
    }

    @Test
    public void whenStartIsIniWithEarliestStart_itShouldBeSetCorrectly() {
        assertEquals(1., activity.getTheoreticalEarliestOperationStartTime(), 0.01);
    }

    @Test
    public void whenStartIsIniWithLatestStart_itShouldBeSetCorrectly() {
        assertEquals(2., activity.getTheoreticalLatestOperationStartTime(), 0.01);
    }

    @Test
    public void whenSettingArrTime_itShouldBeSetCorrectly() {
        activity.setArrTime(4.0);
        assertEquals(4., activity.getArrTime(), 0.01);
    }

    @Test
    public void whenSettingEndTime_itShouldBeSetCorrectly() {
        activity.setEndTime(5.0);
        assertEquals(5., activity.getEndTime(), 0.01);
    }

    @Test
    public void whenIniLocationId_itShouldBeSetCorrectly() {
        assertEquals("loc", activity.getLocation().getId());
    }

    @Test
    public void whenCopyingStart_classShouldBeTheSame() {
        JobActivity copy = (JobActivity) activity.duplicate();
        assertEquals(activity.getClass(), copy.getClass());
    }

    @Test
    public void whenCopyingStart_jobShouldBeTheSame() {
        JobActivity copy = (JobActivity) activity.duplicate();
        assertEquals(activity.getJob(), copy.getJob());
    }

    @Test
    public void whenCopyingStart_orderNumberShouldBeTheSame() {
        JobActivity copy = (JobActivity) activity.duplicate();
        assertEquals(activity.getOrderNumber(), copy.getOrderNumber());
    }

    @Test
    public void whenCopyingStart_operationTimeShouldBeTheSame() {
        JobActivity copy = (JobActivity) activity.duplicate();
        assertEquals(20d, copy.getOperationTime(), 0.01);
    }

    @Test
    public void whenCopyingStart_typeShouldBeTheSame() {
        JobActivity copy = (JobActivity) activity.duplicate();
        assertEquals(activity.getType(), copy.getType());
    }

    @Test
    public void whenCopyingStart_theoreticalTimesShouldBeTheSame() {
        JobActivity copy = (JobActivity) activity.duplicate();
        assertEquals(1., copy.getTheoreticalEarliestOperationStartTime(), 0.01);
        assertEquals(2., copy.getTheoreticalLatestOperationStartTime(), 0.01);
    }

    @Test
    public void whenCopyingStart_locationShouldBeTheSame() {
        JobActivity copy = (JobActivity) activity.duplicate();
        assertEquals("loc", copy.getLocation().getId());
    }

    @Test
    public void whenCopyingStart_sizeShouldBeTheSame() {
        JobActivity copy = (JobActivity) activity.duplicate();
        assertEquals(activity.getLoadSize(), copy.getLoadSize());
        assertEquals(activity.getLoadChange(), copy.getLoadChange());
    }

}
