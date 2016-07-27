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

/**
 * Created by schroeder on 28.11.14.
 */
public class GreatCircleDistanceCalculator {

    private static final double R = 6372.8; // km

    /**
     * Harversine method.
     * <p>
     * double lon1 = coord1.getX();
     * double lon2 = coord2.getX();
     * double lat1 = coord1.getY();
     * double lat2 = coord2.getY();
     *
     * @param coord1 - from coord
     * @param coord2 - to coord
     * @return great circle distance
     */
    public static double calculateDistance(Coordinate coord1, Coordinate coord2, DistanceUnit distanceUnit) {
        double lon1 = coord1.getX();
        double lon2 = coord2.getX();
        double lat1 = coord1.getY();
        double lat2 = coord2.getY();

        double delta_Lat = Math.toRadians(lat2 - lat1);
        double delta_Lon = Math.toRadians(lon2 - lon1);
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);

        double a = Math.sin(delta_Lat / 2) * Math.sin(delta_Lat / 2) + Math.sin(delta_Lon / 2) * Math.sin(delta_Lon / 2) * Math.cos(lat1) * Math.cos(lat2);
        double c = 2 * Math.asin(Math.sqrt(a));
        double distance = R * c;
        if (distanceUnit.equals(DistanceUnit.Meter)) {
            distance = distance * 1000.;
        }
        return distance;
    }

}
