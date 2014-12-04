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
import jsprit.core.algorithm.box.GreedySchrimpfFactory;
import jsprit.core.analysis.SolutionAnalyser;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.job.Service;
import jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import jsprit.core.problem.vehicle.VehicleImpl;
import jsprit.core.problem.vehicle.VehicleType;
import jsprit.core.problem.vehicle.VehicleTypeImpl;
import jsprit.core.util.Coordinate;
import jsprit.core.util.Solutions;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

/**
 * Created by schroeder on 04.12.14.
 */
public class VrpJsonWriterTest {

    private VehicleRoutingProblem vrp;

    private VehicleRoutingProblemSolution solution;

    private JsonNode root;

    @Before
    public void doBefore(){
        VehicleType type = VehicleTypeImpl.Builder.newInstance("type").addCapacityDimension(0,20).build();
        VehicleImpl v = VehicleImpl.Builder.newInstance("v").setStartLocationId("start").setStartLocationCoordinate(Coordinate.newInstance(10,40))
                .setEndLocationId("end").setEndLocationCoordinate(Coordinate.newInstance(50,20)).setReturnToDepot(true)
                .setType(type)
                .build();
        Service s = Service.Builder.newInstance("s").setCoord(Coordinate.newInstance(50,60)).addSizeDimension(0,10).build();
        vrp = VehicleRoutingProblem.Builder.newInstance().addVehicle(v).addJob(s).build();
        solution = Solutions.bestOf(new GreedySchrimpfFactory().createAlgorithm(vrp).searchSolutions());
        File out = new File("src/test/resources/vrp-solution.json");
        new VrpJsonWriter(vrp,solution,new SolutionAnalyser.DistanceCalculator() {
            @Override
            public double getDistance(String fromLocationId, String toLocationId) {
                return vrp.getTransportCosts().getTransportCost(fromLocationId,toLocationId,0.,null,null);
            }
        }).write(out);
        ObjectMapper om = new ObjectMapper();
        try {
            root = om.readTree(out).path("solution");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void solutionValShouldBeCorrect(){
        Assert.assertEquals(solution.getCost(),root.path("costs").asDouble(),0.01);
    }

    @Test
    public void fixShouldBeCorrect(){
        Assert.assertEquals(0.,root.path("fixed_costs").asDouble(),0.01);
    }

    @Test
    public void varShouldBeCorrect(){
        Assert.assertEquals(solution.getCost(),root.path("variable_costs").asDouble(),0.01);
    }

    @Test
    public void timeShouldBeCorrect(){
        Assert.assertEquals(solution.getCost(),root.path("time").asDouble(),0.01);
    }

    @Test
    public void distanceShouldBeCorrect(){
        Assert.assertEquals(solution.getCost(),root.path("distance").asDouble(),0.01);
    }

    @Test
    public void noRoutesShouldBeCorrect(){
        Assert.assertEquals(solution.getRoutes().size(),root.path("no_routes").asDouble(),0.01);
    }

    @Test
    public void noUnassignedShouldBeCorrect(){
        Assert.assertEquals(solution.getUnassignedJobs().size(),root.path("no_unassigned").asDouble(),0.01);
    }



}
