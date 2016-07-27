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

package com.graphhopper.jsprit.core.algorithm.ruin.listener;

import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;


public class RuinListeners {

    private Collection<RuinListener> ruinListeners = new ArrayList<RuinListener>();

    public void ruinStarts(Collection<VehicleRoute> routes) {
        for (RuinListener l : ruinListeners) l.ruinStarts(routes);
    }

    public void ruinEnds(Collection<VehicleRoute> routes, Collection<Job> unassignedJobs) {
        for (RuinListener l : ruinListeners) l.ruinEnds(routes, unassignedJobs);
    }

    public void removed(Job job, VehicleRoute fromRoute) {
        for (RuinListener l : ruinListeners) l.removed(job, fromRoute);
    }

    public void addListener(RuinListener ruinListener) {
        ruinListeners.add(ruinListener);
    }

    public void removeListener(RuinListener ruinListener) {
        ruinListeners.remove(ruinListener);
    }

    public Collection<RuinListener> getListeners() {
        return Collections.unmodifiableCollection(ruinListeners);
    }
}
