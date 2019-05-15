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
package com.graphhopper.jsprit.core.algorithm.termination;

import com.graphhopper.jsprit.core.algorithm.SearchStrategy;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;


/**
 * Terminates algorithm prematurely based on iterations without any improvement (i.e. new solution acceptance).
 * <p>
 * <p>Termination will be activated by:<br>
 * <p>
 * <code>algorithm.setPrematureAlgorithmTermination(this);</code><br>
 *
 * @author stefan schroeder
 */
public class IterationWithoutImprovementTermination implements PrematureAlgorithmTermination {

    private static Logger log = LoggerFactory.getLogger(IterationWithoutImprovementTermination.class);

    private int noIterationWithoutImprovement;

    private int iterationsWithoutImprovement = 0;

    private List<Double> costs;

    private List<Integer> unassignedCount;

    private double bestCost = Double.MAX_VALUE;
    /**
     * Constructs termination.
     *
     * @param noIterationsWithoutImprovement previous iterations without any improvement
     */
    public IterationWithoutImprovementTermination(int noIterationsWithoutImprovement) {
        this.noIterationWithoutImprovement = noIterationsWithoutImprovement;
        costs = new ArrayList<>();
        unassignedCount = new ArrayList<>();
        log.debug("initialise " + this);
    }

    @Override
    public String toString() {
        return "[name=IterationWithoutImprovementBreaker][iterationsWithoutImprovement=" + noIterationWithoutImprovement + "]";
    }

    @Override
    public boolean isPrematureBreak(SearchStrategy.DiscoveredSolution discoveredSolution) {
        VehicleRoutingProblemSolution sol = discoveredSolution.getSolution();

        double currentCost = sol.getCost();
        bestCost = Math.min(currentCost, bestCost);
        costs.add(bestCost);

        int currentUnassigned = sol.getUnassignedJobs().size();
        unassignedCount.add(currentUnassigned);

        int i = costs.size() - 1;
        if (i < noIterationWithoutImprovement)
            return false;

        double progressPercentTerminationCondition = 0.1; // TODO: Move to configuration
        boolean unassignedEqual = (currentUnassigned == unassignedCount.get(i-noIterationWithoutImprovement));
        boolean progressTooSlow = (100 * Math.abs(currentCost - costs.get(i - noIterationWithoutImprovement))) / currentCost < progressPercentTerminationCondition;
        return (unassignedEqual && progressTooSlow);
    }
}
