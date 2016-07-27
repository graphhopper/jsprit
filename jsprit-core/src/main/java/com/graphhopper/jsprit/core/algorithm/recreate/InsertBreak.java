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

import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;

/**
 * Created by schroeder on 19/05/15.
 */
class InsertBreak implements Event {

    private VehicleRoute vehicleRoute;

    private Vehicle newVehicle;

    private TourActivity activity;

    private int index;

    public InsertBreak(VehicleRoute vehicleRoute, Vehicle newVehicle, TourActivity activity, int index) {
        this.vehicleRoute = vehicleRoute;
        this.newVehicle = newVehicle;
        this.activity = activity;
        this.index = index;
    }

    public Vehicle getNewVehicle() {
        return newVehicle;
    }

    public VehicleRoute getVehicleRoute() {
        return vehicleRoute;
    }

    public TourActivity getActivity() {
        return activity;
    }

    public int getIndex() {
        return index;
    }
}
