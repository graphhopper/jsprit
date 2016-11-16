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

package com.graphhopper.jsprit.core.algorithm.recreate;

import com.graphhopper.jsprit.core.algorithm.state.StateManager;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;

/**
 * Created by schroeder on 15/08/16.
 */
public class ConfigureFixCostCalculatorTest {

    VehicleRoutingProblem vrp;

    @Before
    public void before() {
        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        for (int i = 0; i < 100; i++) {
            Service service = new Service.Builder("" + i).setLocation(Location.newInstance(0)).build();
            vrpBuilder.addJob(service);
        }
        vrp = vrpBuilder.build();
    }

    @Test
    public void shouldCalculateCorrectly() {
        List<Job> unassigned = new ArrayList<>();
        int count = 1;
        for (String key : vrp.getJobs().keySet()) {
            if (count <= 25) {
                unassigned.add(vrp.getJobs().get(key));
            }
            count++;
        }
        JobInsertionConsideringFixCostsCalculator jicc = new JobInsertionConsideringFixCostsCalculator(mock(JobInsertionCostsCalculator.class), mock(StateManager.class));
        ConfigureFixCostCalculator c = new ConfigureFixCostCalculator(vrp, jicc);
        c.informInsertionStarts(new ArrayList<VehicleRoute>(), unassigned);
        Assert.assertEquals(0.75, jicc.getSolutionCompletenessRatio(), 0.001);
    }

    @Test
    public void shouldBeMinRatio() {
        List<Job> unassigned = new ArrayList<>();
        int count = 1;
        for (String key : vrp.getJobs().keySet()) {
            if (count <= 75) {
                unassigned.add(vrp.getJobs().get(key));
            }
            count++;
        }
        JobInsertionConsideringFixCostsCalculator jicc = new JobInsertionConsideringFixCostsCalculator(mock(JobInsertionCostsCalculator.class), mock(StateManager.class));
        ConfigureFixCostCalculator c = new ConfigureFixCostCalculator(vrp, jicc);
        c.informInsertionStarts(new ArrayList<VehicleRoute>(), unassigned);
        Assert.assertEquals(0.5, jicc.getSolutionCompletenessRatio(), 0.001);
    }

    @Test
    public void shouldBeOne() {
        List<Job> unassigned = new ArrayList<>();
        JobInsertionConsideringFixCostsCalculator jicc = new JobInsertionConsideringFixCostsCalculator(mock(JobInsertionCostsCalculator.class), mock(StateManager.class));
        ConfigureFixCostCalculator c = new ConfigureFixCostCalculator(vrp, jicc);
        c.informInsertionStarts(new ArrayList<VehicleRoute>(), unassigned);
        Assert.assertEquals(1.0, jicc.getSolutionCompletenessRatio(), 0.001);
    }
}
