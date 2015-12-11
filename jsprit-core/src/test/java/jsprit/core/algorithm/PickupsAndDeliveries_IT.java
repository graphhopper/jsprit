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
import jsprit.core.algorithm.io.VehicleRoutingAlgorithms;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.io.VrpXMLReader;
import jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import jsprit.core.util.Solutions;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collection;

import static org.junit.Assert.assertEquals;

public class PickupsAndDeliveries_IT {

    @Test
    @Category(IntegrationTest.class)
    public void whenSolvingLR101InstanceOfLiLim_solutionsMustNoBeWorseThan5PercentOfBestKnownSolution() {
        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(vrpBuilder).read("src/test/resources/lilim_lr101.xml");
        VehicleRoutingProblem vrp = vrpBuilder.build();
        VehicleRoutingAlgorithm vra = VehicleRoutingAlgorithms.readAndCreateAlgorithm(vrp, "src/test/resources/lilim_algorithmConfig.xml");
        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();
        assertEquals(1650.8, Solutions.bestOf(solutions).getCost(), 80.);
        assertEquals(19, Solutions.bestOf(solutions).getRoutes().size(), 1);
    }

    @Test
    @Category(IntegrationTest.class)
    public void whenSolvingLR101InstanceOfLiLim_withJsprit_solutionsMustNoBeWorseThan5PercentOfBestKnownSolution() {
        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(vrpBuilder).read("src/test/resources/lilim_lr101.xml");
        VehicleRoutingProblem vrp = vrpBuilder.build();
        VehicleRoutingAlgorithm vra = Jsprit.createAlgorithm(vrp);
        vra.setMaxIterations(1000);
        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();
        assertEquals(1650.8, Solutions.bestOf(solutions).getCost(), 80.);
        assertEquals(19, Solutions.bestOf(solutions).getRoutes().size(), 1);
    }

}
