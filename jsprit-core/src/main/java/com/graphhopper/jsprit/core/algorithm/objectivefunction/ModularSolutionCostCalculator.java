package com.graphhopper.jsprit.core.algorithm.objectivefunction;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.solution.SolutionCostCalculator;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;

/**
 * A solution cost calculator implementation with customizable cost components.
 *
 * <p>
 * This calculator uses {@linkplain SolutionCostComponent} implementations to
 * calculate solution cost (objective function). The user can define and manage
 * the set of components it has to take into account. The value of each
 * component is calculated independently and stored. The aggregated cost will be
 * sum of the wighted cost of each component.
 * </p>
 * <p>
 * The calculator has two internal states. In the first one, the components are
 * free to alter. However, by calling the
 * {@linkplain #beforeRun(VehicleRoutingProblem, double)} function, the state
 * change to initialized, and the components are not allowed to be altered any
 * more. ({@linkplain IllegalStateException} is thrown.)
 * </p>
 *
 * @author balage
 *
 */
public class ModularSolutionCostCalculator implements SolutionCostCalculator {

    // The problem the calculator works on
    private VehicleRoutingProblem problem;
    // The registered components
    private Map<SolutionCostComponent, Double> components = new LinkedHashMap<>();

    // Internal state flag
    private boolean initialized = false;

    /**
     * Constructor. Creates a calculator with no components assigned to.
     */
    public ModularSolutionCostCalculator() {
        super();
    }

    /**
     * Registers a new component with a weight of 1.
     *
     * @param component
     *            The component to register.
     * @return The cost calculator itself.
     */
    public ModularSolutionCostCalculator addComponent(SolutionCostComponent component) {
        return addComponent(component, 1d);
    }

    /**
     * Registers several new components in one step, with each component having
     * the weight of 1.
     *
     * @param component
     *            The first component to register.
     * @param components
     *            The additional components.
     * @return The cost calculator itself.
     */
    public ModularSolutionCostCalculator addComponents(SolutionCostComponent component, SolutionCostComponent... components) {
        addComponent(component);
        for (SolutionCostComponent c : components) {
            addComponent(c);
        }
        return this;
    }

    /**
     * Registers a new component with weight specified.
     * <p>
     * Note, that the weight could be zero (the component will be ignored), or
     * even negative which makes the component a benefical (cost reducing)
     * factor.
     * </p>
     *
     * @param component
     *            The component to register.
     * @param weight
     *            The weight of the component.
     *
     * @return The cost calculator itself.
     * @throws IllegalArgumentException
     *             If the component is already registered.
     */
    public ModularSolutionCostCalculator addComponent(SolutionCostComponent component, double weight) {
        if (components.containsKey(component.getId())) {
            throw new IllegalArgumentException("Cost component '" + component.getId() + "' is duplicated.");
        }
        components.put(component, weight);
        return this;
    }


    /**
     * Returns whether the component with the key is registered.
     *
     * @param key
     *            The key to look for.
     * @return True if the component is registered.
     */
    public boolean containsComponent(String key) {
        return components.keySet().stream().anyMatch(c -> c.getId().equals(key));
    }

    /**
     * Returns the component with the key.
     *
     * @param key
     *            The key to look for.
     * @return The registered component if registered or empty.
     */
    public Optional<SolutionCostComponent> findComponent(String key) {
        return components.keySet().stream().filter(c -> c.getId().equals(key)).findAny();
    }

    /**
     * Changes the weigth value of an already registered component. Does nothing
     * if the component is not registered.
     *
     * @param key
     *            The key to look for.
     * @param newWeight
     *            The new weight.
     */
    public void changeComponentWeight(String key, double newWeight) {
        findComponent(key).ifPresent(c -> components.put(c, newWeight));
    }

    /**
     * Removes (unregisters) a component and returns it.
     * <p>
     * This makes it possible to start from a predefined calculator (such as the
     * default one) and reconfiguring one of its components.
     * </p>
     * 
     * @param key
     *            The key of the component to remove.
     * @return The removed component or empty if the component was not
     *         registered.
     */
    public Optional<SolutionCostComponent> removeComponent(String key) {
        Optional<SolutionCostComponent> optC = findComponent(key);
        optC.ifPresent(c -> components.remove(c));
        return optC;
    }

    /**
     * Calculates the cost values of each component.
     *
     * @param solution
     *            The solution to calculate the costs on.
     * @return A list of calculated component cost values.
     */
    public List<ComponentValue> calculate(VehicleRoutingProblemSolution solution) {
        if (!isInitialized()) {
            throw new IllegalStateException("Not initialized.");
        }
        beforeSolution();
        return components.entrySet().stream()
                        .map(en -> en.getKey().calculateCost(problem, solution).withWeight(en.getValue()))
                        .collect(Collectors.toList());
    }


    /**
     * {@inheritDoc}
     * <p>
     * Calls the {@linkplain #calculate(VehicleRoutingProblemSolution)} function
     * and aggregates the weighted sum of the components costs.
     * </p>
     */
    @Override
    public double getCosts(VehicleRoutingProblemSolution solution) {
        if (!isInitialized()) {
            throw new IllegalStateException("Not initialized.");
        }
        return calculate(solution).stream()
                        .mapToDouble(cv -> cv.getWeightedValue())
                        .sum();
    }

    /**
     * Called by the optimizer to initialize the calculator.
     * <p>
     * This function changes the state of the calculator to initialized and no
     * further component management is possible afterward.
     * </p>
     *
     * @param problem
     *            The problem the calculator works on.
     * @param maxCosts
     *            The maximum cost constant to use in cost components.
     */
    public void beforeRun(VehicleRoutingProblem problem, double maxCosts) {
        this.problem = problem;
        components.keySet().forEach(c -> c.beforeRun(problem, maxCosts));
        initialized = true;
    }

    /**
     * @return Whether the calculator is initialized.
     */
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * Called by the calculator to make any pre solution initialization.
     */
    private void beforeSolution() {
        components.keySet().forEach(c -> c.beforeSolution(problem));
    }

}
