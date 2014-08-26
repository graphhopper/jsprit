/*******************************************************************************
 * Copyright (c) 2014 Stefan Schroeder.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either 
 * version 3.0 of the License, or (at your option) any later version.
 *  
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Stefan Schroeder - initial API and implementation
 ******************************************************************************/
package jsprit.core.algorithm;

import jsprit.core.algorithm.state.InternalStates;
import jsprit.core.problem.solution.SolutionCostCalculator;
import jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.solution.route.state.RouteAndActivityStateGetter;
import jsprit.core.problem.vehicle.Vehicle;

/**
 * Default objective function which is the sum of all fixed vehicle and variable
 * transportation costs, i.e. each is generated solution is evaluated according
 * this objective function.
 * 
 * @author schroeder
 *
 */
public class VariablePlusFixedSolutionCostCalculatorFactory {
	
	private RouteAndActivityStateGetter stateManager;
	
	public VariablePlusFixedSolutionCostCalculatorFactory(RouteAndActivityStateGetter stateManager) {
		super();
		this.stateManager = stateManager;
	}

	public SolutionCostCalculator createCalculator(){
		return new SolutionCostCalculator() {

			@Override
			public double getCosts(VehicleRoutingProblemSolution solution) {
				double c = 0.0;
                for(VehicleRoute r : solution.getRoutes()){
					c += stateManager.getRouteState(r, InternalStates.COSTS, Double.class);
					c += getFixedCosts(r.getVehicle());
				}
                c += solution.getUnassignedJobs().size() * c * .1;
				return c;
			}

            private double getFixedCosts(Vehicle vehicle) {
                if(vehicle == null) return 0.0;
                if(vehicle.getType() == null) return 0.0;
                return vehicle.getType().getVehicleCostParams().fix;
            }
		};
	}

}
