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

import com.graphhopper.jsprit.core.algorithm.recreate.listener.InsertionStartsListener;
import com.graphhopper.jsprit.core.problem.driver.Driver;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.util.RandomNumberGeneration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

@Deprecated
class CalculatesServiceInsertionWithTimeScheduling implements JobInsertionCostsCalculator {


    public static class KnowledgeInjection implements InsertionStartsListener {
        private CalculatesServiceInsertionWithTimeScheduling c;

        public KnowledgeInjection(CalculatesServiceInsertionWithTimeScheduling c) {
            super();
            this.c = c;
        }

        @Override
        public void informInsertionStarts(Collection<VehicleRoute> vehicleRoutes, Collection<Job> unassignedJobs) {
            List<Double> knowledge = new ArrayList<Double>();
            if (vehicleRoutes.isEmpty()) {
//                System.out.println("hmm");
            }
            for (VehicleRoute route : vehicleRoutes) {
//                if(route.getDepartureTime() == 21600.){
//                    System.out.println("hu");
//                }
                knowledge.add(route.getDepartureTime());
            }
            c.setDepartureTimeKnowledge(knowledge);
        }
    }

    private static Logger log = LoggerFactory.getLogger(CalculatesServiceInsertionWithTimeScheduling.class);

    private JobInsertionCostsCalculator jic;

    private List<Double> departureTimeKnowledge = new ArrayList<Double>();

    public void setRandom(Random random) {
        this.random = random;
    }

    private Random random = RandomNumberGeneration.getRandom();

    CalculatesServiceInsertionWithTimeScheduling(JobInsertionCostsCalculator jic, double t, double f) {
        super();
        this.jic = jic;
        log.debug("initialise " + this);
    }

    @Override
    public String toString() {
        return "[name=" + this.getClass().toString() + "]";
    }

    @Override
    public InsertionData getInsertionData(VehicleRoute currentRoute, Job jobToInsert, Vehicle newVehicle, double newVehicleDepartureTime, Driver newDriver, double bestKnownScore) {
        double departureTime = newVehicleDepartureTime;
        if (currentRoute.isEmpty()) {
            if (!departureTimeKnowledge.isEmpty()) {
                departureTime = departureTimeKnowledge.get(random.nextInt(departureTimeKnowledge.size()));
            }
        } else if (!currentRoute.getVehicle().getId().equals(newVehicle.getId())) {
            departureTime = currentRoute.getDepartureTime();
        }

        InsertionData insertionData = jic.getInsertionData(currentRoute, jobToInsert, newVehicle, departureTime, newDriver, bestKnownScore);
//        if(!(insertionData instanceof NoInsertionFound) && insertionData.getVehicleDepartureTime() < 28000){
//            System.out.println("hmm");
//        }
        return insertionData;
    }

    public void setDepartureTimeKnowledge(List<Double> departureTimes) {
        departureTimeKnowledge = departureTimes;
    }
}
