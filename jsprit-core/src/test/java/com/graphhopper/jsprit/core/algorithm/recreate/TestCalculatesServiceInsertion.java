/*
 * Licensed to GraphHopper GmbH under one or more contributor
 * license agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * GraphHopper GmbH licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.graphhopper.jsprit.core.algorithm.recreate;

import com.graphhopper.jsprit.core.algorithm.state.StateManager;
import com.graphhopper.jsprit.core.problem.AbstractActivity;
import com.graphhopper.jsprit.core.problem.JobActivityFactory;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.constraint.ConstraintManager;
import com.graphhopper.jsprit.core.problem.cost.AbstractForwardVehicleRoutingTransportCosts;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingActivityCosts;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import com.graphhopper.jsprit.core.problem.driver.Driver;
import com.graphhopper.jsprit.core.problem.driver.DriverImpl;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.misc.JobInsertionContext;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleType;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import com.graphhopper.jsprit.core.util.Coordinate;
import com.graphhopper.jsprit.core.util.EuclideanDistanceCalculator;
import com.graphhopper.jsprit.core.util.Locations;
import com.graphhopper.jsprit.core.util.ManhattanDistanceCalculator;
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

    private DriverImpl.NoDriver driver;

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
            public double getDistance(Location from, Location to, double departureTime, Vehicle vehicle) {
                return ManhattanDistanceCalculator.calculateDistance(locations.getCoord(from.getId()), locations.getCoord(to.getId()));
            }

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

        serviceInsertion = new ServiceInsertionCalculator(costs, vrp.getActivityCosts(), new LocalActivityInsertionCostsCalculator(costs, actCosts, states), cManager);
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
            public double getDistance(Location from, Location to, double departureTime, Vehicle vehicle) {
                return EuclideanDistanceCalculator.calculateDistance(coords.get(from.getId()), coords.get(to.getId()));
            }

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
