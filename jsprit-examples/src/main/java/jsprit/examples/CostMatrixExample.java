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
import jsprit.core.algorithm.VehicleRoutingAlgorithm;
import jsprit.core.algorithm.io.VehicleRoutingAlgorithms;
import jsprit.core.problem.Location;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.VehicleRoutingProblem.FleetSize;
import jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import jsprit.core.problem.job.Service;
import jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import jsprit.core.problem.vehicle.VehicleImpl;
import jsprit.core.problem.vehicle.VehicleType;
import jsprit.core.problem.vehicle.VehicleTypeImpl;
import jsprit.core.reporting.SolutionPrinter;
import jsprit.core.util.Solutions;
import jsprit.core.util.VehicleRoutingTransportCostsMatrix;
import jsprit.util.Examples;

import java.util.Collection;


/**
 * Illustrates how you can use jsprit with an already compiled distance and time matrix.
 * 
 * @author schroeder
 *
 */
public class CostMatrixExample {

	public static void main(String[] args) {
		/*
		 * some preparation - create output folder
		 */
		Examples.createOutputFolder();
		
		VehicleType type = VehicleTypeImpl.Builder.newInstance("type").addCapacityDimension(0, 2).setCostPerDistance(1).setCostPerTime(2).build();
		VehicleImpl vehicle = VehicleImpl.Builder.newInstance("vehicle")
				.setStartLocation(Location.newInstance("0")).setType(type).build();
		
		Service s1 = Service.Builder.newInstance("1").addSizeDimension(0, 1).setLocation(Location.newInstance("1")).build();
		Service s2 = Service.Builder.newInstance("2").addSizeDimension(0, 1).setLocation(Location.newInstance("2")).build();
		Service s3 = Service.Builder.newInstance("3").addSizeDimension(0, 1).setLocation(Location.newInstance("3")).build();
		
		
		/*
		 * Assume the following symmetric distance-matrix
		 * from,to,distance
		 * 0,1,10.0
		 * 0,2,20.0
		 * 0,3,5.0
		 * 1,2,4.0
		 * 1,3,1.0
		 * 2,3,2.0
		 * 
		 * and this time-matrix
		 * 0,1,5.0
		 * 0,2,10.0
		 * 0,3,2.5
		 * 1,2,2.0
		 * 1,3,0.5
		 * 2,3,1.0
		 */
		//define a matrix-builder building a symmetric matrix
		VehicleRoutingTransportCostsMatrix.Builder costMatrixBuilder = VehicleRoutingTransportCostsMatrix.Builder.newInstance(true);
		costMatrixBuilder.addTransportDistance("0", "1", 10.0);
		costMatrixBuilder.addTransportDistance("0", "2", 20.0);
		costMatrixBuilder.addTransportDistance("0", "3", 5.0);
		costMatrixBuilder.addTransportDistance("1", "2", 4.0);
		costMatrixBuilder.addTransportDistance("1", "3", 1.0);
		costMatrixBuilder.addTransportDistance("2", "3", 2.0);
		
		costMatrixBuilder.addTransportTime("0", "1", 10.0);
		costMatrixBuilder.addTransportTime("0", "2", 20.0);
		costMatrixBuilder.addTransportTime("0", "3", 5.0);
		costMatrixBuilder.addTransportTime("1", "2", 4.0);
		costMatrixBuilder.addTransportTime("1", "3", 1.0);
		costMatrixBuilder.addTransportTime("2", "3", 2.0);
		
		VehicleRoutingTransportCosts costMatrix = costMatrixBuilder.build();
		
		VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().setFleetSize(FleetSize.INFINITE).setRoutingCost(costMatrix)
				.addVehicle(vehicle).addJob(s1).addJob(s2).addJob(s3).build();
		
		VehicleRoutingAlgorithm vra = VehicleRoutingAlgorithms.readAndCreateAlgorithm(vrp, "input/fastAlgo.xml");
		
		Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();
		
		SolutionPrinter.print(Solutions.bestOf(solutions));
		
		new Plotter(vrp, Solutions.bestOf(solutions)).plot("output/yo.png", "po");
		
	}

}
