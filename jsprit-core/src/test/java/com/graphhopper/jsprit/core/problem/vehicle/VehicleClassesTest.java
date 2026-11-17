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
package com.graphhopper.jsprit.core.problem.vehicle;

import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.box.Jsprit;
import com.graphhopper.jsprit.core.algorithm.state.StateId;
import com.graphhopper.jsprit.core.algorithm.state.StateManager;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.constraint.ConstraintManager;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.util.Solutions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class VehicleClassesTest {

    private static VehicleImpl vehicle(String id, int x, String skill) {
        VehicleImpl.Builder builder = VehicleImpl.Builder.newInstance(id).setStartLocation(Location.newInstance(x, 0));
        if (skill != null) builder.addSkill(skill);
        return builder.build();
    }

    /** what shapes a schedule: locations, operating times, return flag; not the skills */
    private static Object scheduleKey(Vehicle v) {
        return Arrays.asList(v.getStartLocation().getId(), v.getEndLocation().getId(), v.getEarliestDeparture(), v.getLatestArrival(), v.isReturnToDepot());
    }

    @Test
    void vehiclesWithEqualKeysFormAClass() {
        VehicleImpl a = vehicle("a", 0, "x"), b = vehicle("b", 0, "y"), c = vehicle("c", 10, null);
        VehicleClasses classes = VehicleClasses.by(Arrays.asList(a, b, c), VehicleClassesTest::scheduleKey);

        assertEquals(2, classes.size());
        assertEquals(classes.classOf(a), classes.classOf(b));
        assertNotEquals(classes.classOf(a), classes.classOf(c));
        assertEquals(Arrays.asList(a, c), classes.representatives());
    }

    @Test
    void vehicleOutsideTheFleetIsClassifiedByItsKeyOrRejected() {
        VehicleImpl a = vehicle("a", 0, null);
        VehicleClasses classes = VehicleClasses.by(Arrays.asList(a), VehicleClassesTest::scheduleKey);

        assertEquals(classes.classOf(a), classes.classOf(vehicle("twin", 0, "x")));
        assertThrows(IllegalArgumentException.class, () -> classes.classOf(vehicle("elsewhere", 5, null)));
    }

    @Test
    void typeKeyClassesSeparateBySkills() {
        VehicleImpl a = vehicle("a", 0, "x"), b = vehicle("b", 0, "y");
        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addVehicle(a).addVehicle(b)
            .addJob(Service.Builder.newInstance("s").setLocation(Location.newInstance(1, 1)).build()).build();
        VehicleClasses classes = VehicleClasses.byTypeKey(vrp);

        assertNotEquals(classes.classOf(a), classes.classOf(b));
        assertEquals(2, classes.representatives().size());
    }

    @Test
    void vehiclesOfAClassShareTheirStates() {
        VehicleImpl a = vehicle("a", 0, "x"), b = vehicle("b", 0, "y"), c = vehicle("c", 10, null);
        Service s = Service.Builder.newInstance("s").setLocation(Location.newInstance(1, 1)).build();
        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addVehicle(a).addVehicle(b).addVehicle(c).addJob(s).build();
        StateManager stateManager = new StateManager(vrp, VehicleClasses.by(vrp.getVehicles(), VehicleClassesTest::scheduleKey));
        // this test reads a state that only the other class has, which the verification mode forbids
        stateManager.setFailOnMissingVehicleState(false);
        VehicleRoute route = VehicleRoute.Builder.newInstance(a).setJobActivityFactory(vrp.getJobActivityFactory()).addService(s).build();
        TourActivity act = route.getActivities().get(0);
        StateId id = stateManager.createDoubleStateId("distance");
        StateId object = stateManager.createStateId("object");

        stateManager.putActivityDouble(act, a, id, 7.);
        stateManager.putRouteDouble(route, a, id, 9.);
        stateManager.putActivityState(act, a, object, "o");

        assertEquals(7., stateManager.getActivityDouble(act, b, id));
        assertEquals(9., stateManager.getRouteDouble(route, b, id));
        assertEquals("o", stateManager.getActivityState(act, b, object, String.class));
        assertTrue(Double.isNaN(stateManager.getActivityDouble(act, c, id)));
        assertNull(stateManager.getActivityState(act, c, object, String.class));

        stateManager.clear();
        assertTrue(Double.isNaN(stateManager.getActivityDouble(act, a, id)));
        assertNull(stateManager.getActivityState(act, a, object, String.class));
    }

    /** Skills do not shape a schedule: classes without them must lead the search to the very same solution. */
    @Test
    void coarserClassesThatCoverWhatTheUpdatersReadGiveTheSameSolution() {
        Random random = new Random(5);
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance().setFleetSize(VehicleRoutingProblem.FleetSize.FINITE);
        for (int i = 0; i < 8; i++) {
            builder.addVehicle(VehicleImpl.Builder.newInstance("v" + i).setStartLocation(Location.newInstance((i % 2) * 50, 0))
                .setEarliestStart(0).setLatestArrival(700).addSkill("skill" + i).addSkill("all").build());
        }
        for (int i = 0; i < 50; i++) {
            Service.Builder service = Service.Builder.newInstance("s" + i).setServiceTime(5).addRequiredSkill(i % 5 == 0 ? "skill" + (i % 8) : "all")
                .setLocation(Location.newInstance(random.nextInt(100), random.nextInt(100)));
            if (i % 3 == 0) service.addTimeWindow(TimeWindow.newInstance(100, 400));
            builder.addJob(service.build());
        }
        VehicleRoutingProblem vrp = builder.build();

        VehicleClasses typeKeys = VehicleClasses.byTypeKey(vrp);
        VehicleClasses schedule = VehicleClasses.by(vrp.getVehicles(), VehicleClassesTest::scheduleKey);
        assertEquals(8, typeKeys.representatives().size());
        assertEquals(2, schedule.size());

        assertEquals(describe(solve(vrp, typeKeys)), describe(solve(vrp, schedule)));
    }

    /** The box algorithm's updaters declare what they read; plain cost functions declare nothing and count as the type key. */
    @Test
    void declaredClassesFollowFromTheRegisteredUpdaters() {
        VehicleImpl a = vehicle("a", 0, "x"), b = vehicle("b", 0, "y"), c = vehicle("c", 10, null);
        Service s = Service.Builder.newInstance("s").setLocation(Location.newInstance(1, 1)).build();
        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addVehicle(a).addVehicle(b).addVehicle(c).addJob(s).build();

        StateManager nobodyReads = StateManager.withDeclaredVehicleClasses(vrp);
        assertEquals(1, nobodyReads.getVehicleClasses().size());

        StateManager byLocation = StateManager.withDeclaredVehicleClasses(vrp);
        byLocation.addStateUpdater(new ReadsStartLocation());
        assertEquals(2, byLocation.getVehicleClasses().size());
        assertEquals(byLocation.getVehicleClasses().classOf(a), byLocation.getVehicleClasses().classOf(b));

        StateManager bySkillsToo = StateManager.withDeclaredVehicleClasses(vrp);
        bySkillsToo.addStateUpdater(new ReadsStartLocation());
        bySkillsToo.addStateUpdater(new ReadsSkills());
        assertEquals(3, bySkillsToo.getVehicleClasses().size());

        // the classes are fixed once they were asked for
        assertThrows(IllegalStateException.class, () -> bySkillsToo.addStateUpdater(new ReadsSkills()));
    }

    private static class ReadsStartLocation implements com.graphhopper.jsprit.core.algorithm.state.StateUpdater, VehicleDependent {
        @Override
        public Object vehicleKey(Vehicle vehicle) {
            return vehicle.getStartLocation().getId();
        }
    }

    private static class ReadsSkills implements com.graphhopper.jsprit.core.algorithm.state.StateUpdater, VehicleDependent {
        @Override
        public Object vehicleKey(Vehicle vehicle) {
            return vehicle.getSkills();
        }
    }

    /** The built-in cost functions read nothing from the vehicle for times and distances, so skills no longer separate. */
    @Test
    void declaredClassesGiveTheSameSolutionAsTypeKeys() {
        VehicleRoutingProblem vrp = skillFleet();

        StateManager declared = StateManager.withDeclaredVehicleClasses(vrp);
        assertEquals(describe(solve(vrp, new StateManager(vrp))), describe(solve(vrp, declared)));
        assertEquals(2, declared.getVehicleClasses().size());
    }

    /** A cost function that declares nothing counts as "reads the type key": the classes stay as they were. */
    @Test
    void undeclaredCostFunctionKeepsTheTypeKey() {
        VehicleRoutingProblem plain = skillFleet();
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance().setFleetSize(VehicleRoutingProblem.FleetSize.FINITE)
            .addAllVehicles(plain.getVehicles()).addAllJobs(plain.getJobs().values());
        final com.graphhopper.jsprit.core.problem.cost.VehicleRoutingTransportCosts crowFly = plain.getTransportCosts();
        builder.setRoutingCost(new com.graphhopper.jsprit.core.problem.cost.AbstractForwardVehicleRoutingTransportCosts() {
            @Override
            public double getDistance(Location from, Location to, double departureTime, Vehicle vehicle) {
                return crowFly.getDistance(from, to, departureTime, vehicle);
            }

            @Override
            public double getTransportTime(Location from, Location to, double departureTime, com.graphhopper.jsprit.core.problem.driver.Driver driver, Vehicle vehicle) {
                return crowFly.getTransportTime(from, to, departureTime, driver, vehicle);
            }

            @Override
            public double getTransportCost(Location from, Location to, double departureTime, com.graphhopper.jsprit.core.problem.driver.Driver driver, Vehicle vehicle) {
                return crowFly.getTransportCost(from, to, departureTime, driver, vehicle);
            }
        });
        VehicleRoutingProblem vrp = builder.build();

        StateManager declared = StateManager.withDeclaredVehicleClasses(vrp);
        assertEquals(describe(solve(vrp, new StateManager(vrp))), describe(solve(vrp, declared)));
        assertEquals(8, declared.getVehicleClasses().size());
    }

    private static VehicleRoutingProblem skillFleet() {
        Random random = new Random(5);
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance().setFleetSize(VehicleRoutingProblem.FleetSize.FINITE);
        for (int i = 0; i < 8; i++) {
            builder.addVehicle(VehicleImpl.Builder.newInstance("v" + i).setStartLocation(Location.newInstance((i % 2) * 50, 0))
                .setEarliestStart(0).setLatestArrival(700).addSkill("skill" + i).addSkill("all").build());
        }
        for (int i = 0; i < 50; i++) {
            Service.Builder service = Service.Builder.newInstance("s" + i).setServiceTime(5).addRequiredSkill(i % 5 == 0 ? "skill" + (i % 8) : "all")
                .setLocation(Location.newInstance(random.nextInt(100), random.nextInt(100)));
            if (i % 3 == 0) service.addTimeWindow(TimeWindow.newInstance(100, 400));
            builder.addJob(service.build());
        }
        return builder.build();
    }

    private static VehicleRoutingProblemSolution solve(VehicleRoutingProblem vrp, VehicleClasses classes) {
        return solve(vrp, new StateManager(vrp, classes));
    }

    private static VehicleRoutingProblemSolution solve(VehicleRoutingProblem vrp, StateManager stateManager) {
        stateManager.setFailOnMissingVehicleState(true);
        Jsprit.Builder builder = Jsprit.Builder.newInstance(vrp)
            .setStateAndConstraintManager(stateManager, new ConstraintManager(vrp, stateManager))
            .setProperty(Jsprit.Parameter.THREADS, "1");
        builder.setRandom(new Random(11));
        VehicleRoutingAlgorithm algorithm = builder.buildAlgorithm();
        algorithm.setMaxIterations(150);
        return Solutions.bestOf(algorithm.searchSolutions());
    }

    private static String describe(VehicleRoutingProblemSolution solution) {
        List<String> routes = new ArrayList<>();
        for (VehicleRoute route : solution.getRoutes()) {
            StringBuilder s = new StringBuilder(route.getVehicle().getId());
            for (TourActivity act : route.getActivities()) s.append(' ').append(((TourActivity.JobActivity) act).getJob().getId());
            routes.add(s.toString());
        }
        return solution.getCost() + " " + solution.getUnassignedJobs().size() + " " + routes;
    }
}
