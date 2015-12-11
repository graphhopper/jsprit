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
package jsprit.core.algorithm.recreate;

import jsprit.core.algorithm.state.StateManager;
import jsprit.core.problem.AbstractActivity;
import jsprit.core.problem.JobActivityFactory;
import jsprit.core.problem.Location;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.constraint.ConstraintManager;
import jsprit.core.problem.cost.AbstractForwardVehicleRoutingTransportCosts;
import jsprit.core.problem.cost.VehicleRoutingActivityCosts;
import jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import jsprit.core.problem.driver.Driver;
import jsprit.core.problem.driver.DriverImpl;
import jsprit.core.problem.driver.DriverImpl.NoDriver;
import jsprit.core.problem.job.Job;
import jsprit.core.problem.job.Service;
import jsprit.core.problem.misc.JobInsertionContext;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.solution.route.activity.TimeWindow;
import jsprit.core.problem.vehicle.Vehicle;
import jsprit.core.problem.vehicle.VehicleImpl;
import jsprit.core.problem.vehicle.VehicleType;
import jsprit.core.problem.vehicle.VehicleTypeImpl;
import jsprit.core.util.Coordinate;
import jsprit.core.util.EuclideanDistanceCalculator;
import jsprit.core.util.Locations;
import jsprit.core.util.ManhattanDistanceCalculator;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;


public class TestCalculatesServiceInsertion {

    ServiceInsertionCalculator serviceInsertion;

    VehicleRoutingTransportCosts costs;

    VehicleImpl vehicle;

    VehicleImpl newVehicle;

    private Service first;

    private Service third;

    private Service second;

    private StateManager states;

    private NoDriver driver;

    private VehicleRoutingProblem vrp;

    @Before
    public void setup() {

        VehicleType t1 = VehicleTypeImpl.Builder.newInstance("t1").addCapacityDimension(0, 1000).setCostPerDistance(1.0).build();
        vehicle = VehicleImpl.Builder.newInstance("vehicle").setLatestArrival(100.0).setStartLocation(Location.newInstance("0,0")).setType(t1).build();

        VehicleType t2 = VehicleTypeImpl.Builder.newInstance("t2").addCapacityDimension(0, 1000).setCostPerDistance(2.0).build();
        newVehicle = VehicleImpl.Builder.newInstance("newVehicle").setLatestArrival(100.0).setStartLocation(Location.newInstance("0,0")).setType(t2).build();

        driver = DriverImpl.noDriver();

        final Locations locations = new Locations() {

            @Override
            public Coordinate getCoord(String id) {
                //assume: locationId="x,y"
                String[] splitted = id.split(",");
                return Coordinate.newInstance(Double.parseDouble(splitted[0]),
                    Double.parseDouble(splitted[1]));
            }

        };
        costs = new AbstractForwardVehicleRoutingTransportCosts() {

            @Override
            public double getTransportTime(Location from, Location to, double departureTime, Driver driver, Vehicle vehicle) {
                return ManhattanDistanceCalculator.calculateDistance(locations.getCoord(from.getId()), locations.getCoord(to.getId()));
            }

            @Override
            public double getTransportCost(Location from, Location to, double departureTime, Driver driver, Vehicle vehicle) {
                return vehicle.getType().getVehicleCostParams().perDistanceUnit * ManhattanDistanceCalculator.calculateDistance(locations.getCoord(from.getId()), locations.getCoord(to.getId()));
            }
        };


        first = Service.Builder.newInstance("1").addSizeDimension(0, 0).setLocation(Location.newInstance("0,10")).setTimeWindow(TimeWindow.newInstance(0.0, 100.0)).build();
        second = Service.Builder.newInstance("2").addSizeDimension(0, 0).setLocation(Location.newInstance("10,10")).setTimeWindow(TimeWindow.newInstance(0.0, 100.0)).build();
        third = Service.Builder.newInstance("3").addSizeDimension(0, 0).setLocation(Location.newInstance("10,0")).setTimeWindow(TimeWindow.newInstance(0.0, 100.0)).build();

        Collection<Job> jobs = new ArrayList<Job>();
        jobs.add(first);
        jobs.add(third);
        jobs.add(second);

        vrp = VehicleRoutingProblem.Builder.newInstance().addAllJobs(jobs)
            .addVehicle(vehicle).setRoutingCost(costs).build();

        states = new StateManager(vrp);
        states.updateLoadStates();
        states.updateTimeWindowStates();

        ConstraintManager cManager = new ConstraintManager(vrp, states);
        cManager.addLoadConstraint();
        cManager.addTimeWindowConstraint();

        VehicleRoutingActivityCosts actCosts = mock(VehicleRoutingActivityCosts.class);

        serviceInsertion = new ServiceInsertionCalculator(costs, new LocalActivityInsertionCostsCalculator(costs, actCosts, states), cManager);
        serviceInsertion.setJobActivityFactory(new JobActivityFactory() {
            @Override
            public List<AbstractActivity> createActivities(Job job) {
                return vrp.copyAndGetActivities(job);
            }
        });
    }

    @Test
    public void whenInsertingTheFirstJobInAnEmptyTourWithVehicle_itCalculatesMarginalCostChanges() {
        VehicleRoute route = VehicleRoute.Builder.newInstance(vehicle, driver).build();
        states.informInsertionStarts(Arrays.asList(route), null);

        InsertionData iData = serviceInsertion.getInsertionData(route, first, vehicle, vehicle.getEarliestDeparture(), null, Double.MAX_VALUE);
        assertEquals(20.0, iData.getInsertionCost(), 0.2);
        assertEquals(0, iData.getDeliveryInsertionIndex());
    }

    @Test
    public void whenInsertingTheSecondJobInAnNonEmptyTourWithVehicle_itCalculatesMarginalCostChanges() {
        VehicleRoute route = VehicleRoute.Builder.newInstance(vehicle, driver).setJobActivityFactory(vrp.getJobActivityFactory()).addService(first).build();
        states.informInsertionStarts(Arrays.asList(route), null);

        InsertionData iData = serviceInsertion.getInsertionData(route, third, vehicle, vehicle.getEarliestDeparture(), null, Double.MAX_VALUE);
        assertEquals(20.0, iData.getInsertionCost(), 0.2);
        assertEquals(0, iData.getDeliveryInsertionIndex());
    }

    @Test
    public void whenInsertingThirdJobWithVehicle_itCalculatesMarginalCostChanges() {
        VehicleRoute route = VehicleRoute.Builder.newInstance(vehicle, driver).setJobActivityFactory(vrp.getJobActivityFactory()).addService(first).addService(third).build();
        states.informInsertionStarts(Arrays.asList(route), null);

        InsertionData iData = serviceInsertion.getInsertionData(route, second, vehicle, vehicle.getEarliestDeparture(), null, Double.MAX_VALUE);
        assertEquals(0.0, iData.getInsertionCost(), 0.2);
        assertEquals(1, iData.getDeliveryInsertionIndex());
    }

    @Test
    public void whenInsertingThirdJobWithNewVehicle_itCalculatesMarginalCostChanges() {
        VehicleRoute route = VehicleRoute.Builder.newInstance(vehicle, driver).setJobActivityFactory(vrp.getJobActivityFactory()).addService(first).addService(third).build();
        states.informInsertionStarts(Arrays.asList(route), null);

        InsertionData iData = serviceInsertion.getInsertionData(route, second, newVehicle, newVehicle.getEarliestDeparture(), null, Double.MAX_VALUE);
        assertEquals(40.0, iData.getInsertionCost(), 0.2);
        assertEquals(1, iData.getDeliveryInsertionIndex());
    }

    @Test
    public void whenInsertingASecondJobWithAVehicle_itCalculatesLocalMarginalCostChanges() {
        VehicleRoute route = VehicleRoute.Builder.newInstance(vehicle, driver).setJobActivityFactory(vrp.getJobActivityFactory()).addService(first).addService(second).build();
        states.informInsertionStarts(Arrays.asList(route), null);

        InsertionData iData = serviceInsertion.getInsertionData(route, third, vehicle, vehicle.getEarliestDeparture(), null, Double.MAX_VALUE);
        assertEquals(0.0, iData.getInsertionCost(), 0.2);
        assertEquals(2, iData.getDeliveryInsertionIndex());
    }

    @Test
    public void whenInsertingASecondJobWithANewVehicle_itCalculatesLocalMarginalCostChanges() {

        VehicleRoute route = VehicleRoute.Builder.newInstance(vehicle, driver).setJobActivityFactory(vrp.getJobActivityFactory()).addService(first).addService(second).build();

        states.informInsertionStarts(Arrays.asList(route), null);

        InsertionData iData = serviceInsertion.getInsertionData(route, third, newVehicle, newVehicle.getEarliestDeparture(), null, Double.MAX_VALUE);
        assertEquals(50.0, iData.getInsertionCost(), 0.2);
        assertEquals(2, iData.getDeliveryInsertionIndex());
    }

    @Test
    public void whenInsertingJobAndCurrRouteIsEmpty_accessEggressCalcShouldReturnZero() {
        VehicleRoute route = VehicleRoute.Builder.newInstance(VehicleImpl.createNoVehicle(), DriverImpl.noDriver()).build();
        AdditionalAccessEgressCalculator accessEgressCalc = new AdditionalAccessEgressCalculator(costs);
        Job job = Service.Builder.newInstance("1").addSizeDimension(0, 0).setLocation(Location.newInstance("1")).setTimeWindow(TimeWindow.newInstance(0.0, 100.0)).build();
        JobInsertionContext iContex = new JobInsertionContext(route, job, newVehicle, mock(Driver.class), 0.0);
        assertEquals(0.0, accessEgressCalc.getCosts(iContex), 0.01);
    }

    @Test
    public void whenInsertingJobAndCurrRouteAndVehicleHaveTheSameLocation_accessEggressCalcShouldReturnZero() {
        VehicleRoute route = VehicleRoute.Builder.newInstance(newVehicle, DriverImpl.noDriver())
            .addService(first)
            .build();

        AdditionalAccessEgressCalculator accessEgressCalc = new AdditionalAccessEgressCalculator(costs);
        JobInsertionContext iContex = new JobInsertionContext(route, first, newVehicle, mock(Driver.class), 0.0);
        assertEquals(0.0, accessEgressCalc.getCosts(iContex), 0.01);
    }

    @Test
    public void whenInsertingJobAndCurrRouteAndNewVehicleHaveDifferentLocations_accessEggressCostsMustBeCorrect() {
        final Map<String, Coordinate> coords = new HashMap<String, Coordinate>();
        coords.put("oldV", Coordinate.newInstance(1, 0));
        coords.put("newV", Coordinate.newInstance(5, 0));
        coords.put("service", Coordinate.newInstance(0, 0));

        AbstractForwardVehicleRoutingTransportCosts routingCosts = new AbstractForwardVehicleRoutingTransportCosts() {

            @Override
            public double getTransportTime(Location from, Location to, double departureTime, Driver driver, Vehicle vehicle) {
                return getTransportCost(from, to, departureTime, driver, vehicle);
            }

            @Override
            public double getTransportCost(Location from, Location to, double departureTime, Driver driver, Vehicle vehicle) {
                return EuclideanDistanceCalculator.calculateDistance(coords.get(from.getId()), coords.get(to.getId()));
            }
        };
        Vehicle oldVehicle = VehicleImpl.Builder.newInstance("oldV").setStartLocation(Location.newInstance("oldV")).build();

        VehicleRoute route = VehicleRoute.Builder.newInstance(oldVehicle, DriverImpl.noDriver())
            .addService(Service.Builder.newInstance("service").addSizeDimension(0, 0).setLocation(Location.newInstance("service")).build())
            .build();

        Vehicle newVehicle = VehicleImpl.Builder.newInstance("newV").setStartLocation(Location.newInstance("newV")).build();

        AdditionalAccessEgressCalculator accessEgressCalc = new AdditionalAccessEgressCalculator(routingCosts);
        Job job = Service.Builder.newInstance("service2").addSizeDimension(0, 0).setLocation(Location.newInstance("service")).build();
        JobInsertionContext iContex = new JobInsertionContext(route, job, newVehicle, mock(Driver.class), 0.0);
        assertEquals(8.0, accessEgressCalc.getCosts(iContex), 0.01);
    }
}
