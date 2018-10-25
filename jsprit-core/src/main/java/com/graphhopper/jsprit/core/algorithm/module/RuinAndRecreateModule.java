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
import com.graphhopper.jsprit.core.problem.job.Break;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;

import java.util.*;


public class RuinAndRecreateModule implements SearchStrategyModule {

    private InsertionStrategy insertion;

    private RuinStrategy ruin;

    private String moduleName;

    public RuinAndRecreateModule(String moduleName, InsertionStrategy insertion, RuinStrategy ruin) {
        super();
        this.insertion = insertion;
        this.ruin = ruin;
        this.moduleName = moduleName;
    }

    @Override
    public VehicleRoutingProblemSolution runAndGetSolution(VehicleRoutingProblemSolution vrpSolution) {
        Collection<Job> ruinedJobs = ruin.ruin(vrpSolution.getRoutes());
        Set<Job> ruinedJobSet = new HashSet<>();
        ruinedJobSet.addAll(ruinedJobs);
        ruinedJobSet.addAll(vrpSolution.getUnassignedJobs());
        Collection<Job> unassignedJobs = insertion.insertJobs(vrpSolution.getRoutes(), ruinedJobSet);
        vrpSolution.getUnassignedJobs().clear();
        vrpSolution.getUnassignedJobs().addAll(getUnassignedJobs(vrpSolution, unassignedJobs));
        removeEmptyRoutes(vrpSolution.getRoutes());
        return vrpSolution;
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

    static Set<Job> getUnassignedJobs(VehicleRoutingProblemSolution vrpSolution, Collection<Job> unassignedJobs) {
        final Set<Job> unassigned = new HashSet<>();
        for (Job job : unassignedJobs) {
            if (!(job instanceof Break))
                unassigned.add(job);
        }

        for (VehicleRoute route : vrpSolution.getRoutes()) {
            Break aBreak = route.getVehicle().getBreak();
            if(aBreak != null && !route.getTourActivities().servesJob(aBreak)) {
                unassigned.add(aBreak);
            }
        }
        return unassigned;
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
