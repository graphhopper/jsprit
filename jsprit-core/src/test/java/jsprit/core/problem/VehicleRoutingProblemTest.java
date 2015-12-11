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
package jsprit.core.problem;

import jsprit.core.problem.VehicleRoutingProblem.FleetSize;
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
import jsprit.core.problem.vehicle.Vehicle;
import jsprit.core.problem.vehicle.VehicleImpl;
import jsprit.core.problem.vehicle.VehicleType;
import jsprit.core.problem.vehicle.VehicleTypeImpl;
import jsprit.core.util.Coordinate;
import jsprit.core.util.TestUtils;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class VehicleRoutingProblemTest {

    @Test
    public void whenBuildingWithInfiniteFleet_fleetSizeShouldBeInfinite() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
        builder.setFleetSize(FleetSize.INFINITE);
        VehicleRoutingProblem vrp = builder.build();
        assertEquals(FleetSize.INFINITE, vrp.getFleetSize());
    }

    @Test
    public void whenBuildingWithFiniteFleet_fleetSizeShouldBeFinite() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
        builder.setFleetSize(FleetSize.FINITE);
        VehicleRoutingProblem vrp = builder.build();
        assertEquals(FleetSize.FINITE, vrp.getFleetSize());
    }

    @Test
    public void whenBuildingWithFourVehicles_vrpShouldContainTheCorrectNuOfVehicles() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();

        VehicleImpl v1 = VehicleImpl.Builder.newInstance("v1").setStartLocation(Location.newInstance("start")).build();
        VehicleImpl v2 = VehicleImpl.Builder.newInstance("v2").setStartLocation(Location.newInstance("start")).build();
        VehicleImpl v3 = VehicleImpl.Builder.newInstance("v3").setStartLocation(Location.newInstance("start")).build();
        VehicleImpl v4 = VehicleImpl.Builder.newInstance("v4").setStartLocation(Location.newInstance("start")).build();

        builder.addVehicle(v1).addVehicle(v2).addVehicle(v3).addVehicle(v4);

        VehicleRoutingProblem vrp = builder.build();
        assertEquals(4, vrp.getVehicles().size());
        assertEquals(1, vrp.getAllLocations().size());

    }

    @Test
    public void whenAddingFourVehiclesAllAtOnce_vrpShouldContainTheCorrectNuOfVehicles() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();

        VehicleImpl v1 = VehicleImpl.Builder.newInstance("v1").setStartLocation(Location.newInstance("start")).build();
        VehicleImpl v2 = VehicleImpl.Builder.newInstance("v2").setStartLocation(Location.newInstance("start")).build();
        VehicleImpl v3 = VehicleImpl.Builder.newInstance("v3").setStartLocation(Location.newInstance("start")).build();
        VehicleImpl v4 = VehicleImpl.Builder.newInstance("v4").setStartLocation(Location.newInstance("start")).build();

        builder.addAllVehicles(Arrays.asList(v1, v2, v3, v4));

        VehicleRoutingProblem vrp = builder.build();
        assertEquals(4, vrp.getVehicles().size());

    }

    @Test
    public void whenBuildingWithFourVehiclesAndTwoTypes_vrpShouldContainTheCorrectNuOfTypes() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();

        VehicleTypeImpl type1 = VehicleTypeImpl.Builder.newInstance("type1").build();
        VehicleTypeImpl type2 = VehicleTypeImpl.Builder.newInstance("type2").build();

        VehicleImpl v1 = VehicleImpl.Builder.newInstance("v1").setStartLocation(Location.newInstance("yo")).setType(type1).build();
        VehicleImpl v2 = VehicleImpl.Builder.newInstance("v2").setStartLocation(Location.newInstance("yo")).setType(type1).build();
        VehicleImpl v3 = VehicleImpl.Builder.newInstance("v3").setStartLocation(Location.newInstance("yo")).setType(type2).build();
        VehicleImpl v4 = VehicleImpl.Builder.newInstance("v4").setStartLocation(Location.newInstance("yo")).setType(type2).build();

        builder.addVehicle(v1).addVehicle(v2).addVehicle(v3).addVehicle(v4);

        VehicleRoutingProblem vrp = builder.build();
        assertEquals(2, vrp.getTypes().size());

    }

    @Test
    public void whenShipmentsAreAdded_vrpShouldContainThem() {
        Shipment s = Shipment.Builder.newInstance("s").addSizeDimension(0, 10).setPickupLocation(Location.Builder.newInstance().setId("foofoo").build()).setDeliveryLocation(Location.newInstance("foo")).build();
        Shipment s2 = Shipment.Builder.newInstance("s2").addSizeDimension(0, 100).setPickupLocation(Location.Builder.newInstance().setId("foofoo").build()).setDeliveryLocation(Location.newInstance("foo")).build();
        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        vrpBuilder.addJob(s);
        vrpBuilder.addJob(s2);
        VehicleRoutingProblem vrp = vrpBuilder.build();

        assertEquals(2, vrp.getJobs().size());
        assertEquals(s, vrp.getJobs().get("s"));
        assertEquals(s2, vrp.getJobs().get("s2"));
        assertEquals(2,vrp.getAllLocations().size());
    }

    @Test
    public void whenServicesAreAdded_vrpShouldContainThem() {
        Service s1 = mock(Service.class);
        when(s1.getId()).thenReturn("s1");
        when(s1.getLocation()).thenReturn(Location.Builder.newInstance().setIndex(1).build());
        Service s2 = mock(Service.class);
        when(s2.getId()).thenReturn("s2");
        when(s2.getLocation()).thenReturn(Location.Builder.newInstance().setIndex(1).build());

        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        vrpBuilder.addJob(s1).addJob(s2);

        VehicleRoutingProblem vrp = vrpBuilder.build();

        assertEquals(2, vrp.getJobs().size());
        assertEquals(s1, vrp.getJobs().get("s1"));
        assertEquals(s2, vrp.getJobs().get("s2"));
        assertEquals(1,vrp.getAllLocations().size());
    }


    @Test
    public void whenPickupsAreAdded_vrpShouldContainThem() {
        Pickup s1 = mock(Pickup.class);
        when(s1.getId()).thenReturn("s1");
        when(s1.getLocation()).thenReturn(Location.Builder.newInstance().setIndex(1).build());
        Pickup s2 = mock(Pickup.class);
        when(s2.getId()).thenReturn("s2");
        when(s2.getLocation()).thenReturn(Location.Builder.newInstance().setIndex(1).build());

        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        vrpBuilder.addJob(s1).addJob(s2);

        VehicleRoutingProblem vrp = vrpBuilder.build();

        assertEquals(2, vrp.getJobs().size());
        assertEquals(s1, vrp.getJobs().get("s1"));
        assertEquals(s2, vrp.getJobs().get("s2"));
    }

    @Test
    public void whenPickupsAreAddedAllAtOnce_vrpShouldContainThem() {
        Pickup s1 = mock(Pickup.class);
        when(s1.getId()).thenReturn("s1");
        when(s1.getLocation()).thenReturn(Location.Builder.newInstance().setIndex(1).build());
        Pickup s2 = mock(Pickup.class);
        when(s2.getId()).thenReturn("s2");
        when(s2.getLocation()).thenReturn(Location.Builder.newInstance().setIndex(1).build());

        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        vrpBuilder.addAllJobs(Arrays.asList(s1, s2));

        VehicleRoutingProblem vrp = vrpBuilder.build();

        assertEquals(2, vrp.getJobs().size());
        assertEquals(s1, vrp.getJobs().get("s1"));
        assertEquals(s2, vrp.getJobs().get("s2"));
    }

    @Test
    public void whenDelivieriesAreAdded_vrpShouldContainThem() {
        Delivery s1 = mock(Delivery.class);
        when(s1.getId()).thenReturn("s1");
        when(s1.getSize()).thenReturn(Capacity.Builder.newInstance().build());
        when(s1.getLocation()).thenReturn(Location.Builder.newInstance().setIndex(1).build());
        Delivery s2 = mock(Delivery.class);
        when(s2.getId()).thenReturn("s2");
        when(s2.getSize()).thenReturn(Capacity.Builder.newInstance().build());
        when(s2.getLocation()).thenReturn(Location.Builder.newInstance().setIndex(1).build());

        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        vrpBuilder.addJob(s1).addJob(s2);

        VehicleRoutingProblem vrp = vrpBuilder.build();

        assertEquals(2, vrp.getJobs().size());
        assertEquals(s1, vrp.getJobs().get("s1"));
        assertEquals(s2, vrp.getJobs().get("s2"));
    }

    @Test
    public void whenDelivieriesAreAddedAllAtOnce_vrpShouldContainThem() {
        Delivery s1 = mock(Delivery.class);
        when(s1.getId()).thenReturn("s1");
        when(s1.getSize()).thenReturn(Capacity.Builder.newInstance().build());
        when(s1.getLocation()).thenReturn(Location.Builder.newInstance().setIndex(1).build());
        Delivery s2 = mock(Delivery.class);
        when(s2.getId()).thenReturn("s2");
        when(s2.getSize()).thenReturn(Capacity.Builder.newInstance().build());
        when(s2.getLocation()).thenReturn(Location.Builder.newInstance().setIndex(1).build());

        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        vrpBuilder.addAllJobs(Arrays.asList(s1, s2));

        VehicleRoutingProblem vrp = vrpBuilder.build();

        assertEquals(2, vrp.getJobs().size());
        assertEquals(s1, vrp.getJobs().get("s1"));
        assertEquals(s2, vrp.getJobs().get("s2"));
    }

    @Test
    public void whenServicesAreAddedAllAtOnce_vrpShouldContainThem() {
        Service s1 = mock(Service.class);
        when(s1.getId()).thenReturn("s1");
        when(s1.getLocation()).thenReturn(Location.Builder.newInstance().setIndex(1).build());
        Service s2 = mock(Service.class);
        when(s2.getId()).thenReturn("s2");
        when(s2.getLocation()).thenReturn(Location.Builder.newInstance().setIndex(1).build());

        Collection<Service> services = new ArrayList<Service>();
        services.add(s1);
        services.add(s2);

        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        vrpBuilder.addAllJobs(services);

        VehicleRoutingProblem vrp = vrpBuilder.build();

        assertEquals(2, vrp.getJobs().size());
        assertEquals(s1, vrp.getJobs().get("s1"));
        assertEquals(s2, vrp.getJobs().get("s2"));
    }


    @Test
    public void whenSettingActivityCosts_vrpShouldContainIt() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
        builder.setActivityCosts(new VehicleRoutingActivityCosts() {

            @Override
            public double getActivityCost(TourActivity tourAct, double arrivalTime, Driver driver, Vehicle vehicle) {
                return 4.0;
            }

        });

        VehicleRoutingProblem problem = builder.build();
        assertEquals(4.0, problem.getActivityCosts().getActivityCost(null, 0.0, null, null), 0.01);
    }

    @Test
    public void whenSettingRoutingCosts_vprShouldContainIt() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();

        builder.setRoutingCost(new AbstractForwardVehicleRoutingTransportCosts() {

            @Override
            public double getTransportTime(Location from, Location to,
                                           double departureTime, Driver driver, Vehicle vehicle) {
                return 0;
            }

            @Override
            public double getTransportCost(Location from, Location to,
                                           double departureTime, Driver driver, Vehicle vehicle) {
                return 4.0;
            }
        });

        VehicleRoutingProblem problem = builder.build();
        assertEquals(4.0, problem.getTransportCosts().getTransportCost(loc(""), loc(""), 0.0, null, null), 0.01);
    }

    private Location loc(String i) {
        return Location.Builder.newInstance().setId(i).build();
    }

    @Test
    public void whenAddingAVehicle_getAddedVehicleTypesShouldReturnItsType() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
        VehicleType type = VehicleTypeImpl.Builder.newInstance("type").build();
        VehicleImpl vehicle = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance("loc")).setType(type).build();
        builder.addVehicle(vehicle);

        assertEquals(1, builder.getAddedVehicleTypes().size());
        assertEquals(type, builder.getAddedVehicleTypes().iterator().next());


    }

    @Test
    public void whenAddingTwoVehicleWithSameType_getAddedVehicleTypesShouldReturnOnlyOneType() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
        VehicleType type = VehicleTypeImpl.Builder.newInstance("type").build();
        VehicleImpl vehicle = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance("loc")).setType(type).build();
        VehicleImpl vehicle2 = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance("loc")).setType(type).build();

        builder.addVehicle(vehicle);
        builder.addVehicle(vehicle2);

        assertEquals(1, builder.getAddedVehicleTypes().size());
        assertEquals(type, builder.getAddedVehicleTypes().iterator().next());
    }

    @Test
    public void whenAddingTwoVehicleWithDiffType_getAddedVehicleTypesShouldReturnTheseType() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
        VehicleType type = VehicleTypeImpl.Builder.newInstance("type").build();
        VehicleType type2 = VehicleTypeImpl.Builder.newInstance("type2").build();

        VehicleImpl vehicle = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance("loc")).setType(type).build();
        VehicleImpl vehicle2 = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance("loc")).setType(type2).build();

        builder.addVehicle(vehicle);
        builder.addVehicle(vehicle2);

        assertEquals(2, builder.getAddedVehicleTypes().size());

    }


    @Test
    public void whenAddingVehicleWithDiffStartAndEnd_startLocationMustBeRegisteredInLocationMap() {
        VehicleImpl vehicle = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance("start"))
            .setEndLocation(Location.newInstance("end")).build();

        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        vrpBuilder.addVehicle(vehicle);
        assertTrue(vrpBuilder.getLocationMap().containsKey("start"));
    }

    @Test
    public void whenAddingVehicleWithDiffStartAndEnd_endLocationMustBeRegisteredInLocationMap() {
        VehicleImpl vehicle = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance("start"))
            .setEndLocation(Location.newInstance("end")).build();

        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        vrpBuilder.addVehicle(vehicle);
        assertTrue(vrpBuilder.getLocationMap().containsKey("end"));
    }

    @Test
    public void whenAddingInitialRoute_itShouldBeAddedCorrectly() {
        VehicleImpl vehicle = VehicleImpl.Builder.newInstance("v")
            .setStartLocation(Location.newInstance("start")).setEndLocation(Location.newInstance("end")).build();
        VehicleRoute route = VehicleRoute.Builder.newInstance(vehicle, DriverImpl.noDriver()).build();
        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        vrpBuilder.addInitialVehicleRoute(route);
        VehicleRoutingProblem vrp = vrpBuilder.build();
        assertTrue(!vrp.getInitialVehicleRoutes().isEmpty());
    }

    @Test
    public void whenAddingInitialRoutes_theyShouldBeAddedCorrectly() {
        VehicleImpl vehicle1 = VehicleImpl.Builder.newInstance("v")
            .setStartLocation(Location.newInstance("start")).setEndLocation(Location.newInstance("end")).build();
        VehicleRoute route1 = VehicleRoute.Builder.newInstance(vehicle1, DriverImpl.noDriver()).build();

        VehicleImpl vehicle2 = VehicleImpl.Builder.newInstance("v")
            .setStartLocation(Location.newInstance("start")).setEndLocation(Location.newInstance("end")).build();
        VehicleRoute route2 = VehicleRoute.Builder.newInstance(vehicle2, DriverImpl.noDriver()).build();

        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        vrpBuilder.addInitialVehicleRoutes(Arrays.asList(route1, route2));

        VehicleRoutingProblem vrp = vrpBuilder.build();
        assertEquals(2, vrp.getInitialVehicleRoutes().size());
        assertEquals(2,vrp.getAllLocations().size());
    }

    @Test
    public void whenAddingInitialRoute_locationOfVehicleMustBeMemorized() {
        VehicleImpl vehicle = VehicleImpl.Builder.newInstance("v")
            .setStartLocation(TestUtils.loc("start", Coordinate.newInstance(0, 1)))
            .setEndLocation(Location.newInstance("end")).build();
        VehicleRoute route = VehicleRoute.Builder.newInstance(vehicle, DriverImpl.noDriver()).build();
        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        vrpBuilder.addInitialVehicleRoute(route);
        VehicleRoutingProblem vrp = vrpBuilder.build();
        assertEquals(0., vrp.getLocations().getCoord("start").getX(), 0.01);
        assertEquals(1., vrp.getLocations().getCoord("start").getY(), 0.01);
    }

    @Test
    public void whenAddingJobAndInitialRouteWithThatJobAfterwards_thisJobShouldNotBeInFinalJobMap() {
        Service service = Service.Builder.newInstance("myService").setLocation(Location.newInstance("loc")).build();
        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        vrpBuilder.addJob(service);
        VehicleImpl vehicle = VehicleImpl.Builder.newInstance("v")
            .setStartLocation(TestUtils.loc("start", Coordinate.newInstance(0, 1)))
            .setEndLocation(Location.newInstance("end")).build();
        VehicleRoute initialRoute = VehicleRoute.Builder.newInstance(vehicle).addService(service).build();
        vrpBuilder.addInitialVehicleRoute(initialRoute);
        VehicleRoutingProblem vrp = vrpBuilder.build();
        assertFalse(vrp.getJobs().containsKey("myService"));
        assertEquals(3,vrp.getAllLocations().size());
    }

    @Test
    public void whenAddingTwoJobs_theyShouldHaveProperIndeces() {
        Service service = Service.Builder.newInstance("myService").setLocation(Location.newInstance("loc")).build();
        Shipment shipment = Shipment.Builder.newInstance("shipment").setPickupLocation(Location.Builder.newInstance().setId("pick").build())
            .setDeliveryLocation(Location.newInstance("del")).build();
        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        vrpBuilder.addJob(service);
        vrpBuilder.addJob(shipment);
        VehicleRoutingProblem vrp = vrpBuilder.build();

        assertEquals(1, service.getIndex());
        assertEquals(2, shipment.getIndex());
        assertEquals(3,vrp.getAllLocations().size());

    }

    @Test
    public void whenAddingTwoVehicles_theyShouldHaveProperIndices() {
        VehicleImpl veh1 = VehicleImpl.Builder.newInstance("v1").setStartLocation(TestUtils.loc("start", Coordinate.newInstance(0, 1)))
            .setEndLocation(Location.newInstance("end")).build();
        VehicleImpl veh2 = VehicleImpl.Builder.newInstance("v2").setStartLocation(TestUtils.loc("start", Coordinate.newInstance(0, 1)))
            .setEndLocation(Location.newInstance("end")).build();

        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        vrpBuilder.addVehicle(veh1);
        vrpBuilder.addVehicle(veh2);
        vrpBuilder.build();

        assertEquals(1, veh1.getIndex());
        assertEquals(2, veh2.getIndex());

    }

    @Test
    public void whenAddingTwoVehiclesWithSameTypeIdentifier_typeIdentifiersShouldHaveSameIndices() {
        VehicleImpl veh1 = VehicleImpl.Builder.newInstance("v1")
            .setStartLocation(TestUtils.loc("start", Coordinate.newInstance(0, 1)))
            .setEndLocation(Location.newInstance("end")).build();
        VehicleImpl veh2 = VehicleImpl.Builder.newInstance("v2")
            .setStartLocation(TestUtils.loc("start", Coordinate.newInstance(0, 1)))
            .setEndLocation(Location.newInstance("end")).build();

        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        vrpBuilder.addVehicle(veh1);
        vrpBuilder.addVehicle(veh2);
        vrpBuilder.build();

        assertEquals(1, veh1.getVehicleTypeIdentifier().getIndex());
        assertEquals(1, veh2.getVehicleTypeIdentifier().getIndex());

    }

    @Test
    public void whenAddingTwoVehiclesDifferentTypeIdentifier_typeIdentifiersShouldHaveDifferentIndices() {
        VehicleImpl veh1 = VehicleImpl.Builder.newInstance("v1")
            .setStartLocation(TestUtils.loc("start", Coordinate.newInstance(0, 1)))
            .setEndLocation(Location.newInstance("end")).build();
        VehicleImpl veh2 = VehicleImpl.Builder.newInstance("v2")
            .setStartLocation(TestUtils.loc("startLoc", Coordinate.newInstance(0, 1)))
            .setEndLocation(Location.newInstance("end")).build();

        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        vrpBuilder.addVehicle(veh1);
        vrpBuilder.addVehicle(veh2);
        vrpBuilder.build();

        assertEquals(1, veh1.getVehicleTypeIdentifier().getIndex());
        assertEquals(2, veh2.getVehicleTypeIdentifier().getIndex());

    }
}
