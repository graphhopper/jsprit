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

package com.graphhopper.jsprit.core.problem.constraint;

import com.graphhopper.jsprit.core.algorithm.state.StateId;
import com.graphhopper.jsprit.core.algorithm.state.StateManager;
import com.graphhopper.jsprit.core.algorithm.state.UpdateActivityTimes;
import com.graphhopper.jsprit.core.algorithm.state.UpdateMaxTimeInVehicle;
import com.graphhopper.jsprit.core.problem.AbstractActivity;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Delivery;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.job.Pickup;
import com.graphhopper.jsprit.core.problem.job.Shipment;
import com.graphhopper.jsprit.core.problem.misc.ActivityContext;
import com.graphhopper.jsprit.core.problem.misc.JobInsertionContext;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

/**
 * Created by schroeder on 19/09/16.
 */
public class MaxTimeInVehicleConstraintTest {

    Delivery d1;

    Shipment s1;

    Shipment s2;

    Delivery d2;

    Pickup p1;

    Pickup p2;

    Vehicle v;

    VehicleRoute route;

    VehicleRoutingProblem vrp;

    @Before
    public void doBefore(){

    }

    private void ini(double maxTimeShipment, double maxTimeDelivery, double maxTimePickup) {
        d1 = Delivery.Builder.newInstance("d1").setLocation(Location.newInstance(10,0)).build();

        s1 = Shipment.Builder.newInstance("s1").setPickupLocation(Location.newInstance(20,0))
            .setDeliveryLocation(Location.newInstance(40, 0)).setMaxTimeInVehicle(maxTimeShipment).build();

        s2 = Shipment.Builder.newInstance("s2").setPickupLocation(Location.newInstance(20,0))
            .setDeliveryLocation(Location.newInstance(40, 0)).setMaxTimeInVehicle(maxTimeShipment).build();

        d2 = Delivery.Builder.newInstance("d2")
            .setMaxTimeInVehicle(maxTimeDelivery)
            .setLocation(Location.newInstance(30, 0)).setServiceTime(10).build();

        p1 = Pickup.Builder.newInstance("p1").setLocation(Location.newInstance(10, 0)).build();
        p2 = Pickup.Builder.newInstance("p2")
//            .setMaxTimeInVehicle(maxTimePickup)
            .setLocation(Location.newInstance(20, 0)).build();

        v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance(0,0)).build();

        vrp = VehicleRoutingProblem.Builder.newInstance().addJob(d1).addJob(s1).addJob(d2).addJob(p1).addJob(p2)
            .addVehicle(v).build();

        route = VehicleRoute.Builder.newInstance(v).setJobActivityFactory(vrp.getJobActivityFactory())
            .addDelivery(d1).addPickup(s1).addDelivery(s1).build();
    }

    @Test
    public void shiftOfExistingShipmentsShouldWork(){
        Vehicle v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance(0,0)).build();

        Shipment s1 = Shipment.Builder.newInstance("s1").setPickupLocation(Location.newInstance(20,0))
            .setDeliveryLocation(Location.newInstance(40,0)).setMaxTimeInVehicle(20).build();

        Shipment s2 = Shipment.Builder.newInstance("s2").setPickupLocation(Location.newInstance(20,0))
            .setPickupServiceTime(10)
            .setDeliveryLocation(Location.newInstance(40,0)).setMaxTimeInVehicle(20).build();

        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addJob(s1).addJob(s2).addVehicle(v).build();

        VehicleRoute route = VehicleRoute.Builder.newInstance(v).setJobActivityFactory(vrp.getJobActivityFactory())
            .addPickup(s1).addDelivery(s1).build();

        StateManager stateManager = new StateManager(vrp);
        StateId minSlackId = stateManager.createStateId("min-slack-id");
        StateId openJobsId = stateManager.createStateId("open-jobs-id");

        UpdateMaxTimeInVehicle updater = new UpdateMaxTimeInVehicle(stateManager, minSlackId, vrp.getTransportCosts(), vrp.getActivityCosts(), openJobsId);
        stateManager.addStateUpdater(updater);
        stateManager.addStateUpdater(new UpdateActivityTimes(vrp.getTransportCosts(), vrp.getActivityCosts()));
        stateManager.informInsertionStarts(Arrays.asList(route),new ArrayList<Job>());

        MaxTimeInVehicleConstraint constraint = new MaxTimeInVehicleConstraint(vrp.getTransportCosts(), vrp.getActivityCosts(), minSlackId, stateManager, vrp, openJobsId);
        JobInsertionContext c = new JobInsertionContext(route,s2,v,route.getDriver(),0.);
        List<AbstractActivity> acts = vrp.getActivities(s2);

        c.getAssociatedActivities().add(acts.get(0));
        c.getAssociatedActivities().add(acts.get(1));

        Assert.assertEquals(HardActivityConstraint.ConstraintsStatus.FULFILLED, constraint.fulfilled(c, route.getStart(), acts.get(0), route.getActivities().get(0), 0));
        Assert.assertEquals(HardActivityConstraint.ConstraintsStatus.NOT_FULFILLED, constraint.fulfilled(c, act(route, 0), acts.get(0), act(route, 1), 20));
        Assert.assertEquals(HardActivityConstraint.ConstraintsStatus.FULFILLED, constraint.fulfilled(c, act(route,1), acts.get(0), route.getEnd(), 40));

        //insert pickup at 0
        c.setRelatedActivityContext(new ActivityContext());
        c.getRelatedActivityContext().setArrivalTime(20);
        c.getRelatedActivityContext().setEndTime(30);
        c.getRelatedActivityContext().setInsertionIndex(0);

        Assert.assertEquals(HardActivityConstraint.ConstraintsStatus.FULFILLED, constraint.fulfilled(c, acts.get(0), acts.get(1), act(route,0), 30));
        Assert.assertEquals(HardActivityConstraint.ConstraintsStatus.FULFILLED, constraint.fulfilled(c, act(route,0), acts.get(1), act(route,1), 30));
    }

    private TourActivity act(VehicleRoute route, int index){
        return route.getActivities().get(index);
    }

    @Test
    public void insertingDeliveryAtAnyPositionShouldWork(){
        ini(30d, Double.MAX_VALUE, Double.MAX_VALUE);
        StateManager stateManager = new StateManager(vrp);
        StateId latestStartId = stateManager.createStateId("latest-start-id");
        StateId openJobsId = stateManager.createStateId("open-jobs-id");

        UpdateMaxTimeInVehicle updater = new UpdateMaxTimeInVehicle(stateManager, latestStartId, vrp.getTransportCosts(), vrp.getActivityCosts(), openJobsId);
        stateManager.addStateUpdater(updater);
        stateManager.addStateUpdater(new UpdateActivityTimes(vrp.getTransportCosts(), vrp.getActivityCosts()));
        stateManager.informInsertionStarts(Arrays.asList(route),new ArrayList<Job>());

        MaxTimeInVehicleConstraint constraint = new MaxTimeInVehicleConstraint(vrp.getTransportCosts(), vrp.getActivityCosts(), latestStartId, stateManager, vrp, openJobsId);
        JobInsertionContext c = new JobInsertionContext(route,d2,v,route.getDriver(),0.);
        List<AbstractActivity> acts = vrp.getActivities(d2);
        c.getAssociatedActivities().add(acts.get(0));

        Assert.assertEquals(HardActivityConstraint.ConstraintsStatus.FULFILLED, constraint.fulfilled(c, route.getStart(), acts.get(0), route.getActivities().get(0), 0));
        Assert.assertEquals(HardActivityConstraint.ConstraintsStatus.FULFILLED, constraint.fulfilled(c, route.getActivities().get(0), acts.get(0), route.getActivities().get(1), 10));
        Assert.assertEquals(HardActivityConstraint.ConstraintsStatus.FULFILLED, constraint.fulfilled(c, route.getActivities().get(1), acts.get(0), route.getActivities().get(2), 20));
        Assert.assertEquals(HardActivityConstraint.ConstraintsStatus.FULFILLED, constraint.fulfilled(c, route.getActivities().get(2), acts.get(0), route.getEnd(), 40));

    }

    @Test
    public void insertingD2JustAfterStartShouldWork() {
        ini(20d, 30, Double.MAX_VALUE);

        StateManager stateManager = new StateManager(vrp);
        StateId latestStartId = stateManager.createStateId("latest-start-id");
        StateId openJobsId = stateManager.createStateId("open-jobs-id");

        UpdateMaxTimeInVehicle updater = new UpdateMaxTimeInVehicle(stateManager, latestStartId, vrp.getTransportCosts(), vrp.getActivityCosts(), openJobsId);
        stateManager.addStateUpdater(updater);
        stateManager.addStateUpdater(new UpdateActivityTimes(vrp.getTransportCosts(), vrp.getActivityCosts()));
        stateManager.informInsertionStarts(Arrays.asList(route), new ArrayList<Job>());

        MaxTimeInVehicleConstraint constraint = new MaxTimeInVehicleConstraint(vrp.getTransportCosts(), vrp.getActivityCosts(), latestStartId, stateManager, vrp, openJobsId);
        JobInsertionContext c = new JobInsertionContext(route, d2, v, route.getDriver(), 0.);
        List<AbstractActivity> acts = vrp.getActivities(d2);
        c.getAssociatedActivities().add(acts.get(0));

        Assert.assertEquals("inserting d2 just after start should work", HardActivityConstraint.ConstraintsStatus.FULFILLED, constraint.fulfilled(c, route.getStart(), acts.get(0), route.getActivities().get(0), 0));
    }

    @Test
    public void insertingD2AfterFirstDeliveryShouldWork() {
        ini(20d, 30, Double.MAX_VALUE);

        StateManager stateManager = new StateManager(vrp);
        StateId latestStartId = stateManager.createStateId("latest-start-id");
        StateId openJobsId = stateManager.createStateId("open-jobs-id");

        UpdateMaxTimeInVehicle updater = new UpdateMaxTimeInVehicle(stateManager, latestStartId, vrp.getTransportCosts(), vrp.getActivityCosts(), openJobsId);
        stateManager.addStateUpdater(updater);
        stateManager.addStateUpdater(new UpdateActivityTimes(vrp.getTransportCosts(), vrp.getActivityCosts()));
        stateManager.informInsertionStarts(Arrays.asList(route), new ArrayList<Job>());

        MaxTimeInVehicleConstraint constraint = new MaxTimeInVehicleConstraint(vrp.getTransportCosts(), vrp.getActivityCosts(), latestStartId, stateManager, vrp, openJobsId);
        JobInsertionContext c = new JobInsertionContext(route, d2, v, route.getDriver(), 0.);
        List<AbstractActivity> acts = vrp.getActivities(d2);
        c.getAssociatedActivities().add(acts.get(0));


        Assert.assertEquals("inserting d2 after first delivery should work", HardActivityConstraint.ConstraintsStatus.FULFILLED, constraint.fulfilled(c, route.getActivities().get(0), acts.get(0), route.getActivities().get(1), 10));
    }

    @Test
    public void insertingDeliveryInBetweenShipmentShouldFail(){
        ini(20d, 30, Double.MAX_VALUE);

        StateManager stateManager = new StateManager(vrp);
        StateId latestStartId = stateManager.createStateId("latest-start-id");
        StateId openJobsId = stateManager.createStateId("open-jobs-id");

        UpdateMaxTimeInVehicle updater = new UpdateMaxTimeInVehicle(stateManager, latestStartId, vrp.getTransportCosts(), vrp.getActivityCosts(), openJobsId);
        stateManager.addStateUpdater(updater);
        stateManager.addStateUpdater(new UpdateActivityTimes(vrp.getTransportCosts(), vrp.getActivityCosts()));
        stateManager.informInsertionStarts(Arrays.asList(route),new ArrayList<Job>());

        MaxTimeInVehicleConstraint constraint = new MaxTimeInVehicleConstraint(vrp.getTransportCosts(), vrp.getActivityCosts(), latestStartId, stateManager, vrp, openJobsId);
        JobInsertionContext c = new JobInsertionContext(route,d2,v,route.getDriver(),0.);
        List<AbstractActivity> acts = vrp.getActivities(d2);
        c.getAssociatedActivities().add(acts.get(0));

        Assert.assertEquals("inserting d2 between pickup and delivery shipment should fail due to max-in-vehicle constraint of shipment", HardActivityConstraint.ConstraintsStatus.NOT_FULFILLED, constraint.fulfilled(c, route.getActivities().get(1), acts.get(0), route.getActivities().get(2), 20));
        Assert.assertEquals("inserting d2 at end should fail", HardActivityConstraint.ConstraintsStatus.NOT_FULFILLED, constraint.fulfilled(c, route.getActivities().get(2), acts.get(0), route.getEnd(), 40));
    }



    @Test
    public void insertingPickupShipmentAtAnyPositionShouldWork(){
        ini(25d, Double.MAX_VALUE, Double.MAX_VALUE);
        VehicleRoute r = VehicleRoute.Builder.newInstance(v).setJobActivityFactory(vrp.getJobActivityFactory())
            .addDelivery(d1).addDelivery(d2).build();

        StateManager stateManager = new StateManager(vrp);
        StateId latestStartId = stateManager.createStateId("latest-start-id");
        StateId openJobsId = stateManager.createStateId("open-jobs-id");

        UpdateMaxTimeInVehicle updater = new UpdateMaxTimeInVehicle(stateManager, latestStartId, vrp.getTransportCosts(), vrp.getActivityCosts(), openJobsId);
        stateManager.addStateUpdater(updater);
        stateManager.addStateUpdater(new UpdateActivityTimes(vrp.getTransportCosts(), vrp.getActivityCosts()));
        stateManager.informInsertionStarts(Arrays.asList(r),new ArrayList<Job>());

        MaxTimeInVehicleConstraint constraint = new MaxTimeInVehicleConstraint(vrp.getTransportCosts(), vrp.getActivityCosts(), latestStartId, stateManager, vrp, openJobsId);
        JobInsertionContext c = new JobInsertionContext(r, s1,v,r.getDriver(),0.);
        List<AbstractActivity> acts = vrp.getActivities(s1);
        c.getAssociatedActivities().add(acts.get(0));
        c.getAssociatedActivities().add(acts.get(1));


        Assert.assertEquals(HardActivityConstraint.ConstraintsStatus.FULFILLED, constraint.fulfilled(c, r.getStart(), acts.get(0), r.getActivities().get(0), 0));
        Assert.assertEquals(HardActivityConstraint.ConstraintsStatus.FULFILLED, constraint.fulfilled(c, r.getActivities().get(0), acts.get(0), r.getActivities().get(1), 10));
        Assert.assertEquals(HardActivityConstraint.ConstraintsStatus.FULFILLED, constraint.fulfilled(c, r.getActivities().get(1), acts.get(0), r.getEnd(), 40));
    }

    @Test
    public void insertingPickupShipmentShouldWork() {

        ini(30, Double.MAX_VALUE, Double.MAX_VALUE);
        VehicleRoute r = VehicleRoute.Builder.newInstance(v).setJobActivityFactory(vrp.getJobActivityFactory())
            .addPickup(p1).addDelivery(d2).build();

        StateManager stateManager = new StateManager(vrp);
        StateId latestStartId = stateManager.createStateId("latest-start-id");
        StateId openJobsId = stateManager.createStateId("open-jobs-id");

        UpdateMaxTimeInVehicle updater = new UpdateMaxTimeInVehicle(stateManager, latestStartId, vrp.getTransportCosts(), vrp.getActivityCosts(), openJobsId);
        stateManager.addStateUpdater(updater);
        stateManager.addStateUpdater(new UpdateActivityTimes(vrp.getTransportCosts(), vrp.getActivityCosts()));
        stateManager.informInsertionStarts(Arrays.asList(r), new ArrayList<Job>());

        MaxTimeInVehicleConstraint constraint = new MaxTimeInVehicleConstraint(vrp.getTransportCosts(), vrp.getActivityCosts(), latestStartId, stateManager, vrp, openJobsId);
        JobInsertionContext c = new JobInsertionContext(r, s1, v, r.getDriver(), 0.);
        List<AbstractActivity> acts = vrp.getActivities(s1);
        c.getAssociatedActivities().add(acts.get(0));
        c.getAssociatedActivities().add(acts.get(1));


        Assert.assertEquals(HardActivityConstraint.ConstraintsStatus.FULFILLED, constraint.fulfilled(c, r.getStart(), acts.get(0), r.getActivities().get(0), 0));
        Assert.assertEquals(HardActivityConstraint.ConstraintsStatus.FULFILLED, constraint.fulfilled(c, r.getActivities().get(0), acts.get(0), r.getActivities().get(1), 10));
        Assert.assertEquals(HardActivityConstraint.ConstraintsStatus.FULFILLED, constraint.fulfilled(c, r.getActivities().get(1), acts.get(0), r.getEnd(), 30));

    }

    @Test
    public void insertingPickupShipmentShouldWork2() {

        ini(30, 30, Double.MAX_VALUE);
        VehicleRoute r = VehicleRoute.Builder.newInstance(v).setJobActivityFactory(vrp.getJobActivityFactory())
            .addPickup(p1).addDelivery(d2).build();

        StateManager stateManager = new StateManager(vrp);
        StateId latestStartId = stateManager.createStateId("latest-start-id");
        StateId openJobsId = stateManager.createStateId("open-jobs-id");

        UpdateMaxTimeInVehicle updater = new UpdateMaxTimeInVehicle(stateManager, latestStartId, vrp.getTransportCosts(), vrp.getActivityCosts(), openJobsId);
        stateManager.addStateUpdater(updater);
        stateManager.addStateUpdater(new UpdateActivityTimes(vrp.getTransportCosts(), vrp.getActivityCosts()));
        stateManager.informInsertionStarts(Arrays.asList(r), new ArrayList<Job>());

        MaxTimeInVehicleConstraint constraint = new MaxTimeInVehicleConstraint(vrp.getTransportCosts(), vrp.getActivityCosts(), latestStartId, stateManager, vrp, openJobsId);
        JobInsertionContext c = new JobInsertionContext(r, s1, v, r.getDriver(), 0.);
        List<AbstractActivity> acts = vrp.getActivities(s1);
        c.getAssociatedActivities().add(acts.get(0));
        c.getAssociatedActivities().add(acts.get(1));

        Assert.assertEquals("pickup shipment cannot happen at first pos. since d2 has max in-vehicle time", HardActivityConstraint.ConstraintsStatus.NOT_FULFILLED, constraint.fulfilled(c, r.getStart(), acts.get(0), r.getActivities().get(0), 0));
        Assert.assertEquals("pickup shipment can happen at second pos.", HardActivityConstraint.ConstraintsStatus.FULFILLED, constraint.fulfilled(c, r.getActivities().get(0), acts.get(0), r.getActivities().get(1), 10));
        Assert.assertEquals("d2 has been delivered so pickup shipment is possible", HardActivityConstraint.ConstraintsStatus.FULFILLED, constraint.fulfilled(c, r.getActivities().get(1), acts.get(0), r.getEnd(), 30));
    }

    @Test
    public void testOpenRoutes() {
        /*
        max time of deliveries and shipment should not be influenced at all by open routes
        when pickups are supported it should matter
         */
        Assert.assertTrue(true);
    }

//    @Test(expected = UnsupportedOperationException.class)
//    public void insertingPickupShouldWork(){
//        ini(30, Double.MAX_VALUE, 30);
//        VehicleRoute r = VehicleRoute.Builder.newInstance(v).setJobActivityFactory(vrp.getJobActivityFactory())
//            .addPickup(p1).addPickup(s1).addDelivery(s1).build();
//
//        StateManager stateManager = new StateManager(vrp);
//        StateId latestStartId = stateManager.createStateId("latest-start-id");
//
//        UpdateMaxTimeInVehicle updater = new UpdateMaxTimeInVehicle(stateManager, latestStartId, vrp.getTransportCosts(), vrp.getActivityCosts());
//        stateManager.addStateUpdater(updater);
//        stateManager.informInsertionStarts(Arrays.asList(r), new ArrayList<Job>());
//
//        MaxTimeInVehicleConstraint constraint = new MaxTimeInVehicleConstraint(vrp.getTransportCosts(), vrp.getActivityCosts(), latestStartId, stateManager, vrp);
//        JobInsertionContext c = new JobInsertionContext(r, p2, v, r.getDriver(), 0.);
//        List<AbstractActivity> acts = vrp.getActivities(p2);
//        c.getAssociatedActivities().add(acts.get(0));
//
//
//        Assert.assertEquals("p2 cannot be done at first pos. due to its own max in-vehicle time restriction",HardActivityConstraint.ConstraintsStatus.NOT_FULFILLED, constraint.fulfilled(c, r.getStart(), acts.get(0), r.getActivities().get(0), 0));
//        Assert.assertEquals("p2 cannot be done at second pos. due to its own max in-vehicle time restriction",HardActivityConstraint.ConstraintsStatus.NOT_FULFILLED, constraint.fulfilled(c, r.getActivities().get(0), acts.get(0), r.getActivities().get(1), 10));
//        Assert.assertEquals("p2 cannot be done at third pos. due to its own max in-vehicle time restriction", HardActivityConstraint.ConstraintsStatus.NOT_FULFILLED, constraint.fulfilled(c, r.getActivities().get(1), acts.get(0), r.getActivities().get(2), 20));
//        Assert.assertEquals("p2 can be done at last", HardActivityConstraint.ConstraintsStatus.FULFILLED, constraint.fulfilled(c, r.getActivities().get(2), acts.get(0), r.getEnd(), 40));
//    }

    @Test
    public void whenPickupIsInsertedAt0_insertingDeliveryShipmentShouldFailWhereConstraintIsBroken(){
        ini(25d, Double.MAX_VALUE, Double.MAX_VALUE);
        VehicleRoute r = VehicleRoute.Builder.newInstance(v).setJobActivityFactory(vrp.getJobActivityFactory())
            .addDelivery(d1).addDelivery(d2).build();

        StateManager stateManager = new StateManager(vrp);
        StateId latestStartId = stateManager.createStateId("latest-start-id");
        StateId openJobsId = stateManager.createStateId("open-jobs-id");

        UpdateMaxTimeInVehicle updater = new UpdateMaxTimeInVehicle(stateManager, latestStartId, vrp.getTransportCosts(), vrp.getActivityCosts(), openJobsId);
        stateManager.addStateUpdater(updater);
        stateManager.addStateUpdater(new UpdateActivityTimes(vrp.getTransportCosts(), vrp.getActivityCosts()));
        stateManager.informInsertionStarts(Arrays.asList(r),new ArrayList<Job>());

        MaxTimeInVehicleConstraint constraint = new MaxTimeInVehicleConstraint(vrp.getTransportCosts(), vrp.getActivityCosts(), latestStartId, stateManager, vrp, openJobsId);
        JobInsertionContext c = new JobInsertionContext(r, s1,v,r.getDriver(),0.);
        List<AbstractActivity> acts = vrp.getActivities(s1);
        c.getAssociatedActivities().add(acts.get(0));
        c.getAssociatedActivities().add(acts.get(1));

        ActivityContext ac = new ActivityContext();
        ac.setArrivalTime(20);
        ac.setEndTime(20);
        c.setRelatedActivityContext(ac);

        Assert.assertEquals(HardActivityConstraint.ConstraintsStatus.FULFILLED, constraint.fulfilled(c, acts.get(0), acts.get(1), r.getActivities().get(0), 20));
        Assert.assertEquals(HardActivityConstraint.ConstraintsStatus.NOT_FULFILLED, constraint.fulfilled(c, r.getActivities().get(0), acts.get(1), r.getActivities().get(1), 30));
        Assert.assertEquals(HardActivityConstraint.ConstraintsStatus.NOT_FULFILLED, constraint.fulfilled(c, r.getActivities().get(1), acts.get(1), r.getEnd(), 40));
    }

    @Test
    public void whenPickupIsInsertedAt1_insertingDeliveryShipmentShouldFailWhereConstraintIsBroken(){
        ini(25d, Double.MAX_VALUE, Double.MAX_VALUE);
        VehicleRoute r = VehicleRoute.Builder.newInstance(v).setJobActivityFactory(vrp.getJobActivityFactory())
            .addDelivery(d1).addDelivery(d2).build();

        StateManager stateManager = new StateManager(vrp);
        StateId latestStartId = stateManager.createStateId("latest-start-id");
        StateId openJobsId = stateManager.createStateId("open-jobs-id");

        Map<String,Double> maxTimes = new HashMap<>();
        maxTimes.put("s1",25d);
        UpdateMaxTimeInVehicle updater = new UpdateMaxTimeInVehicle(stateManager, latestStartId, vrp.getTransportCosts(), vrp.getActivityCosts(), openJobsId);
        stateManager.addStateUpdater(updater);
        stateManager.addStateUpdater(new UpdateActivityTimes(vrp.getTransportCosts(), vrp.getActivityCosts()));
        stateManager.informInsertionStarts(Arrays.asList(r),new ArrayList<Job>());

        MaxTimeInVehicleConstraint constraint = new MaxTimeInVehicleConstraint(vrp.getTransportCosts(), vrp.getActivityCosts(), latestStartId, stateManager, vrp, openJobsId);
        JobInsertionContext c = new JobInsertionContext(r, s1,v,r.getDriver(),0.);
        List<AbstractActivity> acts = vrp.getActivities(s1);
        c.getAssociatedActivities().add(acts.get(0));
        c.getAssociatedActivities().add(acts.get(1));

        ActivityContext ac = new ActivityContext();
        ac.setArrivalTime(20);
        ac.setEndTime(20);
        c.setRelatedActivityContext(ac);

        Assert.assertEquals(HardActivityConstraint.ConstraintsStatus.FULFILLED, constraint.fulfilled(c, acts.get(0), acts.get(1), r.getActivities().get(1), 20));
        Assert.assertEquals(HardActivityConstraint.ConstraintsStatus.NOT_FULFILLED, constraint.fulfilled(c, r.getActivities().get(1), acts.get(1), r.getEnd(), 40));
//        Assert.assertEquals(HardActivityConstraint.ConstraintsStatus.NOT_FULFILLED, constraint.fulfilled(c, r.getActivities().get(1), acts.get(1), r.getEnd(), 40));
    }
}
