/*******************************************************************************
 * Copyright (C) 2014  Stefan Schroeder
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package jsprit.core.util;

import jsprit.core.problem.Location;
import jsprit.core.problem.cost.AbstractForwardVehicleRoutingTransportCosts;
import jsprit.core.problem.cost.TransportDistance;
import jsprit.core.problem.driver.Driver;
import jsprit.core.problem.vehicle.Vehicle;
import jsprit.core.problem.vehicle.VehicleTypeImpl.VehicleCostParams;


/**
 * CostMatrix that allows pre-compiled time and distance-matrices to be considered as {@link jsprit.core.problem.cost.VehicleRoutingTransportCosts}
 * in the {@link jsprit.core.problem.VehicleRoutingProblem}.
 * <p>Note that you can also use it with distance matrix only (or time matrix).
 * @author schroeder
 *
 */
public class FastVehicleRoutingTransportCostsMatrix extends AbstractForwardVehicleRoutingTransportCosts implements TransportDistance {

	/**
	 * Builder that builds the matrix.
	 *
	 * @author schroeder
	 *
	 */
	public static class Builder {

		private boolean isSymmetric;

        private double[][][] matrix;

		/**
		 * Creates a new builder returning the matrix-builder.
		 * <p>If you want to consider symmetric matrices, set isSymmetric to true.
		 * @param isSymmetric true if matrix is symmetric, false otherwise
		 * @return builder
		 */
		public static Builder newInstance(int noLocations, boolean isSymmetric){
			return new Builder(noLocations, isSymmetric);
		}

		private Builder(int noLocations, boolean isSymmetric){
			this.isSymmetric = isSymmetric;
            matrix = new double[noLocations][noLocations][2];
		}

		/**
		 * Adds a transport-distance for a particular relation.
		 * @param fromIndex from location index
		 * @param toIndex to location index
		 * @param distance the distance to be added
		 * @return builder
		 */
		public Builder addTransportDistance(int fromIndex, int toIndex, double distance){
            add(fromIndex,toIndex,0,distance);
            return this;
		}

        private void add(int fromIndex, int toIndex, int indicatorIndex, double distance){
            if(isSymmetric){
                if(fromIndex < toIndex) matrix[fromIndex][toIndex][indicatorIndex] = distance;
                else matrix[toIndex][fromIndex][indicatorIndex] = distance;
            }
            else matrix[fromIndex][toIndex][indicatorIndex] = distance;
        }

		/**
		 * Adds transport-time for a particular relation.
		 * @param fromIndex from location index
		 * @param toIndex to location index
		 * @param time the time to be added
		 * @return builder
		 */
		public Builder addTransportTime(int fromIndex, int toIndex, double time){
            add(fromIndex,toIndex,1,time);
			return this;
		}

		/**
		 * Builds the matrix.
		 * @return matrix
		 */
		public FastVehicleRoutingTransportCostsMatrix build(){
			return new FastVehicleRoutingTransportCostsMatrix(this);
		}


	}

    private final boolean isSymmetric;

    private final double[][][] matrix;

	private FastVehicleRoutingTransportCostsMatrix(Builder builder){
		this.isSymmetric = builder.isSymmetric;
		matrix = builder.matrix;
	}


	@Override
	public double getTransportTime(Location from, Location to, double departureTime, Driver driver, Vehicle vehicle) {
        if(from.getIndex() < 0 || to.getIndex() < 0) throw new IllegalArgumentException("index of from " + from + " to " + to + " < 0 ");
        int timeIndex = 1;
        return get(from.getIndex(),to.getIndex(), timeIndex);
	}

    private double get(int from, int to, int indicatorIndex){
        double value;
        if(isSymmetric){
            if(from < to) value = matrix[from][to][indicatorIndex];
            else value = matrix[to][from][indicatorIndex];
        }
        else{
            value = matrix[from][to][indicatorIndex];
        }
        return value;
    }

	/**
	 * Returns the distance from to to.
	 * 
	 * @param fromIndex from location index
	 * @param toIndex to location index
	 * @return the distance
	 */
	public double getDistance(int fromIndex, int toIndex) {
        int distanceIndex = 0;
        return get(fromIndex, toIndex, distanceIndex);
	}

	@Override
	public double getDistance(Location from, Location to) {
		return getDistance(from.getIndex(),to.getIndex());
	}

	@Override
	public double getTransportCost(Location from, Location to, double departureTime, Driver driver, Vehicle vehicle) {
        if(from.getIndex() < 0 || to.getIndex() < 0) throw new IllegalArgumentException("index of from " + from + " to " + to + " < 0 ");
        if(vehicle == null) return getDistance(from.getIndex(), to.getIndex());
		VehicleCostParams costParams = vehicle.getType().getVehicleCostParams();
		return costParams.perDistanceUnit * getDistance(from.getIndex(), to.getIndex()) + costParams.perTimeUnit * getTransportTime(from, to, departureTime, driver, vehicle);
	}
	
}
