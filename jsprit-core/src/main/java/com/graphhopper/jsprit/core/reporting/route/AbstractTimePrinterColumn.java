package com.graphhopper.jsprit.core.reporting.route;

import java.util.function.Consumer;

import com.graphhopper.jsprit.core.reporting.AbstractPrinterColumn;
import com.graphhopper.jsprit.core.reporting.columndefinition.ColumnDefinition;
import com.graphhopper.jsprit.core.reporting.columndefinition.HumanReadableEnabled;
import com.graphhopper.jsprit.core.reporting.columndefinition.HumanReadableTimeFormatter;
import com.graphhopper.jsprit.core.reporting.columndefinition.StringColumnType;

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
public abstract class AbstractTimePrinterColumn<T extends AbstractTimePrinterColumn<T>>
extends AbstractPrinterColumn<RoutePrinterContext, String, AbstractTimePrinterColumn<T>>
implements HumanReadableEnabled<T> {

    // The time formatter to use (only used when humanReadable flag is true)
    private HumanReadableTimeFormatter formatter;
    // Whether to use human readable form
    private boolean humanReadable = false;

    /**
     * Constructor to define a numeric format column.
     */
    public AbstractTimePrinterColumn() {
        this(null);
    }

    /**
     * Constructor to define a numeric format column, with a post creation
     * decorator provided.
     */
    public AbstractTimePrinterColumn(Consumer<ColumnDefinition.Builder> decorator) {
        super(decorator);
        formatter = new HumanReadableTimeFormatter();
    }

    /**
     * {@inheritDoc}
     *
     */
    @Override
    @SuppressWarnings("unchecked")
    public T withFormatter(HumanReadableTimeFormatter formatter) {
        this.formatter = formatter;
        return (T) this;
    }

    /**
     * {@inheritDoc}
     *
     */
    @Override
    @SuppressWarnings("unchecked")
    public T asHumanReadable() {
        this.humanReadable = true;
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
     * If the value is null, returns null, otherwise it returns the string
     * representation of the numeric value or the human readable format based on
     * the humanReadable flag.
     * </p>
     *
     */
    @Override
    public String getData(RoutePrinterContext context) {
        Long timeValue = getValue(context);
        if (timeValue == null) {
            return null;
        }
        if (humanReadable) {
            return formatter.format(timeValue);
        } else {
            return ""+timeValue;
        }
    }

    /**
     * Extracts the numerical value for this time or duration column.
     *
     * @param context
     *            The context.
     * @return The numerical value or null.
     */
    protected abstract Long getValue(RoutePrinterContext context);

}
