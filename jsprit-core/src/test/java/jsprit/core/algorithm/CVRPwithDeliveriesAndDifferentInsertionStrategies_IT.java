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
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.io.VrpXMLReader;
import jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import junit.framework.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collection;

public class CVRPwithDeliveriesAndDifferentInsertionStrategies_IT {

    @Test
    @Category(IntegrationTest.class)
    public void whenWithTwoInsertionStrategiesWhereOnleOneIsInAlgo_itShouldWork() {
        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(vrpBuilder).read("src/test/resources/vrpnc1-jsprit-with-deliveries.xml");
        VehicleRoutingProblem vrp = vrpBuilder.build();
        VehicleRoutingAlgorithm vra = VehicleRoutingAlgorithms.readAndCreateAlgorithm(vrp, "src/test/resources/algorithmConfig_greedyWithRegret.xml");
        vra.setMaxIterations(10);
        try {
            Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();
            Assert.assertTrue(true);
        } catch (Exception e) {
            Assert.assertTrue(false);
        }
    }

    @Test
    @Category(IntegrationTest.class)
    public void whenWithTwoInsertionStrategiesWhereBothAreInAlgo_itShouldWork() {
        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(vrpBuilder).read("src/test/resources/vrpnc1-jsprit-with-deliveries.xml");
        VehicleRoutingProblem vrp = vrpBuilder.build();
        VehicleRoutingAlgorithm vra = VehicleRoutingAlgorithms.readAndCreateAlgorithm(vrp, "src/test/resources/algorithmConfig_greedyWithRegret_v2.xml");
        vra.setMaxIterations(10);
        try {
            Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();
            Assert.assertTrue(true);
        } catch (Exception e) {
            Assert.assertTrue(false);
        }
    }

}
