package com.graphhopper.jsprit.core.reporting;

import java.util.ArrayList;
import java.util.List;

import com.graphhopper.jsprit.core.reporting.DynamicTableDefinition.Builder;

public class PrinterColumnList<C extends PrinterContext> {

    private String heading = null;

    private List<AbstractPrinterColumn<C, ?>> columns = new ArrayList<>();

    public PrinterColumnList() {
        super();
    }

    public PrinterColumnList(String heading) {
        super();
        this.heading = heading;
    }

    public PrinterColumnList<C> addColumn(AbstractPrinterColumn<C, ?> column) {
        columns.add(column);
        return this;
    }

    public DynamicTableDefinition getTableDefinition() {
        Builder defBuilder = new DynamicTableDefinition.Builder();
        columns.forEach(c -> defBuilder.addColumn(c.getColumnDefinition()));
        defBuilder.withHeading(heading);
        return defBuilder.build();
    }

    public void populateRow(ConfigurableTablePrinter<C>.TableRow row, C context) {
        columns.forEach(c -> row.add(c.getData(context)));
    }


}
