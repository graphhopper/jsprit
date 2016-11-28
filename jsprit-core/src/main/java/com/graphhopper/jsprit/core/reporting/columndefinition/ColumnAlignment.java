package com.graphhopper.jsprit.core.reporting.columndefinition;

/**
 * Alignment of the column.
 * <p>
 * Longer values will be truncated, shorter values will be padded by spaces.
 * </p>
 *
 * @author balage
 *
 */
public enum ColumnAlignment {
    /**
     * The values are aligned left, padded on the right side.
     */
    LEFT {

        @Override
        public String align(String data, int width) {
            if (data.length() > width) {
                return data.substring(0, width);
            }
            return String.format("%1$-" + width + "s", data);
        }

    },
    /**
     * The values are aligned right, padded on the left side.
     */
    RIGHT {

        @Override
        public String align(String data, int width) {
            if (data.length() > width) {
                return data.substring(0, width);
            }
            return String.format("%1$" + width + "s", data);
        }

    },
    /**
     * The values are centered, padded on the both sides evenly (in case of odd
     * character padding, the left padding will be one more than the right one).
     */
    CENTER {
        @Override
        public String align(String data, int width) {
            if (data.length() > width) {
                return data.substring(0, width);
            }
            int leftPad = (width - data.length())/2;
            return LEFT.align(RIGHT.align(data, width-leftPad), width);
        }
    };

    /**
     * Applies the alignment on the data according the width. Truncates or pads
     * the value.
     * 
     * @param data
     *            The data to align.
     * @param width
     *            The width to pad to.
     * @return The aligned (padded) values with the exact length of width.
     */
    public abstract String align(String data, int width);
}