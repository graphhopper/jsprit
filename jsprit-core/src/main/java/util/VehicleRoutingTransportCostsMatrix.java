package util;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import basics.costs.VehicleRoutingTransportCosts;
import basics.route.Driver;
import basics.route.Vehicle;
import basics.route.VehicleTypeImpl.VehicleCostParams;


public class VehicleRoutingTransportCostsMatrix implements VehicleRoutingTransportCosts {

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

	
	
	public static class Builder {
		private static Logger log = Logger.getLogger(Builder.class);
		
		private boolean isSymmetric;
		
		private Map<RelationKey,Double> distances = new HashMap<RelationKey, Double>();
		
		private Map<RelationKey,Double> times = new HashMap<RelationKey, Double>();
		
		public static Builder newInstance(boolean isSymmetric){
			return new Builder(isSymmetric);
		}
		
		private Builder(boolean isSymmetric){
			this.isSymmetric = isSymmetric;
		}
		
		public Builder addTransportDistance(String from, String to, double distance){
			RelationKey key = RelationKey.newKey(from, to);
			if(distances.containsKey(key)){
				log.warn("distance from " + from + " to " + to + " already exists. This overrides distance.");
			}
			distances.put(key, distance);
			return this;
		}
		
		public Builder addTransportTime(String from, String to, double time){
			RelationKey key = RelationKey.newKey(from, to);
			if(times.containsKey(key)){
				log.warn("transport-time from " + from + " to " + to + " already exists. This overrides distance.");
			}
			times.put(key, time);
			return this;
		}
		
		public VehicleRoutingTransportCostsMatrix build(){
			return new VehicleRoutingTransportCostsMatrix(this);
		}
	}
	
	private Map<RelationKey,Double> distances = new HashMap<RelationKey, Double>();
	
	private Map<RelationKey,Double> times = new HashMap<RelationKey, Double>();
	
	private boolean isSymmetric;
	
	private VehicleRoutingTransportCostsMatrix(Builder builder){
		this.isSymmetric = builder.isSymmetric;
		distances.putAll(builder.distances);
		times.putAll(builder.times);
	}


	@Override
	public double getTransportTime(String fromId, String toId, double departureTime, Driver driver, Vehicle vehicle) {
		return getTime(fromId, toId);
	}


	private double getTime(String fromId, String toId) {
		if(fromId.equals(toId)) return 0.0;
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
	public double getBackwardTransportTime(String fromId, String toId, double arrivalTime, Driver driver, Vehicle vehicle) {
		return getTransportTime(fromId, toId, arrivalTime, driver, vehicle);
	}


	@Override
	public double getTransportCost(String fromId, String toId, double departureTime, Driver driver, Vehicle vehicle) {
		if(vehicle == null) return getDistance(fromId, toId);
		VehicleCostParams costParams = vehicle.getType().getVehicleCostParams();
		return costParams.perDistanceUnit*getDistance(fromId, toId) + costParams.perTimeUnit*getTime(fromId, toId);
	}


	@Override
	public double getBackwardTransportCost(String fromId, String toId, double arrivalTime, Driver driver, Vehicle vehicle) {
		return getTransportCost(fromId, toId, arrivalTime, driver, vehicle);
	}
	
	
	
}
