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

package com.graphhopper.jsprit.core.algorithm.ruin.distance;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.job.Shipment;
import com.graphhopper.jsprit.core.util.EuclideanCosts;
import org.junit.Assert;
import org.junit.Test;

public class AvgServiceAndShipmentDistanceTest {

    @Test
    public void avgDistanceBetweenTwoServicesTest() {
        Service s1 = Service.Builder.newInstance("s1").setLocation(Location.newInstance(10, 0)).build();
        Service s2 = Service.Builder.newInstance("s2").setLocation(Location.newInstance(20, 0)).build();
        AvgServiceAndShipmentDistance distance = new AvgServiceAndShipmentDistance(new EuclideanCosts());
        Assert.assertEquals(10d, distance.getDistance(s1, s2), 0.01);
    }

    @Test
    public void avgDistanceBetweenServiceAndShipmentTest() {
        Service s1 = Service.Builder.newInstance("s1").setLocation(Location.newInstance(10, 0)).build();
        Shipment shipment = Shipment.Builder.newInstance("shipment").setPickupLocation(Location.newInstance(20, 0)).setDeliveryLocation(Location.newInstance(30, 0)).build();
        AvgServiceAndShipmentDistance distance = new AvgServiceAndShipmentDistance(new EuclideanCosts());
        Assert.assertEquals(15d, distance.getDistance(s1, shipment), 0.01);
    }

    @Test
    public void avgDistanceBetweenShipmentAndShipmentTest() {
        Shipment shipment1 = Shipment.Builder.newInstance("shipment1").setPickupLocation(Location.newInstance(20, 0)).setDeliveryLocation(Location.newInstance(30, 0)).build();
        Shipment shipment2 = Shipment.Builder.newInstance("shipment2").setPickupLocation(Location.newInstance(40, 0)).setDeliveryLocation(Location.newInstance(50, 0)).build();
        AvgServiceAndShipmentDistance distance = new AvgServiceAndShipmentDistance(new EuclideanCosts());
        Assert.assertEquals(20d, distance.getDistance(shipment1, shipment2), 0.01);
    }
}
