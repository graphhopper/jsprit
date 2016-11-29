package com.graphhopper.jsprit.core.reporting.vehicle;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow;
import com.graphhopper.jsprit.core.reporting.AbstractPrinterColumn;
import com.graphhopper.jsprit.core.reporting.columndefinition.ColumnDefinition;
import com.graphhopper.jsprit.core.reporting.columndefinition.HumanReadableEnabled;
import com.graphhopper.jsprit.core.reporting.columndefinition.HumanReadableTimeFormatter;
import com.graphhopper.jsprit.core.reporting.columndefinition.StringColumnType;
import com.graphhopper.jsprit.core.reporting.route.RoutePrinterContext;

/**
 * Abstract base class for time window columns.
 *
 * <p>
 * Each columns derived from this abstract base has two variants: a numerical
 * (an integer value) and a human readable. The numerical value displays the
 * integer value pair representing the time windows, the same the algorithm used
 * internally. The human readable value converts this value into a calendar
 * (date and time) value pair.
 * </p>
 *
 * @author balage
 *
 * @param <T>
 *            Self reference.
 * @See {@linkplain HumanReadableTimeFormatter}
 */
public abstract class AbstractVehicleTimeWindowPrinterColumn<T extends AbstractVehicleTimeWindowPrinterColumn<T>>
extends AbstractPrinterColumn<VehicleStatisticsContext, String, AbstractVehicleTimeWindowPrinterColumn<T>>
implements HumanReadableEnabled<T> {

    // The time formatter to use (only used when humanReadable flag is true)
    private HumanReadableTimeFormatter formatter;
    // Whether to use human readable form
    private boolean humanReadable = false;

    /**
     * Constructor to define a numeric format column.
     */
    public AbstractVehicleTimeWindowPrinterColumn() {
        this(null);
    }

    /**
     * Constructor to define a numeric format column, with a post creation
     * decorator provided.
     */
    public AbstractVehicleTimeWindowPrinterColumn(Consumer<ColumnDefinition.Builder> decorator) {
        super(decorator);
        formatter = new HumanReadableTimeFormatter();
    }

    @Override
    @SuppressWarnings("unchecked")
    public T withFormatter(HumanReadableTimeFormatter formatter) {
        this.formatter = formatter;
        return (T) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T asHumanReadable() {
        this.humanReadable = true;
        return (T) this;
    }

    @Override
    protected String getDefaultTitle() {
        return getDefaultTitleBase() + (humanReadable ? " (H)" : "");
    }

    protected abstract String getDefaultTitleBase();


    @Override
    public ColumnDefinition.Builder getColumnBuilder() {
        return new ColumnDefinition.Builder(new StringColumnType("-"));
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * The implementation delegates the value extracting to the abstract method
     * {@linkplain #getValue(RoutePrinterContext)}.
     * <p>
     * <p>
     * If the value is null or empty, returns null, otherwise it returns the
     * string representation of the numeric value or the human readable format
     * based on the humanReadable flag.
     * </p>
     *
     */
    @Override
    public String getData(VehicleStatisticsContext context) {
        Collection<TimeWindow> timeWindows = getValue(context);
        if (timeWindows == null || timeWindows.isEmpty()) {
            return null;
        }
        return timeWindows.stream().map(tw -> formatTimeWindow(tw)).collect(Collectors.joining());
    }

    /**
     * Formats the time window.
     *
     * <p>
     * The implementation returns the two (start, end) values sepratated by
     * hyphen (-) and wrapped within brackets. When the end value is
     * {@linkplain Double#MAX_VALUE} it omits the value indicating open
     * interval.
     * </p>
     *
     * @param tw
     *            The time window to format.
     * @return The string representation of the time window.
     */
    protected String formatTimeWindow(TimeWindow tw) {
        String res = "";
        if (humanReadable) {
            res = "[" + formatter.format((long) tw.getStart()) + "-";
            if (tw.getEnd() == Double.MAX_VALUE) {
                res += "";
            } else {
                res += formatter.format((long) tw.getEnd());
            }
            res += "]";

        } else {
            res = "[" + (long) tw.getStart() + "-";
            if (tw.getEnd() == Double.MAX_VALUE) {
                res += "";
            } else {
                res += (long) tw.getEnd();
            }
            res += "]";
        }
        return res;
    }

    /**
     * Extracts the collection of time windows from the context.
     *
     * @param context
     *            The context.
     * @return The collection of time windows.
     */
    protected abstract Collection<TimeWindow> getValue(VehicleStatisticsContext context);

}
