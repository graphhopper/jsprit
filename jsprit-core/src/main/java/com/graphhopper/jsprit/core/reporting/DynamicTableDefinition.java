package com.graphhopper.jsprit.core.reporting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author balage
 */
public class DynamicTableDefinition {

    public static interface ColumnType<T> {
        public String convert(Object data);

        public boolean accepts(Object data);
    }

    public static abstract class AbstractColumnType<T> implements ColumnType<T> {

        private String nullValue = "";


        public AbstractColumnType() {
            super();
        }

        public AbstractColumnType(String nullValue) {
            super();
            this.nullValue = nullValue;
        }

        @SuppressWarnings("unchecked")
        @Override
        public String convert(Object data) {
            if (data == null) {
                return nullValue;
            } else {
                if (accepts(data)) {
                    return convertNotNull((T) data);
                } else {
                    throw new ClassCastException();
                }
            }
        }

        protected abstract String convertNotNull(T data);
    }

    public static class StringColumnType extends AbstractColumnType<Object> {

        public StringColumnType() {
            super();
        }

        public StringColumnType(String nullValue) {
            super(nullValue);
        }

        @Override
        protected String convertNotNull(Object data) {
            return data.toString();
        }

        @Override
        public boolean accepts(Object data) {
            return true;
        }

    }

    public static class IntColumnType extends AbstractColumnType<Integer> {

        public IntColumnType() {
            super();
        }

        public IntColumnType(String nullValue) {
            super(nullValue);
        }

        @Override
        protected String convertNotNull(Integer data) {
            return data.toString();
        }

        @Override
        public boolean accepts(Object data) {
            return data instanceof Integer;
        }

    }

    public static class LongColumnType extends AbstractColumnType<Long> {

        public LongColumnType() {
            super();
        }

        public LongColumnType(String nullValue) {
            super(nullValue);
        }

        @Override
        protected String convertNotNull(Long data) {
            return data.toString();
        }

        @Override
        public boolean accepts(Object data) {
            return data instanceof Long;
        }


    }

    public static class DoubleColumnType extends AbstractColumnType<Double> {

        private int decimals = 2;

        public DoubleColumnType() {
            super();
        }

        public DoubleColumnType(String nullValue) {
            super(nullValue);
        }

        public DoubleColumnType(int decimals) {
            super();
            this.decimals = decimals;
        }

        public DoubleColumnType(String nullValue, int decimals) {
            super(nullValue);
            this.decimals = decimals;
        }

        @Override
        protected String convertNotNull(Double data) {
            return String.format("%50." + decimals + "f", data).trim();
        }

        @Override
        public boolean accepts(Object data) {
            return data instanceof Double;
        }


    }


    public static class BooleanColumnType extends AbstractColumnType<Boolean> {
        private String trueValue = "true";
        private String falseValue = "false";

        public BooleanColumnType() {
            super();
        }

        public BooleanColumnType(String nullValue) {
            super(nullValue);
        }

        public BooleanColumnType(String trueValue, String falseValue) {
            super();
            this.trueValue = trueValue;
            this.falseValue = falseValue;
        }

        public BooleanColumnType(String trueValue, String falseValue, String nullValue) {
            super(nullValue);
            this.trueValue = trueValue;
            this.falseValue = falseValue;
        }

        @Override
        protected String convertNotNull(Boolean data) {
            return data ? trueValue : falseValue;
        }

        @Override
        public boolean accepts(Object data) {
            return data instanceof Boolean;
        }
    }

    public enum Alignment {
        LEFT {

            @Override
            public String align(String data, int width) {
                if (data.length() > width) {
                    return data.substring(0, width);
                }
                return String.format("%1$-" + width + "s", data);
            }

        }, RIGHT {

            @Override
            public String align(String data, int width) {
                if (data.length() > width) {
                    return data.substring(0, width);
                }
                return String.format("%1$" + width + "s", data);
            }

        }, CENTER {
            @Override
            public String align(String data, int width) {
                if (data.length() > width) {
                    return data.substring(0, width);
                }
                int leftPad = (width - data.length())/2;
                return LEFT.align(RIGHT.align(data, width-leftPad), width);
            }
        };

        public abstract String align(String data, int width);
    }

    public static class ColumnDefinition {

        public static class Builder {
            private ColumnType<?> type;
            private String title;
            private int minWidth = 0;
            private int maxWidth = Integer.MAX_VALUE;
            private Alignment alignment = Alignment.LEFT;

            public Builder(ColumnType<?> type, String title) {
                super();
                this.type = type;
                this.title = title;
            }

            public Builder(ColumnType<?> type) {
                super();
                this.type = type;
            }

            public Builder withTitle(String title) {
                this.title = title;
                return this;
            }

            public Builder withMinWidth(int minWidth) {
                this.minWidth = minWidth;
                return this;
            }

            public Builder withMaxWidth(int maxWidth) {
                this.maxWidth = maxWidth;
                return this;
            }

            public Builder withAlignment(Alignment alignment) {
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
        private Alignment alignment = Alignment.LEFT;

        private ColumnDefinition(Builder builder) {
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

        public Alignment getAlignment() {
            return alignment;
        }

    }


    public static class Builder {
        private char corner = '+';
        private char vertical = '|';
        private char horizontal = '-';

        private String heading = null;
        private List<ColumnDefinition> columns = new ArrayList<>();

        private int padding = 1;

        public Builder withCorner(char corner) {
            this.corner = corner;
            return this;
        }

        public Builder withVertical(char vertical) {
            this.vertical = vertical;
            return this;
        }

        public Builder withHorizontal(char horizontal) {
            this.horizontal = horizontal;
            return this;
        }

        public Builder withHeading(String heading) {
            this.heading = heading;
            return this;
        }

        public Builder addColumn(ColumnDefinition column) {
            columns.add(column);
            return this;
        }

        public Builder withPadding(int padding) {
            this.padding = Math.max(0, padding);
            return this;
        }

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

    private DynamicTableDefinition(Builder builder) {
        corner = builder.corner;
        vertical = builder.vertical;
        horizontal = builder.horizontal;
        heading = builder.heading;
        columns = Collections.unmodifiableList(builder.columns);
        padding = builder.padding;
    }

    public char getCorner() {
        return corner;
    }

    public char getVertical() {
        return vertical;
    }

    public char getHorizontal() {
        return horizontal;
    }

    public String getHeading() {
        return heading;
    }

    public List<ColumnDefinition> getColumns() {
        return columns;
    }

    public int getPadding() {
        return padding;
    }

    public int size() {
        return columns.size();
    }

}
