package com.graphhopper.jsprit.core.algorithm.ruin;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.util.Coordinate;

import java.util.*;

import static com.graphhopper.jsprit.core.algorithm.recreate.GreedyInsertionByAverage.getJobLocation;
import static com.graphhopper.jsprit.core.algorithm.recreate.GreedyInsertionByAverage.getRouteCenter;

public class RuinFarthest extends AbstractRuinStrategy {
    private final double percentileOfFreeTimeRouteToBeRemoved;
    private final double percentileOfFreeTimeRouteToBeRemovedFinalStep;

    public RuinFarthest(VehicleRoutingProblem vrp, double percentileOfFreeTimeRouteToBeRemoved, double percentileOfFreeTimeRouteToBeRemovedFinalStep) {
        super(vrp);
        this.percentileOfFreeTimeRouteToBeRemoved = percentileOfFreeTimeRouteToBeRemoved;
        this.percentileOfFreeTimeRouteToBeRemovedFinalStep = percentileOfFreeTimeRouteToBeRemovedFinalStep;
    }

    @Override
    public Collection<Job> ruinRoutes(Collection<VehicleRoute> vehicleRoutes) {
        int nOfJobs2BeRemoved = getRuinShareFactory().createNumberToBeRemoved();
        return ruin(new ArrayList<>(vehicleRoutes), nOfJobs2BeRemoved);
    }

    private List<Job> ruin(List<VehicleRoute> vehicleRoutes, int nOfJobs2BeRemoved) {
        List<Job> unassignedJobs = new ArrayList<>();
        nOfJobs2BeRemoved = removeRoutesWithFreeTimesExceedingMaxRatio(vehicleRoutes, nOfJobs2BeRemoved, unassignedJobs, false);

        final Map<Job, VehicleRoute> jobToRoute = new HashMap<>();
        final Map<Job, Double> distanceFromCenter = getDistanceFromRouteCenter(vehicleRoutes, jobToRoute);
        ArrayList<Job> jobs = new ArrayList<>(distanceFromCenter.keySet());
        Collections.sort(jobs, new Comparator<Job>() {
            @Override
            public int compare(Job job1, Job job2) {
                return Double.compare(distanceFromCenter.get(job2), distanceFromCenter.get(job1));
            }
        });

        for (int i = 0 ; i < jobs.size() && nOfJobs2BeRemoved > 0; ++i, --nOfJobs2BeRemoved) {
            Job job = jobs.get(i);
            if (removeJob(job, jobToRoute.get(job)))
                unassignedJobs.add(job);
        }
        removeRoutesWithFreeTimesExceedingMaxRatio(vehicleRoutes, nOfJobs2BeRemoved, unassignedJobs, true);
        return unassignedJobs;
    }

    private Map<Job, Double> getDistanceFromRouteCenter(List<VehicleRoute> vehicleRoutes, Map<Job, VehicleRoute> jobToRoute) {
        final Map<Job, Double> distanceFromCenter = new HashMap<>();
        for (VehicleRoute route : vehicleRoutes) {
            Coordinate routeCenter = getRouteCenter(route.getTourActivities().getJobs());
            if (routeCenter == null)
                continue;

            Location centerLocation = Location.newInstance(routeCenter.getX(), routeCenter.getY());
            for (Job job : route.getTourActivities().getJobs()) {
                Location jobLocation = getJobLocation(job);
                if (jobLocation != null) {
                    distanceFromCenter.put(job, vrp.getTransportCosts().getDistance(jobLocation, centerLocation, route.getDepartureTime(), route.getVehicle()));
                    jobToRoute.put(job, route);
                }
            }
        }
        return distanceFromCenter;
    }

    private int removeRoutesWithFreeTimesExceedingMaxRatio(List<VehicleRoute> vehicleRoutes, int nOfJobs2BeRemoved, List<Job> unassignedJobs, boolean force) {
        if (vehicleRoutes.size() > 1) {
            final Map<VehicleRoute, Double> idleTimes = getRouteIdleTimes(vehicleRoutes);
            ArrayList<VehicleRoute> vehicleRoutesToRemove = getVehicleRoutesToBeRemoved(vehicleRoutes, idleTimes, force ? percentileOfFreeTimeRouteToBeRemovedFinalStep : percentileOfFreeTimeRouteToBeRemoved);
            for (VehicleRoute route : vehicleRoutesToRemove) {
                if (withJobsInInitialRoutes(route))
                    continue;

                Collection<Job> jobs = new HashSet<>(route.getTourActivities().getJobs());
                if (force || jobs.size() >= nOfJobs2BeRemoved) {
                    nOfJobs2BeRemoved -= jobs.size();

                    for (Job job : jobs) {
                        if (removeJob(job, vehicleRoutes))
                            unassignedJobs.add(job);
                    }
                }
            }
        }
        return nOfJobs2BeRemoved;
    }

    private boolean withJobsInInitialRoutes(VehicleRoute route) {
        for (Job job : route.getTourActivities().getJobs())
            if (jobIsInitial(job))
                return true;
        return false;
    }

    private ArrayList<VehicleRoute> getVehicleRoutesToBeRemoved(List<VehicleRoute> vehicleRoutes, final Map<VehicleRoute, Double> freeTimes, double percentileOfFreeTimeRouteToBeRemoved) {
        Collections.sort(vehicleRoutes, new Comparator<VehicleRoute>() {
            @Override
            public int compare(VehicleRoute route1, VehicleRoute route2) {
                return Double.compare(freeTimes.get(route2), freeTimes.get(route1));
            }
        });
        ArrayList<VehicleRoute> vehicleRoutesToRemove = new ArrayList<>();
        for (int i = 0; i < vehicleRoutes.size() && freeTimes.get(vehicleRoutes.get(i)) >= percentileOfFreeTimeRouteToBeRemoved; ++i) {
            vehicleRoutesToRemove.add(vehicleRoutes.get(i));
        }
        return vehicleRoutesToRemove;
    }

    private Map<VehicleRoute, Double> getRouteIdleTimes(List<VehicleRoute> vehicleRoutes) {
        final Map<VehicleRoute, Double> freeTimes = new HashMap<>();
        for (VehicleRoute route : vehicleRoutes) {
            double operationTime = route.getEnd().getArrTime() - route.getStart().getEndTime();
            double workingTime = route.getVehicle().getLatestArrival() - route.getVehicle().getEarliestDeparture();
            double waitingTime = 0;
            for (int i = 0; i < route.getActivities().size(); ++i) {
                TourActivity act = route.getActivities().get(i);
                waitingTime += Math.max(0, act.getTheoreticalEarliestOperationStartTime() - act.getArrTime());
            }
            freeTimes.put(route, (workingTime - operationTime + waitingTime) / workingTime);
        }
        return freeTimes;
    }
}
