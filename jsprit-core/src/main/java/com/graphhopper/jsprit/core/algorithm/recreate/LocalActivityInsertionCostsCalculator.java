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

import com.graphhopper.jsprit.core.algorithm.state.InternalStates;
import com.graphhopper.jsprit.core.problem.cost.SetupTime;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingActivityCosts;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import com.graphhopper.jsprit.core.problem.misc.JobInsertionContext;
import com.graphhopper.jsprit.core.problem.solution.route.activity.End;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.problem.solution.route.state.RouteAndActivityStateGetter;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;

/**
 * Calculates activity insertion costs locally, i.e. by comparing the additional costs of insertion the new activity k between
 * activity i (prevAct) and j (nextAct).
 * Additional costs are then basically calculated as delta c = c_ik + c_kj - c_ij.
 * <p>
 * <p>Note once time has an effect on costs this class requires activity endTimes.
 *
 * @author stefan
 */
class LocalActivityInsertionCostsCalculator implements ActivityInsertionCostsCalculator {

    private VehicleRoutingTransportCosts routingCosts;

    private VehicleRoutingActivityCosts activityCosts;

    private SetupTime setupCosts = new SetupTime();

    private double activityCostsWeight = 1.;

    private double solutionCompletenessRatio = 1.;

    private RouteAndActivityStateGetter stateManager;

    public LocalActivityInsertionCostsCalculator(VehicleRoutingTransportCosts routingCosts, VehicleRoutingActivityCosts actCosts, RouteAndActivityStateGetter stateManager) {
        super();
        this.routingCosts = routingCosts;
        this.activityCosts = actCosts;
        this.stateManager = stateManager;
    }

    @Override
    public double getCosts(JobInsertionContext iFacts, TourActivity prevAct, TourActivity nextAct, TourActivity newAct, double depTimeAtPrevAct) {
        double setup_time_prevAct_newAct = setupCosts.getSetupTime(prevAct, newAct, iFacts.getNewVehicle());
        double setup_cost_prevAct_newAct = setupCosts.getSetupCost(setup_time_prevAct_newAct, iFacts.getNewVehicle());
        double tp_costs_prevAct_newAct = routingCosts.getTransportCost(prevAct.getLocation(), newAct.getLocation(), depTimeAtPrevAct, iFacts.getNewDriver(), iFacts.getNewVehicle());
        double tp_time_prevAct_newAct = routingCosts.getTransportTime(prevAct.getLocation(), newAct.getLocation(), depTimeAtPrevAct, iFacts.getNewDriver(), iFacts.getNewVehicle());

        double newAct_arrTime = depTimeAtPrevAct + tp_time_prevAct_newAct;
        double newAct_readyTime = newAct_arrTime + setup_time_prevAct_newAct;
        double newAct_endTime = Math.max(newAct_readyTime, newAct.getTheoreticalEarliestOperationStartTime()) + activityCosts.getActivityDuration(newAct, newAct_readyTime, iFacts.getNewDriver(), iFacts.getNewVehicle());

        double act_costs_newAct = activityCosts.getActivityCost(newAct, newAct_readyTime, iFacts.getNewDriver(), iFacts.getNewVehicle());

        if (isEnd(nextAct) && !toDepot(iFacts.getNewVehicle())) return tp_costs_prevAct_newAct;

        double setup_time_newAct_nextAct = setupCosts.getSetupTime(newAct, nextAct, iFacts.getNewVehicle());
        double setup_cost_newAct_nextAct = setupCosts.getSetupCost(setup_time_newAct_nextAct, iFacts.getNewVehicle());
        double tp_costs_newAct_nextAct = routingCosts.getTransportCost(newAct.getLocation(), nextAct.getLocation(), newAct_endTime, iFacts.getNewDriver(), iFacts.getNewVehicle());
        double tp_time_newAct_nextAct = routingCosts.getTransportTime(newAct.getLocation(), nextAct.getLocation(), newAct_endTime, iFacts.getNewDriver(), iFacts.getNewVehicle());
        
        double nextAct_arrTime = newAct_endTime + tp_time_newAct_nextAct;
        double nextAct_readyTime = nextAct_arrTime + setup_time_newAct_nextAct;
        double endTime_nextAct_new = Math.max(nextAct_readyTime, nextAct.getTheoreticalEarliestOperationStartTime()) + activityCosts.getActivityDuration(nextAct, nextAct_readyTime, iFacts.getNewDriver(), iFacts.getNewVehicle());
        double act_costs_nextAct = activityCosts.getActivityCost(nextAct, nextAct_readyTime, iFacts.getNewDriver(), iFacts.getNewVehicle());

        double totalCosts = tp_costs_prevAct_newAct + tp_costs_newAct_nextAct + setup_cost_prevAct_newAct + setup_cost_newAct_nextAct + solutionCompletenessRatio * activityCostsWeight * (act_costs_newAct + act_costs_nextAct);

        double oldCosts = 0.;
        if (iFacts.getRoute().isEmpty()) {
            double tp_costs_prevAct_nextAct = routingCosts.getTransportCost(prevAct.getLocation(), nextAct.getLocation(), depTimeAtPrevAct, iFacts.getNewDriver(), iFacts.getNewVehicle());
            oldCosts += tp_costs_prevAct_nextAct;
        } else {
            double setup_time_prevAct_nextAct = setupCosts.getSetupTime(prevAct, nextAct, iFacts.getNewVehicle());
            double setup_cost_prevAct_nextAct = setupCosts.getSetupCost(setup_time_prevAct_nextAct, iFacts.getNewVehicle());
            double tp_costs_prevAct_nextAct = routingCosts.getTransportCost(prevAct.getLocation(), nextAct.getLocation(), prevAct.getEndTime(), iFacts.getRoute().getDriver(), iFacts.getRoute().getVehicle());
            double tp_time_prevAct_nextAct = routingCosts.getTransportTime(prevAct.getLocation(), nextAct.getLocation(), prevAct.getEndTime(), iFacts.getRoute().getDriver(), iFacts.getRoute().getVehicle());
            
            double arrTime_nextAct = depTimeAtPrevAct + tp_time_prevAct_nextAct;
            double readyTime_nextAct = arrTime_nextAct + setup_time_prevAct_nextAct;
            double endTime_nextAct_old = Math.max(readyTime_nextAct, nextAct.getTheoreticalEarliestOperationStartTime()) + activityCosts.getActivityDuration(nextAct, readyTime_nextAct, iFacts.getRoute().getDriver(),iFacts.getRoute().getVehicle());
            double actCost_nextAct = activityCosts.getActivityCost(nextAct, readyTime_nextAct, iFacts.getRoute().getDriver(), iFacts.getRoute().getVehicle());

            double endTimeDelay_nextAct = Math.max(0, endTime_nextAct_new - endTime_nextAct_old);
            Double futureWaiting = stateManager.getActivityState(nextAct, iFacts.getRoute().getVehicle(), InternalStates.FUTURE_WAITING, Double.class);
            if (futureWaiting == null) futureWaiting = 0.;
            double waitingTime_savings_timeUnit = Math.min(futureWaiting, endTimeDelay_nextAct);
            double waitingTime_savings = waitingTime_savings_timeUnit * iFacts.getRoute().getVehicle().getType().getVehicleCostParams().perWaitingTimeUnit;
            oldCosts += solutionCompletenessRatio * activityCostsWeight * waitingTime_savings;
            oldCosts += tp_costs_prevAct_nextAct + setup_cost_prevAct_nextAct + solutionCompletenessRatio * activityCostsWeight * actCost_nextAct;
        }
        return totalCosts - oldCosts;
    }

    private boolean toDepot(Vehicle newVehicle) {
        return newVehicle.isReturnToDepot();
    }

    private boolean isEnd(TourActivity nextAct) {
        return nextAct instanceof End;
    }

    public void setSolutionCompletenessRatio(double solutionCompletenessRatio) {
        this.solutionCompletenessRatio = solutionCompletenessRatio;
    }
}
