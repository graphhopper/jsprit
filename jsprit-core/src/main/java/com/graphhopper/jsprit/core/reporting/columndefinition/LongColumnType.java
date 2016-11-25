package com.graphhopper.jsprit.core.reporting.columndefinition;

public class LongColumnType extends AbstractColumnType<Long> {

    public LongColumnType() {
        super();
    }

    public LongColumnType(String nullValue) {
        super(nullValue);
    }

    @Override
    protected String convertNotNull(Long data) {
        return data.toString();
    }

    @Override
    public boolean accepts(Object data) {
        return data instanceof Long;
    }


}