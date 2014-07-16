package jsprit.core.problem;

import jsprit.core.problem.vehicle.Vehicle;
import jsprit.core.problem.vehicle.VehicleTypeKey;

/**
 * AbstractVehicle to handle indeces of vehicles.
 */
public abstract class AbstractVehicle implements Vehicle {

    public abstract static class AbstractTypeKey implements HasIndex {

        private int index;

        public int getIndex(){ return index; }

        public void setIndex(int index) { this.index = index; }

    }

    private int index;

    private VehicleTypeKey vehicleIdentifier;

    public int getIndex(){ return index; }

    protected void setIndex(int index){ this.index = index; }

    public VehicleTypeKey getVehicleTypeIdentifier(){
        return vehicleIdentifier;
    }

    protected void setVehicleIdentifier(VehicleTypeKey vehicleTypeIdentifier){
        this.vehicleIdentifier = vehicleTypeIdentifier;
    }
}
