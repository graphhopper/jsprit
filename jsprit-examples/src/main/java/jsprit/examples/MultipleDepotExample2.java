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

import jsprit.analysis.toolbox.AlgorithmSearchProgressChartListener;
import jsprit.analysis.toolbox.GraphStreamViewer;
import jsprit.analysis.toolbox.Plotter;
import jsprit.analysis.toolbox.StopWatch;
import jsprit.core.algorithm.VehicleRoutingAlgorithm;
import jsprit.core.algorithm.box.Jsprit;
import jsprit.core.algorithm.listener.VehicleRoutingAlgorithmListeners.Priority;
import jsprit.core.problem.Location;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.VehicleRoutingProblem.FleetSize;
import jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import jsprit.core.problem.vehicle.VehicleImpl;
import jsprit.core.problem.vehicle.VehicleType;
import jsprit.core.problem.vehicle.VehicleTypeImpl;
import jsprit.core.reporting.SolutionPrinter;
import jsprit.core.util.Coordinate;
import jsprit.core.util.Solutions;
import jsprit.instance.reader.CordeauReader;
import jsprit.util.Examples;

import java.util.Arrays;
import java.util.Collection;


public class MultipleDepotExample2 {


	public static void main(String[] args) {
		/*
		 * some preparation - create output folder
		 */
		Examples.createOutputFolder();
		
		VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
		/*
		 * Read cordeau-instance p01, BUT only its services without any vehicles 
		 */
		new CordeauReader(vrpBuilder).read("input/p08");
		
		/*
		 * add vehicles with its depots
		 * 2 depots:
		 * (-33,33)
		 * (33,-33)
		 * 
		 * each with 14 vehicles each with a capacity of 500 and a maximum duration of 310
		 */
		int nuOfVehicles = 13;
		int capacity = 500;
		double maxDuration = 310;
		Coordinate firstDepotCoord = Coordinate.newInstance(-33, 33);
		Coordinate second = Coordinate.newInstance(33, -33);
		
		int depotCounter = 1;
		for(Coordinate depotCoord : Arrays.asList(firstDepotCoord,second)){
			for(int i=0;i<nuOfVehicles;i++){
				VehicleType vehicleType = VehicleTypeImpl.Builder.newInstance(depotCounter + "_type")
                        .addCapacityDimension(0, capacity).setCostPerDistance(1.0).build();
				String vehicleId = depotCounter + "_" + (i+1) + "_vehicle";
				VehicleImpl.Builder vehicleBuilder = VehicleImpl.Builder.newInstance(vehicleId);
				vehicleBuilder.setStartLocation(Location.newInstance(depotCoord.getX(),depotCoord.getY()));
				vehicleBuilder.setType(vehicleType);
				vehicleBuilder.setLatestArrival(maxDuration);
				VehicleImpl vehicle = vehicleBuilder.build();
				vrpBuilder.addVehicle(vehicle);
			}
			depotCounter++;
		}

		
		/*
		 * define problem with finite fleet
		 */
		vrpBuilder.setFleetSize(FleetSize.FINITE);
		
		/*
		 * build the problem
		 */
		VehicleRoutingProblem vrp = vrpBuilder.build();
		
		/*
		 * plot to see how the problem looks like
		 */
//		SolutionPlotter.plotVrpAsPNG(vrp, "output/problem08.png", "p08");

		/*
		 * solve the problem
		 */
		VehicleRoutingAlgorithm vra = Jsprit.Builder.newInstance(vrp).setProperty(Jsprit.Parameter.THREADS,"5").buildAlgorithm();
		vra.setMaxIterations(2000);
        vra.getAlgorithmListeners().addListener(new StopWatch(),Priority.HIGH);
		vra.getAlgorithmListeners().addListener(new AlgorithmSearchProgressChartListener("output/progress.png"));
		Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();
		
		SolutionPrinter.print(vrp, Solutions.bestOf(solutions), jsprit.core.reporting.SolutionPrinter.Print.VERBOSE);

		new Plotter(vrp, Solutions.bestOf(solutions)).plot("output/p08_solution.png", "p08");
		
		new GraphStreamViewer(vrp,Solutions.bestOf(solutions)).setRenderDelay(50).display();
	}

}
