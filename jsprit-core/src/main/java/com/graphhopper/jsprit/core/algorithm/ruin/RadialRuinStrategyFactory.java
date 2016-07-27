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

import com.graphhopper.jsprit.core.algorithm.ruin.distance.JobDistance;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;

public class RadialRuinStrategyFactory implements RuinStrategyFactory {

    private double fraction;

    private JobDistance jobDistance;

    public RadialRuinStrategyFactory(double fraction, JobDistance jobDistance) {
        super();
        this.fraction = fraction;
        this.jobDistance = jobDistance;
    }

    @Override
    public RuinStrategy createStrategy(VehicleRoutingProblem vrp) {
        return new RuinRadial(vrp, fraction, jobDistance);
    }

}
