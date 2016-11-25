package com.graphhopper.jsprit.core.reporting.route;

import java.util.function.Consumer;

import com.graphhopper.jsprit.core.reporting.DynamicTableDefinition.ColumnDefinition.Builder;

public abstract class AbstractDurationPrinterColumn<T extends AbstractDurationPrinterColumn<T>>
                extends AbstractTimePrinterColumn<T> {

    public AbstractDurationPrinterColumn() {
        this(null);
    }

    public AbstractDurationPrinterColumn(Consumer<Builder> decorator) {
        super(decorator);
        withFormatter(new HumanReadableDurationFormatter());
    }

}
