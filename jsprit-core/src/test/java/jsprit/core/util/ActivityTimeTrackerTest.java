package jsprit.core.util;

import jsprit.core.problem.Location;
import jsprit.core.problem.cost.ForwardTransportTime;
import jsprit.core.problem.driver.Driver;
import jsprit.core.problem.job.Service;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.solution.route.activity.TourActivity;
import jsprit.core.problem.vehicle.Vehicle;
import jsprit.core.problem.vehicle.VehicleImpl;
import junit.framework.Assert;
import org.junit.Test;

public class ActivityTimeTrackerTest {

	protected void testActivityTimeTrackerForVehicleOperationTimeFactor(final double transportTime, double serviceTime, Float serviceTimeFactor, double expectedActEndTime) {

		Location startLocation = Location.newInstance("1");
		Vehicle vehicle = VehicleImpl.Builder.newInstance("1")
				.setStartLocation(startLocation)
				.setEarliestStart(0)
				.setOperationTimeFactor(serviceTimeFactor)
				.build();

		Location serviceLocation = Location.newInstance("2");
		Service service = Service.Builder.newInstance("1")
				.setServiceTime(serviceTime)
				.setLocation(serviceLocation)
				.build();

		VehicleRoute vehicleRoute = VehicleRoute.Builder.newInstance(vehicle).addService(service).build();
		TourActivity startActivity = vehicleRoute.getStart();
		TourActivity serviceActivity = vehicleRoute.getActivities().get(0);

		ForwardTransportTime forwardTransportTime = new ForwardTransportTime() {
			@Override
			public double getTransportTime(Location from, Location to, double departureTime, Driver driver, Vehicle vehicle) {
				if (from.equals(to)) return 0;
				return transportTime;
			}
		};

		ActivityTimeTracker activityTimeTracker = new ActivityTimeTracker(forwardTransportTime);

		activityTimeTracker.begin(vehicleRoute);
		activityTimeTracker.visit(startActivity);
		activityTimeTracker.visit(serviceActivity);

		Assert.assertEquals(expectedActEndTime, activityTimeTracker.getActEndTime(), 0.001);
	}

	@Test
	public void whenVehicleDoNotSpecifyOperationTimeFactor_itShouldNotChangeTheActEndTime() {
		testActivityTimeTrackerForVehicleOperationTimeFactor(1, 1, null, 2);
	}

	@Test
	public void whenVehicleSpecifyNeutralOperationTimeFactor_itShouldNotChangeTheActEndTime() {
		testActivityTimeTrackerForVehicleOperationTimeFactor(1, 1, 1f, 2);
	}

	@Test
	public void whenVehicleSpecifyHighOperationTimeFactor_itShouldChangeTheActEndTimeAccordingly() {
		testActivityTimeTrackerForVehicleOperationTimeFactor(1, 1, 1.5f, 2.5d);
	}

	@Test
	public void whenVehicleSpecifyLowOperationTimeFactor_itShouldChangeTheActEndTimeAccordingly() {
		testActivityTimeTrackerForVehicleOperationTimeFactor(1, 1, 0.5f, 1.5d);
	}

	@Test
	public void whenVehicleSpecifyOperationTimeFactorButJobOperationTimeIsZero_itShouldNotChangeTheActEndTime() {
		testActivityTimeTrackerForVehicleOperationTimeFactor(1, 0, 2f, 1d);
	}
}
