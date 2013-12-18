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
package jsprit.core.algorithm.recreate;

import static org.junit.Assert.assertEquals;

import java.util.Collection;

import jsprit.core.algorithm.VehicleRoutingAlgorithm;
import jsprit.core.algorithm.io.VehicleRoutingAlgorithms;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.VehicleRoutingProblem.FleetSize;
import jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import jsprit.core.problem.driver.Driver;
import jsprit.core.problem.job.Service;
import jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.vehicle.Vehicle;
import jsprit.core.problem.vehicle.VehicleImpl;
import jsprit.core.problem.vehicle.VehicleTypeImpl;
import jsprit.core.util.Coordinate;
import jsprit.core.util.CrowFlyCosts;
import jsprit.core.util.Solutions;


public class CalcWithTimeSchedulingTest {
	

	public void timeScheduler(){
		VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
		Vehicle vehicle = VehicleImpl.Builder.newInstance("myVehicle").setEarliestStart(0.0).setLatestArrival(100.0).
				setLocationCoord(Coordinate.newInstance(0, 0)).setLocationId("0,0")
				.setType(VehicleTypeImpl.Builder.newInstance("myType", 20).setCostPerDistance(1.0).build()).build();
		vrpBuilder.addVehicle(vehicle);
		vrpBuilder.addJob(Service.Builder.newInstance("myService", 2).setLocationId("0,20").setCoord(Coordinate.newInstance(0, 20)).build());
		vrpBuilder.setFleetSize(FleetSize.INFINITE);
		vrpBuilder.setRoutingCost(getTpCosts(new CrowFlyCosts(vrpBuilder.getLocations())));
		VehicleRoutingProblem vrp = vrpBuilder.build();
		
		VehicleRoutingAlgorithm vra = VehicleRoutingAlgorithms.readAndCreateAlgorithm(vrp, "src/test/resources/testConfig.xml");
		Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();
		
		VehicleRoutingProblemSolution sol = Solutions.bestOf(solutions);
		assertEquals(40.0,sol.getCost(),0.01);
		assertEquals(1, sol.getRoutes().size());
		VehicleRoute route = sol.getRoutes().iterator().next();
		assertEquals(50.0,route.getStart().getEndTime(),0.01);
	}

	private VehicleRoutingTransportCosts getTpCosts(final VehicleRoutingTransportCosts baseCosts) {
		return new VehicleRoutingTransportCosts() {
			
			@Override
			public double getBackwardTransportCost(String fromId, String toId,double arrivalTime, Driver driver, Vehicle vehicle) {
				return getTransportCost(fromId, toId, arrivalTime, driver, vehicle);
			}
			
			@Override
			public double getTransportCost(String fromId, String toId, double departureTime, Driver driver, Vehicle vehicle) {
				if(departureTime < 50){
					return baseCosts.getTransportCost(fromId, toId, departureTime, driver, vehicle)*2.0;
				}
				return baseCosts.getTransportCost(fromId, toId, departureTime, driver, vehicle);
			}
			
			@Override
			public double getBackwardTransportTime(String fromId, String toId,double arrivalTime, Driver driver, Vehicle vehicle) {
				return getTransportTime(fromId, toId, arrivalTime, driver, vehicle);
			}
			
			@Override
			public double getTransportTime(String fromId, String toId,double departureTime, Driver driver, Vehicle vehicle) {
				return getTransportCost(fromId, toId, departureTime, driver, vehicle);
			}
		};
	}

}
