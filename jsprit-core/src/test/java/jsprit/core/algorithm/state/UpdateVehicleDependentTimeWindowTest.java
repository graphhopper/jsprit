package jsprit.core.algorithm.state;

import jsprit.core.problem.AbstractActivity;
import jsprit.core.problem.JobActivityFactory;
import jsprit.core.problem.Location;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import jsprit.core.problem.job.Job;
import jsprit.core.problem.job.Service;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.vehicle.FiniteFleetManagerFactory;
import jsprit.core.problem.vehicle.Vehicle;
import jsprit.core.problem.vehicle.VehicleFleetManager;
import jsprit.core.problem.vehicle.VehicleImpl;
import jsprit.core.util.CostFactory;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

/**
 * unit tests to test vehicle dependent time window updater
 */
public class UpdateVehicleDependentTimeWindowTest {

    private StateManager stateManager;

    private VehicleRoute route;

    private VehicleImpl vehicle;

    private VehicleImpl vehicle2;

    private VehicleImpl vehicle3;

    private VehicleImpl equivalentOf3;

    private VehicleRoutingTransportCosts routingCosts;

    private VehicleFleetManager fleetManager;

    private VehicleRoutingProblem vrp;

    @Before
    public void doBefore() {
        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();

        routingCosts = CostFactory.createEuclideanCosts();
        vrpBuilder.setRoutingCost(routingCosts);

        vehicle = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance("0,0")).setEarliestStart(0.).setLatestArrival(100.).build();

        vehicle2 = VehicleImpl.Builder.newInstance("v2").setStartLocation(Location.newInstance("0,0")).setEarliestStart(0.).setLatestArrival(60.).build();

        vehicle3 = VehicleImpl.Builder.newInstance("v3").setStartLocation(Location.newInstance("40,0")).setEarliestStart(0.).setLatestArrival(100.).build();

        equivalentOf3 = VehicleImpl.Builder.newInstance("v4").setStartLocation(Location.newInstance("40,0")).setEarliestStart(0.).setLatestArrival(100.).build();

        vrpBuilder.addVehicle(vehicle).addVehicle(vehicle2).addVehicle(vehicle3).addVehicle(equivalentOf3);

        Collection<Vehicle> vehicles = new ArrayList<Vehicle>();
        vehicles.add(vehicle);
        vehicles.add(vehicle2);
        vehicles.add(vehicle3);


        fleetManager = new FiniteFleetManagerFactory(vehicles).createFleetManager();

        Service service = Service.Builder.newInstance("s1").setLocation(Location.newInstance("10,0")).build();
        Service service2 = Service.Builder.newInstance("s2").setLocation(Location.newInstance("20,0")).build();
        Service service3 = Service.Builder.newInstance("s3").setLocation(Location.newInstance("30,0")).build();

        vrpBuilder.addJob(service).addJob(service2).addJob(service3);
        vrp = vrpBuilder.build();

        route = VehicleRoute.Builder.newInstance(vehicle).setJobActivityFactory(new JobActivityFactory() {
            @Override
            public List<AbstractActivity> createActivities(Job job) {
                return vrp.copyAndGetActivities(job);
            }
        }).addService(service).addService(service2).addService(service3).build();


        stateManager = new StateManager(vrp);
        UpdateVehicleDependentPracticalTimeWindows updater = new UpdateVehicleDependentPracticalTimeWindows(stateManager, routingCosts);
        updater.setVehiclesToUpdate(new UpdateVehicleDependentPracticalTimeWindows.VehiclesToUpdate() {

            @Override
            public Collection<Vehicle> get(VehicleRoute route) {
                Collection<Vehicle> vehicles = new ArrayList<Vehicle>();
                vehicles.add(route.getVehicle());
                vehicles.addAll(fleetManager.getAvailableVehicles(route.getVehicle()));
                return vehicles;
            }

        });
        stateManager.addStateUpdater(updater);
        stateManager.informInsertionStarts(Arrays.asList(route), Collections.<Job>emptyList());
    }

    @Test
    public void whenSwitchIsNotAllowed_itShouldCalOnlyStatesOfCurrentVehicle() {
        stateManager = new StateManager(vrp);
        UpdateVehicleDependentPracticalTimeWindows updater = new UpdateVehicleDependentPracticalTimeWindows(stateManager, routingCosts);

        stateManager.addStateUpdater(updater);
        stateManager.informInsertionStarts(Arrays.asList(route), Collections.<Job>emptyList());
        assertTrue(stateManager.hasActivityState(route.getActivities().get(0), vehicle, InternalStates.LATEST_OPERATION_START_TIME));
        assertFalse(stateManager.hasActivityState(route.getActivities().get(0), vehicle2, InternalStates.LATEST_OPERATION_START_TIME));
    }

    @Test
    public void stateManagerShouldHaveMemorizedCorrectLatestEndOfAct3() {
        assertEquals(70., stateManager.getActivityState(route.getActivities().get(2), vehicle,
            InternalStates.LATEST_OPERATION_START_TIME, Double.class), 0.01);

    }

    @Test
    public void stateManagerShouldHaveMemorizedCorrectLatestEndOfAct3_v2() {
        assertEquals(70., stateManager.getActivityState(route.getActivities().get(2), vehicle,
            InternalStates.LATEST_OPERATION_START_TIME, Double.class), 0.01);
    }

    @Test
    public void stateManagerShouldHaveMemorizedCorrectLatestEndOfAct3WithVehicle2() {
        assertEquals(30., stateManager.getActivityState(route.getActivities().get(2), vehicle2,
            InternalStates.LATEST_OPERATION_START_TIME, Double.class), 0.01);
    }

    @Test
    public void stateManagerShouldHaveMemorizedCorrectLatestEndOfAct3WithVehicle3() {
        assertEquals(90., stateManager.getActivityState(route.getActivities().get(2), vehicle3,
            InternalStates.LATEST_OPERATION_START_TIME, Double.class), 0.01);
    }

    @Test
    public void stateManagerShouldHaveMemorizedCorrectLatestEndOfAct2() {
        assertEquals(60., stateManager.getActivityState(route.getActivities().get(1), vehicle,
            InternalStates.LATEST_OPERATION_START_TIME, Double.class), 0.01);
    }

    @Test
    public void stateManagerShouldHaveMemorizedCorrectLatestEndOfAct2_v2() {
        assertEquals(60., stateManager.getActivityState(route.getActivities().get(1), vehicle,
            InternalStates.LATEST_OPERATION_START_TIME, Double.class), 0.01);
    }

    @Test
    public void stateManagerShouldHaveMemorizedCorrectLatestEndOfAct2WithVehicle2() {
        assertEquals(20., stateManager.getActivityState(route.getActivities().get(1), vehicle2,
            InternalStates.LATEST_OPERATION_START_TIME, Double.class), 0.01);
    }

    @Test
    public void stateManagerShouldHaveMemorizedCorrectLatestEndOfAct2WithVehicle3() {
        assertEquals(80., stateManager.getActivityState(route.getActivities().get(1), vehicle3,
            InternalStates.LATEST_OPERATION_START_TIME, Double.class), 0.01);
    }

    @Test
    public void stateManagerShouldHaveMemorizedCorrectLatestEndOfAct2WithEquivalentOfVehicle3() {
        assertEquals(80., stateManager.getActivityState(route.getActivities().get(1), equivalentOf3,
            InternalStates.LATEST_OPERATION_START_TIME, Double.class), 0.01);
    }

    @Test
    public void stateManagerShouldHaveMemorizedCorrectLatestEndOfAct1WithVehicle2() {
        assertEquals(10., stateManager.getActivityState(route.getActivities().get(0), vehicle2,
            InternalStates.LATEST_OPERATION_START_TIME, Double.class), 0.01);
    }

    @Test
    public void stateManagerShouldHaveMemorizedCorrectLatestEndOfAct1WithVehicle3() {
        assertEquals(70., stateManager.getActivityState(route.getActivities().get(0), vehicle3,
            InternalStates.LATEST_OPERATION_START_TIME, Double.class), 0.01);
    }


}
