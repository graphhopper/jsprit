package com.graphhopper.jsprit.core.algorithm;

import com.graphhopper.jsprit.core.IntegrationTest;
import com.graphhopper.jsprit.core.algorithm.box.Jsprit;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.io.VrpXMLReader;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.util.Solutions;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collection;

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
        Assert.assertEquals(828.94, Solutions.bestOf(solutions).getCost(), 0.01);
    }

}
