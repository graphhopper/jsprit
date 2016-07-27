/*******************************************************************************
 * Copyright (C) 2014  Stefan Schroeder
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
