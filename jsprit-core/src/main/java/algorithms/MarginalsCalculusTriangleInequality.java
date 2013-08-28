package algorithms;

import algorithms.HardConstraints.HardActivityLevelConstraint;
import basics.costs.VehicleRoutingActivityCosts;
import basics.costs.VehicleRoutingTransportCosts;
import basics.route.TourActivity;

class MarginalsCalculusTriangleInequality implements MarginalsCalculus{

	private HardActivityLevelConstraint hardConstraint;

	private VehicleRoutingTransportCosts routingCosts;
	private VehicleRoutingActivityCosts activityCosts;
	
	public MarginalsCalculusTriangleInequality(VehicleRoutingTransportCosts routingCosts, VehicleRoutingActivityCosts actCosts, HardActivityLevelConstraint hardActivityLevelConstraint) {
		super();
		this.routingCosts = routingCosts;
		this.activityCosts = actCosts;
		this.hardConstraint = hardActivityLevelConstraint;
	}

	@Override
	public Marginals calculate(InsertionContext iFacts, TourActivity prevAct, TourActivity nextAct, TourActivity newAct, double depTimeAtPrevAct) {
		if(!hardConstraint.fulfilled(iFacts, prevAct, newAct, nextAct, depTimeAtPrevAct)){
			return null;
		}
		
		double tp_costs_prevAct_newAct = routingCosts.getTransportCost(prevAct.getLocationId(), newAct.getLocationId(), depTimeAtPrevAct, iFacts.getNewDriver(), iFacts.getNewVehicle());
		double tp_time_prevAct_newAct = routingCosts.getTransportTime(prevAct.getLocationId(), newAct.getLocationId(), depTimeAtPrevAct, iFacts.getNewDriver(), iFacts.getNewVehicle());
		
		double newAct_arrTime = depTimeAtPrevAct + tp_time_prevAct_newAct;
		
		double newAct_endTime = CalcUtils.getActivityEndTime(newAct_arrTime, newAct);
		
		double act_costs_newAct = activityCosts.getActivityCost(newAct, newAct_arrTime, iFacts.getNewDriver(), iFacts.getNewVehicle());
		
		double tp_costs_newAct_nextAct = routingCosts.getTransportCost(newAct.getLocationId(), nextAct.getLocationId(), newAct_endTime, iFacts.getNewDriver(), iFacts.getNewVehicle());
		double tp_time_newAct_nextAct = routingCosts.getTransportTime(newAct.getLocationId(), nextAct.getLocationId(), newAct_endTime, iFacts.getNewDriver(), iFacts.getNewVehicle());
		
		double nextAct_arrTime = newAct_endTime + tp_time_newAct_nextAct;
				
		double act_costs_nextAct = activityCosts.getActivityCost(nextAct, nextAct_arrTime, iFacts.getNewDriver(), iFacts.getNewVehicle());
		
		double totalCosts = tp_costs_prevAct_newAct + tp_costs_newAct_nextAct + act_costs_newAct + act_costs_nextAct; 
		
		double oldCosts;
		double oldTime;
		if(iFacts.getRoute().isEmpty()){
			oldCosts = 0.0;
			oldTime = 0.0;
		}
		else{
			double tp_costs_prevAct_nextAct = routingCosts.getTransportCost(prevAct.getLocationId(), nextAct.getLocationId(), prevAct.getEndTime(), iFacts.getRoute().getDriver(), iFacts.getRoute().getVehicle());
			double arrTime_nextAct = routingCosts.getTransportTime(prevAct.getLocationId(), nextAct.getLocationId(), prevAct.getEndTime(), iFacts.getNewDriver(), iFacts.getNewVehicle());
			
			double actCost_nextAct = activityCosts.getActivityCost(nextAct, arrTime_nextAct, iFacts.getRoute().getDriver(), iFacts.getRoute().getVehicle());
			oldCosts = tp_costs_prevAct_nextAct + actCost_nextAct;
			oldTime = (nextAct.getArrTime() - iFacts.getRoute().getDepartureTime());
		}
		
		double additionalCosts = totalCosts - oldCosts;
		double additionalTime = (nextAct_arrTime - iFacts.getNewDepTime()) - oldTime;

		return new Marginals(additionalCosts,additionalTime);
	}

}
