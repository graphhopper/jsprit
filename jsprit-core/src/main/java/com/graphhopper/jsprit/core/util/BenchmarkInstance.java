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

import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;

public class BenchmarkInstance {
    public final String name;
    public final VehicleRoutingProblem vrp;
    public final Double bestKnownResult;
    public Double bestKnownVehicles;

    public BenchmarkInstance(String name, VehicleRoutingProblem vrp, Double bestKnownResult, Double bestKnowVehicles) {
        super();
        this.name = name;
        this.vrp = vrp;
        this.bestKnownResult = bestKnownResult;
        this.bestKnownVehicles = bestKnowVehicles;
    }
}
