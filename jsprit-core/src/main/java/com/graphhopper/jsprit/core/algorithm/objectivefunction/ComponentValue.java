package com.graphhopper.jsprit.core.algorithm.objectivefunction;

/**
 * Data class to hold the value of a cost component.
 *
 * @author balage
 *
 * @see {@linkplain RouteLevelComponentValue}
 * @see {@linkplain SolutionCostComponent}
 */
public class ComponentValue {
    // The key of the component
    private String key;
    // The value (unweighted)
    private double value = 0d;
    // The weight
    private double weight = 1d;

    /**
     * Constrictor with no value set.
     *
     * @param key
     *            The key of the component.
     */
    public ComponentValue(String key) {
        super();
        this.key = key;
    }

    /**
     * Constructor with value set.
     *
     * @param key
     *            The key of the component.
     * @param value
     *            The cost value.
     */
    public ComponentValue(String key, double value) {
        this(key);
        this.value = value;
    }

    /**
     * Alters the weight.
     *
     * @param weight
     *            The new weight.
     * @return The value object itself.
     */
    final ComponentValue withWeight(double weight) {
        this.weight = weight;
        return this;
    }

    /**
     * @return The key of the component.
     *
     */
    public String getKey() {
        return key;
    }

    /**
     * @return The cost value (unweighted).
     */
    public double getValue() {
        return value;
    }

    /**
     * @return The weight.
     */
    public double getWeight() {
        return weight;
    }

    /**
     * @return The weighted value.
     */
    public double getWeightedValue() {
        return weight * value;
    }

    /**
     * Increases the value.
     *
     * @param valueToAdd
     *            The value to add to the current value.
     */
    protected void addToValue(double valueToAdd) {
        value += valueToAdd;
    }

    @Override
    public String toString() {
        return "ComponentValue [key=" + key + ", weight=" + weight + ", value=" + value + "]";
    }

    /**
     * Clones the value object.
     * 
     * @return A copy of the value object.
     */
    public ComponentValue copy() {
        return new ComponentValue(key, value).withWeight(weight);
    }

}