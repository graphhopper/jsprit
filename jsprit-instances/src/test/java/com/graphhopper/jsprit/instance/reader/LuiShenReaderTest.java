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
package com.graphhopper.jsprit.instance.reader;

import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem.FleetSize;
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
