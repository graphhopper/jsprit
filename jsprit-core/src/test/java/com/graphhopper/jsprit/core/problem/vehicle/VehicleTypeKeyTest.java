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

package com.graphhopper.jsprit.core.problem.vehicle;


import com.graphhopper.jsprit.core.problem.Location;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class VehicleTypeKeyTest {

    @Test
    public void typeIdentifierShouldBeEqual() {
        Vehicle v1 = VehicleImpl.Builder.newInstance("v1").setStartLocation(Location.newInstance("start")).addSkill("skill1").addSkill("skill2")
            .addSkill("skill3").build();
        Vehicle v2 = VehicleImpl.Builder.newInstance("v2").setStartLocation(Location.newInstance("start")).addSkill("skill2").addSkill("skill1")
            .addSkill("skill3").build();
        assertTrue(v1.getVehicleTypeIdentifier().equals(v2.getVehicleTypeIdentifier()));
    }

    @Test
    public void typeIdentifierShouldNotBeEqual() {
        Vehicle v1 = VehicleImpl.Builder.newInstance("v1").setStartLocation(Location.newInstance("start")).addSkill("skill1").addSkill("skill2")
            .build();
        Vehicle v2 = VehicleImpl.Builder.newInstance("v2").setStartLocation(Location.newInstance("start")).addSkill("skill2").addSkill("skill1")
            .addSkill("skill3").build();
        assertFalse(v1.getVehicleTypeIdentifier().equals(v2.getVehicleTypeIdentifier()));
    }
}
