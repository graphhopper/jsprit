package com.graphhopper.jsprit.core.util;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

import java.util.Collection;

import org.junit.Before;
import org.junit.Test;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.driver.Driver;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;

public class MultiVehicleCostMatrixTest {

	private MultiVehicleCostMatrix		costMatrix;
	private final double				delta				= 10e-8 , departureTime = 0;
	private final Driver				driver				= null;
	private final Location				zerothLoc			= Location.newInstance(0) , firstLoc = Location.newInstance(1);
	private final Vehicle				bike				= VehicleImpl.Builder.newInstance("vehicle1")
			.setType(VehicleTypeImpl.Builder.newInstance("bike")
					.setCostPerTransportTime(1)
					.build())
			.setStartLocation(zerothLoc)
			.build();
	private final Vehicle				van					= VehicleImpl.Builder.newInstance("vehicle2")
			.setType(VehicleTypeImpl.Builder.newInstance("van")
					.setCostPerTransportTime(1)
					.build())
			.setStartLocation(zerothLoc)
			.build();
	private final Collection<String>	vehicleTypes		= asList("bike", "van");
	private final String				defaultVehicleType	= "bike";

	@Before
	public void setUp() throws Exception {

		MultiVehicleCostMatrix.Builder costMatrixBuilder = MultiVehicleCostMatrix.Builder.newInstance(2, false, vehicleTypes, defaultVehicleType);
		costMatrixBuilder.addTransportationDistance(0, 0, "bike", 0);
		costMatrixBuilder.addTransportationDistance(0, 1, "bike", 4.2);
		costMatrixBuilder.addTransportationDistance(1, 0, "bike", 3.6);
		costMatrixBuilder.addTransportationDistance(1, 1, "bike", 0);

		costMatrixBuilder.addTransportationDistance(0, 0, "van", 0);
		costMatrixBuilder.addTransportationDistance(0, 1, "van", 5.2);
		costMatrixBuilder.addTransportationDistance(1, 0, "van", 4.5);
		costMatrixBuilder.addTransportationDistance(1, 1, "van", 0);

		costMatrixBuilder.addTransportationTime(0, 0, "bike", 0);
		costMatrixBuilder.addTransportationTime(0, 1, "bike", 12.);
		costMatrixBuilder.addTransportationTime(1, 0, "bike", 22.6);
		costMatrixBuilder.addTransportationTime(1, 1, "bike", 0);

		costMatrixBuilder.addTransportationTime(0, 0, "van", 0);
		costMatrixBuilder.addTransportationTime(0, 1, "van", 15.266);
		costMatrixBuilder.addTransportationTime(1, 0, "van", 25.123);
		costMatrixBuilder.addTransportationTime(1, 1, "van", 0);

		this.costMatrix = costMatrixBuilder.build();
	}

	@Test
	public void testGetTransportCost() {
		assertEquals(costMatrix.getTransportCost(zerothLoc, zerothLoc, departureTime, driver, bike), 0, delta);
		assertEquals(costMatrix.getTransportCost(zerothLoc, firstLoc, departureTime, driver, bike), 16.2, delta);// 16.2 = 4.2 + 12
		assertEquals(costMatrix.getTransportCost(firstLoc, zerothLoc, departureTime, driver, bike), 26.2, delta);//26.2 = 3.6 + 22.6
		assertEquals(costMatrix.getTransportCost(firstLoc, firstLoc, departureTime, driver, bike), 0, delta);

		assertEquals(costMatrix.getTransportCost(zerothLoc, zerothLoc, departureTime, driver, van), 0, delta);
		assertEquals(costMatrix.getTransportCost(zerothLoc, firstLoc, departureTime, driver, van), 20.466, delta);//20.466 = 5.2 + 15.266
		assertEquals(costMatrix.getTransportCost(firstLoc, zerothLoc, departureTime, driver, van), 29.623, delta);//29.623 = 4.5 + 25.123
		assertEquals(costMatrix.getTransportCost(firstLoc, firstLoc, departureTime, driver, van), 0, delta);
	}

	@Test
	public void testGetTransportTime() {

		assertEquals(costMatrix.getTransportTime(zerothLoc, zerothLoc, departureTime, driver, bike), 0, delta);
		assertEquals(costMatrix.getTransportTime(zerothLoc, firstLoc, departureTime, driver, bike), 12, delta);
		assertEquals(costMatrix.getTransportTime(firstLoc, zerothLoc, departureTime, driver, bike), 22.6, delta);
		assertEquals(costMatrix.getTransportTime(firstLoc, firstLoc, departureTime, driver, bike), 0, delta);

		assertEquals(costMatrix.getTransportTime(zerothLoc, zerothLoc, departureTime, driver, van), 0, delta);
		assertEquals(costMatrix.getTransportTime(zerothLoc, firstLoc, departureTime, driver, van), 15.266, delta);
		assertEquals(costMatrix.getTransportTime(firstLoc, zerothLoc, departureTime, driver, van), 25.123, delta);
		assertEquals(costMatrix.getTransportTime(firstLoc, firstLoc, departureTime, driver, van), 0, delta);

	}

	@Test
	public void testGetDistanceBetween() {
		assertEquals(costMatrix.getDistanceBetween(0, 0, "bike"), 0, delta);
		assertEquals(costMatrix.getDistanceBetween(0, 1, "bike"), 4.2, delta);
		assertEquals(costMatrix.getDistanceBetween(1, 0, "bike"), 3.6, delta);
		assertEquals(costMatrix.getDistanceBetween(1, 1, "bike"), 0, delta);

		assertEquals(costMatrix.getDistanceBetween(0, 0, "van"), 0, delta);
		assertEquals(costMatrix.getDistanceBetween(0, 1, "van"), 5.2, delta);
		assertEquals(costMatrix.getDistanceBetween(1, 0, "van"), 4.5, delta);
		assertEquals(costMatrix.getDistanceBetween(1, 1, "van"), 0, delta);
	}

	@Test
	public void testGetTimeBetween() {
		assertEquals(costMatrix.getTransportTimeBetween(0, 0, "bike"), 0, delta);
		assertEquals(costMatrix.getTransportTimeBetween(0, 1, "bike"), 12, delta);
		assertEquals(costMatrix.getTransportTimeBetween(1, 0, "bike"), 22.6, delta);
		assertEquals(costMatrix.getTransportTimeBetween(1, 1, "bike"), 0, delta);

		assertEquals(costMatrix.getTransportTimeBetween(0, 0, "van"), 0, delta);
		assertEquals(costMatrix.getTransportTimeBetween(0, 1, "van"), 15.266, delta);
		assertEquals(costMatrix.getTransportTimeBetween(1, 0, "van"), 25.123, delta);
		assertEquals(costMatrix.getTransportTimeBetween(1, 1, "van"), 0, delta);
	}

}
