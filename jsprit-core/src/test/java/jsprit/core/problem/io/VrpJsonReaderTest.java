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

package jsprit.core.problem.io;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.job.Job;
import jsprit.core.problem.job.Service;
import jsprit.core.problem.vehicle.Vehicle;
import jsprit.core.util.DistanceUnit;
import jsprit.core.util.TimeUnit;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class VrpJsonReaderTest {

     private File jsonFile;

    VehicleRoutingProblem vrp;

    @Before
    public void setup(){
        jsonFile = new File("src/test/resources/vrp.json");
        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpJsonReader(vrpBuilder).read(jsonFile);
        vrp = vrpBuilder.build();
    }

    @Test
    public void noVehiclesShouldBeCorrect(){
        Assert.assertEquals(2,vrp.getVehicles().size());
    }

    @Test
    public void noJobsShouldBeCorrect(){
        Assert.assertEquals(2, vrp.getJobs().size());
    }

    @Test
    public void noTypesShouldBeCorrect(){
        Assert.assertEquals(2, vrp.getTypes().size());
    }

    @Test
    public void vehicle1ShouldBeOfTypeSmall(){
        Assert.assertEquals("small",getVehicle("v1").getType().getTypeId());
    }

    @Test
    public void vehicle2ShouldBeOfTypeMedium(){
        Assert.assertEquals("medium",getVehicle("v2").getType().getTypeId());
    }

    @Test
    public void vehicle1ShouldHaveCorrectStartAddress(){
        Assert.assertEquals("startLoc", getVehicle("v1").getStartLocationId());
    }

    @Test
    public void vehicle1ShouldHaveCorrectEndAddress(){
        Assert.assertEquals("endLoc",getVehicle("v1").getEndLocationId());
    }

    @Test
    public void vehicle1ShouldHaveCorrectEndLocCoordinate(){
        Assert.assertEquals(12.,getVehicle("v1").getEndLocationCoordinate().getX(),0.01);
        Assert.assertEquals(13.,getVehicle("v1").getEndLocationCoordinate().getY(),0.01);
    }

    @Test
    public void vehicle1ShouldHaveCorrectEarliestStart(){
        Assert.assertEquals(0.,getVehicle("v1").getEarliestDeparture());
    }

    @Test
    public void vehicle1ShouldHaveCorrectLatestStart(){
        Assert.assertEquals(1000., getVehicle("v1").getLatestArrival());
    }

    @Test
    public void vehicle1ShouldHaveCorrectSkills(){
        Assert.assertEquals("screw-driver", getVehicle("v1").getSkills().values().iterator().next());
    }

    @Test
    public void fleetSizeShouldBeCorrect(){
        Assert.assertTrue(VehicleRoutingProblem.FleetSize.FINITE.equals(vrp.getFleetSize()));
    }

    @Test
    public void returnToDepotShouldBeCorrect(){
        Assert.assertTrue(getVehicle("v1").isReturnToDepot());
    }

    @Test
    public void serviceTypeShouldBeCorrect(){
        Assert.assertEquals("pickup", getService("pickup2").getType());
    }

    @Test
    public void serviceNameShouldBeCorrect(){
        Assert.assertEquals("no-name", getService("pickup2").getName());
    }

    @Test
    public void startLocShouldBeCorrect(){
        Assert.assertEquals("s2_loc", getService("pickup2").getLocationId());
    }

    @Test
    public void coordinateShouldBeCorrect(){
        Assert.assertEquals(40., getService("pickup2").getCoord().getX());
        Assert.assertEquals(10., getService("pickup2").getCoord().getY());
    }

    @Test
    public void serviceDurationShouldBeCorrect(){
        Assert.assertEquals(2., getService("pickup2").getServiceDuration());
    }

    @Test
    public void serviceTWShouldBeCorrect(){
        Assert.assertEquals(10., getService("pickup2").getTimeWindow().getStart());
        Assert.assertEquals(200., getService("pickup2").getTimeWindow().getEnd());
    }

    @Test
    public void sizeShouldBeCorrect(){
        Assert.assertEquals(10, getService("pickup2").getSize().get(0));
        Assert.assertEquals(30, getService("pickup2").getSize().get(1));
    }

    @Test
    public void skillsShouldBeCorrect(){
        Assert.assertEquals("screw-driver", getService("pickup2").getRequiredSkills().values().iterator().next());
    }

    private Service getService(String jobId) {
        for(Job s : vrp.getJobs().values()){
            if(s.getId().equals(jobId)) return (Service)s;
        }
        return null;
    }

    @Test
    public void readMetaInfo(){
        ObjectMapper om = new ObjectMapper();
        try {
            JsonNode meta = om.readTree(jsonFile).path(JsonConstants.META);
            Assert.assertEquals(DistanceUnit.Meter.abbreviation(),meta.path(JsonConstants.MetaInfo.DISTANCE_UNIT).asText());
            Assert.assertEquals(TimeUnit.SEC.abbreviation(),meta.path(JsonConstants.MetaInfo.TIME_UNIT).asText());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Vehicle getVehicle(String id) {
        for(Vehicle v : vrp.getVehicles()){
            if(v.getId().equals(id)) return v;
        }
        return null;
    }

}
