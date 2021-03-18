package com.graphhopper.jsprit.core.algorithm.recreate;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Break;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.job.Shipment;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;

import java.util.*;

public class GreedyInsertion extends RegretInsertion {
    protected final JobInsertionCostsCalculator bestInsertionCalculator;

    public GreedyInsertion(JobInsertionCostsCalculator jobInsertionCalculator, VehicleRoutingProblem vehicleRoutingProblem) {
        super(jobInsertionCalculator, vehicleRoutingProblem);
        this.bestInsertionCalculator = jobInsertionCalculator;
    }


    protected static VehicleRoute findRouteThatServesJob(Collection<VehicleRoute> routes, Job job) {
        for (VehicleRoute r : routes) {
            if (r.getTourActivities().servesJob(job))
                return r;
        }
        return null;
    }

    protected final static Location getLocation(Job job) {
        if (job instanceof Service)
            return ((Service) job).getLocation();
        if (job instanceof Shipment)
            return ((Shipment) job).getDeliveryLocation();
        return null;
    }

    protected final Collection<Job> insertBreaks(Collection<VehicleRoute> routes, Collection<Job> unassignedJobs) {
        List<Job> badJobs = new ArrayList<>(unassignedJobs.size());
        Iterator<Job> jobIterator = unassignedJobs.iterator();
        while (jobIterator.hasNext()){
            Job job = jobIterator.next();
            if(job instanceof Break) {
                VehicleRoute route = findRoute(routes, job);
                if(route != null) {
                    InsertionData iData = bestInsertionCalculator.getInsertionData(route, job, NO_NEW_VEHICLE_YET, NO_NEW_DEPARTURE_TIME_YET, NO_NEW_DRIVER_YET, Double.MAX_VALUE);
                    if (iData instanceof InsertionData.NoInsertionFound) {
                        badJobs.add(job);
                    } else {
                        insertJob(job, iData, route);
                    }
                }
                jobIterator.remove();
            }
        }
        return badJobs;
    }
}
