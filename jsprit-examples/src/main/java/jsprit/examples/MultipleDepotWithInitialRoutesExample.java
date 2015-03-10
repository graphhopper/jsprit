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
package jsprit.examples;

import jsprit.analysis.toolbox.Plotter;
import jsprit.analysis.toolbox.Plotter.Label;
import jsprit.core.algorithm.VehicleRoutingAlgorithm;
import jsprit.core.algorithm.io.VehicleRoutingAlgorithms;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.VehicleRoutingProblem.Builder;
import jsprit.core.problem.io.VrpXMLReader;
import jsprit.core.problem.job.Job;
import jsprit.core.problem.job.Service;
import jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.vehicle.Vehicle;
import jsprit.core.reporting.SolutionPrinter;
import jsprit.core.util.Solutions;
import jsprit.util.Examples;

import java.util.Collection;


public class MultipleDepotWithInitialRoutesExample {


	public static void main(String[] args) {
		/*
		 * some preparation - create output folder
		 */
		Examples.createOutputFolder();
		
		VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
		/*
		 * Read cordeau-instance p01
		 */
		new VrpXMLReader(vrpBuilder).read("input/cordeau01.xml");
		
		/*
		 * Add initial route with 1_4_vehicle and services 44, 26
		 */
		VehicleRoute initialRoute = VehicleRoute.Builder.newInstance(getVehicle("1_4_vehicle",vrpBuilder)).addService(getService("44",vrpBuilder))
				.addService(getService("26",vrpBuilder)).build();
		vrpBuilder.addInitialVehicleRoute(initialRoute);
		
		/*
		 * build the problem
		 */
		VehicleRoutingProblem vrp = vrpBuilder.build();
		/*
		 * since job (service) 26 and 44 are already planned in initial route and thus static AND sequence is fixed they
		 * should not be in jobMap anymore (only variable jobs are in jobMap)
		 */
		assert !vrp.getJobs().containsKey("26") : "strange. service 26 should not be part of the problem";
		assert !vrp.getJobs().containsKey("44") : "strange. service 44 should not be part of the problem";
		
		/*
		 * plot to see how the problem looks like
		 */
		new Plotter(vrp).setLabel(Label.ID).plot("output/cordeau01_problem_withInitialRoute.png", "c");

		/*
		 * solve the problem
		 */
//		VehicleRoutingAlgorithm vra = Jsprit.Builder.newInstance(vrp)
//				.setProperty(Jsprit.Parameter.ITERATIONS,"10000").buildAlgorithm();

		VehicleRoutingAlgorithm vra = VehicleRoutingAlgorithms.readAndCreateAlgorithm(vrp, "input/algorithmConfig_noVehicleSwitch.xml");
		Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();
		
		SolutionPrinter.print(Solutions.bestOf(solutions));
		
		new Plotter(vrp, Solutions.bestOf(solutions)).setLabel(Label.ID).plot("output/cordeau01_solution_withInitialRoute.png", "p01");
		
		
	}

	private static Service getService(String serviceId, Builder vrpBuilder) {
		for(Job j : vrpBuilder.getAddedJobs()){
			if(j.getId().equals(serviceId)){
				return (Service)j;
			}
		}
		return null;
	}

	private static Vehicle getVehicle(String vehicleId, Builder vrpBuilder) {
		for(Vehicle v : vrpBuilder.getAddedVehicles()){
			if(v.getId().equals(vehicleId)) return v;
		}
		return null;
	}

}
