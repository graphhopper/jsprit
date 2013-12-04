package jsprit.core.algorithm.recreate;

import jsprit.core.problem.driver.Driver;
import jsprit.core.problem.job.Job;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.vehicle.PenaltyVehicleType;
import jsprit.core.problem.vehicle.Vehicle;

class PenalyzeInsertionCostsWithPenaltyVehicle implements JobInsertionCostsCalculator{

	JobInsertionCostsCalculator base;
	
	public PenalyzeInsertionCostsWithPenaltyVehicle(JobInsertionCostsCalculator baseInsertionCostsCalculator) {
		super();
		this.base = baseInsertionCostsCalculator;
	}

	@Override
	public InsertionData getInsertionData(VehicleRoute currentRoute,Job newJob, Vehicle newVehicle, double newVehicleDepartureTime, Driver newDriver, double bestKnownCosts) {
		if(newVehicle.getType() instanceof PenaltyVehicleType){
			if(currentRoute.getVehicle().getType() instanceof PenaltyVehicleType){
				InsertionData iData = base.getInsertionData(currentRoute, newJob, newVehicle, newVehicleDepartureTime, newDriver, bestKnownCosts);
				double penaltyC = iData.getInsertionCost()*((PenaltyVehicleType)newVehicle.getType()).getPenaltyFactor();
				InsertionData newData = new InsertionData(penaltyC, iData.getPickupInsertionIndex(), iData.getDeliveryInsertionIndex(), iData.getSelectedVehicle(), iData.getSelectedDriver());
				newData.setAdditionalTime(iData.getAdditionalTime());
				newData.setVehicleDepartureTime(iData.getVehicleDepartureTime());
				return newData;
			}
		}
		return base.getInsertionData(currentRoute, newJob, newVehicle, newVehicleDepartureTime, newDriver, bestKnownCosts);
	}



}
