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
import com.graphhopper.jsprit.core.problem.job.Delivery;
import com.graphhopper.jsprit.core.problem.job.Shipment;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.reporting.SolutionPrinter;
import com.graphhopper.jsprit.core.util.Solutions;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MaxTimeInVehicleTest {

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
        stateManager.addStateUpdater(new UpdateMaxTimeInVehicle(stateManager, id, vrp.getTransportCosts(), vrp.getActivityCosts(), openJobsId));

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
        stateManager.addStateUpdater(new UpdateMaxTimeInVehicle(stateManager, id, vrp.getTransportCosts(), vrp.getActivityCosts(), openJobsId));

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
        stateManager.addStateUpdater(new UpdateMaxTimeInVehicle(stateManager, id, vrp.getTransportCosts(), vrp.getActivityCosts(), openJobsId));

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
        stateManager.addStateUpdater(new UpdateMaxTimeInVehicle(stateManager, id, vrp.getTransportCosts(), vrp.getActivityCosts(), openJobsId));

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
        stateManager.addStateUpdater(new UpdateMaxTimeInVehicle(stateManager, id, vrp.getTransportCosts(), vrp.getActivityCosts(), openJobsId));

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
        stateManager.addStateUpdater(new UpdateMaxTimeInVehicle(stateManager, id, vrp.getTransportCosts(), vrp.getActivityCosts(), openJobsId));

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
        stateManager.addStateUpdater(new UpdateMaxTimeInVehicle(stateManager, id, vrp.getTransportCosts(), vrp.getActivityCosts(), openJobsId));

        ConstraintManager constraintManager = new ConstraintManager(vrp,stateManager);
        constraintManager.addConstraint(new MaxTimeInVehicleConstraint(vrp.getTransportCosts(), vrp.getActivityCosts(), id, stateManager, vrp, openJobsId), ConstraintManager.Priority.CRITICAL);

        VehicleRoutingAlgorithm vra = Jsprit.Builder.newInstance(vrp).setStateAndConstraintManager(stateManager,constraintManager).buildAlgorithm();
        VehicleRoutingProblemSolution solution = Solutions.bestOf(vra.searchSolutions());
        SolutionPrinter.print(vrp,solution, SolutionPrinter.Print.VERBOSE);
        assertEquals(1,solution.getRoutes().size());
        assertEquals(0,solution.getUnassignedJobs().size());

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
        stateManager.addStateUpdater(new UpdateMaxTimeInVehicle(stateManager, id, vrp.getTransportCosts(), vrp.getActivityCosts(), openJobsId));

        ConstraintManager constraintManager = new ConstraintManager(vrp,stateManager);
        constraintManager.addConstraint(new MaxTimeInVehicleConstraint(vrp.getTransportCosts(), vrp.getActivityCosts(), id, stateManager, vrp, openJobsId), ConstraintManager.Priority.CRITICAL);

        VehicleRoutingAlgorithm vra = Jsprit.Builder.newInstance(vrp).setStateAndConstraintManager(stateManager,constraintManager).buildAlgorithm();
        VehicleRoutingProblemSolution solution = Solutions.bestOf(vra.searchSolutions());
        SolutionPrinter.print(vrp,solution, SolutionPrinter.Print.VERBOSE);
        assertEquals(2,solution.getRoutes().size());
        assertEquals(0,solution.getUnassignedJobs().size());
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
        stateManager.addStateUpdater(new UpdateMaxTimeInVehicle(stateManager, id, vrp.getTransportCosts(), vrp.getActivityCosts(), openJobsId));

        ConstraintManager constraintManager = new ConstraintManager(vrp,stateManager);
        constraintManager.addConstraint(new MaxTimeInVehicleConstraint(vrp.getTransportCosts(), vrp.getActivityCosts(), id, stateManager, vrp, openJobsId), ConstraintManager.Priority.CRITICAL);

        VehicleRoutingAlgorithm vra = Jsprit.Builder.newInstance(vrp).setStateAndConstraintManager(stateManager,constraintManager).buildAlgorithm();
        VehicleRoutingProblemSolution solution = Solutions.bestOf(vra.searchSolutions());
        SolutionPrinter.print(vrp,solution, SolutionPrinter.Print.VERBOSE);

        assertEquals(1,solution.getRoutes().size());
        assertEquals(0,solution.getUnassignedJobs().size());
    }
}
