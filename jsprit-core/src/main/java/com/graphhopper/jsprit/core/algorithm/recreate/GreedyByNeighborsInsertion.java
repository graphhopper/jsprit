package com.graphhopper.jsprit.core.algorithm.recreate;

import com.graphhopper.jsprit.core.algorithm.ruin.JobNeighborhoods;
import com.graphhopper.jsprit.core.algorithm.ruin.JobNeighborhoodsFactory;
import com.graphhopper.jsprit.core.algorithm.ruin.distance.AvgServiceAndShipmentDistance;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class GreedyByNeighborsInsertion extends GreedyInsertion {
    final double distanceDiffForSameLocation;
    final double ratioToSort;
    private static Logger logger = LoggerFactory.getLogger(GreedyByNeighborsInsertion.class);

    Map<String, Collection<Job>> jobsThaHaveToBeInSameRoute = new HashMap<>();

    public GreedyByNeighborsInsertion(JobInsertionCostsCalculator jobInsertionCalculator, VehicleRoutingProblem vehicleRoutingProblem, double distanceDiffForSameLocationMeter) {
        this(jobInsertionCalculator, vehicleRoutingProblem, distanceDiffForSameLocationMeter, 0);
    }

    public GreedyByNeighborsInsertion(JobInsertionCostsCalculator jobInsertionCalculator, VehicleRoutingProblem vehicleRoutingProblem, double distanceDiffForSameLocationMeter, double ratioToSort) {
        super(jobInsertionCalculator, vehicleRoutingProblem);
        this.distanceDiffForSameLocation = distanceDiffForSameLocationMeter;
        this.ratioToSort = ratioToSort;
        initializeNeighbors();
    }


    @Override
    public String toString() {
        return "[name=greedyByNeighborhoodInsertion]";
    }

    Map<String, Collection<Job>> initializeNeighbors() {
        JobNeighborhoods neighborhoods = new JobNeighborhoodsFactory().createNeighborhoods(vrp, new AvgServiceAndShipmentDistance(vrp.getTransportCosts()));
        neighborhoods.initialise();
        for (Job job : vrp.getJobs().values()) {
            Location location = getLocation(job);
            Iterator<Job> nearestNeighborsIterator = neighborhoods.getNearestNeighborsIterator(vrp.getJobs().size(), job);
            HashSet<Job> nearestJobs = new HashSet<>();
            while (nearestNeighborsIterator.hasNext()) {
                Job next = nearestNeighborsIterator.next();
                if (distanceDiffForSameLocation >= vrp.getTransportCosts().getDistance(location, getLocation(next), 0, VehicleImpl.createNoVehicle()))
                    nearestJobs.add(next);
                else break;
            }
            jobsThaHaveToBeInSameRoute.put(job.getId(), nearestJobs);
        }
        return jobsThaHaveToBeInSameRoute;
    }

    @Override
    public Collection<Job> insertUnassignedJobs(Collection<VehicleRoute> vehicleRoutes, Collection<Job> unassignedJobs) {
        final List<Job> jobsToInsert = new ArrayList<>(unassignedJobs);
        Set<Job> failedToAssign = new HashSet<>(insertBreaks(vehicleRoutes, jobsToInsert));
        final Map<String, Integer> nearestUnassigned = new HashMap<>();
        for (Job job : unassignedJobs)
            nearestUnassigned.put(job.getId(), getNumberOfNearestUnassigned(job, jobsToInsert));

        Comparator<Job> withMostNeighborsComparator = new Comparator<Job>() {
            @Override
            public int compare(Job job1, Job job2) {
                return Double.compare(nearestUnassigned.get(job2.getId()), nearestUnassigned.get(job1.getId()));
            }
        };
        try {
            Collections.shuffle(jobsToInsert);
            if (random.nextDouble() <= ratioToSort) {
                Collections.sort(jobsToInsert, withMostNeighborsComparator);
            }
        } catch (Exception e) {
            logger.error("failed to sort", e);
        }
        while (!jobsToInsert.isEmpty()) {
            Job withMostNeighbors = jobsToInsert.remove(0);
            failedToAssign.addAll(insertJobWithNearest(vehicleRoutes, withMostNeighbors, jobsToInsert));
        }
        return failedToAssign;
    }

    private int getNumberOfNearestUnassigned(Job job, List<Job> jobsToInsert) {
        if (!jobsThaHaveToBeInSameRoute.containsKey(job.getId()))
            return 0;

        HashSet<Job> toInsert = new HashSet<>(jobsToInsert);
        toInsert.removeAll(jobsThaHaveToBeInSameRoute.get(job.getId()));
        return jobsToInsert.size() - toInsert.size();
    }

    private Collection<Job> insertJobWithNearest(Collection<VehicleRoute> vehicleRoutes, Job withMostNeighbors, List<Job> jobsToInsert) {
        List<Job> jobs = new ArrayList<>();
        jobs.add(withMostNeighbors);
        Collection<Job> failedToInsert = super.insertUnassignedJobs(vehicleRoutes, jobs);
        if (!failedToInsert.isEmpty())
            return failedToInsert;

        VehicleRoute route = findRouteThatServesJob(vehicleRoutes, withMostNeighbors);
        if (route != null && jobsThaHaveToBeInSameRoute.containsKey(withMostNeighbors.getId())) {
            Collection<Job> jobCollection = jobsThaHaveToBeInSameRoute.get(withMostNeighbors.getId());
            for (Job job : jobCollection) {
                if (jobsToInsert.contains(job)) {
                    InsertionData iData = bestInsertionCalculator.getInsertionData(route, job, route.getVehicle(), route.getDepartureTime(), route.getDriver(), Double.MAX_VALUE);
                    if (!(iData instanceof InsertionData.NoInsertionFound)) {
                        super.insertJob(job, iData, route);
                        jobsToInsert.remove(job);
                    }
                }
            }
        } else {
            logger.error("this should not happen route {} jobsThaHavToBeInSameRoute contains key {}", route, jobsThaHaveToBeInSameRoute.containsKey(withMostNeighbors.getId()));
        }

        return failedToInsert;
    }
}
