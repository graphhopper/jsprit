/*******************************************************************************
 * Copyright (C) 2013  Stefan Schroeder
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
package jsprit.instance.reader;

import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.VehicleRoutingProblem.FleetSize;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class LuiShenReaderTest {

    VehicleRoutingProblem vrp;


    @Before
    public void doBefore() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
        new LuiShenReader(builder).read(this.getClass().getClassLoader().getResource("C101_solomon.txt").getPath(),
            this.getClass().getClassLoader().getResource("C1_LuiShenVehicles.txt").getPath(), "a");
        vrp = builder.build();
    }

    @Test
    public void testFleetSize() {
        assertEquals(FleetSize.INFINITE, vrp.getFleetSize());
    }


    @Test
    public void testNuOfTypes() {
        assertEquals(3, vrp.getTypes().size());
    }

    @Test
    public void testNuOfRepresentativeVehicles() {
        assertEquals(3, vrp.getVehicles().size());
    }

    @Test
    public void testNuOfJobs() {
        assertEquals(100, vrp.getJobs().values().size());
    }
}
