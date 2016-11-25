package com.graphhopper.jsprit.core.reporting;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

import com.graphhopper.jsprit.core.reporting.route.ActivityCostPrinterColumn;
import com.graphhopper.jsprit.core.reporting.route.ActivityDurationPrinterColumn;
import com.graphhopper.jsprit.core.reporting.route.ActivityLoadChangePrinterColumn;
import com.graphhopper.jsprit.core.reporting.route.ActivityTypePrinterColumn;
import com.graphhopper.jsprit.core.reporting.route.ArrivalTimePrinterColumn;
import com.graphhopper.jsprit.core.reporting.route.EndTimePrinterColumn;
import com.graphhopper.jsprit.core.reporting.route.HumanReadableEnabled;
import com.graphhopper.jsprit.core.reporting.route.HumanReadableTimeFormatter;
import com.graphhopper.jsprit.core.reporting.route.JobNamePrinterColumn;
import com.graphhopper.jsprit.core.reporting.route.JobPriorityPrinterColumn;
import com.graphhopper.jsprit.core.reporting.route.JobTypePrinterColumn;
import com.graphhopper.jsprit.core.reporting.route.LoacationPrinterColumn;
import com.graphhopper.jsprit.core.reporting.route.OperationDurationPrinterColumn;
import com.graphhopper.jsprit.core.reporting.route.RouteCostPrinterColumn;
import com.graphhopper.jsprit.core.reporting.route.RouteLoadPrinterColumn;
import com.graphhopper.jsprit.core.reporting.route.RouteNumberPrinterColumn;
import com.graphhopper.jsprit.core.reporting.route.RoutePrinterContext;
import com.graphhopper.jsprit.core.reporting.route.SelectedTimeWindowPrinterColumn;
import com.graphhopper.jsprit.core.reporting.route.StartTimePrinterColumn;
import com.graphhopper.jsprit.core.reporting.route.TimeWindowsPrinterColumn;
import com.graphhopper.jsprit.core.reporting.route.TransportCostPrinterColumn;
import com.graphhopper.jsprit.core.reporting.route.TravelDurationPrinterColumn;
import com.graphhopper.jsprit.core.reporting.route.VehicleNamePrinterColumn;
import com.graphhopper.jsprit.core.reporting.route.WaitingDurationPrinterColumn;

public class SolutionPrintColumnLists {


    public enum PredefinedList {
        MINIMAL, DEFAULT, VERBOSE
    }

    // .addColumn(new RouteNumberPrinterColumn())
    // .addColumn(new VehicleNamePrinterColumn())
    // .addColumn(new ActivityTypePrinterColumn())
    // .addColumn(new JobNamePrinterColumn(b -> b.withMinWidth(10)))
    // .addColumn(new JobTypePrinterColumn())
    // .addColumn(new JobPriorityPrinterColumn())
    // .addColumn(new ActivityLoadChangePrinterColumn())
    // .addColumn(new RouteLoadPrinterColumn())
    // .addColumn(new LoacationPrinterColumn())
    // .addColumn(new OperationDurationPrinterColumn())
    // .addColumn(new OperationDurationPrinterColumn().asHumanReadable())
    // .addColumn(new TravelDurationPrinterColumn())
    // .addColumn(new TravelDurationPrinterColumn().asHumanReadable())
    // .addColumn(new ActivityDurationPrinterColumn())
    // .addColumn(new ActivityDurationPrinterColumn().asHumanReadable())
    // .addColumn(new WaitingDurationPrinterColumn())
    // .addColumn(new WaitingDurationPrinterColumn().asHumanReadable())
    // .addColumn(new ArrivalTimePrinterColumn())
    // .addColumn(new
    // ArrivalTimePrinterColumn().asHumanReadable().withFormatter(dateFormatter))
    // .addColumn(new StartTimePrinterColumn())
    // .addColumn(new
    // StartTimePrinterColumn().asHumanReadable().withFormatter(dateFormatter))
    // .addColumn(new EndTimePrinterColumn())
    // .addColumn(new
    // EndTimePrinterColumn().asHumanReadable().withFormatter(dateFormatter))
    // .addColumn(new TransportCostPrinterColumn())
    // .addColumn(new ActivityCostPrinterColumn())
    // .addColumn(new RouteCostPrinterColumn())
    // .addColumn(new SelectedTimeWindowPrinterColumn())
    // .addColumn(new
    // SelectedTimeWindowPrinterColumn().asHumanReadable().withFormatter(dateFormatter))
    // .addColumn(new TimeWindowsPrinterColumn())
    // .addColumn(new
    // TimeWindowsPrinterColumn().asHumanReadable().withFormatter(dateFormatter))

    private static final EnumMap<PredefinedList, List<Class<? extends AbstractPrinterColumn<RoutePrinterContext, ?, ?>>>> COLUMNS;

    static {
        COLUMNS = new EnumMap<>(PredefinedList.class);
        List<Class<? extends AbstractPrinterColumn<RoutePrinterContext, ?, ?>>> minimalSet = new ArrayList<>();
        minimalSet.add(RouteNumberPrinterColumn.class);
        minimalSet.add(VehicleNamePrinterColumn.class);
        minimalSet.add(ActivityTypePrinterColumn.class);
        minimalSet.add(JobNamePrinterColumn.class);
        minimalSet.add(ArrivalTimePrinterColumn.class);
        minimalSet.add(EndTimePrinterColumn.class);
        minimalSet.add(RouteCostPrinterColumn.class);
        COLUMNS.put(PredefinedList.MINIMAL, minimalSet);

        List<Class<? extends AbstractPrinterColumn<RoutePrinterContext, ?, ?>>> defaultSet = new ArrayList<>();
        defaultSet.add(RouteNumberPrinterColumn.class);
        defaultSet.add(VehicleNamePrinterColumn.class);
        defaultSet.add(ActivityTypePrinterColumn.class);
        defaultSet.add(JobNamePrinterColumn.class);
        defaultSet.add(LoacationPrinterColumn.class);
        defaultSet.add(ActivityLoadChangePrinterColumn.class);
        defaultSet.add(OperationDurationPrinterColumn.class);
        defaultSet.add(ArrivalTimePrinterColumn.class);
        defaultSet.add(StartTimePrinterColumn.class);
        defaultSet.add(EndTimePrinterColumn.class);
        defaultSet.add(ActivityCostPrinterColumn.class);
        defaultSet.add(RouteCostPrinterColumn.class);
        COLUMNS.put(PredefinedList.DEFAULT, defaultSet);

        List<Class<? extends AbstractPrinterColumn<RoutePrinterContext, ?, ?>>> verboseSet = new ArrayList<>();
        verboseSet.add(RouteNumberPrinterColumn.class);
        verboseSet.add(VehicleNamePrinterColumn.class);
        verboseSet.add(ActivityTypePrinterColumn.class);

        verboseSet.add(JobNamePrinterColumn.class);
        verboseSet.add(JobTypePrinterColumn.class);
        verboseSet.add(JobPriorityPrinterColumn.class);

        verboseSet.add(LoacationPrinterColumn.class);
        verboseSet.add(ActivityLoadChangePrinterColumn.class);
        verboseSet.add(RouteLoadPrinterColumn.class);
        verboseSet.add(TimeWindowsPrinterColumn.class);

        verboseSet.add(OperationDurationPrinterColumn.class);
        verboseSet.add(TravelDurationPrinterColumn.class);
        verboseSet.add(WaitingDurationPrinterColumn.class);
        verboseSet.add(ActivityDurationPrinterColumn.class);

        verboseSet.add(ArrivalTimePrinterColumn.class);
        verboseSet.add(StartTimePrinterColumn.class);
        verboseSet.add(EndTimePrinterColumn.class);
        verboseSet.add(SelectedTimeWindowPrinterColumn.class);

        verboseSet.add(TransportCostPrinterColumn.class);
        verboseSet.add(ActivityCostPrinterColumn.class);
        verboseSet.add(RouteCostPrinterColumn.class);

        COLUMNS.put(PredefinedList.VERBOSE, verboseSet);
    }

    public static PrinterColumnList<RoutePrinterContext> getNumeric(PredefinedList listType) {
        return getList(listType, false, null);
    }

    public static PrinterColumnList<RoutePrinterContext> getHumanReadable(PredefinedList listType) {
        return getList(listType, true, null);
    }

    public static PrinterColumnList<RoutePrinterContext> getHumanReadable(PredefinedList listType,
                    HumanReadableTimeFormatter timeFormatter) {
        return getList(listType, true, timeFormatter);
    }

    private static PrinterColumnList<RoutePrinterContext> getList(PredefinedList listType, boolean humanReadable,
                    HumanReadableTimeFormatter timeFormatter) {
        PrinterColumnList<RoutePrinterContext> res = new PrinterColumnList<>();

        for (Class<? extends AbstractPrinterColumn<RoutePrinterContext, ?, ?>> c : COLUMNS.get(listType)) {
            try {
                AbstractPrinterColumn<RoutePrinterContext, ?, ?> col = c.newInstance();
                if (humanReadable && col instanceof HumanReadableEnabled) {
                    HumanReadableEnabled<?> hrCol = (HumanReadableEnabled<?>) col;
                    hrCol.asHumanReadable();
                    if (timeFormatter != null) {
                        hrCol.withFormatter(timeFormatter);
                    }
                }
                res.addColumn(col);
            } catch (InstantiationException | IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
        }

        return res;
    }

}
