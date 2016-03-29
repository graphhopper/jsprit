/*******************************************************************************
 * Copyright (C) 2014  Stefan Schroeder
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package com.graphhopper.jsprit.core.algorithm.recreate;

import com.graphhopper.jsprit.core.problem.constraint.SoftActivityConstraint;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingActivityCosts;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import com.graphhopper.jsprit.core.problem.misc.JobInsertionContext;
import com.graphhopper.jsprit.core.problem.solution.route.activity.End;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;

public class VariableTransportCostCalculator implements SoftActivityConstraint {

    private final VehicleRoutingTransportCosts routingCosts;

    private final VehicleRoutingActivityCosts activityCosts;

    public VariableTransportCostCalculator(VehicleRoutingTransportCosts routingCosts, VehicleRoutingActivityCosts activityCosts) {
        super();
        this.routingCosts = routingCosts;
        this.activityCosts = activityCosts;
    }

    @Override
    public double getCosts(JobInsertionContext iFacts, TourActivity prevAct, TourActivity newAct, TourActivity nextAct, double depTimeAtPrevAct) {
        double coef = 1.0;
        if(iFacts.getNewVehicle() != null)
        	coef = iFacts.getNewVehicle().getCoefSetupTime();
    	
    	double setup_time_prevAct_newAct = 0.0;
    	if(!prevAct.getLocation().equals(newAct.getLocation()))
    		setup_time_prevAct_newAct = newAct.getSetupTime() * coef;
    	double setup_costs_prevAct_newAct = setup_time_prevAct_newAct * iFacts.getNewVehicle().getType().getVehicleCostParams().perTransportTimeUnit;
        double tp_costs_prevAct_newAct = setup_costs_prevAct_newAct + routingCosts.getTransportCost(prevAct.getLocation(), newAct.getLocation(), depTimeAtPrevAct, iFacts.getNewDriver(), iFacts.getNewVehicle());
        double tp_time_prevAct_newAct = setup_time_prevAct_newAct + routingCosts.getTransportTime(prevAct.getLocation(), newAct.getLocation(), depTimeAtPrevAct, iFacts.getNewDriver(), iFacts.getNewVehicle());

        double newAct_arrTime = depTimeAtPrevAct + tp_time_prevAct_newAct;
        double newAct_endTime = Math.max(newAct_arrTime, newAct.getTheoreticalEarliestOperationStartTime()) + activityCosts.getActivityDuration(newAct,newAct_arrTime,iFacts.getNewDriver(),iFacts.getNewVehicle());

        //open routes
        if (nextAct instanceof End) {
            if (!iFacts.getNewVehicle().isReturnToDepot()) {
                return tp_costs_prevAct_newAct;
            }
        }

        double setup_time_newAct_nextAct = 0.0;
        if(!newAct.getLocation().equals(nextAct.getLocation()))
        	setup_time_newAct_nextAct = nextAct.getSetupTime() * coef;
        double setup_costs_newAct_nextAct = setup_time_newAct_nextAct * iFacts.getNewVehicle().getType().getVehicleCostParams().perTransportTimeUnit;
        double tp_costs_newAct_nextAct = setup_costs_newAct_nextAct + routingCosts.getTransportCost(newAct.getLocation(), nextAct.getLocation(), newAct_endTime, iFacts.getNewDriver(), iFacts.getNewVehicle());
        double totalCosts = tp_costs_prevAct_newAct + tp_costs_newAct_nextAct;

        double oldCosts;
        if (iFacts.getRoute().isEmpty()) {
            double tp_costs_prevAct_nextAct = routingCosts.getTransportCost(prevAct.getLocation(), nextAct.getLocation(), depTimeAtPrevAct, iFacts.getNewDriver(), iFacts.getNewVehicle());
            oldCosts = tp_costs_prevAct_nextAct;
        } else {
        	double setup_time_prevAct_nextAct = 0.0;
            if(!prevAct.getLocation().equals(nextAct.getLocation()))
            	setup_time_prevAct_nextAct = nextAct.getSetupTime() * coef;
            double setup_costs_prevAct_nextAct = setup_time_prevAct_nextAct * iFacts.getNewVehicle().getType().getVehicleCostParams().perTransportTimeUnit;
            double tp_costs_prevAct_nextAct = setup_costs_prevAct_nextAct + routingCosts.getTransportCost(prevAct.getLocation(), nextAct.getLocation(), prevAct.getEndTime(), iFacts.getRoute().getDriver(), iFacts.getRoute().getVehicle());
            oldCosts = tp_costs_prevAct_nextAct;
        }
        return totalCosts - oldCosts;
    }

}
