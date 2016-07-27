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

import com.graphhopper.jsprit.core.problem.misc.JobInsertionContext;
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
