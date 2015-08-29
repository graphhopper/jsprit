/*******************************************************************************
 * Copyright (c) 2014 Stefan Schroeder.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     Stefan Schroeder - initial API and implementation
 ******************************************************************************/
package jsprit.core.problem.solution.route.activity;

import jsprit.core.problem.Location;
import jsprit.core.problem.job.Delivery;
import jsprit.core.problem.job.Pickup;
import jsprit.core.problem.job.Service;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class DefaultTourActivityFactoryTest {

    @Test
    public void whenCreatingActivityWithService_itShouldReturnPickupService() {
        DefaultTourActivityFactory factory = new DefaultTourActivityFactory();
        Service service = Service.Builder.newInstance("service").setLocation(Location.newInstance("loc")).build();
        TourActivity act = factory.createActivity(service);
        assertNotNull(act);
        assertTrue(act instanceof PickupService);
    }

    @Test
    public void whenCreatingActivityWithPickup_itShouldReturnPickupService() {
        DefaultTourActivityFactory factory = new DefaultTourActivityFactory();
        Pickup service = (Pickup) Pickup.Builder.newInstance("service").setLocation(Location.newInstance("loc")).build();
        TourActivity act = factory.createActivity(service);
        assertNotNull(act);
        assertTrue(act instanceof PickupService);
    }

    @Test
    public void whenCreatingActivityWithDelivery_itShouldReturnDeliverService() {
        DefaultTourActivityFactory factory = new DefaultTourActivityFactory();
        Delivery service = (Delivery) Delivery.Builder.newInstance("service").setLocation(Location.newInstance("loc")).build();
        TourActivity act = factory.createActivity(service);
        assertNotNull(act);
        assertTrue(act instanceof DeliverService);
    }

}
