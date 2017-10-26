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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Created by schroeder on 26/05/15.
 */
public class TimeWindowsImpl implements TimeWindows {

    private Collection<TimeWindow> timeWindows = new ArrayList<TimeWindow>();

    public void add(TimeWindow timeWindow){
        for(TimeWindow tw : timeWindows){
            if(timeWindow.getStart() > tw.getStart() && timeWindow.getStart() < tw.getEnd()){
                throw new IllegalArgumentException("time-windows cannot overlap each other. overlap: " + tw + ", " + timeWindow);
            }
            if(timeWindow.getEnd() > tw.getStart() && timeWindow.getEnd() < tw.getEnd()){
                throw new IllegalArgumentException("time-windows cannot overlap each other. overlap: " + tw + ", " + timeWindow);
            }
            if(timeWindow.getStart() <= tw.getStart() && timeWindow.getEnd() >= tw.getEnd()){
                throw new IllegalArgumentException("time-windows cannot overlap each other. overlap: " + tw + ", " + timeWindow);
            }
        }
        timeWindows.add(timeWindow);
    }

    public Collection<TimeWindow> getTimeWindows() {
        return Collections.unmodifiableCollection(timeWindows);
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer(timeWindows.size() * 60);
        for (TimeWindow tw : timeWindows) {
            sb.append("[timeWindow=").append(tw).append("]");
        }
        return sb.toString();
    }
}
