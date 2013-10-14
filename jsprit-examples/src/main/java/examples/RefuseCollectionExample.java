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
package examples;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import util.Solutions;
import util.VehicleRoutingTransportCostsMatrix;
import util.VehicleRoutingTransportCostsMatrix.Builder;
import algorithms.GreedySchrimpfFactory;
import analysis.SolutionPrinter;
import analysis.SolutionPrinter.Print;
import basics.Service;
import basics.VehicleRoutingAlgorithm;
import basics.VehicleRoutingProblem;
import basics.VehicleRoutingProblem.FleetSize;
import basics.VehicleRoutingProblemSolution;
import basics.costs.VehicleRoutingTransportCosts;
import basics.io.VrpXMLWriter;
import basics.route.Driver;
import basics.route.Vehicle;
import basics.route.VehicleImpl;
import basics.route.VehicleTypeImpl;


/**
 * This example is base on
 * http://web.mit.edu/urban_or_book/www/book/chapter6/6.4.12.html
 * 
 * @author stefan schroeder
 *
 */
public class RefuseCollectionExample {

	static class RelationKey {
		
		static RelationKey newKey(String from, String to){
			int fromInt = Integer.parseInt(from);
			int toInt = Integer.parseInt(to);
			if(fromInt < toInt){
				return new RelationKey(from, to);
			}
			else {
				return new RelationKey(to, from);
			}
		}
		
		final String from;
		final String to;
		
		public RelationKey(String from, String to) {
			super();
			this.from = from;
			this.to = to;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((from == null) ? 0 : from.hashCode());
			result = prime * result + ((to == null) ? 0 : to.hashCode());
			return result;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			RelationKey other = (RelationKey) obj;
			if (from == null) {
				if (other.from != null)
					return false;
			} else if (!from.equals(other.from))
				return false;
			if (to == null) {
				if (other.to != null)
					return false;
			} else if (!to.equals(other.to))
				return false;
			return true;
		}
	}
	
	static class RoutingCosts implements VehicleRoutingTransportCosts {

		private Map<RelationKey,Integer> distances;
		
		public RoutingCosts(Map<RelationKey, Integer> distances) {
			super();
			this.distances = distances;
		}

		@Override
		public double getTransportTime(String fromId, String toId, double departureTime, Driver driver, Vehicle vehicle) {
			return getTransportCost(fromId, toId, departureTime, driver, vehicle);
		}

		@Override
		public double getBackwardTransportTime(String fromId, String toId, double arrivalTime, Driver driver, Vehicle vehicle) {
			return getTransportCost(fromId, toId, arrivalTime, driver, vehicle);
		}

		@Override
		public double getTransportCost(String fromId, String toId,double departureTime, Driver driver, Vehicle vehicle) {
			if(fromId.equals(toId)) return 0.0;
			RelationKey key = RelationKey.newKey(fromId, toId);
			return distances.get(key);
		}

		@Override
		public double getBackwardTransportCost(String fromId, String toId,double arrivalTime, Driver driver, Vehicle vehicle) {
			return getTransportCost(fromId, toId, arrivalTime, driver, vehicle);
		}
		
	}
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
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
		 * create vehicle-type and vehicle
		 */
		VehicleTypeImpl.Builder typeBuilder = VehicleTypeImpl.Builder.newInstance("vehicle-type", 23);
		typeBuilder.setCostPerDistance(1.0);
		VehicleTypeImpl bigType = typeBuilder.build();
		
		VehicleImpl.Builder vehicleBuilder = VehicleImpl.Builder.newInstance("vehicle");
		vehicleBuilder.setLocationId("1");
		vehicleBuilder.setType(bigType);
		Vehicle bigVehicle = vehicleBuilder.build();
		
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
		VehicleRoutingTransportCostsMatrix.Builder matrixBuilder = VehicleRoutingTransportCostsMatrix.Builder.newInstance(true);
		readDistances(matrixBuilder);

		vrpBuilder.setRoutingCost(matrixBuilder.build());
		
		VehicleRoutingProblem vrp = vrpBuilder.build();
		
		VehicleRoutingAlgorithm vra = new GreedySchrimpfFactory().createAlgorithm(vrp);
		vra.setPrematureBreak(100);
		Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();
		
		SolutionPrinter.print(Solutions.getBest(solutions),Print.VERBOSE);
		
		new VrpXMLWriter(vrp, solutions).write("output/refuseCollectionExampleSolution.xml");
		
	}


	private static void readDemandQuantities(VehicleRoutingProblem.Builder vrpBuilder) throws FileNotFoundException, IOException {
		BufferedReader reader = new BufferedReader(new FileReader(new File("input/RefuseCollectionExample_Quantities")));
		String line = null;
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
			Service service = Service.Builder.newInstance(lineTokens[0], Integer.parseInt(lineTokens[1])).setLocationId(lineTokens[0]).build();
			/*
			 * and add it to problem
			 */
			vrpBuilder.addService(service);
		}
		reader.close();
	}
	

	private static void readDistances(Builder matrixBuilder) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(new File("input/RefuseCollectionExample_Distances")));
		String line = null;
		boolean firstLine = true;
		while((line = reader.readLine()) != null){
			if(firstLine) {
				firstLine = false;
				continue;
			}
			String[] lineTokens = line.split(",");
			matrixBuilder.addTransportDistance(lineTokens[0],lineTokens[1], Integer.parseInt(lineTokens[2]));
		}
		reader.close();
		
	}

}
