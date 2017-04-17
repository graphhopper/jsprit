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
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Break;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.job.Shipment;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.BreakActivity;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleType;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import com.graphhopper.jsprit.core.util.Solutions;
import junit.framework.Assert;
import org.junit.Test;

/**
 * Created by schroeder on 08/01/16.
 */
public class IgnoreBreakTimeWindowTest {

    @Test
    public void doNotIgnoreBreakTW(){
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
            vehicleBuilder.setBreak(Break.Builder.newInstance("lunch").setTimeWindow(TimeWindow.newInstance(14, 14)).setServiceTime(1.).build());
            vehicle2 = vehicleBuilder.build();
        }

		/*
         * build services at the required locations, each with a capacity-demand of 1.
		 */

        Service service4 = Service.Builder.newInstance("2").setLocation(Location.newInstance(0, 0))
            .setServiceTime(1.).setTimeWindow(TimeWindow.newInstance(17,17)).build();

        Service service5 = Service.Builder.newInstance("3").setLocation(Location.newInstance(0, 0))
            .setServiceTime(1.).setTimeWindow(TimeWindow.newInstance(18, 18)).build();

        Service service7 = Service.Builder.newInstance("4").setLocation(Location.newInstance(0, 0))
            .setServiceTime(1.).setTimeWindow(TimeWindow.newInstance(10, 10)).build();

        Service service8 = Service.Builder.newInstance("5").setLocation(Location.newInstance(0, 0))
            .setServiceTime(1.).setTimeWindow(TimeWindow.newInstance(12, 12)).build();

        Service service10 = Service.Builder.newInstance("6").setLocation(Location.newInstance(0, 0))
            .setServiceTime(1.).setTimeWindow(TimeWindow.newInstance(16, 16)).build();

        Service service11 = Service.Builder.newInstance("7").setLocation(Location.newInstance(0, 0))
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

        Assert.assertTrue(breakShouldBeTime(solution));
    }

    private boolean breakShouldBeTime(VehicleRoutingProblemSolution solution) {
        boolean inTime = true;
        for(TourActivity act : solution.getRoutes().iterator().next().getActivities()){
            if(act instanceof BreakActivity){
                if(act.getEndTime() < ((BreakActivity) act).getJob().getTimeWindow().getStart()){
                    inTime = false;
                }
                if(act.getArrTime() > ((BreakActivity) act).getJob().getTimeWindow().getEnd()){
                    inTime = false;
                }
            }
        }
        return inTime;
    }

    @Test
    public void breakCannotBeInserted_services() {
        VehicleTypeImpl type = VehicleTypeImpl.Builder.newInstance("type")
            .build();
        VehicleImpl v1 = VehicleImpl.Builder.newInstance("v1")
            .setType(type)
            .setReturnToDepot(false)
            .setStartLocation(Location.newInstance(0, 0))
            .setEarliestStart(0)
            .setLatestArrival(14)
            .setBreak(
                Break.Builder.newInstance("break")
                    .setServiceTime(1)
                    .addTimeWindow(10, 10)
                    .build()
            )
            .build();

        Service s1 = Service.Builder.newInstance("s1").setLocation(Location.newInstance(0, 0))
            .setServiceTime(4)
            .build();
        Service s2 = Service.Builder.newInstance("s2").setLocation(Location.newInstance(0, 0))
            .setServiceTime(4)
            .build();
        Service s3 = Service.Builder.newInstance("s3").setLocation(Location.newInstance(0, 0))
            .setServiceTime(4)
            .build();

        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance()
            .addJob(s1).addJob(s2).addJob(s3)
            .addVehicle(v1)
            .setFleetSize(VehicleRoutingProblem.FleetSize.FINITE)
            .build();

        Jsprit.Builder algoBuilder = Jsprit.Builder.newInstance(vrp);
        VehicleRoutingAlgorithm algorithm = algoBuilder.buildAlgorithm();
        VehicleRoutingProblemSolution solution = Solutions.bestOf(algorithm.searchSolutions());

        Assert.assertTrue(breakShouldNotBe(solution));
    }

    @Test
    public void breakCannotBeInserted_shipments() {
        VehicleTypeImpl type = VehicleTypeImpl.Builder.newInstance("type")
            .build();
        VehicleImpl v1 = VehicleImpl.Builder.newInstance("v1")
            .setType(type)
            .setReturnToDepot(false)
            .setStartLocation(Location.newInstance(0, 0))
            .setEarliestStart(0)
            .setLatestArrival(14)
            .setBreak(
                Break.Builder.newInstance("break")
                    .setServiceTime(1)
                    .addTimeWindow(10, 10)
                    .build()
            )
            .build();

        Shipment s1 = Shipment.Builder.newInstance("s1")
            .setPickupLocation(Location.newInstance(0, 0))
            .setPickupServiceTime(0)
            .setDeliveryLocation(Location.newInstance(0, 0))
            .setDeliveryServiceTime(4)
            .build();
        Shipment s2 = Shipment.Builder.newInstance("s2")
            .setPickupLocation(Location.newInstance(0, 0))
            .setPickupServiceTime(0)
            .setDeliveryLocation(Location.newInstance(0, 0))
            .setDeliveryServiceTime(4)
            .build();
        Shipment s3 = Shipment.Builder.newInstance("s3")
            .setPickupLocation(Location.newInstance(0, 0))
            .setPickupServiceTime(0)
            .setDeliveryLocation(Location.newInstance(0, 0))
            .setDeliveryServiceTime(4)
            .build();

        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance()
            .addJob(s1).addJob(s2).addJob(s3)
            .addVehicle(v1)
            .setFleetSize(VehicleRoutingProblem.FleetSize.FINITE)
            .build();

        Jsprit.Builder algoBuilder = Jsprit.Builder.newInstance(vrp);
        VehicleRoutingAlgorithm algorithm = algoBuilder.buildAlgorithm();
        VehicleRoutingProblemSolution solution = Solutions.bestOf(algorithm.searchSolutions());

        Assert.assertTrue(breakShouldNotBe(solution));
    }

    private boolean breakShouldNotBe(VehicleRoutingProblemSolution solution) {
        boolean breakNotBe = true;
        for (VehicleRoute route : solution.getRoutes()) {
            Break aBreak = route.getVehicle().getBreak();
            if (aBreak != null) {
                if (route.getEnd().getArrTime() > aBreak.getTimeWindow().getEnd()) {
                    if (!route.getTourActivities().servesJob(aBreak))
                        breakNotBe = false;
                }
                else {
                    if (route.getTourActivities().servesJob(aBreak))
                        breakNotBe = false;
                }
            }
        }
        return breakNotBe;
    }
}
