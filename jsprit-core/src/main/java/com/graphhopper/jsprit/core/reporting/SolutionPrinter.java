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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.SizeDimension;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.AbstractActivity;
import com.graphhopper.jsprit.core.problem.solution.route.activity.JobActivity;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.reporting.DynamicTableDefinition.Alignment;
import com.graphhopper.jsprit.core.reporting.DynamicTableDefinition.ColumnDefinition;
import com.graphhopper.jsprit.core.reporting.DynamicTableDefinition.IntColumnType;
import com.graphhopper.jsprit.core.reporting.DynamicTableDefinition.LongColumnType;
import com.graphhopper.jsprit.core.reporting.DynamicTableDefinition.StringColumnType;


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

    /**
     * Prints costs and #vehicles to the given writer
     *
     * @param out      the destination writer
     * @param solution the solution to be printed
     */
    public static void print(PrintWriter out, VehicleRoutingProblem problem, VehicleRoutingProblemSolution solution, Print print) {

        DynamicTableDefinition problemTableDef = new DynamicTableDefinition.Builder()
                        .withHeading("Problem")
                        .addColumn(new ColumnDefinition.Builder(new StringColumnType(), "indicator")
                                        .build())
                        .addColumn(new ColumnDefinition.Builder(new StringColumnType(), "value")
                                        .build())
                        .build();

        DynamicTablePrinter problemTablePrinter = new DynamicTablePrinter(problemTableDef);
        problemTablePrinter.addRow().add("fleetsize").add(problem.getFleetSize());
        problemTablePrinter.addSeparator();
        problemTablePrinter.addRow().add("noJobs").add(problem.getJobs().values().size());
        for (Entry<Class<? extends Job>, Long> jc : getNuOfJobs(problem).entrySet()) {
            problemTablePrinter.addRow().add("   " + jc.getKey().getSimpleName())
            .add(jc.getValue());
        }
        out.println(problemTablePrinter.print());

        DynamicTableDefinition solutionTableDef = new DynamicTableDefinition.Builder()
                        .withHeading("Solution")
                        .addColumn(new ColumnDefinition.Builder(new StringColumnType(), "indicator")
                                        .build())
                        .addColumn(new ColumnDefinition.Builder(new StringColumnType(), "value")
                                        .build())
                        .build();

        DynamicTablePrinter solutionTablePrinter = new DynamicTablePrinter(solutionTableDef);
        solutionTablePrinter.addRow().add("costs")
        .add(String.format("%6.2f", solution.getCost()).trim());
        solutionTablePrinter.addRow().add("noVehicles").add(solution.getRoutes().size());
        solutionTablePrinter.addRow().add("unassgndJobs").add(solution.getUnassignedJobs().size());
        out.println(solutionTablePrinter.print());

        if (print.equals(Print.VERBOSE)) {
            printVerbose(out, problem, solution);
        }
    }


    private static void printVerbose(VehicleRoutingProblem problem, VehicleRoutingProblemSolution solution) {
        printVerbose(SYSTEM_OUT_AS_PRINT_WRITER, problem, solution);
        SYSTEM_OUT_AS_PRINT_WRITER.flush();
    }

    private static void printVerbose(PrintWriter out, VehicleRoutingProblem problem, VehicleRoutingProblemSolution solution) {

        DynamicTableDefinition tableDef = new DynamicTableDefinition.Builder()
                        .withHeading("Detailed solution")
                        .addColumn(new ColumnDefinition.Builder(new IntColumnType(), "route")
                                        .build())
                        .addColumn(new ColumnDefinition.Builder(new StringColumnType(), "vehicle")
                                        .build())
                        .addColumn(new ColumnDefinition.Builder(new StringColumnType(), "activity")
                                        .build())
                        .addColumn(new ColumnDefinition.Builder(new StringColumnType("-"), "job")
                                        .withMinWidth(10)
                                        .build())
                        .addColumn(new ColumnDefinition.Builder(new StringColumnType(), "load")
                                        .build())
                        .addColumn(new ColumnDefinition.Builder(new StringColumnType(), "location")
                                        .build())
                        .addColumn(new ColumnDefinition.Builder(new LongColumnType("-"), "arrTime")
                                        .withAlignment(Alignment.RIGHT)
                                        .build())
                        .addColumn(new ColumnDefinition.Builder(new LongColumnType("-"), "endTime")
                                        .withAlignment(Alignment.RIGHT)
                                        .build())
                        .addColumn(new ColumnDefinition.Builder(new LongColumnType(), "cost")
                                        .withAlignment(Alignment.RIGHT)
                                        .build())
                        .build();

        DynamicTablePrinter tablePrinter = new DynamicTablePrinter(tableDef);
        int routeNu = 1;

        List<VehicleRoute> list = new ArrayList<VehicleRoute>(solution.getRoutes());
        Collections.sort(list, new com.graphhopper.jsprit.core.util.VehicleIndexComparator());
        for (VehicleRoute route : list) {
            if (routeNu != 1) {
                tablePrinter.addSeparator();
            }

            double costs = 0;
            SizeDimension load = getInitialLoad(route);
            tablePrinter.addRow().add(routeNu).add(getVehicleString(route)).add(route.getStart().getName()).add(null)
            .add(getString(load))
            .add(getLocationString(route.getStart().getLocation()))
            .add(null)
            .add(Math.round(route.getStart().getEndTime())).add(Math.round(costs));

            TourActivity prevAct = route.getStart();
            for (TourActivity act : route.getActivities()) {
                String jobId;
                if (act instanceof JobActivity) {
                    jobId = ((JobActivity) act).getJob().getId();
                } else {
                    jobId = "-";
                }
                String type = (act instanceof AbstractActivity)
                                ? ((AbstractActivity) act).getType() : act.getName();
                                double c = problem.getTransportCosts().getTransportCost(prevAct.getLocation(), act.getLocation(), prevAct.getEndTime(), route.getDriver(),
                                                route.getVehicle());
                                c += problem.getActivityCosts().getActivityCost(act, act.getArrTime(), route.getDriver(), route.getVehicle());
                                costs += c;
                                load = load.add(act.getLoadChange());

                                tablePrinter.addRow().add(routeNu).add(getVehicleString(route)).add(type).add(jobId)
                                .add(getString(load)).add(getLocationString(act.getLocation()))
                                .add(Math.round(act.getArrTime()))
                                .add(Math.round(act.getEndTime())).add(Math.round(costs));
                                prevAct = act;
            }
            double c = problem.getTransportCosts().getTransportCost(prevAct.getLocation(), route.getEnd().getLocation(), prevAct.getEndTime(),
                            route.getDriver(), route.getVehicle());
            c += problem.getActivityCosts().getActivityCost(route.getEnd(), route.getEnd().getArrTime(), route.getDriver(), route.getVehicle());
            costs += c;

            tablePrinter.addRow().add(routeNu).add(getVehicleString(route))
            .add(route.getEnd().getName()).add(null)
            .add(getString(load))
            .add(getLocationString(route.getEnd().getLocation()))
            .add(null).add(Math.round(route.getEnd().getEndTime()))
            .add(Math.round(costs));

            routeNu++;
        }
        out.println(tablePrinter.print());


        if (!solution.getUnassignedJobs().isEmpty()) {

            DynamicTableDefinition unassignedTableDef = new DynamicTableDefinition.Builder()
                            .withHeading("Unassigned jobs")
                            .addColumn(new ColumnDefinition.Builder(new StringColumnType(),
                                            "id")
                                            .withMinWidth(10)
                                            .build())
                            .addColumn(new ColumnDefinition.Builder(new StringColumnType(),
                                            "type")
                                            .build())
                            .build();

            DynamicTablePrinter unassignedTablePrinter = new DynamicTablePrinter(unassignedTableDef);

            for (Job j : solution.getUnassignedJobs()) {
                unassignedTablePrinter.addRow().add(j.getId()).add(j.getClass().getSimpleName());
            }
            out.println(unassignedTablePrinter.print());
        }
    }

    private static String getLocationString(Location l) {
        if (l == null) {
            return null;
        } else {
            return l.getId();
        }
    }

    private static String getString(SizeDimension load) {
        String l = "[";
        for (int i = 0; i < load.getNuOfDimensions(); i++) {
            if (i > 0) {
                l += ", " + load.get(i);
            } else {
                l += load.get(i);
            }
        }
        l += "]";
        return l;
    }

    private static SizeDimension getInitialLoad(VehicleRoute route) {
        SizeDimension initialLoad = SizeDimension.EMPTY;
        for (TourActivity act : route.getActivities()) {
            initialLoad = initialLoad.add(act.getLoadChange());
        }
        return initialLoad.getNegativeDimensions().abs();
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
