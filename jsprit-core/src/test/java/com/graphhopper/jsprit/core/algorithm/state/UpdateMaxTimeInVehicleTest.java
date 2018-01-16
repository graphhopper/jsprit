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

package com.graphhopper.jsprit.core.algorithm.state;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Delivery;
import com.graphhopper.jsprit.core.problem.job.Pickup;
import com.graphhopper.jsprit.core.problem.job.Shipment;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.PickupActivity;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleType;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;

/**
 * Created by schroeder on 15/09/16.
 */
public class UpdateMaxTimeInVehicleTest {

    private VehicleRoute route;

    private VehicleRoute route2;

    private VehicleImpl vehicle;

    private VehicleImpl v;

    private VehicleImpl vehicle2;

    private VehicleRoutingProblem vrp;

    private com.graphhopper.jsprit.core.algorithm.state.UpdateMaxTimeInVehicle maxTimeInVehicleConstraint;

    private StateManager stateManager;

    private StateId minSlackId;

    private StateId openJobsId;

    @Before
    public void doBefore() {
        VehicleType type = VehicleTypeImpl.Builder.newInstance("t").build();

        v = VehicleImpl.Builder.newInstance("v0").setStartLocation(Location.newInstance(0, 0))
            .setType(type).build();

        vehicle = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance(0,0))
            .setEndLocation(Location.newInstance(0,50)).setType(type).build();

        vehicle2 = VehicleImpl.Builder.newInstance("v2").setStartLocation(Location.newInstance(0,10))
            .setEndLocation(Location.newInstance(0,40)).setType(type).build();

        Pickup service = Pickup.Builder.newInstance("s").setLocation(Location.newInstance(0, 10)).build();
        Pickup service2 = Pickup.Builder.newInstance("s2").setLocation(Location.newInstance(0, 20)).build();

        Pickup service3 = Pickup.Builder.newInstance("s3").setLocation(Location.newInstance(0, 30)).build();
        Pickup service4 = Pickup.Builder.newInstance("s4").setLocation(Location.newInstance(0, 40)).build();

        Delivery d1 = Delivery.Builder.newInstance("d1").setLocation(Location.newInstance(10,0)).build();

        Shipment shipment = Shipment.Builder.newInstance("shipment").setPickupLocation(Location.newInstance(20,0))
            .setDeliveryLocation(Location.newInstance(40,0))
            .setMaxTimeInVehicle(20d)
            .build();

        Delivery d2 = Delivery.Builder.newInstance("d2").setLocation(Location.newInstance(30,0)).setServiceTime(10).build();


        vrp = VehicleRoutingProblem.Builder.newInstance().addVehicle(v).addVehicle(vehicle).addVehicle(vehicle2).addJob(service)
            .addJob(service2).addJob(service3).addJob(service4)
            .addJob(d1).addJob(shipment).addJob(d2)
            .build();

        route = VehicleRoute.Builder.newInstance(vehicle).setJobActivityFactory(vrp.getJobActivityFactory())
            .addService(service).addService(service2).addService(service3).addService(service4).build();

        route2 = VehicleRoute.Builder.newInstance(v).setJobActivityFactory(vrp.getJobActivityFactory())
            .addDelivery(d1).addPickup(shipment).addDelivery(shipment).build();

        stateManager = new StateManager(vrp);
        stateManager.addStateUpdater(new UpdateActivityTimes(vrp.getTransportCosts(),vrp.getActivityCosts()));
        stateManager.informInsertionStarts(Arrays.asList(route), null);

        minSlackId = stateManager.createStateId("min-slack-id");
        openJobsId = stateManager.createStateId("open-jobs-id");

//        Map<String,Double> maxTimes = new HashMap<>();
//        maxTimes.put("s",40d);
//        maxTimes.put("shipment",20d);
        maxTimeInVehicleConstraint = new UpdateMaxTimeInVehicle(stateManager, minSlackId, vrp.getTransportCosts(), vrp.getActivityCosts(), openJobsId);
        maxTimeInVehicleConstraint.setVehiclesToUpdate(new UpdateVehicleDependentPracticalTimeWindows.VehiclesToUpdate() {
            @Override
            public Collection<Vehicle> get(VehicleRoute route) {
                return Arrays.asList((Vehicle)vehicle,(Vehicle)vehicle2,v);
            }
        });
        stateManager.addStateUpdater(maxTimeInVehicleConstraint);
    }

//    @Test
//    public void testVehicle(){
//        stateManager.informInsertionStarts(Arrays.asList(route), null);
//        for(TourActivity act : route.getActivities()){
//            String jobId = ((TourActivity.JobActivity)act).getJob().getId();
//            if(jobId.equals("s4")){
//                Double slackTime = stateManager.getActivityState(act,route.getVehicle(), minSlackId,Double.class);
//                Assert.assertEquals(40, slackTime, 0.001);
//            }
//            if(jobId.equals("s3")){
//                Double slackTime = stateManager.getActivityState(act,route.getVehicle(), minSlackId,Double.class);
//                Assert.assertEquals(30, slackTime, 0.001);
//            }
//            if(jobId.equals("s2")){
//                Double slackTime = stateManager.getActivityState(act,route.getVehicle(), minSlackId,Double.class);
//                Assert.assertEquals(20, slackTime, 0.001);
//            }
//            if(jobId.equals("s")){
//                Double slackTime = stateManager.getActivityState(act,route.getVehicle(), minSlackId,Double.class);
//                Assert.assertEquals(Double.MAX_VALUE, slackTime, 0.001);
//            }
//        }
//        Double slackTime = stateManager.getRouteState(route,route.getVehicle(), minSlackId,Double.class);
//        Assert.assertNotNull(slackTime);
//        Assert.assertEquals(50,slackTime,0.001);
//    }
//
//    @Test
//    public void testVehicle2(){
//        stateManager.informInsertionStarts(Arrays.asList(route), null);
//        for(TourActivity act : route.getActivities()){
//            String jobId = ((TourActivity.JobActivity)act).getJob().getId();
//            if(jobId.equals("s4")){
//                Double slackTime = stateManager.getActivityState(act,vehicle2, minSlackId,Double.class);
//                Assert.assertEquals(40, slackTime, 0.001);
//            }
//            if(jobId.equals("s3")){
//                Double slackTime = stateManager.getActivityState(act,vehicle2, minSlackId,Double.class);
//                Assert.assertEquals(30, slackTime, 0.001);
//            }
//            if(jobId.equals("s2")){
//                Double slackTime = stateManager.getActivityState(act,vehicle2, minSlackId,Double.class);
//                Assert.assertEquals(20, slackTime, 0.001);
//            }
//            if(jobId.equals("s")){
//                Double slackTime = stateManager.getActivityState(act,vehicle2, minSlackId,Double.class);
//                Assert.assertEquals(Double.MAX_VALUE, slackTime, 0.001);
//            }
//        }
//        Double slackTime = stateManager.getRouteState(route,vehicle2, minSlackId,Double.class);
//        Assert.assertNotNull(slackTime);
//        Assert.assertEquals(40,slackTime,0.001);
//    }

    @Test
    public void testWithShipment(){
        stateManager.informInsertionStarts(Arrays.asList(route2), null);
        for(TourActivity act : route2.getActivities()){
            String jobId = ((TourActivity.JobActivity)act).getJob().getId();
            if(jobId.equals("d1")){
                Double slackTime = stateManager.getActivityState(act, v, minSlackId, Double.class);
                Assert.assertEquals(Double.MAX_VALUE, slackTime, 0.001);
            }
            if(jobId.equals("shipment")){
                if(act instanceof PickupActivity){
                    Double slackTime = stateManager.getActivityState(act, v, minSlackId, Double.class);
                    Assert.assertEquals(Double.MAX_VALUE, slackTime, 0.001);
                }
                else{
                    Double slackTime = stateManager.getActivityState(act, v, minSlackId, Double.class);
                    Assert.assertEquals(0, slackTime, 0.001);
                }

            }
        }
    }


}
