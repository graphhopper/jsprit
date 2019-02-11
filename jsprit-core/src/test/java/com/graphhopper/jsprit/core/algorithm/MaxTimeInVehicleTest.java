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

package com.graphhopper.jsprit.core.algorithm;
import com.graphhopper.jsprit.core.algorithm.box.Jsprit;
import com.graphhopper.jsprit.core.algorithm.state.StateId;
import com.graphhopper.jsprit.core.algorithm.state.StateManager;
import com.graphhopper.jsprit.core.algorithm.state.UpdateMaxTimeInVehicle;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.constraint.ConstraintManager;
import com.graphhopper.jsprit.core.problem.constraint.MaxTimeInVehicleConstraint;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import com.graphhopper.jsprit.core.problem.driver.Driver;
import com.graphhopper.jsprit.core.problem.job.Delivery;
import com.graphhopper.jsprit.core.problem.job.Shipment;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import com.graphhopper.jsprit.core.reporting.SolutionPrinter;
import com.graphhopper.jsprit.core.util.Solutions;
import org.junit.Test;

import java.util.Iterator;
import java.util.Random;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MaxTimeInVehicleTest {

    Random RANDOM = new Random();

    @Test
    public void testShipment(){
        Shipment s1 = Shipment.Builder.newInstance("s1").setPickupLocation(Location.newInstance(34.773586,32.079754)).setDeliveryLocation(Location.newInstance(34.781247,38.294571))
            .setDeliveryServiceTime(10)
            .setMaxTimeInVehicle(20)
            .build();

        VehicleImpl v1 = VehicleImpl.Builder.newInstance("v1").setStartLocation(Location.newInstance(34.771200,32.067646)).setEndLocation(Location.newInstance(34.768404,32.081525)).build();
        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addVehicle(v1).addJob(s1).build();

        StateManager stateManager = new StateManager(vrp);
        StateId id = stateManager.createStateId("max-time");
        StateId openJobsId = stateManager.createStateId("open-jobs-id");
        stateManager.addStateUpdater(new UpdateMaxTimeInVehicle(stateManager, id, vrp.getVehicles(), vrp.getTransportCosts(), vrp.getActivityCosts(), openJobsId));

        ConstraintManager constraintManager = new ConstraintManager(vrp,stateManager);
        constraintManager.addConstraint(new MaxTimeInVehicleConstraint(vrp.getTransportCosts(), vrp.getActivityCosts(), id, stateManager, vrp, openJobsId), ConstraintManager.Priority.CRITICAL);

        VehicleRoutingAlgorithm vra = Jsprit.Builder.newInstance(vrp).setStateAndConstraintManager(stateManager,constraintManager).buildAlgorithm();
        VehicleRoutingProblemSolution solution = Solutions.bestOf(vra.searchSolutions());
        SolutionPrinter.print(vrp,solution, SolutionPrinter.Print.VERBOSE);
        assertEquals(0,solution.getUnassignedJobs().size());
        assertEquals(1,solution.getRoutes().size());
    }

    @Test
    public void testShipmentUnassigned(){
        Shipment s1 = Shipment.Builder.newInstance("s1").setPickupLocation(Location.newInstance(34.773586,32.079754)).setDeliveryLocation(Location.newInstance(34.781247,38.294571))
            .setDeliveryServiceTime(10)
            .setMaxTimeInVehicle(4)
            .build();

        VehicleImpl v1 = VehicleImpl.Builder.newInstance("v1").setStartLocation(Location.newInstance(34.771200,32.067646)).setEndLocation(Location.newInstance(34.768404,32.081525)).build();
        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addVehicle(v1).addJob(s1).build();


        StateManager stateManager = new StateManager(vrp);
        StateId id = stateManager.createStateId("max-time");
        StateId openJobsId = stateManager.createStateId("open-jobs-id");
        stateManager.addStateUpdater(new UpdateMaxTimeInVehicle(stateManager, id, vrp.getVehicles(), vrp.getTransportCosts(), vrp.getActivityCosts(), openJobsId));

        ConstraintManager constraintManager = new ConstraintManager(vrp,stateManager);
        constraintManager.addConstraint(new MaxTimeInVehicleConstraint(vrp.getTransportCosts(), vrp.getActivityCosts(), id, stateManager, vrp, openJobsId), ConstraintManager.Priority.CRITICAL);

        VehicleRoutingAlgorithm vra = Jsprit.Builder.newInstance(vrp).setStateAndConstraintManager(stateManager,constraintManager).buildAlgorithm();
        VehicleRoutingProblemSolution solution = Solutions.bestOf(vra.searchSolutions());
        SolutionPrinter.print(vrp,solution, SolutionPrinter.Print.VERBOSE);

        assertEquals(1,solution.getUnassignedJobs().size());
        assertEquals(0,solution.getRoutes().size());

    }

    @Test
    public void testDelivery(){

        Delivery d2 = Delivery.Builder.newInstance("d2")
            .setMaxTimeInVehicle(10)
            .setLocation(Location.newInstance(10, 5)).setServiceTime(2).build();

        VehicleImpl v1 = VehicleImpl.Builder.newInstance("v1").setStartLocation(Location.newInstance(2,3)).setEndLocation(Location.newInstance(0,0)).build();
        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addVehicle(v1).addJob(d2).build();


        StateManager stateManager = new StateManager(vrp);
        StateId id = stateManager.createStateId("max-time");
        StateId openJobsId = stateManager.createStateId("open-jobs-id");
        stateManager.addStateUpdater(new UpdateMaxTimeInVehicle(stateManager, id, vrp.getVehicles(), vrp.getTransportCosts(), vrp.getActivityCosts(), openJobsId));

        ConstraintManager constraintManager = new ConstraintManager(vrp,stateManager);
        constraintManager.addConstraint(new MaxTimeInVehicleConstraint(vrp.getTransportCosts(), vrp.getActivityCosts(), id, stateManager, vrp, openJobsId), ConstraintManager.Priority.CRITICAL);

        VehicleRoutingAlgorithm vra = Jsprit.Builder.newInstance(vrp).setStateAndConstraintManager(stateManager,constraintManager).buildAlgorithm();
        VehicleRoutingProblemSolution solution = Solutions.bestOf(vra.searchSolutions());
        SolutionPrinter.print(vrp,solution, SolutionPrinter.Print.VERBOSE);

        assertEquals(0,solution.getUnassignedJobs().size());
        assertEquals(1,solution.getRoutes().size());

    }

    @Test
    public void testDeliveryUnassigned(){
        Delivery d2 = Delivery.Builder.newInstance("d2")
            .setMaxTimeInVehicle(4)
            .setLocation(Location.newInstance(10, 5)).setServiceTime(2).build();

        VehicleImpl v1 = VehicleImpl.Builder.newInstance("v1").setStartLocation(Location.newInstance(2,3)).setEndLocation(Location.newInstance(0,0)).build();
        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addVehicle(v1).addJob(d2).build();

        StateManager stateManager = new StateManager(vrp);
        StateId id = stateManager.createStateId("max-time");
        StateId openJobsId = stateManager.createStateId("open-jobs-id");
        stateManager.addStateUpdater(new UpdateMaxTimeInVehicle(stateManager, id, vrp.getVehicles(), vrp.getTransportCosts(), vrp.getActivityCosts(), openJobsId));

        ConstraintManager constraintManager = new ConstraintManager(vrp,stateManager);
        constraintManager.addConstraint(new MaxTimeInVehicleConstraint(vrp.getTransportCosts(), vrp.getActivityCosts(), id, stateManager, vrp, openJobsId), ConstraintManager.Priority.CRITICAL);

        VehicleRoutingAlgorithm vra = Jsprit.Builder.newInstance(vrp).setStateAndConstraintManager(stateManager,constraintManager).buildAlgorithm();
        VehicleRoutingProblemSolution solution = Solutions.bestOf(vra.searchSolutions());
        SolutionPrinter.print(vrp,solution, SolutionPrinter.Print.VERBOSE);

        assertEquals(1,solution.getUnassignedJobs().size());
        assertEquals(0,solution.getRoutes().size());

    }

    @Test
    public void testPickUpDropOffTwoDriversSameLocation(){
        Shipment s1 = Shipment.Builder.newInstance("s1").setPickupLocation(Location.newInstance(34.773586,32.079754))
            .setDeliveryLocation(Location.newInstance(34.781247,38.294571))
            .setDeliveryServiceTime(10)
            .setMaxTimeInVehicle(10)
            .build();

        VehicleImpl v1 = VehicleImpl.Builder.newInstance("v1")
            .setStartLocation(Location.newInstance(34.771200,32.067646))
            .setEndLocation(Location.newInstance(34.768404,32.081525)).build();

        VehicleImpl v2 = VehicleImpl.Builder.newInstance("v2")
            .setStartLocation(Location.newInstance(34.771200,32.067646))
            .setEndLocation(Location.newInstance(34.768404,32.081525)).build();
        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addVehicle(v1).addVehicle(v2).addJob(s1).build();

        StateManager stateManager = new StateManager(vrp);
        StateId id = stateManager.createStateId("max-time");
        StateId openJobsId = stateManager.createStateId("open-jobs-id");
        stateManager.addStateUpdater(new UpdateMaxTimeInVehicle(stateManager, id, vrp.getVehicles(), vrp.getTransportCosts(), vrp.getActivityCosts(), openJobsId));

        ConstraintManager constraintManager = new ConstraintManager(vrp,stateManager);
        constraintManager.addConstraint(new MaxTimeInVehicleConstraint(vrp.getTransportCosts(), vrp.getActivityCosts(), id, stateManager, vrp, openJobsId), ConstraintManager.Priority.CRITICAL);

        VehicleRoutingAlgorithm vra = Jsprit.Builder.newInstance(vrp).setStateAndConstraintManager(stateManager,constraintManager).buildAlgorithm();
        VehicleRoutingProblemSolution solution = Solutions.bestOf(vra.searchSolutions());
        SolutionPrinter.print(vrp,solution, SolutionPrinter.Print.VERBOSE);
        assertEquals(0,solution.getUnassignedJobs().size());

    }

    @Test
    public void testPickUpDropOffTwoDriversDiffrentLocation(){
        Shipment s1 = Shipment.Builder.newInstance("s1").setPickupLocation(Location.newInstance(34.773586,32.079754))
            .setDeliveryLocation(Location.newInstance(34.781247,38.294571))
            .setDeliveryServiceTime(10)
            .setMaxTimeInVehicle(10)
            .build();

        VehicleImpl v1 = VehicleImpl.Builder.newInstance("v1")
            .setStartLocation(Location.newInstance(34.771200,32.067646))
            .setEndLocation(Location.newInstance(34.768404,32.081525)).build();

        VehicleImpl v2 = VehicleImpl.Builder.newInstance("v2")
            .setStartLocation(Location.newInstance(34.771200,32.067646))
            .setEndLocation(Location.newInstance(34.5555,32.081324)).build();
        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addVehicle(v1).addVehicle(v2).addJob(s1).build();

        StateManager stateManager = new StateManager(vrp);
        StateId id = stateManager.createStateId("max-time");
        StateId openJobsId = stateManager.createStateId("open-jobs-id");
        stateManager.addStateUpdater(new UpdateMaxTimeInVehicle(stateManager, id, vrp.getVehicles(), vrp.getTransportCosts(), vrp.getActivityCosts(), openJobsId));

        ConstraintManager constraintManager = new ConstraintManager(vrp,stateManager);
        constraintManager.addConstraint(new MaxTimeInVehicleConstraint(vrp.getTransportCosts(), vrp.getActivityCosts(), id, stateManager, vrp, openJobsId), ConstraintManager.Priority.CRITICAL);

        VehicleRoutingAlgorithm vra = Jsprit.Builder.newInstance(vrp).setStateAndConstraintManager(stateManager,constraintManager).buildAlgorithm();
        VehicleRoutingProblemSolution solution = Solutions.bestOf(vra.searchSolutions());
        SolutionPrinter.print(vrp,solution, SolutionPrinter.Print.VERBOSE);

        assertEquals(0,solution.getUnassignedJobs().size());
    }

    @Test
    public void testRouteTwoDriversDiffrentLocationOneRoute(){
        Shipment s1 = Shipment.Builder.newInstance("s1").setPickupLocation(Location.newInstance(8,0))
            .setDeliveryLocation(Location.newInstance(10,0))
            .setDeliveryServiceTime(2)
            .setMaxTimeInVehicle(10)
            .build();

        Delivery d2 = Delivery.Builder.newInstance("d2")
            .setMaxTimeInVehicle(14)
            .setLocation(Location.newInstance(10, 5)).setServiceTime(2).build();

        final VehicleTypeImpl type = VehicleTypeImpl.Builder.newInstance("1").setFixedCost(1).build();
        VehicleImpl v1 = VehicleImpl.Builder.newInstance("v1")
            .setType(type)
            .setStartLocation(Location.newInstance(8,5)).setReturnToDepot(true).build();

        VehicleImpl v2 = VehicleImpl.Builder.newInstance("v2")
            .setType(type)
            .setStartLocation(Location.newInstance(5,0)).setReturnToDepot(true).build();

        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance()
            .addVehicle(v1)
            .addVehicle(v2)
            .addJob(s1)
            .addJob(d2)
            .build();

        StateManager stateManager = new StateManager(vrp);
        StateId id = stateManager.createStateId("max-time");
        StateId openJobsId = stateManager.createStateId("open-jobs-id");
        stateManager.addStateUpdater(new UpdateMaxTimeInVehicle(stateManager, id, vrp.getVehicles(), vrp.getTransportCosts(), vrp.getActivityCosts(), openJobsId));

        ConstraintManager constraintManager = new ConstraintManager(vrp,stateManager);
        constraintManager.addConstraint(new MaxTimeInVehicleConstraint(vrp.getTransportCosts(), vrp.getActivityCosts(), id, stateManager, vrp, openJobsId), ConstraintManager.Priority.CRITICAL);

        VehicleRoutingAlgorithm vra = Jsprit.Builder.newInstance(vrp).setStateAndConstraintManager(stateManager,constraintManager).buildAlgorithm();
        VehicleRoutingProblemSolution solution = Solutions.bestOf(vra.searchSolutions());
        SolutionPrinter.print(vrp, solution, SolutionPrinter.Print.VERBOSE);
        assertEquals(1, solution.getRoutes().size());
        assertEquals(0, solution.getUnassignedJobs().size());

    }



    @Test
    public void testRouteTwoDriversTwoRouts(){
        Shipment s1 = Shipment.Builder.newInstance("s1").setPickupLocation(Location.newInstance(8,0))
            .setDeliveryLocation(Location.newInstance(10,0))
            .setDeliveryServiceTime(9)
            .setMaxTimeInVehicle(10)
            .build();

        Delivery d2 = Delivery.Builder.newInstance("d2")
            .setMaxTimeInVehicle(3)
            .setLocation(Location.newInstance(10, 5)).setServiceTime(10).build();

        VehicleImpl v1 = VehicleImpl.Builder.newInstance("v1")
            .setStartLocation(Location.newInstance(8,5)).setReturnToDepot(true).build();

        VehicleImpl v2 = VehicleImpl.Builder.newInstance("v2")
            .setStartLocation(Location.newInstance(5,0)).setReturnToDepot(true).build();

        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance()
            .addVehicle(v1)
            .addVehicle(v2)
            .addJob(s1)
            .addJob(d2)
            .build();

        StateManager stateManager = new StateManager(vrp);
        StateId id = stateManager.createStateId("max-time");
        StateId openJobsId = stateManager.createStateId("open-jobs-id");
        stateManager.addStateUpdater(new UpdateMaxTimeInVehicle(stateManager, id, vrp.getVehicles(), vrp.getTransportCosts(), vrp.getActivityCosts(), openJobsId));

        ConstraintManager constraintManager = new ConstraintManager(vrp,stateManager);
        constraintManager.addConstraint(new MaxTimeInVehicleConstraint(vrp.getTransportCosts(), vrp.getActivityCosts(), id, stateManager, vrp, openJobsId), ConstraintManager.Priority.CRITICAL);

        VehicleRoutingAlgorithm vra = Jsprit.Builder.newInstance(vrp).setStateAndConstraintManager(stateManager,constraintManager).buildAlgorithm();
        VehicleRoutingProblemSolution solution = Solutions.bestOf(vra.searchSolutions());
        SolutionPrinter.print(vrp,solution, SolutionPrinter.Print.VERBOSE);
        assertEquals(2,solution.getRoutes().size());
        assertEquals(0,solution.getUnassignedJobs().size());
    }

    @Test
    public void testRouteTwoDriversTwoRouts_NotAllAssigned() {
        int numJobs = Math.abs(RANDOM.nextInt(100)) + 10,
            start = Math.abs(RANDOM.nextInt(100)),
            maxTimeInVehicle = 50 + Math.abs(RANDOM.nextInt(50)),
            serviceTime = Math.abs(RANDOM.nextInt(10)) + 5,
            end = start + numJobs * maxTimeInVehicle + numJobs * serviceTime + Math.abs(RANDOM.nextInt(200));

        final VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
        for (int i = 0; i < 5; ++i) {
            builder.addVehicle(VehicleImpl.Builder.newInstance(UUID.randomUUID().toString())
                .setStartLocation(Location.newInstance(8, 5)).setReturnToDepot(true)
                .setEarliestStart(start).setLatestArrival(end)
                .setType(VehicleTypeImpl.Builder.newInstance(UUID.randomUUID().toString()).addCapacityDimension(0, numJobs).setCostPerDistance(1).setFixedCost(100).build())
                .build());
        }

        for (int i = 0; i < numJobs; ++i) {
            builder.addJob(Delivery.Builder.newInstance("d" + i)
                .setMaxTimeInVehicle(maxTimeInVehicle)
                .addTimeWindow(TimeWindow.newInstance(start, end))
                .addSizeDimension(0, 1)
                .setLocation(Location.newInstance(RANDOM.nextDouble(), RANDOM.nextDouble())).setServiceTime(serviceTime).build());
        }

        VehicleRoutingProblemSolution solution = getVehicleRoutingProblemSolution(builder, true, 4);

        assertFalse(solution.getRoutes().isEmpty());
        final Iterator<VehicleRoute> iterator = solution.getRoutes().iterator();
        while (iterator.hasNext()) {
            final VehicleRoute route = iterator.next();
            final TourActivity lastActivity = route.getActivities().get(route.getActivities().size() - 1);
            assertTrue(lastActivity.getArrTime() - route.getStart().getEndTime() <= maxTimeInVehicle);
        }
    }

    @Test
    public void testRouteTwoDriversTwoRoutsAllAssignedToCheapestDrivers() {
        int numJobs = 100,
            start = 0,
            maxTimeInVehicle = 50,
            serviceTime = 4,
            end = 360;

        //in route 50/(4+1)=10 tasks

        final VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
        final VehicleTypeImpl cheap = VehicleTypeImpl.Builder.newInstance(UUID.randomUUID().toString()).addCapacityDimension(0, numJobs).setCostPerDistance(1).setFixedCost(10).build();
        for (int i = 0; i < 10; ++i) {
            builder.addVehicle(VehicleImpl.Builder.newInstance(UUID.randomUUID().toString())
                .setStartLocation(Location.newInstance(8, 5)).setReturnToDepot(true)
                .setEarliestStart(start).setLatestArrival(end)
                .setType(cheap)
                .build());
        }


        final VehicleTypeImpl expensive = VehicleTypeImpl.Builder.newInstance(UUID.randomUUID().toString()).addCapacityDimension(0, numJobs).setCostPerDistance(1).setFixedCost(100).build();
        for (int i = 0; i < 10; ++i) {
            builder.addVehicle(VehicleImpl.Builder.newInstance(UUID.randomUUID().toString())
                .setStartLocation(Location.newInstance(8, 5)).setReturnToDepot(true)
                .setEarliestStart(start).setLatestArrival(end)
                .setType(expensive)
                .build());
        }

        for (int i = 0; i < numJobs; ++i) {
            builder.addJob(Delivery.Builder.newInstance("d" + i)
                .setMaxTimeInVehicle(maxTimeInVehicle)
                .addTimeWindow(TimeWindow.newInstance(start, end))
                .addSizeDimension(0, 1)
                .setLocation(Location.newInstance(RANDOM.nextDouble(), RANDOM.nextDouble())).setServiceTime(serviceTime).build());
        }

        VehicleRoutingProblemSolution solution = getVehicleRoutingProblemSolution(builder, true, 4);

        final Iterator<VehicleRoute> iterator = solution.getRoutes().iterator();
        while (iterator.hasNext()) {
            final VehicleRoute route = iterator.next();
            final TourActivity lastActivity = route.getActivities().get(route.getActivities().size() - 1);
            assertTrue(lastActivity.getArrTime() - route.getStart().getEndTime() <= maxTimeInVehicle);
            assertEquals(route.getVehicle().getType(), cheap);
        }

        assertTrue(solution.getUnassignedJobs().isEmpty());
    }

    @Test
    public void testLowMaxTimeCauseTwoRoutes(){
        Shipment s1 = Shipment.Builder.newInstance("s1").setPickupLocation(Location.newInstance(8,0))
            .setDeliveryLocation(Location.newInstance(10,0))
            .setDeliveryServiceTime(2)
            .setMaxTimeInVehicle(10)
            .build();

        Delivery d2 = Delivery.Builder.newInstance("d2")
            .setMaxTimeInVehicle(13)
            .setLocation(Location.newInstance(10, 5)).setServiceTime(2).build();

        VehicleImpl v1 = VehicleImpl.Builder.newInstance("v1")
            .setStartLocation(Location.newInstance(8,5)).setReturnToDepot(true).build();

        VehicleImpl v2 = VehicleImpl.Builder.newInstance("v2")
            .setStartLocation(Location.newInstance(5,0)).setReturnToDepot(true).build();


        final VehicleRoutingProblemSolution solution = getVehicleRoutingProblemSolution(VehicleRoutingProblem.Builder.newInstance()
            .addVehicle(v1)
            .addVehicle(v2)
            .addJob(s1)
            .addJob(d2), false, 4);
        assertEquals(2, solution.getRoutes().size());
        assertEquals(0, solution.getUnassignedJobs().size());

    }

    private VehicleRoutingProblemSolution getVehicleRoutingProblemSolution(VehicleRoutingProblem.Builder builder, boolean routeCost, int numThreads) {
        if (routeCost) {
            builder.setRoutingCost(new VehicleRoutingTransportCosts() {
                @Override
                public double getBackwardTransportCost(Location location, Location location1, double v, Driver driver, Vehicle vehicle) {
                    return 1;
                }

                @Override
                public double getBackwardTransportTime(Location location, Location location1, double v, Driver driver, Vehicle vehicle) {
                    return 1;
                }

                @Override
                public double getTransportCost(Location location, Location location1, double v, Driver driver, Vehicle vehicle) {
                    return 1;
                }

                @Override
                public double getTransportTime(Location location, Location location1, double v, Driver driver, Vehicle vehicle) {
                    return 1;
                }

                @Override
                public double getDistance(Location location, Location location1, double v, Vehicle vehicle) {
                    return 1;
                }
            });
        }
        VehicleRoutingProblem vrp = builder
            .setFleetSize(VehicleRoutingProblem.FleetSize.FINITE)
            .build();

        StateManager stateManager = new StateManager(vrp);
        StateId id = stateManager.createStateId("max-time");
        StateId openJobsId = stateManager.createStateId("open-jobs-id");
        stateManager.addStateUpdater(new UpdateMaxTimeInVehicle(stateManager, id, vrp.getVehicles(), vrp.getTransportCosts(), vrp.getActivityCosts(), openJobsId));

        ConstraintManager constraintManager = new ConstraintManager(vrp,stateManager);
        constraintManager.addConstraint(new MaxTimeInVehicleConstraint(vrp.getTransportCosts(), vrp.getActivityCosts(), id, stateManager, vrp, openJobsId), ConstraintManager.Priority.CRITICAL);

        VehicleRoutingAlgorithm vra = Jsprit.Builder.newInstance(vrp).setStateAndConstraintManager(stateManager,constraintManager).setProperty(Jsprit.Parameter.THREADS, String.valueOf(numThreads)).buildAlgorithm();
        vra.setMaxIterations(100);
        VehicleRoutingProblemSolution solution = Solutions.bestOf(vra.searchSolutions());
        SolutionPrinter.print(vrp,solution, SolutionPrinter.Print.VERBOSE);
        return solution;
    }

    @Test
    public void testRoute(){
        Shipment s1 = Shipment.Builder.newInstance("s1").setPickupLocation(Location.newInstance(5,0))
            .setDeliveryLocation(Location.newInstance(10,0))
            .setMaxTimeInVehicle(5)
            .build();

        Shipment s2 = Shipment.Builder.newInstance("s2").setPickupLocation(Location.newInstance(6,5))
            .setDeliveryLocation(Location.newInstance(12,0))
            .setMaxTimeInVehicle(10)
            .build();

        Shipment s3 = Shipment.Builder.newInstance("s3").setPickupLocation(Location.newInstance(3,2))
            .setDeliveryLocation(Location.newInstance(3,5))
            .setMaxTimeInVehicle(10)
            .build();


        VehicleImpl v1 = VehicleImpl.Builder.newInstance("v1")
            .setStartLocation(Location.newInstance(0,0)).setReturnToDepot(true).build();

        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance()
            .addVehicle(v1)
            .addJob(s1)
            .addJob(s2)
            .addJob(s3)
            .build();

        StateManager stateManager = new StateManager(vrp);
        StateId id = stateManager.createStateId("max-time");
        StateId openJobsId = stateManager.createStateId("open-jobs-id");
        stateManager.addStateUpdater(new UpdateMaxTimeInVehicle(stateManager, id, vrp.getVehicles(), vrp.getTransportCosts(), vrp.getActivityCosts(), openJobsId));

        ConstraintManager constraintManager = new ConstraintManager(vrp,stateManager);
        constraintManager.addConstraint(new MaxTimeInVehicleConstraint(vrp.getTransportCosts(), vrp.getActivityCosts(), id, stateManager, vrp, openJobsId), ConstraintManager.Priority.CRITICAL);

        VehicleRoutingAlgorithm vra = Jsprit.Builder.newInstance(vrp).setStateAndConstraintManager(stateManager,constraintManager).buildAlgorithm();
        VehicleRoutingProblemSolution solution = Solutions.bestOf(vra.searchSolutions());
        SolutionPrinter.print(vrp,solution, SolutionPrinter.Print.VERBOSE);

        assertEquals(1,solution.getRoutes().size());
        assertEquals(0,solution.getUnassignedJobs().size());
    }
}
