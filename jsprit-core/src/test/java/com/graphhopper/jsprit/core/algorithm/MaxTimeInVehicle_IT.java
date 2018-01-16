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
import com.graphhopper.jsprit.core.problem.job.Shipment;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.reporting.SolutionPrinter;
import com.graphhopper.jsprit.core.util.Solutions;
import org.junit.Test;

/**
 * Created by schroeder on 20/09/16.
 */
public class MaxTimeInVehicle_IT {

    @Test
    public void test(){

        Shipment s1 = Shipment.Builder.newInstance("s1").setPickupLocation(Location.newInstance(0,0)).setDeliveryLocation(Location.newInstance(100,0)).setDeliveryServiceTime(10)
            .setMaxTimeInVehicle(100d)
            .build();
        Shipment s2 = Shipment.Builder.newInstance("s2").setPickupLocation(Location.newInstance(0,0)).setDeliveryLocation(Location.newInstance(100,0)).setDeliveryServiceTime(10)
            .setMaxTimeInVehicle(100d)
            .build();

        VehicleImpl v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance(0,0)).build();

        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addVehicle(v).addJob(s1).addJob(s2).build();

        StateManager stateManager = new StateManager(vrp);
        StateId id = stateManager.createStateId("max-time");
        StateId openJobsId = stateManager.createStateId("open-jobs-id");
        stateManager.addStateUpdater(new UpdateMaxTimeInVehicle(stateManager, id, vrp.getTransportCosts(), vrp.getActivityCosts(), openJobsId));

        ConstraintManager constraintManager = new ConstraintManager(vrp,stateManager);
        constraintManager.addConstraint(new MaxTimeInVehicleConstraint(vrp.getTransportCosts(), vrp.getActivityCosts(), id, stateManager, vrp, openJobsId), ConstraintManager.Priority.CRITICAL);

        VehicleRoutingAlgorithm vra = Jsprit.Builder.newInstance(vrp).setStateAndConstraintManager(stateManager,constraintManager).buildAlgorithm();
        VehicleRoutingProblemSolution solution = Solutions.bestOf(vra.searchSolutions());

//        Assert.assertEquals(400, solution.getCost(), 0.001);
        SolutionPrinter.print(vrp,solution, SolutionPrinter.Print.VERBOSE);
    }
}
