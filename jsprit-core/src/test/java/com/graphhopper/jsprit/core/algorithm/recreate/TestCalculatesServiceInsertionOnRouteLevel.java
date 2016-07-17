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
package com.graphhopper.jsprit.core.algorithm.recreate;

import com.graphhopper.jsprit.core.algorithm.ExampleActivityCostFunction;
import com.graphhopper.jsprit.core.algorithm.state.StateManager;
import com.graphhopper.jsprit.core.algorithm.state.UpdateVariableCosts;
import com.graphhopper.jsprit.core.problem.*;
import com.graphhopper.jsprit.core.problem.constraint.ConstraintManager;
import com.graphhopper.jsprit.core.problem.cost.AbstractForwardVehicleRoutingTransportCosts;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import com.graphhopper.jsprit.core.problem.driver.Driver;
import com.graphhopper.jsprit.core.problem.driver.DriverImpl;
import com.graphhopper.jsprit.core.problem.driver.DriverImpl.NoDriver;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleType;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import com.graphhopper.jsprit.core.util.CostFactory;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;


public class TestCalculatesServiceInsertionOnRouteLevel {

    ServiceInsertionOnRouteLevelCalculator serviceInsertion;

    VehicleRoutingTransportCosts costs;

    AbstractVehicle vehicle;

    AbstractVehicle newVehicle;

    private Service first;

    private Service second;

    private Service third;

    private StateManager states;

    private NoDriver driver;

    private VehicleRoutingProblem vrp;

    @Before
    public void setup() {


        costs = mock(VehicleRoutingTransportCosts.class);

        VehicleType type = VehicleTypeImpl.Builder.newInstance("t").addCapacityDimension(0, 1000).build();
        vehicle = VehicleImpl.Builder.newInstance("v1").setType(type).setStartLocation(Location.newInstance("0,0")).setLatestArrival(100.).build();
        newVehicle = VehicleImpl.Builder.newInstance("v2").setType(type).setStartLocation(Location.newInstance("0,0")).setLatestArrival(100.).build();
        driver = DriverImpl.noDriver();

        costs = new AbstractForwardVehicleRoutingTransportCosts() {

            VehicleRoutingTransportCosts routingCosts = CostFactory.createManhattanCosts();

            @Override
            public double getTransportTime(Location from, Location to, double departureTime, Driver driver, Vehicle vehicle) {
                return 0;
            }

            @Override
            public double getTransportCost(Location from, Location to, double departureTime, Driver driver, Vehicle vehicle) {
                double tpCosts = routingCosts.getTransportCost(from, to, departureTime, driver, vehicle);
                if (vehicle.getId().equals("v1")) return tpCosts;
                return 2. * tpCosts;
            }

            @Override
            public double getDistance(Location from, Location to, double departureTime, Vehicle vehicle) {
                return routingCosts.getDistance(from, to, departureTime, vehicle);
            }

        };

        first = Service.Builder.newInstance("1").setLocation(Location.newInstance("0,10")).setTimeWindow(TimeWindow.newInstance(0.0, 100.0)).build();
        second = Service.Builder.newInstance("3").setLocation(Location.newInstance("10,0")).setTimeWindow(TimeWindow.newInstance(0.0, 100.0)).build();
        third = Service.Builder.newInstance("2").setLocation(Location.newInstance("10,10")).setTimeWindow(TimeWindow.newInstance(0.0, 100.0)).build();
        Collection<Job> jobs = new ArrayList<Job>();
        jobs.add(first);
        jobs.add(second);
        jobs.add(third);

        vrp = VehicleRoutingProblem.Builder.newInstance().addAllJobs(jobs).addVehicle(vehicle).addVehicle(newVehicle).setRoutingCost(costs).build();

        states = new StateManager(vrp);
        states.updateLoadStates();
        states.updateTimeWindowStates();
        states.addStateUpdater(new UpdateVariableCosts(vrp.getActivityCosts(), vrp.getTransportCosts(), states));

        ConstraintManager cManager = new ConstraintManager(vrp, states);
        cManager.addLoadConstraint();
        cManager.addTimeWindowConstraint();


        ExampleActivityCostFunction activityCosts = new ExampleActivityCostFunction();
        ActivityInsertionCostsCalculator actInsertionCostCalculator = new RouteLevelActivityInsertionCostsEstimator(costs, activityCosts, states);
        serviceInsertion = new ServiceInsertionOnRouteLevelCalculator(costs, activityCosts, actInsertionCostCalculator, cManager, cManager);
        serviceInsertion.setNuOfActsForwardLooking(4);
        serviceInsertion.setStates(states);
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
    public void whenInsertingThirdJobWithVehicle_itCalculatesMarginalCostChanges() {
        VehicleRoute route = VehicleRoute.Builder.newInstance(vehicle, driver).setJobActivityFactory(vrp.getJobActivityFactory()).addService(first).addService(second).build();
        states.informInsertionStarts(Arrays.asList(route), null);

        InsertionData iData = serviceInsertion.getInsertionData(route, third, vehicle, vehicle.getEarliestDeparture(), null, Double.MAX_VALUE);
        assertEquals(0.0, iData.getInsertionCost(), 0.2);
        assertEquals(1, iData.getDeliveryInsertionIndex());
    }

    @Test
    public void whenInsertingThirdJobWithNewVehicle_itCalculatesMarginalCostChanges() {
        VehicleRoute route = VehicleRoute.Builder.newInstance(vehicle, driver).setJobActivityFactory(vrp.getJobActivityFactory()).addService(first).addService(second).build();
        states.informInsertionStarts(Arrays.asList(route), null);

        InsertionData iData = serviceInsertion.getInsertionData(route, third, newVehicle, vehicle.getEarliestDeparture(), null, Double.MAX_VALUE);
        assertEquals(40.0, iData.getInsertionCost(), 0.2);
        assertEquals(1, iData.getDeliveryInsertionIndex());
    }

    @Test
    public void whenInsertingASecondJobWithAVehicle_itCalculatesLocalMarginalCostChanges() {
        VehicleRoute route = VehicleRoute.Builder.newInstance(vehicle, driver).setJobActivityFactory(vrp.getJobActivityFactory()).addService(first).addService(third).build();
        states.informInsertionStarts(Arrays.asList(route), null);

        InsertionData iData = serviceInsertion.getInsertionData(route, second, vehicle, vehicle.getEarliestDeparture(), null, Double.MAX_VALUE);
        assertEquals(0.0, iData.getInsertionCost(), 0.2);
        assertEquals(2, iData.getDeliveryInsertionIndex());
    }

    @Test
    public void whenInsertingASecondJobWithANewVehicle_itCalculatesLocalMarginalCostChanges() {
        VehicleRoute route = VehicleRoute.Builder.newInstance(vehicle, driver).setJobActivityFactory(vrp.getJobActivityFactory()).addService(first).addService(third).build();
        states.informInsertionStarts(Arrays.asList(route), null);

        InsertionData iData = serviceInsertion.getInsertionData(route, second, newVehicle, vehicle.getEarliestDeparture(), null, Double.MAX_VALUE);
        assertEquals(40.0, iData.getInsertionCost(), 0.2);
        assertEquals(2, iData.getDeliveryInsertionIndex());
    }


}
