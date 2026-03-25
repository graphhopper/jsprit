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
package com.graphhopper.jsprit.core.algorithm.module;

import com.graphhopper.jsprit.core.algorithm.DynamicStrategyIdProvider;
import com.graphhopper.jsprit.core.algorithm.SearchStrategyModule;
import com.graphhopper.jsprit.core.algorithm.listener.SearchStrategyModuleListener;
import com.graphhopper.jsprit.core.algorithm.recreate.InsertionStrategy;
import com.graphhopper.jsprit.core.algorithm.recreate.listener.InsertionListener;
import com.graphhopper.jsprit.core.algorithm.ruin.RuinStrategy;
import com.graphhopper.jsprit.core.algorithm.ruin.listener.RuinListener;
import com.graphhopper.jsprit.core.algorithm.selector.WeightedOperatorSelector;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.util.RandomNumberGeneration;

import java.util.*;

/**
 * A ruin-and-recreate module that selects ruin and insertion operators independently.
 *
 * <p>Unlike the standard {@link RuinAndRecreateModule} which has fixed operators,
 * this module maintains weighted selectors for both ruin and insertion operators
 * and selects them independently at each iteration.</p>
 *
 * <p>This enables more flexible search strategies where the ruin and insertion
 * choices are decoupled, allowing more combinations to be explored.</p>
 *
 * @see WeightedOperatorSelector
 */
public class IndependentRuinAndRecreateModule implements SearchStrategyModule, DynamicStrategyIdProvider {

    private final WeightedOperatorSelector<InsertionStrategy> insertionSelector;
    private final WeightedOperatorSelector<RuinStrategy> ruinSelector;
    private final String moduleName;

    private Random random = RandomNumberGeneration.newInstance();
    private int minUnassignedJobsToBeReinserted = Integer.MAX_VALUE;
    private double proportionOfUnassignedJobsToBeReinserted = 1d;

    /**
     * Creates a new independent ruin-and-recreate module.
     *
     * @param moduleName        the name of this module
     * @param insertionSelector the weighted selector for insertion strategies
     * @param ruinSelector      the weighted selector for ruin strategies
     */
    public IndependentRuinAndRecreateModule(
        String moduleName,
        WeightedOperatorSelector<InsertionStrategy> insertionSelector,
        WeightedOperatorSelector<RuinStrategy> ruinSelector
    ) {
        this.moduleName = moduleName;
        this.insertionSelector = insertionSelector;
        this.ruinSelector = ruinSelector;
    }

    /**
     * Sets the random number generator for reproducibility.
     *
     * @param random the random number generator
     */
    public void setRandom(Random random) {
        this.random = random;
        this.insertionSelector.setRandom(random);
        this.ruinSelector.setRandom(random);
    }

    /**
     * Sets the minimum number of unassigned jobs to be reinserted in each iteration.
     *
     * @param minUnassignedJobsToBeReinserted minimum jobs to reinsert
     */
    public void setMinUnassignedJobsToBeReinserted(int minUnassignedJobsToBeReinserted) {
        this.minUnassignedJobsToBeReinserted = minUnassignedJobsToBeReinserted;
    }

    /**
     * Sets the proportion of unassigned jobs to be reinserted in each iteration.
     *
     * @param proportionOfUnassignedJobsToBeReinserted proportion (0.0-1.0)
     */
    public void setProportionOfUnassignedJobsToBeReinserted(double proportionOfUnassignedJobsToBeReinserted) {
        this.proportionOfUnassignedJobsToBeReinserted = proportionOfUnassignedJobsToBeReinserted;
    }

    @Override
    public VehicleRoutingProblemSolution runAndGetSolution(VehicleRoutingProblemSolution previousVrpSolution) {
        // Select operators independently
        RuinStrategy ruin = ruinSelector.select();
        InsertionStrategy insertion = insertionSelector.select();

        // Perform ruin
        Collection<Job> ruinedJobs = ruin.ruin(previousVrpSolution.getRoutes());
        Set<Job> ruinedJobSet = new HashSet<>(ruinedJobs);

        // Handle previously unassigned jobs
        List<Job> stillUnassignedInThisIteration = new ArrayList<>();
        if (previousVrpSolution.getUnassignedJobs().size() < minUnassignedJobsToBeReinserted) {
            ruinedJobSet.addAll(previousVrpSolution.getUnassignedJobs());
        } else {
            int noUnassignedToBeInserted = Math.max(
                minUnassignedJobsToBeReinserted,
                (int) (previousVrpSolution.getUnassignedJobs().size() * proportionOfUnassignedJobsToBeReinserted)
            );
            List<Job> jobList = new ArrayList<>(previousVrpSolution.getUnassignedJobs());
            Collections.shuffle(jobList, random);
            for (int i = 0; i < noUnassignedToBeInserted; i++) {
                ruinedJobSet.add(jobList.get(i));
            }
            for (int i = noUnassignedToBeInserted; i < jobList.size(); i++) {
                stillUnassignedInThisIteration.add(jobList.get(i));
            }
        }

        // Order ruined jobs for deterministic results
        List<Job> orderedRuinedJobs = new ArrayList<>(ruinedJobSet);
        orderedRuinedJobs.sort(Comparator.comparing(Job::getId));

        // Perform insertion
        Collection<Job> unassignedJobs = insertion.insertJobs(previousVrpSolution.getRoutes(), orderedRuinedJobs);

        // Update solution
        previousVrpSolution.getUnassignedJobs().clear();
        previousVrpSolution.getUnassignedJobs().addAll(unassignedJobs);
        previousVrpSolution.getUnassignedJobs().addAll(stillUnassignedInThisIteration);

        return previousVrpSolution;
    }

    @Override
    public String getName() {
        return moduleName;
    }

    @Override
    public void addModuleListener(SearchStrategyModuleListener moduleListener) {
        // Add listeners to all registered operators
        if (moduleListener instanceof InsertionListener iListener) {
            for (var entry : insertionSelector.getEntries()) {
                if (!entry.operator().getListeners().contains(iListener)) {
                    entry.operator().addListener(iListener);
                }
            }
        }
        if (moduleListener instanceof RuinListener rListener) {
            for (var entry : ruinSelector.getEntries()) {
                if (!entry.operator().getListeners().contains(rListener)) {
                    entry.operator().addListener(rListener);
                }
            }
        }
    }

    /**
     * Returns the insertion selector.
     */
    public WeightedOperatorSelector<InsertionStrategy> getInsertionSelector() {
        return insertionSelector;
    }

    /**
     * Returns the ruin selector.
     */
    public WeightedOperatorSelector<RuinStrategy> getRuinSelector() {
        return ruinSelector;
    }

    /**
     * Returns the strategy ID for the last execution, reflecting which operators were selected.
     *
     * <p>Format: "{ruinName}+{insertionName}"</p>
     *
     * @return the dynamic strategy ID, e.g., "radial+regretFast"
     */
    @Override
    public String getLastExecutionStrategyId() {
        String ruinName = ruinSelector.getLastSelectedName();
        String insertionName = insertionSelector.getLastSelectedName();
        return ruinName + "+" + insertionName;
    }

    @Override
    public String toString() {
        return "IndependentRuinAndRecreate{" +
            "insertions=" + insertionSelector +
            ", ruins=" + ruinSelector +
            '}';
    }
}
