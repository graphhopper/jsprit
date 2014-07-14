package jsprit.core.problem;

import jsprit.core.problem.vehicle.Vehicle;

/**
 * Created by schroeder on 14.07.14.
 */
public abstract class AbstractVehicle implements Vehicle {

    private int index;

    public int getIndex(){ return index; }

    protected void setIndex(int index){ this.index = index; }
}
