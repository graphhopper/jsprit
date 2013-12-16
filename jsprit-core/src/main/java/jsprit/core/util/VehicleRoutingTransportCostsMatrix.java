/*******************************************************************************
 * Copyright (C) 2013  Stefan Schroeder
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

import java.util.HashMap;
import java.util.Map;

import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.cost.AbstractForwardVehicleRoutingTransportCosts;
import jsprit.core.problem.driver.Driver;
import jsprit.core.problem.vehicle.Vehicle;
import jsprit.core.problem.vehicle.VehicleTypeImpl.VehicleCostParams;

import org.apache.log4j.Logger;


/**
 * CostMatrix that allows pre-compiled time and distance-matrices to be considered as {@link VehicleRoutingRoutingCosts}
 * in the {@link VehicleRoutingProblem}.
 * <p>Note that you can also use it with distance matrix only (or time matrix). But ones
 * you set a particular distance, this expects distance-entries for all relations. This counts also 
 * for a particular time. If the method getTransportCosts(...) is then invoked for a relation, where no distance can be found, an 
 * IllegalStateException will be thrown. Thus if you want to only use distances only, do not use addTransportTime(...).
 * @author schroeder
 *
 */
public class VehicleRoutingTransportCostsMatrix extends AbstractForwardVehicleRoutingTransportCosts {

	static class RelationKey {
		
		static RelationKey newKey(String from, String to){
			int fromInt = Integer.parseInt(from);
			int toInt = Integer.parseInt(to);
			if(fromInt < toInt){
				return new RelationKey(from, to);
			}
			else {
				return new RelationKey(to, from);
			}
		}
		
		final String from;
		final String to;
		
		public RelationKey(String from, String to) {
			super();
			this.from = from;
			this.to = to;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((from == null) ? 0 : from.hashCode());
			result = prime * result + ((to == null) ? 0 : to.hashCode());
			return result;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			RelationKey other = (RelationKey) obj;
			if (from == null) {
				if (other.from != null)
					return false;
			} else if (!from.equals(other.from))
				return false;
			if (to == null) {
				if (other.to != null)
					return false;
			} else if (!to.equals(other.to))
				return false;
			return true;
		}
	}

	
	/**
	 * Builder that builds the matrix.
	 * 
	 * @author schroeder
	 *
	 */
	public static class Builder {
		private static Logger log = Logger.getLogger(Builder.class);
		
		private boolean isSymmetric;
		
		private Map<RelationKey,Double> distances = new HashMap<RelationKey, Double>();
		
		private Map<RelationKey,Double> times = new HashMap<RelationKey, Double>();
		
		private boolean distancesSet = false;
		
		private boolean timesSet = false;
		
		/**
		 * Creates a new builder returning the matrix-builder.
		 * <p>If you want to consider symmetric matrices, set isSymmetric to true.
		 * @param isSymmetric
		 * @return
		 */
		public static Builder newInstance(boolean isSymmetric){
			return new Builder(isSymmetric);
		}
		
		private Builder(boolean isSymmetric){
			this.isSymmetric = isSymmetric;
		}
		
		/**
		 * Adds a transport-distance for a particular relation.
		 * @param from
		 * @param to
		 * @param distance
		 * @return
		 */
		public Builder addTransportDistance(String from, String to, double distance){
			RelationKey key = RelationKey.newKey(from, to);
			if(!distancesSet) distancesSet = true;
			if(distances.containsKey(key)){
				log.warn("distance from " + from + " to " + to + " already exists. This overrides distance.");
			}
			distances.put(key, distance);
			return this;
		}
		
		/**
		 * Adds transport-time for a particular relation.
		 * @param from
		 * @param to
		 * @param time
		 * @return
		 */
		public Builder addTransportTime(String from, String to, double time){
			RelationKey key = RelationKey.newKey(from, to);
			if(!timesSet) timesSet = true;
			if(times.containsKey(key)){
				log.warn("transport-time from " + from + " to " + to + " already exists. This overrides distance.");
			}
			times.put(key, time);
			return this;
		}
		
		/**
		 * Builds the matrix.
		 * @return
		 */
		public VehicleRoutingTransportCostsMatrix build(){
			return new VehicleRoutingTransportCostsMatrix(this);
		}
	}
	
	private Map<RelationKey,Double> distances = new HashMap<RelationKey, Double>();
	
	private Map<RelationKey,Double> times = new HashMap<RelationKey, Double>();
	
	private boolean isSymmetric;
	
	private boolean timesSet;
	
	private boolean distancesSet;
	
	private VehicleRoutingTransportCostsMatrix(Builder builder){
		this.isSymmetric = builder.isSymmetric;
		distances.putAll(builder.distances);
		times.putAll(builder.times);
		timesSet = builder.timesSet;
		distancesSet = builder.distancesSet;
	}


	@Override
	public double getTransportTime(String fromId, String toId, double departureTime, Driver driver, Vehicle vehicle) {
		return getTime(fromId, toId);
	}


	private double getTime(String fromId, String toId) {
		if(fromId.equals(toId)) return 0.0;
		if(!timesSet) return 0.0;
		RelationKey key = RelationKey.newKey(fromId, toId);
		if(!isSymmetric){
			if(times.containsKey(key)) return times.get(key);
			else throw new IllegalStateException("time value for relation from " + fromId + " to " + toId + " does not exist");
		}
		else{
			Double time = times.get(key);
			if(time == null){
				time = times.get(RelationKey.newKey(toId, fromId));
			}
			if(time != null) return time;
			else throw new IllegalStateException("time value for relation from " + fromId + " to " + toId + " does not exist");
		}
	}

	private double getDistance(String fromId, String toId) {
		if(fromId.equals(toId)) return 0.0;
		if(!distancesSet) return 0.0;
		RelationKey key = RelationKey.newKey(fromId, toId);
		if(!isSymmetric){
			if(distances.containsKey(key)) return distances.get(key);
			else throw new IllegalStateException("distance value for relation from " + fromId + " to " + toId + " does not exist");
		}
		else{
			Double time = distances.get(key);
			if(time == null){
				time = distances.get(RelationKey.newKey(toId, fromId));
			}
			if(time != null) return time;
			else throw new IllegalStateException("distance value for relation from " + fromId + " to " + toId + " does not exist");
		}
	}

	@Override
	public double getTransportCost(String fromId, String toId, double departureTime, Driver driver, Vehicle vehicle) {
		if(vehicle == null) return getDistance(fromId, toId);
		VehicleCostParams costParams = vehicle.getType().getVehicleCostParams();
		return costParams.perDistanceUnit*getDistance(fromId, toId) + costParams.perTimeUnit*getTime(fromId, toId);
	}
	
}
