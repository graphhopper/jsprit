package com.graphhopper.jsprit.core.reporting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.graphhopper.jsprit.core.reporting.ConfigurableTablePrinter.TableRow;
import com.graphhopper.jsprit.core.reporting.DynamicTableDefinition.Builder;

/**
 * The list of the printer columns. This helps the user to construct, manage and
 * alter the column definitions. Also this function populates the
 * {@linkplain TableRow}.
 *
 * @author balage
 *
 * @param <C>
 *            The context the colums
 */
public class PrinterColumnList<C extends PrinterContext> {

    // The heading line
    private String heading = null;

    // The list of the columns
    private List<AbstractPrinterColumn<C, ?, ?>> columns = new ArrayList<>();

    /**
     * The constructor to create a table without heading.
     */
    public PrinterColumnList() {
        super();
    }

    /**
     * Constructor to create with heading text.
     *
     * @param heading
     *            The heading text.
     */
    public PrinterColumnList(String heading) {
        super();
        this.heading = heading;
    }

    /**
     * Adds a column to the column list.
     *
     * @param column
     *            The column to add.
     * @return The object itself (fluent api)
     */
    public PrinterColumnList<C> addColumn(AbstractPrinterColumn<C, ?,?> column) {
        if (findByTitle(column.getTitle()).isPresent()) {
            throw new IllegalArgumentException("Name is duplicated: " + column.getTitle());
        } else {
            columns.add(column);
        }
        return this;
    }

    /**
     * Removes a column.
     * <p>
     * Requires the exact column instance that was added- Use the
     * {@linkplain #findByClass(Class)} or {@linkplain #findByTitle(String)}
     * functions to get the instance.
     * </p>
     *
     * @param column
     *            the column to remove.
     * @return true if the column was found and removed
     */
    public boolean removeColumn(AbstractPrinterColumn<C, ?, ?> column) {
        boolean res = columns.contains(column);
        if (res) {
            columns.remove(column);
        }
        return res;
    }

    /**
     * Builds the table definition from the column list and other parameters.
     *
     * @return the table definition
     */
    public DynamicTableDefinition getTableDefinition() {
        Builder defBuilder = new DynamicTableDefinition.Builder();
        columns.forEach(c -> defBuilder.addColumn(c.getColumnDefinition()));
        defBuilder.withHeading(heading);
        return defBuilder.build();
    }

    /**
     * Populates a table row with the data extracted from the context and
     * formatted by the column definition.
     *
     * @param row
     *            The row to populate. The row must match the column definition.
     * @param context
     *            The context to work on
     */
    void populateRow(ConfigurableTablePrinter<C>.TableRow row, C context) {
        columns.forEach(c -> row.add(c.getData(context)));
    }

    /**
     * @return unmodifiable list of columns
     */
    public List<AbstractPrinterColumn<C, ?,?>> getColumns() {
        return Collections.unmodifiableList(columns);
    }

    /**
     * @return the heading text. Null means there will be no heading.
     */
    public String getHeading() {
        return heading;
    }

    /**
     * @param heading
     *            The new heading text or null to remove heading.
     * @return The object itself (fluent api)
     */
    public PrinterColumnList<C> withHeading(String heading) {
        this.heading = heading;
        return this;
    }

    /**
     * Finds the columns with the type given.
     * <p>
     * A table could contain more columns of the same type, so this function
     * returns all matching columns.
     * </p>
     * <p>
     * Note that this function intentially uses
     * <code>getClass().equals(clazz)</code> instead of <code>instanceof</code>,
     * so only the exact matches are returned. Columns of inherited classes are
     * not returned.
     *
     * @param clazz
     *            The class to look for
     * @return The list of all the columns with the type
     */
    public List<AbstractPrinterColumn<C, ?, ?>> findByClass(Class<? extends AbstractPrinterColumn<C, ?,?>> clazz) {
        return columns.stream().filter(c -> c.getClass().equals(clazz)).collect(Collectors.toList());
    }

    /**
     * Returns the column with the title.
     * 
     * @param title
     *            The title to look for
     * @return The column definition if there is any match
     */
    public Optional<AbstractPrinterColumn<C, ?, ?>> findByTitle(String title) {
        return columns.stream().filter(c -> c.getTitle().equals(title)).findAny();
    }

}
