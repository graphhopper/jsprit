/*******************************************************************************
 * Copyright (C) 2013  Stefan Schroeder
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * 
 * Contributors:
 *     Stefan Schroeder - initial API and implementation
 ******************************************************************************/
package examples;

import java.io.File;
import java.util.Collection;

import algorithms.VehicleRoutingAlgorithms;
import algorithms.selectors.SelectBest;
import analysis.AlgorithmSearchProgressChartListener;
import analysis.Plotter;
import analysis.Plotter.Label;
import analysis.SolutionPlotter;
import analysis.SolutionPrinter;
import analysis.SolutionPrinter.Print;
import basics.VehicleRoutingAlgorithm;
import basics.VehicleRoutingProblem;
import basics.VehicleRoutingProblemSolution;
import basics.VehicleRoutingProblem.Constraint;
import basics.io.VrpXMLReader;

public class PickupAndDeliveryExample {
	
	public static void main(String[] args) {
		
		/*
		 * some preparation - create output folder
		 */
		File dir = new File("output");
		// if the directory does not exist, create it
		if (!dir.exists()){
			System.out.println("creating directory ./output");
			boolean result = dir.mkdir();  
			if(result) System.out.println("./output created");  
		}
		
		/*
		 * Build the problem.
		 * 
		 * But define a problem-builder first.
		 */
		VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
		
		/*
		 * A solomonReader reads solomon-instance files, and stores the required information in the builder.
		 */
		new VrpXMLReader(vrpBuilder).read("input/pickups_and_deliveries_solomon_r101.xml");
		
		/*
		 * Finally, the problem can be built. By default, transportCosts are crowFlyDistances (as usually used for vrp-instances).
		 */
		
		VehicleRoutingProblem vrp = vrpBuilder.build();
		
		SolutionPlotter.plotVrpAsPNG(vrp, "output/pd_solomon_r101.png", "pd_r101");
		
		/*
		 * Define the required vehicle-routing algorithms to solve the above problem.
		 * 
		 * The algorithm can be defined and configured in an xml-file.
		 */
//		VehicleRoutingAlgorithm vra = new SchrimpfFactory().createAlgorithm(vrp);
		VehicleRoutingAlgorithm vra = VehicleRoutingAlgorithms.readAndCreateAlgorithm(vrp, "input/algorithmConfig_solomon.xml");
		vra.getAlgorithmListeners().addListener(new AlgorithmSearchProgressChartListener("output/sol_progress.png"));
		/*
		 * Solve the problem.
		 * 
		 *
		 */
		Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();
		
		/*
		 * Retrieve best solution.
		 */
		VehicleRoutingProblemSolution solution = new SelectBest().selectSolution(solutions);
		
		/*
		 * print solution
		 */
		SolutionPrinter.print(solution, Print.VERBOSE);
		
		/*
		 * Plot solution. 
		 */
//		SolutionPlotter.plotSolutionAsPNG(vrp, solution, "output/pd_solomon_r101_solution.png","pd_r101");
		Plotter plotter = new Plotter(vrp, solution);
		plotter.setLabel(Label.SIZE);
		plotter.setShowFirstActivity(true);
		plotter.plot("output/pd_solomon_r101_solution.png","pd_r101");
	
		
		
	}

}
