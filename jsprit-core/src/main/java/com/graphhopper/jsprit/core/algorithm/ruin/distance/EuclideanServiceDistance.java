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
package com.graphhopper.jsprit.core.algorithm.ruin.distance;

import com.graphhopper.jsprit.core.distance.EuclideanDistanceCalculator;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.job.Service;

@Deprecated
public class EuclideanServiceDistance implements JobDistance {

    public EuclideanServiceDistance() {
        super();
    }

    @Override
    public double getDistance(Job i, Job j) {
        double avgCost = 0.0;
        if (i instanceof Service && j instanceof Service) {
            if (i.equals(j)) {
                avgCost = 0.0;
            } else {
                Service s_i = (Service) i;
                Service s_j = (Service) j;
                if (s_i.getLocation().getCoordinate() == null || s_j.getLocation().getCoordinate() == null) {
                    throw new IllegalStateException("cannot calculate euclidean distance. since service coords are missing");
                }
                avgCost = EuclideanDistanceCalculator.getInstance().calculateDistance(s_i.getLocation().getCoordinate(), s_j.getLocation().getCoordinate());
            }
        } else {
            throw new UnsupportedOperationException(
                            "currently, this class just works with shipments and services.");
        }
        return avgCost;
    }

}
