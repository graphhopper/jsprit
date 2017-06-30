/*
 * Licensed to GraphHopper GmbH under one or more contributor
 * license agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * GraphHopper GmbH licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.graphhopper.jsprit.core.algorithm.recreate;

import com.graphhopper.jsprit.core.algorithm.recreate.InsertionData.NoInsertionFound;
import com.graphhopper.jsprit.core.algorithm.recreate.listener.InsertionListeners;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.driver.Driver;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
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

    private static Logger logger = LoggerFactory.getLogger(BestInsertionConcurrent.class);

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
        Collections.sort(unassignedJobList, new AccordingToPriorities());
        List<Batch> batches = distributeRoutes(vehicleRoutes, nuOfBatches);
        List<String> failedConstraintNames = new ArrayList<>();
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
                    if (insertion.insertionData instanceof NoInsertionFound) {
                        failedConstraintNames.addAll(insertion.getInsertionData().getFailedConstraintNames());
                        continue;
                    }
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
            if (bestInsertion == null) {
                badJobs.add(unassignedJob);
                markUnassigned(unassignedJob, failedConstraintNames);
            }
            else insertJob(unassignedJob, bestInsertion.getInsertionData(), bestInsertion.getRoute());
        }
        return badJobs;
    }


    private Insertion getBestInsertion(Batch batch, Job unassignedJob) {
        Insertion bestInsertion = null;
        InsertionData empty = new InsertionData.NoInsertionFound();
        double bestInsertionCost = Double.MAX_VALUE;
        for (VehicleRoute vehicleRoute : batch.routes) {
            InsertionData iData = bestInsertionCostCalculator.getInsertionData(vehicleRoute, unassignedJob, NO_NEW_VEHICLE_YET, NO_NEW_DEPARTURE_TIME_YET, NO_NEW_DRIVER_YET, bestInsertionCost);
            if (iData instanceof NoInsertionFound) {
                empty.getFailedConstraintNames().addAll(iData.getFailedConstraintNames());
                continue;
            }
            if (iData.getInsertionCost() < bestInsertionCost) {
                bestInsertion = new Insertion(vehicleRoute, iData);
                bestInsertionCost = iData.getInsertionCost();
            }
        }
        if (bestInsertion == null) return new Insertion(null, empty);
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
