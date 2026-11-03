package com.graphhopper.jsprit.core.algorithm.recreate;

import com.graphhopper.jsprit.core.algorithm.state.StateManager;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.constraint.ConstraintManager;
import com.graphhopper.jsprit.core.problem.cost.AbstractForwardVehicleRoutingTransportCosts;
import com.graphhopper.jsprit.core.problem.driver.Driver;
import com.graphhopper.jsprit.core.problem.driver.DriverImpl;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.job.Shipment;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * A job with several time windows: the insertion data names the window the calculator chose, so a caller that
 * builds its own activity from the result can set that window instead of the job's first one.
 */
public class SelectedTimeWindowTest {

    /** Manhattan distance on "x,y" location ids, one time unit per distance unit. */
    private static final AbstractForwardVehicleRoutingTransportCosts COSTS = new AbstractForwardVehicleRoutingTransportCosts() {
        private double d(Location from, Location to) {
            String[] a = from.getId().split(","), b = to.getId().split(",");
            return Math.abs(Double.parseDouble(a[0]) - Double.parseDouble(b[0])) + Math.abs(Double.parseDouble(a[1]) - Double.parseDouble(b[1]));
        }

        @Override
        public double getDistance(Location from, Location to, double departureTime, Vehicle vehicle) { return d(from, to); }

        @Override
        public double getTransportTime(Location from, Location to, double departureTime, Driver driver, Vehicle vehicle) { return d(from, to); }

        @Override
        public double getTransportCost(Location from, Location to, double departureTime, Driver driver, Vehicle vehicle) { return d(from, to); }
    };

    private static final VehicleImpl VEHICLE = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance("0,0")).setEarliestStart(0).setLatestArrival(1000)
        .setType(VehicleTypeImpl.Builder.newInstance("t").addCapacityDimension(0, 10).build()).build();

    /** The job's location is 10 time units from the vehicle, so a window that closes at 5 cannot be met. */
    private static final TimeWindow CLOSED = TimeWindow.newInstance(0, 5), OPEN = TimeWindow.newInstance(50, 60);

    private static class Setup {
        final VehicleRoutingProblem vrp;
        final StateManager states;
        final ConstraintManager constraints;

        Setup(com.graphhopper.jsprit.core.problem.job.Job job) {
            vrp = VehicleRoutingProblem.Builder.newInstance().addJob(job).addVehicle(VEHICLE).setRoutingCost(COSTS).build();
            states = new StateManager(vrp);
            states.updateLoadStates();
            states.updateTimeWindowStates();
            constraints = new ConstraintManager(vrp, states);
            constraints.addLoadConstraint();
            constraints.addTimeWindowConstraint();
        }

        VehicleRoute emptyRoute() {
            VehicleRoute route = VehicleRoute.Builder.newInstance(VEHICLE, DriverImpl.noDriver()).build();
            states.informInsertionStarts(Collections.singletonList(route), null);
            return route;
        }

        ServiceInsertionCalculator services() {
            return new ServiceInsertionCalculator(COSTS, vrp.getActivityCosts(), new LocalActivityInsertionCostsCalculator(COSTS, vrp.getActivityCosts(), states), constraints, vrp::copyAndGetActivities);
        }

        ShipmentInsertionCalculator shipments() {
            return new ShipmentInsertionCalculator(COSTS, vrp.getActivityCosts(), new LocalActivityInsertionCostsCalculator(COSTS, vrp.getActivityCosts(), states), constraints, vrp::copyAndGetActivities);
        }
    }

    @Test
    public void aServiceWhoseFirstWindowIsClosedGetsItsSecondWindowSelected() {
        Service service = Service.Builder.newInstance("s").addSizeDimension(0, 1).setLocation(Location.newInstance("0,10")).addTimeWindow(CLOSED).addTimeWindow(OPEN).build();
        Setup setup = new Setup(service);
        InsertionData data = setup.services().getInsertionData(setup.emptyRoute(), service, VEHICLE, 0, DriverImpl.noDriver(), Double.MAX_VALUE);
        assertTrue(data.isFound());
        assertEquals(OPEN, data.getSelectedTimeWindow());
        assertNull(data.getSelectedPickupTimeWindow());
    }

    @Test
    public void everyFeasiblePositionOfAServiceNamesItsWindow() {
        Service service = Service.Builder.newInstance("s").addSizeDimension(0, 1).setLocation(Location.newInstance("0,10")).addTimeWindow(CLOSED).addTimeWindow(OPEN).build();
        Setup setup = new Setup(service);
        List<InsertionData> positions = setup.services().getAllInsertionPositions(setup.emptyRoute(), service, VEHICLE, 0, DriverImpl.noDriver());
        assertEquals(1, positions.size());
        assertEquals(OPEN, positions.get(0).getSelectedTimeWindow());
    }

    @Test
    public void aShipmentNamesThePickupAndTheDeliveryWindow() {
        Shipment shipment = Shipment.Builder.newInstance("sh").addSizeDimension(0, 1)
            .setPickupLocation(Location.newInstance("0,10")).addPickupTimeWindow(CLOSED).addPickupTimeWindow(OPEN)
            .setDeliveryLocation(Location.newInstance("0,20")).addDeliveryTimeWindow(TimeWindow.newInstance(0, 30)).addDeliveryTimeWindow(TimeWindow.newInstance(70, 90)).build();
        Setup setup = new Setup(shipment);
        InsertionData data = setup.shipments().getInsertionData(setup.emptyRoute(), shipment, VEHICLE, 0, DriverImpl.noDriver(), Double.MAX_VALUE);
        assertTrue(data.isFound());
        assertEquals(OPEN, data.getSelectedPickupTimeWindow());
        // pickup at 50, delivery reachable at 60: only the late delivery window works
        assertEquals(TimeWindow.newInstance(70, 90), data.getSelectedTimeWindow());
    }

    @Test
    public void nothingIsSelectedWhenNoInsertionIsFound() {
        Service service = Service.Builder.newInstance("s").addSizeDimension(0, 1).setLocation(Location.newInstance("0,10")).addTimeWindow(CLOSED).build();
        Setup setup = new Setup(service);
        InsertionData data = setup.services().getInsertionData(setup.emptyRoute(), service, VEHICLE, 0, DriverImpl.noDriver(), Double.MAX_VALUE);
        assertTrue(!data.isFound());
        assertNull(data.getSelectedTimeWindow());
    }
}
