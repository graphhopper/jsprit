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

package jsprit.core.algorithm.recreate;

import jsprit.core.algorithm.listener.VehicleRoutingAlgorithmListeners;
import jsprit.core.algorithm.recreate.listener.InsertionListener;
import jsprit.core.algorithm.state.StateManager;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.constraint.ConstraintManager;
import jsprit.core.problem.job.Job;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.vehicle.VehicleFleetManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by schroeder on 11.12.14.
 */
public class JobInsertionCostsCalculatorLightFactory {

    /**
     * Returns standard insertion calculator, i.e. the calculator that identifies best insertion positions for the
     * jobs to be inserted. The position basically consists of the route and the according indices.
     *
     * @param vrp vehicle routing problem
     * @param fleetManager fleet manager
     * @param stateManager state manager
     * @param constraintManager constraint manager
     * @return insertion calculator
     */
    public static JobInsertionCostsCalculatorLight createStandardCalculator(VehicleRoutingProblem vrp, VehicleFleetManager fleetManager, StateManager stateManager, ConstraintManager constraintManager){
        List<VehicleRoutingAlgorithmListeners.PrioritizedVRAListener> al = new ArrayList<VehicleRoutingAlgorithmListeners.PrioritizedVRAListener>();
        List<InsertionListener> il = new ArrayList<InsertionListener>();
        JobInsertionCostsCalculatorBuilder builder = new JobInsertionCostsCalculatorBuilder(il,al);
        builder.setVehicleRoutingProblem(vrp).setConstraintManager(constraintManager).setStateManager(stateManager).setVehicleFleetManager(fleetManager);
        final JobInsertionCostsCalculator calculator = builder.build();
        return new JobInsertionCostsCalculatorLight() {

            @Override
            public InsertionData getInsertionData(Job unassignedJob, VehicleRoute route, double bestKnownCosts) {
                return calculator.getInsertionData(route,unassignedJob,AbstractInsertionStrategy.NO_NEW_VEHICLE_YET,AbstractInsertionStrategy.NO_NEW_DEPARTURE_TIME_YET,AbstractInsertionStrategy.NO_NEW_DRIVER_YET,bestKnownCosts);
            }

        };
    }

}
