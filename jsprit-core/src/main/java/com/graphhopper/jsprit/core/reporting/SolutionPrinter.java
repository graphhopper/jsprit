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

import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.reporting.ConfigurableTablePrinter.CsvConfig;
import com.graphhopper.jsprit.core.reporting.columndefinition.ColumnDefinition;
import com.graphhopper.jsprit.core.reporting.columndefinition.SolutionPrintColumnLists;
import com.graphhopper.jsprit.core.reporting.columndefinition.StringColumnType;
import com.graphhopper.jsprit.core.reporting.columndefinition.SolutionPrintColumnLists.PredefinedList;
import com.graphhopper.jsprit.core.reporting.route.RoutePrinterContext;


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

    public static void print(PrintWriter out, VehicleRoutingProblem problem, VehicleRoutingProblemSolution solution, Print print) {
        print(out, problem, solution, print, SolutionPrintColumnLists.getNumeric(PredefinedList.DEFAULT));
    }

    public static void print(VehicleRoutingProblem problem, VehicleRoutingProblemSolution solution,
                    PrinterColumnList<RoutePrinterContext> verbosePrintColumns) {
        print(SYSTEM_OUT_AS_PRINT_WRITER, problem, solution, verbosePrintColumns);
        SYSTEM_OUT_AS_PRINT_WRITER.flush();
    }

    public static void print(PrintWriter out, VehicleRoutingProblem problem, VehicleRoutingProblemSolution solution,
                    PrinterColumnList<RoutePrinterContext> verbosePrintColumns) {
        print(out, problem, solution, Print.VERBOSE, verbosePrintColumns);
    }

    /**
     * Prints costs and #vehicles to the given writer
     *
     * @param out      the destination writer
     * @param solution the solution to be printed
     */
    public static void print(PrintWriter out, VehicleRoutingProblem problem, VehicleRoutingProblemSolution solution, Print print,
                    PrinterColumnList<RoutePrinterContext> verbosePrintColumns) {

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
            printVerbose(out, problem, solution, verbosePrintColumns);
        }
    }


    private static void printVerbose(PrintWriter out, VehicleRoutingProblem problem, VehicleRoutingProblemSolution solution,
                    PrinterColumnList<RoutePrinterContext> columns) {

        ConfigurableTablePrinter<RoutePrinterContext> tablePrinter = buildTablePrinter(problem, solution, columns);
        out.println(tablePrinter.print());

        if (!solution.getUnassignedJobs().isEmpty()) {

            DynamicTableDefinition unassignedTableDef = new DynamicTableDefinition.Builder().withHeading("Unassigned jobs")
                            .addColumn(new ColumnDefinition.Builder(new StringColumnType(), "id").withMinWidth(10).build())
                            .addColumn(new ColumnDefinition.Builder(new StringColumnType(), "type").build()).build();

            DynamicTablePrinter unassignedTablePrinter = new DynamicTablePrinter(unassignedTableDef);

            for (Job j : solution.getUnassignedJobs()) {
                unassignedTablePrinter.addRow().add(j.getId()).add(j.getClass().getSimpleName());
            }
            out.println(unassignedTablePrinter.print());
        }
    }

    protected static ConfigurableTablePrinter<RoutePrinterContext> buildTablePrinter(VehicleRoutingProblem problem,
                    VehicleRoutingProblemSolution solution, PrinterColumnList<RoutePrinterContext> columns) {
        ConfigurableTablePrinter<RoutePrinterContext> tablePrinter = new ConfigurableTablePrinter<>(columns);
        int routeNu = 1;

        List<VehicleRoute> list = new ArrayList<>(solution.getRoutes());
        Collections.sort(list, new com.graphhopper.jsprit.core.util.VehicleIndexComparator());
        for (VehicleRoute route : list) {
            if (routeNu != 1) {
                tablePrinter.addSeparator();
            }

            RoutePrinterContext context = new RoutePrinterContext(routeNu, route, route.getStart(), problem);
            tablePrinter.addRow(context);

            for (TourActivity act : route.getActivities()) {
                context.setActivity(act);
                tablePrinter.addRow(context);
            }

            context.setActivity(route.getEnd());
            tablePrinter.addRow(context);

            routeNu++;
        }
        return tablePrinter;
    }

    private static Map<Class<? extends Job>, Long> getNuOfJobs(VehicleRoutingProblem problem) {
        return problem.getJobs().values().stream()
                        .map(j -> (Class<? extends Job>) j.getClass())
                        .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
    }

    public static String export(VehicleRoutingProblem problem, VehicleRoutingProblemSolution solution,
                    PrinterColumnList<RoutePrinterContext> columns, CsvConfig csvConfig) {
        ConfigurableTablePrinter<RoutePrinterContext> table = buildTablePrinter(problem, solution, columns);
        return table.exportToCsv(csvConfig);
    }

}
