package com.graphhopper.jsprit.core.reporting.columndefinition;

public enum ColumnAlignment {
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