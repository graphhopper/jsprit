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

package com.graphhopper.jsprit.analysis.toolbox;

import org.junit.Ignore;
import org.junit.Test;

import com.graphhopper.jsprit.core.algorithm.box.Jsprit;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.SizeDimension;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.CustomJob;
import com.graphhopper.jsprit.core.problem.job.ShipmentJob;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleType;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import com.graphhopper.jsprit.core.util.Solutions;

/**
 * Created by schroeder on 18/11/16.
 */
@Ignore
public class PlotterTest {

    @Test
    public void testPlotCustomJob() {

        Vehicle vehicle = VehicleImpl.Builder.newInstance("vehicle").setStartLocation(Location.newInstance(0, 0))
                .build();
        CustomJob cj = CustomJob.Builder.newInstance("job")
                .addPickup(Location.newInstance(10, 0), SizeDimension.Builder.newInstance().addDimension(0, 1).build())
                .addPickup(Location.newInstance(5, 0), SizeDimension.Builder.newInstance().addDimension(0, 2).build())
                .addDelivery(Location.newInstance(20, 00), SizeDimension.Builder.newInstance().addDimension(0, 3).build())
                .build();
        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addJob(cj).addVehicle(vehicle).build();
        new Plotter(vrp).plot("output/plot", "plot");
    }

    @Test
    public void testPlotCustomJobSolution() {
        VehicleType type = VehicleTypeImpl.Builder.newInstance("type").addCapacityDimension(0, 3).build();
        Vehicle vehicle = VehicleImpl.Builder.newInstance("vehicle").setStartLocation(Location.newInstance(0, 0))
                .setType(type).build();
        CustomJob cj = CustomJob.Builder.newInstance("job")
                .addPickup(Location.newInstance(10, 0), SizeDimension.Builder.newInstance().addDimension(0, 1).build())
                .addPickup(Location.newInstance(-5, 4), SizeDimension.Builder.newInstance().addDimension(0, 2).build())
                .addDelivery(Location.newInstance(20, 10), SizeDimension.Builder.newInstance().addDimension(0, 3).build())
                .build();
        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addJob(cj).addVehicle(vehicle).build();
        VehicleRoutingProblemSolution solution = Solutions.bestOf(Jsprit.createAlgorithm(vrp).searchSolutions());
        new Plotter(vrp, solution).plot("output/plotSolution", "plot");
    }

    @Test
    public void testPlotWithExchange() {
        VehicleType type = VehicleTypeImpl.Builder.newInstance("type").addCapacityDimension(0, 3).addCapacityDimension(1, 3).build();
        Vehicle vehicle = VehicleImpl.Builder.newInstance("vehicle").setStartLocation(Location.newInstance(0, 0))
                .setType(type).build();
        CustomJob cj = CustomJob.Builder.newInstance("job")
                .addPickup(Location.newInstance(10, 0), SizeDimension.Builder.newInstance().addDimension(0, 1).addDimension(1, 1).build())
                .addExchange(Location.newInstance(-5, 4), SizeDimension.Builder.newInstance().addDimension(0, -1).addDimension(1, 1).build())
                .addDelivery(Location.newInstance(20, 10), SizeDimension.Builder.newInstance().addDimension(0, 3).build())
                .build();
        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addJob(cj).addVehicle(vehicle).build();
        VehicleRoutingProblemSolution solution = Solutions.bestOf(Jsprit.createAlgorithm(vrp).searchSolutions());
        new Plotter(vrp).plotJobRelations(true).plot("output/plotExchange", "plot");
        new Plotter(vrp, solution).plot("output/plotSolution", "plot");
    }

    @Test
    public void testPlotWithShipments() {
        VehicleType type = VehicleTypeImpl.Builder.newInstance("type").addCapacityDimension(0, 3).build();
        Vehicle vehicle = VehicleImpl.Builder.newInstance("vehicle").setStartLocation(Location.newInstance(0, 0))
                .setType(type).build();
        ShipmentJob shipment = ShipmentJob.Builder.newInstance("shipment").setPickupLocation(Location.newInstance(-5, 4))
                .addSizeDimension(0, 2).setDeliveryLocation(Location.newInstance(20, 10)).build();
        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addJob(shipment).addVehicle(vehicle).build();
        VehicleRoutingProblemSolution solution = Solutions.bestOf(Jsprit.createAlgorithm(vrp).searchSolutions());
        new Plotter(vrp).plotJobRelations(true).plot("output/plotJobs", "plot");
        new Plotter(vrp, solution).plot("output/plotSolution", "plot");
    }
}
