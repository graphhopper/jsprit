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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by schroeder on 13/01/17.
 */
class StringUtil {

    static List<Integer> getLowerBoundsOfAllStrings(int length, int seedIndex, int routeLength) {
        List<Integer> lowerBounds = new ArrayList<>();
        for (int i = 1; i <= length; i++) {
            int lower = seedIndex - (length - i);
            int upper = seedIndex + (i - 1);
            if (lower >= 0 && upper < routeLength) {
                lowerBounds.add(lower);
            }
        }
        return lowerBounds;
    }

    static int determineSubstringLength(int baseLength, int routeLength, Random random) {
        if (baseLength == routeLength) return 0;
        int substringLength = 1;
        while (baseLength + substringLength < routeLength) {
            if (random.nextDouble() < 0.01) {
                return substringLength;
            } else substringLength++;
        }
        return substringLength;
    }
}
