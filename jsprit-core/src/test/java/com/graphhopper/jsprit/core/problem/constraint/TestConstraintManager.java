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

import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.solution.route.state.RouteAndActivityStateGetter;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class TestConstraintManager {

    @Test
    public void whenGettingConstraintsViaConstructor_theyShouldBeResolvedCorrectly() {
        List<Constraint> constraints = new ArrayList<Constraint>();
        constraints.add(new ServiceDeliveriesFirstConstraint());
        constraints.add(mock(HardRouteConstraint.class));
        ConstraintManager cManager = new ConstraintManager(mock(VehicleRoutingProblem.class), mock(RouteAndActivityStateGetter.class), constraints);
        assertEquals(2, cManager.getConstraints().size());
    }

    @Test
    public void whenGettingConstraintsViaConstructorAndAtLeastOneConstraintCannotBeResolved_itShouldOnlyAddTheKnownConstraints() {
        List<Constraint> constraints = new ArrayList<Constraint>();
        constraints.add(new ServiceDeliveriesFirstConstraint());
        constraints.add(mock(Constraint.class));
        ConstraintManager cManager = new ConstraintManager(mock(VehicleRoutingProblem.class), mock(RouteAndActivityStateGetter.class), constraints);
        assertEquals(1, cManager.getConstraints().size());
    }

    @Test
    public void whenAddingSoftRouteConstraint_managerShouldHaveIt() {
        SoftRouteConstraint c = mock(SoftRouteConstraint.class);
        ConstraintManager man = new ConstraintManager(mock(VehicleRoutingProblem.class), mock(RouteAndActivityStateGetter.class));
        assertEquals(0, man.getConstraints().size());
        man.addConstraint(c);
        assertEquals(1, man.getConstraints().size());
    }

    @Test
    public void whenAddingTwoSoftRouteConstraint_managerShouldHaveIt() {
        SoftRouteConstraint c1 = mock(SoftRouteConstraint.class);
        SoftRouteConstraint c2 = mock(SoftRouteConstraint.class);
        ConstraintManager man = new ConstraintManager(mock(VehicleRoutingProblem.class), mock(RouteAndActivityStateGetter.class));
        assertEquals(0, man.getConstraints().size());
        man.addConstraint(c1);
        man.addConstraint(c2);
        assertEquals(2, man.getConstraints().size());
    }

    @Test
    public void whenAddingSoftActivityConstraint_managerShouldHaveIt() {
        SoftActivityConstraint c = mock(SoftActivityConstraint.class);
        ConstraintManager man = new ConstraintManager(mock(VehicleRoutingProblem.class), mock(RouteAndActivityStateGetter.class));
        assertEquals(0, man.getConstraints().size());
        man.addConstraint(c);
        assertEquals(1, man.getConstraints().size());
    }

    @Test
    public void whenAddingTwoSoftActivityConstraints_managerShouldHaveIt() {
        SoftActivityConstraint c1 = mock(SoftActivityConstraint.class);
        SoftActivityConstraint c2 = mock(SoftActivityConstraint.class);
        ConstraintManager man = new ConstraintManager(mock(VehicleRoutingProblem.class), mock(RouteAndActivityStateGetter.class));
        assertEquals(0, man.getConstraints().size());
        man.addConstraint(c1);
        man.addConstraint(c2);
        assertEquals(2, man.getConstraints().size());
    }

}
