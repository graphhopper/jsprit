package com.graphhopper.jsprit.core.reporting.columndefinition;

/**
 * A column type with long (8-byte) values.
 *
 * @author balage
 *
 */
public class LongColumnType extends AbstractColumnType<Long> {

    /**
     * Konstructor. The column will use the default values for null.
     */
    public LongColumnType() {
        super();
    }

    /**
     * Konstructor.
     *
     * @param nullValue
     *            The text representation for null values.
     */
    public LongColumnType(String nullValue) {
        super(nullValue);
    }

    @Override
    protected String convertNotNull(Long data) {
        return data.toString();
    }

    /**
     * {@inheritDoc} Only accepts Long values.
     */
    @Override
    public boolean accepts(Object data) {
        return data instanceof Long;
    }


}