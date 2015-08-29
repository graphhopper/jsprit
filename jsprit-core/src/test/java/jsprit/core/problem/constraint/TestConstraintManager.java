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
package jsprit.core.problem.constraint;

import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.solution.route.state.RouteAndActivityStateGetter;
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
