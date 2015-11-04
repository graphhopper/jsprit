/*******************************************************************************
 * Copyright (C) 2013  Stefan Schroeder
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package jsprit.core.problem.vehicle;

import jsprit.core.problem.vehicle.VehicleImpl.NoVehicle;
import jsprit.core.util.RandomNumberGeneration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

        private Random random = RandomNumberGeneration.getRandom();

        public TypeContainer() {
            super();
            vehicleList = new ArrayList<Vehicle>();
        }

        public void setRandom(Random random) {
            this.random = random;
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

        public Vehicle getVehicle() {
            int randomIndex = random.nextInt(vehicleList.size());
            return vehicleList.get(randomIndex);
        }

        public boolean isEmpty() {
            return vehicleList.isEmpty();
        }

    }

    private static Logger logger = LogManager.getLogger(VehicleFleetManagerImpl.class);

    private Collection<Vehicle> vehicles;

    private TypeContainer[] vehicleTypes;

    private boolean[] locked;

    private Vehicle[] vehicleArr;

    private Random random = RandomNumberGeneration.getRandom();

    public VehicleFleetManagerImpl(Collection<Vehicle> vehicles) {
        super();
        this.vehicles = vehicles;
        int arrSize = vehicles.size() + 2;
        locked = new boolean[arrSize];
        vehicleArr = new Vehicle[arrSize];
        initializeVehicleTypes();
        logger.debug("initialise {}",this);
    }

    public void setRandom(Random random) {
        this.random = random;
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
            typeContainer.setRandom(random);
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
     * <p/>
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
        if (vehicles.isEmpty() || vehicle instanceof NoVehicle) {
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
        if (vehicle == null || vehicles.isEmpty() || vehicle instanceof NoVehicle) {
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
    }

}
