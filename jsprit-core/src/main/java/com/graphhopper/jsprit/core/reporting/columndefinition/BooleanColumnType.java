package com.graphhopper.jsprit.core.reporting.columndefinition;

/**
 * A column type with boolean values.
 * <p>
 * The display value for true and false values could be configured.
 * </p>
 *
 * @author balage
 *
 */
public class BooleanColumnType extends AbstractColumnType<Boolean> {
    // The display value for true
    private String trueValue = "true";
    // The display value for false
    private String falseValue = "false";

    /**
     * Konstructor. The column will use the default values for null, true or
     * false.
     */
    public BooleanColumnType() {
        super();
    }

    /**
     * Konstructor. The column will use the default values for true or false.
     *
     * @param nullValue
     *            The text representation for null values.
     */
    public BooleanColumnType(String nullValue) {
        super(nullValue);
    }

    /**
     * Konstructor. The column will use the default values for null.
     *
     * @param trueValue
     *            The text representation for true values.
     * @param falseValue
     *            The text representation for false values.
     */
    public BooleanColumnType(String trueValue, String falseValue) {
        super();
        this.trueValue = trueValue;
        this.falseValue = falseValue;
    }

    /**
     * Konstructor.
     *
     * @param trueValue
     *            The text representation for true values.
     * @param falseValue
     *            The text representation for false values.
     * @param nullValue
     *            The text representation for null values.
     */
    public BooleanColumnType(String trueValue, String falseValue, String nullValue) {
        super(nullValue);
        this.trueValue = trueValue;
        this.falseValue = falseValue;
    }

    @Override
    protected String convertNotNull(Boolean data) {
        return data ? trueValue : falseValue;
    }

    /**
     * {@inheritDoc}
     *
     * Only accepts {@linkplain Boolean} input.
     */
    @Override
    public boolean accepts(Object data) {
        return data instanceof Boolean;
    }
}