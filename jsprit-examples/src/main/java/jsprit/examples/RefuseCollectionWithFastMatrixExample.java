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

import jsprit.core.algorithm.VehicleRoutingAlgorithm;
import jsprit.core.algorithm.box.GreedySchrimpfFactory;
import jsprit.core.algorithm.termination.IterationWithoutImprovementTermination;
import jsprit.core.problem.Location;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.VehicleRoutingProblem.FleetSize;
import jsprit.core.problem.io.VrpXMLWriter;
import jsprit.core.problem.job.Service;
import jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import jsprit.core.problem.vehicle.VehicleImpl;
import jsprit.core.problem.vehicle.VehicleTypeImpl;
import jsprit.core.reporting.SolutionPrinter;
import jsprit.core.util.FastVehicleRoutingTransportCostsMatrix;
import jsprit.core.util.Solutions;
import jsprit.util.Examples;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;


/**
 * This example is base on
 * http://web.mit.edu/urban_or_book/www/book/chapter6/6.4.12.html
 * 
 * @author stefan schroeder
 *
 */
public class RefuseCollectionWithFastMatrixExample {

	public static void main(String[] args) throws IOException {
		/*
		 * some preparation - create output folder
		 */
		Examples.createOutputFolder();

		/*
		 * create vehicle-type and vehicle
		 */
		VehicleTypeImpl.Builder typeBuilder = VehicleTypeImpl.Builder.newInstance("vehicle-type").addCapacityDimension(0, 23);
		typeBuilder.setCostPerDistance(1.0);
		VehicleTypeImpl bigType = typeBuilder.build();

		VehicleImpl.Builder vehicleBuilder = VehicleImpl.Builder.newInstance("vehicle");
		vehicleBuilder.setStartLocation(Location.Builder.newInstance().setIndex(1).build());
		vehicleBuilder.setType(bigType);
		VehicleImpl bigVehicle = vehicleBuilder.build();

		/*
		 * start building the problem
		 */
		VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
		vrpBuilder.setFleetSize(FleetSize.INFINITE);
		vrpBuilder.addVehicle(bigVehicle);

		/*
		 * read demand quantities
		 */
		readDemandQuantities(vrpBuilder);

		/*
		 * create cost-matrix
		 */
		FastVehicleRoutingTransportCostsMatrix.Builder matrixBuilder = FastVehicleRoutingTransportCostsMatrix.Builder.newInstance(11,true);
		readDistances(matrixBuilder);

		vrpBuilder.setRoutingCost(matrixBuilder.build());
		
		VehicleRoutingProblem vrp = vrpBuilder.build();
		
		VehicleRoutingAlgorithm vra = new GreedySchrimpfFactory().createAlgorithm(vrp);
		vra.setPrematureAlgorithmTermination(new IterationWithoutImprovementTermination(100));
		Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();
		
		SolutionPrinter.print(Solutions.bestOf(solutions));
		
		new VrpXMLWriter(vrp, solutions).write("output/refuseCollectionExampleSolution.xml");
		
	}


	private static void readDemandQuantities(VehicleRoutingProblem.Builder vrpBuilder) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(new File("input/RefuseCollectionExample_Quantities")));
		String line;
		boolean firstLine = true;
		while((line = reader.readLine()) != null){
			if(firstLine) {
				firstLine = false;
				continue;
			}
			String[] lineTokens = line.split(",");
			/*
			 * build service
			 */
			Service service = Service.Builder.newInstance(lineTokens[0])
                    .addSizeDimension(0, Integer.parseInt(lineTokens[1]))
                    .setLocation(Location.Builder.newInstance().setIndex(Integer.parseInt(lineTokens[0])).build())
                    .build();
			/*
			 * and add it to problem
			 */
			vrpBuilder.addJob(service);
		}
		reader.close();
	}
	

	private static void readDistances(FastVehicleRoutingTransportCostsMatrix.Builder matrixBuilder) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(new File("input/RefuseCollectionExample_Distances")));
		String line;
		boolean firstLine = true;
		while((line = reader.readLine()) != null){
			if(firstLine) {
				firstLine = false;
				continue;
			}
			String[] lineTokens = line.split(",");
			matrixBuilder.addTransportDistance(Integer.parseInt(lineTokens[0]),Integer.parseInt(lineTokens[1]), Integer.parseInt(lineTokens[2]));
		}
		reader.close();
		
	}

}
