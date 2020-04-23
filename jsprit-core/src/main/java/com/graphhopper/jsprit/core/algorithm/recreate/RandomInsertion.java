package com.graphhopper.jsprit.core.algorithm.recreate;

import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.job.Shipment;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class RandomInsertion extends AbstractInsertionStrategy {
    private static Logger logger = LoggerFactory.getLogger(BestInsertion.class);

    private JobInsertionCostsCalculator bestInsertionCostCalculator;
    final Map<String, Integer> jobCanBeServedByDriversCount = new HashMap<String, Integer>() {
        @Override
        public Integer get(Object key) {
            if (!super.containsKey(key)) {
                return 1;
            }
            return super.get(key);
        }
    };

    public RandomInsertion(JobInsertionCostsCalculator jobInsertionCalculator, VehicleRoutingProblem vehicleRoutingProblem) {
        super(vehicleRoutingProblem);
        bestInsertionCostCalculator = jobInsertionCalculator;

        initJobsCanBeServedByNumDrivers();
        logger.debug("initialise {}", this);
    }

    void initJobsCanBeServedByNumDrivers() {
        for (Job job : vrp.getJobs().values()) {
            int count = 0;
            for (Vehicle vehicle : vrp.getInitialVehicles()) {
                if (cabBeServedByVehicle(job, vehicle))
                    count++;
            }

            jobCanBeServedByDriversCount.put(job.getId(), count);
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

        sortJobs(unassignedJobList);

        for (Job unassignedJob : unassignedJobList) {
            List<VehicleRoute> routes = new ArrayList<>(vehicleRoutes);
            Collections.shuffle(routes, random);
            final VehicleRoute newRoute = VehicleRoute.emptyRoute();
            routes.add(newRoute);
            InsertionData empty = new InsertionData.NoInsertionFound();
            double bestInsertionCost = Double.MAX_VALUE;
            boolean inserted = false;
            for (VehicleRoute vehicleRoute : routes) {
                InsertionData iData;
                final boolean isNewRoute = vehicleRoute.equals(newRoute);
                if (isNewRoute)
                    iData = bestInsertionCostCalculator.getInsertionData(vehicleRoute, unassignedJob, NO_NEW_VEHICLE_YET, NO_NEW_DEPARTURE_TIME_YET, NO_NEW_DRIVER_YET, bestInsertionCost);
                else iData = bestInsertionCostCalculator.getInsertionData(vehicleRoute, unassignedJob, vehicleRoute.getVehicle(), vehicleRoute.getDepartureTime(), vehicleRoute.getDriver(), bestInsertionCost);
                if (iData instanceof InsertionData.NoInsertionFound) {
                    empty.getFailedConstraintNames().addAll(iData.getFailedConstraintNames());
                    continue;
                }

                inserted = true;
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

    void sortJobs(List<Job> unassignedJobList) {
        final double p = random.nextDouble();
        if (p < .25)
            Collections.sort(unassignedJobList, new AccordingToPriorities());
        else if (p < .75)
            Collections.sort(unassignedJobList, new Comparator<Job>() {
                @Override
                public int compare(Job o1, Job o2) {return jobCanBeServedByDriversCount.get(o1.getId()) - jobCanBeServedByDriversCount.get(o2.getId());}
            });
    }

    protected static boolean inTimeWindow(Job job, double earliestDeparture, double latestArrival) {
        if (job instanceof Service) {
            return inTimeWindow(((Service) job).getTimeWindows(), earliestDeparture, latestArrival);
        } else if (job instanceof Shipment) {
            Shipment shipment = (Shipment) job;
            return inTimeWindow(shipment.getDeliveryTimeWindows(), earliestDeparture, latestArrival) && inTimeWindow(shipment.getPickupTimeWindows(), earliestDeparture, latestArrival);
        }
        return true;
    }

    private static boolean cabBeServedByVehicle(Job job, Vehicle vehicle) {
        return inTimeWindow(job, vehicle.getEarliestDeparture(), vehicle.getLatestArrival()) && vehicle.getSkills().values().containsAll(job.getRequiredSkills().values()) && vehicle.isTaskPermited(job.getId());
    }

    private static boolean inTimeWindow(Collection<TimeWindow> timeWindows, double earliestDeparture, double latestArrival) {
        for (TimeWindow timeWindow : timeWindows) {
            if (timeWindow.getStart() < latestArrival && timeWindow.getEnd() > earliestDeparture)
                return true;
        }
        return false;
    }

}
