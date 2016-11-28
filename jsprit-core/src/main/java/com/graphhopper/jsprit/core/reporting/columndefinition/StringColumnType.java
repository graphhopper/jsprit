package com.graphhopper.jsprit.core.reporting.columndefinition;

/**
 * A column type for any values.
 *
 * @author balage
 *
 */
public class StringColumnType extends AbstractColumnType<Object> {

    /**
     * Konstructor. The column will use the default values for null.
     */
    public StringColumnType() {
        super();
    }

    /**
     * Konstructor.
     *
     * @param nullValue
     *            The text representation for null values.
     */
    public StringColumnType(String nullValue) {
        super(nullValue);
    }

    @Override
    protected String convertNotNull(Object data) {
        return data.toString();
    }

    /**
     * {@inheritDoc} Accepts any type of values (uses
     * {@linkplain Object#toString()}).
     */
    @Override
    public boolean accepts(Object data) {
        return true;
    }

}
