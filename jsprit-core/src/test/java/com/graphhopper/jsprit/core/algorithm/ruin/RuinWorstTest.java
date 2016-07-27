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

package com.graphhopper.jsprit.core.algorithm.ruin;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.job.Shipment;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.util.Coordinate;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by schroeder on 30/01/15.
 */
public class RuinWorstTest {

    @Test
    public void itShouldRemoveCorrectNumber() {
        Service s1 = Service.Builder.newInstance("s1")
            .setLocation(Location.Builder.newInstance().setCoordinate(Coordinate.newInstance(1, 1)).build()).build();
        Service s2 = Service.Builder.newInstance("s2")
            .setLocation(Location.Builder.newInstance().setCoordinate(Coordinate.newInstance(3, 1)).build()).build();
        Service s3 = Service.Builder.newInstance("s3")
            .setLocation(Location.Builder.newInstance().setCoordinate(Coordinate.newInstance(10, 10)).build()).build();
        VehicleImpl v = VehicleImpl.Builder.newInstance("v")
            .setStartLocation(Location.Builder.newInstance().setCoordinate(Coordinate.newInstance(0, 0)).build()).build();
        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addJob(s1).addJob(s2).addJob(s3).addVehicle(v).build();
        RuinWorst worst = new RuinWorst(vrp, 1);

        VehicleRoute route = VehicleRoute.Builder.newInstance(v).addService(s1).addService(s2).addService(s3).setJobActivityFactory(vrp.getJobActivityFactory()).build();
        Collection<Job> unassigned = worst.ruinRoutes(Arrays.asList(route));
        assertEquals(1, unassigned.size());

    }

    @Test
    public void itShouldRemoveWorst() {
        Service s1 = Service.Builder.newInstance("s1")
            .setLocation(Location.Builder.newInstance().setCoordinate(Coordinate.newInstance(1, 1)).build()).build();
        Service s2 = Service.Builder.newInstance("s2")
            .setLocation(Location.Builder.newInstance().setCoordinate(Coordinate.newInstance(3, 1)).build()).build();
        Service s3 = Service.Builder.newInstance("s3")
            .setLocation(Location.Builder.newInstance().setCoordinate(Coordinate.newInstance(10, 10)).build()).build();
        VehicleImpl v = VehicleImpl.Builder.newInstance("v")
            .setStartLocation(Location.Builder.newInstance().setCoordinate(Coordinate.newInstance(0, 0)).build()).build();
        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addJob(s1).addJob(s2).addJob(s3).addVehicle(v).build();
        RuinWorst worst = new RuinWorst(vrp, 1);

        VehicleRoute route = VehicleRoute.Builder.newInstance(v).addService(s1).addService(s2).addService(s3).setJobActivityFactory(vrp.getJobActivityFactory()).build();
        Collection<Job> unassigned = worst.ruinRoutes(Arrays.asList(route));
        assertEquals(s3, unassigned.iterator().next());

    }

    @Test
    public void itShouldRemoveWorstTwo() {
        Service s1 = Service.Builder.newInstance("s1")
            .setLocation(Location.Builder.newInstance().setCoordinate(Coordinate.newInstance(1, 1)).build()).build();
        Service s2 = Service.Builder.newInstance("s2")
            .setLocation(Location.Builder.newInstance().setCoordinate(Coordinate.newInstance(3, 1)).build()).build();
        Service s3 = Service.Builder.newInstance("s3")
            .setLocation(Location.Builder.newInstance().setCoordinate(Coordinate.newInstance(10, 10)).build()).build();
        VehicleImpl v = VehicleImpl.Builder.newInstance("v")
            .setStartLocation(Location.Builder.newInstance().setCoordinate(Coordinate.newInstance(0, 0)).build()).build();
        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addJob(s1).addJob(s2).addJob(s3).addVehicle(v).build();
        RuinWorst worst = new RuinWorst(vrp, 1);
        worst.setRuinShareFactory(new RuinShareFactory() {
            @Override
            public int createNumberToBeRemoved() {
                return 2;
            }
        });

        VehicleRoute route = VehicleRoute.Builder.newInstance(v).addService(s1).addService(s2).addService(s3).setJobActivityFactory(vrp.getJobActivityFactory()).build();
        Collection<Job> unassigned = worst.ruinRoutes(Arrays.asList(route));

        assertTrue(unassigned.size() == 2);
        assertTrue(unassigned.contains(s2));
        assertTrue(unassigned.contains(s3));

    }

    @Test
    public void itShouldRemoveShipment() {
        Service s1 = Service.Builder.newInstance("s1")
            .setLocation(Location.Builder.newInstance().setCoordinate(Coordinate.newInstance(1, 1)).build()).build();
        Service s2 = Service.Builder.newInstance("s2")
            .setLocation(Location.Builder.newInstance().setCoordinate(Coordinate.newInstance(3, 1)).build()).build();
        Service s3 = Service.Builder.newInstance("s3")
            .setLocation(Location.Builder.newInstance().setCoordinate(Coordinate.newInstance(10, 10)).build()).build();
        Shipment shipment = Shipment.Builder.newInstance("ship1")
            .setPickupLocation(Location.Builder.newInstance().setCoordinate(Coordinate.newInstance(2, 2)).build())
            .setDeliveryLocation(Location.Builder.newInstance().setCoordinate(Coordinate.newInstance(9, 9)).build()).build();
        VehicleImpl v = VehicleImpl.Builder.newInstance("v")
            .setStartLocation(Location.Builder.newInstance().setCoordinate(Coordinate.newInstance(0, 0)).build()).build();
        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance()
            .addJob(shipment).addJob(s1).addJob(s2).addJob(s3).addVehicle(v).build();
        RuinWorst worst = new RuinWorst(vrp, 1);
        worst.setRuinShareFactory(new RuinShareFactory() {
            @Override
            public int createNumberToBeRemoved() {
                return 1;
            }
        });

        VehicleRoute route = VehicleRoute.Builder.newInstance(v)
            .addPickup(shipment).addService(s1).addService(s2).addService(s3).addDelivery(shipment)
            .setJobActivityFactory(vrp.getJobActivityFactory()).build();
        Collection<Job> unassigned = worst.ruinRoutes(Arrays.asList(route));

        assertTrue(unassigned.size() == 1);
        assertTrue(unassigned.contains(shipment));

    }

    @Test
    public void itShouldRemoveShipmentFromSecondRoute() {
        Service s1 = Service.Builder.newInstance("s1")
            .setLocation(Location.Builder.newInstance().setCoordinate(Coordinate.newInstance(1, 1)).build()).build();
        Service s2 = Service.Builder.newInstance("s2")
            .setLocation(Location.Builder.newInstance().setCoordinate(Coordinate.newInstance(3, 1)).build()).build();
        Service s3 = Service.Builder.newInstance("s3")
            .setLocation(Location.Builder.newInstance().setCoordinate(Coordinate.newInstance(10, 10)).build()).build();
        Shipment shipment = Shipment.Builder.newInstance("ship1")
            .setPickupLocation(Location.Builder.newInstance().setCoordinate(Coordinate.newInstance(3, 1)).build())
            .setDeliveryLocation(Location.Builder.newInstance().setCoordinate(Coordinate.newInstance(10, 10.1)).build()).build();
        VehicleImpl v = VehicleImpl.Builder.newInstance("v")
            .setStartLocation(Location.Builder.newInstance().setCoordinate(Coordinate.newInstance(0, 0)).build()).build();
        VehicleImpl v2 = VehicleImpl.Builder.newInstance("v2")
            .setStartLocation(Location.Builder.newInstance().setCoordinate(Coordinate.newInstance(0, 0)).build()).build();
        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance()
            .addJob(shipment).addJob(s1).addJob(s2).addJob(s3).addVehicle(v).addVehicle(v2).build();
        RuinWorst worst = new RuinWorst(vrp, 1);
        worst.setRuinShareFactory(new RuinShareFactory() {
            @Override
            public int createNumberToBeRemoved() {
                return 1;
            }
        });

        VehicleRoute route1 = VehicleRoute.Builder.newInstance(v)
            .addService(s1).addService(s2).addService(s3)
            .setJobActivityFactory(vrp.getJobActivityFactory()).build();
        VehicleRoute route2 = VehicleRoute.Builder.newInstance(v2)
            .addPickup(shipment).addDelivery(shipment).build();
        Collection<Job> unassigned = worst.ruinRoutes(Arrays.asList(route1, route2));

        assertTrue(unassigned.size() == 1);
        assertTrue(unassigned.contains(shipment));

    }

    @Test
    public void itShouldRemoveServiceAndShipmentFromSecondRoute() {
        Service s1 = Service.Builder.newInstance("s1")
            .setLocation(Location.Builder.newInstance().setCoordinate(Coordinate.newInstance(1, 1)).build()).build();
        Service s2 = Service.Builder.newInstance("s2")
            .setLocation(Location.Builder.newInstance().setCoordinate(Coordinate.newInstance(3, 1)).build()).build();
        Service s3 = Service.Builder.newInstance("s3")
            .setLocation(Location.Builder.newInstance().setCoordinate(Coordinate.newInstance(10, 10)).build()).build();
        Shipment shipment = Shipment.Builder.newInstance("ship1")
            .setPickupLocation(Location.Builder.newInstance().setCoordinate(Coordinate.newInstance(3, 1)).build())
            .setDeliveryLocation(Location.Builder.newInstance().setCoordinate(Coordinate.newInstance(10, 10.1)).build()).build();
        VehicleImpl v = VehicleImpl.Builder.newInstance("v")
            .setStartLocation(Location.Builder.newInstance().setCoordinate(Coordinate.newInstance(0, 0)).build()).build();
        VehicleImpl v2 = VehicleImpl.Builder.newInstance("v2")
            .setStartLocation(Location.Builder.newInstance().setCoordinate(Coordinate.newInstance(0, 0)).build()).build();
        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance()
            .addJob(shipment).addJob(s1).addJob(s2).addJob(s3).addVehicle(v).addVehicle(v2).build();
        RuinWorst worst = new RuinWorst(vrp, 1);
        worst.setRuinShareFactory(new RuinShareFactory() {
            @Override
            public int createNumberToBeRemoved() {
                return 2;
            }
        });

        VehicleRoute route1 = VehicleRoute.Builder.newInstance(v)
            .addService(s1).addService(s2).addService(s3)
            .setJobActivityFactory(vrp.getJobActivityFactory()).build();
        VehicleRoute route2 = VehicleRoute.Builder.newInstance(v2)
            .addPickup(shipment).addDelivery(shipment).build();
        Collection<Job> unassigned = worst.ruinRoutes(Arrays.asList(route1, route2));

        assertTrue(unassigned.size() == 2);
        assertTrue(unassigned.contains(shipment));
        assertTrue(unassigned.contains(s3));

    }


}
