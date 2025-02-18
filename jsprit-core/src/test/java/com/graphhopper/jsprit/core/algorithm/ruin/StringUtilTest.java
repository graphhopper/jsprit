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
package com.graphhopper.jsprit.core.algorithm.ruin;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * Created by schroeder on 13/01/17.
 */
@DisplayName("String Util Test")
class StringUtilTest {

    @Test
    @DisplayName("Test")
    void test() {
        int stringLength = 4;
        int seedIndex = 4;
        int noActivities = 10;
        List<Integer> bounds = StringUtil.getLowerBoundsOfAllStrings(stringLength, seedIndex, noActivities);
        Assertions.assertEquals(4, bounds.size());
        Assertions.assertEquals(1, (int) bounds.get(0));
        Assertions.assertEquals(2, (int) bounds.get(1));
        Assertions.assertEquals(3, (int) bounds.get(2));
        Assertions.assertEquals(4, (int) bounds.get(3));
    }

    @Test
    @DisplayName("Test 2")
    void test2() {
        int stringLength = 4;
        int seedIndex = 2;
        int noActivities = 10;
        List<Integer> bounds = StringUtil.getLowerBoundsOfAllStrings(stringLength, seedIndex, noActivities);
        Assertions.assertEquals(3, bounds.size());
        Assertions.assertEquals(0, (int) bounds.get(0));
        Assertions.assertEquals(1, (int) bounds.get(1));
        Assertions.assertEquals(2, (int) bounds.get(2));
    }

    @Test
    @DisplayName("Test 3")
    void test3() {
        int stringLength = 4;
        int seedIndex = 0;
        int noActivities = 10;
        List<Integer> bounds = StringUtil.getLowerBoundsOfAllStrings(stringLength, seedIndex, noActivities);
        Assertions.assertEquals(1, bounds.size());
        Assertions.assertEquals(0, (int) bounds.get(0));
    }

    @Test
    @DisplayName("Test 4")
    void test4() {
        int stringLength = 4;
        int seedIndex = 9;
        int noActivities = 10;
        List<Integer> bounds = StringUtil.getLowerBoundsOfAllStrings(stringLength, seedIndex, noActivities);
        Assertions.assertEquals(1, bounds.size());
        Assertions.assertEquals(6, (int) bounds.get(0));
    }

    @Test
    @DisplayName("Test 5")
    void test5() {
        int stringLength = 4;
        int seedIndex = 8;
        int noActivities = 10;
        List<Integer> bounds = StringUtil.getLowerBoundsOfAllStrings(stringLength, seedIndex, noActivities);
        Assertions.assertEquals(2, bounds.size());
        Assertions.assertEquals(5, (int) bounds.get(0));
        Assertions.assertEquals(6, (int) bounds.get(1));
    }
}
