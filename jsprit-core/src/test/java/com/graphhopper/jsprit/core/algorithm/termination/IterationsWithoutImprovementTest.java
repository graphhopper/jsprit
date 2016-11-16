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

package com.graphhopper.jsprit.core.algorithm.termination;


import com.graphhopper.jsprit.core.algorithm.SearchStrategy;
import junit.framework.Assert;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class IterationsWithoutImprovementTest {

    @Test
    public void itShouldTerminateAfter100() {
        IterationWithoutImprovementTermination termination = new IterationWithoutImprovementTermination(100);
        SearchStrategy.DiscoveredSolution discoveredSolution = mock(SearchStrategy.DiscoveredSolution.class);
        when(discoveredSolution.isAccepted()).thenReturn(false);
        int terminatedAfter = 0;
        for (int i = 0; i < 200; i++) {
            boolean terminate = termination.isPrematureBreak(discoveredSolution);
            if (terminate) {
                terminatedAfter = i;
                break;
            }
        }
        Assert.assertEquals(100, terminatedAfter);
    }

    @Test
    public void itShouldTerminateAfter1() {
        IterationWithoutImprovementTermination termination = new IterationWithoutImprovementTermination(1);
        SearchStrategy.DiscoveredSolution discoveredSolution = mock(SearchStrategy.DiscoveredSolution.class);
        when(discoveredSolution.isAccepted()).thenReturn(false);
        int terminatedAfter = 0;
        for (int i = 0; i < 200; i++) {
            boolean terminate = termination.isPrematureBreak(discoveredSolution);
            if (terminate) {
                terminatedAfter = i;
                break;
            }
        }
        Assert.assertEquals(1, terminatedAfter);
    }

    @Test
    public void itShouldTerminateAfter150() {
        IterationWithoutImprovementTermination termination = new IterationWithoutImprovementTermination(100);
        SearchStrategy.DiscoveredSolution discoveredSolution = mock(SearchStrategy.DiscoveredSolution.class);
        int terminatedAfter = 0;
        for (int i = 0; i < 200; i++) {
            when(discoveredSolution.isAccepted()).thenReturn(false);
            if (i == 49) when(discoveredSolution.isAccepted()).thenReturn(true);
            boolean terminate = termination.isPrematureBreak(discoveredSolution);
            if (terminate) {
                terminatedAfter = i;
                break;
            }
        }
        Assert.assertEquals(150, terminatedAfter);
    }
}
