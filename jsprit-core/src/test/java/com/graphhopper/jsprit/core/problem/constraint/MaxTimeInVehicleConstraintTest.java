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

    Shipment shipment;

    Delivery d2;

    Pickup p1;

    Pickup p2;

    Vehicle v;

    VehicleRoute route;

    VehicleRoutingProblem vrp;

    @Before
    public void doBefore(){

    }

    private void ini(double maxTime){
        d1 = Delivery.Builder.newInstance("d1").setLocation(Location.newInstance(10,0)).build();
        shipment = Shipment.Builder.newInstance("shipment").setPickupLocation(Location.newInstance(20,0))
            .setDeliveryLocation(Location.newInstance(40,0)).setMaxTimeInVehicle(maxTime).build();
        d2 = Delivery.Builder.newInstance("d2").setLocation(Location.newInstance(30,0)).setServiceTime(10).build();

        p1 = Pickup.Builder.newInstance("p1").setLocation(Location.newInstance(10, 0)).build();
        p2 = Pickup.Builder.newInstance("p2").setLocation(Location.newInstance(20,0)).build();

        v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance(0,0)).build();

        vrp = VehicleRoutingProblem.Builder.newInstance().addJob(d1).addJob(shipment).addJob(d2).addJob(p1).addJob(p2)
            .addVehicle(v).build();

        route = VehicleRoute.Builder.newInstance(v).setJobActivityFactory(vrp.getJobActivityFactory())
            .addDelivery(d1).addPickup(shipment).addDelivery(shipment).build();
    }

    @Test
    public void insertingDeliveryAtAnyPositionShouldWork(){
        ini(30d);
        StateManager stateManager = new StateManager(vrp);
        StateId latestStartId = stateManager.createStateId("latest-start-id");

        UpdateMaxTimeInVehicle updater = new UpdateMaxTimeInVehicle(stateManager,latestStartId,vrp.getTransportCosts(), vrp.getActivityCosts());
        stateManager.addStateUpdater(updater);
        stateManager.informInsertionStarts(Arrays.asList(route),new ArrayList<Job>());

        MaxTimeInVehicleConstraint constraint = new MaxTimeInVehicleConstraint(vrp.getTransportCosts(),vrp.getActivityCosts() , latestStartId, stateManager);
        JobInsertionContext c = new JobInsertionContext(route,d2,v,route.getDriver(),0.);
        List<AbstractActivity> acts = vrp.getActivities(d2);
        c.getAssociatedActivities().add(acts.get(0));

        Assert.assertEquals(HardActivityConstraint.ConstraintsStatus.FULFILLED, constraint.fulfilled(c, route.getStart(), acts.get(0), route.getActivities().get(0), 0));
        Assert.assertEquals(HardActivityConstraint.ConstraintsStatus.FULFILLED, constraint.fulfilled(c, route.getActivities().get(0), acts.get(0), route.getActivities().get(1), 10));
        Assert.assertEquals(HardActivityConstraint.ConstraintsStatus.FULFILLED, constraint.fulfilled(c, route.getActivities().get(1), acts.get(0), route.getActivities().get(2), 20));
        Assert.assertEquals(HardActivityConstraint.ConstraintsStatus.FULFILLED, constraint.fulfilled(c, route.getActivities().get(2), acts.get(0), route.getEnd(), 40));

    }

    @Test
    public void insertingDeliveryInBetweenShipmentShouldFail(){
        ini(25d);
        StateManager stateManager = new StateManager(vrp);
        StateId latestStartId = stateManager.createStateId("latest-start-id");

        UpdateMaxTimeInVehicle updater = new UpdateMaxTimeInVehicle(stateManager,latestStartId,vrp.getTransportCosts(),vrp.getActivityCosts());
        stateManager.addStateUpdater(updater);
        stateManager.informInsertionStarts(Arrays.asList(route),new ArrayList<Job>());

        MaxTimeInVehicleConstraint constraint = new MaxTimeInVehicleConstraint(vrp.getTransportCosts(),vrp.getActivityCosts() , latestStartId, stateManager);
        JobInsertionContext c = new JobInsertionContext(route,d2,v,route.getDriver(),0.);
        List<AbstractActivity> acts = vrp.getActivities(d2);
        c.getAssociatedActivities().add(acts.get(0));

        Assert.assertEquals(HardActivityConstraint.ConstraintsStatus.FULFILLED, constraint.fulfilled(c, route.getStart(), acts.get(0), route.getActivities().get(0), 0));
        Assert.assertEquals(HardActivityConstraint.ConstraintsStatus.FULFILLED, constraint.fulfilled(c, route.getActivities().get(0), acts.get(0), route.getActivities().get(1), 10));
        Assert.assertEquals(HardActivityConstraint.ConstraintsStatus.NOT_FULFILLED, constraint.fulfilled(c, route.getActivities().get(1), acts.get(0), route.getActivities().get(2), 20));
        Assert.assertEquals(HardActivityConstraint.ConstraintsStatus.FULFILLED, constraint.fulfilled(c, route.getActivities().get(2), acts.get(0), route.getEnd(), 40));
    }



    @Test
    public void insertingPickupShipmentAtAnyPositionShouldWork(){
        ini(25d);
        VehicleRoute r = VehicleRoute.Builder.newInstance(v).setJobActivityFactory(vrp.getJobActivityFactory())
            .addDelivery(d1).addDelivery(d2).build();

        StateManager stateManager = new StateManager(vrp);
        StateId latestStartId = stateManager.createStateId("latest-start-id");

        UpdateMaxTimeInVehicle updater = new UpdateMaxTimeInVehicle(stateManager,latestStartId,vrp.getTransportCosts(), vrp.getActivityCosts());
        stateManager.addStateUpdater(updater);
        stateManager.informInsertionStarts(Arrays.asList(r),new ArrayList<Job>());

        MaxTimeInVehicleConstraint constraint = new MaxTimeInVehicleConstraint(vrp.getTransportCosts(),vrp.getActivityCosts() , latestStartId, stateManager);
        JobInsertionContext c = new JobInsertionContext(r,shipment,v,r.getDriver(),0.);
        List<AbstractActivity> acts = vrp.getActivities(shipment);
        c.getAssociatedActivities().add(acts.get(0));
        c.getAssociatedActivities().add(acts.get(1));


        Assert.assertEquals(HardActivityConstraint.ConstraintsStatus.FULFILLED, constraint.fulfilled(c, r.getStart(), acts.get(0), r.getActivities().get(0), 0));
        Assert.assertEquals(HardActivityConstraint.ConstraintsStatus.FULFILLED, constraint.fulfilled(c, r.getActivities().get(0), acts.get(0), r.getActivities().get(1), 10));
        Assert.assertEquals(HardActivityConstraint.ConstraintsStatus.FULFILLED, constraint.fulfilled(c, r.getActivities().get(1), acts.get(0), r.getEnd(), 40));
    }

//    @Test
//    public void insertingPickupBeforeDeliveryShouldFail(){
//        VehicleRoute r = VehicleRoute.Builder.newInstance(v).setJobActivityFactory(vrp.getJobActivityFactory())
//            .addPickup(p1).addDelivery(d2).build();
//
//        StateManager stateManager = new StateManager(vrp);
//        StateId latestStartId = stateManager.createStateId("latest-start-id");
//
//        Map<String,Double> maxTimes = new HashMap<>();
//        maxTimes.put("p2",30d);
//        UpdateMaxTimeInVehicle updater = new UpdateMaxTimeInVehicle(stateManager,latestStartId,vrp.getTransportCosts(),vrp.getActivityCosts());
//        stateManager.addStateUpdater(updater);
//        stateManager.informInsertionStarts(Arrays.asList(r),new ArrayList<Job>());
//
//        MaxTimeInVehicleConstraint constraint = new MaxTimeInVehicleConstraint(vrp.getTransportCosts(), vrp.getActivityCosts(), latestStartId, stateManager);
//        JobInsertionContext c = new JobInsertionContext(r,shipment,v,r.getDriver(),0.);
//        List<AbstractActivity> acts = vrp.getActivities(shipment);
//        c.getAssociatedActivities().add(acts.get(0));
//        c.getAssociatedActivities().add(acts.get(1));
//
//
//        Assert.assertEquals(HardActivityConstraint.ConstraintsStatus.NOT_FULFILLED, constraint.fulfilled(c, r.getStart(), acts.get(0), r.getActivities().get(0), 0));
//        Assert.assertEquals(HardActivityConstraint.ConstraintsStatus.NOT_FULFILLED, constraint.fulfilled(c, r.getActivities().get(0), acts.get(0), r.getActivities().get(1), 10));
//        Assert.assertEquals(HardActivityConstraint.ConstraintsStatus.FULFILLED, constraint.fulfilled(c, r.getActivities().get(1), acts.get(0), r.getEnd(), 30));
////        Assert.assertEquals(HardActivityConstraint.ConstraintsStatus.FULFILLED, constraint.fulfilled(c, r.getActivities().get(0), acts.get(0), r.getActivities().get(1), 10));
////        Assert.assertEquals(HardActivityConstraint.ConstraintsStatus.FULFILLED, constraint.fulfilled(c, r.getActivities().get(1), acts.get(0), r.getEnd(), 40));
//    }

    @Test
    public void whenPickupIsInsertedAt0_insertingDeliveryShipmentShouldFailWhereConstraintIsBroken(){
        ini(25d);
        VehicleRoute r = VehicleRoute.Builder.newInstance(v).setJobActivityFactory(vrp.getJobActivityFactory())
            .addDelivery(d1).addDelivery(d2).build();

        StateManager stateManager = new StateManager(vrp);
        StateId latestStartId = stateManager.createStateId("latest-start-id");

        Map<String,Double> maxTimes = new HashMap<>();
        maxTimes.put("shipment",25d);
        UpdateMaxTimeInVehicle updater = new UpdateMaxTimeInVehicle(stateManager,latestStartId,vrp.getTransportCosts(), vrp.getActivityCosts());
        stateManager.addStateUpdater(updater);
        stateManager.informInsertionStarts(Arrays.asList(r),new ArrayList<Job>());

        MaxTimeInVehicleConstraint constraint = new MaxTimeInVehicleConstraint(vrp.getTransportCosts(),vrp.getActivityCosts() , latestStartId, stateManager);
        JobInsertionContext c = new JobInsertionContext(r,shipment,v,r.getDriver(),0.);
        List<AbstractActivity> acts = vrp.getActivities(shipment);
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
        ini(25d);
        VehicleRoute r = VehicleRoute.Builder.newInstance(v).setJobActivityFactory(vrp.getJobActivityFactory())
            .addDelivery(d1).addDelivery(d2).build();

        StateManager stateManager = new StateManager(vrp);
        StateId latestStartId = stateManager.createStateId("latest-start-id");

        Map<String,Double> maxTimes = new HashMap<>();
        maxTimes.put("shipment",25d);
        UpdateMaxTimeInVehicle updater = new UpdateMaxTimeInVehicle(stateManager,latestStartId,vrp.getTransportCosts(), vrp.getActivityCosts());
        stateManager.addStateUpdater(updater);
        stateManager.informInsertionStarts(Arrays.asList(r),new ArrayList<Job>());

        MaxTimeInVehicleConstraint constraint = new MaxTimeInVehicleConstraint(vrp.getTransportCosts(),vrp.getActivityCosts() , latestStartId, stateManager);
        JobInsertionContext c = new JobInsertionContext(r,shipment,v,r.getDriver(),0.);
        List<AbstractActivity> acts = vrp.getActivities(shipment);
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
