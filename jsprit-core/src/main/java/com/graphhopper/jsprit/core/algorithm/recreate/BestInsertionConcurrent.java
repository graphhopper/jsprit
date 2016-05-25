/*******************************************************************************
 * Copyright (C) 2013  Stefan Schroeder
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package com.graphhopper.jsprit.core.algorithm.recreate;

import com.graphhopper.jsprit.core.algorithm.recreate.InsertionData.NoInsertionFound;
import com.graphhopper.jsprit.core.algorithm.recreate.listener.InsertionListeners;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.driver.Driver;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.*;


/**
 * @author stefan schroeder
 */

public final class BestInsertionConcurrent extends AbstractInsertionStrategy {

    static class Batch {
        List<VehicleRoute> routes = new ArrayList<VehicleRoute>();

    }

    class Insertion {

        private final VehicleRoute route;

        private final InsertionData insertionData;

        public Insertion(VehicleRoute vehicleRoute, InsertionData insertionData) {
            super();
            this.route = vehicleRoute;
            this.insertionData = insertionData;
        }

        public VehicleRoute getRoute() {
            return route;
        }

        public InsertionData getInsertionData() {
            return insertionData;
        }

    }

    private static Logger logger = LogManager.getLogger(BestInsertionConcurrent.class);

    private final static double NO_NEW_DEPARTURE_TIME_YET = -12345.12345;

    private final static Vehicle NO_NEW_VEHICLE_YET = null;

    private final static Driver NO_NEW_DRIVER_YET = null;

    private InsertionListeners insertionsListeners;

    private JobInsertionCostsCalculator bestInsertionCostCalculator;

    private int nuOfBatches;

    private ExecutorCompletionService<Insertion> completionService;

    public BestInsertionConcurrent(JobInsertionCostsCalculator jobInsertionCalculator, ExecutorService executorService, int nuOfBatches, VehicleRoutingProblem vehicleRoutingProblem) {
        super(vehicleRoutingProblem);
        this.insertionsListeners = new InsertionListeners();
        this.nuOfBatches = nuOfBatches;
        bestInsertionCostCalculator = jobInsertionCalculator;
        completionService = new ExecutorCompletionService<Insertion>(executorService);
        logger.debug("initialise {}", this);
    }

    @Override
    public String toString() {
        return "[name=bestInsertion]";
    }

    @Override
    public Collection<Job> insertUnassignedJobs(Collection<VehicleRoute> vehicleRoutes, Collection<Job> unassignedJobs) {
        List<Job> badJobs = new ArrayList<Job>(unassignedJobs.size());
        List<Job> unassignedJobList = new ArrayList<Job>(unassignedJobs);
        Collections.shuffle(unassignedJobList, random);
        sometimesSortPriorities(unassignedJobList);
        List<Batch> batches = distributeRoutes(vehicleRoutes, nuOfBatches);
        for (final Job unassignedJob : unassignedJobList) {
            Insertion bestInsertion = null;
            double bestInsertionCost = Double.MAX_VALUE;
            for (final Batch batch : batches) {
                completionService.submit(new Callable<Insertion>() {

                    @Override
                    public Insertion call() throws Exception {
                        return getBestInsertion(batch, unassignedJob);
                    }

                });
            }
            try {
                for (int i = 0; i < batches.size(); i++) {
                    Future<Insertion> futureIData = completionService.take();
                    Insertion insertion = futureIData.get();
                    if (insertion == null) continue;
                    if (insertion.getInsertionData().getInsertionCost() < bestInsertionCost) {
                        bestInsertion = insertion;
                        bestInsertionCost = insertion.getInsertionData().getInsertionCost();
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
            VehicleRoute newRoute = VehicleRoute.emptyRoute();
            InsertionData newIData = bestInsertionCostCalculator.getInsertionData(newRoute, unassignedJob, NO_NEW_VEHICLE_YET, NO_NEW_DEPARTURE_TIME_YET, NO_NEW_DRIVER_YET, bestInsertionCost);
            if (newIData.getInsertionCost() < bestInsertionCost) {
                bestInsertion = new Insertion(newRoute, newIData);
                vehicleRoutes.add(newRoute);
                batches.get(random.nextInt(batches.size())).routes.add(newRoute);
            }
            if (bestInsertion == null) badJobs.add(unassignedJob);
            else insertJob(unassignedJob, bestInsertion.getInsertionData(), bestInsertion.getRoute());
        }
        return badJobs;
    }

    private void sometimesSortPriorities(List<Job> unassignedJobList) {
        if(random.nextDouble() < 0.5){
            Collections.sort(unassignedJobList, new Comparator<Job>() {
                @Override
                public int compare(Job o1, Job o2) {
                    return o1.getPriority() - o2.getPriority();
                }
            });
        }
    }

    private Insertion getBestInsertion(Batch batch, Job unassignedJob) {
        Insertion bestInsertion = null;
        double bestInsertionCost = Double.MAX_VALUE;
        for (VehicleRoute vehicleRoute : batch.routes) {
            InsertionData iData = bestInsertionCostCalculator.getInsertionData(vehicleRoute, unassignedJob, NO_NEW_VEHICLE_YET, NO_NEW_DEPARTURE_TIME_YET, NO_NEW_DRIVER_YET, bestInsertionCost);
            if (iData instanceof NoInsertionFound) {
                continue;
            }
            if (iData.getInsertionCost() < bestInsertionCost) {
                bestInsertion = new Insertion(vehicleRoute, iData);
                bestInsertionCost = iData.getInsertionCost();
            }
        }
        return bestInsertion;
    }

    private List<Batch> distributeRoutes(Collection<VehicleRoute> vehicleRoutes, int nuOfBatches) {
        List<Batch> batches = new ArrayList<Batch>();
        for (int i = 0; i < nuOfBatches; i++) batches.add(new Batch());
        /*
         * if route.size < nuOfBatches add as much routes as empty batches are available
		 * else add one empty route anyway
		 */
        if (vehicleRoutes.size() < nuOfBatches) {
            int nOfNewRoutes = nuOfBatches - vehicleRoutes.size();
            for (int i = 0; i < nOfNewRoutes; i++) {
                vehicleRoutes.add(VehicleRoute.emptyRoute());
            }
        } else {
            vehicleRoutes.add(VehicleRoute.emptyRoute());
        }
        /*
         * distribute routes to batches equally
		 */
        int count = 0;
        for (VehicleRoute route : vehicleRoutes) {
            if (count == nuOfBatches) count = 0;
            batches.get(count).routes.add(route);
            count++;
        }
        return batches;
    }


}
