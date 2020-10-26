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
package com.graphhopper.jsprit.core.algorithm.state;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.cost.ForwardTransportTime;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingActivityCosts;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.ActivityVisitor;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.util.ActivityTimeTracker;

import java.util.ArrayList;
import java.util.List;


/**
 * Updates arrival and end times of activities.
 * <p>
 * <p>Note that this modifies arrTime and endTime of each activity in a route.
 *
 * @author stefan
 */
public class UpdateActivityTimes implements ActivityVisitor, StateUpdater {

    private final ActivityTimeTracker timeTracker;

    private VehicleRoute route;

    /**
     * Updates arrival and end times of activities.
     * <p>
     * <p>Note that this modifies arrTime and endTime of each activity in a route.
     * <p>
     * <p>ArrTimes and EndTimes can be retrieved by <br>
     * <code>activity.getArrTime()</code> and
     * <code>activity.getEndTime()</code>
     */
    public UpdateActivityTimes(ForwardTransportTime transportTime, VehicleRoutingActivityCosts activityCosts) {
        timeTracker = new ActivityTimeTracker(transportTime, activityCosts);
    }

    public UpdateActivityTimes(ForwardTransportTime transportTime, ActivityTimeTracker.ActivityPolicy activityPolicy, VehicleRoutingActivityCosts activityCosts) {
        timeTracker = new ActivityTimeTracker(transportTime, activityPolicy, activityCosts);
    }

    @Override
    public void begin(VehicleRoute route) {
        timeTracker.begin(route);
        this.route = route;
        route.getStart().setEndTime(timeTracker.getActEndTime());
    }

    @Override
    public void visit(TourActivity activity) {
        timeTracker.visit(activity);
        activity.setArrTime(timeTracker.getActArrTime());
        activity.setEndTime(timeTracker.getActEndTime());
    }

    @Override
    public void finish() {
        timeTracker.finish();
        List<TourActivity> activities = route.getActivities();
        double totalSavedTime = 0;
        for (int i = 0; i < activities.size(); i++) {
            TourActivity current = activities.get(i);
            double endTime = current.getEndTime();
            double accumulatedOperatingTime = 0;
            double savedTimeByGrouping = 0;
            double maximumOperatingTime = current.getOperationTime();
            double minimumOperatingTime = current.getOperationTime();
            List<TourActivity> groupedActivities = new ArrayList<>();
            for (int j = i + 1; j < activities.size(); j++) {
                TourActivity next = activities.get(j);
                if (isSameLocation(current.getLocation(), next.getLocation()) && shouldOperateAtSameTime(next, endTime, accumulatedOperatingTime + next.getOperationTime())) {
                    accumulatedOperatingTime += next.getOperationTime();
                    maximumOperatingTime = Math.max(maximumOperatingTime, next.getOperationTime());
                    minimumOperatingTime = Math.min(minimumOperatingTime, next.getOperationTime());
                    savedTimeByGrouping += minimumOperatingTime;
                    groupedActivities.add(next);
                    i++;
                } else {
                    break;
                }
            }
            // if activities have been grouped before, adjust the arrival time for the vehicle by the previously accumulated saved time
            current.setArrTime(current.getArrTime() - totalSavedTime);
            current.setEndTime(endTime - current.getOperationTime() + maximumOperatingTime - totalSavedTime);
            groupedActivities.forEach(activity -> {
                activity.setArrTime(current.getArrTime());
                activity.setEndTime(current.getEndTime());
            });
            // Adjust the saved time by comparing the savedTimePerGroup with the difference to the accumulated time.
            // Three activities with the service time of 1 each
            //   - accumulatedOperatingTime = 2
            //   - savedTimeByGrouping = 2
            //   - total saved time = max(0, 2) = 2
            // Three activities with the service time of 1, 2, 1 respectively:
            //   - accumulatedOperatingTime = 3
            //   - savedTimeByGrouping = 2
            //   - total saved time = max(1, 2) = 2
            // Three activities with the service time of 1, 3, 2 respectively:
            //   - accumulatedOperatingTime = 5
            //   - savedTimeByGrouping = 2
            //   - total saved time = max(3, 2) = 3
            totalSavedTime += Math.max(accumulatedOperatingTime - savedTimeByGrouping, savedTimeByGrouping);
        }

        route.getEnd().setArrTime(timeTracker.getActArrTime() - totalSavedTime);
    }

    private boolean isSameLocation(Location location, Location other) {
        if (location.getCoordinate() != null && other.getCoordinate() != null) {
            double maxDelta = 0.000001;
            double diffLng = Math.abs(location.getCoordinate().getX() - other.getCoordinate().getX());
            double diffLat = Math.abs(location.getCoordinate().getY() - other.getCoordinate().getY());
            return diffLat < maxDelta && diffLng < maxDelta;
        }
        return location.equals(other);
    }

    private boolean shouldOperateAtSameTime(TourActivity next, double endTime, double accumulatedOperatingTime) {
        boolean similarOperatingTime = Math.abs(next.getEndTime() - accumulatedOperatingTime - endTime) <= 0.001;
        boolean laterThanTheoreticalStart = next.getEndTime() - accumulatedOperatingTime >= next.getTheoreticalEarliestOperationStartTime();
        return similarOperatingTime && laterThanTheoreticalStart;
    }

}
