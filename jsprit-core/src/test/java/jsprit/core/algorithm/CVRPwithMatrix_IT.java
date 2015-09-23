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
package jsprit.core.algorithm;

import jsprit.core.IntegrationTest;
import jsprit.core.algorithm.io.VehicleRoutingAlgorithms;
import jsprit.core.analysis.SolutionAnalyser;
import jsprit.core.problem.Location;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.cost.TransportDistance;
import jsprit.core.problem.io.VrpXMLReader;
import jsprit.core.problem.job.Job;
import jsprit.core.problem.job.Service;
import jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import jsprit.core.problem.vehicle.Vehicle;
import jsprit.core.problem.vehicle.VehicleImpl;
import jsprit.core.util.EuclideanDistanceCalculator;
import jsprit.core.util.FastVehicleRoutingTransportCostsMatrix;
import jsprit.core.util.Solutions;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.*;

public class CVRPwithMatrix_IT {

    private int index = 0;


    @Test
    @Category(IntegrationTest.class)
    public void whenReturnToDepot_itShouldWorkWithMatrix() {
        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(vrpBuilder).read("src/test/resources/vrpnc1-jsprit-with-deliveries.xml");
        VehicleRoutingProblem vrp_ = vrpBuilder.build();
        VehicleRoutingProblem vrp = createVrpWithLocationIndecesAndMatrix(vrp_, true);
        VehicleRoutingAlgorithm vra = VehicleRoutingAlgorithms.readAndCreateAlgorithm(vrp, "src/test/resources/algorithmConfig.xml");
        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();
        assertEquals(530.0, Solutions.bestOf(solutions).getCost(), 50.0);
        assertEquals(5, Solutions.bestOf(solutions).getRoutes().size());
    }

    @Test
    @Category(IntegrationTest.class)
    public void whenNotReturnToDepot_itShouldWorkWithMatrix() {
        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(vrpBuilder).read("src/test/resources/vrpnc1-jsprit-with-deliveries.xml");
        VehicleRoutingProblem vrp_ = vrpBuilder.build();
        VehicleRoutingProblem vrp = createVrpWithLocationIndecesAndMatrix(vrp_, false);
        VehicleRoutingAlgorithm vra = VehicleRoutingAlgorithms.readAndCreateAlgorithm(vrp, "src/test/resources/algorithmConfig.xml");
        try {
            Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();
            assertTrue(true);
        } catch (Exception e) {
            assertFalse(true);
        }
    }

    @Test
    @Category(IntegrationTest.class)
    public void whenCalcTimeWithSolutionAnalyser_itShouldWork() {
        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(vrpBuilder).read("src/test/resources/vrpnc1-jsprit-with-deliveries.xml");
        VehicleRoutingProblem vrp_ = vrpBuilder.build();
        final VehicleRoutingProblem vrp = createVrpWithLocationIndecesAndMatrix(vrp_, false);
        VehicleRoutingAlgorithm vra = VehicleRoutingAlgorithms.readAndCreateAlgorithm(vrp, "src/test/resources/algorithmConfig.xml");
        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();
        SolutionAnalyser sa = new SolutionAnalyser(vrp, Solutions.bestOf(solutions), new TransportDistance() {
            @Override
            public double getDistance(Location from, Location to) {
                return vrp.getTransportCosts().getTransportCost(from, to, 0., null, null);
            }
        });
        System.out.println(sa.getDistance());
        System.out.println(sa.getTransportTime());
    }


    private VehicleRoutingProblem createVrpWithLocationIndecesAndMatrix(VehicleRoutingProblem vrp_, boolean return_to_depot) {
        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        List<Location> locations = new ArrayList<Location>();
        for (Vehicle v : vrp_.getVehicles()) {
            Location l = Location.Builder.newInstance().setIndex(getIndex()).setId(v.getStartLocation().getId())
                .setCoordinate(v.getStartLocation().getCoordinate()).build();
            VehicleImpl.Builder newVehicleBuilder = VehicleImpl.Builder.newInstance(v.getId()).setType(v.getType())
                .setEarliestStart(v.getEarliestDeparture()).setLatestArrival(v.getLatestArrival())
                .setStartLocation(l).setReturnToDepot(return_to_depot);
            VehicleImpl newVehicle = newVehicleBuilder.build();
            vrpBuilder.addVehicle(newVehicle);
            locations.add(l);
        }
        for (Job j : vrp_.getJobs().values()) {
            Service s = (Service) j;
            Location l = Location.Builder.newInstance().setIndex(getIndex())
                .setId(s.getLocation().getId()).setCoordinate(s.getLocation().getCoordinate()).build();
            Service newService = Service.Builder.newInstance(s.getId()).setServiceTime(s.getServiceDuration())
                .addSizeDimension(0, s.getSize().get(0))
                .setLocation(l).build();
            vrpBuilder.addJob(newService);
            locations.add(l);
        }
        FastVehicleRoutingTransportCostsMatrix.Builder matrixBuilder = FastVehicleRoutingTransportCostsMatrix.Builder.newInstance(locations.size(), true);
        for (Location from : locations) {
            for (Location to : locations) {
                double distance = EuclideanDistanceCalculator.calculateDistance(from.getCoordinate(), to.getCoordinate());
                matrixBuilder.addTransportDistance(from.getIndex(), to.getIndex(), distance);
                matrixBuilder.addTransportTime(from.getIndex(), to.getIndex(), distance);
            }
        }
        vrpBuilder.setRoutingCost(matrixBuilder.build());
        return vrpBuilder.build();
    }


    public int getIndex() {
        int i = index;
        index++;
        return i;
    }
}
