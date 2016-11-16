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
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SoftActivityConstraintManagerTest {

    @Test
    public void whenAddingSoftConstraint_managerShouldHaveIt() {
        SoftActivityConstraint c = mock(SoftActivityConstraint.class);
        SoftActivityConstraintManager man = new SoftActivityConstraintManager();
        assertEquals(0, man.getConstraints().size());
        man.addConstraint(c);
        assertEquals(1, man.getConstraints().size());
    }

    @Test
    public void whenAddingTwoSoftConstraints_managerShouldHaveIt() {
        SoftActivityConstraint c1 = mock(SoftActivityConstraint.class);
        SoftActivityConstraint c2 = mock(SoftActivityConstraint.class);
        SoftActivityConstraintManager man = new SoftActivityConstraintManager();
        assertEquals(0, man.getConstraints().size());
        man.addConstraint(c1);
        man.addConstraint(c2);
        assertEquals(2, man.getConstraints().size());
    }

    @Test
    public void whenAddingTwoSoftConstrainta_managerShouldSumCostsCorrectly() {
        SoftActivityConstraint c1 = mock(SoftActivityConstraint.class);
        JobInsertionContext iContext = mock(JobInsertionContext.class);
        TourActivity act_i = mock(TourActivity.class);
        TourActivity act_k = mock(TourActivity.class);
        TourActivity act_j = mock(TourActivity.class);
        when(c1.getCosts(iContext, act_i, act_k, act_j, 0.0)).thenReturn(1.0);
        SoftActivityConstraint c2 = mock(SoftActivityConstraint.class);
        when(c2.getCosts(iContext, act_i, act_k, act_j, 0.0)).thenReturn(2.0);

        SoftActivityConstraintManager man = new SoftActivityConstraintManager();

        man.addConstraint(c1);
        man.addConstraint(c2);
        assertEquals(3.0, man.getCosts(iContext, act_i, act_k, act_j, 0.0), 0.01);
    }
}
