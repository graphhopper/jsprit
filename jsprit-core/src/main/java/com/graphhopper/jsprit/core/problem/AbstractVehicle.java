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

package com.graphhopper.jsprit.core.problem;

import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeKey;

/**
 * AbstractVehicle to handle indeces of vehicles.
 */
public abstract class AbstractVehicle implements Vehicle {

    public abstract static class AbstractTypeKey implements HasIndex {

        private int index;

        @Override
        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }

    }

    private int index;

    private VehicleTypeKey vehicleIdentifier;

    private Object userData;

    /**
     * @return User-specific domain data associated with the vehicle
     */
    @Override
    public Object getUserData() {
        return userData;
    }

    protected void setUserData(Object userData) {
        this.userData = userData;
    }

    @Override
    public int getIndex() {
        return index;
    }

    protected void setIndex(int index) {
        this.index = index;
    }

    @Override
    public VehicleTypeKey getVehicleTypeIdentifier() {
        return vehicleIdentifier;
    }

    protected void setVehicleIdentifier(VehicleTypeKey vehicleTypeIdentifier) {
        this.vehicleIdentifier = vehicleTypeIdentifier;
    }
}
