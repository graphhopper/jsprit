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

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.graphhopper.jsprit.core.algorithm.box.Jsprit;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Break;
import com.graphhopper.jsprit.core.problem.job.ServiceJob;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.activity.BreakActivity;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleType;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import com.graphhopper.jsprit.core.util.Solutions;

/**
 * Created by schroeder on 08/01/16.
 */
public class IgnoreBreakTimeWindowTest {

    @Test
    public void doNotIgnoreBreakTW() {
        VehicleTypeImpl.Builder vehicleTypeBuilder = VehicleTypeImpl.Builder.newInstance("vehicleType");
        VehicleType vehicleType = vehicleTypeBuilder.setCostPerWaitingTime(0.8).build();

        /*
         * get a vehicle-builder and build a vehicle located at (10,10) with type "vehicleType"
         */

        VehicleImpl vehicle2;
        {
            VehicleImpl.Builder vehicleBuilder = VehicleImpl.Builder.newInstance("v2");
            vehicleBuilder.setStartLocation(Location.newInstance(0, 0));
            vehicleBuilder.setType(vehicleType);
            vehicleBuilder.setEarliestStart(10).setLatestArrival(50);
            vehicleBuilder.setBreak(new Break.Builder("lunch").setTimeWindow(TimeWindow.newInstance(14, 14)).setServiceTime(1.).build());
            vehicle2 = vehicleBuilder.build();
        }
        /*
         * build services at the required locations, each with a capacity-demand of 1.
         */


        ServiceJob service4 = new ServiceJob.Builder("2").setLocation(Location.newInstance(0, 0))
                .setServiceTime(1.).setTimeWindow(TimeWindow.newInstance(17, 17)).build();

        ServiceJob service5 = new ServiceJob.Builder("3").setLocation(Location.newInstance(0, 0))
                .setServiceTime(1.).setTimeWindow(TimeWindow.newInstance(18, 18)).build();

        ServiceJob service7 = new ServiceJob.Builder("4").setLocation(Location.newInstance(0, 0))
                .setServiceTime(1.).setTimeWindow(TimeWindow.newInstance(10, 10)).build();

        ServiceJob service8 = new ServiceJob.Builder("5").setLocation(Location.newInstance(0, 0))
                .setServiceTime(1.).setTimeWindow(TimeWindow.newInstance(12, 12)).build();

        ServiceJob service10 = new ServiceJob.Builder("6").setLocation(Location.newInstance(0, 0))
                .setServiceTime(1.).setTimeWindow(TimeWindow.newInstance(16, 16)).build();

        ServiceJob service11 = new ServiceJob.Builder("7").setLocation(Location.newInstance(0, 0))
                .setServiceTime(1.).setTimeWindow(TimeWindow.newInstance(13, 13)).build();

        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance()
                .addVehicle(vehicle2)
                .addJob(service4)
                .addJob(service5).addJob(service7)
                .addJob(service8).addJob(service10).addJob(service11)
                .setFleetSize(VehicleRoutingProblem.FleetSize.FINITE)
                .build();

        VehicleRoutingAlgorithm vra = Jsprit.createAlgorithm(vrp);
        vra.setMaxIterations(50);

        VehicleRoutingProblemSolution solution = Solutions.bestOf(vra.searchSolutions());


        assertTrue(breakShouldBeTime(solution));
    }

    private boolean breakShouldBeTime(VehicleRoutingProblemSolution solution) {
        boolean inTime = true;
        for (TourActivity act : solution.getRoutes().iterator().next().getActivities()) {
            if (act instanceof BreakActivity) {
                TimeWindow timeWindow = ((BreakActivity) act).getBreakTimeWindow();
                if (act.getEndTime() < timeWindow.getStart()) {
                    inTime = false;
                }
                if (act.getArrTime() > timeWindow.getEnd()) {
                    inTime = false;
                }
            }
        }
        return inTime;
    }
}
