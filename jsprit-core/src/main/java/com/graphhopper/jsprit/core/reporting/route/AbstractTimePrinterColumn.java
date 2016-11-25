package com.graphhopper.jsprit.core.reporting.route;

import java.util.function.Consumer;

import com.graphhopper.jsprit.core.reporting.AbstractPrinterColumn;
import com.graphhopper.jsprit.core.reporting.DynamicTableDefinition.ColumnDefinition;
import com.graphhopper.jsprit.core.reporting.DynamicTableDefinition.ColumnDefinition.Builder;
import com.graphhopper.jsprit.core.reporting.DynamicTableDefinition.StringColumnType;

public abstract class AbstractTimePrinterColumn<T extends AbstractTimePrinterColumn<T>>
extends AbstractPrinterColumn<RoutePrinterContext, String, AbstractTimePrinterColumn<T>>
implements HumanReadableEnabled<T> {

    private HumanReadableTimeFormatter formatter;
    private boolean humanReadable = false;

    public AbstractTimePrinterColumn() {
        this(null);
    }

    public AbstractTimePrinterColumn(Consumer<Builder> decorator) {
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
    public ColumnDefinition.Builder getColumnBuilder() {
        return new ColumnDefinition.Builder(new StringColumnType("-"));
    }

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

    protected abstract Long getValue(RoutePrinterContext context);

}
