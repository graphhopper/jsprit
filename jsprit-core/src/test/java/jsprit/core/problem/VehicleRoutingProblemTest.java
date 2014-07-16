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
package jsprit.core.problem;

import jsprit.core.problem.VehicleRoutingProblem.FleetSize;
import jsprit.core.problem.constraint.Constraint;
import jsprit.core.problem.cost.AbstractForwardVehicleRoutingTransportCosts;
import jsprit.core.problem.cost.VehicleRoutingActivityCosts;
import jsprit.core.problem.driver.Driver;
import jsprit.core.problem.driver.DriverImpl;
import jsprit.core.problem.job.Delivery;
import jsprit.core.problem.job.Pickup;
import jsprit.core.problem.job.Service;
import jsprit.core.problem.job.Shipment;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.solution.route.activity.TourActivity;
import jsprit.core.problem.vehicle.*;
import jsprit.core.util.Coordinate;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class VehicleRoutingProblemTest {
	
	@Test
	public void whenBuildingWithInfiniteFleet_fleetSizeShouldBeInfinite(){
		VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
		builder.setFleetSize(FleetSize.INFINITE);
		VehicleRoutingProblem vrp = builder.build();
		assertEquals(FleetSize.INFINITE,vrp.getFleetSize());
	}
	
	@Test
	public void whenBuildingWithFiniteFleet_fleetSizeShouldBeFinite(){
		VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
		builder.setFleetSize(FleetSize.FINITE);
		VehicleRoutingProblem vrp = builder.build();
		assertEquals(FleetSize.FINITE,vrp.getFleetSize());
	}

	@Test
	public void whenBuildingWithFourVehicles_vrpShouldContainTheCorrectNuOfVehicles(){
		VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
		
		VehicleImpl v1 = VehicleImpl.Builder.newInstance("v1").setStartLocationId("start").build();
        VehicleImpl v2 = VehicleImpl.Builder.newInstance("v2").setStartLocationId("start").build();
        VehicleImpl v3 = VehicleImpl.Builder.newInstance("v3").setStartLocationId("start").build();
        VehicleImpl v4 = VehicleImpl.Builder.newInstance("v4").setStartLocationId("start").build();
		
		builder.addVehicle(v1).addVehicle(v2).addVehicle(v3).addVehicle(v4);
		
		VehicleRoutingProblem vrp = builder.build();
		assertEquals(4,vrp.getVehicles().size());

	}
	
	@Test
	public void whenAddingFourVehiclesAllAtOnce_vrpShouldContainTheCorrectNuOfVehicles(){
		VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
		
		VehicleImpl v1 = VehicleImpl.Builder.newInstance("v1").setStartLocationId("start").build();
        VehicleImpl v2 = VehicleImpl.Builder.newInstance("v2").setStartLocationId("start").build();
        VehicleImpl v3 = VehicleImpl.Builder.newInstance("v3").setStartLocationId("start").build();
        VehicleImpl v4 = VehicleImpl.Builder.newInstance("v4").setStartLocationId("start").build();
		
		builder.addAllVehicles(Arrays.asList(v1,v2,v3,v4));
		
		VehicleRoutingProblem vrp = builder.build();
		assertEquals(4,vrp.getVehicles().size());

	}

	@Test
	public void whenBuildingWithFourVehiclesAndTwoTypes_vrpShouldContainTheCorrectNuOfTypes(){
		VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
		
		VehicleTypeImpl type1 = mock(VehicleTypeImpl.class);
		VehicleTypeImpl type2 = mock(VehicleTypeImpl.class);
		
		Vehicle v1 = VehicleImpl.Builder.newInstance("v1").setStartLocationId("yo").setType(type1).build();
		Vehicle v2 = VehicleImpl.Builder.newInstance("v2").setStartLocationId("yo").setType(type1).build();
		Vehicle v3 = VehicleImpl.Builder.newInstance("v3").setStartLocationId("yo").setType(type2).build();
		Vehicle v4 = VehicleImpl.Builder.newInstance("v4").setStartLocationId("yo").setType(type2).build();
		
		builder.addVehicle(v1).addVehicle(v2).addVehicle(v3).addVehicle(v4);
		
		VehicleRoutingProblem vrp = builder.build();
		assertEquals(2,vrp.getTypes().size());

	}

	@Test
	public void whenShipmentsAreAdded_vrpShouldContainThem(){
		Shipment s = Shipment.Builder.newInstance("s").addSizeDimension(0, 10).setPickupLocation("foofoo").setDeliveryLocation("foo").build();
		Shipment s2 = Shipment.Builder.newInstance("s2").addSizeDimension(0, 100).setPickupLocation("foofoo").setDeliveryLocation("foo").build();
		VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
		vrpBuilder.addJob(s);
		vrpBuilder.addJob(s2);
		VehicleRoutingProblem vrp = vrpBuilder.build();
		
		assertEquals(2,vrp.getJobs().size());
		assertEquals(s,vrp.getJobs().get("s"));
		assertEquals(s2,vrp.getJobs().get("s2"));
	}
	
	@Test
	public void whenServicesAreAdded_vrpShouldContainThem(){
		Service s1 = mock(Service.class);
		when(s1.getId()).thenReturn("s1");
		Service s2 = mock(Service.class);
		when(s2.getId()).thenReturn("s2");
		
		VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
		vrpBuilder.addJob(s1).addJob(s2);
		
		VehicleRoutingProblem vrp = vrpBuilder.build();
		
		assertEquals(2,vrp.getJobs().size());
		assertEquals(s1,vrp.getJobs().get("s1"));
		assertEquals(s2,vrp.getJobs().get("s2"));
	}
	
	@Test
	public void whenPickupsAreAdded_vrpShouldContainThem(){
		Pickup s1 = mock(Pickup.class);
		when(s1.getId()).thenReturn("s1");
		Pickup s2 = mock(Pickup.class);
		when(s2.getId()).thenReturn("s2");
		
		VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
		vrpBuilder.addJob(s1).addJob(s2);
		
		VehicleRoutingProblem vrp = vrpBuilder.build();
		
		assertEquals(2,vrp.getJobs().size());
		assertEquals(s1,vrp.getJobs().get("s1"));
		assertEquals(s2,vrp.getJobs().get("s2"));
	}
	
	@Test
	public void whenPickupsAreAddedAllAtOnce_vrpShouldContainThem(){
		Pickup s1 = mock(Pickup.class);
		when(s1.getId()).thenReturn("s1");
		Pickup s2 = mock(Pickup.class);
		when(s2.getId()).thenReturn("s2");
		
		VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
		vrpBuilder.addAllJobs(Arrays.asList(s1,s2));
		
		VehicleRoutingProblem vrp = vrpBuilder.build();
		
		assertEquals(2,vrp.getJobs().size());
		assertEquals(s1,vrp.getJobs().get("s1"));
		assertEquals(s2,vrp.getJobs().get("s2"));
	}
	
	@Test
	public void whenDelivieriesAreAdded_vrpShouldContainThem(){
		Delivery s1 = mock(Delivery.class);
		when(s1.getId()).thenReturn("s1");
        when(s1.getSize()).thenReturn(Capacity.Builder.newInstance().build());
		Delivery s2 = mock(Delivery.class);
		when(s2.getId()).thenReturn("s2");
        when(s2.getSize()).thenReturn(Capacity.Builder.newInstance().build());

		VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
		vrpBuilder.addJob(s1).addJob(s2);
		
		VehicleRoutingProblem vrp = vrpBuilder.build();
		
		assertEquals(2,vrp.getJobs().size());
		assertEquals(s1,vrp.getJobs().get("s1"));
		assertEquals(s2,vrp.getJobs().get("s2"));
	}
	
	@Test
	public void whenDelivieriesAreAddedAllAtOnce_vrpShouldContainThem(){
		Delivery s1 = mock(Delivery.class);
		when(s1.getId()).thenReturn("s1");
        when(s1.getSize()).thenReturn(Capacity.Builder.newInstance().build());
		Delivery s2 = mock(Delivery.class);
		when(s2.getId()).thenReturn("s2");
        when(s2.getSize()).thenReturn(Capacity.Builder.newInstance().build());

		VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
		vrpBuilder.addAllJobs(Arrays.asList(s1,s2));
		
		VehicleRoutingProblem vrp = vrpBuilder.build();
		
		assertEquals(2,vrp.getJobs().size());
		assertEquals(s1,vrp.getJobs().get("s1"));
		assertEquals(s2,vrp.getJobs().get("s2"));
	}
	
	@Test
	public void whenServicesAreAddedAllAtOnce_vrpShouldContainThem(){
		Service s1 = mock(Service.class);
		when(s1.getId()).thenReturn("s1");
		Service s2 = mock(Service.class);
		when(s2.getId()).thenReturn("s2");
		
		Collection<Service> services = new ArrayList<Service>();
		services.add(s1);
		services.add(s2);
		
		VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
		vrpBuilder.addAllJobs(services);
		
		VehicleRoutingProblem vrp = vrpBuilder.build();
		
		assertEquals(2,vrp.getJobs().size());
		assertEquals(s1,vrp.getJobs().get("s1"));
		assertEquals(s2,vrp.getJobs().get("s2"));
	}
	
	@SuppressWarnings("deprecation")
    @Test
	public void whenConstraintsAdded_vrpShouldContainThem(){
		Constraint c1 = mock(Constraint.class);
		Constraint c2 = mock(Constraint.class);
		VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
		builder.addConstraint(c1).addConstraint(c2);
		VehicleRoutingProblem problem = builder.build();
		assertEquals(2,problem.getConstraints().size());
	}
	
	@Test
	public void whenSettingActivityCosts_vrpShouldContainIt(){
		VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
		builder.setActivityCosts(new VehicleRoutingActivityCosts(){

			@Override
			public double getActivityCost(TourActivity tourAct,double arrivalTime, Driver driver, Vehicle vehicle) {
				return 4.0;
			}
			
		});
		
		VehicleRoutingProblem problem = builder.build();
		assertEquals(4.0,problem.getActivityCosts().getActivityCost(null, 0.0, null, null),0.01);
	}
	
	@Test
	public void whenSettingRoutingCosts_vprShouldContainIt(){
		VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
		
		builder.setRoutingCost(new AbstractForwardVehicleRoutingTransportCosts() {
			
			@Override
			public double getTransportTime(String fromId, String toId,
					double departureTime, Driver driver, Vehicle vehicle) {
				return 0;
			}
			
			@Override
			public double getTransportCost(String fromId, String toId,
					double departureTime, Driver driver, Vehicle vehicle) {
				return 4.0;
			}
		});
		
		VehicleRoutingProblem problem = builder.build();
		assertEquals(4.0,problem.getTransportCosts().getTransportCost("", "", 0.0, null, null),0.01);
	}
	
	@Test
	public void whenAddingAVehicle_getAddedVehicleTypesShouldReturnItsType(){
		VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
		VehicleType type = VehicleTypeImpl.Builder.newInstance("type").build();
		VehicleImpl vehicle = VehicleImpl.Builder.newInstance("v").setStartLocationId("loc").setType(type).build();
		builder.addVehicle(vehicle);
		
		assertEquals(1,builder.getAddedVehicleTypes().size());
		assertEquals(type,builder.getAddedVehicleTypes().iterator().next());
		
	}
	
	@Test
	public void whenAddingTwoVehicleWithSameType_getAddedVehicleTypesShouldReturnOnlyOneType(){
		VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
		VehicleType type = VehicleTypeImpl.Builder.newInstance("type").build();
		VehicleImpl vehicle = VehicleImpl.Builder.newInstance("v").setStartLocationId("loc").setType(type).build();
		VehicleImpl vehicle2 = VehicleImpl.Builder.newInstance("v").setStartLocationId("loc").setType(type).build();
		
		builder.addVehicle(vehicle);
		builder.addVehicle(vehicle2);
		
		assertEquals(1,builder.getAddedVehicleTypes().size());
		assertEquals(type,builder.getAddedVehicleTypes().iterator().next());
	}

	@Test
	public void whenAddingTwoVehicleWithDiffType_getAddedVehicleTypesShouldReturnTheseType(){
		VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
		VehicleType type = VehicleTypeImpl.Builder.newInstance("type").build();
		VehicleType type2 = VehicleTypeImpl.Builder.newInstance("type2").build();
		
		VehicleImpl vehicle = VehicleImpl.Builder.newInstance("v").setStartLocationId("loc").setType(type).build();
		VehicleImpl vehicle2 = VehicleImpl.Builder.newInstance("v").setStartLocationId("loc").setType(type2).build();
		
		builder.addVehicle(vehicle);
		builder.addVehicle(vehicle2);
		
		assertEquals(2,builder.getAddedVehicleTypes().size());

	}
	
	@Test
	public void whenSettingAddPenaltyVehicleOptions_itShouldAddPenaltyVehicle(){
		VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
		VehicleType type = VehicleTypeImpl.Builder.newInstance("type").build();
		VehicleImpl vehicle = VehicleImpl.Builder.newInstance("v").setStartLocationId("loc").setType(type).build();
		
		builder.addVehicle(vehicle);
		builder.setFleetSize(FleetSize.FINITE);
		builder.addPenaltyVehicles(3.0);
		
		VehicleRoutingProblem vrp = builder.build();
		
		assertEquals(2,vrp.getVehicles().size());
		
		boolean penaltyVehicleInCollection = false;
		for(Vehicle v : vrp.getVehicles()){
			if(v.getType() instanceof PenaltyVehicleType) penaltyVehicleInCollection = true;
		}
		assertTrue(penaltyVehicleInCollection);
		
	}
	
	@Test
	public void whenSettingAddPenaltyVehicleOptionsAndFleetSizeIsInfinite_noPenaltyVehicleIsAdded(){
		VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
		VehicleType type = VehicleTypeImpl.Builder.newInstance("type").build();
		VehicleImpl vehicle = VehicleImpl.Builder.newInstance("v").setStartLocationId("loc").setType(type).build();
		
		builder.addVehicle(vehicle);
		builder.addPenaltyVehicles(3.0);
		
		VehicleRoutingProblem vrp = builder.build();
		
		assertEquals(1,vrp.getVehicles().size());
		
		boolean penaltyVehicleInCollection = false;
		for(Vehicle v : vrp.getVehicles()){
			if(v.getType() instanceof PenaltyVehicleType) penaltyVehicleInCollection = true;
		}
		assertFalse(penaltyVehicleInCollection);
		
	}

	
	
	@Test
	public void whenSettingAddPenaltyVehicleOptionsAndTwoVehiclesWithSameLocationAndType_onlyOnePenaltyVehicleIsAdded(){
		VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
		VehicleType type = VehicleTypeImpl.Builder.newInstance("type").build();
		VehicleImpl vehicle = VehicleImpl.Builder.newInstance("v").setStartLocationId("loc").setType(type).build();
		VehicleImpl vehicle2 = VehicleImpl.Builder.newInstance("v2").setStartLocationId("loc").setType(type).build();
		
		builder.addVehicle(vehicle);
		builder.addVehicle(vehicle2);
		builder.setFleetSize(FleetSize.FINITE);
		builder.addPenaltyVehicles(3.0);
		
		VehicleRoutingProblem vrp = builder.build();
		
		assertEquals(3,vrp.getVehicles().size());
		
		boolean penaltyVehicleInCollection = false;
		for(Vehicle v : vrp.getVehicles()){
			if(v.getType() instanceof PenaltyVehicleType) penaltyVehicleInCollection = true;
		}
		assertTrue(penaltyVehicleInCollection);
		
	}
	
	@Test
	public void whenSettingAddPenaltyVehicleOptionsWithAbsoluteFixedCostsAndTwoVehiclesWithSameLocationAndType_onePenaltyVehicleIsAddedWithTheCorrectPenaltyFixedCosts(){
		VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
		VehicleType type = VehicleTypeImpl.Builder.newInstance("type").build();
		VehicleImpl vehicle = VehicleImpl.Builder.newInstance("v").setStartLocationId("loc").setType(type).build();
		VehicleImpl vehicle2 = VehicleImpl.Builder.newInstance("v2").setStartLocationId("loc").setType(type).build();
		
		builder.addVehicle(vehicle);
		builder.addVehicle(vehicle2);
		builder.setFleetSize(FleetSize.FINITE);
		builder.addPenaltyVehicles(3.0,10000);
		
		VehicleRoutingProblem vrp = builder.build();
		
		assertEquals(3,vrp.getVehicles().size());
		
		double fix = 0.0;
		for(Vehicle v : vrp.getVehicles()){
			if(v.getType() instanceof PenaltyVehicleType) {
				fix = v.getType().getVehicleCostParams().fix;
			}
		}
		assertEquals(10000,fix,0.01);
		
	}
	
	@Test
	public void whenSettingAddPenaltyVehicleOptionsAndTwoVehiclesWithDiffLocationAndType_twoPenaltyVehicleIsAdded(){
		VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
		VehicleType type = VehicleTypeImpl.Builder.newInstance("type").build();
		VehicleImpl vehicle = VehicleImpl.Builder.newInstance("v").setStartLocationId("loc").setType(type).build();
		VehicleImpl vehicle2 = VehicleImpl.Builder.newInstance("v2").setStartLocationId("loc2").setType(type).build();
		
		builder.addVehicle(vehicle);
		builder.addVehicle(vehicle2);
		builder.setFleetSize(FleetSize.FINITE);
		builder.addPenaltyVehicles(3.0);
		
		VehicleRoutingProblem vrp = builder.build();
		
		assertEquals(4,vrp.getVehicles().size());
		
		int countPenaltyVehicles = 0;
		for(Vehicle v : vrp.getVehicles()){
			if(v.getType() instanceof PenaltyVehicleType) {
				countPenaltyVehicles++;
			}
			
		}
		assertEquals(2,countPenaltyVehicles);
		
	}
	
	@Test
	public void whenSettingAddPenaltyVehicleOptionsAndTwoVehiclesWithSameLocationButDiffType_twoPenaltyVehicleIsAdded(){
		VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
		VehicleType type = VehicleTypeImpl.Builder.newInstance("type").build();
		VehicleType type2 = VehicleTypeImpl.Builder.newInstance("type2").build();
		VehicleImpl vehicle = VehicleImpl.Builder.newInstance("v").setStartLocationId("loc").setType(type).build();
		VehicleImpl vehicle2 = VehicleImpl.Builder.newInstance("v2").setStartLocationId("loc").setType(type2).build();
		
		builder.addVehicle(vehicle);
		builder.addVehicle(vehicle2);
		builder.setFleetSize(FleetSize.FINITE);
		builder.addPenaltyVehicles(3.0);
		
		VehicleRoutingProblem vrp = builder.build();
		
		assertEquals(4,vrp.getVehicles().size());
		
		int countPenaltyVehicles = 0;
		for(Vehicle v : vrp.getVehicles()){
			if(v.getType() instanceof PenaltyVehicleType) {
				countPenaltyVehicles++;
			}
		}
		assertEquals(2,countPenaltyVehicles);
	}
	
	@Test
	public void whenAddingVehicleWithDiffStartAndEnd_startLocationMustBeRegisteredInLocationMap(){
		VehicleImpl vehicle = VehicleImpl.Builder.newInstance("v").setStartLocationId("start").setEndLocationId("end").build();
		
		VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
		vrpBuilder.addVehicle(vehicle);
		assertTrue(vrpBuilder.getLocationMap().containsKey("start"));
	}
	
	@Test
	public void whenAddingVehicleWithDiffStartAndEnd_endLocationMustBeRegisteredInLocationMap(){
		VehicleImpl vehicle = VehicleImpl.Builder.newInstance("v").setStartLocationId("start").setEndLocationId("end").build();
		
		VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
		vrpBuilder.addVehicle(vehicle);
		assertTrue(vrpBuilder.getLocationMap().containsKey("end"));
	}
	
	@Test 
	public void whenAddingInitialRoute_itShouldBeAddedCorrectly(){
		VehicleImpl vehicle = VehicleImpl.Builder.newInstance("v").setStartLocationId("start").setEndLocationId("end").build();
		VehicleRoute route = VehicleRoute.Builder.newInstance(vehicle, DriverImpl.noDriver()).build();
		VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
		vrpBuilder.addInitialVehicleRoute(route);
		VehicleRoutingProblem vrp = vrpBuilder.build();	
		assertTrue(!vrp.getInitialVehicleRoutes().isEmpty());
	}
	
	@Test 
	public void whenAddingInitialRoutes_theyShouldBeAddedCorrectly(){
		VehicleImpl vehicle1 = VehicleImpl.Builder.newInstance("v").setStartLocationId("start").setEndLocationId("end").build();
		VehicleRoute route1 = VehicleRoute.Builder.newInstance(vehicle1, DriverImpl.noDriver()).build();
		
		VehicleImpl vehicle2 = VehicleImpl.Builder.newInstance("v").setStartLocationId("start").setEndLocationId("end").build();
		VehicleRoute route2 = VehicleRoute.Builder.newInstance(vehicle2, DriverImpl.noDriver()).build();
		
		VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
		vrpBuilder.addInitialVehicleRoutes(Arrays.asList(route1,route2));
		
		VehicleRoutingProblem vrp = vrpBuilder.build();	
		assertEquals(2,vrp.getInitialVehicleRoutes().size());
	}
	
	@Test 
	public void whenAddingInitialRoute_locationOfVehicleMustBeMemorized(){
		VehicleImpl vehicle = VehicleImpl.Builder.newInstance("v").setStartLocationId("start").setStartLocationCoordinate(Coordinate.newInstance(0, 1)).setEndLocationId("end").build();
		VehicleRoute route = VehicleRoute.Builder.newInstance(vehicle, DriverImpl.noDriver()).build();
		VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
		vrpBuilder.addInitialVehicleRoute(route);
		VehicleRoutingProblem vrp = vrpBuilder.build();	
		assertEquals(0.,vrp.getLocations().getCoord("start").getX(),0.01);
		assertEquals(1.,vrp.getLocations().getCoord("start").getY(),0.01);
	}
	
	@Test
	public void whenAddingJobAndInitialRouteWithThatJobAfterwards_thisJobShouldNotBeInFinalJobMap(){
		Service service = Service.Builder.newInstance("myService").setLocationId("loc").build();
		VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
		vrpBuilder.addJob(service);
		VehicleImpl vehicle = VehicleImpl.Builder.newInstance("v").setStartLocationId("start").setStartLocationCoordinate(Coordinate.newInstance(0, 1)).setEndLocationId("end").build();
		VehicleRoute initialRoute = VehicleRoute.Builder.newInstance(vehicle).addService(service).build();
		vrpBuilder.addInitialVehicleRoute(initialRoute);
		VehicleRoutingProblem vrp = vrpBuilder.build();
		assertFalse(vrp.getJobs().containsKey("myService"));
	}

    @Test
    public void whenAddingTwoJobs_theyShouldHaveProperIndeces(){
        Service service = Service.Builder.newInstance("myService").setLocationId("loc").build();
        Shipment shipment = Shipment.Builder.newInstance("shipment").setPickupLocation("pick").setDeliveryLocation("del").build();
        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        vrpBuilder.addJob(service);
        vrpBuilder.addJob(shipment);
        vrpBuilder.build();

        assertEquals(0,service.getIndex());
        assertEquals(1,shipment.getIndex());

    }

    @Test
    public void whenAddingTwoVehicles_theyShouldHaveProperIndices(){
        VehicleImpl veh1 = VehicleImpl.Builder.newInstance("v1").setStartLocationId("start").setStartLocationCoordinate(Coordinate.newInstance(0, 1)).setEndLocationId("end").build();
        VehicleImpl veh2 = VehicleImpl.Builder.newInstance("v2").setStartLocationId("start").setStartLocationCoordinate(Coordinate.newInstance(0, 1)).setEndLocationId("end").build();

        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        vrpBuilder.addVehicle(veh1);
        vrpBuilder.addVehicle(veh2);
        vrpBuilder.build();

        assertEquals(0,veh1.getIndex());
        assertEquals(1,veh2.getIndex());

    }

    @Test
    public void whenAddingTwoVehiclesWithSameTypeIdentifier_typeIdentifiersShouldHaveSameIndices(){
        VehicleImpl veh1 = VehicleImpl.Builder.newInstance("v1").setStartLocationId("start").setStartLocationCoordinate(Coordinate.newInstance(0, 1)).setEndLocationId("end").build();
        VehicleImpl veh2 = VehicleImpl.Builder.newInstance("v2").setStartLocationId("start").setStartLocationCoordinate(Coordinate.newInstance(0, 1)).setEndLocationId("end").build();

        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        vrpBuilder.addVehicle(veh1);
        vrpBuilder.addVehicle(veh2);
        vrpBuilder.build();

        assertEquals(0, veh1.getVehicleTypeIdentifier().getIndex());
        assertEquals(0, veh2.getVehicleTypeIdentifier().getIndex());

    }

    @Test
    public void whenAddingTwoVehiclesDifferentTypeIdentifier_typeIdentifiersShouldHaveDifferentIndices(){
        VehicleImpl veh1 = VehicleImpl.Builder.newInstance("v1").setStartLocationId("start").setStartLocationCoordinate(Coordinate.newInstance(0, 1)).setEndLocationId("end").build();
        VehicleImpl veh2 = VehicleImpl.Builder.newInstance("v2").setStartLocationId("startLoc").setStartLocationCoordinate(Coordinate.newInstance(0, 1)).setEndLocationId("end").build();

        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        vrpBuilder.addVehicle(veh1);
        vrpBuilder.addVehicle(veh2);
        vrpBuilder.build();

        assertEquals(0,veh1.getVehicleTypeIdentifier().getIndex());
        assertEquals(1,veh2.getVehicleTypeIdentifier().getIndex());

    }
}
