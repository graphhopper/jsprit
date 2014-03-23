package jsprit.core.algorithm.recreate;

import jsprit.core.problem.constraint.SoftActivityConstraint;
import jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import jsprit.core.problem.misc.JobInsertionContext;
import jsprit.core.problem.solution.route.activity.End;
import jsprit.core.problem.solution.route.activity.TourActivity;
import jsprit.core.util.CalculationUtils;

public class VariableTransportCostCalculator implements SoftActivityConstraint{

	private VehicleRoutingTransportCosts routingCosts;
	
	public VariableTransportCostCalculator(VehicleRoutingTransportCosts routingCosts) {
		super();
		this.routingCosts = routingCosts;
	}

	@Override
	public double getCosts(JobInsertionContext iFacts, TourActivity prevAct,TourActivity newAct, TourActivity nextAct, double depTimeAtPrevAct) {
		double tp_costs_prevAct_newAct = routingCosts.getTransportCost(prevAct.getLocationId(), newAct.getLocationId(), depTimeAtPrevAct, iFacts.getNewDriver(), iFacts.getNewVehicle());
		double tp_time_prevAct_newAct = routingCosts.getTransportTime(prevAct.getLocationId(), newAct.getLocationId(), depTimeAtPrevAct, iFacts.getNewDriver(), iFacts.getNewVehicle());
		
		double newAct_arrTime = depTimeAtPrevAct + tp_time_prevAct_newAct;
		double newAct_endTime = CalculationUtils.getActivityEndTime(newAct_arrTime, newAct);
		
		//open routes
		if(nextAct instanceof End){
			if(!iFacts.getNewVehicle().isReturnToDepot()){
				return tp_costs_prevAct_newAct;
			}
		}
		
		double tp_costs_newAct_nextAct = routingCosts.getTransportCost(newAct.getLocationId(), nextAct.getLocationId(), newAct_endTime, iFacts.getNewDriver(), iFacts.getNewVehicle());	
		double totalCosts = tp_costs_prevAct_newAct + tp_costs_newAct_nextAct; 
		
		double oldCosts;
		if(iFacts.getRoute().isEmpty()){
			double tp_costs_prevAct_nextAct = routingCosts.getTransportCost(prevAct.getLocationId(), nextAct.getLocationId(), depTimeAtPrevAct, iFacts.getNewDriver(), iFacts.getNewVehicle());
			oldCosts = tp_costs_prevAct_nextAct;
		}
		else{
			double tp_costs_prevAct_nextAct = routingCosts.getTransportCost(prevAct.getLocationId(), nextAct.getLocationId(), prevAct.getEndTime(), iFacts.getRoute().getDriver(), iFacts.getRoute().getVehicle());
			oldCosts = tp_costs_prevAct_nextAct;
		}
		return totalCosts - oldCosts;
	}

}
