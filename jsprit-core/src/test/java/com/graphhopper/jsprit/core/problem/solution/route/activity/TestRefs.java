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
package com.graphhopper.jsprit.core.problem.solution.route.activity;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;


public class TestRefs {

    @Test
    public void testReferencs() {
        List<Start> starts = new ArrayList<Start>();
        starts.add(Start.newInstance("foo0", 0.0, 0.0));
        starts.add(Start.newInstance("foo1", 1.0, 1.0));

        doSmth(starts);

        assertTrue(starts.get(0).getLocation().getId().startsWith("foo"));
        assertTrue(starts.get(1).getLocation().getId().startsWith("foo"));
    }

    private void doSmth(List<Start> starts) {
        int count = 0;
        for (@SuppressWarnings("unused") Start s : starts) {
            s = Start.newInstance("yo_" + count, 0.0, 0.0);
            count++;
        }

    }

}
