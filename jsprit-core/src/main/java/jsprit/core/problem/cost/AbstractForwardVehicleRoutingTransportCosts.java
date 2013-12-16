package jsprit.core.problem.cost;

import jsprit.core.problem.driver.Driver;
import jsprit.core.problem.vehicle.Vehicle;

public abstract class AbstractForwardVehicleRoutingTransportCosts implements VehicleRoutingTransportCosts{

	@Override
	public abstract double getTransportTime(String fromId, String toId,double departureTime, Driver driver, Vehicle vehicle);
	
	@Override
	public abstract double getTransportCost(String fromId, String toId,double departureTime, Driver driver, Vehicle vehicle);

	@Override
	public double getBackwardTransportTime(String fromId, String toId,double arrivalTime, Driver driver, Vehicle vehicle) {
		return getTransportTime(fromId, toId, arrivalTime, driver, vehicle);
	}

	@Override
	public double getBackwardTransportCost(String fromId, String toId,double arrivalTime, Driver driver, Vehicle vehicle) {
		return getTransportCost(fromId, toId, arrivalTime, driver, vehicle);
	}

}
