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

import com.graphhopper.jsprit.core.util.RandomNumberGeneration;

import java.util.Collection;
import java.util.Random;

/**
 * Factory that creates a finite fleetmanager.
 *
 * @author schroeder
 */
public class FiniteFleetManagerFactory implements VehicleFleetManagerFactory {

    private Collection<Vehicle> vehicles;

    private Random random = RandomNumberGeneration.getRandom();

    /**
     * Constucts the factory.
     *
     * @param vehicles vehicles to be added to the fleetManager
     */
    public FiniteFleetManagerFactory(Collection<Vehicle> vehicles) {
        super();
        this.vehicles = vehicles;
    }

    public void setRandom(Random random) {
        this.random = random;
    }

    /**
     * Creates the finite fleetmanager.
     *
     * @return VehicleFleetManager
     * @throws java.lang.IllegalStateException if vehicles == null or vehicles.isEmpty()
     */
    @Override
    public VehicleFleetManager createFleetManager() {
        if (vehicles == null) throw new IllegalStateException("vehicles is null. this must not be.");
        if (vehicles.isEmpty()) throw new IllegalStateException("vehicle-collection is empty. this must not be");
        VehicleFleetManagerImpl vehicleFleetManager = new VehicleFleetManagerImpl(vehicles);
        vehicleFleetManager.setRandom(random);
        vehicleFleetManager.init();
        return vehicleFleetManager;
    }

}
