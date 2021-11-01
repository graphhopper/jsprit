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
        for (Job other : vrp.getJobs().values()) {
            final Location otherLocation = getLocation(other);
            if (location.equals(otherLocation.getZipCode()))
                nearestJobs.add(other);
        }
        nearestJobs.addAll(super.getNearestJobs(neighborhoods, job));
        return nearestJobs;
    }

    @Override
    public String toString() {
        return "[name=greedyByZipCodeInsertion]";
    }

}
