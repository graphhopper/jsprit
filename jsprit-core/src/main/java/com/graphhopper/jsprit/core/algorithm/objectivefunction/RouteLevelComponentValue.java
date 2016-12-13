package com.graphhopper.jsprit.core.algorithm.objectivefunction;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * A cost value implementation which holds an additional per route cost
 * information.
 *
 * @author balage
 *
 */
public class RouteLevelComponentValue extends ComponentValue {

    // The per route cost value
    private Map<Integer, Double> routeLevelValue = new HashMap<>();

    /**
     * Constrictor with no value set.
     *
     * @param key
     *            The key of the component.
     */
    public RouteLevelComponentValue(String key) {
        super(key);
    }

    /**
     * Sets the route value.
     *
     * @param routeId
     *            The route id.
     * @param value
     *            The value of the route.
     */
    public void setRouteValue(int routeId, double value) {
        getRouteValue(routeId).ifPresent(v -> addToValue(-v));
        routeLevelValue.put(routeId, value);
        addToValue(value);
    }

    /**
     * Returns the cost value of the route.
     * 
     * @param routeId
     *            The route id.
     * @return The cost value of the route or empty if no value available for
     *         the route.
     */
    public Optional<Double> getRouteValue(int routeId) {
        if (routeLevelValue.containsKey(routeId)) {
            return Optional.ofNullable(routeLevelValue.get(routeId));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public String toString() {
        return "RouteLevelComponentValue [key=" + getKey() + ", weight="
                        + getWeight() + ", value=" + getValue() + "routeLevel=" + routeLevelValue + "]";
    }

    @Override
    public RouteLevelComponentValue copy() {
        RouteLevelComponentValue copy = new RouteLevelComponentValue(getKey());
        copy.withWeight(getWeight());
        copy.routeLevelValue = new HashMap<>(routeLevelValue);
        return copy;
    }

}
