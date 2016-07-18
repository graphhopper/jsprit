package com.graphhopper.jsprit.core.problem.cost;

import java.util.List;

import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.End;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;

public class SoftTimeWindowCost {

    private final VehicleRoutingTransportCosts routingCosts;
    
    private boolean hasSoftTimeWindows;

    public SoftTimeWindowCost(VehicleRoutingTransportCosts routingCosts, boolean hasSoftTimeWindows) {
        this.routingCosts = routingCosts;
        this.hasSoftTimeWindows = hasSoftTimeWindows;
    }
    
    public void setHasSoftTimeWindows(boolean activ) {
        this.hasSoftTimeWindows = activ;
    }
	
	public double getSoftTimeWindowCost(TourActivity act, double arrTime, Vehicle vehicle){
        double act_OperationStart = Math.max(arrTime, act.getTheoreticalEarliestOperationStartTime());
        double cost = 0.;
        if(!hasSoftTimeWindows) return 0.;
        if(act_OperationStart < act.getSoftLowerBoundOperationStartTime())
            cost += (act.getSoftLowerBoundOperationStartTime() - act_OperationStart)*vehicle.getType().getVehicleCostParams().perLowerLatenessTimeUnit;
        if(act_OperationStart > act.getSoftUpperBoundOperationStartTime())
            cost += (act_OperationStart - act.getSoftUpperBoundOperationStartTime())*vehicle.getType().getVehicleCostParams().perUpperLatenessTimeUnit;
        return cost;
	}
	
	public double getSoftTimeWindowCost(VehicleRoute route, TourActivity prevAct, TourActivity newAct, TourActivity nextAct, double prevActDepTime){
        double cost = 0.;
        double cost_old = 0.0;
        if(!hasSoftTimeWindows) return 0.;
        List<TourActivity> tourActivities = route.getActivities();

        double newActArrTime = prevActDepTime + routingCosts.getTransportTime(prevAct.getLocation(), newAct.getLocation(), prevActDepTime, route.getDriver(), route.getVehicle());
        double newActEndTime = Math.max(newActArrTime, newAct.getTheoreticalEarliestOperationStartTime()) + newAct.getOperationTime();
        cost += getSoftTimeWindowCost(newAct, newActArrTime, route.getVehicle());

        if (!(nextAct instanceof End)) {
            boolean actAfterNew = false;
            for (TourActivity tourActivity : tourActivities) {
                if (!actAfterNew && !tourActivity.equals(nextAct))
                    continue;
                actAfterNew = true;

                prevAct = newAct;
                newAct = tourActivity;
                prevActDepTime = newActEndTime;

                newActArrTime = prevActDepTime + routingCosts.getTransportTime(prevAct.getLocation(), newAct.getLocation(), prevActDepTime, route.getDriver(), route.getVehicle());

                cost += getSoftTimeWindowCost(newAct, newActArrTime, route.getVehicle());
                cost_old += getSoftTimeWindowCost(newAct, newAct.getArrTime(), route.getVehicle());
                newActEndTime = Math.max(newActArrTime, newAct.getTheoreticalEarliestOperationStartTime()) + newAct.getOperationTime();
            }
        }
        return cost - cost_old;
	}
}
