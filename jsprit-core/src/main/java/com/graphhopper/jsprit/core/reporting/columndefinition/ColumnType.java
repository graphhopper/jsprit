package com.graphhopper.jsprit.core.reporting.columndefinition;

/**
 * Column type.
 *
 * @author balage
 *
 * @param <T>
 *            The object type it accepts.
 */
public interface ColumnType<T> {
    /**
     * Converts the data into String.
     * 
     * @param data
     *            the data to convert.
     * @return The converted data.
     */
    public String convert(Object data);

    /**
     * Checks if the given data is acceptable for the type. (Mostly by class
     * type.)
     * 
     * @param data
     *            the data to check
     * @return True if the data can be converted by this implementation.
     */
    public boolean accepts(Object data);
}