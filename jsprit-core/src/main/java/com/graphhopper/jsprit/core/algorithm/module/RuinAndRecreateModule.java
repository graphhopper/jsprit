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

import com.graphhopper.jsprit.core.algorithm.SearchStrategyModule;
import com.graphhopper.jsprit.core.algorithm.listener.SearchStrategyModuleListener;
import com.graphhopper.jsprit.core.algorithm.recreate.InsertionStrategy;
import com.graphhopper.jsprit.core.algorithm.recreate.listener.InsertionListener;
import com.graphhopper.jsprit.core.algorithm.ruin.RuinStrategy;
import com.graphhopper.jsprit.core.algorithm.ruin.listener.RuinListener;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;

import java.util.*;


public class RuinAndRecreateModule implements SearchStrategyModule {

    private InsertionStrategy insertion;

    private RuinStrategy ruin;

    private String moduleName;

    private Random random = new Random(4711);

    private int minUnassignedJobsToBeReinserted = Integer.MAX_VALUE;

    private double proportionOfUnassignedJobsToBeReinserted = 1d;

    public RuinAndRecreateModule(String moduleName, InsertionStrategy insertion, RuinStrategy ruin) {
        super();
        this.insertion = insertion;
        this.ruin = ruin;
        this.moduleName = moduleName;
    }

    /**
     * To make overall results reproducible, make sure this class is provided with the "global" random number generator.
     *
     * @param random
     */
    public void setRandom(Random random) {
        this.random = random;
    }

    /**
     * Minimum number of unassigned jobs that is reinserted in each iteration.
     *
     * @param minUnassignedJobsToBeReinserted
     */
    public void setMinUnassignedJobsToBeReinserted(int minUnassignedJobsToBeReinserted) {
        this.minUnassignedJobsToBeReinserted = minUnassignedJobsToBeReinserted;
    }

    /**
     * Proportion of unassigned jobs that is reinserted in each iteration.
     *
     * @param proportionOfUnassignedJobsToBeReinserted
     */
    public void setProportionOfUnassignedJobsToBeReinserted(double proportionOfUnassignedJobsToBeReinserted) {
        this.proportionOfUnassignedJobsToBeReinserted = proportionOfUnassignedJobsToBeReinserted;
    }

    @Override
    public VehicleRoutingProblemSolution runAndGetSolution(VehicleRoutingProblemSolution previousVrpSolution) {
        Collection<Job> ruinedJobs = ruin.ruin(previousVrpSolution.getRoutes());
        Set<Job> ruinedJobSet = new HashSet<>();
        ruinedJobSet.addAll(ruinedJobs);
        List<Job> stillUnassignedInThisIteration = new ArrayList<>();
        if (previousVrpSolution.getUnassignedJobs().size() < minUnassignedJobsToBeReinserted) {
            ruinedJobSet.addAll(previousVrpSolution.getUnassignedJobs());
        } else {
            int noUnassignedToBeInserted = Math.max(minUnassignedJobsToBeReinserted, (int) (previousVrpSolution.getUnassignedJobs().size() * proportionOfUnassignedJobsToBeReinserted));
            List<Job> jobList = new ArrayList<>(previousVrpSolution.getUnassignedJobs());
            Collections.shuffle(jobList, random);
            for (int i = 0; i < noUnassignedToBeInserted; i++) {
                ruinedJobSet.add(jobList.get(i));
            }
            for (int i = noUnassignedToBeInserted; i < jobList.size(); i++) {
                stillUnassignedInThisIteration.add(jobList.get(i));
            }
        }
        Collection<Job> unassignedJobs = insertion.insertJobs(previousVrpSolution.getRoutes(), ruinedJobSet);
        previousVrpSolution.getUnassignedJobs().clear();
        previousVrpSolution.getUnassignedJobs().addAll(unassignedJobs);
        previousVrpSolution.getUnassignedJobs().addAll(stillUnassignedInThisIteration);
        VehicleRoutingProblemSolution newSolution = previousVrpSolution;
        removeEmptyRoutes(newSolution.getRoutes());
        return newSolution;

    }

    static void removeEmptyRoutes(Collection<VehicleRoute> routes) {
        final Iterator<VehicleRoute> iterator = routes.iterator();
        List<VehicleRoute> emptyRoutes = new ArrayList<>();
        while (iterator.hasNext()) {
            final VehicleRoute route = iterator.next();
            if (route.isEmpty())
                emptyRoutes.add(route);
        }
        routes.removeAll(emptyRoutes);
    }

    @Override
    public String getName() {
        return moduleName;
    }

    @Override
    public void addModuleListener(SearchStrategyModuleListener moduleListener) {
        if (moduleListener instanceof InsertionListener) {
            InsertionListener iListener = (InsertionListener) moduleListener;
            if (!insertion.getListeners().contains(iListener)) {
                insertion.addListener(iListener);
            }
        }
        if (moduleListener instanceof RuinListener) {
            RuinListener rListener = (RuinListener) moduleListener;
            if (!ruin.getListeners().contains(rListener)) {
                ruin.addListener(rListener);
            }
        }

    }

    public InsertionStrategy getInsertion() {
        return insertion;
    }

    public RuinStrategy getRuin() {
        return ruin;
    }
}
