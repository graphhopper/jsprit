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
import java.util.Map;

import jsprit.core.problem.cost.VehicleRoutingActivityCosts;
import jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import jsprit.core.problem.driver.Driver;
import jsprit.core.problem.job.Job;
import jsprit.core.problem.job.Service;
import jsprit.core.problem.job.Shipment;
import jsprit.core.problem.solution.route.activity.TourActivity;
import jsprit.core.problem.vehicle.Vehicle;
import jsprit.core.problem.vehicle.VehicleType;
import jsprit.core.problem.vehicle.VehicleTypeImpl;
import jsprit.core.util.Coordinate;
import jsprit.core.util.CrowFlyCosts;
import jsprit.core.util.Locations;
import jsprit.core.util.Neighborhood;

import org.apache.log4j.Logger;


/**
 * Contains and describes the vehicle routing problem.
 * 
 * <p>A routing problem is defined as jobs, vehicles and costs. 
 * 
 * <p> To construct the problem, use VehicleRoutingProblem.Builder (VehicleRoutingProblem.Builder.newInstance()). 
 * 
 * <p>By default, fleetSize is INFINITE and fleetComposition is HOMOGENEOUS, transport-costs are calculated as euclidean-distance (CrowFlyCosts),
 * and activity-costs are set to DefaultVehicleRoutingActivityCosts which represent hard time-windows (missed time-windows are penalyzed with Double.MAX_VALUE).
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
	 * @deprecated define constraint directly - since constraints are too diverse to put them in an enum
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
		
		private Collection<Service> services;

		private Collection<Vehicle> vehicles;

		private Map<String, Coordinate> coordinates;

		private FleetSize fleetSize = FleetSize.INFINITE;

		private FleetComposition fleetComposition = FleetComposition.HOMOGENEOUS;
		
		private Collection<VehicleType> vehicleTypes;
		
		private Collection<Constraint> problemConstraints;
		
		private Collection<jsprit.core.problem.constraint.Constraint> constraints;

		/**
		 * by default all locations are neighbors
		 */
		private Neighborhood neighborhood = new Neighborhood() {
			
			@Override
			public boolean areNeighbors(String location1, String location2) {
				return true;
			}
		};

		public Builder() {
			jobs = new HashMap<String, Job>();
			vehicles = new ArrayList<Vehicle>();
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
		 * @return
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
		 * <p>FleetSize is either FleetSize.INFINITE or FleetSize.FINITE
		 * 
		 * @param fleetSize
		 * @return
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
		 * @return
		 * @throws IllegalStateException if job is neither a shipment or a service, or jobId has already been added.
		 */
		public Builder addJob(Job job) {
			if(jobs.containsKey(job.getId())) throw new IllegalStateException("jobList already contains a job with id " + job.getId() + ". make sure you use unique ids for your jobs (i.e. service and shipments)");
			if(job instanceof Service) {
				addService((Service) job);
				return this;
			}
			else if(job instanceof Shipment){
				addShipment((Shipment)job);
				return this;
			}
			throw new IllegalStateException("job must be either a service or a shipment");
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
		 * @return
		 */
		public Builder addVehicle(Vehicle vehicle) {
			vehicles.add(vehicle);
			if(!vehicleTypes.contains(vehicle.getType())){
				vehicleTypes.add(vehicle.getType());
			}
			coordinates.put(vehicle.getLocationId(), vehicle.getCoord());
			return this;
		}
		
		/**
		 * Sets the activityCostFunction that considers also activities on a vehicle-route.
		 * 
		 * <p>Here you can consider missed time-windows for example. By default, this is set to a DefaultVehicleActivityCostFunction.
		 * 
		 * @param activityCosts
		 * @return
		 * @see VehicleRoutingTransportCosts, DefaultVehicleRouteCostFunction
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
			return new VehicleRoutingProblem(this);
		}

		public Builder addLocation(String id, Coordinate coord) {
			coordinates.put(id, coord);
			return this;
		}

		/**
		 * Adds a collection of jobs.
		 * 
		 * @param jobs
		 * @return
		 */
		public Builder addAllJobs(Collection<Job> jobs) {
			for(Job j : jobs){
				addJob(j);
			}
			return this;
		}

		/**
		 * Adds a collection of vehicles.
		 * 
		 * @param vehicles
		 * @return
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
			return Collections.unmodifiableCollection(vehicles);
		}
		
		/**
		 * Adds constraint to problem.
		 * 
		 * @param constraint
		 * @return
		 */
		public Builder addConstraint(jsprit.core.problem.constraint.Constraint constraint){
			constraints.add(constraint);
			return this;
		}
		
		/**
		 * Gets an unmodifiable collection of already added services.
		 * 
		 * @return collection of services
		 */
		public Collection<Service> getAddedServices(){
			return Collections.unmodifiableCollection(services);
		}
		
		public Collection<Job> getAddedJobs(){
			return Collections.unmodifiableCollection(jobs.values());
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
	 *
	 */
	@Deprecated
	public static enum FleetComposition {
		HETEROGENEOUS, HOMOGENEOUS;
	}
	
	private static Logger logger = Logger.getLogger(VehicleRoutingProblem.class);

	private VehicleRoutingTransportCosts transportCosts;
	
	private VehicleRoutingActivityCosts activityCosts;
	
	private Neighborhood neighborhood;
	
	private final Map<String, Job> jobs;

	/**
	 * Collection that contains available vehicles.
	 */
	private final Collection<Vehicle> vehicles;
	
	/**
	 * Collection that contains all available types.
	 */
	private Collection<VehicleType> vehicleTypes;
	
	/**
	 * An enum that indicates type of fleetSize. By default, it is INFINTE
	 */
	private FleetSize fleetSize = FleetSize.INFINITE;
	
	/**
	 * An enum that indicates fleetSizeComposition. By default, it is HOMOGENOUS.
	 */
	private FleetComposition fleetComposition;
	

	private Collection<Constraint> problemConstraints;
	
	private Collection<jsprit.core.problem.constraint.Constraint> constraints;
	
	private VehicleRoutingProblem(Builder builder) {
		this.jobs = builder.jobs;
		this.fleetComposition = builder.fleetComposition;
		this.fleetSize = builder.fleetSize;
		this.vehicles=builder.vehicles;
		this.vehicleTypes = builder.vehicleTypes;
		this.transportCosts = builder.transportCosts;
		this.activityCosts = builder.activityCosts;
		this.neighborhood = builder.neighborhood;
		this.problemConstraints = builder.problemConstraints;
		this.constraints = builder.constraints;
		logger.info("initialise " + this);
	}
	
	@Override
	public String toString() {
		return "[fleetSize="+fleetSize+"][fleetComposition="+fleetComposition+"][#jobs="+jobs.size()+"][#vehicles="+vehicles.size()+"][#vehicleTypes="+vehicleTypes.size()+"]["+
						"transportCost="+transportCosts+"][activityCosts="+activityCosts+"]";
	}

	/**
	 * Returns fleet-composition.
	 * 
	 * @return fleetComposition which is either FleetComposition.HETEROGENEOUS or FleetComposition.HOMOGENEOUS
	 */
	public FleetComposition getFleetComposition() {
		return fleetComposition;
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
