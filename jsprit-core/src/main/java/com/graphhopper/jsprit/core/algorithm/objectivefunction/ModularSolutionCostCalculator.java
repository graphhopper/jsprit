package com.graphhopper.jsprit.core.algorithm.objectivefunction;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.solution.SolutionCostCalculator;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;

public class ModularSolutionCostCalculator implements SolutionCostCalculator {

    private VehicleRoutingProblem problem;
    private Map<SolutionCostComponent, Double> components = new LinkedHashMap<>();

    private boolean initialized = false;

    public ModularSolutionCostCalculator() {
        super();
    }

    public ModularSolutionCostCalculator addComponent(SolutionCostComponent component) {
        return addComponent(component, 1d);
    }

    public ModularSolutionCostCalculator addComponents(SolutionCostComponent component, SolutionCostComponent... components) {
        addComponent(component);
        for (SolutionCostComponent c : components) {
            addComponent(c);
        }
        return this;
    }

    public ModularSolutionCostCalculator addComponent(SolutionCostComponent component, double weight) {
        if (components.containsKey(component.getId())) {
            throw new IllegalArgumentException("Cost component '" + component.getId() + "' is duplicated.");
        }
        components.put(component, weight);
        return this;
    }

    public List<ComponentValue> calculate(VehicleRoutingProblemSolution solution) {
        if (!isInitialized()) {
            throw new IllegalStateException("Not initialized.");
        }
        beforeSolution();
        return components.entrySet().stream()
                        .map(en -> en.getKey().calculateCost(problem, solution).withWeight(en.getValue()))
                        .collect(Collectors.toList());}


    @Override
    public double getCosts(VehicleRoutingProblemSolution solution) {
        if (!isInitialized()) {
            throw new IllegalStateException("Not initialized.");
        }
        return calculate(solution).stream()
                        .mapToDouble(cv -> cv.getWeightedValue())
                        .sum();
    }

    public void beforeRun(VehicleRoutingProblem problem, double maxCosts) {
        this.problem = problem;
        components.keySet().forEach(c -> c.beforeRun(problem, maxCosts));
        initialized = true;
    }

    public boolean isInitialized() {
        return initialized;
    }

    private void beforeSolution() {
        components.keySet().forEach(c -> c.beforeSolution(problem));
    }

    public boolean containsComponent(String key) {
        return components.keySet().stream().anyMatch(c -> c.getId().equals(key));
    }

    public Optional<SolutionCostComponent> findComponent(String key) {
        return components.keySet().stream().filter(c -> c.getId().equals(key)).findAny();
    }

    public void changeComponentWeight(String key, double newWeight) {
        findComponent(key).ifPresent(c -> components.put(c, newWeight));
    }

    public Optional<SolutionCostComponent> removeComponent(String key) {
        Optional<SolutionCostComponent> optC = findComponent(key);
        optC.ifPresent(c -> components.remove(c));
        return optC;
    }

}
