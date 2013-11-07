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
package algorithms;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import util.Coordinate;
import util.ManhattanDistanceCalculator;
import util.RouteUtils;
import basics.Job;
import basics.Service;
import basics.VehicleRoutingProblem;
import basics.VehicleRoutingProblemSolution;
import basics.costs.VehicleRoutingActivityCosts;
import basics.costs.VehicleRoutingTransportCosts;
import basics.route.Driver;
import basics.route.DriverImpl;
import basics.route.FiniteFleetManagerFactory;
import basics.route.ServiceActivity;
import basics.route.TimeWindow;
import basics.route.TourActivities;
import basics.route.Vehicle;
import basics.route.VehicleFleetManager;
import basics.route.VehicleImpl;
import basics.route.VehicleRoute;
import basics.route.VehicleTypeImpl;

public class GendreauPostOptTest {
	
	TourActivities tour;

	Vehicle heavyVehicle;

	Vehicle lightVehicle1;
	
	Vehicle lightVehicle2;
	
	VehicleRoutingTransportCosts cost;
	
	VehicleRoutingActivityCosts activityCosts;
	
	VehicleRoutingProblem vrp;
	
	Service job1;
	
	Service job2;
	
	Service job3;

	private StateManager states;

	private List<Vehicle> vehicles;

	private VehicleFleetManager fleetManager;
	
	private JobInsertionCostsCalculator insertionCalc;

	@Before
	public void setUp(){
		
		cost = new VehicleRoutingTransportCosts() {
			
			@Override
			public double getBackwardTransportTime(String fromId, String toId,
					double arrivalTime, Driver driver, Vehicle vehicle) {
				// TODO Auto-generated method stub
				return 0;
			}
			
			@Override
			public double getBackwardTransportCost(String fromId, String toId,
					double arrivalTime, Driver driver, Vehicle vehicle) {
				// TODO Auto-generated method stub
				return 0;
			}
			
			@Override
			public double getTransportCost(String fromId, String toId, double departureTime, Driver driver, Vehicle vehicle) {
				
				String[] fromTokens = fromId.split(",");
				String[] toTokens = toId.split(",");
				double fromX = Double.parseDouble(fromTokens[0]);
				double fromY = Double.parseDouble(fromTokens[1]);
				
				double toX = Double.parseDouble(toTokens[0]);
				double toY = Double.parseDouble(toTokens[1]);
				
				double costPerDistanceUnit;
				if(vehicle != null){
					costPerDistanceUnit = vehicle.getType().getVehicleCostParams().perDistanceUnit;
				}
				else{
					costPerDistanceUnit = 1;
				}
				
				return costPerDistanceUnit*ManhattanDistanceCalculator.calculateDistance(new Coordinate(fromX, fromY), new Coordinate(toX, toY));
			}
			
			@Override
			public double getTransportTime(String fromId, String toId, double departureTime, Driver driver, Vehicle vehicle) {		
				return 0;
			}
		};
		
		VehicleTypeImpl lightType = VehicleTypeImpl.Builder.newInstance("light", 10).setFixedCost(10).setCostPerDistance(1.0).build();
		VehicleTypeImpl heavyType = VehicleTypeImpl.Builder.newInstance("heavy", 10).setFixedCost(30).setCostPerDistance(2.0).build();
		
		lightVehicle1 = VehicleImpl.Builder.newInstance("light").setLocationId("0,0").setType(lightType).build(); 
		lightVehicle2 = VehicleImpl.Builder.newInstance("light2").setLocationId("0,0").setType(lightType).build(); 
		heavyVehicle = VehicleImpl.Builder.newInstance("heavy").setLocationId("0,0").setType(heavyType).build(); 
			
		
		job1 = getService("10,0");
		job2 = getService("10,10");
		job3 = getService("0,10");
		
		Collection<Job> jobs = new ArrayList<Job>();
		jobs.add(job1);
		jobs.add(job2);
		jobs.add(job3);
		
		vehicles = Arrays.asList(lightVehicle1,lightVehicle2, heavyVehicle);
		
//		Collection<Vehicle> vehicles = Arrays.asList(lightVehicle1,lightVehicle2, heavyVehicle);
		fleetManager = new FiniteFleetManagerFactory(vehicles).createFleetManager();
		states = new StateManager();
		
		activityCosts = new ExampleActivityCostFunction();
		

		ServiceInsertionCalculator standardServiceInsertion = new ServiceInsertionCalculator(cost, new LocalActivityInsertionCostsCalculator(cost, activityCosts), new HardLoadConstraint(states), new HardTimeWindowActivityLevelConstraint(states, cost));


		
		JobInsertionConsideringFixCostsCalculator withFixCost = new JobInsertionConsideringFixCostsCalculator(standardServiceInsertion, states);
		withFixCost.setWeightOfFixCost(1.2);
		
		insertionCalc = new VehicleTypeDependentJobInsertionCalculator(fleetManager, withFixCost);
		
//		updater = new TourStateUpdater(states, cost, activityCosts);
		
	}
	
	@Test
	public void whenPostOpt_splitsTour_oneActiveTourBecomeTwoSeperateActiveTours(){
		Collection<Job> jobs = new ArrayList<Job>();
		jobs.add(job1);
		jobs.add(job2);
		
		vrp = VehicleRoutingProblem.Builder.newInstance().addAllJobs(jobs).addAllVehicles(vehicles).setRoutingCost(cost).build();
				
		TourActivities tour = new TourActivities();
		tour.addActivity(ServiceActivity.newInstance(job1));
		tour.addActivity(ServiceActivity.newInstance(job2));
		
		VehicleRoute route = VehicleRoute.newInstance(tour,DriverImpl.noDriver(),heavyVehicle);
		
		fleetManager.lock(heavyVehicle);
		
		UpdateStates stateUpdater = new UpdateStates(states, vrp.getTransportCosts(), vrp.getActivityCosts());
		stateUpdater.update(route);
		
		Collection<VehicleRoute> routes = new ArrayList<VehicleRoute>();
		routes.add(route);
//		routes.add(new VehicleRoute(getEmptyTour(),getDriver(),getNoVehicle()));
//		routes.add(new VehicleRoute(getEmptyTour(),getDriver(),getNoVehicle()));


		VehicleRoutingProblemSolution sol = new VehicleRoutingProblemSolution(routes, states.getRouteState(route, StateFactory.COSTS).toDouble() + getFixedCosts(routes));

		
		assertEquals(110.0, sol.getCost(), 0.5);
		
		
		RuinRadial radialRuin = new RuinRadial(vrp, 0.2, new JobDistanceAvgCosts(vrp.getTransportCosts()));
//		radialRuin.addListener(stateUpdater);
		
		InsertionStrategy insertionStrategy = new BestInsertion(insertionCalc);
		insertionStrategy.addListener(stateUpdater);
		insertionStrategy.addListener(new VehicleSwitched(fleetManager));
		Gendreau postOpt = new Gendreau(vrp, radialRuin, insertionStrategy);
		postOpt.setFleetManager(fleetManager);
		
		VehicleRoutingProblemSolution newSolution = postOpt.runAndGetSolution(sol);
		newSolution.setCost(getCosts(newSolution,states));
		
		assertEquals(2,RouteUtils.getNuOfActiveRoutes(newSolution.getRoutes()));
		assertEquals(2,newSolution.getRoutes().size());
		assertEquals(80.0,newSolution.getCost(),0.5);
	}
	
	private double getFixedCosts(Collection<VehicleRoute> routes) {
		double c = 0.0;
		for(VehicleRoute r : routes){ c += r.getVehicle().getType().getVehicleCostParams().fix; }
		return c;
	}

	private double getCosts(VehicleRoutingProblemSolution newSolution, StateManager states) {
		double c = 0.0;
		for(VehicleRoute r : newSolution.getRoutes()){

			c += states.getRouteState(r, StateFactory.COSTS).toDouble() + r.getVehicle().getType().getVehicleCostParams().fix;

		}
		return c;
	}

	@Test
	public void whenPostOpt_optsRoutesWithMoreThanTwoJobs_oneRouteBecomesTwoRoutes(){
		Collection<Job> jobs = new ArrayList<Job>();
		jobs.add(job1);
		jobs.add(job2);
		jobs.add(job3);
		
		vrp = VehicleRoutingProblem.Builder.newInstance().addAllJobs(jobs).addAllVehicles(vehicles).setRoutingCost(cost).build();
		
		TourActivities tour = new TourActivities();
		tour.addActivity(ServiceActivity.newInstance(job1));
		tour.addActivity(ServiceActivity.newInstance(job2));
		tour.addActivity(ServiceActivity.newInstance(job3));
		
		VehicleRoute route = VehicleRoute.newInstance(tour,DriverImpl.noDriver(),heavyVehicle);
		
		UpdateStates stateUpdater = new UpdateStates(states, vrp.getTransportCosts(), vrp.getActivityCosts());
		stateUpdater.update(route);
		
		fleetManager.lock(heavyVehicle);
		
		Collection<VehicleRoute> routes = new ArrayList<VehicleRoute>();
		routes.add(route);

		VehicleRoutingProblemSolution sol = new VehicleRoutingProblemSolution(routes, route.getCost());
		sol.setCost(getCosts(sol,states));
		
		assertEquals(110.0, sol.getCost(), 0.5);
		
		RuinRadial radialRuin = new RuinRadial(vrp, 0.2, new JobDistanceAvgCosts(vrp.getTransportCosts()));
		InsertionStrategy insertionStrategy = new BestInsertion(insertionCalc);
		insertionStrategy.addListener(stateUpdater);
		insertionStrategy.addListener(new VehicleSwitched(fleetManager));
		Gendreau postOpt = new Gendreau(vrp, radialRuin, insertionStrategy);
		postOpt.setShareOfJobsToRuin(1.0);
		postOpt.setNuOfIterations(1);
		postOpt.setFleetManager(fleetManager);
//		postOpt.setWithFix(withFixCost);
		VehicleRoutingProblemSolution newSolution = postOpt.runAndGetSolution(sol);
		newSolution.setCost(getCosts(newSolution,states));
		
		assertEquals(2,RouteUtils.getNuOfActiveRoutes(newSolution.getRoutes()));
		assertEquals(2,newSolution.getRoutes().size());
		assertEquals(80.0,newSolution.getCost(),0.5);
	}

	private Service getService(String to, double serviceTime) {
		Service s = Service.Builder.newInstance(to, 0).setLocationId(to).setServiceTime(serviceTime).setTimeWindow(TimeWindow.newInstance(0.0, 20.0)).build(); 
			
		return s;
	}
	
	private Service getService(String to) {
		Service s = getService(to, 0.0);
		return s;
	}
	
	
		

}
