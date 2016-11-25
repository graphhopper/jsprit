package com.graphhopper.jsprit.core.reporting.columndefinition;

public class IntColumnType extends AbstractColumnType<Integer> {

    public IntColumnType() {
        super();
    }

    public IntColumnType(String nullValue) {
        super(nullValue);
    }

    @Override
    protected String convertNotNull(Integer data) {
        return data.toString();
    }

    @Override
    public boolean accepts(Object data) {
        return data instanceof Integer;
    }

}