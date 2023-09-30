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
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindowConditionalOnVehicleType;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindowsOverlapImpl;
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

    @Test
    public void ignoreConditionalTimeWindowWithExclusion(){
        VehicleTypeImpl.Builder vehicleTypeBuilder1 = VehicleTypeImpl.Builder.newInstance("vehicleType1");
        VehicleTypeImpl.Builder vehicleTypeBuilder2 = VehicleTypeImpl.Builder.newInstance("vehicleType2");

        VehicleType vehicleType1 = vehicleTypeBuilder1.build();
        VehicleType vehicleType2 = vehicleTypeBuilder2.build();

        VehicleImpl.Builder vehicleBuilder = VehicleImpl.Builder.newInstance("v1");
        vehicleBuilder.setStartLocation(Location.newInstance(0, 0));
        vehicleBuilder.setType(vehicleType1);
        vehicleBuilder.setEarliestStart(0).setLatestArrival(200);

        VehicleImpl vehicle1 = vehicleBuilder.build();

        vehicleBuilder = VehicleImpl.Builder.newInstance("v2");
        vehicleBuilder.setStartLocation(Location.newInstance(0, 0));
        vehicleBuilder.setType(vehicleType2);
        vehicleBuilder.setEarliestStart(0).setLatestArrival(200);

        VehicleImpl vehicle2 = vehicleBuilder.build();


        // First service is possible for vehicle v1 but only after a while due to exclusion.
        TimeWindowsOverlapImpl tws = new TimeWindowsOverlapImpl();
        tws.addIncludedTimeWindow(TimeWindow.newInstance(0,100));
        tws.addExcludedTimeWindow(TimeWindowConditionalOnVehicleType.newInstance(0,40, "vehicleType1"));
        Service service1 = Service.Builder.newInstance("s1").setLocation(Location.newInstance(0, 0))
            .setServiceTime(0).setTimeWindows(tws).build();

        VehicleRoutingProblem vrp1 = VehicleRoutingProblem.Builder.newInstance()
            .addVehicle(vehicle1)
            .addJob(service1)
            .setFleetSize(VehicleRoutingProblem.FleetSize.FINITE)
            .build();

        VehicleRoutingAlgorithm vra1 = Jsprit.createAlgorithm(vrp1);
        vra1.setMaxIterations(50);
        VehicleRoutingProblemSolution solution1 = Solutions.bestOf(vra1.searchSolutions());

        Assert.assertEquals(0, solution1.getUnassignedJobs().size());
        Assert.assertEquals(1, (int)solution1.getRoutes().iterator().next().getActivities().size());
        Assert.assertEquals(40, (int)solution1.getRoutes().iterator().next().getActivities().get(0).getEndTime());

        // For a vehicle not respecting the condition, the exclusion should not apply.
        VehicleRoutingProblem vrp2 = VehicleRoutingProblem.Builder.newInstance()
            .addVehicle(vehicle2)
            .addJob(service1)
            .setFleetSize(VehicleRoutingProblem.FleetSize.FINITE)
            .build();

        VehicleRoutingAlgorithm vra2 = Jsprit.createAlgorithm(vrp2);
        vra2.setMaxIterations(50);
        VehicleRoutingProblemSolution solution2 = Solutions.bestOf(vra2.searchSolutions());

        Assert.assertEquals(0, solution2.getUnassignedJobs().size());
        Assert.assertEquals(0, (int)solution2.getRoutes().iterator().next().getActivities().get(0).getArrTime());

        // Finaly, try the different kinds of exclusion
        tws = new TimeWindowsOverlapImpl();
        tws.addIncludedTimeWindow(TimeWindow.newInstance(50,100));
        tws.addExcludedTimeWindow(TimeWindowConditionalOnVehicleType.newInstance(0,70, "vehicleType1"));
        Service service3 = Service.Builder.newInstance("s1").setLocation(Location.newInstance(0, 0))
            .setServiceTime(0).setTimeWindows(tws).build();

        VehicleRoutingProblem vrp3 = VehicleRoutingProblem.Builder.newInstance()
            .addVehicle(vehicle1)
            .addJob(service3)
            .setFleetSize(VehicleRoutingProblem.FleetSize.FINITE)
            .build();

        VehicleRoutingAlgorithm vra3 = Jsprit.createAlgorithm(vrp3);
        vra3.setMaxIterations(50);
        VehicleRoutingProblemSolution solution3 = Solutions.bestOf(vra3.searchSolutions());

        Assert.assertEquals(0, solution3.getUnassignedJobs().size());
        Assert.assertEquals(1, (int)solution3.getRoutes().iterator().next().getActivities().size());
        Assert.assertEquals(70, (int)solution3.getRoutes().iterator().next().getActivities().get(0).getEndTime());

        //
        tws = new TimeWindowsOverlapImpl();
        tws.addIncludedTimeWindow(TimeWindow.newInstance(50,100));
        tws.addExcludedTimeWindow(TimeWindowConditionalOnVehicleType.newInstance(70,150, "vehicleType1"));
        Service service4 = Service.Builder.newInstance("s1").setLocation(Location.newInstance(0, 0))
            .setServiceTime(0).setTimeWindows(tws).build();

        VehicleRoutingProblem vrp4 = VehicleRoutingProblem.Builder.newInstance()
            .addVehicle(vehicle1)
            .addJob(service4)
            .setFleetSize(VehicleRoutingProblem.FleetSize.FINITE)
            .build();

        VehicleRoutingAlgorithm vra4 = Jsprit.createAlgorithm(vrp4);
        vra4.setMaxIterations(50);
        VehicleRoutingProblemSolution solution4 = Solutions.bestOf(vra4.searchSolutions());

        Assert.assertEquals(0, solution4.getUnassignedJobs().size());
        Assert.assertEquals(1, (int)solution4.getRoutes().iterator().next().getActivities().size());
        Assert.assertEquals(50, (int)solution4.getRoutes().iterator().next().getActivities().get(0).getEndTime());

        //
        tws = new TimeWindowsOverlapImpl();
        tws.addIncludedTimeWindow(TimeWindow.newInstance(50,100));
        tws.addExcludedTimeWindow(TimeWindowConditionalOnVehicleType.newInstance(120,150, "vehicleType1"));
        Service service5 = Service.Builder.newInstance("s1").setLocation(Location.newInstance(0, 0))
            .setServiceTime(0).setTimeWindows(tws).build();

        VehicleRoutingProblem vrp5 = VehicleRoutingProblem.Builder.newInstance()
            .addVehicle(vehicle1)
            .addJob(service5)
            .setFleetSize(VehicleRoutingProblem.FleetSize.FINITE)
            .build();

        VehicleRoutingAlgorithm vra5 = Jsprit.createAlgorithm(vrp5);
        vra4.setMaxIterations(50);
        VehicleRoutingProblemSolution solution5 = Solutions.bestOf(vra4.searchSolutions());

        Assert.assertEquals(0, solution5.getUnassignedJobs().size());
        Assert.assertEquals(1, (int)solution5.getRoutes().iterator().next().getActivities().size());
        Assert.assertEquals(50, (int)solution5.getRoutes().iterator().next().getActivities().get(0).getEndTime());
    }
}
