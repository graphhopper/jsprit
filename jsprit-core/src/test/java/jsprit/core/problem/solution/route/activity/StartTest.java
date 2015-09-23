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

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class StartTest {

    @Test
    public void whenCallingCapacity_itShouldReturnEmptyCapacity() {
        Start start = Start.newInstance("loc", 0., 0.);
        assertEquals(0, start.getSize().get(0));
    }

    @Test
    public void whenStartIsIniWithEarliestStart_itShouldBeSetCorrectly() {
        Start start = Start.newInstance("loc", 1., 2.);
        assertEquals(1., start.getTheoreticalEarliestOperationStartTime(), 0.01);
    }

    @Test
    public void whenStartIsIniWithLatestStart_itShouldBeSetCorrectly() {
        Start start = Start.newInstance("loc", 1., 2.);
        assertEquals(2., start.getTheoreticalLatestOperationStartTime(), 0.01);
    }

    @Test
    public void whenSettingStartEndTime_itShouldBeSetCorrectly() {
        Start start = Start.newInstance("loc", 1., 2.);
        start.setEndTime(4.0);
        assertEquals(4., start.getEndTime(), 0.01);
    }

    @Test
    public void whenSettingLocationId_itShouldBeSetCorrectly() {
        Start start = Start.newInstance("loc", 1., 2.);
        start.setLocationId("newLoc");
        assertEquals("newLoc", start.getLocation().getId());
    }

    @Test
    public void whenSettingEarliestStart_itShouldBeSetCorrectly() {
        Start start = Start.newInstance("loc", 1., 2.);
        start.setTheoreticalEarliestOperationStartTime(5.);
        assertEquals(5., start.getTheoreticalEarliestOperationStartTime(), 0.01);
    }

    @Test
    public void whenSettingLatestStart_itShouldBeSetCorrectly() {
        Start start = Start.newInstance("loc", 1., 2.);
        start.setTheoreticalLatestOperationStartTime(5.);
        assertEquals(5., start.getTheoreticalLatestOperationStartTime(), 0.01);
    }

    @Test
    public void whenCopyingStart_itShouldBeDoneCorrectly() {
        Start start = Start.newInstance("loc", 1., 2.);
        start.setTheoreticalEarliestOperationStartTime(3.);
        start.setTheoreticalLatestOperationStartTime(5.);

        Start copy = Start.copyOf(start);
        assertEquals(3., copy.getTheoreticalEarliestOperationStartTime(), 0.01);
        assertEquals(5., copy.getTheoreticalLatestOperationStartTime(), 0.01);
        assertEquals("loc", copy.getLocation().getId());
        assertTrue(copy != start);
    }

}
