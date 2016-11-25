package com.graphhopper.jsprit.core.reporting;

import java.util.function.Consumer;

import com.graphhopper.jsprit.core.reporting.DynamicTableDefinition.ColumnDefinition.Builder;

public abstract class AbstractPrinterColumn<C extends PrinterContext, T> {

    private Consumer<DynamicTableDefinition.ColumnDefinition.Builder> decorator;

    public AbstractPrinterColumn() {
        super();
    }

    public AbstractPrinterColumn(Consumer<Builder> decorator) {
        super();
        this.decorator = decorator;
    }

    public DynamicTableDefinition.ColumnDefinition getColumnDefinition() {
        Builder builder = getColumnBuilder();
        if (decorator != null) {
            decorator.accept(builder);
        }
        return builder.build();
    }

    protected abstract DynamicTableDefinition.ColumnDefinition.Builder getColumnBuilder();

    public abstract T getData(C context);

}
