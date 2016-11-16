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
import com.graphhopper.jsprit.core.problem.JobActivityFactory;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.constraint.ConstraintManager;
import com.graphhopper.jsprit.core.problem.constraint.HardRouteConstraint;
import com.graphhopper.jsprit.core.problem.constraint.PickupAndDeliverShipmentLoadActivityLevelConstraint;
import com.graphhopper.jsprit.core.problem.constraint.ShipmentPickupsFirstConstraint;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingActivityCosts;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import com.graphhopper.jsprit.core.problem.driver.Driver;
import com.graphhopper.jsprit.core.problem.driver.DriverImpl;
import com.graphhopper.jsprit.core.problem.job.Pickup;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.job.Shipment;
import com.graphhopper.jsprit.core.problem.misc.JobInsertionContext;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.*;
import com.graphhopper.jsprit.core.problem.solution.route.state.RouteAndActivityStateGetter;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleType;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import com.graphhopper.jsprit.core.util.CostFactory;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class GeneralJobInsertionWithShipmentsTest {

    VehicleRoutingTransportCosts routingCosts;

    VehicleRoutingProblem vehicleRoutingProblem;

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

    HardRouteConstraint hardRouteLevelConstraint = new HardRouteConstraint() {

        @Override
        public boolean fulfilled(JobInsertionContext insertionContext) {
            return true;
        }

    };

    ActivityInsertionCostsCalculator activityInsertionCostsCalculator;

    GeneralJobInsertionCalculator insertionCalculator;

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
        insertionCalculator = new GeneralJobInsertionCalculator(routingCosts, activityCosts, activityInsertionCostsCalculator, constraintManager);
    }

    @Test
    public void whenCalculatingInsertionCostsOfShipment_itShouldReturnCorrectCostValue() {
        Shipment shipment = Shipment.Builder.newInstance("s").addSizeDimension(0, 1).setPickupLocation(Location.Builder.newInstance().setId("0,10").build()).setDeliveryLocation(Location.newInstance("10,0")).build();
        VehicleRoute route = VehicleRoute.emptyRoute();
        JobActivityFactory activityFactory = mock(JobActivityFactory.class);
        List<JobActivity> activities = new ArrayList<>();
        activities.add(new PickupShipmentDEPRECATED(shipment));
        activities.add(new DeliverShipmentDEPRECATED(shipment));
        when(activityFactory.createActivities(shipment)).thenReturn(activities);
        insertionCalculator.setJobActivityFactory(activityFactory);
        InsertionData iData = insertionCalculator.getInsertionData(route, shipment, vehicle, 0.0, null, Double.MAX_VALUE);
        assertEquals(40.0, iData.getInsertionCost(), 0.05);
    }

    @Test
    public void whenCalculatingInsertionIntoExistingRoute_itShouldReturnCorrectCosts() {
        Shipment shipment = Shipment.Builder.newInstance("s").addSizeDimension(0, 1).setPickupLocation(Location.Builder.newInstance().setId("0,10").build()).setDeliveryLocation(Location.newInstance("10,0")).build();
        Shipment shipment2 = Shipment.Builder.newInstance("s2").addSizeDimension(0, 1).setPickupLocation(Location.Builder.newInstance().setId("10,10").build()).setDeliveryLocation(Location.newInstance("0,0")).build();
        VehicleRoute route = VehicleRoute.emptyRoute();
        List<JobActivity> tourActivities = getTourActivities(shipment);
        route.setVehicleAndDepartureTime(vehicle,0);
        add(tourActivities,route,0,0);

        JobActivityFactory activityFactory = mock(JobActivityFactory.class);
        List<JobActivity> activities = new ArrayList<>();
        activities.add(new PickupShipmentDEPRECATED(shipment2));
        activities.add(new DeliverShipmentDEPRECATED(shipment2));
        when(activityFactory.createActivities(shipment2)).thenReturn(activities);
        insertionCalculator.setJobActivityFactory(activityFactory);

        InsertionData iData = insertionCalculator.getInsertionData(route, shipment2, vehicle, 0.0, null, Double.MAX_VALUE);
        assertEquals(0.0, iData.getInsertionCost(), 0.05);
        assertEquals(1, iData.getUnmodifiableEventsByType(InsertActivity.class).get(1).getIndex());
        assertEquals(2, iData.getUnmodifiableEventsByType(InsertActivity.class).get(0).getIndex());
    }

    private List<JobActivity> getTourActivities(Shipment shipment) {
        List<JobActivity> acts = new ArrayList<>();
        PickupShipmentDEPRECATED pick = new PickupShipmentDEPRECATED(shipment);
        DeliverShipmentDEPRECATED del = new DeliverShipmentDEPRECATED(shipment);
        acts.add(pick);
        acts.add(del);
        return acts;
    }

    @Test
    public void whenInsertingShipmentInRouteWithNotEnoughCapacity_itShouldReturnNoInsertion() {
        Shipment shipment = Shipment.Builder.newInstance("s").addSizeDimension(0, 1).setPickupLocation(Location.Builder.newInstance().setId("0,10").build()).setDeliveryLocation(Location.newInstance("10,0")).build();
        Shipment shipment2 = Shipment.Builder.newInstance("s2").addSizeDimension(0, 1).setPickupLocation(Location.Builder.newInstance().setId("10,10").build()).setDeliveryLocation(Location.newInstance("0,0")).build();
        VehicleRoute route = VehicleRoute.emptyRoute();
        List<JobActivity> tourActivities = getTourActivities(shipment);
        route.setVehicleAndDepartureTime(vehicle,0);
        add(tourActivities,route,0,0);

        createInsertionCalculator(insertionContext -> false);

        JobActivityFactory activityFactory = mock(JobActivityFactory.class);
        List<JobActivity> activities = new ArrayList<JobActivity>();
        activities.add(new PickupShipmentDEPRECATED(shipment2));
        activities.add(new DeliverShipmentDEPRECATED(shipment2));
        when(activityFactory.createActivities(shipment2)).thenReturn(activities);
        insertionCalculator.setJobActivityFactory(activityFactory);

        InsertionData iData = insertionCalculator.getInsertionData(route, shipment2, vehicle, 0.0, null, Double.MAX_VALUE);
        assertEquals(InsertionData.createEmptyInsertionData(), iData);

    }


    @Test
    public void whenInsertingThirdShipment_itShouldCalcCorrectVal() {
        Shipment shipment = Shipment.Builder.newInstance("s").addSizeDimension(0, 1).setPickupLocation(Location.Builder.newInstance().setId("0,10").build()).setDeliveryLocation(Location.newInstance("10,0")).build();
        Shipment shipment2 = Shipment.Builder.newInstance("s2").addSizeDimension(0, 1).setPickupLocation(Location.Builder.newInstance().setId("10,10").build()).setDeliveryLocation(Location.newInstance("0,0")).build();
        Shipment shipment3 = Shipment.Builder.newInstance("s3").addSizeDimension(0, 1).setPickupLocation(Location.Builder.newInstance().setId("0,0").build()).setDeliveryLocation(Location.newInstance("9,10")).build();

        VehicleRoute route = VehicleRoute.emptyRoute();
        List<JobActivity> shipmentActivities = getTourActivities(shipment);
        List<JobActivity> shipment2Activities = getTourActivities(shipment2);

        route.setVehicleAndDepartureTime(vehicle,0d);
        add(shipmentActivities,route,0,0);
        add(shipment2Activities,route,1,2);

        JobActivityFactory activityFactory = mock(JobActivityFactory.class);
        List<JobActivity> activities = new ArrayList<>();
        activities.add(new PickupShipmentDEPRECATED(shipment3));
        activities.add(new DeliverShipmentDEPRECATED(shipment3));
        when(activityFactory.createActivities(shipment3)).thenReturn(activities);
        insertionCalculator.setJobActivityFactory(activityFactory);

        InsertionData iData = insertionCalculator.getInsertionData(route, shipment3, vehicle, 0.0, null, Double.MAX_VALUE);
        assertEquals(0.0, iData.getInsertionCost(), 0.05);
        List<InsertActivity> unmodifiableEventsByType = iData.getUnmodifiableEventsByType(InsertActivity.class);
        assertEquals(1, unmodifiableEventsByType.get(0).getIndex());
        assertEquals(0, unmodifiableEventsByType.get(1).getIndex());
    }

    @Test
    public void whenInsertingThirdShipment_itShouldCalcCorrectVal2() {
        Shipment shipment = Shipment.Builder.newInstance("s").addSizeDimension(0, 1).setPickupLocation(Location.Builder.newInstance().setId("0,10").build()).setDeliveryLocation(Location.newInstance("10,0")).build();
        Shipment shipment2 = Shipment.Builder.newInstance("s2").addSizeDimension(0, 1).setPickupLocation(Location.Builder.newInstance().setId("10,10").build()).setDeliveryLocation(Location.newInstance("0,0")).build();
        Shipment shipment3 = Shipment.Builder.newInstance("s3").addSizeDimension(0, 1).setPickupLocation(Location.Builder.newInstance().setId("0,0").build()).setDeliveryLocation(Location.newInstance("9,9")).build();
        List<JobActivity> shipmentActivities = getTourActivities(shipment);
        List<JobActivity> shipment2Activities = getTourActivities(shipment2);
        VehicleRoute route = VehicleRoute.emptyRoute();

        route.setVehicleAndDepartureTime(vehicle,0d);
        add(shipmentActivities,route,0,0);
        add(shipment2Activities,route,1,2);

        JobActivityFactory activityFactory = mock(JobActivityFactory.class);
        List<JobActivity> activities = new ArrayList<>();
        activities.add(new PickupShipmentDEPRECATED(shipment3));
        activities.add(new DeliverShipmentDEPRECATED(shipment3));
        when(activityFactory.createActivities(shipment3)).thenReturn(activities);
        insertionCalculator.setJobActivityFactory(activityFactory);


        InsertionData iData = insertionCalculator.getInsertionData(route, shipment3, vehicle, 0.0, null, Double.MAX_VALUE);
        assertEquals(2.0, iData.getInsertionCost(), 0.05);
        assertEquals(0, iData.getUnmodifiableEventsByType(InsertActivity.class).get(1).getIndex());
        assertEquals(1, iData.getUnmodifiableEventsByType(InsertActivity.class).get(0).getIndex());
    }

    @Test
    public void whenInstertingShipmentWithLoadConstraintWhereCapIsNotSufficient_capConstraintsAreFulfilled() {
        Shipment shipment = Shipment.Builder.newInstance("s").addSizeDimension(0, 1).setPickupLocation(Location.Builder.newInstance().setId("0,10").build()).setDeliveryLocation(Location.newInstance("10,0")).build();
        Shipment shipment2 = Shipment.Builder.newInstance("s2").addSizeDimension(0, 1).setPickupLocation(Location.Builder.newInstance().setId("10,10").build()).setDeliveryLocation(Location.newInstance("0,0")).build();
        Shipment shipment3 = Shipment.Builder.newInstance("s3").addSizeDimension(0, 1).setPickupLocation(Location.Builder.newInstance().setId("0,0").build()).setDeliveryLocation(Location.newInstance("9,9")).build();

        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        VehicleRoutingProblem vrp = vrpBuilder.addJob(shipment).addJob(shipment2).addJob(shipment3).build();

        VehicleRoute route = VehicleRoute.emptyRoute();
        route.setVehicleAndDepartureTime(vehicle, 0.0);

        add(vrp,route,shipment,0,0);
        add(vrp,route,shipment2,1,2);

        StateManager stateManager = new StateManager(vrp);
        stateManager.updateLoadStates();
        stateManager.informInsertionStarts(Arrays.asList(route), null);

        ConstraintManager constraintManager = new ConstraintManager(vrp, stateManager);
        constraintManager.addConstraint(new PickupAndDeliverShipmentLoadActivityLevelConstraint(stateManager), ConstraintManager.Priority.CRITICAL);
        constraintManager.addConstraint(new ShipmentPickupsFirstConstraint(), ConstraintManager.Priority.CRITICAL);

        insertionCalculator = new GeneralJobInsertionCalculator(routingCosts, activityCosts, activityInsertionCostsCalculator, constraintManager);
        insertionCalculator.setJobActivityFactory(vrp.getJobActivityFactory());

        InsertionData iData = insertionCalculator.getInsertionData(route, shipment3, vehicle, 0.0, DriverImpl.noDriver(), Double.MAX_VALUE);
        assertTrue(iData instanceof InsertionData.NoInsertionFound);

    }

    @Test
    public void whenInsertingServiceWhileNoCapIsAvailable_itMustReturnNoInsertionData() {
        Shipment shipment = Shipment.Builder.newInstance("s").addSizeDimension(0, 1).setPickupLocation(Location.Builder.newInstance().setId("0,10").build()).setDeliveryLocation(Location.newInstance("0,0")).build();
        Shipment shipment2 = Shipment.Builder.newInstance("s2").addSizeDimension(0, 1).setPickupLocation(Location.Builder.newInstance().setId("10,10").build()).setDeliveryLocation(Location.newInstance("0,0")).build();

        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        VehicleRoutingProblem vrp = vrpBuilder.addJob(shipment).addJob(shipment2).build();

        VehicleRoute route = VehicleRoute.emptyRoute();
        route.setVehicleAndDepartureTime(vehicle, 0.0);

        add(vrp,route,shipment,0,0);
        add(vrp,route,shipment2,1,2);

        StateManager stateManager = new StateManager(vrp);
        stateManager.updateLoadStates();
        stateManager.informInsertionStarts(Arrays.asList(route), null);

        ConstraintManager constraintManager = new ConstraintManager(vrp, stateManager);
        constraintManager.addLoadConstraint();

        insertionCalculator = new GeneralJobInsertionCalculator(routingCosts, activityCosts, activityInsertionCostsCalculator, constraintManager);
        stateManager.informInsertionStarts(Arrays.asList(route), null);

        //		Service service = new Service.Builder("pick", 1).setLocationId("5,5").build();
        Pickup service = new Pickup.Builder("pick").addSizeDimension(0, 1).setLocation(Location.newInstance("5,5")).build();

        JobActivityFactory activityFactory = mock(JobActivityFactory.class);
        List<JobActivity> activities = new ArrayList<>();
        activities.add(new PickupServiceDEPRECATED(service));
        when(activityFactory.createActivities(service)).thenReturn(activities);
        insertionCalculator.setJobActivityFactory(activityFactory);

        InsertionData iData = insertionCalculator.getInsertionData(route, service, vehicle, 0, DriverImpl.noDriver(), Double.MAX_VALUE);
        //		routeActVisitor.visit(route);

        assertEquals(3, iData.getUnmodifiableEventsByType(InsertActivity.class).get(0).getIndex());
    }

    private void add(VehicleRoutingProblem vrp, VehicleRoute route, Shipment shipment, int pickI, int delI) {
        List<JobActivity> shipmentActivities = vrp.copyAndGetActivities(shipment);
        route.getTourActivities().addActivity(delI, shipmentActivities.get(1));
        route.getTourActivities().addActivity(pickI, shipmentActivities.get(0));
    }

    private void add(List<JobActivity> shipmentActivities, VehicleRoute route, int pickI, int delI) {
        route.getTourActivities().addActivity(delI, shipmentActivities.get(1));
        route.getTourActivities().addActivity(pickI, shipmentActivities.get(0));
    }


}
