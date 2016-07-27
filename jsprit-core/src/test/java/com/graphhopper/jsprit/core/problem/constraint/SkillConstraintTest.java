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

package com.graphhopper.jsprit.core.problem.constraint;

import com.graphhopper.jsprit.core.algorithm.state.StateManager;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.misc.JobInsertionContext;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleType;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class SkillConstraintTest {

    private HardRouteConstraint skillConstraint;

    private VehicleRoute route;

    private VehicleImpl vehicle;

    private VehicleImpl vehicle2;

    private VehicleRoutingProblem vrp;

    @Before
    public void doBefore() {
        VehicleType type = VehicleTypeImpl.Builder.newInstance("t").build();
        vehicle = VehicleImpl.Builder.newInstance("v").addSkill("skill1").addSkill("skill2").addSkill("skill3").addSkill("skill4").setStartLocation(Location.newInstance("start")).setType(type).build();
        vehicle2 = VehicleImpl.Builder.newInstance("v2").addSkill("skill4").addSkill("skill5").setStartLocation(Location.newInstance("start")).setType(type).build();

        Service service = Service.Builder.newInstance("s").setLocation(Location.newInstance("loc")).addRequiredSkill("skill1").build();
        Service service2 = Service.Builder.newInstance("s2").setLocation(Location.newInstance("loc")).addRequiredSkill("skill1").addRequiredSkill("skill2").addRequiredSkill("skill3").build();

        Service service3 = Service.Builder.newInstance("s3").setLocation(Location.newInstance("loc")).addRequiredSkill("skill4").addRequiredSkill("skill5").build();
        Service service4 = Service.Builder.newInstance("s4").setLocation(Location.newInstance("loc")).addRequiredSkill("skill1").build();

        vrp = VehicleRoutingProblem.Builder.newInstance().addVehicle(vehicle).addVehicle(vehicle2).addJob(service)
            .addJob(service2).addJob(service3).addJob(service4).build();

        route = VehicleRoute.Builder.newInstance(vehicle).setJobActivityFactory(vrp.getJobActivityFactory()).addService(service).addService(service2).build();

        StateManager stateManager = new StateManager(vrp);
        stateManager.updateSkillStates();
        stateManager.informInsertionStarts(Arrays.asList(route), null);

        skillConstraint = new HardSkillConstraint(stateManager);
    }

    @Test
    public void whenJobToBeInsertedRequiresSkillsThatNewVehicleDoesNotHave_itShouldReturnFalse() {
        JobInsertionContext insertionContext = new JobInsertionContext(route, vrp.getJobs().get("s3"), vehicle, route.getDriver(), 0.);
        assertFalse(skillConstraint.fulfilled(insertionContext));
    }

    @Test
    public void whenJobToBeInsertedRequiresSkillsThatVehicleHave_itShouldReturnTrue() {
        JobInsertionContext insertionContext = new JobInsertionContext(route, vrp.getJobs().get("s4"), vehicle, route.getDriver(), 0.);
        assertTrue(skillConstraint.fulfilled(insertionContext));
    }

    @Test
    public void whenRouteToBeOvertakenRequiresSkillsThatVehicleDoesNotHave_itShouldReturnFalse() {
        JobInsertionContext insertionContext = new JobInsertionContext(route, vrp.getJobs().get("s3"), vehicle2, route.getDriver(), 0.);
        assertFalse(skillConstraint.fulfilled(insertionContext));
    }

    @Test
    public void whenRouteToBeOvertakenRequiresSkillsThatVehicleDoesNotHave2_itShouldReturnFalse() {
        JobInsertionContext insertionContext = new JobInsertionContext(route, vrp.getJobs().get("s4"), vehicle2, route.getDriver(), 0.);
        assertFalse(skillConstraint.fulfilled(insertionContext));
    }

    @Test
    public void whenRouteToBeOvertakenRequiresSkillsThatVehicleDoesHave_itShouldReturnTrue() {
        JobInsertionContext insertionContext = new JobInsertionContext(route, vrp.getJobs().get("s4"), vehicle, route.getDriver(), 0.);
        assertTrue(skillConstraint.fulfilled(insertionContext));
    }

}
