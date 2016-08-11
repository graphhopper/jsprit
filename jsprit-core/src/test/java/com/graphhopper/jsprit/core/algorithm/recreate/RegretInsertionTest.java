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

package com.graphhopper.jsprit.core.algorithm.recreate;

import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.box.Jsprit;
import com.graphhopper.jsprit.core.algorithm.recreate.listener.BeforeJobInsertionListener;
import com.graphhopper.jsprit.core.algorithm.state.StateId;
import com.graphhopper.jsprit.core.algorithm.state.StateManager;
import com.graphhopper.jsprit.core.algorithm.state.StateUpdater;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.constraint.ConstraintManager;
import com.graphhopper.jsprit.core.problem.constraint.DependencyType;
import com.graphhopper.jsprit.core.problem.constraint.HardRouteConstraint;
import com.graphhopper.jsprit.core.problem.driver.Driver;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.job.Shipment;
import com.graphhopper.jsprit.core.problem.misc.JobInsertionContext;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.ActivityVisitor;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.problem.vehicle.*;
import com.graphhopper.jsprit.core.util.Coordinate;
import com.graphhopper.jsprit.core.util.Solutions;
import junit.framework.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;

public class RegretInsertionTest {

    @Test
    public void noRoutesShouldBeCorrect() {
        Service s1 = Service.Builder.newInstance("s1").setLocation(Location.newInstance(0, 10)).build();
        Service s2 = Service.Builder.newInstance("s2").setLocation(Location.newInstance(0, 5)).build();

        VehicleImpl v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance(0, 0)).build();
        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addJob(s1).addJob(s2).addVehicle(v).build();

        VehicleFleetManager fm = new FiniteFleetManagerFactory(vrp.getVehicles()).createFleetManager();
        JobInsertionCostsCalculator calculator = getCalculator(vrp);
        RegretInsertionFast regretInsertion = new RegretInsertionFast(calculator, vrp, fm);
        Collection<VehicleRoute> routes = new ArrayList<VehicleRoute>();

        regretInsertion.insertJobs(routes, vrp.getJobs().values());
        Assert.assertEquals(1, routes.size());
    }

    @Test
    public void noJobsInRouteShouldBeCorrect() {
        Service s1 = Service.Builder.newInstance("s1").setLocation(Location.newInstance(0, 10)).build();
        Service s2 = Service.Builder.newInstance("s2").setLocation(Location.newInstance(0, 5)).build();

        VehicleImpl v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance(0, 0)).build();
        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addJob(s1).addJob(s2).addVehicle(v).build();

        VehicleFleetManager fm = new FiniteFleetManagerFactory(vrp.getVehicles()).createFleetManager();
        JobInsertionCostsCalculator calculator = getCalculator(vrp);
        RegretInsertionFast regretInsertion = new RegretInsertionFast(calculator, vrp, fm);
        Collection<VehicleRoute> routes = new ArrayList<VehicleRoute>();

        regretInsertion.insertJobs(routes, vrp.getJobs().values());
        Assert.assertEquals(2, routes.iterator().next().getActivities().size());
    }

    @Test
    public void s1ShouldBeAddedFirst() {
        Service s1 = Service.Builder.newInstance("s1").setLocation(Location.newInstance(0, 10)).build();
        Service s2 = Service.Builder.newInstance("s2").setLocation(Location.newInstance(0, 5)).build();

        VehicleImpl v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance(0, 0)).build();
        final VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addJob(s1).addJob(s2).addVehicle(v).build();

        VehicleFleetManager fm = new FiniteFleetManagerFactory(vrp.getVehicles()).createFleetManager();
        JobInsertionCostsCalculator calculator = getCalculator(vrp);
        RegretInsertionFast regretInsertion = new RegretInsertionFast(calculator, vrp, fm);
        Collection<VehicleRoute> routes = new ArrayList<VehicleRoute>();

        CkeckJobSequence position = new CkeckJobSequence(2, s1);
        regretInsertion.addListener(position);
        regretInsertion.insertJobs(routes, vrp.getJobs().values());
        Assert.assertTrue(position.isCorrect());
    }

    @Test
    public void solutionWithFastRegretMustBeCorrect() {
        Service s1 = Service.Builder.newInstance("s1").setLocation(Location.newInstance(0, 10)).build();
        Service s2 = Service.Builder.newInstance("s2").setLocation(Location.newInstance(0, -10)).build();

        VehicleImpl v1 = VehicleImpl.Builder.newInstance("v1").setStartLocation(Location.newInstance(0, 5)).build();
        VehicleImpl v2 = VehicleImpl.Builder.newInstance("v2").setStartLocation(Location.newInstance(0, -5)).build();
        final VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addJob(s1).addJob(s2)
            .addVehicle(v1).addVehicle(v2).setFleetSize(VehicleRoutingProblem.FleetSize.FINITE).build();

        StateManager stateManager = new StateManager(vrp);
        ConstraintManager constraintManager = new ConstraintManager(vrp,stateManager);

        VehicleRoutingAlgorithm vra = Jsprit.Builder.newInstance(vrp)
            .addCoreStateAndConstraintStuff(true)
            .setProperty(Jsprit.Parameter.FAST_REGRET,"true")
            .setStateAndConstraintManager(stateManager, constraintManager).buildAlgorithm();

        VehicleRoutingProblemSolution solution = Solutions.bestOf(vra.searchSolutions());

        Assert.assertEquals(2, solution.getRoutes().size());
    }

    static class JobInRouteUpdater implements StateUpdater, ActivityVisitor {

        private StateManager stateManager;

        private StateId job1AssignedId;

        private StateId job2AssignedId;

        private VehicleRoute route;

        public JobInRouteUpdater(StateManager stateManager, StateId job1AssignedId, StateId job2AssignedId) {
            this.stateManager = stateManager;
            this.job1AssignedId = job1AssignedId;
            this.job2AssignedId = job2AssignedId;
        }

        @Override
        public void begin(VehicleRoute route) {
            this.route = route;
        }

        @Override
        public void visit(TourActivity activity) {
            if(((TourActivity.JobActivity)activity).getJob().getId().equals("s1")){
                stateManager.putProblemState(job1AssignedId,Boolean.class,true);
            }
            if(((TourActivity.JobActivity)activity).getJob().getId().equals("s2")){
                stateManager.putProblemState(job2AssignedId,Boolean.class,true);
            }

        }

        @Override
        public void finish() {

        }
    }

    static class RouteConstraint implements HardRouteConstraint{

        private final StateId job1AssignedId;

        private final StateId job2AssignedId;

        private StateManager stateManager;

        public RouteConstraint(StateId job1Assigned, StateId job2Assigned, StateManager stateManager) {
            this.job1AssignedId = job1Assigned;
            this.job2AssignedId = job2Assigned;
            this.stateManager = stateManager;
        }

        @Override
        public boolean fulfilled(JobInsertionContext insertionContext) {
            if(insertionContext.getJob().getId().equals("s1")){
                Boolean job2Assigned = stateManager.getProblemState(job2AssignedId,Boolean.class);
                if(job2Assigned == null || job2Assigned == false) return true;
                else {
                    for(Job j : insertionContext.getRoute().getTourActivities().getJobs()){
                        if(j.getId().equals("s2")) return true;
                    }
                }
                return false;
            }
            if(insertionContext.getJob().getId().equals("s2")){
                Boolean job1Assigned = stateManager.getProblemState(job1AssignedId,Boolean.class);
                if(job1Assigned == null || job1Assigned == false) return true;
                else {
                    for(Job j : insertionContext.getRoute().getTourActivities().getJobs()){
                        if(j.getId().equals("s1")) return true;
                    }
                }
                return false;
            }
            return true;
        }
    }

    @Test
    public void solutionWithConstraintAndWithFastRegretMustBeCorrect() {
        Service s1 = Service.Builder.newInstance("s1").addSizeDimension(0,1).setLocation(Location.newInstance(0, 10)).build();
        Service s2 = Service.Builder.newInstance("s2").addSizeDimension(0,1).setLocation(Location.newInstance(0, -10)).build();
        Service s3 = Service.Builder.newInstance("s3").addSizeDimension(0,1).setLocation(Location.newInstance(0, -11)).build();
        Service s4 = Service.Builder.newInstance("s4").addSizeDimension(0,1).setLocation(Location.newInstance(0, 11)).build();

        VehicleType type = VehicleTypeImpl.Builder.newInstance("type").addCapacityDimension(0,2).build();
        VehicleImpl v1 = VehicleImpl.Builder.newInstance("v1").setType(type).setStartLocation(Location.newInstance(0, 10)).build();
        VehicleImpl v2 = VehicleImpl.Builder.newInstance("v2").setType(type).setStartLocation(Location.newInstance(0, -10)).build();
        final VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addJob(s1).addJob(s2).addJob(s3).addJob(s4)
            .addVehicle(v1).addVehicle(v2).setFleetSize(VehicleRoutingProblem.FleetSize.FINITE).build();

        final StateManager stateManager = new StateManager(vrp);
        StateId job1Assigned = stateManager.createStateId("job1-assigned");
        StateId job2Assigned = stateManager.createStateId("job2-assigned");
        stateManager.addStateUpdater(new JobInRouteUpdater(stateManager,job1Assigned,job2Assigned));
        ConstraintManager constraintManager = new ConstraintManager(vrp,stateManager);
        constraintManager.addConstraint(new RouteConstraint(job1Assigned,job2Assigned,stateManager));
        constraintManager.setDependencyType("s1", DependencyType.INTRA_ROUTE);
        constraintManager.setDependencyType("s2", DependencyType.INTRA_ROUTE);

        VehicleRoutingAlgorithm vra = Jsprit.Builder.newInstance(vrp)
            .addCoreStateAndConstraintStuff(true)
            .setProperty(Jsprit.Parameter.FAST_REGRET, "true")
            .setStateAndConstraintManager(stateManager, constraintManager)
//            .setProperty(Jsprit.Strategy.CLUSTER_REGRET, "0.")
//            .setProperty(Jsprit.Strategy.CLUSTER_BEST, "0.")
//            .setProperty(Jsprit.Strategy.RADIAL_REGRET, "0.")
//            .setProperty(Jsprit.Strategy.RADIAL_BEST, "0.")
//            .setProperty(Jsprit.Strategy.RANDOM_REGRET, "1.")
//            .setProperty(Jsprit.Strategy.RANDOM_BEST, "0.")
//            .setProperty(Jsprit.Strategy.WORST_REGRET, "0.")
//            .setProperty(Jsprit.Strategy.WORST_BEST, "0.")
            .buildAlgorithm();

        VehicleRoutingProblemSolution solution = Solutions.bestOf(vra.searchSolutions());
        for(VehicleRoute route : solution.getRoutes()){
            if(route.getTourActivities().servesJob(s1)){
                if(!route.getTourActivities().servesJob(s2)){
                    Assert.assertFalse(true);
                }
                else Assert.assertTrue(true);
            }
        }
//        Assert.assertEquals(1, solution.getRoutes().size());
    }

    @Test
    public void solutionWithConstraintAndWithFastRegretConcurrentMustBeCorrect() {
        Service s1 = Service.Builder.newInstance("s1").addSizeDimension(0,1).setLocation(Location.newInstance(0, 10)).build();
        Service s2 = Service.Builder.newInstance("s2").addSizeDimension(0,1).setLocation(Location.newInstance(0, -10)).build();
        Service s3 = Service.Builder.newInstance("s3").addSizeDimension(0,1).setLocation(Location.newInstance(0, -11)).build();
        Service s4 = Service.Builder.newInstance("s4").addSizeDimension(0,1).setLocation(Location.newInstance(0, 11)).build();

        VehicleType type = VehicleTypeImpl.Builder.newInstance("type").addCapacityDimension(0,2).build();
        VehicleImpl v1 = VehicleImpl.Builder.newInstance("v1").setType(type).setStartLocation(Location.newInstance(0, 10)).build();
        VehicleImpl v2 = VehicleImpl.Builder.newInstance("v2").setType(type).setStartLocation(Location.newInstance(0, -10)).build();
        final VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addJob(s1).addJob(s2).addJob(s3).addJob(s4)
            .addVehicle(v1).addVehicle(v2).setFleetSize(VehicleRoutingProblem.FleetSize.FINITE).build();

        final StateManager stateManager = new StateManager(vrp);
        StateId job1Assigned = stateManager.createStateId("job1-assigned");
        StateId job2Assigned = stateManager.createStateId("job2-assigned");
        stateManager.addStateUpdater(new JobInRouteUpdater(stateManager,job1Assigned,job2Assigned));
        ConstraintManager constraintManager = new ConstraintManager(vrp,stateManager);
        constraintManager.addConstraint(new RouteConstraint(job1Assigned,job2Assigned,stateManager));
        constraintManager.setDependencyType("s1", DependencyType.INTRA_ROUTE);
        constraintManager.setDependencyType("s2", DependencyType.INTRA_ROUTE);

        VehicleRoutingAlgorithm vra = Jsprit.Builder.newInstance(vrp)
            .addCoreStateAndConstraintStuff(true)
            .setProperty(Jsprit.Parameter.FAST_REGRET, "true")
            .setProperty(Jsprit.Parameter.THREADS,"4")
            .setStateAndConstraintManager(stateManager, constraintManager)
            .buildAlgorithm();

        VehicleRoutingProblemSolution solution = Solutions.bestOf(vra.searchSolutions());
        for(VehicleRoute route : solution.getRoutes()){
            if(route.getTourActivities().servesJob(s1)){
                if(!route.getTourActivities().servesJob(s2)){
                    Assert.assertFalse(true);
                }
                else Assert.assertTrue(true);
            }
        }
    }

    @Test
    public void shipment1ShouldBeAddedFirst() {
        Shipment s1 = Shipment.Builder.newInstance("s1")
            .setPickupLocation(Location.Builder.newInstance().setId("pick1").setCoordinate(Coordinate.newInstance(-1, 10)).build())
            .setDeliveryLocation(Location.Builder.newInstance().setId("del1").setCoordinate(Coordinate.newInstance(1, 10)).build())
            .build();

        Shipment s2 = Shipment.Builder.newInstance("s2")
            .setPickupLocation(Location.Builder.newInstance().setId("pick2").setCoordinate(Coordinate.newInstance(-1, 20)).build())
            .setDeliveryLocation(Location.Builder.newInstance().setId("del2").setCoordinate(Coordinate.newInstance(1, 20)).build())
            .build();

        VehicleImpl v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.Builder.newInstance().setCoordinate(Coordinate.newInstance(0, 0)).build()).build();
        final VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addJob(s1).addJob(s2).addVehicle(v).build();

        VehicleFleetManager fm = new FiniteFleetManagerFactory(vrp.getVehicles()).createFleetManager();
        JobInsertionCostsCalculator calculator = getShipmentCalculator(vrp);
        RegretInsertionFast regretInsertion = new RegretInsertionFast(calculator, vrp, fm);
        Collection<VehicleRoute> routes = new ArrayList<VehicleRoute>();

        CkeckJobSequence position = new CkeckJobSequence(2, s2);
        regretInsertion.addListener(position);
        regretInsertion.insertJobs(routes, vrp.getJobs().values());
        Assert.assertTrue(position.isCorrect());
    }

    private JobInsertionCostsCalculator getShipmentCalculator(final VehicleRoutingProblem vrp) {
        return new JobInsertionCostsCalculator() {

            @Override
            public InsertionData getInsertionData(VehicleRoute currentRoute, Job newJob, Vehicle newVehicle, double newVehicleDepartureTime, Driver newDriver, double bestKnownCosts) {
                Vehicle vehicle = vrp.getVehicles().iterator().next();
                if (newJob.getId().equals("s1")) {
                    return new InsertionData(10, 0, 0, vehicle, newDriver);
                } else {
                    return new InsertionData(20, 0, 0, vehicle, newDriver);
                }
            }
        };
    }


    static class CkeckJobSequence implements BeforeJobInsertionListener {

        int atPosition;

        Job job;

        int positionCounter = 1;

        boolean correct = false;

        CkeckJobSequence(int atPosition, Job job) {
            this.atPosition = atPosition;
            this.job = job;
        }

        @Override
        public void informBeforeJobInsertion(Job job, InsertionData data, VehicleRoute route) {
            if (job == this.job && atPosition == positionCounter) {
                correct = true;
            }
            positionCounter++;
        }

        public boolean isCorrect() {
            return correct;
        }
    }

    private JobInsertionCostsCalculator getCalculator(final VehicleRoutingProblem vrp) {
        return new JobInsertionCostsCalculator() {

            @Override
            public InsertionData getInsertionData(VehicleRoute currentRoute, Job newJob, Vehicle newVehicle, double newVehicleDepartureTime, Driver newDriver, double bestKnownCosts) {
                Service service = (Service) newJob;
                Vehicle vehicle = vrp.getVehicles().iterator().next();
                InsertionData iData;
                if (currentRoute.isEmpty()) {
                    double mc = getCost(service.getLocation(), vehicle.getStartLocation());
                    iData = new InsertionData(2 * mc, -1, 0, vehicle, newDriver);
                    iData.getEvents().add(new InsertActivity(currentRoute, vehicle, vrp.copyAndGetActivities(newJob).get(0), 0));
                    iData.getEvents().add(new SwitchVehicle(currentRoute, vehicle, newVehicleDepartureTime));
                } else {
                    double best = Double.MAX_VALUE;
                    int bestIndex = 0;
                    int index = 0;
                    TourActivity prevAct = currentRoute.getStart();
                    for (TourActivity act : currentRoute.getActivities()) {
                        double mc = getMarginalCost(service, prevAct, act);
                        if (mc < best) {
                            best = mc;
                            bestIndex = index;
                        }
                        index++;
                        prevAct = act;
                    }
                    double mc = getMarginalCost(service, prevAct, currentRoute.getEnd());
                    if (mc < best) {
                        best = mc;
                        bestIndex = index;
                    }
                    iData = new InsertionData(best, -1, bestIndex, vehicle, newDriver);
                    iData.getEvents().add(new InsertActivity(currentRoute, vehicle, vrp.copyAndGetActivities(newJob).get(0), bestIndex));
                    iData.getEvents().add(new SwitchVehicle(currentRoute, vehicle, newVehicleDepartureTime));
                }
                return iData;
            }

            private double getMarginalCost(Service service, TourActivity prevAct, TourActivity act) {
                double prev_new = getCost(prevAct.getLocation(), service.getLocation());
                double new_act = getCost(service.getLocation(), act.getLocation());
                double prev_act = getCost(prevAct.getLocation(), act.getLocation());
                return prev_new + new_act - prev_act;
            }

            private double getCost(Location loc1, Location loc2) {
                return vrp.getTransportCosts().getTransportCost(loc1, loc2, 0., null, null);
            }
        };

//        LocalActivityInsertionCostsCalculator local = new LocalActivityInsertionCostsCalculator(vrp.getTransportCosts(),vrp.getActivityCosts());
//        StateManager stateManager = new StateManager(vrp);
//        ConstraintManager manager = new ConstraintManager(vrp,stateManager);
//        ServiceInsertionCalculator calculator = new ServiceInsertionCalculator(vrp.getTransportCosts(), local, manager);
//        calculator.setJobActivityFactory(vrp.getJobActivityFactory());
//        return calculator;
    }

}
