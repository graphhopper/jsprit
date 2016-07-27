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

import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingTransportCosts;

public class CostFactory {

    /**
     * Return manhattanCosts.
     * <p>
     * This retrieves coordinates from locationIds. LocationId has to be locId="{x},{y}". For example,
     * locId="10,10" is interpreted such that x=10 and y=10.
     *
     * @return manhattanCosts
     */
    public static VehicleRoutingTransportCosts createManhattanCosts() {
        Locations locations = new Locations() {

            @Override
            public Coordinate getCoord(String id) {
                //assume: locationId="x,y"
                String[] splitted = id.split(",");
                return Coordinate.newInstance(Double.parseDouble(splitted[0]),
                    Double.parseDouble(splitted[1]));
            }

        };
        return new ManhattanCosts(locations);
    }

    /**
     * Return euclideanCosts.
     * <p>
     * This retrieves coordinates from locationIds. LocationId has to be locId="{x},{y}". For example,
     * locId="10,10" is interpreted such that x=10 and y=10.
     *
     * @return euclidean
     */
    public static VehicleRoutingTransportCosts createEuclideanCosts() {
        Locations locations = new Locations() {

            @Override
            public Coordinate getCoord(String id) {
                //assume: locationId="x,y"
                String[] splitted = id.split(",");
                return Coordinate.newInstance(Double.parseDouble(splitted[0]),
                    Double.parseDouble(splitted[1]));
            }

        };
        return new CrowFlyCosts(locations);
    }
}
