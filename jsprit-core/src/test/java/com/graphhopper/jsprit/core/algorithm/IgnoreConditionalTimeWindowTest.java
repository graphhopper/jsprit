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

import org.junit.Assert;
import org.junit.Test;

import com.graphhopper.jsprit.core.algorithm.box.Jsprit;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindowConditionalOnVehicleType;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleType;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import com.graphhopper.jsprit.core.util.Solutions;

/**
 * Created by marcanpilami on 29/08/2023. This is a test of insertion mechanisms when using a conditional time window.
 */
public class IgnoreConditionalTimeWindowTest {

    @Test
    public void ignoreConditionalTimeWindow(){
        VehicleTypeImpl.Builder vehicleTypeBuilder1 = VehicleTypeImpl.Builder.newInstance("vehicleType1");
        VehicleTypeImpl.Builder vehicleTypeBuilder2 = VehicleTypeImpl.Builder.newInstance("vehicleType2");

        VehicleType vehicleType1 = vehicleTypeBuilder1.build();
        VehicleType vehicleType2 = vehicleTypeBuilder2.build();
		/*
         * get a vehicle-builder and build a vehicle located at (10,10) with type "vehicleType"
		 */

        VehicleImpl vehicle1,vehicle2;
        {
            VehicleImpl.Builder vehicleBuilder = VehicleImpl.Builder.newInstance("v1");
            vehicleBuilder.setStartLocation(Location.newInstance(0, 0));
            vehicleBuilder.setType(vehicleType1);
            vehicleBuilder.setEarliestStart(10).setLatestArrival(50);

            vehicle1 = vehicleBuilder.build();

            vehicleBuilder = VehicleImpl.Builder.newInstance("v2");
            vehicleBuilder.setStartLocation(Location.newInstance(0, 0));
            vehicleBuilder.setType(vehicleType2);
            vehicleBuilder.setEarliestStart(10).setLatestArrival(50);

            vehicle2 = vehicleBuilder.build();
        }

        // First service is impossible for vehicle v1 (TW after vehicle return), but possible for all others which ignore the TW.
        Service service1 = Service.Builder.newInstance("s1").setLocation(Location.newInstance(0, 0))
            .setServiceTime(1.).setTimeWindow(TimeWindowConditionalOnVehicleType.newInstance(100,100, "vehicleType1")).build();

        VehicleRoutingProblem vrp1 = VehicleRoutingProblem.Builder.newInstance()
            .addVehicle(vehicle1)
            .addJob(service1)
            .setFleetSize(VehicleRoutingProblem.FleetSize.FINITE)
            .build();

        VehicleRoutingAlgorithm vra1 = Jsprit.createAlgorithm(vrp1);
        vra1.setMaxIterations(50);
        VehicleRoutingProblemSolution solution1 = Solutions.bestOf(vra1.searchSolutions());

        Assert.assertEquals(1, solution1.getUnassignedJobs().size());

        // Second service is possible for v1.
        Service service2 = Service.Builder.newInstance("s2").setLocation(Location.newInstance(0, 0))
            .setServiceTime(1.).setTimeWindow(TimeWindowConditionalOnVehicleType.newInstance(11, 11, "vehicleType1")).build();

        VehicleRoutingProblem vrp2 = VehicleRoutingProblem.Builder.newInstance()
            .addVehicle(vehicle1)
            .addJob(service1)
            .addJob(service2)
            .setFleetSize(VehicleRoutingProblem.FleetSize.FINITE)
            .build();

        VehicleRoutingAlgorithm vra2 = Jsprit.createAlgorithm(vrp2);
        vra2.setMaxIterations(50);
        VehicleRoutingProblemSolution solution2 = Solutions.bestOf(vra2.searchSolutions());

        Assert.assertEquals(1, solution2.getUnassignedJobs().size());
        Assert.assertEquals(1, solution2.getRoutes().size());
        Assert.assertEquals(1, solution2.getRoutes().iterator().next().getActivities().size());

        // First service should be possible for v2.
        VehicleRoutingProblem vrp3 = VehicleRoutingProblem.Builder.newInstance()
            .addVehicle(vehicle2)
            .addJob(service1)
            .setFleetSize(VehicleRoutingProblem.FleetSize.FINITE)
            .build();

        VehicleRoutingAlgorithm vra3 = Jsprit.createAlgorithm(vrp3);
        vra3.setMaxIterations(50);
        VehicleRoutingProblemSolution solution3 = Solutions.bestOf(vra3.searchSolutions());

        Assert.assertEquals(0, solution3.getUnassignedJobs().size());
    }
}
