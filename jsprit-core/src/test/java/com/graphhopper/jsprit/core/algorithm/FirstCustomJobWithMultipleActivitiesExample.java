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

import static org.junit.Assert.assertEquals;

import org.junit.Assert;
import org.junit.Test;

import com.graphhopper.jsprit.core.algorithm.box.Jsprit;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.SizeDimension;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.CustomJob;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleType;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import com.graphhopper.jsprit.core.reporting.SolutionPrinter;
import com.graphhopper.jsprit.core.util.Solutions;

/**
 * Created by schroeder on 11/11/16.
 */
public class FirstCustomJobWithMultipleActivitiesExample {



    @Test
    public void shouldRunOK() {
        CustomJob cj = new CustomJob.Builder("job")
                .addPickup(Location.newInstance(10, 0), SizeDimension.Builder.newInstance().addDimension(0, 1).build())
                .addPickup(Location.newInstance(5, 0), SizeDimension.Builder.newInstance().addDimension(0, 2).build())
                .addPickup(Location.newInstance(20, 0), SizeDimension.Builder.newInstance().addDimension(0, 1).build())
                .build();
        VehicleType type = VehicleTypeImpl.Builder.newInstance("type").addCapacityDimension(0, 4).build();
        Vehicle v = VehicleImpl.Builder.newInstance("v").setType(type).setStartLocation(Location.newInstance(0, 0)).build();
        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance()
                .addJob(cj).addVehicle(v).build();
        VehicleRoutingAlgorithm vra = Jsprit.createAlgorithm(vrp);
        vra.setMaxIterations(10);
        VehicleRoutingProblemSolution solution = Solutions.bestOf(vra.searchSolutions());
        SolutionPrinter.print(vrp, solution, SolutionPrinter.Print.VERBOSE);
        Assert.assertTrue(solution.getUnassignedJobs().isEmpty());
    }

    @Test
    public void shouldNotIgnoresCapacity() {
        CustomJob cj = new CustomJob.Builder("job")
                .addPickup(Location.newInstance(10, 0), SizeDimension.Builder.newInstance().addDimension(0, 1).build())
                .addPickup(Location.newInstance(5, 0), SizeDimension.Builder.newInstance().addDimension(0, 2).build())
                .addPickup(Location.newInstance(20, 0), SizeDimension.Builder.newInstance().addDimension(0, 1).build())
                .build();
        VehicleType type = VehicleTypeImpl.Builder.newInstance("type").addCapacityDimension(0, 2).build();
        Vehicle v = VehicleImpl.Builder.newInstance("v").setType(type).setStartLocation(Location.newInstance(0, 0)).build();
        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance()
                .addJob(cj).addVehicle(v).build();
        VehicleRoutingAlgorithm vra = Jsprit.createAlgorithm(vrp);
        vra.setMaxIterations(10);
        VehicleRoutingProblemSolution solution = Solutions.bestOf(vra.searchSolutions());
        SolutionPrinter.print(vrp, solution, SolutionPrinter.Print.VERBOSE);
        Assert.assertFalse(solution.getUnassignedJobs().isEmpty());
    }

    @Test
    public void shouldNotIgnoresCapacityWithMixedPicksAndDeliveries() {
        CustomJob cj = new CustomJob.Builder("job")
                .addPickup(Location.newInstance(10, 0), SizeDimension.Builder.newInstance().addDimension(0, 1).build())
                .addPickup(Location.newInstance(5, 0), SizeDimension.Builder.newInstance().addDimension(0, 2).build())
                .addDelivery(Location.newInstance(20, 0), SizeDimension.Builder.newInstance().addDimension(0, 3).build())
                .build();
        VehicleType type = VehicleTypeImpl.Builder.newInstance("type").addCapacityDimension(0, 2).build();
        Vehicle v = VehicleImpl.Builder.newInstance("v").setType(type).setStartLocation(Location.newInstance(0, 0)).build();
        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance()
                .addJob(cj).addVehicle(v).build();
        VehicleRoutingAlgorithm vra = Jsprit.createAlgorithm(vrp);
        vra.setMaxIterations(10);
        VehicleRoutingProblemSolution solution = Solutions.bestOf(vra.searchSolutions());
        SolutionPrinter.print(vrp, solution, SolutionPrinter.Print.VERBOSE);
        Assert.assertFalse(solution.getUnassignedJobs().isEmpty());
    }

    @Test
    public void shouldNotIgnoresCapacityWithMixedPicksAndDeliveriesV2() {
        CustomJob cj = new CustomJob.Builder("job")
                .addPickup(Location.newInstance(10, 0), SizeDimension.Builder.newInstance().addDimension(0, 1).build())
                .addPickup(Location.newInstance(5, 0), SizeDimension.Builder.newInstance().addDimension(0, 2).build())
                .addDelivery(Location.newInstance(20, 0), SizeDimension.Builder.newInstance().addDimension(0, 3).build())
                .build();
        assertEquals(SizeDimension.Builder.newInstance().addDimension(0, 0).build(), cj.getSizeAtStart());
        assertEquals(SizeDimension.Builder.newInstance().addDimension(0, 0).build(), cj.getSizeAtEnd());
        VehicleType type = VehicleTypeImpl.Builder.newInstance("type").addCapacityDimension(0, 3).build();
        Vehicle v = VehicleImpl.Builder.newInstance("v").setType(type).setStartLocation(Location.newInstance(0, 0)).build();
        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance()
                .addJob(cj).addVehicle(v).build();
        VehicleRoutingAlgorithm vra = Jsprit.createAlgorithm(vrp);
        vra.setMaxIterations(10);
        VehicleRoutingProblemSolution solution = Solutions.bestOf(vra.searchSolutions());
        SolutionPrinter.print(vrp, solution, SolutionPrinter.Print.VERBOSE);
        Assert.assertTrue(solution.getUnassignedJobs().isEmpty());
    }

    @Test
    public void shouldNotIgnoresCapacityWithExchange() {
        CustomJob cj = new CustomJob.Builder("job")
                .addPickup(Location.newInstance(10, 0), SizeDimension.Builder.newInstance().addDimension(0, 1).addDimension(1, 0).build())
                .addExchange(Location.newInstance(5, 0), SizeDimension.Builder.newInstance().addDimension(0, -3).addDimension(1, 2).build())
                .addDelivery(Location.newInstance(20, 0), SizeDimension.Builder.newInstance().addDimension(1, 1).build())
                .build();
        VehicleType type = VehicleTypeImpl.Builder.newInstance("type").addCapacityDimension(0, 3).addCapacityDimension(1, 2).build();
        Vehicle v = VehicleImpl.Builder.newInstance("v").setType(type).setStartLocation(Location.newInstance(0, 0)).build();
        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance()
                .addJob(cj).addVehicle(v).build();
        VehicleRoutingAlgorithm vra = Jsprit.createAlgorithm(vrp);
        vra.setMaxIterations(10);
        VehicleRoutingProblemSolution solution = Solutions.bestOf(vra.searchSolutions());
        SolutionPrinter.print(vrp, solution, SolutionPrinter.Print.VERBOSE);
        Assert.assertTrue(solution.getUnassignedJobs().isEmpty());
    }
}
