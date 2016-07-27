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
package com.graphhopper.jsprit.core.util;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;


public class BenchmarkResult {
    private double[] results;
    private double[] vehicles;
    private double[] times;

    private DescriptiveStatistics statsResults;
    private DescriptiveStatistics statsVehicles;
    private DescriptiveStatistics statsTimes;

    public final BenchmarkInstance instance;

    public final int runs;

    public BenchmarkResult(BenchmarkInstance instance, int runs, double[] results, double[] compTimes, double[] vehicles) {
        super();
        this.results = results;
        this.runs = runs;
        this.times = compTimes;
        this.instance = instance;
        this.vehicles = vehicles;
        this.statsResults = new DescriptiveStatistics(results);
        this.statsTimes = new DescriptiveStatistics(times);
        this.statsVehicles = new DescriptiveStatistics(vehicles);
    }

    public double[] getResults() {
        return results;
    }

    public double[] getVehicles() {
        return vehicles;
    }

    public double[] getCompTimes() {
        return times;
    }

    public DescriptiveStatistics getResultStats() {
        return statsResults;
    }

    public DescriptiveStatistics getVehicleStats() {
        return statsVehicles;
    }

    public DescriptiveStatistics getTimesStats() {
        return statsTimes;
    }

}
