package com.graphhopper.jsprit.core.reporting.columndefinition;

/**
 * Column definition. Contains all information for converting and formatting the
 * column.
 * <p>
 * The definition itself immutable and cannot be directly instantiate. Use the
 * {@linkplain Builder} class for constructing the definition.
 * </p>
 *
 * @author balage
 *
 */
public class ColumnDefinition {

    /**
     * The builder for {@linkplain ColumnDefinition}.
     * <p>
     * When it is not specified, the default title is null (the default title of
     * the column will be used), the minWidth is 0, the maxWidth is
     * {@linkplain Integer#MAX_VALUE} and the alignment is
     * {@linkplain ColumnAlignment#LEFT}.
     * </p>
     *
     * @author balage
     *
     */
    public static class Builder {
        // Type of the column.
        private ColumnType<?> type;
        // The title of the column.
        private String title;
        // The minimal width of the column.
        private int minWidth = 0;
        // The maximal width of the column.
        private int maxWidth = Integer.MAX_VALUE;
        // The alignment of the column.
        private ColumnAlignment alignment = ColumnAlignment.LEFT;

        /**
         * Constructor with title specified.
         *
         * @param type
         *            Type of the column.
         * @param title
         *            The title of the column.
         * @see {@linkplain #withTitle(String)}
         */
        public Builder(ColumnType<?> type, String title) {
            super();
            this.type = type;
            this.title = title;
        }

        /**
         * Constructor.
         *
         * @param type
         *            Type of the column.
         */
        public Builder(ColumnType<?> type) {
            super();
            this.type = type;
        }

        /**
         * @param title
         *            The title of the column
         * @return The builder object.
         */
        public ColumnDefinition.Builder withTitle(String title) {
            this.title = title;
            return this;
        }

        /**
         * @param minWidth
         *            The minimal width of the column.
         * @return The builder object.
         * @throws IllegalArgumentException
         *             If the minWidth is negative or higher than the maxWidth.
         */
        public ColumnDefinition.Builder withMinWidth(int minWidth) {
            if (minWidth < 0) {
                throw new IllegalArgumentException("Minimal width should be non-negative.");
            }
            if (minWidth > maxWidth) {
                throw new IllegalArgumentException("Minimal width should be less or equal than the maximal width.");
            }
            this.minWidth = minWidth;
            return this;
        }

        /**
         * @param maxWidth
         *            The maximal width of the column.
         * @return The builder object.
         * @throws IllegalArgumentException
         *             If the maxWidth is negative or less than the minWidth.
         */
        public ColumnDefinition.Builder withMaxWidth(int maxWidth) {
            if (maxWidth < 0) {
                throw new IllegalArgumentException("Maximal width should be non-negative.");
            }
            if (maxWidth > maxWidth) {
                throw new IllegalArgumentException("Maximal width should be greater or equal than the minimal width.");
            }
            this.maxWidth = maxWidth;
            return this;
        }

        /**
         * @param alignment
         *            The alignment of the column.
         * @return The builder object.
         */
        public ColumnDefinition.Builder withAlignment(ColumnAlignment alignment) {
            this.alignment = alignment;
            return this;
        }

        /**
         * @return The constructed imutable definition object.
         */
        public ColumnDefinition build() {
            return new ColumnDefinition(this);
        }

    }


    // Type of the column.
    private ColumnType<?> type;
    // The title of the column.
    private String title;
    // The minimal width of the column.
    private int minWidth = 0;
    // The maximal width of the column.
    private int maxWidth = Integer.MAX_VALUE;
    // The alignment of the column.
    private ColumnAlignment alignment = ColumnAlignment.LEFT;

    /**
     * Private constructor for the builder.
     *
     * @param builder
     *            The builder.
     */
    private ColumnDefinition(ColumnDefinition.Builder builder) {
        type = builder.type;
        title = builder.title;
        minWidth = builder.minWidth;
        maxWidth = builder.maxWidth;
        alignment = builder.alignment;
    }

    /**
     * @return The type of the column.
     */
    public ColumnType<?> getType() {
        return type;
    }

    /**
     * @return The title of the column. If null, the default title will be used.
     */
    public String getTitle() {
        return title;
    }

    /**
     * @return The minimal width of the column.
     */
    public int getMinWidth() {
        return minWidth;
    }

    /**
     * @return The maximal width of the column.
     */
    public int getMaxWidth() {
        return maxWidth;
    }

    /**
     * @return The alignment of the column.
     */
    public ColumnAlignment getAlignment() {
        return alignment;
    }

}
