package com.graphhopper.jsprit.core.problem.cost;

import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;

public class SoftTimeWindowCost {
	
	public double getSoftTimeWindowCost(TourActivity act, double arrTime, Vehicle vehicle){
		double act_OperationStart = Math.max(arrTime, act.getTheoreticalEarliestOperationStartTime());
		double cost = 0.;
		if(act_OperationStart < act.getSoftLowerBoundOperationStartTime())
			cost += (act.getSoftLowerBoundOperationStartTime() - act_OperationStart)*vehicle.getType().getVehicleCostParams().perLowerLatenessTimeUnit;
        if(act_OperationStart > act.getSoftUpperBoundOperationStartTime())
        	cost += (act_OperationStart - act.getSoftUpperBoundOperationStartTime())*vehicle.getType().getVehicleCostParams().perUpperLatenessTimeUnit;
		return cost;
	}

}
