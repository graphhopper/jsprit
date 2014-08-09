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
package jsprit.examples;

import jsprit.analysis.toolbox.AlgorithmSearchProgressChartListener;
import jsprit.analysis.toolbox.GraphStreamViewer;
import jsprit.analysis.toolbox.Plotter;
import jsprit.analysis.toolbox.SolutionPrinter;
import jsprit.analysis.toolbox.SolutionPrinter.Print;
import jsprit.core.algorithm.VehicleRoutingAlgorithm;
import jsprit.core.algorithm.io.VehicleRoutingAlgorithms;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import jsprit.core.util.Solutions;
import jsprit.instance.reader.VrphGoldenReader;
import jsprit.instance.reader.VrphGoldenReader.VrphType;
import jsprit.util.Examples;

import java.util.Collection;

/**
 * Shows how to benchmark the algorithm on different classical HVRP and FSM instances.
 * 
 * <p>These instances are from Golden and Taillard and copied from 
 * <a href=http://mistic.heig-vd.ch/taillard/problemes.dir/vrp.dir/vrp.html>.
 * 
 * <p>You can find best results of different problems, instances and authors here:
 * <br><a href="http://link.springer.com/article/10.1007%2Fs10732-011-9186-y">http://link.springer.com/article/10.1007%2Fs10732-011-9186-y</a>
 * <br><a href="http://www2.ic.uff.br/~satoru/conteudo/artigos/PAPER%20PUCA-JHeuristics-2011.pdf">http://www2.ic.uff.br/~satoru/conteudo/artigos/PAPER%20PUCA-JHeuristics-2011.pdf</a>
 * <br><a href="http://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.100.2331&rep=rep1&type=pdf">http://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.100.2331&rep=rep1&type=pdf</a>
 * 
 * @author schroeder
 *
 */
public class HVRPBenchmarkExample {
	
	public static void main(String[] args) {
		Examples.createOutputFolder();
		
		VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
		//read modified Golden-instance, you can find all relevant instances in jsprit-instances/instances/vrph
		//you can build various problems, see VrphType doc for more details
		new VrphGoldenReader(vrpBuilder, VrphType.HVRPFD).read("input/cn_14mix.txt");
//		vrpBuilder.addPenaltyVehicles(10.0);
		VehicleRoutingProblem vrp = vrpBuilder.build();
		
		//try also input//jsprit-examples/input/algorithmConfig_considerFixedCosts_routeLevel.xml
		//results might even be a bit better, but it is slower, since it checks insertion on routeLevel
		//rather than on local level
		VehicleRoutingAlgorithm vra = VehicleRoutingAlgorithms.readAndCreateAlgorithm(vrp, "input/algorithmConfig_considerFixedCosts.xml");
		vra.setNuOfIterations(10000);
//		vra.setPrematureAlgorithmTermination(new IterationWithoutImprovementTermination(500));
		vra.addListener(new AlgorithmSearchProgressChartListener("output/progress.png"));
		Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();
		
		VehicleRoutingProblemSolution best = Solutions.bestOf(solutions);
		
		SolutionPrinter.print(vrp, best, Print.VERBOSE);
		
		
		Plotter plotter = new Plotter(vrp,best);
		plotter.plot("output/cn14.png", "cn14");
		
		new GraphStreamViewer(vrp, best).setRenderDelay(100).display();
	}

}
