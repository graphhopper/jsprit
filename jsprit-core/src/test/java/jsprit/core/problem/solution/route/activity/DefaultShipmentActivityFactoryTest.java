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
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class DefaultShipmentActivityFactoryTest {

    @Test
    public void whenCreatingPickupActivityWithShipment_itShouldReturnPickupShipment() {
        DefaultShipmentActivityFactory factory = new DefaultShipmentActivityFactory();
        Shipment shipment = Shipment.Builder.newInstance("s")
            .setPickupLocation(Location.Builder.newInstance().setId("pLoc").build()).setDeliveryLocation(Location.newInstance("dLoc")).build();
        TourActivity act = factory.createPickup(shipment);
        assertNotNull(act);
        assertTrue(act instanceof PickupShipment);
    }

    @Test
    public void whenCreatingDeliverActivityWithShipment_itShouldReturnDeliverShipment() {
        DefaultShipmentActivityFactory factory = new DefaultShipmentActivityFactory();
        Shipment shipment = Shipment.Builder.newInstance("s")
            .setPickupLocation(Location.Builder.newInstance().setId("pLoc").build()).setDeliveryLocation(Location.newInstance("dLoc")).build();
        TourActivity act = factory.createDelivery(shipment);
        assertNotNull(act);
        assertTrue(act instanceof DeliverShipment);
    }
}
