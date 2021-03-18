package com.graphhopper.jsprit.core.algorithm.recreate;

import com.graphhopper.jsprit.core.algorithm.ruin.JobNeighborhoods;
import com.graphhopper.jsprit.core.algorithm.ruin.JobNeighborhoodsFactory;
import com.graphhopper.jsprit.core.algorithm.ruin.distance.AvgServiceAndShipmentDistance;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import com.graphhopper.jsprit.core.problem.job.Break;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.vehicle.*;
import com.graphhopper.jsprit.core.util.Coordinate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class GreedyInsertionByDistance extends GreedyInsertion {
    private static Logger logger = LoggerFactory.getLogger(GreedyInsertionByDistance.class);

    private final VehicleFleetManager fleetManager;
    Map<Coordinate, List<Job>> nearestJobByVehicleStartLocation = new HashMap<>();
    private final JobNeighborhoods neighborhoods;
    private final int maxJobs;

    public GreedyInsertionByDistance(JobInsertionCostsCalculator jobInsertionCalculator, VehicleRoutingProblem vehicleRoutingProblem, VehicleFleetManager fleetManager) {
        super(jobInsertionCalculator, vehicleRoutingProblem);
        this.fleetManager = fleetManager;
        this.maxJobs = vehicleRoutingProblem.getJobsInclusiveInitialJobsInRoutes().size();
        neighborhoods = new JobNeighborhoodsFactory().createNeighborhoods(vehicleRoutingProblem, new AvgServiceAndShipmentDistance(vehicleRoutingProblem.getTransportCosts()));
        neighborhoods.initialise();
        initialize(vehicleRoutingProblem);
    }

    GreedyInsertionByDistance(JobInsertionCostsCalculator jobInsertionCalculator, VehicleRoutingProblem vehicleRoutingProblem) {
        this(jobInsertionCalculator, vehicleRoutingProblem, new FiniteFleetManagerFactory(vehicleRoutingProblem.getVehicles()).createFleetManager());
    }


    @Override
    public String toString() {
        return "[name=greedyByDistanceFromDepotInsertion]";
    }

    void initialize(VehicleRoutingProblem vehicleRoutingProblem) {
        final VehicleRoutingTransportCosts transportCosts = vehicleRoutingProblem.getTransportCosts();
        for (final Vehicle vehicle : vehicleRoutingProblem.getVehicles()) {
            if (nearestJobByVehicleStartLocation.containsKey(vehicle.getStartLocation().getCoordinate()))
                continue;

            final Map<String, Double> routingTimes = new HashMap<>();
            for (Job job : vehicleRoutingProblem.getJobsInclusiveInitialJobsInRoutes().values()) {
                routingTimes.put(job.getId(), transportCosts.getDistance(vehicle.getStartLocation(), getLocation(job), vehicle.getEarliestDeparture(), vehicle));
            }

            final Comparator<Job> comparator = new Comparator<Job>() {
                @Override
                public int compare(Job job1, Job job2) {
                    return Double.compare(routingTimes.get(job1.getId()), routingTimes.get(job2.getId()));
                }
            };
            ArrayList<Job> jobs = new ArrayList<>(vehicleRoutingProblem.getJobsInclusiveInitialJobsInRoutes().values());
            try {
                Collections.sort(jobs, comparator);
            } catch (Exception e) {
                logger.error("failed to sort jobs", e);
            }
            nearestJobByVehicleStartLocation.put(vehicle.getStartLocation().getCoordinate(), jobs);
        }
    }

    @Override
    public Collection<Job> insertUnassignedJobs(Collection<VehicleRoute> vehicleRoutes, Collection<Job> unassignedJobs) {
        List<Job> jobsToInsert = new ArrayList<>(unassignedJobs);
        Set<Job> failedToAssign = new HashSet<>(insertBreaks(vehicleRoutes, jobsToInsert));
        List<VehicleRoute> openRoutes = new ArrayList<>(vehicleRoutes);

        while (!jobsToInsert.isEmpty()) {
            if (openRoutes.isEmpty()) {
                List<Vehicle> availableVehicles = new ArrayList<>(fleetManager.getAvailableVehicles());
                if (availableVehicles.isEmpty()) {
                    failedToAssign.addAll(jobsToInsert);
                    return failedToAssign;
                }

                Vehicle nextVehicle = availableVehicles.get(random.nextInt(availableVehicles.size()));
                fleetManager.lock(nextVehicle);
                VehicleRoute newRoute = VehicleRoute.Builder.newInstance(nextVehicle).build();
                openRoutes.add(newRoute);
                vehicleRoutes.add(newRoute);
            }

            VehicleRoute nextRoute = openRoutes.get(random.nextInt(openRoutes.size()));
            Job nearestJob;
            if (nextRoute.isEmpty() || routeWithBreakOnly(nextRoute)) {
                Iterator<Job> nearestJobsIter = nearestJobByVehicleStartLocation.get(nextRoute.getVehicle().getStartLocation().getCoordinate()).iterator();
                nearestJob = nearestJobsIter.next();
                while (!jobsToInsert.contains(nearestJob) && nearestJobsIter.hasNext()) {
                    nearestJob = nearestJobsIter.next();
                }
            } else {
                List<Job> routeJobs = new ArrayList<>(nextRoute.getTourActivities().getJobs());
                do {
                    nearestJob = routeJobs.get(random.nextInt(routeJobs.size()));
                } while (nearestJob instanceof Break);
            }
            insertJobWithNearest(openRoutes, nextRoute, nearestJob, jobsToInsert);
        }
        return failedToAssign;
    }

    private boolean routeWithBreakOnly(VehicleRoute nextRoute) {
        return nextRoute.getTourActivities().getJobs().size() == 1 && nextRoute.getTourActivities().getJobs().iterator().next() instanceof Break;
    }

    private void insertJobWithNearest(Collection<VehicleRoute> openRoutes, VehicleRoute route, Job jobToInsert, List<Job> jobsToInsert) {
        if (jobsToInsert.contains(jobToInsert)) {
            InsertionData iData = bestInsertionCalculator.getInsertionData(route, jobToInsert, route.getVehicle(), NO_NEW_DEPARTURE_TIME_YET, NO_NEW_DRIVER_YET, Double.MAX_VALUE);
            if (!(iData instanceof InsertionData.NoInsertionFound)) {
                super.insertJob(jobToInsert, iData, route);
                jobsToInsert.remove(jobToInsert);
            }
        }

        Iterator<Job> jobNeighborsIterator = neighborhoods.getNearestNeighborsIterator(maxJobs, jobToInsert);
        while (jobNeighborsIterator.hasNext()) {
            Job job = jobNeighborsIterator.next();
            if (jobsToInsert.contains(job)) {
                InsertionData iData = bestInsertionCalculator.getInsertionData(route, job, route.getVehicle(), NO_NEW_DEPARTURE_TIME_YET, NO_NEW_DRIVER_YET, Double.MAX_VALUE);
                if (!(iData instanceof InsertionData.NoInsertionFound)) {
                    super.insertJob(job, iData, route);
                    jobsToInsert.remove(job);
                }
            }
        }
        openRoutes.remove(route);
    }
}
