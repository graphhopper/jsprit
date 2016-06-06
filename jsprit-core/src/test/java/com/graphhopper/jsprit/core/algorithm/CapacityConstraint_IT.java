/*******************************************************************************
 * Copyright (c) 2014 Stefan Schroeder.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     Stefan Schroeder - initial API and implementation
 ******************************************************************************/
package com.graphhopper.jsprit.core.algorithm;

import com.graphhopper.jsprit.core.algorithm.box.Jsprit;
import com.graphhopper.jsprit.core.analysis.SolutionAnalyser;
import com.graphhopper.jsprit.core.problem.Capacity;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.cost.TransportDistance;
import com.graphhopper.jsprit.core.problem.job.Delivery;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import com.graphhopper.jsprit.core.reporting.SolutionPrinter;
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

        SolutionPrinter.print(vrp,solution, SolutionPrinter.Print.VERBOSE);
        SolutionAnalyser sa = new SolutionAnalyser(vrp, solution, new TransportDistance() {
            @Override
            public double getDistance(Location from, Location to) {
                return new ManhattanCosts().getDistance(from,to);
            }
        });

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
