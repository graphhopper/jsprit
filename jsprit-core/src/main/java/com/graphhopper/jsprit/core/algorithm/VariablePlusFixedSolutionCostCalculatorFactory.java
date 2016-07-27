/*
 * Licensed to GraphHopper GmbH under one or more contributor
 * license agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * GraphHopper GmbH licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.graphhopper.jsprit.core.algorithm;

import com.graphhopper.jsprit.core.algorithm.state.InternalStates;
import com.graphhopper.jsprit.core.problem.solution.SolutionCostCalculator;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.state.RouteAndActivityStateGetter;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;

/**
 * Default objective function which is the sum of all fixed vehicle and variable
 * transportation costs, i.e. each is generated solution is evaluated according
 * this objective function.
 *
 * @author schroeder
 */
public class VariablePlusFixedSolutionCostCalculatorFactory {

    private RouteAndActivityStateGetter stateManager;

    public VariablePlusFixedSolutionCostCalculatorFactory(RouteAndActivityStateGetter stateManager) {
        super();
        this.stateManager = stateManager;
    }

    public SolutionCostCalculator createCalculator() {
        return new SolutionCostCalculator() {

            @Override
            public double getCosts(VehicleRoutingProblemSolution solution) {
                double c = 0.0;
                for (VehicleRoute r : solution.getRoutes()) {
                    c += stateManager.getRouteState(r, InternalStates.COSTS, Double.class);
                    c += getFixedCosts(r.getVehicle());
                }
                c += solution.getUnassignedJobs().size() * c * .1;
                return c;
            }

            private double getFixedCosts(Vehicle vehicle) {
                if (vehicle == null) return 0.0;
                if (vehicle.getType() == null) return 0.0;
                return vehicle.getType().getVehicleCostParams().fix;
            }
        };
    }

}
