package com.graphhopper.jsprit.core.reporting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.graphhopper.jsprit.core.reporting.DynamicTableDefinition.Builder;

public class PrinterColumnList<C extends PrinterContext> {

    private String heading = null;

    private List<AbstractPrinterColumn<C, ?, ?>> columns = new ArrayList<>();

    public PrinterColumnList() {
        super();
    }

    public PrinterColumnList(String heading) {
        super();
        this.heading = heading;
    }

    public PrinterColumnList<C> addColumn(AbstractPrinterColumn<C, ?,?> column) {
        if (findByTitle(column.getTitle()).isPresent()) {
            throw new IllegalArgumentException("Name is duplicated: " + column.getTitle());
        } else {
            columns.add(column);
        }
        return this;
    }

    public boolean removeColumn(AbstractPrinterColumn<C, ?, ?> column) {
        boolean res = columns.contains(column);
        if (res) {
            columns.remove(column);
        }
        return res;
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

    public List<AbstractPrinterColumn<C, ?,?>> getColumns() {
        return Collections.unmodifiableList(columns);
    }

    public String getHeading() {
        return heading;
    }

    public PrinterColumnList<C> withHeading(String heading) {
        this.heading = heading;
        return this;
    }

    public List<AbstractPrinterColumn<C, ?, ?>> findByClass(Class<? extends AbstractPrinterColumn<C, ?,?>> clazz) {
        return columns.stream().filter(c -> c.getClass().equals(clazz)).collect(Collectors.toList());
    }

    public Optional<AbstractPrinterColumn<C, ?, ?>> findByTitle(String title) {
        return columns.stream().filter(c -> c.getTitle().equals(title)).findAny();
    }

}
