package com.graphhopper.jsprit.core.reporting.columndefinition;

public class BooleanColumnType extends AbstractColumnType<Boolean> {
    private String trueValue = "true";
    private String falseValue = "false";

    public BooleanColumnType() {
        super();
    }

    public BooleanColumnType(String nullValue) {
        super(nullValue);
    }

    public BooleanColumnType(String trueValue, String falseValue) {
        super();
        this.trueValue = trueValue;
        this.falseValue = falseValue;
    }

    public BooleanColumnType(String trueValue, String falseValue, String nullValue) {
        super(nullValue);
        this.trueValue = trueValue;
        this.falseValue = falseValue;
    }

    @Override
    protected String convertNotNull(Boolean data) {
        return data ? trueValue : falseValue;
    }

    @Override
    public boolean accepts(Object data) {
        return data instanceof Boolean;
    }
}