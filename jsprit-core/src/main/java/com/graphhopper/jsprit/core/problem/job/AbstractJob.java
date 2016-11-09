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

package com.graphhopper.jsprit.core.problem.job;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow;

/**
 * Created by schroeder on 14.07.14.
 */
public abstract class AbstractJob implements Job {

    private int index;

    protected List<Location> allLocations = new ArrayList<>();

    private JobActivityList activityList;

    protected Set<TimeWindow> operationTimeWindows = new HashSet<>();

    public AbstractJob() {
        super();
        activityList = new SequentialJobActivityList(this);
    }

    @Override
    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    protected void addLocation(Location location) {
        if (location != null) {
            allLocations.add(location);
        }
    }

    @Override
    public List<Location> getAllLocations() {
        return allLocations;
    }

    protected abstract void addLocations();

    protected abstract void createActivities();

    protected abstract void addOperationTimeWindows();

    protected void setActivities(JobActivityList list) {
        activityList = list;
    }

    @Override
    public JobActivityList getActivityList() {
        return activityList;
    }


    public Set<TimeWindow> getOperationTimeWindows() {
        return operationTimeWindows;
    }

}
