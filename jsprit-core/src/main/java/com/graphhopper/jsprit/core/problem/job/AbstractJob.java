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
import java.util.Collections;
import java.util.List;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.solution.route.activity.InternalActivityMarker;
import com.graphhopper.jsprit.core.problem.solution.route.activity.JobActivity;

/**
 * Created by schroeder on 14.07.14.
 */
public abstract class AbstractJob implements Job {

    private int index;

    protected List<Location> allLocations = new ArrayList<>();

    private List<JobActivity> _activities = new ArrayList<>();
    private List<JobActivity> unmodifiableActivities = Collections.unmodifiableList(_activities);

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

    protected abstract void createActivities();


    protected void addActivity(JobActivity activity) {
        if (activity instanceof InternalActivityMarker && !(this instanceof InternalJobMarker)) {
            throw new IllegalArgumentException("Can't add an internal activity to a non-internal job: " + activity.getClass().getCanonicalName());
        }
        if (!activity.getJob().equals(this)) {
            throw new IllegalArgumentException("The activity " + activity.getName() + " is not associated with this job.");
        }
        _activities.add(activity);
    }

    public List<JobActivity> getActivities() {
        return unmodifiableActivities;
    }



}
