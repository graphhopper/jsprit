package com.graphhopper.jsprit.core.reporting.vehicle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;

import com.graphhopper.jsprit.core.reporting.AbstractPrinterColumn;
import com.graphhopper.jsprit.core.reporting.PrinterColumnList;
import com.graphhopper.jsprit.core.reporting.columndefinition.HumanReadableDurationFormatter;
import com.graphhopper.jsprit.core.reporting.columndefinition.HumanReadableEnabled;
import com.graphhopper.jsprit.core.reporting.columndefinition.HumanReadableTimeFormatter;
import com.graphhopper.jsprit.core.reporting.vehicle.AbstractVehicleDurationPrinterColumn.Mode;

/**
 * Utility class to provide predefined column lists for Solution printing.
 *
 * @author balage
 *
 */
public class VehicleSummaryColumnLists {

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

    private static final EnumMap<PredefinedList, List<Class<? extends AbstractPrinterColumn<VehicleSummaryContext, ?, ?>>>> COLUMNS;

    static {
        COLUMNS = new EnumMap<>(PredefinedList.class);
        List<Class<? extends AbstractPrinterColumn<VehicleSummaryContext, ?, ?>>> minimalSet = new ArrayList<>();
        minimalSet.add(VehicleRouteNumberPrinterColumn.class);
        minimalSet.add(VehicleTruckNamePrinterColumn.class);
        minimalSet.add(VehicleAllActivityTypeCountPrinterColumn.class);
        minimalSet.add(VehicleRouteDurationPrinterColumn.class);
        minimalSet.add(VehicleActiveDurationPrinterColumn.class);
        COLUMNS.put(PredefinedList.MINIMAL, minimalSet);

        List<Class<? extends AbstractPrinterColumn<VehicleSummaryContext, ?, ?>>> defaultSet = new ArrayList<>();
        defaultSet.add(VehicleRouteNumberPrinterColumn.class);
        defaultSet.add(VehicleTruckNamePrinterColumn.class);
        defaultSet.add(VehicleAllActivityTypeCountPrinterColumn.class);
        defaultSet.add(VehicleRouteTimeWindowPrinterColumn.class);
        defaultSet.add(VehicleRouteDurationPrinterColumn.class);
        defaultSet.add(VehicleTravelDurationPrinterColumn.class);
        defaultSet.add(VehicleOperationDurationPrinterColumn.class);
        defaultSet.add(VehicleActiveDurationPrinterColumn.class);
        defaultSet.add(VehicleIdleDurationPrinterColumn.class);
        COLUMNS.put(PredefinedList.DEFAULT, defaultSet);

        List<Class<? extends AbstractPrinterColumn<VehicleSummaryContext, ?, ?>>> verboseSet = new ArrayList<>();
        verboseSet.add(VehicleRouteNumberPrinterColumn.class);
        verboseSet.add(VehicleTruckNamePrinterColumn.class);
        verboseSet.add(VehicleTypePrinterColumn.class);
        verboseSet.add(VehicleDriverNamePrinterColumn.class);
        verboseSet.add(VehicleActivityCountPrinterColumn.class);
        verboseSet.add(VehicleAllActivityTypeCountPrinterColumn.class);
        verboseSet.add(VehicleShiftTimeWindowPrinterColumn.class);
        verboseSet.add(VehicleShiftDurationPrinterColumn.class);
        verboseSet.add(VehicleRouteTimeWindowPrinterColumn.class);
        verboseSet.add(VehicleRouteDurationPrinterColumn.class);
        verboseSet.add(VehicleTravelDurationPrinterColumn.class);
        verboseSet.add(VehicleOperationDurationPrinterColumn.class);
        verboseSet.add(VehicleActiveDurationPrinterColumn.class);
        verboseSet.add(VehicleIdleDurationPrinterColumn.class);
        COLUMNS.put(PredefinedList.VERBOSE, verboseSet);
    }

    public static PrinterColumnList<VehicleSummaryContext> getNumeric(PredefinedList listType) {
        return getList(listType, Collections.singletonList(Mode.NUMERIC), false, null, null);
    }

    public static PrinterColumnList<VehicleSummaryContext> getHumanReadable(PredefinedList listType) {
        return getList(listType, Collections.singletonList(Mode.HUMAN_READABLE), true, null, null);
    }

    public static PrinterColumnList<VehicleSummaryContext> getMultiple(PredefinedList listType, Mode... durationModes) {
        List<Mode> modes = Arrays.asList(durationModes);
        return getMultiple(listType, modes);
    }

    public static PrinterColumnList<VehicleSummaryContext> getMultiple(PredefinedList listType, List<Mode> durationModes) {
        return getList(listType, durationModes, durationModes.contains(Mode.HUMAN_READABLE), null, null);
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
    public static PrinterColumnList<VehicleSummaryContext> getList(PredefinedList listType, List<Mode> durationModes,
                    boolean humanReadableTimeWindows, HumanReadableTimeFormatter timeWindowTimeFormatter,
                    HumanReadableDurationFormatter durationFormatter) {
        PrinterColumnList<VehicleSummaryContext> res = new PrinterColumnList<>();

        for (Class<? extends AbstractPrinterColumn<VehicleSummaryContext, ?, ?>> c : COLUMNS.get(listType)) {
            try {
                if (AbstractVehicleDurationPrinterColumn.class.isAssignableFrom(c)) {
                    for(Mode mode : durationModes) {
                        AbstractVehicleDurationPrinterColumn<?> col = (AbstractVehicleDurationPrinterColumn<?>) c.newInstance();
                        if (durationFormatter != null) {
                            col.withFormatter(durationFormatter);
                        }
                        col.withDisplayMode(mode);
                        res.addColumn(col);
                    }
                } else {
                    AbstractPrinterColumn<VehicleSummaryContext, ?, ?> col = c.newInstance();
                    if (humanReadableTimeWindows && col instanceof HumanReadableEnabled) {
                        HumanReadableEnabled<?> hrCol = (HumanReadableEnabled<?>) col;
                        hrCol.asHumanReadable();
                        if (durationFormatter != null) {
                            hrCol.withFormatter(durationFormatter);
                        }
                    }
                    res.addColumn(col);
                }
            } catch (InstantiationException | IllegalAccessException e) {
                // Technically you can't get here as long as all column
                // implementation has default constructor
                throw new IllegalStateException(e);
            }
        }

        return res;
    }

}
