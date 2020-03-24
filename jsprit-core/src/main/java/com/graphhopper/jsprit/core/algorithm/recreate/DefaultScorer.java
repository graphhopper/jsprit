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

package com.graphhopper.jsprit.core.algorithm.recreate;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Activity;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow;

/**
 * Created by schroeder on 15/10/15.
 */
public class DefaultScorer implements ScoringFunction  {

    private VehicleRoutingProblem vrp;

    private double timeWindowParam = -0.5;

    private double depotDistanceParam = +0.1;

    private double minTimeWindowScore = -100000;

    public DefaultScorer(VehicleRoutingProblem vrp) {
        this.vrp = vrp;
    }

    public void setTimeWindowParam(double twParam) {
        this.timeWindowParam = twParam;
    }

    public void setDepotDistanceParam(double depotDistanceParam) {
        this.depotDistanceParam = depotDistanceParam;
    }

    @Override
    public double score(InsertionData best, Job job) {
        return scoreJob(best, job);
    }

    private double scoreJob(InsertionData best, Job job) {
        Location startLocation = best.getSelectedVehicle().getStartLocation();
        Location endLocation = best.getSelectedVehicle().getEndLocation();
        double maxDepotDistance = 0;
        double minTimeToOperate = Double.MAX_VALUE;
        for (Activity act : job.getActivities()) {
            maxDepotDistance = Math.max(maxDepotDistance, getDistance(startLocation, act.getLocation()));
            maxDepotDistance = Math.max(maxDepotDistance, getDistance(endLocation, act.getLocation()));
            TimeWindow tw = getLargestTimeWindow(act);
            minTimeToOperate = Math.min(minTimeToOperate, tw.getEnd() - tw.getStart());
        }
        return Math.max(timeWindowParam * minTimeToOperate, minTimeWindowScore) + depotDistanceParam * maxDepotDistance;
    }

    private TimeWindow getLargestTimeWindow(Activity act) {
        TimeWindow timeWindow = null;
        for (TimeWindow tw : act.getTimeWindows()) {
            if (timeWindow == null) timeWindow = tw;
            else if (tw.larger(timeWindow)) timeWindow = tw;
        }
        return TimeWindow.newInstance(0, Double.MAX_VALUE);
    }


    private double getDistance(Location loc1, Location loc2) {
        if (loc1 == null || loc2 == null) return 0d;
        return vrp.getTransportCosts().getTransportCost(loc1, loc2, 0., null, null);
    }

    @Override
    public String toString() {
        return "[name=defaultScorer][twParam=" + timeWindowParam + "][depotDistanceParam=" + depotDistanceParam + "]";
    }
}
