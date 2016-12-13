package com.graphhopper.jsprit.core.algorithm.objectivefunction;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.Optional;

import org.junit.Test;

import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;

public class ModularSolutionCostCalculatorTest {

    @Test
    public void whenModuleAdded_itIsThere() {
        ModularSolutionCostCalculator calc = new ModularSolutionCostCalculator();
        FixCostPerVehicle fix = new FixCostPerVehicle();
        calc.addComponent(fix);
        assertEquals(1, calc.getComponentCount());
        assertEquals(true, calc.findComponent(FixCostPerVehicle.COMPONENT_ID).isPresent());
        assertEquals(fix, calc.findComponent(FixCostPerVehicle.COMPONENT_ID).get());
    }

    @Test
    public void whenAnotherModuleAdded_itIsAlsoThere() {
        ModularSolutionCostCalculator calc = new ModularSolutionCostCalculator();
        FixCostPerVehicle fix = new FixCostPerVehicle();
        UnassignedJobs unassigned = new UnassignedJobs();
        calc.addComponent(fix);
        calc.addComponent(unassigned);
        assertEquals(2, calc.getComponentCount());
        assertEquals(true, calc.findComponent(FixCostPerVehicle.COMPONENT_ID).isPresent());
        assertEquals(fix, calc.findComponent(FixCostPerVehicle.COMPONENT_ID).get());
        assertEquals(true, calc.findComponent(UnassignedJobs.COMPONENT_ID).isPresent());
        assertEquals(unassigned, calc.findComponent(UnassignedJobs.COMPONENT_ID).get());
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenModuleAddedTwice_exceptionIsThrown() {
        ModularSolutionCostCalculator calc = new ModularSolutionCostCalculator();
        FixCostPerVehicle fix = new FixCostPerVehicle();
        calc.addComponent(fix);
        calc.addComponent(fix);
    }

    @Test
    public void whenRegisteredModuleRemoved_itIsRemoved() {
        ModularSolutionCostCalculator calc = new ModularSolutionCostCalculator();
        FixCostPerVehicle fix = new FixCostPerVehicle();
        UnassignedJobs unassigned = new UnassignedJobs();
        calc.addComponent(fix);
        calc.addComponent(unassigned);
        calc.removeComponent(FixCostPerVehicle.COMPONENT_ID);

        assertEquals(1, calc.getComponentCount());
        assertEquals(false, calc.findComponent(FixCostPerVehicle.COMPONENT_ID).isPresent());
        assertEquals(true, calc.findComponent(UnassignedJobs.COMPONENT_ID).isPresent());
    }

    @Test
    public void whenRegisteredModuleRemoved_itIsReturned() {
        ModularSolutionCostCalculator calc = new ModularSolutionCostCalculator();
        FixCostPerVehicle fix = new FixCostPerVehicle();
        UnassignedJobs unassigned = new UnassignedJobs();
        calc.addComponent(fix);
        calc.addComponent(unassigned);
        Optional<SolutionCostComponent> res = calc.removeComponent(FixCostPerVehicle.COMPONENT_ID);

        assertTrue(res.isPresent());
        assertEquals(fix, res.get());
    }

    @Test
    public void whenUnregisteredModuleRemoved_nothingHappens() {
        ModularSolutionCostCalculator calc = new ModularSolutionCostCalculator();
        UnassignedJobs unassigned = new UnassignedJobs();
        calc.addComponent(unassigned);
        Optional<SolutionCostComponent> res = calc.removeComponent(FixCostPerVehicle.COMPONENT_ID);

        assertEquals(1, calc.getComponentCount());
        assertEquals(true, calc.findComponent(UnassignedJobs.COMPONENT_ID).isPresent());
        assertEquals(false, res.isPresent());
    }

    @Test
    public void whenGettingTheWeightOfRegisteredComponent_theDefaultIsOne() {
        ModularSolutionCostCalculator calc = new ModularSolutionCostCalculator();
        FixCostPerVehicle fix = new FixCostPerVehicle();
        calc.addComponent(fix);
        Optional<Double> res = calc.getWeight(FixCostPerVehicle.COMPONENT_ID);

        assertTrue(res.isPresent());
        assertEquals(1d, res.get(), 0d);
    }

    @Test
    public void whenGettingTheWeightOfUnregisteredComponent_theValueIsEmpty() {
        ModularSolutionCostCalculator calc = new ModularSolutionCostCalculator();
        FixCostPerVehicle fix = new FixCostPerVehicle();
        calc.addComponent(fix);
        Optional<Double> res = calc.getWeight(UnassignedJobs.COMPONENT_ID);

        assertFalse(res.isPresent());
    }

    @Test
    public void whenSettingTheWeightOfAComponentWhenRegistered_theWeightIsSet() {
        ModularSolutionCostCalculator calc = new ModularSolutionCostCalculator();
        FixCostPerVehicle fix = new FixCostPerVehicle();
        calc.addComponent(fix, 2d);
        Optional<Double> res = calc.getWeight(FixCostPerVehicle.COMPONENT_ID);

        assertTrue(res.isPresent());
        assertEquals(2d, res.get(), 0d);
    }

    @Test
    public void whenSettingTheWeightOfAComponentLater_theWeightIsSet() {
        ModularSolutionCostCalculator calc = new ModularSolutionCostCalculator();
        FixCostPerVehicle fix = new FixCostPerVehicle();
        calc.addComponent(fix);
        calc.changeComponentWeight(FixCostPerVehicle.COMPONENT_ID, 2d);
        Optional<Double> res = calc.getWeight(FixCostPerVehicle.COMPONENT_ID);

        assertTrue(res.isPresent());
        assertEquals(2d, res.get(), 0d);
    }

    @Test(expected = IllegalStateException.class)
    public void whenCallingCalculatorFunctionsBeforeInitialization_itThrowsException() {
        ModularSolutionCostCalculator calc = new ModularSolutionCostCalculator();
        FixCostPerVehicle fix = new FixCostPerVehicle();
        calc.addComponent(fix);
        calc.calculate(mock(VehicleRoutingProblemSolution.class));
    }

    @Test(expected = IllegalStateException.class)
    public void whenAddingComponentAfterInitialization_itThrowsException() {
        ModularSolutionCostCalculator calc = new ModularSolutionCostCalculator();
        FixCostPerVehicle fix = new FixCostPerVehicle();
        UnassignedJobs unassigned = new UnassignedJobs();
        calc.addComponent(fix);
        calc.beforeRun(mock(VehicleRoutingProblem.class), 0d);
        calc.addComponent(unassigned);
    }

}
