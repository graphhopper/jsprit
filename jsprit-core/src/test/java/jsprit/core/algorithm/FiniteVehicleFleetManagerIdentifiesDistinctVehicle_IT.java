/*******************************************************************************
 * Copyright (c) 2014 Stefan Schroeder.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     Stefan Schroeder - initial API and implementation
 ******************************************************************************/
package jsprit.core.algorithm;

import jsprit.core.IntegrationTest;
import jsprit.core.algorithm.box.Jsprit;
import jsprit.core.algorithm.box.SchrimpfFactory;
import jsprit.core.algorithm.recreate.NoSolutionFoundException;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.io.VrpXMLReader;
import jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class FiniteVehicleFleetManagerIdentifiesDistinctVehicle_IT {

    @Test
    @Category(IntegrationTest.class)
    public void whenEmployingVehicleWhereOnlyOneDistinctVehicleCanServeAParticularJob_algorithmShouldFoundDistinctSolution() {
        final List<Boolean> testFailed = new ArrayList<Boolean>();
        for (int i = 0; i < 10; i++) {
            VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
            new VrpXMLReader(vrpBuilder).read("src/test/resources/biggerProblem.xml");
            VehicleRoutingProblem vrp = vrpBuilder.build();

            VehicleRoutingAlgorithm vra = new SchrimpfFactory().createAlgorithm(vrp);
            vra.setMaxIterations(10);
            try {
                @SuppressWarnings("unused")
                Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();
            } catch (NoSolutionFoundException e) {
                testFailed.add(true);
            }
        }
        System.out.println("failed: " + testFailed.size());
        assertTrue(testFailed.isEmpty());
    }

    @Test
    public void whenEmployingVehicleWhereOnlyOneDistinctVehicleCanServeAParticularJobWith_jspritAlgorithmShouldFoundDistinctSolution() {
        final List<Boolean> testFailed = new ArrayList<Boolean>();
        for (int i = 0; i < 10; i++) {
            VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
            new VrpXMLReader(vrpBuilder).read("src/test/resources/biggerProblem.xml");
            VehicleRoutingProblem vrp = vrpBuilder.build();

            VehicleRoutingAlgorithm vra = Jsprit.createAlgorithm(vrp);
            vra.setMaxIterations(10);
            try {
                @SuppressWarnings("unused")
                Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();
            } catch (NoSolutionFoundException e) {
                testFailed.add(true);
            }
        }
        System.out.println("failed: " + testFailed.size());
        assertTrue(testFailed.isEmpty());
    }

}
