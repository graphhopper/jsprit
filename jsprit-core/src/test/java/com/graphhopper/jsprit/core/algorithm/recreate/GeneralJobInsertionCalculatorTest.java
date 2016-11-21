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

package com.graphhopper.jsprit.core.algorithm.recreate;


import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.mockito.Mockito.mock;

/**
 * Created by schroeder on 19/11/16.
 */
public class GeneralJobInsertionCalculatorTest {

    GeneralJobInsertionCalculatorV2.IndexedTourActivity start;

    GeneralJobInsertionCalculatorV2.IndexedTourActivity act;

    GeneralJobInsertionCalculatorV2.IndexedTourActivity end;

    GeneralJobInsertionCalculatorV2.IndexedTourActivity toInsert;

    @Before
    public void doBefore() {
        start = new GeneralJobInsertionCalculatorV2.IndexedTourActivity(0, mock(TourActivity.class));
        act = new GeneralJobInsertionCalculatorV2.IndexedTourActivity(1, mock(TourActivity.class));
        end = new GeneralJobInsertionCalculatorV2.IndexedTourActivity(2, mock(TourActivity.class));
        toInsert = new GeneralJobInsertionCalculatorV2.IndexedTourActivity(3, mock(TourActivity.class));
    }

    @Test
    public void testSuccessor() {
        GeneralJobInsertionCalculatorV2.Route route = new GeneralJobInsertionCalculatorV2.Route(Arrays.asList(start, act, end), Arrays.asList(toInsert));
        Assert.assertEquals(start, route.getFirst());
        Assert.assertEquals(act, route.getSuccessor(route.getFirst()));
        Assert.assertEquals(end, route.getSuccessor(act));
        Assert.assertEquals(null, route.getSuccessor(toInsert));
    }

    @Test
    public void testPredecessor() {
        GeneralJobInsertionCalculatorV2.Route route = new GeneralJobInsertionCalculatorV2.Route(Arrays.asList(start, act, end), Arrays.asList(toInsert));
        Assert.assertEquals(null, route.getPredecessor(route.getFirst()));
        Assert.assertEquals(route.getFirst(), route.getPredecessor(act));
        Assert.assertEquals(act, route.getPredecessor(route.getSuccessor(act)));
        Assert.assertEquals(null, route.getSuccessor(toInsert));
    }

    @Test
    public void insertNew() {
        GeneralJobInsertionCalculatorV2.Route route = new GeneralJobInsertionCalculatorV2.Route(Arrays.asList(start, act, end), Arrays.asList(toInsert));
        Assert.assertEquals(route.getFirst(), route.getPredecessor(act));
        route.addAfter(toInsert, route.getFirst());
        Assert.assertEquals(toInsert, route.getPredecessor(act));
        Assert.assertEquals(route.getFirst(), route.getPredecessor(toInsert));
        Assert.assertEquals(start, route.getFirst());
        Assert.assertEquals(toInsert, route.getSuccessor(route.getFirst()));
        Assert.assertEquals(act, route.getSuccessor(toInsert));
    }

    @Test
    public void removeAct() {
        GeneralJobInsertionCalculatorV2.Route route = new GeneralJobInsertionCalculatorV2.Route(Arrays.asList(start, act, end), Arrays.asList(toInsert));
        Assert.assertEquals(route.getFirst(), route.getPredecessor(act));
        route.remove(act);
        Assert.assertEquals(null, route.getPredecessor(act));
        Assert.assertEquals(null, route.getSuccessor(act));
        Assert.assertEquals(start, route.getFirst());
        Assert.assertEquals(end, route.getSuccessor(start));

    }


}
