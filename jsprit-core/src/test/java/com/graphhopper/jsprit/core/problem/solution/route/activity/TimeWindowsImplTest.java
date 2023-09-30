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

import java.util.Collection;
import java.util.Iterator;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by schroeder on 18/12/15.
 */
public class TimeWindowsImplTest {

    @Test(expected = IllegalArgumentException.class)
    public void overlappingTW_shouldThrowException(){
        TimeWindowsImpl tws = new TimeWindowsImpl();
        tws.add(TimeWindow.newInstance(50, 100));
        tws.add(TimeWindow.newInstance(90,150));
    }

    @Test(expected = IllegalArgumentException.class)
    public void overlappingTW2_shouldThrowException(){
        TimeWindowsImpl tws = new TimeWindowsImpl();
        tws.add(TimeWindow.newInstance(50, 100));
        tws.add(TimeWindow.newInstance(40,150));
    }

    @Test(expected = IllegalArgumentException.class)
    public void overlappingTW3_shouldThrowException(){
        TimeWindowsImpl tws = new TimeWindowsImpl();
        tws.add(TimeWindow.newInstance(50, 100));
        tws.add(TimeWindow.newInstance(50, 100));
    }

    @Test
    public void slightlyOverlappingTw_shouldReturnExclusion() {
        TimeWindowsOverlapImpl tws = new TimeWindowsOverlapImpl();
        tws.addExcludedTimeWindow(TimeWindow.newInstance(1695461400, 1695488400));
        tws.addIncludedTimeWindow(TimeWindow.newInstance(1695454200, 1695465300));

        Collection<TimeWindow> res = tws.getTimeWindows(null);

        Assert.assertEquals(1, res.size());
        Assert.assertEquals(1695454200, res.iterator().next().getStart(), 1);
        Assert.assertEquals(1695461400, res.iterator().next().getEnd(), 1);
    }

    @Test
    public void entirelyExcludedTW_shouldReturnEmptyList() {
        TimeWindowsOverlapImpl tws = new TimeWindowsOverlapImpl();
        tws.addExcludedTimeWindow(TimeWindow.newInstance(10, 100));
        tws.addIncludedTimeWindow(TimeWindow.newInstance(20, 90));

        Collection<TimeWindow> res = tws.getTimeWindows(null);

        Assert.assertEquals(0, res.size());
    }

    @Test
    public void twoIncludedAndExcludedTW_shouldReturnTwoExclusions() {
        TimeWindowsOverlapImpl tws = new TimeWindowsOverlapImpl();
        tws.addExcludedTimeWindow(TimeWindow.newInstance(10, 100));
        tws.addExcludedTimeWindow(TimeWindow.newInstance(120, 200));
        tws.addIncludedTimeWindow(TimeWindow.newInstance(0, 90));
        tws.addIncludedTimeWindow(TimeWindow.newInstance(110, 150));

        Collection<TimeWindow> res = tws.getTimeWindows(null);

        Assert.assertEquals(2, res.size());
        Iterator<TimeWindow> iterator = res.iterator();
        TimeWindow first = iterator.next();
        TimeWindow second = iterator.next();

        Assert.assertEquals(0, first.getStart(), 1);
        Assert.assertEquals(110, second.getStart(), 1);

        Assert.assertEquals(10, first.getEnd(), 1);
        Assert.assertEquals(120, second.getEnd(), 1);
    }
}
