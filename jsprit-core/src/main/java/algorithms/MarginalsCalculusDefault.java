package algorithms;

import algorithms.HardConstraints.HardActivityLevelConstraint;
import basics.costs.VehicleRoutingActivityCosts;
import basics.costs.VehicleRoutingTransportCosts;
import basics.route.TourActivity;

class MarginalsCalculusDefault implements MarginalsCalculus{

	private HardActivityLevelConstraint hardConstraint;

	private VehicleRoutingTransportCosts routingCosts;
	private VehicleRoutingActivityCosts activityCosts;
	
	public MarginalsCalculusDefault(VehicleRoutingTransportCosts routingCosts, VehicleRoutingActivityCosts actCosts, HardActivityLevelConstraint hardActivityLevelConstraint) {
		super();
		this.routingCosts = routingCosts;
		this.activityCosts = actCosts;
		this.hardConstraint = hardActivityLevelConstraint;
	}

	@Override
	public Marginals calculate(InsertionFacts iFacts, TourActivity prevAct, TourActivity nextAct, TourActivity newAct) {
		double tp_costs_prevAct_newAct = routingCosts.getTransportCost(prevAct.getLocationId(), newAct.getLocationId(), prevAct.getEndTime(), iFacts.getNewDriver(), iFacts.getNewVehicle());
		double tp_time_prevAct_newAct = routingCosts.getTransportTime(prevAct.getLocationId(), newAct.getLocationId(), prevAct.getEndTime(), iFacts.getNewDriver(), iFacts.getNewVehicle());
		
		double newAct_arrTime = prevAct.getEndTime() + tp_time_prevAct_newAct;
		
		if(!hardConstraint.fulfilled(iFacts, newAct, newAct_arrTime)){
			return null;
		}
		
		double newAct_operationStartTime = Math.max(newAct_arrTime, newAct.getTheoreticalEarliestOperationStartTime());
		
		double newAct_endTime = newAct_operationStartTime + newAct.getOperationTime();
		
		double act_costs_newAct = activityCosts.getActivityCost(newAct, newAct_arrTime, iFacts.getNewDriver(), iFacts.getNewVehicle());
		
		double tp_costs_newAct_nextAct = routingCosts.getTransportCost(newAct.getLocationId(), nextAct.getLocationId(), newAct_endTime, iFacts.getNewDriver(), iFacts.getNewVehicle());
		double tp_time_newAct_nextAct = routingCosts.getTransportTime(newAct.getLocationId(), nextAct.getLocationId(), newAct_endTime, iFacts.getNewDriver(), iFacts.getNewVehicle());
		
		double nextAct_arrTime = newAct_endTime + tp_time_newAct_nextAct;
		
		if(!hardConstraint.fulfilled(iFacts, nextAct, nextAct_arrTime)){
			return null;
		}
		
		double act_costs_nextAct = activityCosts.getActivityCost(nextAct, nextAct_arrTime, iFacts.getNewDriver(), iFacts.getNewVehicle());
		
		double totalCosts = tp_costs_prevAct_newAct + tp_costs_newAct_nextAct + act_costs_newAct + act_costs_nextAct; 
		
		double oldCosts;
		if(iFacts.getRoute().isEmpty()){
			oldCosts = 0.0;
		}
		else{
			double tp_costs_prevAct_nextAct = routingCosts.getTransportCost(prevAct.getLocationId(), nextAct.getLocationId(), prevAct.getEndTime(), iFacts.getRoute().getDriver(), iFacts.getRoute().getVehicle());
			double arrTime_nextAct = routingCosts.getTransportTime(prevAct.getLocationId(), nextAct.getLocationId(), prevAct.getEndTime(), iFacts.getNewDriver(), iFacts.getNewVehicle());
			
			double actCost_nextAct = activityCosts.getActivityCost(nextAct, arrTime_nextAct, iFacts.getRoute().getDriver(), iFacts.getRoute().getVehicle());
			oldCosts = tp_costs_prevAct_nextAct + actCost_nextAct;
		}
		
		double additionalCosts = totalCosts - oldCosts;
		double additionalTime = nextAct_arrTime - nextAct.getArrTime();

		return new Marginals(additionalCosts,additionalTime);
	}

}
