package com.graphhopper.jsprit.core.reporting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.graphhopper.jsprit.core.reporting.columndefinition.ColumnDefinition;

/**
 * Table definition form dynamic table printers (both implementations)
 *
 * @author balage
 * @see {@linkplain DynamicTablePrinter}
 * @see {@linkplain ConfigurableTablePrinter}
 */
public class DynamicTableDefinition {

    /**
     * Builder for the table definition.
     *
     * @author balage
     */
    public static class Builder {
        private char corner = '+';
        private char vertical = '|';
        private char horizontal = '-';

        private String heading = null;
        private List<ColumnDefinition> columns = new ArrayList<>();

        private int padding = 1;

        /**
         * @param corner
         *            The corner (where vertical and horizontal lines meet)
         *            character.
         * @return the builder
         */
        public Builder withCorner(char corner) {
            this.corner = corner;
            return this;
        }

        /**
         * @param vertical
         *            The vertical line character.
         * @return the builder
         */
        public Builder withVertical(char vertical) {
            this.vertical = vertical;
            return this;
        }

        /**
         * @param horizontal
         *            The horizontal line character.
         * @return the builder
         */
        public Builder withHorizontal(char horizontal) {
            this.horizontal = horizontal;
            return this;
        }

        /**
         * @param heading
         *            The heading text of the table. If not defined or null
         *            specified, no heading will be printed.
         * @return the builder
         */
        public Builder withHeading(String heading) {
            this.heading = heading;
            return this;
        }

        /**
         * Adds a column for the table definition.
         *
         * @param column
         *            The column definition to add.
         * @return the builder
         */
        public Builder addColumn(ColumnDefinition column) {
            columns.add(column);
            return this;
        }

        /**
         * @param padding
         *            The padding size of the table.
         * @return the builder
         */
        public Builder withPadding(int padding) {
            this.padding = Math.max(0, padding);
            return this;
        }

        /**
         * @return The imutable table definition object.
         */
        public DynamicTableDefinition build() {
            return new DynamicTableDefinition(this);
        }
    }


    private char corner = '+';
    private char vertical = '|';
    private char horizontal = '-';

    private String heading = null;
    private List<ColumnDefinition> columns = new ArrayList<>();

    private int padding = 1;

    /**
     * Private constructor for builder.
     *
     * @param builder
     *            the builder to initialize from.
     */
    private DynamicTableDefinition(Builder builder) {
        corner = builder.corner;
        vertical = builder.vertical;
        horizontal = builder.horizontal;
        heading = builder.heading;
        columns = Collections.unmodifiableList(builder.columns);
        padding = builder.padding;
    }

    /**
     * @return the corner (where vertical and horizontal lines meet) character.
     */
    public char getCorner() {
        return corner;
    }

    /**
     * @return the character for vertical line
     */
    public char getVertical() {
        return vertical;
    }

    /**
     * @return the character for horizontal line
     */
    public char getHorizontal() {
        return horizontal;
    }

    /**
     * @return the heading text
     */
    public String getHeading() {
        return heading;
    }

    /**
     * @return the unmodifiable column list
     */
    public List<ColumnDefinition> getColumns() {
        return Collections.unmodifiableList(columns);
    }

    /**
     * @return the padding size
     */
    public int getPadding() {
        return padding;
    }

    /**
     * @return The number of columns.
     */
    public int size() {
        return columns.size();
    }

}
