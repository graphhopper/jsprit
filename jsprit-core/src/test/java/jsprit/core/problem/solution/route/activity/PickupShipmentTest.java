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
package jsprit.core.problem.solution.route.activity;

import jsprit.core.problem.Location;
import jsprit.core.problem.job.Shipment;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PickupShipmentTest {

    private PickupShipment pickup;

    @Before
    public void doBefore() {
        Shipment shipment = Shipment.Builder.newInstance("shipment").setPickupLocation(Location.Builder.newInstance().setId("pickupLoc").build())
            .setDeliveryLocation(Location.newInstance("deliveryLoc"))
            .setPickupTimeWindow(TimeWindow.newInstance(1., 2.))
            .setDeliveryTimeWindow(TimeWindow.newInstance(3., 4.))
            .addSizeDimension(0, 10).addSizeDimension(1, 100).addSizeDimension(2, 1000).build();
        pickup = new PickupShipment(shipment);
    }

    @Test
    public void whenCallingCapacity_itShouldReturnCorrectCapacity() {
        assertEquals(10, pickup.getSize().get(0));
        assertEquals(100, pickup.getSize().get(1));
        assertEquals(1000, pickup.getSize().get(2));
    }

    @Test
    public void whenStartIsIniWithEarliestStart_itShouldBeSetCorrectly() {
        assertEquals(1., pickup.getTheoreticalEarliestOperationStartTime(), 0.01);
    }

    @Test
    public void whenStartIsIniWithLatestStart_itShouldBeSetCorrectly() {
        assertEquals(2., pickup.getTheoreticalLatestOperationStartTime(), 0.01);
    }

    @Test
    public void whenSettingArrTime_itShouldBeSetCorrectly() {
        pickup.setArrTime(4.0);
        assertEquals(4., pickup.getArrTime(), 0.01);
    }

    @Test
    public void whenSettingEndTime_itShouldBeSetCorrectly() {
        pickup.setEndTime(5.0);
        assertEquals(5., pickup.getEndTime(), 0.01);
    }

    @Test
    public void whenIniLocationId_itShouldBeSetCorrectly() {
        assertEquals("pickupLoc", pickup.getLocation().getId());
    }

    @Test
    public void whenCopyingStart_itShouldBeDoneCorrectly() {
        PickupShipment copy = (PickupShipment) pickup.duplicate();
        assertEquals(1., copy.getTheoreticalEarliestOperationStartTime(), 0.01);
        assertEquals(2., copy.getTheoreticalLatestOperationStartTime(), 0.01);
        assertEquals("pickupLoc", copy.getLocation().getId());
        assertEquals(10, copy.getSize().get(0));
        assertEquals(100, copy.getSize().get(1));
        assertEquals(1000, copy.getSize().get(2));
        assertTrue(copy != pickup);
    }


    @Test
    public void whenGettingCapacity_itShouldReturnItCorrectly() {
        Shipment shipment = Shipment.Builder.newInstance("s").setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build()).setDeliveryLocation(Location.newInstance("delLoc"))
            .addSizeDimension(0, 10).addSizeDimension(1, 100).build();
        PickupShipment pick = new PickupShipment(shipment);
        assertEquals(10, pick.getSize().get(0));
        assertEquals(100, pick.getSize().get(1));
    }

}
