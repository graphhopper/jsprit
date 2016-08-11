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

package com.graphhopper.jsprit.core.algorithm;

import com.graphhopper.jsprit.core.algorithm.box.Jsprit;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.util.Solutions;
import junit.framework.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;


public class ExternalInitialSolutionIsInValidTest {

    @Test
    public void itShouldSolveProblemWithIniSolutionExternallyCreated() {

        Service s1 = Service.Builder.newInstance("s1").setLocation(Location.newInstance(10, 0)).build();
        Service s2 = Service.Builder.newInstance("s2").setLocation(Location.newInstance(0, 10)).build();

        VehicleImpl vehicle = VehicleImpl.Builder.newInstance("v1").setStartLocation(Location.newInstance(0, 0)).build();

        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addJob(s1).addJob(s2).addVehicle(vehicle).build();

        VehicleRoutingAlgorithm vra = Jsprit.createAlgorithm(vrp);

        /*
        create ini sol
         */
        VehicleRoute route1 = VehicleRoute.Builder.newInstance(vehicle).setJobActivityFactory(vrp.getJobActivityFactory()).addService(s1).build();

        vra.addInitialSolution(new VehicleRoutingProblemSolution(Arrays.asList(route1), 20.));

        try {
            vra.searchSolutions();
            Assert.assertTrue(true);
        }
        catch (Exception e){
            Assert.assertFalse(true);
        }

    }

}
