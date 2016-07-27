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
package com.graphhopper.jsprit.core.problem.vehicle;

import java.util.Collection;


public interface VehicleFleetManager {

    /**
     * Locks vehicle.
     * <p>
     * <p>This indicates that this vehicle is being used. Thus it is not in list of available vehicles.
     *
     * @param vehicle
     */
    public abstract void lock(Vehicle vehicle);

    /**
     * Unlocks vehicle.
     * <p>
     * <p>This indicates that this vehicle is not being used anymore. Thus it is in list of available vehicles.
     *
     * @param vehicle
     */
    public abstract void unlock(Vehicle vehicle);

    /**
     * Returns true if locked.
     *
     * @param vehicle
     * @return
     */
    public abstract boolean isLocked(Vehicle vehicle);

    /**
     * Unlocks all locked vehicles.
     */
    public abstract void unlockAll();

    /**
     * Returns a collection of available vehicles.
     * <p>
     * <p>Note that this does not return ALL available vehicles that were added to the fleetmanager. Vehicles are clustered according
     * to {@link VehicleTypeKey}. If there are two unlocked vehicle with the same VehicleTypeKey then only one of them will be returned.
     * This is to avoid returning too many vehicles that are basically equal.
     * <p>Look at {@link VehicleTypeKey} to figure out whether two vehicles are equal or not.
     *
     * @return
     */
    public abstract Collection<Vehicle> getAvailableVehicles();

    public Collection<Vehicle> getAvailableVehicles(Vehicle withoutThisType);

    public Vehicle getAvailableVehicle(VehicleTypeKey vehicleTypeIdentifier);
}
