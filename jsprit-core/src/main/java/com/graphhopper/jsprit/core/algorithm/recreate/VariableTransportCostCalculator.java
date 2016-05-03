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
import com.graphhopper.jsprit.core.problem.cost.SetupTime;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingActivityCosts;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import com.graphhopper.jsprit.core.problem.misc.JobInsertionContext;
import com.graphhopper.jsprit.core.problem.solution.route.activity.End;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;

public class VariableTransportCostCalculator implements SoftActivityConstraint {

    private final VehicleRoutingTransportCosts routingCosts;

    private final VehicleRoutingActivityCosts activityCosts;

    private SetupTime setupCosts = new SetupTime();

    public VariableTransportCostCalculator(VehicleRoutingTransportCosts routingCosts, VehicleRoutingActivityCosts activityCosts) {
        super();
        this.routingCosts = routingCosts;
        this.activityCosts = activityCosts;
    }

    @Override
    public double getCosts(JobInsertionContext iFacts, TourActivity prevAct, TourActivity newAct, TourActivity nextAct, double depTimeAtPrevAct) {
    	
        double setup_time_prevAct_newAct = setupCosts.getSetupTime(prevAct, newAct, iFacts.getNewVehicle());
        double setup_costs_prevAct_newAct = setupCosts.getSetupCost(setup_time_prevAct_newAct, iFacts.getNewVehicle());
        double tp_costs_prevAct_newAct = routingCosts.getTransportCost(prevAct.getLocation(), newAct.getLocation(), depTimeAtPrevAct, iFacts.getNewDriver(), iFacts.getNewVehicle());
        double tp_time_prevAct_newAct = routingCosts.getTransportTime(prevAct.getLocation(), newAct.getLocation(), depTimeAtPrevAct, iFacts.getNewDriver(), iFacts.getNewVehicle());

        double newAct_arrTime = depTimeAtPrevAct + tp_time_prevAct_newAct;
        double newAct_readyTime = newAct_arrTime + setup_time_prevAct_newAct;
        double newAct_endTime = Math.max(newAct_readyTime, newAct.getTheoreticalEarliestOperationStartTime()) + activityCosts.getActivityDuration(newAct,newAct_readyTime,iFacts.getNewDriver(),iFacts.getNewVehicle());

        //open routes
        if (nextAct instanceof End) {
            if (!iFacts.getNewVehicle().isReturnToDepot()) {
                return tp_costs_prevAct_newAct;
            }
        }

        double setup_time_newAct_nextAct = setupCosts.getSetupTime(newAct, nextAct, iFacts.getNewVehicle());
        double setup_costs_newAct_nextAct = setupCosts.getSetupCost(setup_time_newAct_nextAct, iFacts.getNewVehicle());
        double tp_costs_newAct_nextAct = routingCosts.getTransportCost(newAct.getLocation(), nextAct.getLocation(), newAct_endTime, iFacts.getNewDriver(), iFacts.getNewVehicle());
        double totalCosts = tp_costs_prevAct_newAct + setup_costs_prevAct_newAct + tp_costs_newAct_nextAct + setup_costs_newAct_nextAct;

        double oldCosts;
        if (iFacts.getRoute().isEmpty()) {
            double tp_costs_prevAct_nextAct = routingCosts.getTransportCost(prevAct.getLocation(), nextAct.getLocation(), depTimeAtPrevAct, iFacts.getNewDriver(), iFacts.getNewVehicle());
            oldCosts = tp_costs_prevAct_nextAct;
        } else {
            double setup_costs_prevAct_nextAct = setupCosts.getSetupCost(prevAct, nextAct, iFacts.getNewVehicle());
            double tp_costs_prevAct_nextAct = routingCosts.getTransportCost(prevAct.getLocation(), nextAct.getLocation(), prevAct.getEndTime(), iFacts.getRoute().getDriver(), iFacts.getRoute().getVehicle());
            oldCosts = tp_costs_prevAct_nextAct + setup_costs_prevAct_nextAct;
        }
        return totalCosts - oldCosts;
    }

}
