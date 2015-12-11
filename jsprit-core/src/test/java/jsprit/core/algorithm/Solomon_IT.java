package jsprit.core.algorithm;

import jsprit.core.IntegrationTest;
import jsprit.core.algorithm.box.Jsprit;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.io.VrpXMLReader;
import jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import jsprit.core.util.Solutions;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collection;

import static org.junit.Assert.assertEquals;

/**
 * Created by schroeder on 23.07.14.
 */
public class Solomon_IT {

    @Test
    @Category(IntegrationTest.class)
    public void itShouldFindTheBestKnownSolution() {
        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(vrpBuilder).read("src/test/resources/solomon_c101.xml");
        VehicleRoutingProblem vrp = vrpBuilder.build();

        VehicleRoutingAlgorithm vra = Jsprit.Builder.newInstance(vrp)
//            .setProperty(Jsprit.Parameter.THREADS,"3")
//            .setProperty(Jsprit.Parameter.FAST_REGRET,"true")
            .buildAlgorithm();
//            VehicleRoutingAlgorithms.readAndCreateAlgorithm(vrp, "src/test/resources/algorithmConfig.xml");
        vra.setMaxIterations(2000);
        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();
        assertEquals(828.94, Solutions.bestOf(solutions).getCost(), 0.01);
    }

}
