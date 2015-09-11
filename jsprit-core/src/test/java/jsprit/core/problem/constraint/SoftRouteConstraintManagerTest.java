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
package jsprit.core.problem.constraint;

import jsprit.core.problem.misc.JobInsertionContext;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SoftRouteConstraintManagerTest {

    @Test
    public void whenAddingSoftRouteConstraint_managerShouldHaveIt() {
        SoftRouteConstraint c = mock(SoftRouteConstraint.class);
        SoftRouteConstraintManager man = new SoftRouteConstraintManager();
        assertEquals(0, man.getConstraints().size());
        man.addConstraint(c);
        assertEquals(1, man.getConstraints().size());
    }

    @Test
    public void whenAddingTwoSoftRouteConstraint_managerShouldHaveIt() {
        SoftRouteConstraint c1 = mock(SoftRouteConstraint.class);
        SoftRouteConstraint c2 = mock(SoftRouteConstraint.class);
        SoftRouteConstraintManager man = new SoftRouteConstraintManager();
        assertEquals(0, man.getConstraints().size());
        man.addConstraint(c1);
        man.addConstraint(c2);
        assertEquals(2, man.getConstraints().size());
    }

    @Test
    public void whenAddingTwoSoftRouteConstraint_managerShouldSumCostsCorrectly() {
        SoftRouteConstraint c1 = mock(SoftRouteConstraint.class);
        JobInsertionContext iContext = mock(JobInsertionContext.class);
        when(c1.getCosts(iContext)).thenReturn(1.0);
        SoftRouteConstraint c2 = mock(SoftRouteConstraint.class);
        when(c2.getCosts(iContext)).thenReturn(2.0);
        SoftRouteConstraintManager man = new SoftRouteConstraintManager();

        man.addConstraint(c1);
        man.addConstraint(c2);
        assertEquals(3.0, man.getCosts(iContext), 0.01);
    }
}
