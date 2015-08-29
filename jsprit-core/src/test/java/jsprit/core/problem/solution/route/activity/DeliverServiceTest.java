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
import jsprit.core.problem.job.Delivery;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DeliverServiceTest {

    private Delivery service;

    private DeliverService deliver;

    @Before
    public void doBefore() {
        service = (Delivery) Delivery.Builder.newInstance("service").setLocation(Location.newInstance("loc")).
            setTimeWindow(TimeWindow.newInstance(1., 2.)).
            addSizeDimension(0, 10).addSizeDimension(1, 100).addSizeDimension(2, 1000).build();
        deliver = new DeliverService(service);
    }

    @Test
    public void whenCallingCapacity_itShouldReturnCorrectCapacity() {
        assertEquals(-10, deliver.getSize().get(0));
        assertEquals(-100, deliver.getSize().get(1));
        assertEquals(-1000, deliver.getSize().get(2));
    }

    @Test
    public void whenStartIsIniWithEarliestStart_itShouldBeSetCorrectly() {
        assertEquals(1., deliver.getTheoreticalEarliestOperationStartTime(), 0.01);
    }

    @Test
    public void whenStartIsIniWithLatestStart_itShouldBeSetCorrectly() {
        assertEquals(2., deliver.getTheoreticalLatestOperationStartTime(), 0.01);
    }

    @Test
    public void whenSettingArrTime_itShouldBeSetCorrectly() {
        deliver.setArrTime(4.0);
        assertEquals(4., deliver.getArrTime(), 0.01);
    }

    @Test
    public void whenSettingEndTime_itShouldBeSetCorrectly() {
        deliver.setEndTime(5.0);
        assertEquals(5., deliver.getEndTime(), 0.01);
    }

    @Test
    public void whenIniLocationId_itShouldBeSetCorrectly() {
        assertEquals("loc", deliver.getLocation().getId());
    }

    @Test
    public void whenCopyingStart_itShouldBeDoneCorrectly() {
        DeliverService copy = (DeliverService) deliver.duplicate();
        assertEquals(1., copy.getTheoreticalEarliestOperationStartTime(), 0.01);
        assertEquals(2., copy.getTheoreticalLatestOperationStartTime(), 0.01);
        assertEquals("loc", copy.getLocation().getId());
        assertEquals(-10, copy.getSize().get(0));
        assertEquals(-100, copy.getSize().get(1));
        assertEquals(-1000, copy.getSize().get(2));
        assertTrue(copy != deliver);
    }

}
