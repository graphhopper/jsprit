/*
 * Licensed to GraphHopper GmbH under one or more contributor
 * license agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * GraphHopper GmbH licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
