/*******************************************************************************
 * Copyright (C) 2013  Stefan Schroeder
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
package com.graphhopper.jsprit.analysis.toolbox;

import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.listener.AlgorithmEndsListener;
import com.graphhopper.jsprit.core.algorithm.listener.AlgorithmStartsListener;
import com.graphhopper.jsprit.core.algorithm.listener.IterationEndsListener;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;


/**
 * VehicleRoutingAlgorithm-Listener to record the solution-search-progress.
 * <p>
 * <p>Register this listener in VehicleRoutingAlgorithm.
 *
 * @author stefan schroeder
 */

public class AlgorithmSearchProgressChartListener implements IterationEndsListener, AlgorithmEndsListener, AlgorithmStartsListener {

    private static Logger log = LoggerFactory.getLogger(AlgorithmSearchProgressChartListener.class);

    private String filename;

    private XYLineChartBuilder chartBuilder;

    /**
     * Constructs chart listener with target png-file (filename plus path).
     *
     * @param pngFileName
     */
    public AlgorithmSearchProgressChartListener(String pngFileName) {
        super();
        this.filename = pngFileName;
        if (!this.filename.endsWith("png")) {
            this.filename += ".png";
        }
    }

    @Override
    public void informAlgorithmEnds(VehicleRoutingProblem problem, Collection<VehicleRoutingProblemSolution> solutions) {
        log.info("create chart {}", filename);
        XYLineChartBuilder.saveChartAsPNG(chartBuilder.build(), filename);
    }

    @Override
    public void informIterationEnds(int i, VehicleRoutingProblem problem, Collection<VehicleRoutingProblemSolution> solutions) {
        double worst = 0.0;
        double best = Double.MAX_VALUE;
        double sum = 0.0;
        for (VehicleRoutingProblemSolution sol : solutions) {
            if (sol.getCost() > worst) worst = Math.min(sol.getCost(), Double.MAX_VALUE);
            if (sol.getCost() < best) best = sol.getCost();
            sum += Math.min(sol.getCost(), Double.MAX_VALUE);
        }
        chartBuilder.addData("best", i, best);
        chartBuilder.addData("worst", i, worst);
        chartBuilder.addData("avg", i, sum / (double) solutions.size());
    }


    @Override
    public void informAlgorithmStarts(VehicleRoutingProblem problem, VehicleRoutingAlgorithm algorithm, Collection<VehicleRoutingProblemSolution> solutions) {
        chartBuilder = XYLineChartBuilder.newInstance("search-progress", "iterations", "results");
    }

}
