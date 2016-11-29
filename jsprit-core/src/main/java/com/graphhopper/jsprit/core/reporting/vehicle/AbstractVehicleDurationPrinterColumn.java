package com.graphhopper.jsprit.core.reporting.vehicle;

import java.util.function.Consumer;

import com.graphhopper.jsprit.core.reporting.AbstractPrinterColumn;
import com.graphhopper.jsprit.core.reporting.columndefinition.ColumnAlignment;
import com.graphhopper.jsprit.core.reporting.columndefinition.ColumnDefinition;
import com.graphhopper.jsprit.core.reporting.columndefinition.HumanReadableDurationFormatter;
import com.graphhopper.jsprit.core.reporting.columndefinition.HumanReadableTimeFormatter;
import com.graphhopper.jsprit.core.reporting.columndefinition.StringColumnType;
import com.graphhopper.jsprit.core.reporting.route.RoutePrinterContext;

/**
 * Abstract base class for time and (technically) duration columns.
 *
 * <p>
 * Each columns derived from this abstract base has two variants: a numerical
 * (an integer value) and a human readable. The numerical value displays the
 * integer value representing the time values internally. The human readable
 * value converts this value into a calendar (date and time) value.
 * </p>
 *
 * @author balage
 *
 * @param <T>
 *            Self reference.
 * @See {@linkplain HumanReadableTimeFormatter}
 */
public abstract class AbstractVehicleDurationPrinterColumn<T extends AbstractVehicleDurationPrinterColumn<T>>
extends AbstractPrinterColumn<VehicleSummaryContext, String, AbstractVehicleDurationPrinterColumn<T>> {

    public enum Mode {
        NUMERIC(""), HUMAN_READABLE(" (H)"), PERCENT_ROUTE(" (R%)"), PERCENT_SHIFT(" (S%)");

        private String postfix;

        private Mode(String postfix) {
            this.postfix = postfix;
        }

        public String getPostfix() {
            return postfix;
        }
    }

    // The time formatter to use (only used when humanReadable flag is true)
    private HumanReadableDurationFormatter formatter;
    // Whether to use human readable form
    private Mode mode = Mode.NUMERIC;
    // Percent decimals
    private int percentDecimals = 2;

    /**
     * Constructor to define a numeric format column.
     */
    public AbstractVehicleDurationPrinterColumn() {
        this(null);
    }

    /**
     * Constructor to define a numeric format column, with a post creation
     * decorator provided.
     */
    public AbstractVehicleDurationPrinterColumn(Consumer<ColumnDefinition.Builder> decorator) {
        super(decorator);
        formatter = new HumanReadableDurationFormatter();
        withDisplayMode(Mode.NUMERIC);
    }

    /**
     * @param formatter
     *            The formatter used for {@linkplain Mode#HUMAN_READABLE}
     *            format.
     *
     */
    @SuppressWarnings("unchecked")
    public T withFormatter(HumanReadableDurationFormatter formatter) {
        this.formatter = formatter;
        return (T) this;
    }

    @Override
    protected String getDefaultTitle() {
        return getDefaultTitleBase() + mode.postfix;
    }

    /**
     * @return The base of the default title. It will be extended by the
     *         mode-specific postfix.
     */
    protected abstract String getDefaultTitleBase();

    /**
     * @param mode
     *            The display mode.
     */
    @SuppressWarnings("unchecked")
    public T withDisplayMode(Mode mode) {
        this.mode = mode;
        return (T) this;
    }


    /**
     * @param digits
     *            Number of decimal digits when mode is
     *            {@linkplain Mode#PERCENT_SHIFT} or
     *            {@linkplain Mode#PERCENT_ROUTE}.
     * @throws IllegalArgumentException
     *             When the digits parameter is negative.
     */
    @SuppressWarnings("unchecked")
    public T withPercentDecimalDigits(int digits) {
        if (digits < 0) {
            throw new IllegalArgumentException("Decimal digit count should be non-negative.");
        }
        this.percentDecimals = digits;
        return (T) this;
    }


    /**
     * {@inheritDoc}
     *
     * <p>
     * The column builder returned will be a string column with the null value
     * represented by a hyphen ("-").
     * </p>
     *
     */
    @Override
    public ColumnDefinition.Builder getColumnBuilder() {
        ColumnDefinition.Builder builder = new ColumnDefinition.Builder(new StringColumnType("-"));
        if (mode != Mode.HUMAN_READABLE) {
            builder.withAlignment(ColumnAlignment.RIGHT);
        }
        return builder;
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * The implementation delegates the value extracting to the abstract method
     * {@linkplain #getValue(RoutePrinterContext)}.
     * <p>
     * <p>
     * If the value is null, returns null, otherwise it returns the string
     * representation of the numeric value or the human readable format based on
     * the humanReadable flag.
     * </p>
     *
     */
    @Override
    public String getData(VehicleSummaryContext context) {
        Long timeValue = getValue(context);
        if (timeValue == null) {
            return null;
        }
        switch (mode) {
        case NUMERIC:
            return "" + timeValue;
        case HUMAN_READABLE:
            return formatter.format(timeValue);
        case PERCENT_ROUTE:
            return formatPercent(timeValue, context.getRouteDuration() - context.getBreakDuration());
        case PERCENT_SHIFT:
            return formatPercent(timeValue, context.getShiftDuration() - context.getBreakDuration());
        default:
            throw new AssertionError("Can't get here.");
        }
    }

    private String formatPercent(Long timeValue, long total) {
        double pct = (100d * timeValue) / total;
        return String.format("%20." + percentDecimals + "f %%", pct).trim();
    }

    /**
     * Extracts the numerical value for this time or duration column.
     *
     * @param context
     *            The context.
     * @return The numerical value or null.
     */
    protected abstract Long getValue(VehicleSummaryContext context);

}
