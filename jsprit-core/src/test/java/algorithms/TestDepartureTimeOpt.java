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
package algorithms;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import java.util.Collection;

import org.junit.Test;

import util.Coordinate;
import util.Solutions;
import basics.Service;
import basics.VehicleRoutingAlgorithm;
import basics.VehicleRoutingProblem;
import basics.VehicleRoutingProblem.Builder;
import basics.VehicleRoutingProblemSolution;
import basics.costs.VehicleRoutingActivityCosts;
import basics.route.Driver;
import basics.route.TimeWindow;
import basics.route.TourActivity;
import basics.route.Vehicle;
import basics.route.VehicleImpl;
import basics.route.VehicleImpl.VehicleType;

public class TestDepartureTimeOpt {
	
	@Test
	public void whenSettingOneCustWithTWAnd_NO_DepTimeChoice_totalCostsShouldBe50(){
		TimeWindow timeWindow = TimeWindow.newInstance(40, 45);
		Service service = Service.Builder.newInstance("s", 0).setLocationId("servLoc").setCoord(Coordinate.newInstance(0, 10)).setTimeWindow(timeWindow).build();
		VehicleType type = mock(VehicleType.class);
		Vehicle vehicle = VehicleImpl.VehicleBuilder.newInstance("v").setLocationId("vehLoc").setLocationCoord(Coordinate.newInstance(0, 0))
				.setType(VehicleType.Builder.newInstance("vType", 0).build()).build();
		
		Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
		vrpBuilder.setActivityCosts(new VehicleRoutingActivityCosts(){

			@Override
			public double getActivityCost(TourActivity tourAct, double arrivalTime, Driver driver, Vehicle vehicle) {
				double waiting = Math.max(0, tourAct.getTheoreticalEarliestOperationStartTime() - arrivalTime)*1;
				double late = Math.max(0, arrivalTime - tourAct.getTheoreticalLatestOperationStartTime())*100;
				return  waiting + late;
			}
			
		});
		VehicleRoutingProblem vrp = vrpBuilder.addService(service).addVehicle(vehicle).build();
		
		VehicleRoutingAlgorithm vra = VehicleRoutingAlgorithms.readAndCreateAlgorithm(vrp, "src/test/resources/algorithmConfig.xml");
		Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();
		
		assertEquals(20.0+30.0,Solutions.getBest(solutions).getCost(),0.1);
		
	}
	
	@Test
	public void whenSettingOneCustWithTWAnd_NO_DepTimeChoice_depTimeShouldBe0(){
		TimeWindow timeWindow = TimeWindow.newInstance(40, 45);
		Service service = Service.Builder.newInstance("s", 0).setLocationId("servLoc").setCoord(Coordinate.newInstance(0, 10)).setTimeWindow(timeWindow).build();
		VehicleType type = mock(VehicleType.class);
		Vehicle vehicle = VehicleImpl.VehicleBuilder.newInstance("v").setLocationId("vehLoc").setLocationCoord(Coordinate.newInstance(0, 0))
				.setType(VehicleType.Builder.newInstance("vType", 0).build()).build();
		
		Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
		vrpBuilder.setActivityCosts(new VehicleRoutingActivityCosts(){

			@Override
			public double getActivityCost(TourActivity tourAct, double arrivalTime, Driver driver, Vehicle vehicle) {
				double waiting = Math.max(0, tourAct.getTheoreticalEarliestOperationStartTime() - arrivalTime)*1;
				double late = Math.max(0, arrivalTime - tourAct.getTheoreticalLatestOperationStartTime())*100;
				return  waiting + late;
			}
			
		});
		VehicleRoutingProblem vrp = vrpBuilder.addService(service).addVehicle(vehicle).build();
		
		VehicleRoutingAlgorithm vra = VehicleRoutingAlgorithms.readAndCreateAlgorithm(vrp, "src/test/resources/algorithmConfig.xml");
		Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();
		
		assertEquals(0.0,Solutions.getBest(solutions).getRoutes().iterator().next().getStart().getEndTime(),0.1);
		
	}
	
	@Test
	public void whenSettingOneCustWithTWAndDepTimeChoice_totalCostsShouldBe50(){
		TimeWindow timeWindow = TimeWindow.newInstance(40, 45);
		Service service = Service.Builder.newInstance("s", 0).setLocationId("servLoc").setCoord(Coordinate.newInstance(0, 10)).setTimeWindow(timeWindow).build();
		Vehicle vehicle = VehicleImpl.VehicleBuilder.newInstance("v").setLocationId("vehLoc").setLocationCoord(Coordinate.newInstance(0, 0))
				.setType(VehicleType.Builder.newInstance("vType", 0).build()).build();
		
		Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
		vrpBuilder.setActivityCosts(new VehicleRoutingActivityCosts(){

			@Override
			public double getActivityCost(TourActivity tourAct, double arrivalTime, Driver driver, Vehicle vehicle) {
				double waiting = Math.max(0, tourAct.getTheoreticalEarliestOperationStartTime() - arrivalTime)*1;
				double late = Math.max(0, arrivalTime - tourAct.getTheoreticalLatestOperationStartTime())*100;
				return  waiting + late;
			}
			
		});
		VehicleRoutingProblem vrp = vrpBuilder.addService(service).addVehicle(vehicle).build();
		
		
		VehicleRoutingAlgorithm vra = VehicleRoutingAlgorithms.readAndCreateAlgorithm(vrp, "src/test/resources/algorithmConfigWithDepartureTimeChoice.xml");
		Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();
		
		assertEquals(20.0,Solutions.getBest(solutions).getCost(),0.1);
		
	}
	
	@Test
	public void whenSettingOneCustWithTWAndDepTimeChoice_depTimeShouldBe0(){
		TimeWindow timeWindow = TimeWindow.newInstance(40, 45);
		Service service = Service.Builder.newInstance("s", 0).setLocationId("servLoc").setCoord(Coordinate.newInstance(0, 10)).setTimeWindow(timeWindow).build();
		Vehicle vehicle = VehicleImpl.VehicleBuilder.newInstance("v").setLocationId("vehLoc").setLocationCoord(Coordinate.newInstance(0, 0))
				.setType(VehicleType.Builder.newInstance("vType", 0).build()).build();
		
		Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
		vrpBuilder.setActivityCosts(new VehicleRoutingActivityCosts(){

			@Override
			public double getActivityCost(TourActivity tourAct, double arrivalTime, Driver driver, Vehicle vehicle) {
				double waiting = Math.max(0, tourAct.getTheoreticalEarliestOperationStartTime() - arrivalTime)*1;
				double late = Math.max(0, arrivalTime - tourAct.getTheoreticalLatestOperationStartTime())*100;
				return  waiting + late;
			}
			
		});
		VehicleRoutingProblem vrp = vrpBuilder.addService(service).addVehicle(vehicle).build();
		
		
		VehicleRoutingAlgorithm vra = VehicleRoutingAlgorithms.readAndCreateAlgorithm(vrp, "src/test/resources/algorithmConfigWithDepartureTimeChoice.xml");
		Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();
		
		assertEquals(30.0,Solutions.getBest(solutions).getRoutes().iterator().next().getStart().getEndTime(),0.1);
		
	}
	
	@Test
	public void whenSettingTwoCustWithTWAndDepTimeChoice_totalCostsShouldBe50(){
		TimeWindow timeWindow = TimeWindow.newInstance(40, 45);
		Service service = Service.Builder.newInstance("s", 0).setLocationId("servLoc").setCoord(Coordinate.newInstance(0, 10)).setTimeWindow(timeWindow).build();
		
		Service service2 = Service.Builder.newInstance("s2", 0).setLocationId("servLoc2").setCoord(Coordinate.newInstance(0, 20)).
				setTimeWindow(TimeWindow.newInstance(30, 40)).build();
		
		Vehicle vehicle = VehicleImpl.VehicleBuilder.newInstance("v").setLocationId("vehLoc").setLocationCoord(Coordinate.newInstance(0, 0))
				.setType(VehicleType.Builder.newInstance("vType", 0).build()).build();
		
		Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
		vrpBuilder.setActivityCosts(new VehicleRoutingActivityCosts(){

			@Override
			public double getActivityCost(TourActivity tourAct, double arrivalTime, Driver driver, Vehicle vehicle) {
				double waiting = Math.max(0, tourAct.getTheoreticalEarliestOperationStartTime() - arrivalTime)*1;
				double late = Math.max(0, arrivalTime - tourAct.getTheoreticalLatestOperationStartTime())*100;
				return  waiting + late;
			}
			
		});
		VehicleRoutingProblem vrp = vrpBuilder.addService(service).addService(service2).addVehicle(vehicle).build();
		
		
		VehicleRoutingAlgorithm vra = VehicleRoutingAlgorithms.readAndCreateAlgorithm(vrp, "src/test/resources/algorithmConfigWithDepartureTimeChoice.xml");
		Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();
		
		assertEquals(40.0,Solutions.getBest(solutions).getCost(),0.1);
		
	}
	
	@Test
	public void whenSettingTwoCustWithTWAndDepTimeChoice_depTimeShouldBe10(){
		TimeWindow timeWindow = TimeWindow.newInstance(40, 45);
		Service service = Service.Builder.newInstance("s", 0).setLocationId("servLoc").setCoord(Coordinate.newInstance(0, 10)).setTimeWindow(timeWindow).build();
		
		Service service2 = Service.Builder.newInstance("s2", 0).setLocationId("servLoc2").setCoord(Coordinate.newInstance(0, 20)).
				setTimeWindow(TimeWindow.newInstance(30, 40)).build();
		
		Vehicle vehicle = VehicleImpl.VehicleBuilder.newInstance("v").setLocationId("vehLoc").setLocationCoord(Coordinate.newInstance(0, 0))
				.setType(VehicleType.Builder.newInstance("vType", 0).build()).build();
		
		Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
		vrpBuilder.setActivityCosts(new VehicleRoutingActivityCosts(){

			@Override
			public double getActivityCost(TourActivity tourAct, double arrivalTime, Driver driver, Vehicle vehicle) {
				double waiting = Math.max(0, tourAct.getTheoreticalEarliestOperationStartTime() - arrivalTime)*1;
				double late = Math.max(0, arrivalTime - tourAct.getTheoreticalLatestOperationStartTime())*100;
				return  waiting + late;
			}
			
		});
		VehicleRoutingProblem vrp = vrpBuilder.addService(service).addService(service2).addVehicle(vehicle).build();
		
		
		VehicleRoutingAlgorithm vra = VehicleRoutingAlgorithms.readAndCreateAlgorithm(vrp, "src/test/resources/algorithmConfigWithDepartureTimeChoice.xml");
		Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();
		
		assertEquals(10.0,Solutions.getBest(solutions).getRoutes().iterator().next().getStart().getEndTime(),0.1);
		
	}	

}
