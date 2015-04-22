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
import jsprit.core.problem.io.VrpXMLReader;
import jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import jsprit.core.problem.vehicle.VehicleImpl;
import jsprit.core.problem.vehicle.VehicleTypeImpl;
import jsprit.core.reporting.SolutionPrinter;
import jsprit.core.util.Coordinate;
import jsprit.core.util.Solutions;
import jsprit.util.Examples;

import java.util.Arrays;
import java.util.Collection;


public class MultipleDepotExample {

	public static void main(String[] args) {
		/*
		 * some preparation - create output folder
		 */
		Examples.createOutputFolder();
		
		VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
		/*
		 * Read cordeau-instance p01, BUT only its services without any vehicles 
		 */
		new VrpXMLReader(vrpBuilder).read("input/vrp_cordeau_01.xml");
		
		/*
		 * add vehicles with its depots
		 * 4 depots:
		 * (20,20)
		 * (30,40)
		 * (50,30)
		 * (60,50)
		 * 
		 * each with 4 vehicles each with a capacity of 80
		 */
		int nuOfVehicles = 4;
		int capacity = 80;
		Coordinate firstDepotCoord = Coordinate.newInstance(20, 20);
		Coordinate second = Coordinate.newInstance(30, 40);
		Coordinate third = Coordinate.newInstance(50, 30);
		Coordinate fourth = Coordinate.newInstance(60, 50);
		
		int depotCounter = 1;
		for(Coordinate depotCoord : Arrays.asList(firstDepotCoord,second,third,fourth)){
			for(int i=0;i<nuOfVehicles;i++){
				VehicleTypeImpl vehicleType = VehicleTypeImpl.Builder.newInstance(depotCounter + "_type").addCapacityDimension(0, capacity).setCostPerDistance(1.0).build();
				VehicleImpl vehicle = VehicleImpl.Builder.newInstance(depotCounter + "_" + (i + 1) + "_vehicle").setStartLocation(Location.newInstance(depotCoord.getX(), depotCoord.getY())).setType(vehicleType).build();
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
//		SolutionPlotter.plotVrpAsPNG(vrp, "output/problem01.png", "p01");

		/*
		 * solve the problem
		 */
		VehicleRoutingAlgorithm vra = Jsprit.Builder.newInstance(vrp).setProperty(Jsprit.Parameter.THREADS,"5").buildAlgorithm();
		vra.getAlgorithmListeners().addListener(new StopWatch(),Priority.HIGH);
		vra.getAlgorithmListeners().addListener(new AlgorithmSearchProgressChartListener("output/progress.png"));
		Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();
		
		SolutionPrinter.print(Solutions.bestOf(solutions));
		
		new Plotter(vrp, Solutions.bestOf(solutions)).plot("output/p01_solution.png", "p01");
		
		new GraphStreamViewer(vrp, Solutions.bestOf(solutions)).setRenderDelay(100).display();
		
	}

}
