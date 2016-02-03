package com.graphhopper.jsprit.core.algorithm;

import com.graphhopper.jsprit.core.algorithm.io.VehicleRoutingAlgorithms;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.io.VrpXMLReader;
import org.junit.Test;

import static org.junit.Assert.fail;

public class SelectRandomlyTest {

    @Test
    public void loadAnAlgorithmWithSelectRandomly() {
        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(vrpBuilder).read("src/test/resources/simpleProblem.xml");
        VehicleRoutingProblem vrp = vrpBuilder.build();
        try {
            VehicleRoutingAlgorithms.readAndCreateAlgorithm(vrp, "src/test/resources/algorithmConfig_selectRandomly.xml");
        } catch (Exception e) {
            e.printStackTrace();
            fail("Should be able to load an algorithm that uses <selector name=\"selectRandomly\"/>: " + e.getMessage());
        }
    }
}
