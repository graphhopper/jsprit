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
package com.graphhopper.jsprit.core.algorithm;

import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingActivityCosts;
import com.graphhopper.jsprit.core.problem.driver.Driver;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;


public class ExampleActivityCostFunction implements VehicleRoutingActivityCosts {

    public ExampleActivityCostFunction() {
        super();
    }

    public double parameter_timeAtAct;

    public double parameter_penaltyTooLate;


    @Override
    public double getActivityCost(TourActivity tourAct, double arrivalTime, Driver driver, Vehicle vehicle, TourActivity prevAct) {
        if (arrivalTime == Time.TOURSTART || arrivalTime == Time.UNDEFINED) {
            return 0.0;
        } else {
            //waiting + act-time
            double endTime = Math.max(arrivalTime, tourAct.getTheoreticalEarliestOperationStartTime()) + getActivityDuration(tourAct,arrivalTime,driver,vehicle, prevAct);
            double timeAtAct = endTime - arrivalTime;

            double totalCost = timeAtAct * parameter_timeAtAct;

            //penalty tooLate
            if (tourAct instanceof TourActivity.JobActivity) {
                if (arrivalTime > tourAct.getTheoreticalLatestOperationStartTime()) {
                    double penTime = arrivalTime - tourAct.getTheoreticalLatestOperationStartTime();
                    totalCost += penTime * parameter_penaltyTooLate;
                }
            }
            return totalCost;

        }
    }

    @Override
    public double getActivityDuration(TourActivity tourAct, double arrivalTime, Driver driver, Vehicle vehicle, TourActivity prevAct) {
        return tourAct.getOperationTime();
    }

}
