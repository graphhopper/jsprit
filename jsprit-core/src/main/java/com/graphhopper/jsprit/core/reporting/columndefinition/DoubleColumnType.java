package com.graphhopper.jsprit.core.reporting.columndefinition;

/**
 * A column type with double values.
 * <p>
 * The number of decimal places could be configured.
 * </p>
 *
 * @author balage
 *
 */
public class DoubleColumnType extends AbstractColumnType<Double> {

    // The number of displayed decimal places
    private int decimals = 2;

    /**
     * Konstructor. The column will use the default values for null and the
     * significant decimal places.
     */
    public DoubleColumnType() {
        super();
    }

    /**
     * Konstructor. The column will use the default values for the significant
     * decimal places.
     *
     * @param nullValue
     *            The text representation for null values.
     */
    public DoubleColumnType(String nullValue) {
        super(nullValue);
    }

    /**
     * Konstructor. The column will use the default values for null.
     *
     * @param decimals The number of decimal places to display.
     * @throws IllegalArgumentException If the parameter is negative.
     */
    public DoubleColumnType(int decimals) {
        super();
        if (decimals < 0) {
            throw new IllegalArgumentException("Decimal places should be 0 or more.");
        }
        this.decimals = decimals;
    }

    /**
     * Konstructor.
     *
     * @param decimals
     *            The number of decimal places to display.
     * @param nullValue
     *            The text representation for null values.
     * @throws IllegalArgumentException
     *             If the parameter is negative.
     */
    public DoubleColumnType(int decimals, String nullValue) {
        super(nullValue);
        if (decimals < 0) {
            throw new IllegalArgumentException("Decimal places should be 0 or more.");
        }
        this.decimals = decimals;
    }

    @Override
    protected String convertNotNull(Double data) {
        return String.format("%50." + decimals + "f", data).trim();
    }

    /**
     * {@inheritDoc} Only accepts Double values.
     */
    @Override
    public boolean accepts(Object data) {
        return data instanceof Double;
    }


}
