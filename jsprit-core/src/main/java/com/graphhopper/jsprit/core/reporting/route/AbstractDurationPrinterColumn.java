package com.graphhopper.jsprit.core.reporting.route;

import java.util.function.Consumer;

import com.graphhopper.jsprit.core.reporting.columndefinition.ColumnDefinition;

public abstract class AbstractDurationPrinterColumn<T extends AbstractDurationPrinterColumn<T>>
extends AbstractTimePrinterColumn<T> {

    public AbstractDurationPrinterColumn() {
        this(null);
    }

    public AbstractDurationPrinterColumn(Consumer<ColumnDefinition.Builder> decorator) {
        super(decorator);
        withFormatter(new HumanReadableDurationFormatter());
    }

}
