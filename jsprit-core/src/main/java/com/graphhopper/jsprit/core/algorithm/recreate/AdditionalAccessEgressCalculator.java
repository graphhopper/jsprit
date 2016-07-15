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

import com.graphhopper.jsprit.core.problem.cost.SetupTime;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import com.graphhopper.jsprit.core.problem.driver.Driver;
import com.graphhopper.jsprit.core.problem.misc.JobInsertionContext;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;

/**
 * Estimates additional access/egress costs when operating route with a new vehicle that has different start/end-location.
 * <p>
 * <p>If two vehicles have the same start/end-location and departure-time .getCosts(...) must return zero.
 *
 * @author schroeder
 */
class AdditionalAccessEgressCalculator {

    private VehicleRoutingTransportCosts routingCosts;

    private SetupTime setupCosts = new SetupTime();

    /**
     * Constructs the estimator that estimates additional access/egress costs when operating route with a new vehicle that has different start/end-location.
     * <p>
     * <p>If two vehicles have the same start/end-location and departure-time .getCosts(...) must return zero.
     *
     * @author schroeder
     */
    public AdditionalAccessEgressCalculator(VehicleRoutingTransportCosts routingCosts) {
        this.routingCosts = routingCosts;
    }

    public double getCosts(JobInsertionContext insertionContext) {
        double delta_access = 0.0;
        double delta_egress = 0.0;
        VehicleRoute currentRoute = insertionContext.getRoute();
        Vehicle newVehicle = insertionContext.getNewVehicle();
        Driver newDriver = insertionContext.getNewDriver();
        double newVehicleDepartureTime = insertionContext.getNewDepTime();
        if (!currentRoute.isEmpty()) {
            double accessSetupCostNew = setupCosts.getSetupCost(newVehicle.getStartLocation(), currentRoute.getActivities().get(0), newVehicle);
            double accessSetupCostold = setupCosts.getSetupCost(currentRoute.getStart(), currentRoute.getActivities().get(0), currentRoute.getVehicle());
            double accessTransportCostNew = routingCosts.getTransportCost(newVehicle.getStartLocation(), currentRoute.getActivities().get(0).getLocation(), newVehicleDepartureTime, newDriver, newVehicle);
            double accessTransportCostOld = routingCosts.getTransportCost(currentRoute.getStart().getLocation(), currentRoute.getActivities().get(0).getLocation(), currentRoute.getDepartureTime(), currentRoute.getDriver(), currentRoute.getVehicle());

            delta_access = accessTransportCostNew + accessSetupCostNew - accessTransportCostOld - accessSetupCostold;

            if (newVehicle.isReturnToDepot()) {
                TourActivity lastActivityBeforeEndOfRoute = currentRoute.getActivities().get(currentRoute.getActivities().size() - 1);
                double lastActivityEndTimeWithOldVehicleAndDepartureTime = lastActivityBeforeEndOfRoute.getEndTime();
                double lastActivityEndTimeEstimationWithNewVehicleAndNewDepartureTime = Math.max(0.0, lastActivityEndTimeWithOldVehicleAndDepartureTime + (newVehicleDepartureTime - currentRoute.getDepartureTime()));
                double egressTransportCostNew = routingCosts.getTransportCost(lastActivityBeforeEndOfRoute.getLocation(), newVehicle.getEndLocation(), lastActivityEndTimeEstimationWithNewVehicleAndNewDepartureTime, newDriver, newVehicle);
                double egressTransportCostOld = routingCosts.getTransportCost(lastActivityBeforeEndOfRoute.getLocation(), currentRoute.getEnd().getLocation(), lastActivityEndTimeWithOldVehicleAndDepartureTime, currentRoute.getDriver(), currentRoute.getVehicle());

                delta_egress = egressTransportCostNew - egressTransportCostOld;
            }
        }
        return delta_access + delta_egress;
    }

}
