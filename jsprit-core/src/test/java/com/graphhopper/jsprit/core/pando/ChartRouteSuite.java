package com.graphhopper.jsprit.core.pando;

import org.junit.Test;

import java.io.IOException;

import static com.graphhopper.jsprit.core.pando.ChartRouteSolutionFinder.runChartRouteAlgo;

public class ChartRouteSuite {
    @Test
    public void testRouteFormation() throws IOException {
        runChartRouteAlgo();
    }
}
