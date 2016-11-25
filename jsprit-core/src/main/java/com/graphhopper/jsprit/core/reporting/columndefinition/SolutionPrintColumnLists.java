package com.graphhopper.jsprit.core.reporting.columndefinition;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

import com.graphhopper.jsprit.core.reporting.AbstractPrinterColumn;
import com.graphhopper.jsprit.core.reporting.PrinterColumnList;
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

/**
 * Utility class to provide predefined column lists for Solution printing.
 *
 * @author balage
 *
 */
public class SolutionPrintColumnLists {

    /**
     * The predefined column sets.
     *
     * @author balage
     *
     */
    public enum PredefinedList {
        /**
         * A minimal column set.
         */
        MINIMAL,
        /**
         * A general, most often used column set.
         */
        DEFAULT,
        /**
         * A verbose column set containing all columns.
         */
        VERBOSE
    }

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

    /**
     * Returns the predefined column set with all time, time window and duration
     * columns printed as numbers.
     *
     * @param listType
     *            The predefined list id.
     * @return The column list containing the predefined columns.
     */
    public static PrinterColumnList<RoutePrinterContext> getNumeric(PredefinedList listType) {
        return getList(listType, false, null);
    }

    /**
     * Returns the predefined column set with all time, time window and duration
     * columns printed with human readable format, using default formatting.
     *
     * @param listType
     *            The predefined list id.
     * @return The column list containing the predefined columns.
     */
    public static PrinterColumnList<RoutePrinterContext> getHumanReadable(PredefinedList listType) {
        return getList(listType, true, null);
    }

    /**
     * Returns the predefined column set with all time, time window and duration
     * columns printed with human readable format, using the provided formatter.
     *
     * @param listType
     *            The predefined list id.
     * @param timeFormatter
     *            the time formatter to use
     * @return The column list containing the predefined columns.
     */
    public static PrinterColumnList<RoutePrinterContext> getHumanReadable(PredefinedList listType,
                    HumanReadableTimeFormatter timeFormatter) {
        return getList(listType, true, timeFormatter);
    }

    /**
     * Generates the list.
     *
     * @param listType
     *            The id of the list.
     * @param humanReadable
     *            Whether human readable format should be used
     * @param timeFormatter
     *            The formatter to use (if null, the default will be used)
     * @return The generated column list.
     */
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
                // Technically you can't get here as long as all column
                // implementation has default constructor
                throw new IllegalStateException(e);
            }
        }

        return res;
    }

}
