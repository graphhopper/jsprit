package com.graphhopper.jsprit.core.algorithm.objectivefunction;

public class ComponentValue {
    private String key;
    private double value = 0d;
    private double weight = 1d;

    public ComponentValue(String key) {
        super();
        this.key = key;
    }

    public ComponentValue(String key, double value) {
        this(key);
        this.value = value;
    }

    final ComponentValue withWeight(double weight) {
        this.weight = weight;
        return this;
    }

    public String getKey() {
        return key;
    }

    public double getValue() {
        return value;
    }

    public double getWeight() {
        return weight;
    }

    public double getWeightedValue() {
        return weight * value;
    }

    protected void addToValue(double valueToAdd) {
        value += valueToAdd;
    }

    @Override
    public String toString() {
        return "ComponentValue [key=" + key + ", weight=" + weight + ", value=" + value + "]";
    }

}