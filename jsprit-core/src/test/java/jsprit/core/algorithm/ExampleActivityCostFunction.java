/*******************************************************************************
 * Copyright (C) 2013  Stefan Schroeder
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
package jsprit.core.algorithm;

import jsprit.core.problem.cost.VehicleRoutingActivityCosts;
import jsprit.core.problem.driver.Driver;
import jsprit.core.problem.solution.route.activity.TourActivity;
import jsprit.core.problem.solution.route.activity.TourActivity.JobActivity;
import jsprit.core.problem.vehicle.Vehicle;


public class ExampleActivityCostFunction implements VehicleRoutingActivityCosts {

    public ExampleActivityCostFunction() {
        super();
    }

    public double parameter_timeAtAct;

    public double parameter_penaltyTooLate;


    @Override
    public double getActivityCost(TourActivity tourAct, double arrivalTime, Driver driver, Vehicle vehicle) {
        if (arrivalTime == Time.TOURSTART || arrivalTime == Time.UNDEFINED) {
            return 0.0;
        } else {
            //waiting + act-time
            double endTime = Math.max(arrivalTime, tourAct.getTheoreticalEarliestOperationStartTime()) + tourAct.getOperationTime();
            double timeAtAct = endTime - arrivalTime;

            double totalCost = timeAtAct * parameter_timeAtAct;

            //penalty tooLate
            if (tourAct instanceof JobActivity) {
                if (arrivalTime > tourAct.getTheoreticalLatestOperationStartTime()) {
                    double penTime = arrivalTime - tourAct.getTheoreticalLatestOperationStartTime();
                    totalCost += penTime * parameter_penaltyTooLate;
                }
            }
            return totalCost;

        }
    }

}
