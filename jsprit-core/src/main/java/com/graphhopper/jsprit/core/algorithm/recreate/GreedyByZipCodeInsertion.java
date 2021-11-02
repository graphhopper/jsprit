package com.graphhopper.jsprit.core.algorithm.recreate;

import com.graphhopper.jsprit.core.algorithm.ruin.JobNeighborhoods;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Job;

import java.util.*;

public class GreedyByZipCodeInsertion extends GreedyByNeighborsInsertion {
    public GreedyByZipCodeInsertion(JobInsertionCostsCalculator jobInsertionCalculator, VehicleRoutingProblem vehicleRoutingProblem, double distanceDiffForSameLocationMeter, double ratioToSort) {
        super(jobInsertionCalculator, vehicleRoutingProblem, distanceDiffForSameLocationMeter, ratioToSort);
    }

    @Override
    List<Job> getNearestJobs(JobNeighborhoods neighborhoods, Job job) {
        final Location location = getLocation(job);
        if (location.getZipCode() == null)
            return super.getNearestJobs(neighborhoods, job);

        final List<Job> nearestJobs = new ArrayList<>();
        final Map<Job, Double> transportTimes = new HashMap<>();
        for (Job other : vrp.getJobs().values()) {
            final Location otherLocation = getLocation(other);
            if (location.getZipCode().equals(otherLocation.getZipCode())) {
                transportTimes.put(other, vrp.getTransportCosts().getTransportTime(location, otherLocation, 0, NO_NEW_DRIVER_YET, NO_NEW_VEHICLE_YET));
                nearestJobs.add(other);
            }
        }
        Collections.sort(nearestJobs,  new Comparator<Job>() {
            @Override
            public int compare(Job job1, Job job2) {
                return Double.compare(transportTimes.get(job1), transportTimes.get(job2));
            }
        });

        List<Job> moreJobs = super.getNearestJobs(neighborhoods, job);
        for (Job other : moreJobs)
            if (!nearestJobs.contains(other))
                nearestJobs.add(other);

        return nearestJobs;
    }

    @Override
    public String toString() {
        return "[name=greedyByZipCodeInsertion]";
    }

}
