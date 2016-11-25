package com.graphhopper.jsprit.core.reporting.route;

public interface HumanReadableEnabled<T extends HumanReadableEnabled<T>> {
    public T withFormatter(HumanReadableTimeFormatter formatter);

    public T asHumanReadable();
}
