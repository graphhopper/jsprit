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

package com.graphhopper.jsprit.examples;


import com.graphhopper.jsprit.analysis.toolbox.GraphStreamViewer;
import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.box.Jsprit;
import com.graphhopper.jsprit.core.algorithm.state.StateId;
import com.graphhopper.jsprit.core.algorithm.state.StateManager;
import com.graphhopper.jsprit.core.algorithm.state.StateUpdater;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.constraint.ConstraintManager;
import com.graphhopper.jsprit.core.problem.constraint.HardActivityConstraint;
import com.graphhopper.jsprit.core.problem.constraint.HardRouteConstraint;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.misc.JobInsertionContext;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.ActivityVisitor;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.reporting.SolutionPrinter;
import com.graphhopper.jsprit.core.util.Solutions;

import java.util.Collection;

/**
 * Illustrates dependencies between jobs.
 * <p>
 * The hard requirement here is that three jobs with name "get key", "use key" and "deliver key" need to
 * be not only in loose sequence in one particular route but also one after another (without any other activities
 * between them).
 */
public class JobAndActivityDependenciesExample {

    static class KeyStatusUpdater implements StateUpdater, ActivityVisitor {

        StateManager stateManager;

        StateId keyPickedStateId;

        StateId keyUsedStateId;

        private StateId keyDeliveredStateId;

        private VehicleRoute route;


        KeyStatusUpdater(StateManager stateManager, StateId keyPickedStateId, StateId keyUsedStateId, StateId keyDeliveredStateId) {
            this.stateManager = stateManager;
            this.keyPickedStateId = keyPickedStateId;
            this.keyUsedStateId = keyUsedStateId;
            this.keyDeliveredStateId = keyDeliveredStateId;
        }

        @Override
        public void begin(VehicleRoute route) {
            this.route = route;
        }

        @Override
        public void visit(TourActivity activity) {
            if (((TourActivity.JobActivity) activity).getJob().getName().equals("use key")) {
                stateManager.putProblemState(keyUsedStateId, VehicleRoute.class, route);
            } else if (((TourActivity.JobActivity) activity).getJob().getName().equals("get key")) {
                stateManager.putProblemState(keyPickedStateId, VehicleRoute.class, route);
            } else if (((TourActivity.JobActivity) activity).getJob().getName().equals("deliver key")) {
                stateManager.putProblemState(keyDeliveredStateId, VehicleRoute.class, route);
            }
        }

        @Override
        public void finish() {
        }
    }

    static class GetUseAndDeliverHardRouteContraint implements HardRouteConstraint {

        StateManager stateManager;

        StateId keyPickedStateId;

        StateId keyUsedStateId;

        StateId keyDeliveredStateId;

        public GetUseAndDeliverHardRouteContraint(StateManager stateManager, StateId keyPickedStateId, StateId keyUsedStateId, StateId keyDeliveredStateId) {
            this.stateManager = stateManager;
            this.keyPickedStateId = keyPickedStateId;
            this.keyUsedStateId = keyUsedStateId;
            this.keyDeliveredStateId = keyDeliveredStateId;
        }

        @Override
        public boolean fulfilled(JobInsertionContext iFacts) {
            if (iFacts.getJob().getName().equals("get key") || iFacts.getJob().getName().equals("use key")
                || iFacts.getJob().getName().equals("deliver key")) {
                VehicleRoute routeOfPickupKey = stateManager.getProblemState(keyPickedStateId, VehicleRoute.class);
                VehicleRoute routeOfUseKey = stateManager.getProblemState(keyUsedStateId, VehicleRoute.class);
                VehicleRoute routeOfDeliverKey = stateManager.getProblemState(keyDeliveredStateId, VehicleRoute.class);

                if (routeOfPickupKey != null) {
                    if (routeOfPickupKey != iFacts.getRoute()) return false;
                }
                if (routeOfUseKey != null) {
                    if (routeOfUseKey != iFacts.getRoute()) return false;
                }
                if (routeOfDeliverKey != null) {
                    if (routeOfDeliverKey != iFacts.getRoute()) return false;
                }
            }
            return true;

        }
    }

    static class GetUseAndDeliverKeySimpleHardActivityConstraint implements HardActivityConstraint {

        StateManager stateManager;

        StateId keyPickedStateId;

        StateId keyUsedStateId;

        StateId keyDeliveredStateId;

        GetUseAndDeliverKeySimpleHardActivityConstraint(StateManager stateManager, StateId keyPickedStateId, StateId keyUsedStateId, StateId keyDeliveredStateId) {
            this.stateManager = stateManager;
            this.keyPickedStateId = keyPickedStateId;
            this.keyUsedStateId = keyUsedStateId;
            this.keyDeliveredStateId = keyDeliveredStateId;
        }

        @Override
        public ConstraintsStatus fulfilled(JobInsertionContext iFacts, TourActivity prevAct, TourActivity newAct, TourActivity nextAct, double prevActDepTime) {

            VehicleRoute routeOfPickupKey = stateManager.getProblemState(keyPickedStateId, VehicleRoute.class);
            VehicleRoute routeOfUseKey = stateManager.getProblemState(keyUsedStateId, VehicleRoute.class);
            VehicleRoute routeOfDeliverKey = stateManager.getProblemState(keyDeliveredStateId, VehicleRoute.class);

            if (!isPickupKey(newAct) && !isUseKey(newAct) && !isDeliverKey(newAct)) {
                if (isPickupKey(prevAct) && isUseKey(nextAct)) return ConstraintsStatus.NOT_FULFILLED;
                if (isPickupKey(prevAct) && isDeliverKey(nextAct)) return ConstraintsStatus.NOT_FULFILLED;
                if (isUseKey(prevAct) && isDeliverKey(nextAct)) return ConstraintsStatus.NOT_FULFILLED;
            }
            if (isPickupKey(newAct)) {
                if (routeOfUseKey != null) {
                    if (!isUseKey(nextAct)) return ConstraintsStatus.NOT_FULFILLED;
                }
                if (routeOfDeliverKey != null) {
                    if (!isDeliverKey(nextAct)) return ConstraintsStatus.NOT_FULFILLED;
                }
                return ConstraintsStatus.FULFILLED;
            }
            if (isUseKey(newAct)) {
                if (routeOfPickupKey != null) {
                    if (!isPickupKey(prevAct)) return ConstraintsStatus.NOT_FULFILLED;
                }
                if (routeOfDeliverKey != null) {
                    if (!isDeliverKey(nextAct)) return ConstraintsStatus.NOT_FULFILLED;
                }
                return ConstraintsStatus.FULFILLED;
            }
            if (isDeliverKey(newAct)) {
                if (routeOfUseKey != null) {
                    if (!isUseKey(prevAct)) return ConstraintsStatus.NOT_FULFILLED;
                }
            }
            return ConstraintsStatus.FULFILLED;
        }

        private boolean isPickupKey(TourActivity act) {
            if (!(act instanceof TourActivity.JobActivity)) return false;
            return ((TourActivity.JobActivity) act).getJob().getName().equals("get key");
        }

        private boolean isUseKey(TourActivity act) {
            if (!(act instanceof TourActivity.JobActivity)) return false;
            return ((TourActivity.JobActivity) act).getJob().getName().equals("use key");
        }

        private boolean isDeliverKey(TourActivity act) {
            if (!(act instanceof TourActivity.JobActivity)) return false;
            return ((TourActivity.JobActivity) act).getJob().getName().equals("deliver key");
        }


    }

    public static void main(String[] args) {

        VehicleImpl driver1 = VehicleImpl.Builder.newInstance("driver1")
            .addSkill("driver1")
            .setStartLocation(Location.newInstance(0, 0)).setReturnToDepot(false).build();

        VehicleImpl driver3 = VehicleImpl.Builder.newInstance("driver3")
            .addSkill("driver3")
            .setStartLocation(Location.newInstance(-3, 5)).setReturnToDepot(true).build();

        Service s1 = Service.Builder.newInstance("s1")
            .addRequiredSkill("driver1")
            .setName("install new device")
            .setLocation(Location.newInstance(2, 2)).build();
        Service s2 = Service.Builder.newInstance("s2")
            .addRequiredSkill("driver3")
            .setName("deliver key")
            .setLocation(Location.newInstance(2, 4)).build();

        Service s3 = Service.Builder.newInstance("s3")
            .addRequiredSkill("driver1")
            .setName("repair heater")
            .setLocation(Location.newInstance(-2, 2)).build();

        Service s4 = Service.Builder.newInstance("s4")
            .addRequiredSkill("driver3")
            .setName("get key")
            .setLocation(Location.newInstance(-2.3, 4)).build();

        Service s5 = Service.Builder.newInstance("s5")
            .addRequiredSkill("driver1")
            .setName("cleaning")
            .setLocation(Location.newInstance(1, 5)).build();

        Service s6 = Service.Builder.newInstance("s6")
            .addRequiredSkill("driver3")
            .setName("use key")
            .setLocation(Location.newInstance(-2, 3)).build();

        Service s7 = Service.Builder.newInstance("s7")
            .addRequiredSkill("driver3")
            .setName("maintenance")
            .setLocation(Location.newInstance(-1.7, 3.5)).build();

        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance().setFleetSize(VehicleRoutingProblem.FleetSize.FINITE)
            .addJob(s1).addJob(s2).addJob(s3).addJob(s4).addJob(s5).addJob(s6).addJob(s7)
            .addVehicle(driver1).addVehicle(driver3);

        VehicleRoutingProblem vrp = vrpBuilder.build();

        StateManager stateManager = new StateManager(vrp);
        StateId keyPicked = stateManager.createStateId("key-picked");
        StateId keyUsed = stateManager.createStateId("key-used");
        StateId keyDelivered = stateManager.createStateId("key-delivered");
        stateManager.addStateUpdater(new KeyStatusUpdater(stateManager, keyPicked, keyUsed, keyDelivered));

        ConstraintManager constraintManager = new ConstraintManager(vrp, stateManager);
        constraintManager.addConstraint(new GetUseAndDeliverKeySimpleHardActivityConstraint(stateManager, keyPicked, keyUsed, keyDelivered), ConstraintManager.Priority.CRITICAL);
        constraintManager.addConstraint(new GetUseAndDeliverHardRouteContraint(stateManager, keyPicked, keyUsed, keyDelivered));

        VehicleRoutingAlgorithm vra = Jsprit.Builder.newInstance(vrp).setStateAndConstraintManager(stateManager,constraintManager).buildAlgorithm();
        vra.setMaxIterations(100);

        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();

        SolutionPrinter.print(vrp, Solutions.bestOf(solutions), SolutionPrinter.Print.VERBOSE);

        new GraphStreamViewer(vrp, Solutions.bestOf(solutions)).labelWith(GraphStreamViewer.Label.JOB_NAME).display();

    }

}
