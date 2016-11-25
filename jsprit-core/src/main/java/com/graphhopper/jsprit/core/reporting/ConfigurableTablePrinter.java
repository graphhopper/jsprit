package com.graphhopper.jsprit.core.reporting;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.QuoteMode;

import com.graphhopper.jsprit.core.reporting.columndefinition.ColumnAlignment;
import com.graphhopper.jsprit.core.reporting.columndefinition.ColumnDefinition;


/**
 * A text-base table formatter with extendible and configurable column set.
 *
 * @author balage
 * @param <C>
 *            The context the table formatter operates. When a new row of data
 *            is being added, this context is passed to all column definitions
 *            to create cell information.
 */
public class ConfigurableTablePrinter<C extends PrinterContext> {

    /**
     * A row of the table.
     *
     * @author balage
     *
     */
    public class TableRow {
        private String row[] = new String[tableDef.size()];

        // Used by add() function to determine the next column index.
        private int lastIndex = 0;

        /**
         * Constructor.
         */
        public TableRow() {
            super();
            Arrays.fill(row, "");
        }

        /**
         * Sets the value of a cell in the row.
         *
         * @param index
         *            The index of the cell.
         * @param data
         *            The data to be formatted.
         * @return The table row itself.
         * @throws IndexOutOfBoundsException
         *             When the index is not valid.
         * @throws ClassCastException
         *             When the column doesn't accept the data provided.
         */
        public TableRow set(int index, Object data) {
            if (index < 0 || index >= row.length) {
                throw new IndexOutOfBoundsException("Invalid index: " + index);
            }
            if (data != null) {
                if (!tableDef.getColumns().get(index).getType().accepts(data)) {
                    throw new ClassCastException("Cannot assign " + data.getClass().getSimpleName()
                                    + " to " + tableDef.getColumns().get(index).getType().getClass()
                                    .getSimpleName()
                                    + "( index: " + index + ")");
                }
            }
            String val = tableDef.getColumns().get(index).getType().convert(data);

            row[index] = val;
            return this;
        }

        /**
         * Adds data for the next cell.
         * <p>
         * Note that calling the {@linkplain #set(int, Object)} doesn't alter
         * the insertation point for this function.
         * <p>
         *
         * @param data
         *            The data to add.
         * @return The table row itself (fluent api).
         * @throws IndexOutOfBoundsException
         *             When the index is not valid.
         * @throws ClassCastException
         *             When the column doesn't accept the data provided.
         */
        public TableRow add(Object data) {
            return set(lastIndex++, data);
        }

        /**
         * Adds data for the next cell.
         * <p>
         * Note that calling the {@linkplain #set(int, Object)} doesn't alter
         * the insertation point for this function.
         * <p>
         *
         * @param data
         *            The data to add.
         * @return The table row itself (fluent api).
         * @throws IndexOutOfBoundsException
         *             When the index is not valid.
         * @throws ClassCastException
         *             When the column doesn't accept the data provided.
         */
        public TableRow add(int data) {
            return add(new Integer(data));
        }

        /**
         * Adds data for the next cell.
         * <p>
         * Note that calling the {@linkplain #set(int, Object)} doesn't alter
         * the insertation point for this function.
         * <p>
         *
         * @param data
         *            The data to add.
         * @return The table row itself (fluent api).
         * @throws IndexOutOfBoundsException
         *             When the index is not valid.
         * @throws ClassCastException
         *             When the column doesn't accept the data provided.
         */
        public TableRow add(long data) {
            return add(new Long(data));
        }

        /**
         * Adds data for the next cell.
         * <p>
         * Note that calling the {@linkplain #set(int, Object)} doesn't alter
         * the insertation point for this function.
         * <p>
         *
         * @param data
         *            The data to add.
         * @return The table row itself (fluent api).
         * @throws IndexOutOfBoundsException
         *             When the index is not valid.
         * @throws ClassCastException
         *             When the column doesn't accept the data provided.
         */
        public TableRow add(double data) {
            return add(new Double(data));
        }

        /**
         * Adds data for the next cell.
         * <p>
         * Note that calling the {@linkplain #set(int, Object)} doesn't alter
         * the insertation point for this function.
         * <p>
         *
         * @param data
         *            The data to add.
         * @return The table row itself (fluent api).
         * @throws IndexOutOfBoundsException
         *             When the index is not valid.
         * @throws ClassCastException
         *             When the column doesn't accept the data provided.
         */
        public TableRow add(boolean data) {
            return add(Boolean.valueOf(data));
        }

        /**
         * Returns the value of a cell.
         *
         * @param index
         *            The index of the cell.
         * @return The string representation of the cell.
         * @throws IndexOutOfBoundsException
         *             When the index is not valid.
         */
        public String get(int index) {
            if (index < 0 || index >= row.length) {
                throw new IndexOutOfBoundsException("Invalid index: " + index);
            }
            return row[index];
        }

        /**
         * @return Returns the unmodifiable data of the complete row.
         */
        public List<String> getAll() {
            return Collections.unmodifiableList(Arrays.asList(row));
        }
    }

    /**
     * Marker row for in-table separator line.
     *
     * @author balage
     */
    private class Separator extends TableRow {
    }

    // The column list
    private PrinterColumnList<C> columnList;
    // The table definition
    private DynamicTableDefinition tableDef;

    // The rows of the table
    List<TableRow> rows = new ArrayList<>();

    /**
     * Constructor.
     *
     * @param columnList
     *            The list of the columns in the table.
     */
    public ConfigurableTablePrinter(PrinterColumnList<C> columnList) {
        super();
        this.columnList = columnList;
        tableDef = columnList.getTableDefinition();
    }

    /**
     * Adds and populates a row.
     *
     * @param context
     *            The context to use for row cell population.
     */
    public void addRow(C context) {
        TableRow row = new TableRow();
        columnList.populateRow(row, context);
        rows.add(row);
    }

    /**
     * Adds an in-table separator line.
     */
    public void addSeparator() {
        rows.add(new Separator());
    }


    /**
     * Repeats <code>c</code> <code>w</code> times.
     *
     * @param c
     *            The character to repeat.
     * @param w
     *            The number of occurencies to repeat.
     * @return A <code>w</code> long string containing <code>c</code>
     *         characters.
     */
    private String repeat(char c, int w) {
        return CharBuffer.allocate(w).toString().replace('\0', c);
    }

    /**
     * Prints the table into a string.
     *
     * @return The string representation of the table.
     */
    public String print() {
        StringBuilder sb = new StringBuilder();
        // Calculating width of each column
        int[] colWidth = calculateWidthInfo();

        // The total width of the table: the sum of column width, plus the
        // padding two times for each column, plus the vertical lines (column
        // count plus one times)
        int totalWidth = colWidth.length * (tableDef.getPadding() * 2 + 1) + 1;
        for (int w : colWidth) {
            totalWidth += w;
        }

        // Caching draw characters and padding size (for cleaner code)
        char corner = tableDef.getCorner();
        char horizontal = tableDef.getHorizontal();
        char vertical = tableDef.getVertical();
        int padding = tableDef.getPadding();

        // Padding string
        String paddingChars = repeat(' ', padding);

        // Build the line for the separator rows
        StringBuilder sbSep = new StringBuilder();
        sbSep.append(corner);
        for (int w : colWidth) {
            sbSep.append(repeat(horizontal, w + 2 * padding)).append(corner);
        }
        sbSep.append("\n");
        String separatorLine = sbSep.toString();

        // Printing heading if defined
        if (tableDef.getHeading() != null) {
            sb.append(corner).append(repeat(horizontal, totalWidth - 2)).append(corner)
            .append("\n");
            sb.append(vertical).append(paddingChars)
            .append(ColumnAlignment.LEFT.align(tableDef.getHeading(),
                            totalWidth - 2 * padding - 2))
            .append(paddingChars)
            .append(vertical)
            .append("\n");
        }

        // Adding a separator line (either as the top line of the table or to
        // separate heading)
        sb.append(separatorLine);

        // Printing header line
        sb.append(vertical);
        for (int i = 0; i < tableDef.size(); i++) {
            ColumnDefinition cd = tableDef.getColumns().get(i);
            sb.append(paddingChars).append(ColumnAlignment.LEFT.align(cd.getTitle(), colWidth[i]))
            .append(paddingChars).append(vertical);
        }
        sb.append("\n");
        sb.append(separatorLine);

        for(TableRow row : rows) {
            if (row instanceof ConfigurableTablePrinter.Separator) {
                // Adding separator line
                sb.append(separatorLine);
            } else {
                // Printing a line
                sb.append(vertical);
                for (int i = 0; i < tableDef.size(); i++) {
                    ColumnDefinition cd = tableDef.getColumns().get(i);
                    sb.append(paddingChars).append(cd.getAlignment().align(row.get(i), colWidth[i]))
                    .append(paddingChars).append(vertical);
                }
                sb.append("\n");
            }
        }
        // Closing the table
        sb.append(separatorLine);

        return sb.toString();
    }

    /**
     * Calculates width of each column.
     *
     * @return The width info for the table.
     */
    private int[] calculateWidthInfo() {
        int colWidth[] = new int[tableDef.size()];
        // For each column
        IntStream.range(0, tableDef.size()).forEach(i -> {
            // Calculate maximum data width
            int max = rows.stream()
                            .filter(r -> r instanceof ConfigurableTablePrinter.TableRow)
                            .map(r -> r.get(i))
                            .filter(d -> d != null)
                            .mapToInt(d -> d.length())
                            .max().orElse(0);
            ColumnDefinition colDef = tableDef.getColumns().get(i);
            // The width will be the max data or title with, bounded by the min
            // and/or max column width constraints.
            colWidth[i] = Math.max(colDef.getTitle().length(),
                            Math.max(colDef.getMinWidth(), Math.min(colDef.getMaxWidth(), max)));
        });
        return colWidth;
    }

    /**
     * CSV export configuration.
     *
     * @author balage
     *
     */
    public static class CsvConfig {
        private char delimiter = ';';
        private char quote = '\"';
        private char escape = '\\';
        private boolean printHeader = true;

        /**
         * @return the delimeter character (cell separator)
         */
        public char getDelimiter() {
            return delimiter;
        }

        /**
         * @param delimiter
         *            the delimeter character (cell separator)
         * @return The config itself (fluent api)
         */
        public CsvConfig withDelimiter(char delimiter) {
            this.delimiter = delimiter;
            return this;
        }

        /**
         * @return the quote character
         */
        public char getQuote() {
            return quote;
        }

        /**
         * @param quote
         *            the quote character
         * @return The config itself (fluent api)
         */
        public CsvConfig withQuote(char quote) {
            this.quote = quote;
            return this;
        }

        /**
         * @return the escape character
         */
        public char getEscape() {
            return escape;
        }

        /**
         * @param escape
         *            the escape character
         * @return The config itself (fluent api)
         */
        public CsvConfig withEscape(char escape) {
            this.escape = escape;
            return this;
        }

        /**
         * @return whether to print header line
         */
        public boolean isPrintHeader() {
            return printHeader;
        }

        /**
         * @param printHeader
         *            whether to print header line
         * @return The config itself (fluent api)
         */
        public CsvConfig withPrintHeader(boolean printHeader) {
            this.printHeader = printHeader;
            return this;
        }

    }

    /**
     * Exports the data of the table into a CSV formatted string
     *
     * @param config
     *            The configuration of the CSV formatting.
     * @return The data in CSV format
     */
    public String exportToCsv(CsvConfig config) {
        CSVFormat format = CSVFormat.DEFAULT
                        .withDelimiter(config.delimiter)
                        .withQuote(config.quote)
                        .withQuoteMode(QuoteMode.NON_NUMERIC)
                        .withEscape(config.escape);

        StringWriter sw = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(sw, format)) {
            if (config.isPrintHeader()) {
                printer.printRecord(columnList.getColumns().stream()
                                .map(c -> c.getColumnDefinition().getTitle())
                                .collect(Collectors.toList()));
            }
            for(TableRow r : rows) {
                if (!(r instanceof ConfigurableTablePrinter.Separator)) {
                    printer.printRecord(r.getAll());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sw.toString();
    }

}
