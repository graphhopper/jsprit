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
package com.graphhopper.jsprit.core.algorithm.box;

import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;


/**
 * Factory that creates the {@link com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm} as proposed by Schrimpf et al., 2000 with the following parameters:
 * <p>
 * <p>
 * R&R_random (prob=0.5, F=0.5);
 * R&R_radial (prob=0.5, F=0.3);
 * threshold-accepting with exponentialDecayFunction (alpha=0.1, warmup-iterations=100);
 * nuOfIterations=2000
 * <p>
 * <p>Gerhard Schrimpf, Johannes Schneider, Hermann Stamm- Wilbrandt, and Gunter Dueck.
 * Record breaking optimization results using the ruin and recreate principle.
 * Journal of Computational Physics, 159(2):139 – 171, 2000. ISSN 0021-9991. doi: 10.1006/jcph.1999. 6413.
 * URL http://www.sciencedirect.com/science/article/ pii/S0021999199964136
 * <p>
 * <p>algorithm-xml-config is available at src/main/resources/schrimpf.xml.
 *
 * @author stefan schroeder
 */
public class SchrimpfFactory {

    /**
     * Creates the {@link com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm}.
     *
     * @param vrp the underlying vehicle routing problem
     * @return algorithm
     */
    public VehicleRoutingAlgorithm createAlgorithm(VehicleRoutingProblem vrp) {
        //TODO determine alpha threshold

        int radialShare = (int) (vrp.getJobs().size() * 0.3);
        int randomShare = (int) (vrp.getJobs().size() * 0.5);
        Jsprit.Builder builder = Jsprit.Builder.newInstance(vrp);
        builder.setProperty(Jsprit.Parameter.THRESHOLD_ALPHA,"0.0");
        builder.setProperty(Jsprit.Strategy.RADIAL_BEST, "0.5");
        builder.setProperty(Jsprit.Strategy.RADIAL_REGRET, "0.0");
        builder.setProperty(Jsprit.Strategy.RANDOM_BEST, "0.5");
        builder.setProperty(Jsprit.Strategy.RANDOM_REGRET, "0.0");
        builder.setProperty(Jsprit.Strategy.WORST_BEST, "0.0");
        builder.setProperty(Jsprit.Strategy.WORST_REGRET, "0.0");
        builder.setProperty(Jsprit.Strategy.CLUSTER_BEST, "0.0");
        builder.setProperty(Jsprit.Strategy.CLUSTER_REGRET, "0.0");
        builder.setProperty(Jsprit.Parameter.RADIAL_MIN_SHARE, String.valueOf(radialShare));
        builder.setProperty(Jsprit.Parameter.RADIAL_MAX_SHARE, String.valueOf(radialShare));
        builder.setProperty(Jsprit.Parameter.RANDOM_BEST_MIN_SHARE, String.valueOf(randomShare));
        builder.setProperty(Jsprit.Parameter.RANDOM_BEST_MAX_SHARE, String.valueOf(randomShare));
        return builder.buildAlgorithm();
    }


}
