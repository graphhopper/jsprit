package jsprit.analysis.toolbox;

import jsprit.core.algorithm.VehicleRoutingAlgorithm;
import jsprit.core.algorithm.box.GreedySchrimpfFactory;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.io.VrpXMLReader;

import java.io.File;

/**
 * Created by stefan on 14.11.14.
 */
public class AnotherGraphTest {

    public static void main(String[] args) {
        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(vrpBuilder).read("/Users/stefan/Documents/git-repositories/jsprit/jsprit-examples/input/cordeau01.xml");
        VehicleRoutingProblem vrp = vrpBuilder.build();

//        GraphStreamEventWriter eventWriter = new GraphStreamEventWriter(new File("output/events.txt"));
        VehicleRoutingAlgorithm vra = new GreedySchrimpfFactory().createAlgorithm(vrp);
//        vra.addListener(eventWriter);

        vra.searchSolutions();

    }
}
