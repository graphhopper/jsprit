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


import com.graphhopper.jsprit.core.algorithm.box.GreedySchrimpfFactory;
import com.graphhopper.jsprit.core.algorithm.box.Jsprit;
import com.graphhopper.jsprit.core.algorithm.box.Jsprit.Builder;
import com.graphhopper.jsprit.core.algorithm.box.SchrimpfFactory;
import com.graphhopper.jsprit.core.algorithm.state.StateManager;
import com.graphhopper.jsprit.core.algorithm.state.UpdateEndLocationIfRouteIsOpen;
import com.graphhopper.jsprit.core.algorithm.state.UpdateVariableCosts;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.constraint.ConstraintManager;
import com.graphhopper.jsprit.core.problem.constraint.ServiceLoadActivityLevelConstraint;
import com.graphhopper.jsprit.core.problem.constraint.ServiceLoadRouteLevelConstraint;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.job.Shipment;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.PickupShipment;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleType;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import com.graphhopper.jsprit.core.util.Coordinate;
import com.graphhopper.jsprit.core.util.Solutions;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;

import static org.junit.Assert.*;

public class InitialRoutesTest {

    private VehicleRoutingProblem vrp;

    private VehicleRoute initialRoute;

    @Before
    public void before(){
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
        VehicleImpl v = VehicleImpl.Builder.newInstance("veh1").setStartLocation(Location.newInstance(0,0)).setLatestArrival(48600).build();
        Service s1 = Service.Builder.newInstance("s1").setLocation(Location.newInstance(1000,0)).build();
        Service s2 = Service.Builder.newInstance("s2").setLocation(Location.newInstance(1000,1000)).build();
        builder.addVehicle(v).addJob(s1).addJob(s2);
        initialRoute = VehicleRoute.Builder.newInstance(v).addService(s1).build();
        builder.addInitialVehicleRoute(initialRoute);
        vrp = builder.build();
    }


    @Test
    public void whenSolving_nuJobsInSolutionShouldBe2() {
        VehicleRoutingAlgorithm vra = Jsprit.createAlgorithm(vrp);
        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();
        VehicleRoutingProblemSolution solution = Solutions.bestOf(solutions);
        assertEquals(2, solution.getRoutes().iterator().next().getTourActivities().getJobs().size());
    }

    @Test
    public void whenSolving_nuActsShouldBe2() {
        VehicleRoutingAlgorithm vra = new SchrimpfFactory().createAlgorithm(vrp);
        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();
        VehicleRoutingProblemSolution solution = Solutions.bestOf(solutions);
        assertEquals(2, solution.getRoutes().iterator().next().getActivities().size());
    }

    @Test
    public void whenSolving_deliverService1_shouldBeInRoute() {
        VehicleRoutingAlgorithm vra = new SchrimpfFactory().createAlgorithm(vrp);
        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();
        VehicleRoutingProblemSolution solution = Solutions.bestOf(solutions);
        Job job = getInitialJob("s1", vrp);
        assertTrue(hasActivityIn(solution, "veh1", job));
    }

    private Job getInitialJob(String jobId, VehicleRoutingProblem vrp) {
        for (VehicleRoute r : vrp.getInitialVehicleRoutes()) {
            for (Job j : r.getTourActivities().getJobs()) {
                if (j.getId().equals(jobId)) return j;
            }
        }
        return null;
    }

    private boolean hasActivityIn(Collection<VehicleRoute> routes, String jobId) {
        boolean isInRoute = false;
        for (VehicleRoute route : routes) {
            for (TourActivity act : route.getActivities()) {
                if (act instanceof TourActivity.JobActivity) {
                    if (((TourActivity.JobActivity) act).getJob().getId().equals(jobId)) isInRoute = true;
                }
            }
        }
        return isInRoute;
    }

    private boolean hasActivityIn(VehicleRoutingProblemSolution solution, String vehicleId, Job job) {
        for (VehicleRoute route : solution.getRoutes()) {
            String vehicleId_ = route.getVehicle().getId();
            if (vehicleId_.equals(vehicleId)) {
                if (route.getTourActivities().servesJob(job)) {
                    return true;
                }
            }
        }
        return false;
    }


    private boolean hasActivityIn(VehicleRoute route, String jobId) {
        boolean isInRoute = false;
        for (TourActivity act : route.getActivities()) {
            if (act instanceof TourActivity.JobActivity) {
                if (((TourActivity.JobActivity) act).getJob().getId().equals(jobId)) isInRoute = true;
            }
        }
        return isInRoute;
    }

    @Test
    public void whenSolving_deliverService2_shouldBeInRoute() {
        VehicleRoutingAlgorithm vra = new SchrimpfFactory().createAlgorithm(vrp);
        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();
        VehicleRoutingProblemSolution solution = Solutions.bestOf(solutions);

        assertTrue(hasActivityIn(solution.getRoutes().iterator().next(), "s2"));
    }

    @Test
    public void maxCapacityShouldNotBeExceeded() {
        VehicleType type = VehicleTypeImpl.Builder.newInstance("type").addCapacityDimension(0, 100).build();
        VehicleImpl vehicle = VehicleImpl.Builder.newInstance("veh")
            .setStartLocation(Location.Builder.newInstance().setId("start").setCoordinate(Coordinate.newInstance(0, 0)).build())
            .setType(type)
            .build();

        Shipment shipment = Shipment.Builder.newInstance("s")
            .setPickupLocation(Location.Builder.newInstance().setCoordinate(Coordinate.newInstance(10, 0)).setId("pick").build())
            .setDeliveryLocation(Location.Builder.newInstance().setId("del").setCoordinate(Coordinate.newInstance(0, 10)).build())
            .addSizeDimension(0, 100)
            .build();

        Shipment another_shipment = Shipment.Builder.newInstance("another_s")
            .setPickupLocation(Location.Builder.newInstance().setCoordinate(Coordinate.newInstance(10, 0)).setId("pick").build())
            .setDeliveryLocation(Location.Builder.newInstance().setId("del").setCoordinate(Coordinate.newInstance(0, 10)).build())
            .addSizeDimension(0, 50)
            .build();

        VehicleRoute iniRoute = VehicleRoute.Builder.newInstance(vehicle).addPickup(shipment).addDelivery(shipment).build();

        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addJob(shipment).addVehicle(vehicle).addJob(another_shipment)
            .setFleetSize(VehicleRoutingProblem.FleetSize.FINITE).addInitialVehicleRoute(iniRoute).build();

        VehicleRoutingAlgorithm vra = new GreedySchrimpfFactory().createAlgorithm(vrp);
        vra.setMaxIterations(10);

        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();

        assertFalse(secondActIsPickup(solutions));

    }

    private boolean secondActIsPickup(Collection<VehicleRoutingProblemSolution> solutions) {
        VehicleRoutingProblemSolution solution = Solutions.bestOf(solutions);
        TourActivity secondAct = solution.getRoutes().iterator().next().getActivities().get(1);
        return secondAct instanceof PickupShipment;
    }

    @Test
    public void whenAllJobsInInitialRoute_itShouldWork() {
        Service s = Service.Builder.newInstance("s").setLocation(Location.newInstance(0, 10)).build();
        VehicleImpl v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance(0, 0)).build();
        VehicleRoute iniRoute = VehicleRoute.Builder.newInstance(v).addService(s).build();
        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addInitialVehicleRoute(iniRoute).build();
        VehicleRoutingAlgorithm vra = Jsprit.createAlgorithm(vrp);
        vra.setMaxIterations(100);
        vra.searchSolutions();
        assertTrue(true);
    }

    @Test
    public void buildWithoutTimeConstraints() {
        Service s1 = Service.Builder.newInstance("s1").setLocation(Location.newInstance(0, 10)).addSizeDimension(0, 10).build();
        Service s2 = Service.Builder.newInstance("s2").setLocation(Location.newInstance(10, 20)).addSizeDimension(0, 12).build();

        VehicleTypeImpl vt = VehicleTypeImpl.Builder.newInstance("vt").addCapacityDimension(0, 15).build();
        VehicleImpl v = VehicleImpl.Builder.newInstance("v").setType(vt).setStartLocation(Location.newInstance(0, 0)).build();

        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addJob(s1).addJob(s2).addVehicle(v).build();
        Builder algBuilder = Jsprit.Builder.newInstance(vrp).addCoreStateAndConstraintStuff(false);

        // only required constraints
        StateManager stateManager = new StateManager(vrp);
        ConstraintManager constraintManager = new ConstraintManager(vrp, stateManager);
        constraintManager.addConstraint(new ServiceLoadRouteLevelConstraint(stateManager));
        constraintManager.addConstraint(new ServiceLoadActivityLevelConstraint(stateManager), ConstraintManager.Priority.LOW);
        stateManager.updateLoadStates();
        stateManager.addStateUpdater(new UpdateEndLocationIfRouteIsOpen());
        stateManager.addStateUpdater(new UpdateVariableCosts(vrp.getActivityCosts(), vrp.getTransportCosts(), stateManager));

        algBuilder.setStateAndConstraintManager(stateManager, constraintManager);
        VehicleRoutingAlgorithm vra = algBuilder.buildAlgorithm();
        vra.setMaxIterations(20);
        Collection<VehicleRoutingProblemSolution> searchSolutions = vra.searchSolutions();
        VehicleRoutingProblemSolution bestOf = Solutions.bestOf(searchSolutions);

        //ensure 2 routes
        assertEquals(2, bestOf.getRoutes().size());

        //ensure no time information in first service of first route
        assertEquals(0, bestOf.getRoutes().iterator().next().getActivities().iterator().next().getArrTime(), 0.001);
    }
}
