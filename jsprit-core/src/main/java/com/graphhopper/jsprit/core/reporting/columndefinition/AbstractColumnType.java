package com.graphhopper.jsprit.core.reporting.columndefinition;

/**
 * Common abstract ancestor for column types.
 *
 * @author balage
 *
 * @param <T>
 *            The type it accepts.
 */
public abstract class AbstractColumnType<T> implements ColumnType<T> {

    // The string to used as null value
    private String nullValue = "";

    public AbstractColumnType() {
        super();
    }

    /**
     * @param nullValue
     *            alternative null value
     */
    public AbstractColumnType(String nullValue) {
        super();
        this.nullValue = nullValue;
    }

    /**
     * {@inheritDoc}
     *
     * This basic implementation takes the burden to handle null values and
     * calls the {@linkplain #convertNotNull(Object)} for all other values.
     *
     * @see com.graphhopper.jsprit.core.reporting.columndefinition.ColumnType#convert(java.lang.Object)
     *
     * @throws ClassCastException
     *             if the data is not accepted by the column type.
     */
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

    /**
     * Converts the data into String. This function never gets null as
     * parameter.
     *
     * @param data
     *            the non-null data to convert.
     * @return The converted data.
     */
    protected abstract String convertNotNull(T data);
}