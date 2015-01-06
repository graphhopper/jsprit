package jsprit.core.algorithm.state;

import jsprit.core.algorithm.VehicleRoutingAlgorithm;
import jsprit.core.algorithm.io.VehicleRoutingAlgorithms;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.io.VrpXMLReader;
import jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import jsprit.core.util.Solutions;
import org.junit.Test;

import java.util.Collection;

import static org.junit.Assert.assertEquals;

/**
 * Created by schroeder on 23.07.14.
 */
public class Solomon_IT {

    @Test
    public void itShouldFindTheBestKnownSolution(){
        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(vrpBuilder).read("src/test/resources/solomon_c101.xml");
        VehicleRoutingProblem vrp = vrpBuilder.build();

        VehicleRoutingAlgorithm vra = VehicleRoutingAlgorithms.readAndCreateAlgorithm(vrp,"src/test/resources/algorithmConfig.xml");
        vra.setMaxIterations(500);
        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();
        assertEquals(828.94, Solutions.bestOf(solutions).getCost(),0.01);
    }

}
