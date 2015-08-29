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
package jsprit.core.algorithm.ruin.distance;

import jsprit.core.problem.Location;
import jsprit.core.problem.job.Service;
import jsprit.core.problem.job.Shipment;
import jsprit.core.util.Coordinate;
import jsprit.core.util.CrowFlyCosts;
import jsprit.core.util.Locations;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class AverageJobDistanceTest {


    private CrowFlyCosts routingCosts;

    @Before
    public void doBefore() {
        Locations locations = new Locations() {

            @Override
            public Coordinate getCoord(String id) {
                //assume: locationId="x,y"
                String[] splitted = id.split(",");
                return Coordinate.newInstance(Double.parseDouble(splitted[0]),
                    Double.parseDouble(splitted[1]));
            }

        };
        routingCosts = new CrowFlyCosts(locations);

    }

    @Test
    public void distanceOfTwoEqualShipmentsShouldBeSmallerThanAnyOtherDistance() {
        Shipment s1 = Shipment.Builder.newInstance("s1").addSizeDimension(0, 1).setPickupLocation(Location.Builder.newInstance().setId("0,0").build()).setDeliveryLocation(Location.newInstance("10,10")).build();
        Shipment s2 = Shipment.Builder.newInstance("s2").addSizeDimension(0, 1).setPickupLocation(Location.Builder.newInstance().setId("0,0").build()).setDeliveryLocation(Location.newInstance("10,10")).build();

        double dist = new AvgServiceAndShipmentDistance(routingCosts).getDistance(s1, s2);

        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                Shipment other1 = Shipment.Builder.newInstance("s1").addSizeDimension(0, 1).setPickupLocation(Location.Builder.newInstance().setId("0,0").build()).setDeliveryLocation(Location.newInstance(i + "," + j)).build();
                Shipment other2 = Shipment.Builder.newInstance("s2").addSizeDimension(0, 1).setPickupLocation(Location.Builder.newInstance().setId("0,0").build()).setDeliveryLocation(Location.newInstance("10,10")).build();
                double dist2 = new AvgServiceAndShipmentDistance(routingCosts).getDistance(other1, other2);
                System.out.println("(" + i + "," + j + "), dist=" + dist + ", dist2=" + dist2);
                assertTrue(dist <= dist2 + dist2 * 0.001);
            }
        }
    }


    @Test
    public void whenServicesHaveSameLocation_distanceShouldBeZero() {
        Service s1 = Service.Builder.newInstance("s1").addSizeDimension(0, 1).setLocation(Location.newInstance("10,0")).build();
        Service s2 = Service.Builder.newInstance("s2").addSizeDimension(0, 1).setLocation(Location.newInstance("10,0")).build();

        double dist = new AvgServiceAndShipmentDistance(routingCosts).getDistance(s1, s2);
        assertEquals(0.0, dist, 0.01);
    }
}
