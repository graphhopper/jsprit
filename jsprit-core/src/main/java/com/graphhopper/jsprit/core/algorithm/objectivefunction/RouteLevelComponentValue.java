package com.graphhopper.jsprit.core.algorithm.objectivefunction;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class RouteLevelComponentValue extends ComponentValue {

    private Map<Integer, Double> routeLevelValue = new HashMap<>();

    public RouteLevelComponentValue(String key) {
        super(key);
    }

    public void setRouteValue(int routeId, double value) {
        routeLevelValue.put(routeId, value);
        addToValue(value);
    }

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
