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
import com.graphhopper.jsprit.core.algorithm.recreate.listener.JobInsertedListener;
import com.graphhopper.jsprit.core.algorithm.recreate.listener.VehicleSwitchedListener;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.job.Shipment;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleType;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import com.graphhopper.jsprit.core.util.FastVehicleRoutingTransportCostsMatrix;
import com.graphhopper.jsprit.core.util.Solutions;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MeetTimeWindowConstraint_IT {

    VehicleRoutingProblem vrp;

    @Before
    public void doBefore(){
        VehicleType type1 = VehicleTypeImpl.Builder.newInstance("5").build();
        VehicleType type2 = VehicleTypeImpl.Builder.newInstance("3.5").build();
        VehicleImpl vehicle1 = VehicleImpl.Builder.newInstance("21").setStartLocation(Location.newInstance(0,0))
            .setEarliestStart(14400).setLatestArrival(46800).setType(type1).build();
        VehicleImpl vehicle2 = VehicleImpl.Builder.newInstance("19").setStartLocation(Location.newInstance(0,0))
            .setEarliestStart(39600).setLatestArrival(64800).setType(type2).build();
        Service service1 = Service.Builder.newInstance("2").setLocation(Location.newInstance(2000, 0))
            .setTimeWindow(TimeWindow.newInstance(54000,54000)).build();
        Service service2 = Service.Builder.newInstance("1").setLocation(Location.newInstance(1000, 1000))
            .setTimeWindow(TimeWindow.newInstance(19800,21600)).build();
        vrp = VehicleRoutingProblem.Builder.newInstance().addVehicle(vehicle1).addVehicle(vehicle2)
            .addJob(service1).addJob(service2).setFleetSize(VehicleRoutingProblem.FleetSize.FINITE).build();
    }

    @Test
    public void whenEmployingVehicleWithDifferentWorkingShifts_nRoutesShouldBeCorrect() {
        VehicleRoutingAlgorithm vra = Jsprit.createAlgorithm(vrp);
        vra.setMaxIterations(100);
        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();
        Assert.assertEquals(2, Solutions.bestOf(solutions).getRoutes().size());
    }

    @Test
    public void whenEmployingVehicleWithDifferentWorkingShifts_certainJobsCanNeverBeAssignedToCertainVehicles() {
        VehicleRoutingAlgorithm vra = Jsprit.createAlgorithm(vrp);
        vra.setMaxIterations(100);
        final List<Boolean> testFailed = new ArrayList<Boolean>();
        vra.addListener(new JobInsertedListener() {

            @Override
            public void informJobInserted(Job job2insert, VehicleRoute inRoute, double additionalCosts, double additionalTime) {
                if (job2insert.getId().equals("1")) {
                    if (inRoute.getVehicle().getId().equals("19")) {
                        testFailed.add(true);
                    }
                }
                if (job2insert.getId().equals("2")) {
                    if (inRoute.getVehicle().getId().equals("21")) {
                        testFailed.add(true);
                    }
                }
            }

        });
        @SuppressWarnings("unused")
        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();
        assertTrue(testFailed.isEmpty());
    }

    @Test
    public void whenEmployingVehicleWithDifferentWorkingShifts_certainVehiclesCanNeverBeAssignedToCertainRoutes() {
        VehicleRoutingAlgorithm vra = Jsprit.createAlgorithm(vrp);
        vra.setMaxIterations(100);
        final List<Boolean> testFailed = new ArrayList<Boolean>();
        vra.addListener(new VehicleSwitchedListener() {

            @Override
            public void vehicleSwitched(VehicleRoute vehicleRoute, Vehicle oldVehicle, Vehicle newVehicle) {
                if (oldVehicle == null) return;
                if (oldVehicle.getId().equals("21") && newVehicle.getId().equals("19")) {
                    for (Job j : vehicleRoute.getTourActivities().getJobs()) {
                        if (j.getId().equals("1")) {
                            testFailed.add(true);
                        }
                    }
                }
                if (oldVehicle.getId().equals("19") && newVehicle.getId().equals("21")) {
                    for (Job j : vehicleRoute.getTourActivities().getJobs()) {
                        if (j.getId().equals("2")) {
                            testFailed.add(true);
                        }
                    }
                }
            }

        });


        @SuppressWarnings("unused")
        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();
        assertTrue(testFailed.isEmpty());
    }

    @Test
    public void whenEmployingVehicleWithDifferentWorkingShifts_job2CanNeverBeInVehicle21() {
        VehicleRoutingAlgorithm vra = Jsprit.createAlgorithm(vrp);
        vra.setMaxIterations(100);
        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();

        assertEquals(2, Solutions.bestOf(solutions).getRoutes().size());
    }

    @Test
    public void whenEmployingVehicleWithDifferentWorkingShifts_job1ShouldBeAssignedCorrectly() {
        VehicleRoutingAlgorithm vra = Jsprit.createAlgorithm(vrp);
        vra.setMaxIterations(100);
        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();
        assertTrue(containsJob(vrp.getJobs().get("1"), getRoute("21", Solutions.bestOf(solutions))));
    }

    @Test
    public void whenEmployingVehicleWithDifferentWorkingShifts_job2ShouldBeAssignedCorrectly() {
        VehicleRoutingAlgorithm vra = Jsprit.createAlgorithm(vrp);
        vra.setMaxIterations(100);
        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();
        assertTrue(containsJob(vrp.getJobs().get("2"), getRoute("19", Solutions.bestOf(solutions))));
    }


    @Test
    public void whenEmployingVehicleWithDifferentWorkingShifts_and_vehicleSwitchIsNotAllowed_nRoutesShouldBeCorrect() {
        VehicleRoutingAlgorithm vra = Jsprit.Builder.newInstance(vrp).setProperty(Jsprit.Parameter.VEHICLE_SWITCH,"false").buildAlgorithm();
        vra.setMaxIterations(100);
        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();

        assertEquals(2, Solutions.bestOf(solutions).getRoutes().size());
    }

    @Test
    public void whenEmployingVehicleWithDifferentWorkingShifts_and_vehicleSwitchIsNotAllowed_certainJobsCanNeverBeAssignedToCertainVehicles() {
        VehicleRoutingAlgorithm vra = Jsprit.Builder.newInstance(vrp).setProperty(Jsprit.Parameter.VEHICLE_SWITCH,"false").buildAlgorithm();
        vra.setMaxIterations(100);
        final List<Boolean> testFailed = new ArrayList<Boolean>();
        vra.addListener(new JobInsertedListener() {

            @Override
            public void informJobInserted(Job job2insert, VehicleRoute inRoute, double additionalCosts, double additionalTime) {
                if (job2insert.getId().equals("1")) {
                    if (inRoute.getVehicle().getId().equals("19")) {
                        testFailed.add(true);
                    }
                }
                if (job2insert.getId().equals("2")) {
                    if (inRoute.getVehicle().getId().equals("21")) {
                        testFailed.add(true);
                    }
                }
            }

        });
        @SuppressWarnings("unused")
        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();

        assertTrue(testFailed.isEmpty());
    }

    @Test
    public void whenEmployingVehicleWithDifferentWorkingShifts_and_vehicleSwitchIsNotAllowed_certainVehiclesCanNeverBeAssignedToCertainRoutes() {
        VehicleRoutingAlgorithm vra = Jsprit.Builder.newInstance(vrp).setProperty(Jsprit.Parameter.VEHICLE_SWITCH,"false").buildAlgorithm();
        vra.setMaxIterations(100);
        final List<Boolean> testFailed = new ArrayList<Boolean>();
        vra.addListener(new VehicleSwitchedListener() {

            @Override
            public void vehicleSwitched(VehicleRoute vehicleRoute, Vehicle oldVehicle, Vehicle newVehicle) {
                if (oldVehicle == null) return;
                if (oldVehicle.getId().equals("21") && newVehicle.getId().equals("19")) {
                    for (Job j : vehicleRoute.getTourActivities().getJobs()) {
                        if (j.getId().equals("1")) {
                            testFailed.add(true);
                        }
                    }
                }
                if (oldVehicle.getId().equals("19") && newVehicle.getId().equals("21")) {
                    for (Job j : vehicleRoute.getTourActivities().getJobs()) {
                        if (j.getId().equals("2")) {
                            testFailed.add(true);
                        }
                    }
                }
            }

        });


        @SuppressWarnings("unused")
        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();
        assertTrue(testFailed.isEmpty());
    }

    @Test
    public void whenEmployingVehicleWithDifferentWorkingShifts_and_vehicleSwitchIsNotAllowed_job2CanNeverBeInVehicle21() {
        VehicleRoutingAlgorithm vra = Jsprit.Builder.newInstance(vrp).setProperty(Jsprit.Parameter.VEHICLE_SWITCH,"false").buildAlgorithm();
        vra.setMaxIterations(100);
        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();

        assertEquals(2, Solutions.bestOf(solutions).getRoutes().size());
    }

    @Test
    public void whenEmployingVehicleWithDifferentWorkingShifts_and_vehicleSwitchIsNotAllowed_job1ShouldBeAssignedCorrectly() {
        VehicleRoutingAlgorithm vra = Jsprit.Builder.newInstance(vrp).setProperty(Jsprit.Parameter.VEHICLE_SWITCH,"false").buildAlgorithm();
        vra.setMaxIterations(100);
        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();

        assertEquals(2, Solutions.bestOf(solutions).getRoutes().size());
        assertTrue(containsJob(vrp.getJobs().get("1"), getRoute("21", Solutions.bestOf(solutions))));
    }

    @Test
    public void whenEmployingVehicleWithDifferentWorkingShifts_and_vehicleSwitchIsNotAllowed_job2ShouldBeAssignedCorrectly() {
        VehicleRoutingAlgorithm vra = Jsprit.Builder.newInstance(vrp).setProperty(Jsprit.Parameter.VEHICLE_SWITCH,"false").buildAlgorithm();
        vra.setMaxIterations(100);
        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();

        assertEquals(2, Solutions.bestOf(solutions).getRoutes().size());
        assertTrue(containsJob(vrp.getJobs().get("2"), getRoute("19", Solutions.bestOf(solutions))));
    }

    @Test
    public void whenUsingJsprit_driverTimesShouldBeMet() throws IOException {
        VehicleRoutingProblem vrp = createTWBugProblem();
        VehicleRoutingAlgorithm algorithm = Jsprit.createAlgorithm(vrp);
        algorithm.setMaxIterations(1000);
        VehicleRoutingProblemSolution solution = Solutions.bestOf(algorithm.searchSolutions());
        for (VehicleRoute r : solution.getRoutes()) {
            assertTrue(r.getVehicle().getEarliestDeparture() <= r.getDepartureTime());
            assertTrue(r.getVehicle().getLatestArrival() >= r.getEnd().getArrTime());
        }
    }

    private FastVehicleRoutingTransportCostsMatrix createMatrix() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("matrix.txt")));
        String line;
        FastVehicleRoutingTransportCostsMatrix.Builder builder = FastVehicleRoutingTransportCostsMatrix.Builder.newInstance(11, false);
        while ((line = reader.readLine()) != null) {
            String[] split = line.split("\t");
            builder.addTransportDistance(Integer.parseInt(split[0]), Integer.parseInt(split[1]), Double.parseDouble(split[2]));
            builder.addTransportTime(Integer.parseInt(split[0]), Integer.parseInt(split[1]), Double.parseDouble(split[3]));
        }
        return builder.build();
    }


    private boolean containsJob(Job job, VehicleRoute route) {
        if (route == null) return false;
        for (Job j : route.getTourActivities().getJobs()) {
            if (job == j) {
                return true;
            }
        }
        return false;
    }

    private VehicleRoute getRoute(String vehicleId, VehicleRoutingProblemSolution vehicleRoutingProblemSolution) {
        for (VehicleRoute r : vehicleRoutingProblemSolution.getRoutes()) {
            if (r.getVehicle().getId().equals(vehicleId)) {
                return r;
            }
        }
        return null;
    }

    private VehicleRoutingProblem createTWBugProblem() throws IOException {
        VehicleType type = VehicleTypeImpl.Builder.newInstance("type").addCapacityDimension(0,20)
            .setCostPerTransportTime(1.).setCostPerDistance(0).build();
        VehicleImpl v0 = VehicleImpl.Builder.newInstance("vehicle0").setStartLocation(Location.newInstance(0))
            .setEarliestStart(60).setLatestArrival(18060).setType(type).build();
        VehicleImpl v1 = VehicleImpl.Builder.newInstance("vehicle1").setStartLocation(Location.newInstance(0))
            .setEarliestStart(60).setLatestArrival(18060).setType(type).build();
        VehicleImpl v2 = VehicleImpl.Builder.newInstance("vehicle2").setStartLocation(Location.newInstance(0))
            .setEarliestStart(7200).setLatestArrival(36060).setType(type).build();
        VehicleImpl v3 = VehicleImpl.Builder.newInstance("vehicle3").setStartLocation(Location.newInstance(0))
            .setEarliestStart(36000).setLatestArrival(54060).setType(type).build();
        VehicleImpl v4 = VehicleImpl.Builder.newInstance("vehicle4").setStartLocation(Location.newInstance(0))
            .setEarliestStart(36000).setLatestArrival(54060).setType(type).build();

        Service s1 = Service.Builder.newInstance("1").setLocation(Location.Builder.newInstance().setIndex(1).setId("js0").build())
            .setServiceTime(600).setTimeWindow(TimeWindow.newInstance(0,1800)).addSizeDimension(0,1).build();
        Service s2 = Service.Builder.newInstance("2").setLocation(Location.Builder.newInstance().setIndex(2).setId("js2").build())
            .setServiceTime(600).setTimeWindow(TimeWindow.newInstance(5400, 7200)).addSizeDimension(0, 2).build();
        Service s3 = Service.Builder.newInstance("3").setLocation(Location.Builder.newInstance().setIndex(3).setId("js5").build())
            .setServiceTime(1800).setTimeWindow(TimeWindow.newInstance(17100, 18000)).addSizeDimension(0, 10).build();
        Service s4 = Service.Builder.newInstance("4").setLocation(Location.Builder.newInstance().setIndex(4).setId("js4").build())
            .setServiceTime(900).addSizeDimension(0, 2).build();
        Service s5 = Service.Builder.newInstance("5").setLocation(Location.Builder.newInstance().setIndex(5).setId("js8").build())
            .setServiceTime(600).addSizeDimension(0, 4).build();
        Service s6 = Service.Builder.newInstance("6").setLocation(Location.Builder.newInstance().setIndex(6).setId("js10").build())
            .setServiceTime(1500).setTimeWindow(TimeWindow.newInstance(29700,32400)).addSizeDimension(0, 10).build();
        Service s7 = Service.Builder.newInstance("7").setLocation(Location.Builder.newInstance().setIndex(7).setId("jsp3").build())
            .setServiceTime(5594).build();

        Shipment shipment1 = Shipment.Builder.newInstance("shipment1")
            .setPickupServiceTime(900)
            .setPickupLocation(Location.Builder.newInstance().setId("jsp1").setIndex(1).build())
            .setDeliveryLocation(Location.Builder.newInstance().setId("jsd1").setIndex(8).build())
            .setDeliveryServiceTime(900).build();

        Shipment shipment2 = Shipment.Builder.newInstance("shipment2")
            .setPickupLocation(Location.Builder.newInstance().setId("jsp4").setIndex(9).build())
            .setPickupServiceTime(1200)
            .addPickupTimeWindow(21600,23400)
            .setDeliveryLocation(Location.Builder.newInstance().setId("jsd4").setIndex(8).build())
            .setDeliveryServiceTime(900)
            .addDeliveryTimeWindow(25200,27000)
            .build();

        Shipment shipment3 = Shipment.Builder.newInstance("shipment3")
            .setPickupLocation(Location.Builder.newInstance().setId("jsp7").setIndex(9).build())
            .setPickupServiceTime(1200)
            .addPickupTimeWindow(37800,41400)
            .setDeliveryLocation(Location.Builder.newInstance().setId("jsd7").setIndex(8).build())
            .setDeliveryServiceTime(1800)
            .addDeliveryTimeWindow(43200,45900)
            .build();

        Shipment shipment4 = Shipment.Builder.newInstance("shipment4")
            .setPickupLocation(Location.Builder.newInstance().setId("jsp9").setIndex(10).build())
            .setPickupServiceTime(300)
            .addPickupTimeWindow(45000,48600)
            .setDeliveryLocation(Location.Builder.newInstance().setId("jsd9").setIndex(8).build())
            .setDeliveryServiceTime(300)
            .addDeliveryTimeWindow(50400,52200)
            .build();

        FastVehicleRoutingTransportCostsMatrix matrix = createMatrix();
        return VehicleRoutingProblem.Builder.newInstance().setFleetSize(VehicleRoutingProblem.FleetSize.FINITE)
            .addJob(s1).addJob(s2).addJob(s3).addJob(s4).addJob(s5).addJob(s6).addJob(s7)
            .addJob(shipment1).addJob(shipment2).addJob(shipment3).addJob(shipment4)
            .addVehicle(v0).addVehicle(v1).addVehicle(v2).addVehicle(v3).addVehicle(v4)
            .setRoutingCost(matrix).build();

    }

}
