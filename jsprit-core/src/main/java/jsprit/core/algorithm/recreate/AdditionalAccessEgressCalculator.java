package jsprit.core.algorithm.recreate;

import jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import jsprit.core.problem.driver.Driver;
import jsprit.core.problem.misc.JobInsertionContext;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.solution.route.activity.TourActivity;
import jsprit.core.problem.vehicle.Vehicle;

/**
 * Estimates additional access/egress costs when operating route with a new vehicle that has different start/end-location.
 * 
 * <p>If two vehicles have the same start/end-location and departure-time .getCosts(...) must return zero.
 * 
 * @author schroeder
 *
 */
class AdditionalAccessEgressCalculator {
	
	private VehicleRoutingTransportCosts routingCosts;
	
	/**
	 * Constructs the estimator that estimates additional access/egress costs when operating route with a new vehicle that has different start/end-location.
	 * 
	 * <p>If two vehicles have the same start/end-location and departure-time .getCosts(...) must return zero.
	 * 
	 * @author schroeder
	 *
	 */
	public AdditionalAccessEgressCalculator(VehicleRoutingTransportCosts routingCosts) {
		this.routingCosts = routingCosts;
	}
	
	public double getCosts(JobInsertionContext insertionContext){
		double delta_access = 0.0;
		double delta_egress = 0.0;
		VehicleRoute currentRoute = insertionContext.getRoute();
		Vehicle newVehicle = insertionContext.getNewVehicle();
		Driver newDriver = insertionContext.getNewDriver();
		double newVehicleDepartureTime = insertionContext.getNewDepTime();
		if(!currentRoute.isEmpty()){
			double accessTransportCostNew = routingCosts.getTransportCost(newVehicle.getLocationId(), currentRoute.getActivities().get(0).getLocationId(), newVehicleDepartureTime, newDriver, newVehicle);
			double accessTransportCostOld = routingCosts.getTransportCost(currentRoute.getStart().getLocationId(), currentRoute.getActivities().get(0).getLocationId(), currentRoute.getDepartureTime(), currentRoute.getDriver(), currentRoute.getVehicle());
			
			delta_access = accessTransportCostNew - accessTransportCostOld;
			
			TourActivity lastActivityBeforeEndOfRoute = currentRoute.getActivities().get(currentRoute.getActivities().size()-1);
			double lastActivityEndTimeWithOldVehicleAndDepartureTime = lastActivityBeforeEndOfRoute.getEndTime();
			double lastActivityEndTimeEstimationWithNewVehicleAndNewDepartureTime = Math.max(0.0, lastActivityEndTimeWithOldVehicleAndDepartureTime + (newVehicleDepartureTime - currentRoute.getDepartureTime()));
			double egressTransportCostNew = routingCosts.getTransportCost(lastActivityBeforeEndOfRoute.getLocationId(), newVehicle.getLocationId() , lastActivityEndTimeEstimationWithNewVehicleAndNewDepartureTime, newDriver, newVehicle);
			double egressTransportCostOld = routingCosts.getTransportCost(lastActivityBeforeEndOfRoute.getLocationId(), currentRoute.getEnd().getLocationId(), lastActivityEndTimeWithOldVehicleAndDepartureTime, currentRoute.getDriver(), currentRoute.getVehicle());
			
			delta_egress = egressTransportCostNew - egressTransportCostOld;
		}
		return delta_access + delta_egress;
	}
	
}