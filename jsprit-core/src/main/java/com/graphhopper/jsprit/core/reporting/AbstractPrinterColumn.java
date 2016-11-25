package com.graphhopper.jsprit.core.reporting;

import java.util.function.Consumer;

import com.graphhopper.jsprit.core.reporting.DynamicTableDefinition.ColumnDefinition.Builder;

public abstract class AbstractPrinterColumn<C extends PrinterContext, T, A extends AbstractPrinterColumn<C, T, A>> {

    private Consumer<DynamicTableDefinition.ColumnDefinition.Builder> decorator;

    private String title;

    public AbstractPrinterColumn() {
        this(null);
    }

    public AbstractPrinterColumn(Consumer<Builder> decorator) {
        super();
        this.decorator = decorator;
        this.title = getDefaultTitle();
    }

    public DynamicTableDefinition.ColumnDefinition getColumnDefinition() {
        Builder builder = getColumnBuilder().withTitle(getTitle());
        if (decorator != null) {
            decorator.accept(builder);
        }
        return builder.build();
    }

    public String getTitle() {
        return title;
    }

    @SuppressWarnings("unchecked")
    public A withTitle(String title) {
        this.title = title;
        return (A) this;
    }

    @SuppressWarnings("unchecked")
    public A withDecorator(Consumer<DynamicTableDefinition.ColumnDefinition.Builder> decorator) {
        this.decorator = decorator;
        return (A) this;
    }

    protected abstract DynamicTableDefinition.ColumnDefinition.Builder getColumnBuilder();

    public abstract T getData(C context);

    protected abstract String getDefaultTitle();

}
