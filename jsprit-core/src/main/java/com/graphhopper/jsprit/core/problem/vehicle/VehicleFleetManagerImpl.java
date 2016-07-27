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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;


class VehicleFleetManagerImpl implements VehicleFleetManager {

    public VehicleFleetManagerImpl newInstance(Collection<Vehicle> vehicles) {
        return new VehicleFleetManagerImpl(vehicles);
    }

    static class TypeContainer {

        private ArrayList<Vehicle> vehicleList;

        private int index = 0;

        TypeContainer() {
            super();
            vehicleList = new ArrayList<Vehicle>();
        }

        void add(Vehicle vehicle) {
            if (vehicleList.contains(vehicle)) {
                throw new IllegalStateException("cannot add vehicle twice " + vehicle.getId());
            }
            vehicleList.add(vehicle);
        }

        void remove(Vehicle vehicle) {
            vehicleList.remove(vehicle);
        }

        Vehicle getVehicle() {
            if(index >= vehicleList.size()) index = 0;
            Vehicle vehicle = vehicleList.get(index);
            return vehicle;
        }

        void incIndex(){
            index++;
        }

        boolean isEmpty() {
            return vehicleList.isEmpty();
        }

    }

    private static Logger logger = LoggerFactory.getLogger(VehicleFleetManagerImpl.class);

    private Collection<Vehicle> vehicles;

    private TypeContainer[] vehicleTypes;

    private boolean[] locked;

    private Vehicle[] vehicleArr;

    private Random random;

    VehicleFleetManagerImpl(Collection<Vehicle> vehicles) {
        super();
        this.vehicles = vehicles;
        int arrSize = vehicles.size() + 2;
        locked = new boolean[arrSize];
        vehicleArr = new Vehicle[arrSize];
    }

    void setRandom(Random random) {
        this.random = random;
    }

    void init(){
        initializeVehicleTypes();
        logger.debug("initialise {}",this);
    }

    @Override
    public String toString() {
        return "[name=finiteVehicles]";
    }

    private void initializeVehicleTypes() {
        int maxTypeIndex = 0;
        for(Vehicle v : vehicles){
            if(v.getVehicleTypeIdentifier().getIndex() > maxTypeIndex){
                maxTypeIndex = v.getVehicleTypeIdentifier().getIndex();
            }
        }
        vehicleTypes = new TypeContainer[maxTypeIndex+1];
        for(int i=0;i< vehicleTypes.length;i++){
            TypeContainer typeContainer = new TypeContainer();
            vehicleTypes[i] = typeContainer;
        }
        for (Vehicle v : vehicles) {
            vehicleArr[v.getIndex()]=v;
            addVehicle(v);
        }
    }

    private void addVehicle(Vehicle v) {
        if (v.getType() == null) {
            throw new IllegalStateException("vehicle needs type");
        }
        vehicleTypes[v.getVehicleTypeIdentifier().getIndex()].add(v);
    }

    private void removeVehicle(Vehicle v) {
        vehicleTypes[v.getVehicleTypeIdentifier().getIndex()].remove(v);
    }


    /**
     * Returns a collection of available vehicles.
     * <p>
     * <p>If there is no vehicle with a certain type and location anymore, it looks up whether a penalty vehicle has been specified with
     * this type and location. If so, it returns this penalty vehicle. If not, no vehicle with this type and location is returned.
     */
    @Override
    public Collection<Vehicle> getAvailableVehicles() {
        List<Vehicle> vehicles = new ArrayList<Vehicle>();
        for(int i=0;i< vehicleTypes.length;i++){
            if(!vehicleTypes[i].isEmpty()){
                vehicles.add(vehicleTypes[i].getVehicle());
            }
        }
        return vehicles;
    }

    @Override
    public Collection<Vehicle> getAvailableVehicles(Vehicle withoutThisType) {
        List<Vehicle> vehicles = new ArrayList<Vehicle>();
        for(int i=0;i< vehicleTypes.length;i++){
            if(!vehicleTypes[i].isEmpty() && i != withoutThisType.getVehicleTypeIdentifier().getIndex()){
                vehicles.add(vehicleTypes[i].getVehicle());
            }
        }
        return vehicles;
    }


    @Override
    public Vehicle getAvailableVehicle(VehicleTypeKey vehicleTypeIdentifier) {
        if(!vehicleTypes[vehicleTypeIdentifier.getIndex()].isEmpty()){
            return vehicleTypes[vehicleTypeIdentifier.getIndex()].getVehicle();
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.matsim.contrib.freight.vrp.basics.VehicleFleetManager#lock(org.matsim.contrib.freight.vrp.basics.Vehicle)
     */
    @Override
    public void lock(Vehicle vehicle) {
        if (vehicles.isEmpty() || vehicle instanceof VehicleImpl.NoVehicle) {
            return;
        }
        if(locked[vehicle.getIndex()]){
            throw new IllegalStateException("cannot lock vehicle twice " + vehicle.getId());
        }
        else{
            locked[vehicle.getIndex()] = true;
            removeVehicle(vehicle);
        }
    }

    /* (non-Javadoc)
     * @see org.matsim.contrib.freight.vrp.basics.VehicleFleetManager#unlock(org.matsim.contrib.freight.vrp.basics.Vehicle)
     */
    @Override
    public void unlock(Vehicle vehicle) {
        if (vehicle == null || vehicles.isEmpty() || vehicle instanceof VehicleImpl.NoVehicle) {
            return;
        }
        locked[vehicle.getIndex()] = false;
        addVehicle(vehicle);
    }

    /* (non-Javadoc)
     * @see org.matsim.contrib.freight.vrp.basics.VehicleFleetManager#isLocked(org.matsim.contrib.freight.vrp.basics.Vehicle)
     */
    @Override
    public boolean isLocked(Vehicle vehicle) {
        return locked[vehicle.getIndex()];
    }

    /* (non-Javadoc)
     * @see org.matsim.contrib.freight.vrp.basics.VehicleFleetManager#unlockAll()
     */
    @Override
    public void unlockAll() {
        for(int i=0;i<vehicleArr.length;i++){
            if(locked[i]){
                unlock(vehicleArr[i]);
            }
        }
        for(int i=0;i<vehicleTypes.length;i++){
            vehicleTypes[i].incIndex();
        }
    }

}
