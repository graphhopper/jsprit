package com.graphhopper.jsprit.core.reporting.columndefinition;

public class ColumnDefinition {

    public static class Builder {
        private ColumnType<?> type;
        private String title;
        private int minWidth = 0;
        private int maxWidth = Integer.MAX_VALUE;
        private ColumnAlignment alignment = ColumnAlignment.LEFT;

        public Builder(ColumnType<?> type, String title) {
            super();
            this.type = type;
            this.title = title;
        }

        public Builder(ColumnType<?> type) {
            super();
            this.type = type;
        }

        public ColumnDefinition.Builder withTitle(String title) {
            this.title = title;
            return this;
        }

        public ColumnDefinition.Builder withMinWidth(int minWidth) {
            this.minWidth = minWidth;
            return this;
        }

        public ColumnDefinition.Builder withMaxWidth(int maxWidth) {
            this.maxWidth = maxWidth;
            return this;
        }

        public ColumnDefinition.Builder withAlignment(ColumnAlignment alignment) {
            this.alignment = alignment;
            return this;
        }

        public ColumnDefinition build() {
            return new ColumnDefinition(this);
        }

    }

    private ColumnType<?> type;
    private String title;
    private int minWidth = 0;
    private int maxWidth = Integer.MAX_VALUE;
    private ColumnAlignment alignment = ColumnAlignment.LEFT;

    private ColumnDefinition(ColumnDefinition.Builder builder) {
        type = builder.type;
        title = builder.title;
        minWidth = builder.minWidth;
        maxWidth = builder.maxWidth;
        alignment = builder.alignment;
    }

    public ColumnType<?> getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public int getMinWidth() {
        return minWidth;
    }

    public int getMaxWidth() {
        return maxWidth;
    }

    public ColumnAlignment getAlignment() {
        return alignment;
    }

}