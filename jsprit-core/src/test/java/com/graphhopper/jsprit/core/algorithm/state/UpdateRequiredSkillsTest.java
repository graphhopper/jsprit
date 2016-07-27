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

package com.graphhopper.jsprit.core.algorithm.state;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.Skills;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleType;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests skill updater
 */
public class UpdateRequiredSkillsTest {

    private VehicleRoute route;

    private StateManager stateManager;

    @Before
    public void doBefore() {
        VehicleType type = VehicleTypeImpl.Builder.newInstance("t").build();
        VehicleImpl vehicle = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance("start")).setType(type).build();
        Service service = Service.Builder.newInstance("s").setLocation(Location.newInstance("loc")).addRequiredSkill("skill1").build();
        Service service2 = Service.Builder.newInstance("s2").setLocation(Location.newInstance("loc")).addRequiredSkill("skill1").addRequiredSkill("skill2").addRequiredSkill("skill3").build();
        Service service3 = Service.Builder.newInstance("s3").setLocation(Location.newInstance("loc")).addRequiredSkill("skill4").addRequiredSkill("skill5").build();
        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addVehicle(vehicle).addJob(service)
            .addJob(service2).addJob(service3).build();
        route = VehicleRoute.Builder.newInstance(vehicle).setJobActivityFactory(vrp.getJobActivityFactory()).addService(service).addService(service2).addService(service3).build();

        stateManager = new StateManager(vrp);
        stateManager.updateSkillStates();
        stateManager.informInsertionStarts(Arrays.asList(route), null);
    }

    @Test
    public void whenUpdatingRoute_skillsAtRouteLevelShouldContainAllSkills() {
        Skills skills = stateManager.getRouteState(route, InternalStates.SKILLS, Skills.class);
        assertNotNull(skills);
        Assert.assertEquals(5, skills.values().size());
        assertTrue(skills.containsSkill("skill1"));
        assertTrue(skills.containsSkill("skill2"));
        assertTrue(skills.containsSkill("skill3"));
        assertTrue(skills.containsSkill("skill4"));
        assertTrue(skills.containsSkill("skill5"));
    }


}
