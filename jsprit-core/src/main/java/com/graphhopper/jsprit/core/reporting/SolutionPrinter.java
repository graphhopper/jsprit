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
package com.graphhopper.jsprit.core.reporting;

import java.io.PrintWriter;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.JobActivity;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;


/**
 * Printer to print the details of a vehicle-routing-problem solution.
 *
 * @author stefan schroeder
 */
public class SolutionPrinter {

    // Wrapping System.out into a PrintWriter
    private static final PrintWriter SYSTEM_OUT_AS_PRINT_WRITER = new PrintWriter(System.out);

    /**
     * Enum to indicate verbose-level.
     * <p>
     * <p>
     * Print.CONCISE and Print.VERBOSE are available.
     *
     * @author stefan schroeder
     */
    public enum Print {

        CONCISE, VERBOSE
    }


    /**
     * Prints costs and #vehicles to stdout (out.println).
     *
     * @param solution the solution to be printed
     */
    public static void print(VehicleRoutingProblemSolution solution) {
        print(SYSTEM_OUT_AS_PRINT_WRITER, solution);
        SYSTEM_OUT_AS_PRINT_WRITER.flush();
    }

    /**
     * Prints costs and #vehicles to the given writer
     *
     * @param out      the destination writer
     * @param solution the solution to be printed
     */
    public static void print(PrintWriter out, VehicleRoutingProblemSolution solution) {
        out.println("[costs=" + solution.getCost() + "]");
        out.println("[#vehicles=" + solution.getRoutes().size() + "]");
    }

    /**
     * Prints costs and #vehicles to the to stdout (out.println).
     *
     * @param out      the destination writer
     * @param solution the solution to be printed
     */
    public static void print(VehicleRoutingProblem problem, VehicleRoutingProblemSolution solution, Print print) {
        print(SYSTEM_OUT_AS_PRINT_WRITER, problem, solution, print);
        SYSTEM_OUT_AS_PRINT_WRITER.flush();
    }

    protected static String drawLineBase(String pattern, int count) {
        Object[] par = new Object[count];
        Arrays.fill(par, "");
        String text = String.format(pattern, par);
        return text;
    }

    protected static String drawLine(String pattern, int count) {
        String text = drawLineBase(pattern, count);
        return text.replaceAll(" ", "-").replaceAll(Pattern.quote("|"), "+");
    }


    protected static String drawHeading(String pattern, int count, String text) {
        String base = drawLineBase(pattern, count).trim();
        int internalWidth = base.lastIndexOf('|') - base.indexOf('|') - 1;
        if (text == null) {
            return "+" + CharBuffer.allocate(internalWidth).toString().replace('\0', '-') + "+\n";
        } else {
            return "| " + String.format("%-" + (internalWidth - 2) + "s", text) + " |\n";
        }
    }

    /**
     * Prints costs and #vehicles to the given writer
     *
     * @param out      the destination writer
     * @param solution the solution to be printed
     */
    public static void print(PrintWriter out, VehicleRoutingProblem problem, VehicleRoutingProblemSolution solution, Print print) {
        String leftAlign = "| %-30s | %-8s |%n";

        out.printf(drawHeading(leftAlign, 2, null));
        out.printf(drawHeading(leftAlign, 2, "problem"));
        out.printf(drawLine(leftAlign, 2));
        out.format(leftAlign, "indicator", "value");
        out.printf(drawLine(leftAlign, 2));

        out.format(leftAlign, "noJobs", problem.getJobs().values().size());
        getNuOfJobs(problem).entrySet().forEach(en -> out.format(leftAlign, "   " + en.getKey().getSimpleName(), en.getValue()));
        out.format(leftAlign, "fleetsize", problem.getFleetSize().toString());
        out.printf(drawLine(leftAlign, 2));


        String leftAlignSolution = "| %-13s | %-40s |%n";
        out.printf(drawHeading(leftAlignSolution, 2, null));
        out.printf(drawHeading(leftAlignSolution, 2, "solution"));
        out.printf(drawLine(leftAlignSolution, 2));
        out.format(leftAlignSolution, "indicator", "value");
        out.printf(drawLine(leftAlignSolution, 2));
        out.format(leftAlignSolution, "costs", solution.getCost());
        out.format(leftAlignSolution, "noVehicles", solution.getRoutes().size());
        out.format(leftAlignSolution, "unassgndJobs", solution.getUnassignedJobs().size());
        out.printf(drawLine(leftAlignSolution, 2));

        if (print.equals(Print.VERBOSE)) {
            printVerbose(out, problem, solution);
        }
    }


    private static void printVerbose(VehicleRoutingProblem problem, VehicleRoutingProblemSolution solution) {
        printVerbose(SYSTEM_OUT_AS_PRINT_WRITER, problem, solution);
        SYSTEM_OUT_AS_PRINT_WRITER.flush();
    }

    private static void printVerbose(PrintWriter out, VehicleRoutingProblem problem, VehicleRoutingProblemSolution solution) {
        String leftAlgin = "| %-7s | %-20s | %-21s | %-15s | %-15s | %-15s | %-15s |%n";
        out.format("+--------------------------------------------------------------------------------------------------------------------------------+%n");
        out.printf("| detailed solution                                                                                                              |%n");
        out.format("+---------+----------------------+-----------------------+-----------------+-----------------+-----------------+-----------------+%n");
        out.printf("| route   | vehicle              | activity              | job             | arrTime         | endTime         | costs           |%n");
        int routeNu = 1;

        List<VehicleRoute> list = new ArrayList<VehicleRoute>(solution.getRoutes());
        Collections.sort(list , new com.graphhopper.jsprit.core.util.VehicleIndexComparator());
        for (VehicleRoute route : list) {
            out.format("+---------+----------------------+-----------------------+-----------------+-----------------+-----------------+-----------------+%n");
            double costs = 0;
            out.format(leftAlgin, routeNu, getVehicleString(route), route.getStart().getName(), "-", "undef", Math.round(route.getStart().getEndTime()),
                    Math.round(costs));
            TourActivity prevAct = route.getStart();
            for (TourActivity act : route.getActivities()) {
                String jobId;
                if (act instanceof JobActivity) {
                    jobId = ((JobActivity) act).getJob().getId();
                } else {
                    jobId = "-";
                }
                double c = problem.getTransportCosts().getTransportCost(prevAct.getLocation(), act.getLocation(), prevAct.getEndTime(), route.getDriver(),
                        route.getVehicle());
                c += problem.getActivityCosts().getActivityCost(act, act.getArrTime(), route.getDriver(), route.getVehicle());
                costs += c;
                out.format(leftAlgin, routeNu, getVehicleString(route), act.getName(), jobId, Math.round(act.getArrTime()),
                        Math.round(act.getEndTime()), Math.round(costs));
                prevAct = act;
            }
            double c = problem.getTransportCosts().getTransportCost(prevAct.getLocation(), route.getEnd().getLocation(), prevAct.getEndTime(),
                    route.getDriver(), route.getVehicle());
            c += problem.getActivityCosts().getActivityCost(route.getEnd(), route.getEnd().getArrTime(), route.getDriver(), route.getVehicle());
            costs += c;
            out.format(leftAlgin, routeNu, getVehicleString(route), route.getEnd().getName(), "-", Math.round(route.getEnd().getArrTime()), "undef",
                    Math.round(costs));
            routeNu++;
        }
        out.format("+--------------------------------------------------------------------------------------------------------------------------------+%n");
        if (!solution.getUnassignedJobs().isEmpty()) {
            out.format("+----------------+%n");
            out.format("| unassignedJobs |%n");
            out.format("+----------------+%n");
            String unassignedJobAlgin = "| %-14s |%n";
            for (Job j : solution.getUnassignedJobs()) {
                out.format(unassignedJobAlgin, j.getId());
            }
            out.format("+----------------+%n");
        }
    }

    private static String getVehicleString(VehicleRoute route) {
        return route.getVehicle().getId();
    }

    private static Map<Class<? extends Job>, Long> getNuOfJobs(VehicleRoutingProblem problem) {
        return problem.getJobs().values().stream()
                .map(j -> (Class<? extends Job>) j.getClass())
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
    }

}
