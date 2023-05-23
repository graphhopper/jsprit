package com.graphhopper.jsprit.core.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.cost.AbstractForwardVehicleRoutingTransportCosts;
import com.graphhopper.jsprit.core.problem.driver.Driver;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;

/**
 * @author Shiv Krishna Jaiswal
 */

public class MultiVehicleCostMatrix extends AbstractForwardVehicleRoutingTransportCosts {

	/*
	 * First index respresents index of from_location
	 * Second index represents index of to_location
	 * Third index represents distance and time: 0th entry is for Distance and 1st entry is for time.
	 * Fourth index represents vehicleType. Each vehicleTypeId is assigned an index.
	 */

	private final double[][][][] costMatrix;

	/*
	 * This Map is used to map vehicleTypeID to an index.
	 * There is one to one relation between them.
	 */

	private final Map<String, Integer>	vehicleTypeToIndex;
	private final int					defaultIndex;

	private MultiVehicleCostMatrix(double[][][][] costMatrix, Map<String, Integer> vehicleTypeToIndex, int defaultIndex) {
		this.costMatrix = costMatrix;
		this.vehicleTypeToIndex = vehicleTypeToIndex;
		this.defaultIndex = defaultIndex;
	}

	/*
	 * This method give transportation cost between two location for a vehicle.
	 * Transportation cost is defined as :
	 * perDistanceCost * distanceBetweenLocation + perTransportTime * timeBetweenLocation
	 * Parameters, perDistanceCost or perTransportTime, are controlled by VehicleType.
	 * Cases where Vehicle is null, then distance between the two locations are returned for default vehicleType.
	 * eg.(DefaultScorer.java:94)
	 */
	@Override
	public double getTransportCost(Location from, Location to, double departureTime, Driver driver, Vehicle vehicle) {
		if (from.getIndex() < 0 || to.getIndex() < 0)
			throw new IllegalArgumentException("Index of location must not be less than zero");

		if (vehicle == null)
			return costMatrix[from.getIndex()][to.getIndex()][0][defaultIndex];

		VehicleTypeImpl.VehicleCostParams costParams = vehicle.getType().getVehicleCostParams();

		return costParams.perDistanceUnit * costMatrix[from.getIndex()][to.getIndex()][0][vehicleTypeToIndex.get(vehicle.getType().getTypeId()).intValue()]
				+ costParams.perTransportTimeUnit * getTransportTime(from, to, departureTime, driver, vehicle);
	}

	/*
	 * This method gives time to move between the two location by Vehicle.
	 */

	@Override
	public double getTransportTime(Location from, Location to, double departureTime, Driver driver, Vehicle vehicle) {
		if (from.getIndex() < 0 || to.getIndex() < 0)
			throw new IllegalArgumentException("Index of location must not be less than zero");

		return costMatrix[from.getIndex()][to.getIndex()][1][vehicle == null ? defaultIndex : vehicleTypeToIndex.get(vehicle.getType().getTypeId()).intValue()];
	}

	/*
	 * For convenience of retriving the distance and time taken between location,
	 * two methods are introduced below.
	 */

	public double getDistanceBetween(int from, int to, String vehicleType) {

		return costMatrix[from][to][0][vehicleTypeToIndex.get(vehicleType).intValue()];
	}

	public double getTransportTimeBetween(int from, int to, String vehicleType) {

		return costMatrix[from][to][1][vehicleTypeToIndex.get(vehicleType).intValue()];
	}

	public static class Builder {

		private final double[][][][]		costMatrix;
		private final Map<String, Integer>	vehicleTypeToIndex;
		private final boolean				isSymmetric;
		private final int					defaultIndex;

		private Builder(int location, boolean isSymmetric, Map<String, Integer> vehicleTypeIDToIndex, int defaultIndex) {
			this.costMatrix = new double[location][location][2][vehicleTypeIDToIndex.size()];
			this.isSymmetric = isSymmetric;
			this.defaultIndex = defaultIndex;
			this.vehicleTypeToIndex = vehicleTypeIDToIndex;

		}

		/*
		 * defaultVehicleTypeID is used to incorporate the case when Jsprit call
		 * method " public double getTransportCost(Location from, Location to, double departureTime, Driver driver,
		 * Vehicle vehicle)" with vehicle as null or any case similar. So in this case distance corresponding to default
		 * vehicleTypeId is used.
		 */

		public static Builder newInstance(int location, boolean isSymmetric, Collection<String> vehicleTypeIDs, String defaultVehicleID) {

			List<String> vehicleTypeIDsList = new ArrayList<>(new TreeSet<>(vehicleTypeIDs));
			Map<String, Integer> vehicleTypeIDToIndex = new HashMap<>(vehicleTypeIDsList.size());

			for (int i = 0; i < vehicleTypeIDsList.size(); i++)
				vehicleTypeIDToIndex.put(vehicleTypeIDsList.get(i), Integer.valueOf(i));

			return new Builder(location, isSymmetric, vehicleTypeIDToIndex, vehicleTypeIDToIndex.get(defaultVehicleID).intValue());

		}

		public Builder addTransportationDistance(int from, int to, String vehicleTypeID, double distance) {
			costMatrix[from][to][0][vehicleTypeToIndex.get(vehicleTypeID).intValue()] = distance;

			if (isSymmetric)
				costMatrix[to][from][0][vehicleTypeToIndex.get(vehicleTypeID).intValue()] = distance;

			return this;
		}

		public Builder addTransportationTime(int from, int to, String vehicleTypeID, double duration) {
			costMatrix[from][to][1][vehicleTypeToIndex.get(vehicleTypeID).intValue()] = duration;

			if (isSymmetric)
				costMatrix[to][from][1][vehicleTypeToIndex.get(vehicleTypeID).intValue()] = duration;

			return this;
		}

		public MultiVehicleCostMatrix build() {
			return new MultiVehicleCostMatrix(costMatrix, vehicleTypeToIndex, defaultIndex);
		}
	}

}
