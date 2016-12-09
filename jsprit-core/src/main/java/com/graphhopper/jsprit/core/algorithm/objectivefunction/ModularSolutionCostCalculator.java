package com.graphhopper.jsprit.core.algorithm.objectivefunction;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.solution.SolutionCostCalculator;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;

public class ModularSolutionCostCalculator implements SolutionCostCalculator {

    private VehicleRoutingProblem problem;
    private Map<SolutionCostComponent, Double> components = new LinkedHashMap<>();
    private double maxCosts;

    public ModularSolutionCostCalculator(VehicleRoutingProblem problem, double maxCosts) {
        super();
        this.problem = problem;
        this.maxCosts = maxCosts;
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
        return components.entrySet().stream()
                        .map(en -> en.getKey().calculateCost(problem, solution).withWeight(en.getValue()))
                        .collect(Collectors.toList());}


    @Override
    public double getCosts(VehicleRoutingProblemSolution solution) {
        return calculate(solution).stream()
                        .mapToDouble(cv -> cv.getWeightedValue())
                        .sum();
    }

    public void beforeRun() {
        components.keySet().forEach(c -> c.beforeRun(problem, maxCosts));
    }

    // TODO: Is it needed and can be easily integrated?
    public void beforeSolution() {
        components.keySet().forEach(c -> c.beforeSolution(problem));
    }

}
