package com.graphhopper.jsprit.core.algorithm.recreate;

import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Break;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class RandomInsertion extends AbstractInsertionStrategy {
    private static Logger logger = LoggerFactory.getLogger(BestInsertion.class);

    private JobInsertionCostsCalculator bestInsertionCostCalculator;
    final Map<String, Integer> jobCanBeServedByDriversCount = new HashMap<>();

    public RandomInsertion(JobInsertionCostsCalculator jobInsertionCalculator, VehicleRoutingProblem vehicleRoutingProblem) {
        super(vehicleRoutingProblem);
        bestInsertionCostCalculator = jobInsertionCalculator;

        initJobsCanBeServedByNumDrivers();
        logger.debug("initialise {}", this);
    }

    void initJobsCanBeServedByNumDrivers() {
        for (Job job : vrp.getJobs().values()) {
            int count = 0;
            for (Vehicle vehicle : vrp.getVehicles())
                if (vehicle.getSkills().values().containsAll(job.getRequiredSkills().values()))
                    count++;

            jobCanBeServedByDriversCount.put(job.getId(), count);
        }

        for (Vehicle vehicle : vrp.getVehicles()) {
            final Break aBreak = vehicle.getBreak();
            if (aBreak != null)
                jobCanBeServedByDriversCount.put(aBreak.getId(), 1);
        }
    }

    @Override
    public String toString() {
        return "[name=randomInsertion]";
    }

    @Override
    public Collection<Job> insertUnassignedJobs(Collection<VehicleRoute> vehicleRoutes, Collection<Job> unassignedJobs) {
        List<Job> badJobs = new ArrayList<>(unassignedJobs.size());
        List<Job> unassignedJobList = new ArrayList<>(unassignedJobs);
        Collections.shuffle(unassignedJobList, random);

        final double p = random.nextDouble();
        if (p < .25)
            Collections.sort(unassignedJobList, new AccordingToPriorities());
        else if (p < .5)
            Collections.sort(unassignedJobList, new Comparator<Job>() {
                @Override
                public int compare(Job o1, Job o2) {return jobCanBeServedByDriversCount.get(o1.getId()) - jobCanBeServedByDriversCount.get(o2.getId());}
            });

        for (Job unassignedJob : unassignedJobList) {
            List<VehicleRoute> routes = new ArrayList<>(vehicleRoutes);
            final VehicleRoute newRoute = VehicleRoute.emptyRoute();
            routes.add(newRoute);
            Collections.shuffle(routes, random);

            InsertionData empty = new InsertionData.NoInsertionFound();
            double bestInsertionCost = Double.MAX_VALUE;
            boolean inserted = false;
            for (VehicleRoute vehicleRoute : routes) {
                InsertionData iData = bestInsertionCostCalculator.getInsertionData(vehicleRoute, unassignedJob, NO_NEW_VEHICLE_YET, NO_NEW_DEPARTURE_TIME_YET, NO_NEW_DRIVER_YET, bestInsertionCost);
                if (iData instanceof InsertionData.NoInsertionFound) {
                    empty.getFailedConstraintNames().addAll(iData.getFailedConstraintNames());
                    continue;
                }

                inserted = true;
                final boolean isNewRoute = vehicleRoute.getActivities().size() == 0;
                if (isNewRoute) {
                    updateNewRouteInsertionData(iData);
                    vehicleRoutes.add(vehicleRoute);
                }

                final boolean vehicleSwitched = !vehicleRoute.getVehicle().getId().equals(iData.getSelectedVehicle().getId());
                insertJob(unassignedJob, iData, vehicleRoute);
                if (vehicleSwitched)
                    insertBreak(bestInsertionCostCalculator, badJobs, vehicleRoute, iData);
                break;
            }

            if (!inserted) {
                markUnassigned(unassignedJob, empty.getFailedConstraintNames());
                badJobs.add(unassignedJob);
            }
        }
        return badJobs;
    }

}
