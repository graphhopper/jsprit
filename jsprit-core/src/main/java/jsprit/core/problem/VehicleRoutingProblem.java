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
package jsprit.core.problem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jsprit.core.problem.cost.VehicleRoutingActivityCosts;
import jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import jsprit.core.problem.driver.Driver;
import jsprit.core.problem.job.Job;
import jsprit.core.problem.job.Service;
import jsprit.core.problem.job.Shipment;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.solution.route.activity.TourActivity;
import jsprit.core.problem.vehicle.PenaltyVehicleType;
import jsprit.core.problem.vehicle.Vehicle;
import jsprit.core.problem.vehicle.VehicleImpl;
import jsprit.core.problem.vehicle.VehicleType;
import jsprit.core.problem.vehicle.VehicleTypeImpl;
import jsprit.core.util.Coordinate;
import jsprit.core.util.CrowFlyCosts;
import jsprit.core.util.Locations;
import jsprit.core.util.Neighborhood;

import org.apache.log4j.Logger;


/**
 * Contains and defines the vehicle routing problem.
 * 
 * <p>A routing problem is defined as jobs, vehicles, costs and constraints.
 * 
 * <p> To construct the problem, use VehicleRoutingProblem.Builder. Get an instance of this by using the static method VehicleRoutingProblem.Builder.newInstance(). 
 * 
 * <p>By default, fleetSize is INFINITE, transport-costs are calculated as euclidean-distance (CrowFlyCosts),
 * and activity-costs are set to zero.
 * 
 *  
 * 
 * @author stefan schroeder
 *
 */
public class VehicleRoutingProblem {
	
	/**
	 * Overall problem constraints.
	 * 
	 * <p>DELIVERIES_FIRST corresponds to the vehicle routing problem with back hauls, i.e. before a vehicle is not entirely unloaded, no pickup can be made. 
	 * 
	 * @deprecated define and add constraint directly with .addConstraint(...) - since constraints are too diverse to put them in an enum
	 * @author stefan
	 *
	 */
	@Deprecated
	public enum Constraint {
		DELIVERIES_FIRST
	}
	
	/**
	 * Builder to build the routing-problem.
	 * 
	 * @author stefan schroeder
	 *
	 */
	public static class Builder {

		/**
		 * Two locTypeKeys are equal if they have the same locationId and typeId
		 * 
		 * @author schroeder
		 *
		 */
		private static class LocTypeKey {
			String locationId;
			String typeId;
			
			public LocTypeKey(String locationId, String typeId) {
				super();
				this.locationId = locationId;
				this.typeId = typeId;
			}

			@Override
			public int hashCode() {
				final int prime = 31;
				int result = 1;
				result = prime * result
						+ ((locationId == null) ? 0 : locationId.hashCode());
				result = prime * result
						+ ((typeId == null) ? 0 : typeId.hashCode());
				return result;
			}

			@Override
			public boolean equals(Object obj) {
				if (this == obj)
					return true;
				if (obj == null)
					return false;
				if (getClass() != obj.getClass())
					return false;
				LocTypeKey other = (LocTypeKey) obj;
				if (locationId == null) {
					if (other.locationId != null)
						return false;
				} else if (!locationId.equals(other.locationId))
					return false;
				if (typeId == null) {
					if (other.typeId != null)
						return false;
				} else if (!typeId.equals(other.typeId))
					return false;
				return true;
			}
			
		}
		
		/**
		 * Returns a new instance of this builder.
		 * 
		 * @return builder
		 */
		public static Builder newInstance(){ return new Builder(); }

		private VehicleRoutingTransportCosts transportCosts;
		
		private VehicleRoutingActivityCosts activityCosts = new VehicleRoutingActivityCosts() {
			
			@Override
			public double getActivityCost(TourActivity tourAct, double arrivalTime, Driver driver, Vehicle vehicle) {
				return 0;
			}
			
			@Override
			public String toString() {
				return "[name=defaultActivityCosts]";
			}
			
		};

		private Map<String,Job> jobs;
		
		private Map<String,Job> tentativeJobs = new HashMap<String,Job>();
		
		private Set<String> jobsInInitialRoutes = new HashSet<String>();
		
		private Collection<Service> services;

		private Map<String, Coordinate> coordinates;

		private FleetSize fleetSize = FleetSize.INFINITE;

		/**
		 * @deprecated is not going to be used anymore
		 */
		private FleetComposition fleetComposition = FleetComposition.HOMOGENEOUS;
		
		private Collection<VehicleType> vehicleTypes;
		
		private Collection<VehicleRoute> initialRoutes = new ArrayList<VehicleRoute>();
		
		private Set<Vehicle> uniqueVehicles = new HashSet<Vehicle>();
		
		/**
		 * @deprecated is not going to be used anymore
		 */
		private Collection<Constraint> problemConstraints;
		
		private Collection<jsprit.core.problem.constraint.Constraint> constraints;

		/**
		 * by default all locations are neighbors
		 * @deprecated is not going to be used anymore
		 */
		private Neighborhood neighborhood = new Neighborhood() {
			
			@Override
			public boolean areNeighbors(String location1, String location2) {
				return true;
			}
		};

		private boolean addPenaltyVehicles = false;

		private double penaltyFactor = 1.0;

		private Double penaltyFixedCosts = null;

		/**
		 * @deprecated use static method .newInstance() instead
		 */
		@Deprecated
		public Builder() {
			jobs = new HashMap<String, Job>();
			new ArrayList<Vehicle>();
			coordinates = new HashMap<String, Coordinate>();
			vehicleTypes = new ArrayList<VehicleType>();
			services = new ArrayList<Service>();
			problemConstraints = new ArrayList<VehicleRoutingProblem.Constraint>();
			constraints = new ArrayList<jsprit.core.problem.constraint.Constraint>();
		}

		/**
		 * Create a location (i.e. coordinate) and returns the key of the location which is Coordinate.toString().
		 *  
		 * @param x
		 * @param y
		 * @return locationId
		 * @see Coordinate
		 */
		public String createLocation(double x, double y){
			Coordinate coord = new Coordinate(x, y);
			String id = coord.toString();
			if(!coordinates.containsKey(id)){
				coordinates.put(id, coord);
			}
			return id;
		}
	

		/**
		 * Returns the unmodifiable map of locations (mapped by their id).
		 * 
		 * @return map with locations
		 */
		public Map<String,Coordinate> getLocationMap(){
			return Collections.unmodifiableMap(coordinates);
		}

		/**
		 * Returns the locations collected by this builder.
		 * 
		 * <p>Locations are cached when adding a shipment, service, depot, vehicle.
		 * 
		 * @return locations
		 * 
		 */
		public Locations getLocations(){
			return new Locations() {

				@Override
				public Coordinate getCoord(String id) {
					return coordinates.get(id);
				}
			};
		}

		/**
		 * Sets routing costs.
		 * 
		 * @param costs
		 * @return builder
		 * @see VehicleRoutingTransportCosts
		 */
		public Builder setRoutingCost(VehicleRoutingTransportCosts costs){
			this.transportCosts = costs;
			return this;
		}
		

		/**
		 * Sets the type of fleetSize.
		 * 
		 * <p>FleetSize is either FleetSize.INFINITE or FleetSize.FINITE. By default it is FleetSize.INFINITE.
		 * 
		 * @param fleetSize
		 * @return this builder
		 */
		public Builder setFleetSize(FleetSize fleetSize){
			this.fleetSize = fleetSize;
			return this;
		}

		/**
		 * Adds a job which is either a service or a shipment.
		 * 
		 * <p>Note that job.getId() must be unique, i.e. no job (either it is a shipment or a service) is allowed to have an already allocated id.
		 * 
		 * @param job
		 * @return this builder
		 * @throws IllegalStateException if job is neither a shipment nor a service, or jobId has already been added.
		 */
		public Builder addJob(Job job) {
			if(tentativeJobs.containsKey(job.getId())) throw new IllegalStateException("jobList already contains a job with id " + job.getId() + ". make sure you use unique ids for your jobs (i.e. service and shipments)");
			if(!(job instanceof Service || job instanceof Shipment)) throw new IllegalStateException("job must be either a service or a shipment");  
			tentativeJobs.put(job.getId(), job);
			return this;
		}
		
		private void addJobToFinalJobMap(Job job){
			if(job instanceof Service) {
				addService((Service) job);
			}
			else if(job instanceof Shipment){
				addShipment((Shipment)job);
			}
		}

		public Builder addInitialVehicleRoute(VehicleRoute route){
			addVehicle(route.getVehicle());
			for(Job job : route.getTourActivities().getJobs()){
				jobsInInitialRoutes.add(job.getId());
				if(job instanceof Service) coordinates.put(((Service)job).getLocationId(), ((Service)job).getCoord());
				if(job instanceof Shipment){
					Shipment shipment = (Shipment)job;
					coordinates.put(shipment.getPickupLocation(), shipment.getPickupCoord());
					coordinates.put(shipment.getDeliveryLocation(), shipment.getDeliveryCoord());
				}
			}
			initialRoutes.add(route);
			return this;
		}
		
		public Builder addInitialVehicleRoutes(Collection<VehicleRoute> routes){
			for(VehicleRoute r : routes){
				addInitialVehicleRoute(r);
			}
			return this;
		}
		
		private void addShipment(Shipment job) {
			if(jobs.containsKey(job.getId())){ logger.warn("job " + job + " already in job list. overrides existing job."); }
			coordinates.put(job.getPickupLocation(), job.getPickupCoord());
			coordinates.put(job.getDeliveryLocation(), job.getDeliveryCoord());
			jobs.put(job.getId(),job);
		}

		/**
		 * Adds a vehicle.
		 * 
		 * 
		 * @param vehicle
		 * @return this builder
		 */
		public Builder addVehicle(Vehicle vehicle) {
			uniqueVehicles.add(vehicle);
			if(!vehicleTypes.contains(vehicle.getType())){
				vehicleTypes.add(vehicle.getType());
			}
			String startLocationId = vehicle.getStartLocationId();
			coordinates.put(startLocationId, vehicle.getStartLocationCoordinate());
			if(!vehicle.getEndLocationId().equals(startLocationId)){
				coordinates.put(vehicle.getEndLocationId(), vehicle.getEndLocationCoordinate());
			}
			return this;
		}
		
		/**
		 * Sets the activity-costs.
		 * 
		 * <p>By default it is set to zero.
		 * 
		 * @param activityCosts
		 * @return this builder
		 * @see VehicleRoutingActivityCosts
		 */
		public Builder setActivityCosts(VehicleRoutingActivityCosts activityCosts){
			this.activityCosts = activityCosts;
			return this;
		}

		/**
		 * Builds the {@link VehicleRoutingProblem}.
		 * 
		 * <p>If {@link VehicleRoutingTransportCosts} are not set, {@link CrowFlyCosts} is used.
		 * 
		 * @return {@link VehicleRoutingProblem}
		 */
		public VehicleRoutingProblem build() {
			logger.info("build problem ...");
			if(transportCosts == null){
				logger.warn("set routing costs crowFlyDistance.");
				transportCosts = new CrowFlyCosts(getLocations());
			}
			if(addPenaltyVehicles){
				if(fleetSize.equals(FleetSize.INFINITE)){
					logger.warn("penaltyType and FleetSize.INFINITE does not make sense. thus no penalty-types are added.");
				}
				else{
					addPenaltyVehicles();
				}
			}
			for(Job job : tentativeJobs.values()){
				if(!jobsInInitialRoutes.contains(job.getId())){
					addJobToFinalJobMap(job);
				}
			}
			return new VehicleRoutingProblem(this);
		}

		private void addPenaltyVehicles() {
			Set<LocTypeKey> locTypeKeys = new HashSet<LocTypeKey>();
			List<Vehicle> uniqueVehicles = new ArrayList<Vehicle>();
			for(Vehicle v : this.uniqueVehicles){
				LocTypeKey key = new LocTypeKey(v.getStartLocationId(),v.getType().getTypeId());
				if(!locTypeKeys.contains(key)){
					uniqueVehicles.add(v);
					locTypeKeys.add(key);
				}
			}
			for(Vehicle v : uniqueVehicles){
				double fixed = v.getType().getVehicleCostParams().fix * penaltyFactor;
				if(penaltyFixedCosts!=null){
					fixed = penaltyFixedCosts;
				}
				VehicleTypeImpl t = VehicleTypeImpl.Builder.newInstance(v.getType().getTypeId())
						.setCostPerDistance(penaltyFactor*v.getType().getVehicleCostParams().perDistanceUnit)
						.setCostPerTime(penaltyFactor*v.getType().getVehicleCostParams().perTimeUnit)
						.setFixedCost(fixed)
						.setCapacityDimensions(v.getType().getCapacityDimensions())
						.build();
				PenaltyVehicleType penType = new PenaltyVehicleType(t,penaltyFactor);
				String vehicleId = "penaltyVehicle_" + v.getStartLocationId() + "_" + t.getTypeId();
				Vehicle penVehicle = VehicleImpl.Builder.newInstance(vehicleId).setEarliestStart(v.getEarliestDeparture())
						.setLatestArrival(v.getLatestArrival()).setStartLocationCoordinate(v.getStartLocationCoordinate()).setStartLocationId(v.getStartLocationId())
						.setEndLocationId(v.getEndLocationId()).setEndLocationCoordinate(v.getEndLocationCoordinate())
						.setReturnToDepot(v.isReturnToDepot()).setType(penType).build();
				addVehicle(penVehicle);
			}
		}

		public Builder addLocation(String locationId, Coordinate coord) {
			coordinates.put(locationId, coord);
			return this;
		}

		/**
		 * Adds a collection of jobs.
		 * 
		 * @param jobs which is a collection of jobs that subclasses Job
		 * @return this builder
		 */
		public Builder addAllJobs(Collection<? extends Job> jobs) {
			for(Job j : jobs){
				addJob(j);
			}
			return this;
		}

		/**
		 * Adds a collection of vehicles.
		 * 
		 * @param vehicles
		 * @return this builder
		 */
		public Builder addAllVehicles(Collection<Vehicle> vehicles) {
			for(Vehicle v : vehicles){
				addVehicle(v);
			}
			return this;
		}
		
		/**
		 * Gets an unmodifiable collection of already added vehicles.
		 * 
		 * @return collection of vehicles
		 */
		public Collection<Vehicle> getAddedVehicles(){
			return Collections.unmodifiableCollection(uniqueVehicles);
		}
		
		/**
		 * Gets an unmodifiable collection of already added vehicle-types.
		 * 
		 * @returns collection of vehicle-types
		 */
		public Collection<VehicleType> getAddedVehicleTypes(){
			return Collections.unmodifiableCollection(vehicleTypes);
		}
		
		/**
		 * Adds constraint to problem.
		 * 
		 * @param constraint
		 * @return this builder
		 */
		public Builder addConstraint(jsprit.core.problem.constraint.Constraint constraint){
			constraints.add(constraint);
			return this;
		}
		
		/**
		 * Adds penaltyVehicles, i.e. for every unique vehicle-location and type combination a penalty-vehicle is constructed having penaltyFactor times higher fixed and variable costs 
		 * (see .addPenaltyVehicles(double penaltyFactor, double penaltyFixedCosts) if fixed costs = 0.0). 
		 * 
		 * <p>This only makes sense for FleetSize.FINITE. Thus, penaltyVehicles are only added if is FleetSize.FINITE.
		 * <p>The id of penaltyVehicles is constructed as follows vehicleId = "penaltyVehicle" + "_" + {locationId} + "_" + {typeId}. 
		 * <p>By default: no penalty-vehicles are added
		 * 
		 * @param penaltyFactor
		 * @return this builder
		 */
		public Builder addPenaltyVehicles(double penaltyFactor){
			this.addPenaltyVehicles = true;
			this.penaltyFactor = penaltyFactor;
			return this;
		}
		
		/**
		 * Adds penaltyVehicles, i.e. for every unique vehicle-location and type combination a penalty-vehicle is constructed having penaltyFactor times higher fixed and variable costs. 
		 * <p>This method takes penaltyFixedCosts as absolute value in contrary to the method without penaltyFixedCosts where fixedCosts is the product of penaltyFactor and typeFixedCosts.
		 * <p>This only makes sense for FleetSize.FINITE. Thus, penaltyVehicles are only added if is FleetSize.FINITE.
		 * <p>The id of penaltyVehicles is constructed as follows vehicleId = "penaltyVehicle" + "_" + {locationId} + "_" + {typeId}. 
		 * <p>By default: no penalty-vehicles are added
		 * 
		 * @param penaltyFactor
		 * @param penaltyFixedCosts which is an absolute penaltyValue (in contrary to penaltyFactor)
		 * @return this builder
		 */
		public Builder addPenaltyVehicles(double penaltyFactor, double penaltyFixedCosts){
			this.addPenaltyVehicles = true;
			this.penaltyFactor = penaltyFactor;
			this.penaltyFixedCosts  = penaltyFixedCosts;
			return this;
		}
		
		/**
         * Returns an unmodifiable collection of already added jobs.
         *
         * @return collection of jobs
         */
		public Collection<Job> getAddedJobs(){
			return Collections.unmodifiableCollection(tentativeJobs.values());
		}

		/**
		 * Gets an unmodifiable collection of already added services.
		 * 
		 * @return collection of services
		         * @deprecated use .getAddedJobs() instead
		 */
		        @Deprecated
		public Collection<Service> getAddedServices(){
			return Collections.unmodifiableCollection(services);
		}

		/**
		 * Sets the fleetComposition.
		 * 
		 * <p>FleetComposition is either FleetComposition.HETEROGENEOUS or FleetComposition.HOMOGENEOUS
		 * 
		 * @deprecated has no effect
		 * @param fleetComposition
		 * @return
		 */
		@Deprecated
		public Builder setFleetComposition(FleetComposition fleetComposition){
			this.fleetComposition = fleetComposition;
			return this;
		}

		/**
		 * Adds a service to jobList.
		 * 
		 * <p>If jobList already contains service, a warning message is printed, and the existing job will be overwritten.
		 * 
		 * @deprecated use addJob(...) instead
		 * @param service
		 * @return
		 */
		@Deprecated
		public Builder addService(Service service){
			coordinates.put(service.getLocationId(), service.getCoord());
			if(jobs.containsKey(service.getId())){ logger.warn("service " + service + " already in job list. overrides existing job."); }
			jobs.put(service.getId(),service);
			services.add(service);
			return this;
		}

		/**
		 * Adds a vehicleType.
		 * 
		 * @deprecated use add vehicle instead
		 * @param type
		 * @return builder
		 */
		@Deprecated
		public Builder addVehicleType(VehicleType type){
			vehicleTypes.add(type);
			return this;
		}

		/**
		 * Sets the neighborhood.
		 * 
		 * @deprecated use HardRoute- or ActivityLevelConstraint instead
		 * @param neighborhood
		 * @return
		 */
		@Deprecated
		public Builder setNeighborhood(Neighborhood neighborhood){
			this.neighborhood = neighborhood;
			return this;
		}

		/**
		 * 
		 * @deprecated use .addConstraint(new ServiceDeliveriesFirstConstraint())
		 * @param constraint
		 */
		@Deprecated
		public void addProblemConstraint(Constraint constraint){
			if(!problemConstraints.contains(constraint)) problemConstraints.add(constraint);
		}
}
	
	/**
	 * Enum that characterizes the fleet-size.
	 * 
	 * @author sschroeder
	 *
	 */
	public static enum FleetSize {
		FINITE, INFINITE;
	}
	
	/**
	 * Enum that characterizes fleet-compostion.
	 * 
	 * @author sschroeder
	 * @deprecated FleetComposition is not used
	 */
	@Deprecated
	public static enum FleetComposition {
		HETEROGENEOUS, HOMOGENEOUS;
	}
	
	/**
	 * logger logging for this class
	 */
	private final static Logger logger = Logger.getLogger(VehicleRoutingProblem.class);

	/**
	 * contains transportation costs, i.e. the costs traveling from location A to B
	 */
	private final VehicleRoutingTransportCosts transportCosts;
	
	/**
	 * contains activity costs, i.e. the costs imposed by an activity
	 */
	private final VehicleRoutingActivityCosts activityCosts;
	
	/**
	 * map of jobs, stored by jobId 
	 */
	private final Map<String, Job> jobs;

	/**
	 * Collection that contains available vehicles.
	 */
	private final Collection<Vehicle> vehicles;
	
	/**
	 * Collection that contains all available types.
	 */
	private final Collection<VehicleType> vehicleTypes;
	
	
	private final Collection<VehicleRoute> initialVehicleRoutes;
	
	/**
	 * An enum that indicates type of fleetSize. By default, it is INFINTE
	 */
	private final FleetSize fleetSize;
	
	/**
	 * contains all constraints
	 */
	private final Collection<jsprit.core.problem.constraint.Constraint> constraints;
	
	private final Locations locations;
	
	/**
	 * @deprecated not used anymore
	 */
	private Neighborhood neighborhood;

	/**
	 * An enum that indicates fleetSizeComposition. By default, it is HOMOGENOUS.
	     * @deprecated
	 */
	private FleetComposition fleetComposition;

	/**
	 * @deprecated
	 */
	private Collection<Constraint> problemConstraints;

	private VehicleRoutingProblem(Builder builder) {
		this.jobs = builder.jobs;
		this.fleetComposition = builder.fleetComposition;
		this.fleetSize = builder.fleetSize;
		this.vehicles=builder.uniqueVehicles;
		this.vehicleTypes = builder.vehicleTypes;
		this.initialVehicleRoutes = builder.initialRoutes;
		this.transportCosts = builder.transportCosts;
		this.activityCosts = builder.activityCosts;
		this.neighborhood = builder.neighborhood;
		this.problemConstraints = builder.problemConstraints;
		this.constraints = builder.constraints;
		this.locations = builder.getLocations();
		logger.info("initialise " + this);
	}
	
	@Override
	public String toString() {
		return "[fleetSize="+fleetSize+"][#jobs="+jobs.size()+"][#vehicles="+vehicles.size()+"][#vehicleTypes="+vehicleTypes.size()+"]["+
						"transportCost="+transportCosts+"][activityCosts="+activityCosts+"]";
	}

	/**
	 * Returns type of fleetSize, either INFINITE or FINITE.
	 * 
	 * <p>By default, it is INFINITE.
	 * 
	 * @return either FleetSize.INFINITE or FleetSize.FINITE
	 */
	public FleetSize getFleetSize() {
		return fleetSize;
	}
	
	/**
	 * Returns the unmodifiable job map.
	 * 
	 * @return unmodifiable jobMap
	 */
	public Map<String, Job> getJobs() {
		return Collections.unmodifiableMap(jobs);
	}
	
	public Collection<VehicleRoute> getInitialVehicleRoutes(){
		return Collections.unmodifiableCollection(initialVehicleRoutes);
	}

	/**
	 * Returns the entire, unmodifiable collection of types.
	 * 
	 * @return unmodifiable collection of types
	 * @see VehicleTypeImpl
	 */
	public Collection<VehicleType> getTypes(){
		return Collections.unmodifiableCollection(vehicleTypes);
	}
	
	
	/**
	 * Returns the entire, unmodifiable collection of vehicles.
	 * 
	 * @return unmodifiable collection of vehicles
	 * @see Vehicle
	 */
	public Collection<Vehicle> getVehicles() {
		return Collections.unmodifiableCollection(vehicles);
	}

	/**
	 * Returns routing costs.
	 * 
	 * @return routingCosts
	 * @see VehicleRoutingTransportCosts
	 */
	public VehicleRoutingTransportCosts getTransportCosts() {
		return transportCosts;
	}

	/**
	 * Returns activityCosts.
	 */
	public VehicleRoutingActivityCosts getActivityCosts(){
		return activityCosts;
	}
	
	/**
	 * Returns an unmodifiable collection of constraints.
	 * 
	 * @return
	 */
	public Collection<jsprit.core.problem.constraint.Constraint> getConstraints(){
		return Collections.unmodifiableCollection(constraints);
	}
	
	public Locations getLocations(){
		return locations;
	}

	/**
	 * Returns fleet-composition.
	 * 
	 * @return fleetComposition which is either FleetComposition.HETEROGENEOUS or FleetComposition.HOMOGENEOUS
	     * @deprecated it is not used and thus has no effect
	 */
	@Deprecated
	public FleetComposition getFleetComposition() {
		return fleetComposition;
	}

	/**
	 * @deprecated see builder.setNeighborhood(...). addConstraint(...) instead.
	 * @return the neighborhood
	 */
	@Deprecated
	public Neighborhood getNeighborhood() {
		return neighborhood;
	}

	/**
	 * Returns unmodifiable collection of problem-constraints.
	 * 
	 * @deprecated use .getConstraints() and builder.add
	 * @return
	 */
	@Deprecated
	public Collection<Constraint> getProblemConstraints(){
		return Collections.unmodifiableCollection(problemConstraints);
	}
	
	
}
