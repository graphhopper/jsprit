package com.graphhopper.jsprit.core.reporting.columndefinition;

public class StringColumnType extends AbstractColumnType<Object> {

    public StringColumnType() {
        super();
    }

    public StringColumnType(String nullValue) {
        super(nullValue);
    }

    @Override
    protected String convertNotNull(Object data) {
        return data.toString();
    }

    @Override
    public boolean accepts(Object data) {
        return true;
    }

}