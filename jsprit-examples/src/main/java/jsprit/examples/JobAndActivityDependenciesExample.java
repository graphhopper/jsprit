/*******************************************************************************
 * Copyright (C) 2014  Stefan Schroeder
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

package jsprit.examples;


import jsprit.analysis.toolbox.GraphStreamViewer;
import jsprit.core.algorithm.VehicleRoutingAlgorithm;
import jsprit.core.algorithm.VehicleRoutingAlgorithmBuilder;
import jsprit.core.algorithm.state.StateId;
import jsprit.core.algorithm.state.StateManager;
import jsprit.core.algorithm.state.StateUpdater;
import jsprit.core.problem.Location;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.constraint.ConstraintManager;
import jsprit.core.problem.constraint.HardActivityConstraint;
import jsprit.core.problem.constraint.HardRouteConstraint;
import jsprit.core.problem.job.Service;
import jsprit.core.problem.misc.JobInsertionContext;
import jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.solution.route.activity.ActivityVisitor;
import jsprit.core.problem.solution.route.activity.TourActivity;
import jsprit.core.problem.vehicle.VehicleImpl;
import jsprit.core.reporting.SolutionPrinter;
import jsprit.core.util.Solutions;

import java.util.Collection;

/**
 * Illustrates dependencies between jobs.
 *
 * The hard requirement here is that three jobs with name "get key", "use key" and "deliver key" need to
 * be not only in loose sequence in one particular route but also one after another (without any other activities
 * between them).
 *
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
            if(((TourActivity.JobActivity)activity).getJob().getName().equals("use key")) {
                stateManager.putProblemState(keyUsedStateId, VehicleRoute.class, route);
            }
            else if(((TourActivity.JobActivity)activity).getJob().getName().equals("get key")) {
                stateManager.putProblemState(keyPickedStateId, VehicleRoute.class, route);
            }
            else if(((TourActivity.JobActivity)activity).getJob().getName().equals("deliver key")) {
                stateManager.putProblemState(keyDeliveredStateId, VehicleRoute.class, route);
            }
        }

        @Override
        public void finish() {}
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
            if(iFacts.getJob().getName().equals("get key") || iFacts.getJob().getName().equals("use key")
                    || iFacts.getJob().getName().equals("deliver key")){
                VehicleRoute routeOfPickupKey = stateManager.getProblemState(keyPickedStateId, VehicleRoute.class);
                VehicleRoute routeOfUseKey = stateManager.getProblemState(keyUsedStateId, VehicleRoute.class);
                VehicleRoute routeOfDeliverKey = stateManager.getProblemState(keyDeliveredStateId, VehicleRoute.class);

                if( routeOfPickupKey != null ){
                    if( routeOfPickupKey != iFacts.getRoute() ) return false;
                }
                if( routeOfUseKey != null ){
                    if( routeOfUseKey != iFacts.getRoute() ) return false;
                }
                if( routeOfDeliverKey != null ) {
                    if( routeOfDeliverKey != iFacts.getRoute() ) return false;
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

            if( !isPickupKey(newAct) && !isUseKey(newAct) && !isDeliverKey(newAct) ){
                if(isPickupKey(prevAct) && isUseKey(nextAct)) return ConstraintsStatus.NOT_FULFILLED;
                if(isPickupKey(prevAct) && isDeliverKey(nextAct)) return ConstraintsStatus.NOT_FULFILLED;
                if(isUseKey(prevAct) && isDeliverKey(nextAct)) return ConstraintsStatus.NOT_FULFILLED;
            }
            if( isPickupKey(newAct) ) {
                if ( routeOfUseKey != null){
                    if ( !isUseKey(nextAct) ) return ConstraintsStatus.NOT_FULFILLED;
                }
                if ( routeOfDeliverKey != null ){
                    if ( !isDeliverKey( nextAct )) return ConstraintsStatus.NOT_FULFILLED;
                }
                return ConstraintsStatus.FULFILLED;
            }
            if( isUseKey(newAct) ) {
                if ( routeOfPickupKey != null ) {
                    if ( !isPickupKey(prevAct) ) return ConstraintsStatus.NOT_FULFILLED;
                }
                if ( routeOfDeliverKey != null ) {
                    if ( !isDeliverKey(nextAct) ) return ConstraintsStatus.NOT_FULFILLED;
                }
                return ConstraintsStatus.FULFILLED;
            }
            if( isDeliverKey(newAct) ){
                if( routeOfUseKey != null ) {
                    if ( !isUseKey(prevAct) ) return ConstraintsStatus.NOT_FULFILLED;
                }
            }
            return ConstraintsStatus.FULFILLED;
        }

        private boolean isPickupKey(TourActivity act) {
            if(!(act instanceof TourActivity.JobActivity)) return false;
            return ((TourActivity.JobActivity)act).getJob().getName().equals("get key");
        }

        private boolean isUseKey(TourActivity act) {
            if(!(act instanceof TourActivity.JobActivity)) return false;
            return ((TourActivity.JobActivity)act).getJob().getName().equals("use key");
        }

        private boolean isDeliverKey(TourActivity act) {
            if(!(act instanceof TourActivity.JobActivity)) return false;
            return ((TourActivity.JobActivity)act).getJob().getName().equals("deliver key");
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

        VehicleRoutingAlgorithmBuilder vraBuilder = new VehicleRoutingAlgorithmBuilder(vrp,"input/algorithmConfig.xml");
        vraBuilder.addDefaultCostCalculators();
        vraBuilder.addCoreConstraints();

        StateManager stateManager = new StateManager(vrp);
        StateId keyPicked = stateManager.createStateId("key-picked");
        StateId keyUsed = stateManager.createStateId("key-used");
        StateId keyDelivered = stateManager.createStateId("key-delivered");
        stateManager.addStateUpdater(new KeyStatusUpdater(stateManager,keyPicked,keyUsed,keyDelivered));

        ConstraintManager constraintManager = new ConstraintManager(vrp,stateManager);
        constraintManager.addConstraint(new GetUseAndDeliverKeySimpleHardActivityConstraint(stateManager,keyPicked,keyUsed,keyDelivered), ConstraintManager.Priority.CRITICAL);
        constraintManager.addConstraint(new GetUseAndDeliverHardRouteContraint(stateManager,keyPicked,keyUsed,keyDelivered));

        vraBuilder.setStateAndConstraintManager(stateManager,constraintManager);
        VehicleRoutingAlgorithm vra = vraBuilder.build();
        vra.setMaxIterations(100);

        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();

        SolutionPrinter.print(vrp, Solutions.bestOf(solutions), SolutionPrinter.Print.VERBOSE);

        new GraphStreamViewer(vrp, Solutions.bestOf(solutions)).labelWith(GraphStreamViewer.Label.JOB_NAME).display();

    }

}
