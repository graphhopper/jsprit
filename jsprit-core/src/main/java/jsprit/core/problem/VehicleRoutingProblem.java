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
import jsprit.core.problem.vehicle.VehicleTypeKey;
import jsprit.core.util.Coordinate;
import jsprit.core.util.CrowFlyCosts;
import jsprit.core.util.Locations;

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
	 * Builder to build the routing-problem.
	 * 
	 * @author stefan schroeder
	 *
	 */
	public static class Builder {

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

		private Map<String,Job> jobs = new HashMap<String, Job>();
		
		private Map<String,Job> tentativeJobs = new HashMap<String,Job>();
		
		private Set<String> jobsInInitialRoutes = new HashSet<String>();
		
		private Collection<Service> services = new ArrayList<Service>();

		private Map<String, Coordinate> coordinates = new HashMap<String, Coordinate>();
		
		private Map<String, Coordinate> tentative_coordinates = new HashMap<String, Coordinate>();

		private FleetSize fleetSize = FleetSize.INFINITE;
		
		private Collection<VehicleType> vehicleTypes = new ArrayList<VehicleType>();
		
		private Collection<VehicleRoute> initialRoutes = new ArrayList<VehicleRoute>();
		
		private Set<Vehicle> uniqueVehicles = new HashSet<Vehicle>();
		
		private Collection<jsprit.core.problem.constraint.Constraint> constraints = new ArrayList<jsprit.core.problem.constraint.Constraint>();

		private boolean addPenaltyVehicles = false;

		private double penaltyFactor = 1.0;

		private Double penaltyFixedCosts = null;

		/**
		 * Create a location (i.e. coordinate) and returns the key of the location which is Coordinate.toString().
		 *  
		 * @param x
		 * @param y
		 * @return locationId
		 * @see Coordinate
		 */
		@Deprecated
		public String createLocation(double x, double y){
			Coordinate coord = new Coordinate(x, y);
			String id = coord.toString();
			if(!tentative_coordinates.containsKey(id)){
				tentative_coordinates.put(id, coord);
			}
			return id;
		}
	

		/**
		 * Returns the unmodifiable map of collected locations (mapped by their location-id).
		 * 
		 * @return map with locations
		 */
		public Map<String,Coordinate> getLocationMap(){
			return Collections.unmodifiableMap(tentative_coordinates);
		}

		/**
		 * Returns the locations collected SO FAR by this builder.
		 * 
		 * <p>Locations are cached when adding a shipment, service, depot, vehicle.
		 * 
		 * @return locations
		 * 
		 **/
		public Locations getLocations(){
			return new Locations() {

				@Override
				public Coordinate getCoord(String id) {
					return tentative_coordinates.get(id);
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
			addLocationToTentativeLocations(job);
			return this;
		}
		
		private void addLocationToTentativeLocations(Job job) {
			if(job instanceof Service) {
				tentative_coordinates.put(((Service)job).getLocationId(), ((Service)job).getCoord());
			}
			else if(job instanceof Shipment){
				Shipment shipment = (Shipment)job;
				tentative_coordinates.put(shipment.getPickupLocation(), shipment.getPickupCoord());
				tentative_coordinates.put(shipment.getDeliveryLocation(), shipment.getDeliveryCoord());
			}
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
				if(job instanceof Service) {
					coordinates.put(((Service)job).getLocationId(), ((Service)job).getCoord());
					tentative_coordinates.put(((Service)job).getLocationId(), ((Service)job).getCoord());
				}
				if(job instanceof Shipment){
					Shipment shipment = (Shipment)job;
					coordinates.put(shipment.getPickupLocation(), shipment.getPickupCoord());
					tentative_coordinates.put(shipment.getPickupLocation(), shipment.getPickupCoord());
					coordinates.put(shipment.getDeliveryLocation(), shipment.getDeliveryCoord());
					tentative_coordinates.put(shipment.getDeliveryLocation(), shipment.getDeliveryCoord());
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
			tentative_coordinates.put(startLocationId, vehicle.getStartLocationCoordinate());
			if(!vehicle.getEndLocationId().equals(startLocationId)){
				coordinates.put(vehicle.getEndLocationId(), vehicle.getEndLocationCoordinate());
				tentative_coordinates.put(vehicle.getEndLocationId(), vehicle.getEndLocationCoordinate());
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
			Set<VehicleTypeKey> vehicleTypeKeys = new HashSet<VehicleTypeKey>();
			List<Vehicle> uniqueVehicles = new ArrayList<Vehicle>();
			for(Vehicle v : this.uniqueVehicles){
				VehicleTypeKey key = new VehicleTypeKey(v.getType().getTypeId(),v.getStartLocationId(),v.getEndLocationId(),v.getEarliestDeparture(),v.getLatestArrival());
				if(!vehicleTypeKeys.contains(key)){
					uniqueVehicles.add(v);
					vehicleTypeKeys.add(key);
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
				Vehicle penVehicle = VehicleImpl.copyAndCreateVehicleWithNewType(v, penType);
//				String vehicleId = v.getId();
//				Vehicle penVehicle = VehicleImpl.Builder.newInstance(vehicleId).setEarliestStart(v.getEarliestDeparture())
//						.setLatestArrival(v.getLatestArrival()).setStartLocationCoordinate(v.getStartLocationCoordinate()).setStartLocationId(v.getStartLocationId())
//						.setEndLocationId(v.getEndLocationId()).setEndLocationCoordinate(v.getEndLocationCoordinate())
//						.setReturnToDepot(v.isReturnToDepot()).setType(penType).build();
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
		 * Adds a service to jobList.
		 * 
		 * <p>If jobList already contains service, a warning message is printed, and the existing job will be overwritten.
		 * 
		 * @param service
		 * @return
		 */
		private Builder addService(Service service){
			coordinates.put(service.getLocationId(), service.getCoord());
			if(jobs.containsKey(service.getId())){ logger.warn("service " + service + " already in job list. overrides existing job."); }
			jobs.put(service.getId(),service);
			services.add(service);
			return this;
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
	
	private VehicleRoutingProblem(Builder builder) {
		this.jobs = builder.jobs;
		this.fleetSize = builder.fleetSize;
		this.vehicles=builder.uniqueVehicles;
		this.vehicleTypes = builder.vehicleTypes;
		this.initialVehicleRoutes = builder.initialRoutes;
		this.transportCosts = builder.transportCosts;
		this.activityCosts = builder.activityCosts;
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
	
}
