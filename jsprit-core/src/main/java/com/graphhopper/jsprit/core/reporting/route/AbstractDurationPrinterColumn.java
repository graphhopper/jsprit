package com.graphhopper.jsprit.core.reporting.route;

import java.util.function.Consumer;

import com.graphhopper.jsprit.core.reporting.columndefinition.ColumnDefinition;
import com.graphhopper.jsprit.core.reporting.columndefinition.HumanReadableTimeFormatter;

/**
 * Abstract base class for duration columns.
 *
 * @author balage
 *
 * @param <T>
 *            Self reference.
 * @See {@linkplain AbstractTimePrinterColumn}
 */
public abstract class AbstractDurationPrinterColumn<T extends AbstractDurationPrinterColumn<T>>
extends AbstractTimePrinterColumn<T> {

    /**
     * Constructor to define a numeric format column.
     */
    public AbstractDurationPrinterColumn() {
        this(null);
    }

    /**
     * Constructor to define a numeric format column, with a post creation
     * decorator provided.
     */
    public AbstractDurationPrinterColumn(Consumer<ColumnDefinition.Builder> decorator) {
        super(decorator);
        withFormatter(new HumanReadableTimeFormatter());
    }

}
