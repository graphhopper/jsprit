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

import java.util.HashMap;
import java.util.Map;

import com.graphhopper.jsprit.core.problem.driver.Driver;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;


class JobCalculatorSwitcher implements JobInsertionCostsCalculator {

    private Map<Class<? extends Job>, JobInsertionCostsCalculator> calcMap = new HashMap<Class<? extends Job>, JobInsertionCostsCalculator>();

    void put(Class<? extends Job> jobClass, JobInsertionCostsCalculator jic) {
        calcMap.put(jobClass, jic);
    }

    public InsertionData getInsertionData(VehicleRoute currentRoute, Job jobToInsert, Vehicle newVehicle, double newVehicleDepartureTime, Driver newDriver, double bestKnownScore) {
        JobInsertionCostsCalculator jic = calcMap.get(jobToInsert.getClass());
        if (jic == null) throw new IllegalStateException("cannot find calculator for " + jobToInsert.getClass());
        return jic.getInsertionData(currentRoute, jobToInsert, newVehicle, newVehicleDepartureTime, newDriver, bestKnownScore);
    }

}
