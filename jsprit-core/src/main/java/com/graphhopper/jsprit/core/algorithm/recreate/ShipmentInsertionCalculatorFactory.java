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

package com.graphhopper.jsprit.core.algorithm.recreate;

import com.graphhopper.jsprit.core.problem.JobActivityFactory;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.constraint.ConstraintManager;

public class ShipmentInsertionCalculatorFactory implements JobInsertionCostsCalculatorFactory {

    private InsertionPositionFilter positionFilter;

    private ParallelInsertionScan parallelScan;

    /** Scans the pickup positions of long routes on the algorithm's ForkJoinPool, see {@link ParallelInsertionScan}; null keeps the sequential scan. */
    public ShipmentInsertionCalculatorFactory setParallelScan(ParallelInsertionScan parallelScan) {
        this.parallelScan = parallelScan;
        return this;
    }

    /**
     * Sets the position filter for reducing position evaluations in shipment insertion.
     *
     * @param filter the position filter, or null to disable filtering
     * @return this factory for chaining
     */
    public ShipmentInsertionCalculatorFactory setPositionFilter(InsertionPositionFilter filter) {
        this.positionFilter = filter;
        return this;
    }

    @Override
    public JobInsertionCostsCalculator create(VehicleRoutingProblem vrp, ActivityInsertionCostsCalculator activityInsertionCostsCalculator, JobActivityFactory jobActivityFactory, ConstraintManager constraintManager) {
        ShipmentInsertionCalculator calculator = new ShipmentInsertionCalculator(vrp.getTransportCosts(), vrp.getActivityCosts(), activityInsertionCostsCalculator, constraintManager, jobActivityFactory);
        if (positionFilter != null) {
            calculator.setPositionFilter(positionFilter);
        }
        if (parallelScan != null) {
            calculator.setParallelScan(parallelScan);
        }
        return calculator;
    }
}
