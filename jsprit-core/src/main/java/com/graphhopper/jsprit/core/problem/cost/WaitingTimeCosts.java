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

package com.graphhopper.jsprit.core.problem.cost;

import com.graphhopper.jsprit.core.problem.driver.Driver;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;

/**
 * Created by schroeder on 23/07/15.
 */
public class WaitingTimeCosts implements VehicleRoutingActivityCosts {

    @Override
    public double getActivityCost(TourActivity tourAct, double arrivalTime, Driver driver, Vehicle vehicle) {
        if (vehicle != null) {
            double waiting = vehicle.getType().getVehicleCostParams().perWaitingTimeUnit * Math.max(0., tourAct.getTheoreticalEarliestOperationStartTime() - arrivalTime);
            double servicing = vehicle.getType().getVehicleCostParams().perServiceTimeUnit * getActivityDuration(tourAct,arrivalTime,driver,vehicle);
            return waiting + servicing;
        }
        return 0;
    }

    @Override
    public double getActivityDuration(TourActivity tourAct, double arrivalTime, Driver driver, Vehicle vehicle) {
        return tourAct.getOperationTime();
    }

}
