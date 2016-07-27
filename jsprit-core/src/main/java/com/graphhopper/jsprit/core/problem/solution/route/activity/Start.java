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

import com.graphhopper.jsprit.core.problem.AbstractActivity;
import com.graphhopper.jsprit.core.problem.Capacity;
import com.graphhopper.jsprit.core.problem.Location;

public final class Start extends AbstractActivity implements TourActivity {

    @Deprecated
    public final static String ACTIVITY_NAME = "start";

    private final static Capacity capacity = Capacity.Builder.newInstance().build();

    public static Start newInstance(String locationId, double theoreticalStart, double theoreticalEnd) {
        return new Start(locationId, theoreticalStart, theoreticalEnd);
    }

    public static Start copyOf(Start start) {
        return new Start(start);
    }

    private String locationId;

    private double theoretical_earliestOperationStartTime;

    private double theoretical_latestOperationStartTime;

    private double endTime;

    private double arrTime;

    private Location location;

    private Start(String locationId, double theoreticalStart, double theoreticalEnd) {
        super();
        if (locationId != null) this.location = Location.Builder.newInstance().setId(locationId).build();
        this.theoretical_earliestOperationStartTime = theoreticalStart;
        this.theoretical_latestOperationStartTime = theoreticalEnd;
        this.endTime = theoreticalStart;
        setIndex(-1);
    }

    public Start(Location location, double theoreticalStart, double theoreticalEnd) {
        super();
        this.location = location;
        this.theoretical_earliestOperationStartTime = theoreticalStart;
        this.theoretical_latestOperationStartTime = theoreticalEnd;
        this.endTime = theoreticalStart;
        setIndex(-1);
    }

    private Start(Start start) {
        this.location = start.getLocation();
        theoretical_earliestOperationStartTime = start.getTheoreticalEarliestOperationStartTime();
        theoretical_latestOperationStartTime = start.getTheoreticalLatestOperationStartTime();
        endTime = start.getEndTime();
        setIndex(-1);
    }

    public double getTheoreticalEarliestOperationStartTime() {
        return theoretical_earliestOperationStartTime;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    ;

    public double getTheoreticalLatestOperationStartTime() {
        return theoretical_latestOperationStartTime;
    }


    public void setTheoreticalEarliestOperationStartTime(double time) {
        this.theoretical_earliestOperationStartTime = time;
    }

    public void setTheoreticalLatestOperationStartTime(double time) {
        this.theoretical_latestOperationStartTime = time;
    }

    @Override
    public Location getLocation() {
        return location;
    }

    @Override
    public double getOperationTime() {
        return 0.0;
    }

    @Override
    public String toString() {
        return "[type=" + getName() + "][location=" + location
            + "][twStart=" + Activities.round(theoretical_earliestOperationStartTime)
            + "][twEnd=" + Activities.round(theoretical_latestOperationStartTime) + "]";
    }

    @Override
    public String getName() {
        return "start";
    }

    @Override
    public double getArrTime() {
        return arrTime;
    }

    @Override
    public double getEndTime() {
        return endTime;
    }

    @Override
    public void setArrTime(double arrTime) {
        this.arrTime = arrTime;
    }

    @Override
    public void setEndTime(double endTime) {
        this.endTime = endTime;
    }

    @Override
    public TourActivity duplicate() {
        return new Start(this);
    }

    @Override
    public Capacity getSize() {
        return capacity;
    }

}
