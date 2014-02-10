package jsprit.core.problem.constraint;

import jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import jsprit.core.problem.misc.JobInsertionContext;
import jsprit.core.problem.solution.route.activity.End;
import jsprit.core.problem.solution.route.activity.TourActivity;
import jsprit.core.util.CalculationUtils;

/**
 * Calculates additional transportation costs induced by inserting newAct.
 * 
 * @author schroeder
 *
 */
class AdditionalTransportationCosts implements SoftActivityConstraint{

	private VehicleRoutingTransportCosts routingCosts;
	
	/**
	 * Constructs the calculator that calculates additional transportation costs induced by inserting new activity.
	 *
	 * <p>It is calculated at local level, i.e. the additional costs of inserting act_new between act_i and act_j is c(act_i,act_new,newVehicle)+c(act_new,act_j,newVehicle)-c(act_i,act_j,oldVehicle)
	 * <p>If newVehicle.isReturnToDepot == false then the additional costs of inserting act_new between act_i and end is c(act_i,act_new) [since act_new is then the new end-of-route]
	 *
	 * @param routingCosts
	 */
	public AdditionalTransportationCosts(VehicleRoutingTransportCosts routingCosts) {
		super();
		this.routingCosts = routingCosts;
	}

	/**
	 * Returns additional transportation costs induced by inserting newAct.
	 * 
	 * <p>It is calculated at local level, i.e. the additional costs of inserting act_new between act_i and act_j is c(act_i,act_new,newVehicle)+c(act_new,act_j,newVehicle)-c(act_i,act_j,oldVehicle)
	 * <p>If newVehicle.isReturnToDepot == false then the additional costs of inserting act_new between act_i and end is c(act_i,act_new) [since act_new is then the new end-of-route]
	 */
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
		
		double additionalCosts = totalCosts - oldCosts;
		return additionalCosts;
	}

}
