package com.graphhopper.jsprit.core.reporting.columndefinition;

/**
 * Interface for columns with human readable formats.
 *
 * @author balage
 *
 * @param <T>
 *            The type of the class itself. (Self-reference)
 */
public interface HumanReadableEnabled<T extends HumanReadableEnabled<T>> {
    /**
     * Sets the formatter.
     * 
     * @param formatter
     *            The formatter.
     * @return The object itself.
     */
    public T withFormatter(HumanReadableTimeFormatter formatter);

    /**
     * Marks the column human readable.
     * 
     * @return The object itself.
     */
    public T asHumanReadable();
}
