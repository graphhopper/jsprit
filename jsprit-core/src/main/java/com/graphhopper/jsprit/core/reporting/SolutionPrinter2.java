package com.graphhopper.jsprit.core.reporting;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.graphhopper.jsprit.core.algorithm.objectivefunction.ComponentValue;
import com.graphhopper.jsprit.core.algorithm.objectivefunction.RouteLevelComponentValue;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem.FleetSize;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.reporting.RouteDetailsConfig.Column;
import com.graphhopper.jsprit.core.reporting.RouteDetailsConfig.DisplayMode;

import hu.vissy.texttable.BorderFormatter;
import hu.vissy.texttable.BorderFormatter.DefaultFormatters;
import hu.vissy.texttable.TableFormatter;
import hu.vissy.texttable.TableFormatter.Builder;
import hu.vissy.texttable.column.ColumnDefinition;
import hu.vissy.texttable.contentformatter.CellContentFormatter;
import hu.vissy.texttable.dataconverter.NumberDataConverter;
import hu.vissy.texttable.dataextractor.StatefulDataExtractor;


public class SolutionPrinter2 {

    private static class Entry {
        String key;
        String value;

        public Entry(String key, Object value) {
            super();
            this.key = key;
            this.value = "" + value;
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }
    }

    private static class Aggregator {
        double sum;
    }

    // Wrapping System.out into a PrintWriter
    private static final PrintWriter SYSTEM_OUT_AS_PRINT_WRITER = new PrintWriter(System.out);

    public static void print(VehicleRoutingProblem problem,
            VehicleRoutingProblemSolution solution) {
        print(SYSTEM_OUT_AS_PRINT_WRITER, problem, solution);
    }

    public static void print(PrintWriter out, VehicleRoutingProblem problem,
            VehicleRoutingProblemSolution solution) {
        printProblemTable(out, problem);
        printSolutionSummary(out, solution);
        printCostDetails(out, solution);
        printRouteDetails(out, problem, solution);
        printVehicleSummary(out, problem, solution);
        out.flush();
    }



    private static void printProblemTable(PrintWriter out, VehicleRoutingProblem problem) {
        TableFormatter<Entry> problemTableDef = createKeyValueTable("Problem");

        List<Entry> data = new ArrayList<>();
        data.add(new Entry("fleetsize", problem.getFleetSize()));
        data.add(new Entry("maxNoVehicles", problem.getFleetSize() == FleetSize.FINITE
                ? problem.getVehicles().size() : "unlimited"));
        data.add(null);
        data.add(new Entry("noJobs", problem.getJobs().values().size()));
        for (Map.Entry<Class<? extends Job>, Long> jc : getNuOfJobs(problem).entrySet()) {
            data.add(new Entry("   " + jc.getKey().getSimpleName(), jc.getValue()));
        }

        out.println(problemTableDef.apply(data));
    }

    private static void printSolutionSummary(PrintWriter out,
            VehicleRoutingProblemSolution solution) {
        TableFormatter<Entry> problemTableDef = createKeyValueTable("Solution");

        List<Entry> data = new ArrayList<>();
        data.add(new Entry("costs", String.format("%6.2f", solution.getCost()).trim()));
        data.add(new Entry("noVehicles", solution.getRoutes().size()));
        data.add(new Entry("unassignedJobs", solution.getUnassignedJobs().size()));

        out.println(problemTableDef.apply(data));
    }


    private static void printCostDetails(PrintWriter out, VehicleRoutingProblemSolution solution) {
        printCostComponents(out, solution);
        printPerRouteCosts(out, solution);
    }

    private static void printCostComponents(PrintWriter out,
            VehicleRoutingProblemSolution solution) {
        TableFormatter<ComponentValue> tableDef = new TableFormatter.Builder<ComponentValue>()
                .withBorderFormatter(new BorderFormatter.Builder(
                        DefaultFormatters.ASCII_LINEDRAW).build())
                .withHeading("Cost components")
                .withColumn(ColumnDefinition.<ComponentValue, String>createSimpleStateless(
                        "component id", c -> c.getKey()))
                .withColumn(ColumnDefinition.<ComponentValue, Double>createSimpleStateless(
                        "value", c -> c.getValue()))
                .withColumn(ColumnDefinition.<ComponentValue, Double>createSimpleStateless(
                        "weight", c -> c.getWeight()))
                .withColumn(ColumnDefinition.<ComponentValue, Double>createSimpleStateless(
                        "weighted value", c -> c.getWeightedValue()))
                .build();

        out.println(tableDef.apply(solution.getDetailedCost()));
    }

    private static void printPerRouteCosts(PrintWriter out,
            VehicleRoutingProblemSolution solution) {

        Builder<ComponentValue> builder = new TableFormatter.Builder<ComponentValue>()
                .withBorderFormatter(new BorderFormatter.Builder(
                        DefaultFormatters.ASCII_LINEDRAW).build())
                .withHeading("Route level costs (weighted)")
                .withShowAggregation(true)
                .withColumn(new ColumnDefinition.StatelessBuilder<ComponentValue, String>()
                        .withTitle("component id").withDataExtractor(c -> c.getKey())
                        .withAggregateRowConstant("Total").build());

        for (VehicleRoute r : solution.getRoutes()) {
            builder.withColumn(
                    new ColumnDefinition.StatefulBuilder<ComponentValue, Aggregator, Double>()
                    .withTitle("Route " + r.getId())
                    .withCellContentFormatter(CellContentFormatter.rightAlignedCell())
                    .withDataConverter(NumberDataConverter.defaultDoubleFormatter())
                    .withDataExtractor(new StatefulDataExtractor<>((cv, agg) -> {
                        Double val = ((RouteLevelComponentValue) cv)
                                .getRouteValue(r.getId()).orElse(null);
                        if (val != null) {
                            agg.sum += val;
                        }
                        return val;
                        }, Aggregator::new, (k, agg) -> agg.sum))
                    .build());
        }


        TableFormatter<ComponentValue> tableDef = builder.build();

        out.println(tableDef.apply(solution.getDetailedCost().stream()
                .filter(cv -> cv instanceof RouteLevelComponentValue)
                .collect(Collectors.toList())));

    }

    private static TableFormatter<Entry> createKeyValueTable(String heading) {
        TableFormatter<Entry> problemTableDef = new TableFormatter.Builder<Entry>()
                .withBorderFormatter(new BorderFormatter.Builder(
                        DefaultFormatters.ASCII_LINEDRAW).build())
                .withHeading(heading)
                .withColumn(ColumnDefinition.<Entry, String>createSimpleStateless("key",
                        en -> en.getKey()))
                .withColumn(ColumnDefinition.<Entry, String>createSimpleStateless("value",
                        en -> en.getValue()))
                .build();
        return problemTableDef;
    }

    private static Map<Class<? extends Job>, Long> getNuOfJobs(VehicleRoutingProblem problem) {
        return problem.getJobs().values().stream().map(j -> (Class<? extends Job>) j.getClass())
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
    }

    private static void printRouteDetails(PrintWriter out, VehicleRoutingProblem problem,
            VehicleRoutingProblemSolution solution) {
        printUnassignedJobs(out, solution);
        printRouteData(out, problem, solution);
    }

    private static void printUnassignedJobs(PrintWriter out,
            VehicleRoutingProblemSolution solution) {
        if (solution.getUnassignedJobs().isEmpty())
            return;
    }

    private static void printRouteData(PrintWriter out, VehicleRoutingProblem problem,
            VehicleRoutingProblemSolution solution) {
        Builder<RouteDeatailsRecord> builder = new TableFormatter.Builder<RouteDeatailsRecord>()
                .withBorderFormatter(
                        new BorderFormatter.Builder(DefaultFormatters.ASCII_LINEDRAW).build())
                .withHeading("Route details");

        new RouteDetailsConfig.Builder().withTimeDisplayMode(DisplayMode.BOTH)
        .withColumns(Column.values()).build().getColumns()
        .forEach(c -> builder.withColumn(c));

        TableFormatter<RouteDeatailsRecord> tableDef = builder.build();

        List<RouteDeatailsRecord> data = new ArrayList<>();
        for (VehicleRoute route : new ArrayList<>(solution.getRoutes())) {
            if (!data.isEmpty()) {
                data.add(null);
            }
            data.add(new RouteDeatailsRecord(route, route.getStart(), problem));

            for (TourActivity act : route.getActivities()) {
                data.add(new RouteDeatailsRecord(route, act, problem));
            }
            data.add(new RouteDeatailsRecord(route, route.getEnd(), problem));
        }

        out.println(tableDef.apply(data));
    }

    private static void printVehicleSummary(PrintWriter out, VehicleRoutingProblem problem,
            VehicleRoutingProblemSolution solution) {
        Builder<VehicleSummaryRecord> builder = new TableFormatter.Builder<VehicleSummaryRecord>()
                .withBorderFormatter(
                        new BorderFormatter.Builder(DefaultFormatters.ASCII_LINEDRAW).build())
                .withHeading("Vehicle summary");

        new VehicleSummaryConfig.Builder()
        .withColumns(VehicleSummaryConfig.Column.values())
        .build()
        .getColumns()
        .forEach(c -> builder.withColumn(c));

        TableFormatter<VehicleSummaryRecord> tableDef = builder.build();

        List<VehicleSummaryRecord> data = solution.getRoutes().stream()
                .map(r -> new VehicleSummaryRecord(r, problem))
                .collect(Collectors.toList());

        out.println(tableDef.apply(data));
    }

}

