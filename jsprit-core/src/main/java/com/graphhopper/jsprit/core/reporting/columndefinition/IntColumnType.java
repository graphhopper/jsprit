package com.graphhopper.jsprit.core.reporting.columndefinition;

/**
 * A column type with integer (4 byte) values.
 *
 * @author balage
 *
 */
public class IntColumnType extends AbstractColumnType<Integer> {

    /**
     * Konstructor. The column will use the default values for null.
     */
    public IntColumnType() {
        super();
    }

    /**
     * Konstructor.
     *
     * @param nullValue
     *            The text representation for null values.
     */
    public IntColumnType(String nullValue) {
        super(nullValue);
    }

    @Override
    protected String convertNotNull(Integer data) {
        return data.toString();
    }

    /**
     * {@inheritDoc} Only accepts Integer values.
     */
    @Override
    public boolean accepts(Object data) {
        return data instanceof Integer;
    }

}