package com.graphhopper.jsprit.core.reporting.columndefinition;

public class DoubleColumnType extends AbstractColumnType<Double> {

    private int decimals = 2;

    public DoubleColumnType() {
        super();
    }

    public DoubleColumnType(String nullValue) {
        super(nullValue);
    }

    public DoubleColumnType(int decimals) {
        super();
        this.decimals = decimals;
    }

    public DoubleColumnType(String nullValue, int decimals) {
        super(nullValue);
        this.decimals = decimals;
    }

    @Override
    protected String convertNotNull(Double data) {
        return String.format("%50." + decimals + "f", data).trim();
    }

    @Override
    public boolean accepts(Object data) {
        return data instanceof Double;
    }


}