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

import com.graphhopper.jsprit.core.problem.driver.Driver;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@Deprecated
class CalculatesServiceInsertionWithTimeSchedulingInSlices implements JobInsertionCostsCalculator {


    private static Logger log = LoggerFactory.getLogger(CalculatesServiceInsertionWithTimeSchedulingInSlices.class);

    private JobInsertionCostsCalculator jic;

    private int nOfDepartureTimes = 3;

    private double timeSlice = 900.0;

    public CalculatesServiceInsertionWithTimeSchedulingInSlices(JobInsertionCostsCalculator jic, double timeSlice, int neighbors) {
        super();
        this.jic = jic;
        this.timeSlice = timeSlice;
        this.nOfDepartureTimes = neighbors;
        log.debug("initialise " + this);
    }

    @Override
    public String toString() {
        return "[name=" + this.getClass().toString() + "][timeSlice=" + timeSlice + "][#timeSlice=" + nOfDepartureTimes + "]";
    }

    @Override
    public InsertionData getInsertionData(VehicleRoute currentRoute, Job jobToInsert, Vehicle newVehicle, double newVehicleDepartureTime, Driver newDriver, double bestKnownScore) {
        List<Double> vehicleDepartureTimes = new ArrayList<Double>();
        double currentStart;
        if (currentRoute.getStart() == null) {
            currentStart = newVehicleDepartureTime;
        } else currentStart = currentRoute.getStart().getEndTime();

        vehicleDepartureTimes.add(currentStart);
//		double earliestDeparture = newVehicle.getEarliestDeparture();
//		double latestEnd = newVehicle.getLatestArrival();

        for (int i = 0; i < nOfDepartureTimes; i++) {
            double neighborStartTime_earlier = currentStart - (i + 1) * timeSlice;
//			if(neighborStartTime_earlier > earliestDeparture) {
            vehicleDepartureTimes.add(neighborStartTime_earlier);
//			}
            double neighborStartTime_later = currentStart + (i + 1) * timeSlice;
//			if(neighborStartTime_later < latestEnd) {
            vehicleDepartureTimes.add(neighborStartTime_later);
//			}
        }

        InsertionData bestIData = null;
        for (Double departureTime : vehicleDepartureTimes) {
            InsertionData iData = jic.getInsertionData(currentRoute, jobToInsert, newVehicle, departureTime, newDriver, bestKnownScore);
            if (bestIData == null) bestIData = iData;
            else if (iData.getInsertionCost() < bestIData.getInsertionCost()) {
                iData.setVehicleDepartureTime(departureTime);
                bestIData = iData;
            }
        }
//		log.info(bestIData);
        return bestIData;
    }

}
