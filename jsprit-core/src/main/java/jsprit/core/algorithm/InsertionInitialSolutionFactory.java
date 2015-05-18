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

import jsprit.core.algorithm.recreate.InsertionStrategy;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.job.Job;
import jsprit.core.problem.solution.InitialSolutionFactory;
import jsprit.core.problem.solution.SolutionCostCalculator;
import jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import jsprit.core.problem.solution.route.VehicleRoute;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public final class InsertionInitialSolutionFactory implements InitialSolutionFactory {

	private static final Logger logger = LogManager.getLogger(InsertionInitialSolutionFactory.class);

	private final InsertionStrategy insertion;

	private SolutionCostCalculator solutionCostsCalculator;

	public InsertionInitialSolutionFactory(InsertionStrategy insertionStrategy, SolutionCostCalculator solutionCostCalculator) {
		super();
		this.insertion = insertionStrategy;
		this.solutionCostsCalculator = solutionCostCalculator;
	}

	@Override
	public VehicleRoutingProblemSolution createSolution(final VehicleRoutingProblem vrp) {
		logger.info("create initial solution");
		List<VehicleRoute> vehicleRoutes = new ArrayList<VehicleRoute>();
		vehicleRoutes.addAll(vrp.getInitialVehicleRoutes());
		Collection<Job> badJobs = insertion.insertJobs(vehicleRoutes, getUnassignedJobs(vrp));
		VehicleRoutingProblemSolution solution = new VehicleRoutingProblemSolution(vehicleRoutes, badJobs, Double.MAX_VALUE);
		double costs = solutionCostsCalculator.getCosts(solution);
		solution.setCost(costs);
		return solution;
	}

	private List<Job> getUnassignedJobs(VehicleRoutingProblem vrp) {
		return new ArrayList<Job>(vrp.getJobs().values());
	}

}
