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

package com.graphhopper.jsprit.core.util;

import junit.framework.Assert;
import org.junit.Test;

/**
 * Created by schroeder on 28.11.14.
 */
public class GreatCircleDistanceCalculatorTest {

    @Test
    public void test() {
        double lon1 = 8.3858333;
        double lat1 = 49.0047222;

        double lon2 = 12.1333333;
        double lat2 = 54.0833333;

        double greatCircle = GreatCircleDistanceCalculator.calculateDistance(
            Coordinate.newInstance(lon1, lat1),
            Coordinate.newInstance(lon2, lat2),
            DistanceUnit.Kilometer
        );
        Assert.assertEquals(600, greatCircle, 30.);
    }

    @Test
    public void testMeter() {
        double lon1 = 8.3858333;
        double lat1 = 49.0047222;

        double lon2 = 12.1333333;
        double lat2 = 54.0833333;

        double greatCircle = GreatCircleDistanceCalculator.calculateDistance(
            Coordinate.newInstance(lon1, lat1),
            Coordinate.newInstance(lon2, lat2),
            DistanceUnit.Meter
        );
        Assert.assertEquals(600000, greatCircle, 30000.);
    }

}
