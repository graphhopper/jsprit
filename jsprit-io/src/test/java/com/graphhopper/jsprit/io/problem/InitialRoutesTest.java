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

package com.graphhopper.jsprit.io.problem;


import com.graphhopper.jsprit.core.problem.AbstractActivity;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.job.Shipment;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class InitialRoutesTest {


    @Test
    public void whenReading_jobMapShouldOnlyContainJob2() {

        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(vrpBuilder).read(getClass().getResourceAsStream("simpleProblem_iniRoutes.xml"));
        VehicleRoutingProblem vrp = vrpBuilder.build();

        assertEquals(1, getNuServices(vrp));
        assertTrue(vrp.getJobs().containsKey("2"));
    }

    @Test
    public void whenReadingProblem2_jobMapShouldContain_service2() {

        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(vrpBuilder).read(getClass().getResourceAsStream("simpleProblem_inclShipments_iniRoutes.xml"));
        VehicleRoutingProblem vrp = vrpBuilder.build();

        assertEquals(1, getNuServices(vrp));
        assertTrue(vrp.getJobs().containsKey("2"));
    }

    @Test
    public void whenReading_jobMapShouldContain_shipment4() {

        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(vrpBuilder).read(getClass().getResourceAsStream("simpleProblem_inclShipments_iniRoutes.xml"));
        VehicleRoutingProblem vrp = vrpBuilder.build();

        assertEquals(1, getNuShipments(vrp));
        assertTrue(vrp.getJobs().containsKey("4"));
    }

    private int getNuShipments(VehicleRoutingProblem vrp) {
        int nuShipments = 0;
        for (Job job : vrp.getJobs().values()) {
            if (job instanceof Shipment) nuShipments++;
        }
        return nuShipments;
    }

    private int getNuServices(VehicleRoutingProblem vrp) {
        int nuServices = 0;
        for (Job job : vrp.getJobs().values()) {
            if (job instanceof Service) nuServices++;
        }
        return nuServices;
    }

    @Test
    public void whenReading_thereShouldBeOnlyOneActAssociatedToJob2() {

        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(vrpBuilder).read(getClass().getResourceAsStream("simpleProblem_iniRoutes.xml"));
        VehicleRoutingProblem vrp = vrpBuilder.build();

        assertEquals(1, vrp.getActivities(vrp.getJobs().get("2")).size());
    }

    @Test
    public void whenReading_thereShouldBeOnlyOneActAssociatedToJob2_v2() {

        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(vrpBuilder).read(getClass().getResourceAsStream("simpleProblem_inclShipments_iniRoutes.xml"));
        VehicleRoutingProblem vrp = vrpBuilder.build();

        assertEquals(1, vrp.getActivities(vrp.getJobs().get("2")).size());
    }

    @Test
    public void whenReading_thereShouldBeTwoActsAssociatedToShipment4() {

        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(vrpBuilder).read(getClass().getResourceAsStream("simpleProblem_inclShipments_iniRoutes.xml"));
        VehicleRoutingProblem vrp = vrpBuilder.build();

        Job job = vrp.getJobs().get("4");
        List<AbstractActivity> activities = vrp.getActivities(job);

        assertEquals(2, activities.size());
    }


}
