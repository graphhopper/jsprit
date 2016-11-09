/*
 * Licensed to GraphHopper GmbH under one or more contributor
 * license agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * GraphHopper GmbH licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.graphhopper.jsprit.core.problem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingActivityCosts;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import com.graphhopper.jsprit.core.problem.cost.WaitingTimeCosts;
import com.graphhopper.jsprit.core.problem.job.AbstractJob;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.AbstractActivityNEW;
import com.graphhopper.jsprit.core.problem.solution.route.activity.DefaultShipmentActivityFactory;
import com.graphhopper.jsprit.core.problem.solution.route.activity.DefaultTourActivityFactory;
import com.graphhopper.jsprit.core.problem.solution.route.activity.JobActivity;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleType;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeKey;
import com.graphhopper.jsprit.core.util.Coordinate;
import com.graphhopper.jsprit.core.util.CrowFlyCosts;
import com.graphhopper.jsprit.core.util.Locations;



/**
 * Contains and defines the vehicle routing problem.
 * <p>
 * <p>A routing problem is defined as jobs, vehicles, costs and constraints.
 * <p>
 * <p> To construct the problem, use VehicleRoutingProblem.Builder. Get an instance of this by using the static method VehicleRoutingProblem.Builder.newInstance().
 * <p>
 * <p>By default, fleetSize is INFINITE, transport-costs are calculated as euclidean-distance (CrowFlyCosts),
 * and activity-costs are set to zero.
 *
 * @author stefan schroeder
 */
public class VehicleRoutingProblem {

    /**
     * Builder to build the routing-problem.
     *
     * @author stefan schroeder
     */
    public static class Builder {


        /**
         * Returns a new instance of this builder.
         *
         * @return builder
         */
        public static Builder newInstance() {
            return new Builder();
        }

        private VehicleRoutingTransportCosts transportCosts;

        private VehicleRoutingActivityCosts activityCosts = new WaitingTimeCosts();

        private Map<String, Job> jobs = new LinkedHashMap<String, Job>();

        private Map<String, Job> tentativeJobs = new LinkedHashMap<String, Job>();

        private Set<String> jobsInInitialRoutes = new HashSet<String>();

        private Map<String, Coordinate> tentative_coordinates = new HashMap<String, Coordinate>();

        private FleetSize fleetSize = FleetSize.INFINITE;

        private Collection<VehicleType> vehicleTypes = new ArrayList<VehicleType>();

        private Collection<VehicleRoute> initialRoutes = new ArrayList<VehicleRoute>();

        private Set<Vehicle> uniqueVehicles = new LinkedHashSet<Vehicle>();

        private Set<String> addedVehicleIds = new LinkedHashSet<String>();

        private boolean hasBreaks = false;

        private JobActivityFactory jobActivityFactory = new JobActivityFactory() {

            @Override
            public List<JobActivity> createActivities(Job job) {
                // Now its safe, but be carful if another implementation of Job is made
                return ((AbstractJob) job).getActivityList().getAll();
            }

        };

        private int jobIndexCounter = 1;

        private int vehicleIndexCounter = 1;

        private int activityIndexCounter = 1;

        private int vehicleTypeIdIndexCounter = 1;

        private Map<VehicleTypeKey, Integer> typeKeyIndices = new HashMap<VehicleTypeKey, Integer>();

// Deprecated ?
//        private Map<Job, List<JobActivity>> activityMap = new HashMap<>();

        private final DefaultShipmentActivityFactory shipmentActivityFactory = new DefaultShipmentActivityFactory();

        private final DefaultTourActivityFactory serviceActivityFactory = new DefaultTourActivityFactory();

        private void incJobIndexCounter() {
            jobIndexCounter++;
        }

        private void incActivityIndexCounter() {
            activityIndexCounter++;
        }

        private void incVehicleTypeIdIndexCounter() {
            vehicleTypeIdIndexCounter++;
        }

        private Set<Location> allLocations = new HashSet<Location>();

        /**
         * Returns the unmodifiable map of collected locations (mapped by their location-id).
         *
         * @return map with locations
         */
        public Map<String, Coordinate> getLocationMap() {
            return Collections.unmodifiableMap(tentative_coordinates);
        }



        /**
         * Returns the locations collected SO FAR by this builder.
         * <p>
         * <p>Locations are cached when adding a shipment, service, depot, vehicle.
         *
         * @return locations
         */
        public Locations getLocations() {
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
        public Builder setRoutingCost(VehicleRoutingTransportCosts costs) {
            transportCosts = costs;
            return this;
        }


        public Builder setJobActivityFactory(JobActivityFactory jobActivityFactory) {
            this.jobActivityFactory = jobActivityFactory;
            return this;
        }

        /**
         * Sets the type of fleetSize.
         * <p>
         * <p>FleetSize is either FleetSize.INFINITE or FleetSize.FINITE. By default it is FleetSize.INFINITE.
         *
         * @param fleetSize the fleet size used in this problem. it can either be FleetSize.INFINITE or FleetSize.FINITE
         * @return this builder
         */
        public Builder setFleetSize(FleetSize fleetSize) {
            this.fleetSize = fleetSize;
            return this;
        }

        /**
         * Adds a job which is either a service or a shipment.
         * <p>
         * <p>Note that job.getId() must be unique, i.e. no job (either it is a shipment or a service) is allowed to have an already allocated id.
         *
         * @param job job to be added
         * @return this builder
         * @throws IllegalStateException if job is neither a shipment nor a service, or jobId has already been added.
         *
         */
        public Builder addJob(Job job) {
            if (!(job instanceof AbstractJob)) {
                throw new IllegalArgumentException("job must be of type AbstractJob");
            }
            return addJob((AbstractJob) job);
        }

        /**
         * Adds a job which is either a service or a shipment.
         * <p>
         * <p>Note that job.getId() must be unique, i.e. no job (either it is a shipment or a service) is allowed to have an already allocated id.
         *
         * @param job job to be added
         * @return this builder
         * @throws IllegalStateException if job is neither a shipment nor a service, or jobId has already been added.
         */
        public Builder addJob(AbstractJob job) {
            if (tentativeJobs.containsKey(job.getId())) {
                throw new IllegalArgumentException("vehicle routing problem already contains a service or shipment with id " + job.getId() + ". make sure you use unique ids for all services and shipments");
            }
            job.setIndex(jobIndexCounter);
            incJobIndexCounter();
            tentativeJobs.put(job.getId(), job);
            addLocationToTentativeLocations(job);
            return this;
        }

        private void addLocationToTentativeLocations(Job job) {
            job.getAllLocations().forEach(l -> addLocationToTentativeLocations(l));
        }

        private void addLocationToTentativeLocations(Location location) {
            tentative_coordinates.put(location.getId(), location.getCoordinate());
            allLocations.add(location);
        }

        private void addJobToFinalJobMapAndCreateActivities(Job job) {
            addLocationToTentativeLocations(job);
            if (jobs.containsKey(job.getId())) {
                logger.warn("job " + job + " is already in job list. overrides existing job.");
            }
            jobs.put(job.getId(), job);

            job.getActivityList().getAll().forEach(act -> {
                act.setIndex(activityIndexCounter);
                incActivityIndexCounter();
            });
        }

        private boolean addBreaksToActivityMap() {
            boolean hasBreaks = false;
            for (Vehicle v : uniqueVehicles) {
                if (v.getBreak() != null) {
                    hasBreaks = true;
                    List<JobActivity> breakActivities = jobActivityFactory.createActivities(v.getBreak());
                    if(breakActivities.isEmpty()) {
                        throw new IllegalArgumentException("at least one activity for break needs to be created by activityFactory");
                    }
                    for (AbstractActivityNEW act : breakActivities) {
                        act.setIndex(activityIndexCounter);
                        incActivityIndexCounter();
                    }
                }
            }
            return hasBreaks;
        }

        /**
         * Adds an initial vehicle route.
         *
         * @param route initial route
         * @return the builder
         */
        public Builder addInitialVehicleRoute(VehicleRoute route) {
            if(!addedVehicleIds.contains(route.getVehicle().getId())){
                addVehicle((AbstractVehicle) route.getVehicle());
                addedVehicleIds.add(route.getVehicle().getId());
            }
            for (TourActivity act : route.getActivities()) {
                AbstractActivityNEW abstractAct = (AbstractActivityNEW) act;
                abstractAct.setIndex(activityIndexCounter);
                incActivityIndexCounter();
                if (act instanceof JobActivity) {
                    JobActivity jobActivity = (JobActivity) act;
                    Job job = jobActivity.getJob();
                    jobsInInitialRoutes.add(job.getId());
                    addLocationToTentativeLocations(job);
//                    registerJobAndActivity((JobActivity) abstractAct, job);
                }
            }
            initialRoutes.add(route);
            return this;
        }



//        private void registerJobAndActivity(JobActivity abstractAct, Job job) {
//            if (activityMap.containsKey(job)) {
//                activityMap.get(job).add(abstractAct);
//            } else {
//                List<JobActivity> actList = new ArrayList<>();
//                actList.add(abstractAct);
//                activityMap.put(job, actList);
//            }
//        }

        /**
         * Adds a collection of initial vehicle routes.
         *
         * @param routes initial routes
         * @return the builder
         */
        public Builder addInitialVehicleRoutes(Collection<VehicleRoute> routes) {
            for (VehicleRoute r : routes) {
                addInitialVehicleRoute(r);
            }
            return this;
        }
        /**
         * Adds a vehicle.
         *
         * @param vehicle vehicle to be added
         * @return this builder
         * */
        public Builder addVehicle(Vehicle vehicle) {
            if (!(vehicle instanceof AbstractVehicle)) {
                throw new IllegalArgumentException("vehicle must be an AbstractVehicle");
            }
            return addVehicle((AbstractVehicle) vehicle);
        }

        /**
         * Adds a vehicle.
         *
         * @param vehicle vehicle to be added
         * @return this builder
         */
        public Builder addVehicle(AbstractVehicle vehicle) {
            if(addedVehicleIds.contains(vehicle.getId())){
                throw new IllegalArgumentException("problem already contains a vehicle with id " + vehicle.getId() + ". choose unique ids for each vehicle.");
            } else {
                addedVehicleIds.add(vehicle.getId());
            }
            if (!uniqueVehicles.contains(vehicle)) {
                vehicle.setIndex(vehicleIndexCounter);
                incVehicleIndexCounter();
            }
            if (typeKeyIndices.containsKey(vehicle.getVehicleTypeIdentifier())) {
                vehicle.getVehicleTypeIdentifier().setIndex(typeKeyIndices.get(vehicle.getVehicleTypeIdentifier()));
            } else {
                vehicle.getVehicleTypeIdentifier().setIndex(vehicleTypeIdIndexCounter);
                typeKeyIndices.put(vehicle.getVehicleTypeIdentifier(), vehicleTypeIdIndexCounter);
                incVehicleTypeIdIndexCounter();
            }
            uniqueVehicles.add(vehicle);
            if (!vehicleTypes.contains(vehicle.getType())) {
                vehicleTypes.add(vehicle.getType());
            }
            String startLocationId = vehicle.getStartLocation().getId();
            addLocationToTentativeLocations(vehicle.getStartLocation());
//            tentative_coordinates.put(startLocationId, vehicle.getStartLocation().getCoordinate());
            if (!vehicle.getEndLocation().getId().equals(startLocationId)) {
                addLocationToTentativeLocations(vehicle.getEndLocation());
//                tentative_coordinates.put(vehicle.getEndLocation().getId(), vehicle.getEndLocation().getCoordinate());
            }
            return this;
        }

        private void incVehicleIndexCounter() {
            vehicleIndexCounter++;
        }

        /**
         * Sets the activity-costs.
         * <p>
         * <p>By default it is set to zero.
         *
         * @param activityCosts activity costs of the problem
         * @return this builder
         * @see VehicleRoutingActivityCosts
         */
        public Builder setActivityCosts(VehicleRoutingActivityCosts activityCosts) {
            this.activityCosts = activityCosts;
            return this;
        }

        /**
         * Builds the {@link VehicleRoutingProblem}.
         * <p>
         * <p>If {@link VehicleRoutingTransportCosts} are not set, {@link CrowFlyCosts} is used.
         *
         * @return {@link VehicleRoutingProblem}
         */
        public VehicleRoutingProblem build() {
            if (transportCosts == null) {
                transportCosts = new CrowFlyCosts(getLocations());
            }
            for (Job job : tentativeJobs.values()) {
                if (!jobsInInitialRoutes.contains(job.getId())) {
                    addJobToFinalJobMapAndCreateActivities(job);
                }
            }
            boolean hasBreaks = addBreaksToActivityMap();
            if (hasBreaks && fleetSize.equals(FleetSize.INFINITE)) {
                throw new UnsupportedOperationException("breaks are not yet supported when dealing with infinite fleet. either set it to finite or omit breaks.");
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
        public Builder addAllJobs(Collection<? extends Job> jobs) {
            for (Job j : jobs) {
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
            for (Vehicle v : vehicles) {
                addVehicle(v);
            }
            return this;
        }

        /**
         * Gets an unmodifiable collection of already added vehicles.
         *
         * @return collection of vehicles
         */
        public Collection<Vehicle> getAddedVehicles() {
            return Collections.unmodifiableCollection(uniqueVehicles);
        }

        /**
         * Gets an unmodifiable collection of already added vehicle-types.
         *
         * @return collection of vehicle-types
         */
        public Collection<VehicleType> getAddedVehicleTypes() {
            return Collections.unmodifiableCollection(vehicleTypes);
        }

        /**
         * Returns an unmodifiable collection of already added jobs.
         *
         * @return collection of jobs
         */
        public Collection<Job> getAddedJobs() {
            return Collections.unmodifiableCollection(tentativeJobs.values());
        }

    }

    /**
     * Enum that characterizes the fleet-size.
     *
     * @author sschroeder
     */
    public static enum FleetSize {
        FINITE, INFINITE
    }

    /**
     * logger logging for this class
     */
    private final static Logger logger = LoggerFactory.getLogger(VehicleRoutingProblem.class);

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

    private final Map<String, Job> allJobs;
    /**
     * Collection that contains available vehicles.
     */
    private final Collection<Vehicle> vehicles;

    /**
     * Collection that contains all available types.
     */
    private final Collection<VehicleType> vehicleTypes;


    private final Collection<VehicleRoute> initialVehicleRoutes;

    private final Collection<Location> allLocations;

    /**
     * An enum that indicates type of fleetSize. By default, it is INFINTE
     */
    private final FleetSize fleetSize;

    private final Locations locations;

    // Deprecated? Using job.getActivityList().getAll() does the same.
    //private Map<Job, List<JobActivity>> activityMap;

    private int nuActivities;

    private final JobActivityFactory jobActivityFactory = new JobActivityFactory() {

        @Override
        public List<JobActivity> createActivities(Job job) {
            return copyAndGetActivities(job);
        }

    };

    private VehicleRoutingProblem(Builder builder) {
        jobs = builder.jobs;
        fleetSize = builder.fleetSize;
        vehicles = builder.uniqueVehicles;
        vehicleTypes = builder.vehicleTypes;
        initialVehicleRoutes = builder.initialRoutes;
        transportCosts = builder.transportCosts;
        activityCosts = builder.activityCosts;
        locations = builder.getLocations();
        //activityMap = builder.activityMap;
        nuActivities = builder.activityIndexCounter;
        allLocations = builder.allLocations;
        allJobs = builder.tentativeJobs;
        logger.info("setup problem: {}", this);
    }


    @Override
    public String toString() {
        return "[fleetSize=" + fleetSize + "][#jobs=" + jobs.size() + "][#vehicles=" + vehicles.size() + "][#vehicleTypes=" + vehicleTypes.size() + "][" +
                "transportCost=" + transportCosts + "][activityCosts=" + activityCosts + "]";
    }

    /**
     * Returns type of fleetSize, either INFINITE or FINITE.
     * <p>
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

    public Map<String, Job> getJobsInclusiveInitialJobsInRoutes(){
        return Collections.unmodifiableMap(allJobs);
    }
    /**
     * Returns a copy of initial vehicle routes.
     *
     * @return copied collection of initial vehicle routes
     */
    public Collection<VehicleRoute> getInitialVehicleRoutes() {
        Collection<VehicleRoute> copiedInitialRoutes = new ArrayList<VehicleRoute>();
        for (VehicleRoute route : initialVehicleRoutes) {
            copiedInitialRoutes.add(VehicleRoute.copyOf(route));
        }
        return copiedInitialRoutes;
    }

    /**
     * Returns the entire, unmodifiable collection of types.
     *
     * @return unmodifiable collection of types
     * @see com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl
     */
    public Collection<VehicleType> getTypes() {
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
    public VehicleRoutingActivityCosts getActivityCosts() {
        return activityCosts;
    }

    public Collection<Location> getAllLocations(){
        return allLocations;
    }
    /**
     * @param job for which the corresponding activities needs to be returned
     * @return associated activities
     */
    public List<JobActivity> getActivities(Job job) {
        return job.getActivityList().getAll();
    }

//    public Map<Job,List<AbstractActivity>> getActivityMap() { return Collections.unmodifiableMap(activityMap); }

    /**
     * @return total number of activities
     */
    public int getNuActivities() {
        return nuActivities;
    }

    /**
     * @return factory that creates the activities associated to a job
     */
    public JobActivityFactory getJobActivityFactory() {
        return jobActivityFactory;
    }

    /**
     * @param job for which the corresponding activities needs to be returned
     * @return a copy of the activities that are associated to the specified job
     */
    public List<JobActivity> copyAndGetActivities(Job job) {
        return job.getActivityList().getAllDuplicated();
    }

}
