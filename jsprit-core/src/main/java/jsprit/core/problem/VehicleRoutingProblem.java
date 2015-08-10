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
package jsprit.core.problem;

import jsprit.core.problem.cost.VehicleRoutingActivityCosts;
import jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import jsprit.core.problem.driver.Driver;
import jsprit.core.problem.job.Job;
import jsprit.core.problem.job.Service;
import jsprit.core.problem.job.Shipment;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.solution.route.activity.DefaultShipmentActivityFactory;
import jsprit.core.problem.solution.route.activity.DefaultTourActivityFactory;
import jsprit.core.problem.solution.route.activity.TourActivity;
import jsprit.core.problem.vehicle.Vehicle;
import jsprit.core.problem.vehicle.VehicleType;
import jsprit.core.problem.vehicle.VehicleTypeImpl;
import jsprit.core.problem.vehicle.VehicleTypeKey;
import jsprit.core.util.Coordinate;
import jsprit.core.util.CrowFlyCosts;
import jsprit.core.util.Locations;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;


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

		private Map<String,Job> jobs = new LinkedHashMap<String, Job>();
		
		private Map<String,Job> tentativeJobs = new LinkedHashMap<String,Job>();
		
		private Set<String> jobsInInitialRoutes = new HashSet<String>();
		
		private Map<String, Coordinate> tentative_coordinates = new HashMap<String, Coordinate>();

		private FleetSize fleetSize = FleetSize.INFINITE;
		
		private Collection<VehicleType> vehicleTypes = new ArrayList<VehicleType>();
		
		private Collection<VehicleRoute> initialRoutes = new ArrayList<VehicleRoute>();
		
		private Set<Vehicle> uniqueVehicles = new HashSet<Vehicle>();

		private JobActivityFactory jobActivityFactory = new JobActivityFactory() {

            @Override
            public List<AbstractActivity> createActivities(Job job) {
                List<AbstractActivity> acts = new ArrayList<AbstractActivity>();
                if(job instanceof Service){
                    acts.add(serviceActivityFactory.createActivity((Service) job));
                }
                else if(job instanceof Shipment){
                    acts.add(shipmentActivityFactory.createPickup((Shipment) job));
                    acts.add(shipmentActivityFactory.createDelivery((Shipment) job));
                }
                return acts;
            }

        };

        private int jobIndexCounter = 1;

        private int vehicleIndexCounter = 1;

        private int activityIndexCounter = 1;

        private int vehicleTypeIdIndexCounter = 1;

        private Map<VehicleTypeKey,Integer> typeKeyIndices = new HashMap<VehicleTypeKey, Integer>();

        private Map<Job,List<AbstractActivity>> activityMap = new HashMap<Job, List<AbstractActivity>>();

        private final DefaultShipmentActivityFactory shipmentActivityFactory = new DefaultShipmentActivityFactory();

        private final DefaultTourActivityFactory serviceActivityFactory = new DefaultTourActivityFactory();

        private void incJobIndexCounter(){
            jobIndexCounter++;
        }

        private void incActivityIndexCounter(){
            activityIndexCounter++;
        }

        private void incVehicleTypeIdIndexCounter() { vehicleTypeIdIndexCounter++; }

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
		 * @param costs the routingCosts
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
		 * @param fleetSize the fleet size used in this problem. it can either be FleetSize.INFINITE or FleetSize.FINITE
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
		 * @param job job to be added
		 * @return this builder
		 * @throws IllegalStateException if job is neither a shipment nor a service, or jobId has already been added.
         * @deprecated use addJob(AbstractJob job) instead
		 */
        @Deprecated
		public Builder addJob(Job job) {
            if(!(job instanceof AbstractJob)) throw new IllegalArgumentException("job must be of type AbstractJob");
            return addJob((AbstractJob)job);
		}

        /**
         * Adds a job which is either a service or a shipment.
         *
         * <p>Note that job.getId() must be unique, i.e. no job (either it is a shipment or a service) is allowed to have an already allocated id.
         *
         * @param job job to be added
         * @return this builder
         * @throws IllegalStateException if job is neither a shipment nor a service, or jobId has already been added.
         */
        public Builder addJob(AbstractJob job) {
            if(tentativeJobs.containsKey(job.getId())) throw new IllegalStateException("jobList already contains a job with id " + job.getId() + ". make sure you use unique ids for your jobs (i.e. service and shipments)");
            if(!(job instanceof Service || job instanceof Shipment)) throw new IllegalStateException("job must be either a service or a shipment");
            job.setIndex(jobIndexCounter);
            incJobIndexCounter();
            tentativeJobs.put(job.getId(), job);
            addLocationToTentativeLocations(job);
            return this;
        }
		
		private void addLocationToTentativeLocations(Job job) {
			if(job instanceof Service) {
				tentative_coordinates.put(((Service)job).getLocation().getId(), ((Service)job).getLocation().getCoordinate());
			}
			else if(job instanceof Shipment){
				Shipment shipment = (Shipment)job;
				tentative_coordinates.put(shipment.getPickupLocation().getId(), shipment.getPickupLocation().getCoordinate());
				tentative_coordinates.put(shipment.getDeliveryLocation().getId(), shipment.getDeliveryLocation().getCoordinate());
			}
		}

		private void addJobToFinalJobMapAndCreateActivities(Job job){
            if(job instanceof Service) {
                Service service = (Service) job;
				addService(service);
			}
			else if(job instanceof Shipment){
				Shipment shipment = (Shipment)job;
                addShipment(shipment);
			}
            List<AbstractActivity> jobActs = jobActivityFactory.createActivities(job);
            for(AbstractActivity act : jobActs){
                act.setIndex(activityIndexCounter);
                incActivityIndexCounter();
            }
            activityMap.put(job, jobActs);
		}


        /**
         * Adds an initial vehicle route.
         *
         * @param route initial route
         * @return the builder
         */
        public Builder addInitialVehicleRoute(VehicleRoute route){
			addVehicle((AbstractVehicle)route.getVehicle());
            for(TourActivity act : route.getActivities()){
                AbstractActivity abstractAct = (AbstractActivity) act;
                abstractAct.setIndex(activityIndexCounter);
                incActivityIndexCounter();
                if(act instanceof TourActivity.JobActivity) {
                    Job job = ((TourActivity.JobActivity) act).getJob();
                    jobsInInitialRoutes.add(job.getId());
                    registerLocation(job);
                    registerJobAndActivity(abstractAct, job);
                }
            }
			initialRoutes.add(route);
			return this;
		}

        private void registerLocation(Job job) {
            if (job instanceof Service) tentative_coordinates.put(((Service) job).getLocation().getId(), ((Service) job).getLocation().getCoordinate());
            if (job instanceof Shipment) {
                Shipment shipment = (Shipment) job;
                tentative_coordinates.put(shipment.getPickupLocation().getId(), shipment.getPickupLocation().getCoordinate());
                tentative_coordinates.put(shipment.getDeliveryLocation().getId(), shipment.getDeliveryLocation().getCoordinate());
            }
        }

        private void registerJobAndActivity(AbstractActivity abstractAct, Job job) {
            if(activityMap.containsKey(job)) activityMap.get(job).add(abstractAct);
            else{
                List<AbstractActivity> actList = new ArrayList<AbstractActivity>();
                actList.add(abstractAct);
                activityMap.put(job,actList);
            }
        }

        /**
         * Adds a collection of initial vehicle routes.
         *
         * @param routes initial routes
         * @return the builder
         */
        public Builder addInitialVehicleRoutes(Collection<VehicleRoute> routes){
			for(VehicleRoute r : routes){
				addInitialVehicleRoute(r);
			}
			return this;
		}
		
		private void addShipment(Shipment job) {
			if(jobs.containsKey(job.getId())){ logger.warn("job {} already in job list. overrides existing job.", job); }
			tentative_coordinates.put(job.getPickupLocation().getId(), job.getPickupLocation().getCoordinate());
			tentative_coordinates.put(job.getDeliveryLocation().getId(), job.getDeliveryLocation().getCoordinate());
			jobs.put(job.getId(),job);
		}

		/**
		 * Adds a vehicle.
		 * 
		 * 
		 * @param vehicle vehicle to be added
		 * @return this builder
         * @deprecated use addVehicle(AbstractVehicle vehicle) instead
		 */
        @Deprecated
		public Builder addVehicle(Vehicle vehicle) {
            if(!(vehicle instanceof AbstractVehicle)) throw new IllegalStateException("vehicle must be an AbstractVehicle");
            return addVehicle((AbstractVehicle)vehicle);
		}

        /**
         * Adds a vehicle.
         *
         *
         * @param vehicle vehicle to be added
         * @return this builder
         */
        public Builder addVehicle(AbstractVehicle vehicle) {
            if(!uniqueVehicles.contains(vehicle)){
                vehicle.setIndex(vehicleIndexCounter);
                incVehicleIndexCounter();
            }
            if(typeKeyIndices.containsKey(vehicle.getVehicleTypeIdentifier())){
                vehicle.getVehicleTypeIdentifier().setIndex(typeKeyIndices.get(vehicle.getVehicleTypeIdentifier()));
            }
            else {
                vehicle.getVehicleTypeIdentifier().setIndex(vehicleTypeIdIndexCounter);
                typeKeyIndices.put(vehicle.getVehicleTypeIdentifier(),vehicleTypeIdIndexCounter);
                incVehicleTypeIdIndexCounter();
            }
            uniqueVehicles.add(vehicle);
            if(!vehicleTypes.contains(vehicle.getType())){
                vehicleTypes.add(vehicle.getType());
            }
            String startLocationId = vehicle.getStartLocation().getId();
            tentative_coordinates.put(startLocationId, vehicle.getStartLocation().getCoordinate());
            if(!vehicle.getEndLocation().getId().equals(startLocationId)){
                tentative_coordinates.put(vehicle.getEndLocation().getId(), vehicle.getEndLocation().getCoordinate());
            }
            return this;
        }

        private void incVehicleIndexCounter() {
            vehicleIndexCounter++;
        }

        /**
		 * Sets the activity-costs.
		 * 
		 * <p>By default it is set to zero.
		 * 
		 * @param activityCosts activity costs of the problem
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
			if(transportCosts == null){
				transportCosts = new CrowFlyCosts(getLocations());
			}
            for(Job job : tentativeJobs.values())
                if (!jobsInInitialRoutes.contains(job.getId())) {
                    addJobToFinalJobMapAndCreateActivities(job);
                }
			return new VehicleRoutingProblem(this);
		}

        @SuppressWarnings("UnusedDeclaration")
        public Builder addLocation(String locationId, Coordinate coordinate) {
			tentative_coordinates.put(locationId, coordinate);
			return this;
		}

		/**
		 * Adds a collection of jobs.
		 * 
		 * @param jobs which is a collection of jobs that subclasses Job
		 * @return this builder
		 */
		@SuppressWarnings("deprecation")
        public Builder addAllJobs(Collection<? extends Job> jobs) {
			for(Job j : jobs){
				addJob(j);
			}
			return this;
		}

		/**
		 * Adds a collection of vehicles.
		 * 
		 * @param vehicles vehicles to be added
		 * @return this builder
		 */
		@SuppressWarnings("deprecation")
        public Builder addAllVehicles(Collection<? extends Vehicle> vehicles) {
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
		 * @return collection of vehicle-types
		 */
		public Collection<VehicleType> getAddedVehicleTypes(){
			return Collections.unmodifiableCollection(vehicleTypes);
		}

        /**
         * Returns an unmodifiable collection of already added jobs.
         *
         * @return collection of jobs
         */
		public Collection<Job> getAddedJobs(){
			return Collections.unmodifiableCollection(tentativeJobs.values());
		}

		private Builder addService(Service service){
			tentative_coordinates.put(service.getLocation().getId(), service.getLocation().getCoordinate());
			if(jobs.containsKey(service.getId())){ logger.warn("service {} already in job list. overrides existing job.", service); }
			jobs.put(service.getId(),service);
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
		FINITE, INFINITE
	}
	
	/**
	 * logger logging for this class
	 */
	private final static Logger logger = LogManager.getLogger(VehicleRoutingProblem.class);

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

	private final Locations locations;

    private Map<Job,List<AbstractActivity>> activityMap;

    private int nuActivities;

    private final JobActivityFactory jobActivityFactory = new JobActivityFactory() {

        @Override
        public List<AbstractActivity> createActivities(Job job) {
            return copyAndGetActivities(job);
        }

    };
	
	private VehicleRoutingProblem(Builder builder) {
		this.jobs = builder.jobs;
		this.fleetSize = builder.fleetSize;
		this.vehicles=builder.uniqueVehicles;
		this.vehicleTypes = builder.vehicleTypes;
		this.initialVehicleRoutes = builder.initialRoutes;
		this.transportCosts = builder.transportCosts;
		this.activityCosts = builder.activityCosts;
		this.locations = builder.getLocations();
        this.activityMap = builder.activityMap;
        this.nuActivities = builder.activityIndexCounter;
		logger.info("setup problem: {}", this);
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

    /**
     * Returns a copy of initial vehicle routes.
     *
     * @return copied collection of initial vehicle routes
     */
	public Collection<VehicleRoute> getInitialVehicleRoutes(){
		Collection<VehicleRoute> copiedInitialRoutes = new ArrayList<VehicleRoute>();
        for(VehicleRoute route : initialVehicleRoutes){
            copiedInitialRoutes.add(VehicleRoute.copyOf(route));
        }
        return copiedInitialRoutes;
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
     * @return returns all location, i.e. from vehicles and jobs.
     */
	public Locations getLocations(){
		return locations;
	}

    /**
     * @param job for which the corresponding activities needs to be returned
     * @return associated activities
     */
    public List<AbstractActivity> getActivities(Job job){
        return Collections.unmodifiableList(activityMap.get(job));
    }

    /**
     * @return total number of activities
     */
    public int getNuActivities(){ return nuActivities; }

    /**
     * @return factory that creates the activities associated to a job
     */
    public JobActivityFactory getJobActivityFactory(){
        return jobActivityFactory;
    }

    /**
     * @param job for which the corresponding activities needs to be returned
     * @return a copy of the activities that are associated to the specified job
     */
    public List<AbstractActivity> copyAndGetActivities(Job job){
        List<AbstractActivity> acts = new ArrayList<AbstractActivity>();
        if(activityMap.containsKey(job)) {
            for (AbstractActivity act : activityMap.get(job)) acts.add((AbstractActivity) act.duplicate());
        }
        return acts;
    }
	
}
