package com.graphhopper.jsprit.core.problem.cost;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;

public class SetupTime {

	public SetupTime(){}
	
	public double getSetupTime(TourActivity from, TourActivity to, Vehicle vehicle){
        double coef = 0.;
        double setupTime = 0.;
        if(vehicle != null)
            coef = vehicle.getCoefSetupTime();
        if(!from.getLocation().equals(to.getLocation()))
            setupTime = to.getSetupTime() * coef;
		return setupTime;
	}
	
	public double getSetupTime(Location from, Location to, double defaultSetupTime, Vehicle vehicle) {
        double setupTime = 0.;
        double coef = 0.;
        if(vehicle != null)
        	coef = vehicle.getCoefSetupTime();
        if(!from.equals(to))
            setupTime = defaultSetupTime * coef;
        return setupTime;
	}
	
    public double getSetupTime(TourActivity to, Vehicle vehicle) {
        double coef = 0.;
        if(vehicle != null)
            coef = vehicle.getCoefSetupTime();
        return to.getSetupTime() * coef;
    }
	
    public double getSetupCost(TourActivity from, TourActivity to, Vehicle vehicle){
	    return  vehicle.getType().getVehicleCostParams().perSetupTimeUnit * getSetupTime(from, to, vehicle);
    }
	
    public double getSetupCost(double setupTime, Vehicle vehicle){
	    return vehicle.getType().getVehicleCostParams().perSetupTimeUnit * setupTime;
    }
    
}
