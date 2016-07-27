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
package com.graphhopper.jsprit.analysis.util;

import com.graphhopper.jsprit.core.util.BenchmarkResult;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;

public class HtmlBenchmarkTableWriter implements BenchmarkWriter {

    private String filename;

    public HtmlBenchmarkTableWriter(String filename) {
        this.filename = filename;
    }

    @Override
    public void write(Collection<BenchmarkResult> results) {

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(new File(filename)));
            writer.write(openTable() + newline());
            //table head
            writer.write(openRow() + newline());
            writer.write(head("inst") + newline());
            writer.write(head("runs") + newline());
            writer.write(head("&Oslash; time [sec]") + newline());
            writer.write(head("results", 4));
            writer.write(head("vehicles", 4));
            writer.write(head("res*") + newline());
            writer.write(head("veh*") + newline());
            writer.write(closeRow() + newline());

            writer.write(openRow() + newline());
            writer.write(head("") + newline());
            writer.write(head("") + newline());
            writer.write(head("") + newline());
            writer.write(head("best") + newline());
            writer.write(head("avg") + newline());
            writer.write(head("worst") + newline());
            writer.write(head("stdev") + newline());
            writer.write(head("best") + newline());
            writer.write(head("avg") + newline());
            writer.write(head("worst") + newline());
            writer.write(head("stdev") + newline());
            writer.write(head("") + newline());
            writer.write(head("") + newline());
            writer.write(closeRow() + newline());

            //data
            double sum_avg_time = 0.0;
            double sum_best_result = 0.0;
            double sum_avg_result = 0.0;
            double sum_worst_result = 0.0;
            double sum_dev_result = 0.0;

            double sum_best_veh = 0.0;
            double sum_avg_veh = 0.0;
            double sum_worst_veh = 0.0;
            double sum_dev_veh = 0.0;

            Integer runs = null;
            Double sum_res_star = null;
            Double sum_veh_star = null;

            for (BenchmarkResult result : results) {
                if (runs == null) runs = result.runs;
                writer.write(openRow() + newline());
                writer.write(date(result.instance.name) + newline());
                writer.write(date(Integer.valueOf(result.runs).toString()) + newline());

                Double avg_time = round(result.getTimesStats().getMean(), 2);
                writer.write(date(Double.valueOf(avg_time).toString()) + newline());
                //bestRes
                Double best_result = round(result.getResultStats().getMin(), 2);
                writer.write(date(Double.valueOf(best_result).toString()) + newline());
                //avgRes
                Double avg_result = round(result.getResultStats().getMean(), 2);
                writer.write(date(Double.valueOf(avg_result).toString()) + newline());
                //worstRes
                Double worst_result = round(result.getResultStats().getMax(), 2);
                writer.write(date(Double.valueOf(worst_result).toString()) + newline());
                //stdevRes
                Double std_result = round(result.getResultStats().getStandardDeviation(), 2);
                writer.write(date(Double.valueOf(std_result).toString()) + newline());
                //bestVeh
                Double best_vehicle = round(result.getVehicleStats().getMin(), 2);
                writer.write(date(Double.valueOf(best_vehicle).toString()) + newline());
                //avgVeh
                Double avg_vehicle = round(result.getVehicleStats().getMean(), 2);
                writer.write(date(Double.valueOf(avg_vehicle).toString()) + newline());
                //worstVeh
                Double worst_vehicle = round(result.getVehicleStats().getMax(), 2);
                writer.write(date(Double.valueOf(worst_vehicle).toString()) + newline());
                //stdevVeh
                Double std_vehicle = round(result.getVehicleStats().getStandardDeviation(), 2);
                writer.write(date(Double.valueOf(std_vehicle).toString()) + newline());
                //bestKnownRes
                writer.write(date("" + result.instance.bestKnownResult + newline()));
                //bestKnownVeh
                writer.write(date("" + result.instance.bestKnownVehicles + newline()));
                writer.write(closeRow() + newline());

                sum_avg_time += avg_time;
                sum_best_result += best_result;
                sum_avg_result += avg_result;
                sum_worst_result += worst_result;
                sum_dev_result += std_result;

                sum_best_veh += best_vehicle;
                sum_avg_veh += avg_vehicle;
                sum_worst_veh += worst_vehicle;
                sum_dev_veh += std_vehicle;

                if (result.instance.bestKnownResult != null) {
                    if (sum_res_star == null) sum_res_star = result.instance.bestKnownResult;
                    else sum_res_star += result.instance.bestKnownResult;
                }
                if (result.instance.bestKnownVehicles != null) {
                    if (sum_veh_star == null) sum_veh_star = result.instance.bestKnownVehicles;
                    else sum_veh_star += result.instance.bestKnownVehicles;
                }

            }
            writer.write(openRow() + newline());
            writer.write(date("&Oslash;") + newline());
            writer.write(date("" + runs) + newline());

            Double average_time = round(sum_avg_time / (double) results.size(), 2);
            writer.write(date(Double.valueOf(average_time).toString()) + newline());
            //bestRes
            writer.write(date(Double.valueOf(round(sum_best_result / (double) results.size(), 2)).toString()) + newline());
            //avgRes
            Double average_result = round(sum_avg_result / (double) results.size(), 2);
            writer.write(date(Double.valueOf(average_result).toString()) + newline());
            //worstRes
            writer.write(date(Double.valueOf(round(sum_worst_result / (double) results.size(), 2)).toString()) + newline());
            //stdevRes
            writer.write(date(Double.valueOf(round(sum_dev_result / (double) results.size(), 2)).toString()) + newline());
            //bestVeh
            writer.write(date(Double.valueOf(round(sum_best_veh / (double) results.size(), 2)).toString()) + newline());
            //avgVeh
            Double average_vehicles = round(sum_avg_veh / (double) results.size(), 2);
            writer.write(date(Double.valueOf(average_vehicles).toString()) + newline());
            //worstVeh
            writer.write(date(Double.valueOf(round(sum_worst_veh / (double) results.size(), 2)).toString()) + newline());
            //stdevVeh
            writer.write(date(Double.valueOf(round(sum_dev_veh / (double) results.size(), 2)).toString()) + newline());
            //bestKnownRes
            Double delta_res = null;
            if (sum_res_star != null) {
                writer.write(date(Double.valueOf(round(sum_res_star.doubleValue() / (double) results.size(), 2)).toString()) + newline());
                delta_res = (sum_avg_result / sum_res_star - 1) * 100;
            } else writer.write(date("null") + newline());
            //bestKnownVeh
            Double delta_veh = null;
            if (sum_veh_star != null) {
                writer.write(date(Double.valueOf(round(sum_veh_star.doubleValue() / (double) results.size(), 2)).toString()) + newline());
                delta_veh = (sum_avg_veh - sum_veh_star) / (double) results.size();
            } else writer.write(date("null") + newline());
            writer.write(closeRow() + newline());

            writer.write(closeTable() + newline());

            writer.write("avg. percentage deviation to best-known result: " + round(delta_res, 2) + newline() + newline());
            writer.write("avg. absolute deviation to best-known vehicles: " + round(delta_veh, 2) + newline());

            writer.write(openTable() + newline());
            writer.write(openRow() + newline());
            writer.write(date("") + newline());
            writer.write(date("") + newline());
            writer.write(date("") + newline());
            writer.write(date("") + newline());
            writer.write(date(Double.valueOf(average_time).toString(), "align=\"right\"") + newline());
            writer.write(date(Double.valueOf(average_result).toString(), "align=\"right\"") + newline());
            writer.write(date(Double.valueOf(average_vehicles).toString(), "align=\"right\"") + newline());
            if (delta_res != null) {
                writer.write(date(Double.valueOf(round(delta_res, 2)).toString(), "align=\"right\"") + newline());
            } else writer.write(date("n.a.") + newline());
            if (delta_veh != null) {
                writer.write(date(Double.valueOf(round(delta_veh, 2)).toString(), "align=\"right\"") + newline());
            } else writer.write(date("n.a.") + newline());
            writer.write(closeRow() + newline());
            writer.write(closeTable() + newline());


            writer.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    private String head(String string, int i) {
        return "<th colspan=\"" + i + "\">" + string + "</th>";
    }

    private Double round(Double value, int i) {
        if (value == null) return null;
        long roundedVal = Math.round(value * Math.pow(10, i));
        return (double) roundedVal / (double) (Math.pow(10, i));
    }

    private String head(String head) {
        return "<th>" + head + "</th>";
    }

    private String closeTable() {
        return "</table>";
    }

    private String openTable() {
        return "<table>";
    }

    private String closeRow() {
        return "</tr>";
    }

    private String date(String date) {
        return "<td>" + date + "</td>";
    }

    private String date(String date, String metaData) {
        return "<td " + metaData + ">" + date + "</td>";
    }

    private String newline() {
        return "\n";
    }

    private String openRow() {
        return "<tr>";
    }


}
