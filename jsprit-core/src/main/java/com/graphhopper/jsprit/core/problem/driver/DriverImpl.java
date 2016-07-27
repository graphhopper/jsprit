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
package com.graphhopper.jsprit.core.problem.driver;

public class DriverImpl implements Driver {

    public static NoDriver noDriver() {
        return new NoDriver();
    }

    public static class NoDriver extends DriverImpl {

        public NoDriver() {
            super("noDriver");
        }

    }

    private String id;

    private double earliestStart = 0.0;

    private double latestEnd = Double.MAX_VALUE;

    private String home;

    private DriverImpl(String id) {
        super();
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public double getEarliestStart() {
        return earliestStart;
    }

    public void setEarliestStart(double earliestStart) {
        this.earliestStart = earliestStart;
    }

    public double getLatestEnd() {
        return latestEnd;
    }

    public void setLatestEnd(double latestEnd) {
        this.latestEnd = latestEnd;
    }

    public void setHomeLocation(String locationId) {
        this.home = locationId;
    }

    public String getHomeLocation() {
        return this.home;
    }

}
