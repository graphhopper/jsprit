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
import com.graphhopper.jsprit.core.util.Coordinate;
import com.graphhopper.jsprit.core.util.CrowFlyCosts;
import com.graphhopper.jsprit.core.util.Locations;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Average Job Distance Test")
class AverageJobDistanceTest {

    private CrowFlyCosts routingCosts;

    @BeforeEach
    void doBefore() {
        Locations locations = new Locations() {

            @Override
            public Coordinate getCoord(String id) {
                // assume: locationId="x,y"
                String[] splitted = id.split(",");
                return Coordinate.newInstance(Double.parseDouble(splitted[0]), Double.parseDouble(splitted[1]));
            }
        };
        routingCosts = new CrowFlyCosts(locations);
    }

    @Test
    @DisplayName("Distance Of Two Equal Shipments Should Be Smaller Than Any Other Distance")
    void distanceOfTwoEqualShipmentsShouldBeSmallerThanAnyOtherDistance() {
        Shipment s1 = Shipment.Builder.newInstance("s1").addSizeDimension(0, 1).setPickupLocation(Location.Builder.newInstance().setId("0,0").build()).setDeliveryLocation(Location.newInstance("10,10")).build();
        Shipment s2 = Shipment.Builder.newInstance("s2").addSizeDimension(0, 1).setPickupLocation(Location.Builder.newInstance().setId("0,0").build()).setDeliveryLocation(Location.newInstance("10,10")).build();
        double dist = new AvgServiceAndShipmentDistance(routingCosts).getDistance(s1, s2);
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                Shipment other1 = Shipment.Builder.newInstance("s1").addSizeDimension(0, 1).setPickupLocation(Location.Builder.newInstance().setId("0,0").build()).setDeliveryLocation(Location.newInstance(i + "," + j)).build();
                Shipment other2 = Shipment.Builder.newInstance("s2").addSizeDimension(0, 1).setPickupLocation(Location.Builder.newInstance().setId("0,0").build()).setDeliveryLocation(Location.newInstance("10,10")).build();
                double dist2 = new AvgServiceAndShipmentDistance(routingCosts).getDistance(other1, other2);
                assertTrue(dist <= dist2 + dist2 * 0.001);
            }
        }
    }

    @Test
    @DisplayName("When Services Have Same Location _ distance Should Be Zero")
    void whenServicesHaveSameLocation_distanceShouldBeZero() {
        Service s1 = Service.Builder.newInstance("s1").addSizeDimension(0, 1).setLocation(Location.newInstance("10,0")).build();
        Service s2 = Service.Builder.newInstance("s2").addSizeDimension(0, 1).setLocation(Location.newInstance("10,0")).build();
        double dist = new AvgServiceAndShipmentDistance(routingCosts).getDistance(s1, s2);
        assertEquals(0.0, dist, 0.01);
    }
}
