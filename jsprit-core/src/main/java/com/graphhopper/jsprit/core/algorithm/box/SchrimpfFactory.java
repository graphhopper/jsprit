/*******************************************************************************
 * Copyright (C) 2013  Stefan Schroeder
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
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
        builder.setProperty(Jsprit.Parameter.RADIAL_MIN_SHARE, String.valueOf(0.3));
        builder.setProperty(Jsprit.Parameter.RADIAL_MAX_SHARE, String.valueOf(0.3));
        builder.setProperty(Jsprit.Parameter.RANDOM_BEST_MIN_SHARE, String.valueOf(0.5));
        builder.setProperty(Jsprit.Parameter.RANDOM_BEST_MAX_SHARE, String.valueOf(0.5));
        return builder.buildAlgorithm();
    }


}
