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
    protected final boolean removeIdleRoutesFinalStep;

    public RuinFarthest(VehicleRoutingProblem vrp, double percentileOfFreeTimeRouteToBeRemoved, double percentileOfFreeTimeRouteToBeRemovedFinalStep) {
        this(vrp, percentileOfFreeTimeRouteToBeRemoved, percentileOfFreeTimeRouteToBeRemovedFinalStep, true);
    }

    public RuinFarthest(VehicleRoutingProblem vrp, double percentileOfFreeTimeRouteToBeRemoved, double percentileOfFreeTimeRouteToBeRemovedFinalStep, boolean removeIdleRoutesFinalStep) {
        super(vrp);
        this.percentileOfFreeTimeRouteToBeRemoved = percentileOfFreeTimeRouteToBeRemoved;
        this.percentileOfFreeTimeRouteToBeRemovedFinalStep = percentileOfFreeTimeRouteToBeRemovedFinalStep;
        this.removeIdleRoutesFinalStep = removeIdleRoutesFinalStep;
    }

    @Override
    public Collection<Job> ruinRoutes(Collection<VehicleRoute> vehicleRoutes) {
        int nOfJobs2BeRemoved = getRuinShareFactory().createNumberToBeRemoved();
        return ruin(new ArrayList<>(vehicleRoutes), nOfJobs2BeRemoved);
    }

    private List<Job> ruin(List<VehicleRoute> vehicleRoutes, int nOfJobs2BeRemoved) {
        List<Job> unassignedJobs = new ArrayList<>();
        nOfJobs2BeRemoved = removeRoutesWithFreeTimesExceedingMaxRatio(vehicleRoutes, nOfJobs2BeRemoved, unassignedJobs, false);

        final Map<Job, VehicleRoute> jobToRoute = getJobRoutes(vehicleRoutes);
        ArrayList<Job> jobs = new ArrayList<>(jobToRoute.keySet());

        nOfJobs2BeRemoved = removeJobsFromRoutes(vehicleRoutes, nOfJobs2BeRemoved, unassignedJobs, jobToRoute, jobs);
        removeRoutesWithFreeTimesExceedingMaxRatio(vehicleRoutes, nOfJobs2BeRemoved, unassignedJobs, removeIdleRoutesFinalStep);
        return unassignedJobs;
    }

    private Map<Job, VehicleRoute> getJobRoutes(List<VehicleRoute> vehicleRoutes) {
        Map<Job, VehicleRoute> jobToRoutes = new HashMap<>();
        for (VehicleRoute route : vehicleRoutes)
            for (Job job : route.getTourActivities().getJobs())
                jobToRoutes.put(job, route);
        return jobToRoutes;
    }

    protected int removeJobsFromRoutes(List<VehicleRoute> vehicleRoutes, int nOfJobs2BeRemoved, List<Job> unassignedJobs, Map<Job, VehicleRoute> jobToRoute, ArrayList<Job> jobs) {
        final Map<Job, Double> distanceFromCenter = getDistanceFromRouteCenter(vehicleRoutes, jobToRoute);
        Collections.sort(jobs, new Comparator<Job>() {
            @Override
            public int compare(Job job1, Job job2) {
                return Double.compare(distanceFromCenter.get(job2), distanceFromCenter.get(job1));
            }
        });

        while (nOfJobs2BeRemoved > 0 && !jobs.isEmpty()) {
            Job job = jobs.remove(0);
            if (removeJob(job, jobToRoute.get(job))) {
                unassignedJobs.add(job);
                nOfJobs2BeRemoved--;
                distanceFromCenter.putAll(getRouteJobDistancesFromCenter(jobToRoute, jobToRoute.get(job)));
                Collections.sort(jobs, new Comparator<Job>() {
                    @Override
                    public int compare(Job job1, Job job2) {
                        return Double.compare(distanceFromCenter.get(job2), distanceFromCenter.get(job1));
                    }
                });
            }
        }
        return nOfJobs2BeRemoved;
    }

    private Map<Job, Double> getDistanceFromRouteCenter(List<VehicleRoute> vehicleRoutes, Map<Job, VehicleRoute> jobToRoute) {
        final Map<Job, Double> distanceFromCenter = new HashMap<>();
        for (VehicleRoute route : vehicleRoutes) {
            distanceFromCenter.putAll(getRouteJobDistancesFromCenter(jobToRoute, route));
        }
        return distanceFromCenter;
    }

    private Map<Job, Double> getRouteJobDistancesFromCenter(Map<Job, VehicleRoute> jobToRoute, VehicleRoute route) {
        Map<Job, Double> distanceFromCenter = new HashMap<>();
        Coordinate routeCenter = getRouteCenter(route.getTourActivities().getJobs());
        if (routeCenter == null)
            return distanceFromCenter;

        Location centerLocation = Location.newInstance(routeCenter.getX(), routeCenter.getY());
        for (Job job : route.getTourActivities().getJobs()) {
            Location jobLocation = getJobLocation(job);
            if (jobLocation != null) {
                distanceFromCenter.put(job, vrp.getTransportCosts().getDistance(jobLocation, centerLocation, route.getDepartureTime(), route.getVehicle()));
                jobToRoute.put(job, route);
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
                if (force || jobs.size() <= nOfJobs2BeRemoved) {
                    for (Job job : jobs) {
                        if (removeJob(job, vehicleRoutes)) {
                            unassignedJobs.add(job);
                            --nOfJobs2BeRemoved;
                        }
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
