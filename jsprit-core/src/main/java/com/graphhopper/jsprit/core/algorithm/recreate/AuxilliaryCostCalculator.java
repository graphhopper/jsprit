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

import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingActivityCosts;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import com.graphhopper.jsprit.core.problem.driver.Driver;
import com.graphhopper.jsprit.core.problem.solution.route.activity.End;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;

import java.util.Iterator;
import java.util.List;


final class AuxilliaryCostCalculator {

    private final VehicleRoutingTransportCosts routingCosts;

    private final VehicleRoutingActivityCosts activityCosts;

    public AuxilliaryCostCalculator(final VehicleRoutingTransportCosts routingCosts, final VehicleRoutingActivityCosts actCosts) {
        super();
        this.routingCosts = routingCosts;
        this.activityCosts = actCosts;
    }

    /**
     * @param path    activity path to get the costs for
     * @param depTime departure time at first activity in path
     * @param driver  driver of vehicle
     * @param vehicle vehicle running the path
     * @return cost of path
     */
    public double costOfPath(final List<TourActivity> path, final double depTime, final Driver driver, final Vehicle vehicle) {
        if (path.isEmpty()) {
            return 0.0;
        }
        double cost = 0.0;
        Iterator<TourActivity> actIter = path.iterator();
        TourActivity prevAct = actIter.next();
        double startCost = 0.0;
        cost += startCost;
        double departureTimePrevAct = depTime;
        while (actIter.hasNext()) {
            TourActivity act = actIter.next();
            if (act instanceof End) {
                if (!vehicle.isReturnToDepot()) {
                    return cost;
                }
            }
            double transportCost = routingCosts.getTransportCost(prevAct.getLocation(), act.getLocation(), departureTimePrevAct, driver, vehicle);
            double transportTime = routingCosts.getTransportTime(prevAct.getLocation(), act.getLocation(), departureTimePrevAct, driver, vehicle);
            cost += transportCost;
            double actStartTime = departureTimePrevAct + transportTime;
            departureTimePrevAct = Math.max(actStartTime, act.getTheoreticalEarliestOperationStartTime()) + activityCosts.getActivityDuration(act, actStartTime, driver, vehicle);
            cost += activityCosts.getActivityCost(act, actStartTime, driver, vehicle);
            prevAct = act;
        }
        return cost;
    }


}
