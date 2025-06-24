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
package com.graphhopper.jsprit.core.algorithm.ruin;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.util.Coordinate;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Created by schroeder on 30/01/15.
 */
@DisplayName("Ruin Radial Test")
class RuinRadialTest {

    @Test
    @DisplayName("When No Job Has Location _ it Should Still Work")
    void whenNoJobHasLocation_itShouldStillWork() {
        Service s1 = Service.Builder.newInstance("s1").build();
        Service s2 = Service.Builder.newInstance("s2").build();
        Service s3 = Service.Builder.newInstance("s3").build();
        VehicleImpl v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.Builder.newInstance().setCoordinate(Coordinate.newInstance(0, 0)).build()).build();
        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addJob(s1).addJob(s2).addJob(s3).addVehicle(v).build();
        RuinRadial ruinRadial = new RuinRadial(vrp, 1, new JobNeighborhoods() {

            @Override
            public Iterator<Job> getNearestNeighborsIterator(int nNeighbors, Job neighborTo) {
                return null;
            }

            @Override
            public void initialise() {
            }

            @Override
            public double getMaxDistance() {
                return 0;
            }
        });
        VehicleRoute route = VehicleRoute.Builder.newInstance(v).addService(s1).addService(s2).addService(s3).setJobActivityFactory(vrp.getJobActivityFactory()).build();
        try {
            ruinRadial.ruinRoutes(Arrays.asList(route));
        } catch (Exception e) {
            Assertions.assertFalse(true, "error when ruining routes with radial ruin");
        }
        Assertions.assertTrue(true);
    }
}
