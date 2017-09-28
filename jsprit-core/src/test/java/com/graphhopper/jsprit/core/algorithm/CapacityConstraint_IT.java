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
import com.graphhopper.jsprit.core.analysis.SolutionAnalyser;
import com.graphhopper.jsprit.core.problem.Capacity;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Delivery;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import com.graphhopper.jsprit.core.util.ManhattanCosts;
import com.graphhopper.jsprit.core.util.Solutions;
import org.junit.Assert;
import org.junit.Test;


public class CapacityConstraint_IT {

    @Test
    public void capacityShouldNotBeExceeded() {

        VehicleTypeImpl type1 = VehicleTypeImpl.Builder.newInstance("type1")
            .addCapacityDimension(0,1)
            .addCapacityDimension(1,0).addCapacityDimension(2,17).addCapacityDimension(3,18)
            .addCapacityDimension(4,14).addCapacityDimension(5,18).addCapacityDimension(6,20).build();
        VehicleTypeImpl type2 = VehicleTypeImpl.Builder.newInstance("type2")
            .addCapacityDimension(0,0)
            .addCapacityDimension(1,0).addCapacityDimension(2,17).addCapacityDimension(3,18)
            .addCapacityDimension(4,13).addCapacityDimension(5,18).addCapacityDimension(6,20).build();
        VehicleTypeImpl type3 = VehicleTypeImpl.Builder.newInstance("type3")
            .addCapacityDimension(0,1)
            .addCapacityDimension(1,0).addCapacityDimension(2,17).addCapacityDimension(3,18)
            .addCapacityDimension(4,14).addCapacityDimension(5,18).addCapacityDimension(6,20).build();
        VehicleTypeImpl type4 = VehicleTypeImpl.Builder.newInstance("type4")
            .addCapacityDimension(0,0)
            .addCapacityDimension(1,0).addCapacityDimension(2,17).addCapacityDimension(3,18)
            .addCapacityDimension(4,14).addCapacityDimension(5,17).addCapacityDimension(6,20).build();
        VehicleTypeImpl type5 = VehicleTypeImpl.Builder.newInstance("type5")
            .addCapacityDimension(0,1)
            .addCapacityDimension(1,0).addCapacityDimension(2,16).addCapacityDimension(3,17)
            .addCapacityDimension(4,14).addCapacityDimension(5,18).addCapacityDimension(6,20).build();

        VehicleImpl v1 = VehicleImpl.Builder.newInstance("v1").setStartLocation(Location.newInstance(0,0)).setType(type1).setReturnToDepot(true).build();
        VehicleImpl v2 = VehicleImpl.Builder.newInstance("v2").setStartLocation(Location.newInstance(0, 0)).setType(type2).setReturnToDepot(true).build();
        VehicleImpl v3 = VehicleImpl.Builder.newInstance("v3").setStartLocation(Location.newInstance(0, 0)).setType(type3).setReturnToDepot(true).build();
        VehicleImpl v4 = VehicleImpl.Builder.newInstance("v4").setStartLocation(Location.newInstance(0, 0)).setType(type4).setReturnToDepot(true).build();
        VehicleImpl v5 = VehicleImpl.Builder.newInstance("v5").setStartLocation(Location.newInstance(0, 0)).setType(type5).setReturnToDepot(true).build();

        Delivery d1 = Delivery.Builder.newInstance("d1").setLocation(Location.newInstance(0,10))
            .addSizeDimension(2,1).build();
        Delivery d2 = Delivery.Builder.newInstance("d2").setLocation(Location.newInstance(0,12))
            .addSizeDimension(2,1).addSizeDimension(3,1).build();
        Delivery d3 = Delivery.Builder.newInstance("d3").setLocation(Location.newInstance(0,15))
            .addSizeDimension(0,1).addSizeDimension(4,1).build();
        Delivery d4 = Delivery.Builder.newInstance("d4").setLocation(Location.newInstance(0,20))
            .addSizeDimension(0,1).addSizeDimension(5,1).build();

        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        vrpBuilder.setFleetSize(VehicleRoutingProblem.FleetSize.FINITE)
            .addJob(d1).addJob(d2).addJob(d3).addJob(d4)
            .addVehicle(v1).addVehicle(v2)
            .addVehicle(v3)
            .addVehicle(v4).addVehicle(v5);
        vrpBuilder.setRoutingCost(new ManhattanCosts());

        VehicleRoutingProblem vrp = vrpBuilder.build();

        VehicleRoutingAlgorithm vra = Jsprit.Builder.newInstance(vrp)
            .setProperty(Jsprit.Parameter.VEHICLE_SWITCH, "true").buildAlgorithm();
        vra.setMaxIterations(2000);
        VehicleRoutingProblemSolution solution = Solutions.bestOf(vra.searchSolutions());

        SolutionAnalyser sa = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());

        for(VehicleRoute r : solution.getRoutes()){
            Capacity loadAtBeginning = sa.getLoadAtBeginning(r);
            Capacity capacityDimensions = r.getVehicle().getType().getCapacityDimensions();
//            System.out.println(r.getVehicle().getId() + " load@beginning: "  + loadAtBeginning);
//            System.out.println("cap: " + capacityDimensions);
            Assert.assertTrue("capacity has been exceeded",
            loadAtBeginning.isLessOrEqual(capacityDimensions));
        }
//
        Assert.assertTrue(solution.getRoutes().size() != 1);

    }



}
