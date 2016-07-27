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

import com.graphhopper.jsprit.core.algorithm.recreate.listener.InsertionListeners;
import com.graphhopper.jsprit.core.algorithm.state.StateManager;
import com.graphhopper.jsprit.core.problem.AbstractActivity;
import com.graphhopper.jsprit.core.problem.JobActivityFactory;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.constraint.ConstraintManager;
import com.graphhopper.jsprit.core.problem.constraint.HardActivityConstraint;
import com.graphhopper.jsprit.core.problem.constraint.HardRouteConstraint;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingActivityCosts;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import com.graphhopper.jsprit.core.problem.driver.Driver;
import com.graphhopper.jsprit.core.problem.driver.DriverImpl;
import com.graphhopper.jsprit.core.problem.job.Delivery;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.job.Pickup;
import com.graphhopper.jsprit.core.problem.job.Shipment;
import com.graphhopper.jsprit.core.problem.misc.JobInsertionContext;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.problem.solution.route.state.RouteAndActivityStateGetter;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleType;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import com.graphhopper.jsprit.core.util.CostFactory;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;


public class ServiceInsertionAndLoadConstraintsTest {

    VehicleRoutingTransportCosts routingCosts;

    VehicleRoutingActivityCosts activityCosts = new VehicleRoutingActivityCosts() {

        @Override
        public double getActivityCost(TourActivity tourAct, double arrivalTime, Driver driver, Vehicle vehicle) {
            return 0;
        }

        @Override
        public double getActivityDuration(TourActivity tourAct, double arrivalTime, Driver driver, Vehicle vehicle) {
            return tourAct.getOperationTime();
        }

    };

    HardActivityConstraint hardActivityLevelConstraint = new HardActivityConstraint() {

        @Override
        public ConstraintsStatus fulfilled(JobInsertionContext iFacts, TourActivity prevAct, TourActivity newAct, TourActivity nextAct, double prevActDepTime) {
            return ConstraintsStatus.FULFILLED;
        }
    };

    HardRouteConstraint hardRouteLevelConstraint = new HardRouteConstraint() {

        @Override
        public boolean fulfilled(JobInsertionContext insertionContext) {
            return true;
        }

    };

    ActivityInsertionCostsCalculator activityInsertionCostsCalculator;

    ShipmentInsertionCalculator insertionCalculator;

    VehicleRoutingProblem vehicleRoutingProblem;

    Vehicle vehicle;

    @Before
    public void doBefore() {
        routingCosts = CostFactory.createManhattanCosts();
        VehicleType type = VehicleTypeImpl.Builder.newInstance("t").addCapacityDimension(0, 2).setCostPerDistance(1).build();
        vehicle = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance("0,0")).setType(type).build();
        activityInsertionCostsCalculator = new LocalActivityInsertionCostsCalculator(routingCosts, activityCosts, mock(StateManager.class));
        createInsertionCalculator(hardRouteLevelConstraint);
        vehicleRoutingProblem = mock(VehicleRoutingProblem.class);
    }

    private void createInsertionCalculator(HardRouteConstraint hardRouteLevelConstraint) {
        ConstraintManager constraintManager = new ConstraintManager(mock(VehicleRoutingProblem.class), mock(RouteAndActivityStateGetter.class));
        constraintManager.addConstraint(hardRouteLevelConstraint);
        insertionCalculator = new ShipmentInsertionCalculator(routingCosts, activityCosts, activityInsertionCostsCalculator, constraintManager);
    }

    @Test
    public void whenInsertingServiceWhileNoCapIsAvailable_itMustReturnTheCorrectInsertionIndex() {
        Delivery delivery = (Delivery) Delivery.Builder.newInstance("del").addSizeDimension(0, 41).setLocation(Location.newInstance("10,10")).build();
        Pickup pickup = (Pickup) Pickup.Builder.newInstance("pick").addSizeDimension(0, 15).setLocation(Location.newInstance("0,10")).build();

        VehicleType type = VehicleTypeImpl.Builder.newInstance("t").addCapacityDimension(0, 50).setCostPerDistance(1).build();
        VehicleImpl vehicle = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance("0,0")).setType(type).build();

        final VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addJob(delivery).addJob(pickup).addVehicle(vehicle).build();

        VehicleRoute route = VehicleRoute.emptyRoute();
        route.setVehicleAndDepartureTime(vehicle, 0.0);

        Inserter inserter = new Inserter(new InsertionListeners(), vrp);
        inserter.insertJob(delivery, new InsertionData(0, 0, 0, vehicle, null), route);

        JobActivityFactory activityFactory = new JobActivityFactory() {
            @Override
            public List<AbstractActivity> createActivities(Job job) {
                return vrp.copyAndGetActivities(job);
            }
        };

        StateManager stateManager = new StateManager(vrp);
        stateManager.updateLoadStates();

        ConstraintManager constraintManager = new ConstraintManager(vrp, stateManager);
        constraintManager.addLoadConstraint();
        stateManager.informInsertionStarts(Arrays.asList(route), null);

        JobCalculatorSwitcher switcher = new JobCalculatorSwitcher();
        ServiceInsertionCalculator serviceInsertionCalc = new ServiceInsertionCalculator(routingCosts, activityCosts, activityInsertionCostsCalculator, constraintManager);
        serviceInsertionCalc.setJobActivityFactory(activityFactory);
        ShipmentInsertionCalculator insertionCalculator = new ShipmentInsertionCalculator(routingCosts, activityCosts, activityInsertionCostsCalculator, constraintManager);
        insertionCalculator.setJobActivityFactory(activityFactory);

        switcher.put(Pickup.class, serviceInsertionCalc);
        switcher.put(Delivery.class, serviceInsertionCalc);
        switcher.put(Shipment.class, insertionCalculator);

        InsertionData iData = switcher.getInsertionData(route, pickup, vehicle, 0, DriverImpl.noDriver(), Double.MAX_VALUE);

        assertEquals(1, iData.getDeliveryInsertionIndex());
    }

}
