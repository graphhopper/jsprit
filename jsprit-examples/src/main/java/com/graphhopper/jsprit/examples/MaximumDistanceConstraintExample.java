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

package com.graphhopper.jsprit.examples;


import com.graphhopper.jsprit.analysis.toolbox.Plotter;
import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.box.Jsprit;
import com.graphhopper.jsprit.core.algorithm.state.StateId;
import com.graphhopper.jsprit.core.algorithm.state.StateManager;
import com.graphhopper.jsprit.core.algorithm.state.StateUpdater;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.constraint.ConstraintManager;
import com.graphhopper.jsprit.core.problem.constraint.HardActivityConstraint;
import com.graphhopper.jsprit.core.problem.misc.JobInsertionContext;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.*;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.reporting.SolutionPrinter;
import com.graphhopper.jsprit.core.util.Coordinate;
import com.graphhopper.jsprit.core.util.EuclideanDistanceCalculator;
import com.graphhopper.jsprit.core.util.Solutions;
import com.graphhopper.jsprit.core.util.VehicleRoutingTransportCostsMatrix;
import com.graphhopper.jsprit.io.problem.VrpXMLReader;

import java.util.Collection;

//import jsprit.core.problem.solution.route.state.StateFactory; //v1.3.1

public class MaximumDistanceConstraintExample {

    static class DistanceUpdater implements StateUpdater, vehicleDependentActivityVisitor {

        private final StateManager stateManager;

        private final VehicleRoutingTransportCostsMatrix costsMatrix;

        private final StateId distanceStateId;

        private VehicleRoute vehicleRoute;

        private Vehicle vehicle;

        private double distance = 0.;

        private Location prevLoc;

        public DistanceUpdater(StateId distanceStateId, StateManager stateManager, VehicleRoutingTransportCostsMatrix transportCosts) {
            this.costsMatrix = transportCosts;
            this.stateManager = stateManager;
            this.distanceStateId = distanceStateId;
        }

        public void begin(VehicleRoute vehicleRoute, Vehicle vehicle) {
            distance = 0.;
            prevLoc = vehicle.getStartLocation();
            this.vehicleRoute = vehicleRoute;
            this.vehicle = vehicle;
        }


        public void visit(TourActivity tourActivity) {
            distance += getDistance(prevLoc, tourActivity.getLocation());
            prevLoc = tourActivity.getLocation();
        }


        public void finish() {
            if(vehicle.isReturnToDepot())
                distance += getDistance(prevLoc, vehicle.getEndLocation());
            stateManager.putRouteState(vehicleRoute,vehicle,distanceStateId, distance);
        }

        double getDistance(Location from, Location to) {
            return this.costsMatrix.getDistance(from.getId(), to.getId());
        }
    }


    static class DistanceConstraint implements HardActivityConstraint {

        private final StateManager stateManager;

        private final VehicleRoutingTransportCostsMatrix costsMatrix;

        private final double maxDistance;

        private final StateId distanceStateId;


        DistanceConstraint(double maxDistance, StateId distanceStateId, StateManager stateManager, VehicleRoutingTransportCostsMatrix transportCosts) {
            this.costsMatrix = transportCosts;
            this.maxDistance = maxDistance;
            this.stateManager = stateManager;
            this.distanceStateId = distanceStateId;
        }



        public ConstraintsStatus fulfilled(JobInsertionContext context, TourActivity prevAct, TourActivity newAct, TourActivity nextAct, double v) {
            double additionalDistance = 0.;
            double pickupDistance = 0.;

            if(newAct instanceof DeliverShipment) {

                Location pickupLoc = context.getAssociatedActivities().get(0).getLocation();
                Location prevtoPickupLoc = null;
                Location nexttoPickupLoc = null;
                int pickupActinsertionIndex = context.getRelatedActivityContext().getInsertionIndex();

                /*
                If first delivery activity
                Total distance = Start-pickup + pickup-delivery + delivery-end
                 */
                if(context.getRoute().getTourActivities().getActivities().size() == 0){
                    Double routeDistance = this.costsMatrix.getDistance(context.getNewVehicle().getStartLocation().getId(),pickupLoc.getId())
                                            + getDistance(pickupLoc,newAct.getLocation());
                    if(context.getNewVehicle().isReturnToDepot())
                        routeDistance += getDistance(newAct.getLocation(),context.getNewVehicle().getEndLocation());
                    if(routeDistance > this.maxDistance )return ConstraintsStatus.NOT_FULFILLED;
                    else  return ConstraintsStatus.FULFILLED;

                /*
                    If pickup insertion index == 0
                    Activity previous to pickup is vehicle start
                     */
                }else if (pickupActinsertionIndex == 0) {
                    prevtoPickupLoc = context.getNewVehicle().getStartLocation();
                    nexttoPickupLoc = context.getRoute().getTourActivities().getActivities().get(0).getLocation();
                    /*
                    If pickup activity is just before end activity
                    next to pickup activity is vehicle end
                     */
                } else if (pickupActinsertionIndex == context.getRoute().getTourActivities().getActivities().size()) {
                    prevtoPickupLoc = context.getRoute().getTourActivities().getActivities().get(pickupActinsertionIndex - 1).getLocation();
                    nexttoPickupLoc = context.getNewVehicle().getEndLocation();
                } else {
                    prevtoPickupLoc = context.getRoute().getTourActivities().getActivities().get(pickupActinsertionIndex - 1).getLocation();
                    nexttoPickupLoc = context.getRoute().getTourActivities().getActivities().get(pickupActinsertionIndex).getLocation();
                }
                /*
                If next to pickup is end and !vehicle.returntoDepot()
                Pickup distance = distance(prevtoPickup,Pickup)
                 */
                if (pickupActinsertionIndex == context.getRoute().getTourActivities().getActivities().size()  && !context.getNewVehicle().isReturnToDepot())
                    pickupDistance = getDistance(prevtoPickupLoc, pickupLoc);
                else
                    pickupDistance = getDistance(prevtoPickupLoc, pickupLoc) + getDistance(pickupLoc, nexttoPickupLoc) - getDistance(prevtoPickupLoc, nexttoPickupLoc);
            }


            if (nextAct instanceof End && !context.getNewVehicle().isReturnToDepot())
                additionalDistance = getDistance(prevAct.getLocation(), newAct.getLocation());
            else if (nextAct instanceof End)
                additionalDistance =  getDistance(prevAct.getLocation(), newAct.getLocation())
                                    + getDistance(newAct.getLocation(),context.getNewVehicle().getEndLocation())
                                    - getDistance(prevAct.getLocation(),context.getNewVehicle().getEndLocation());
            else if(prevAct instanceof Start)
                additionalDistance = getDistance(context.getNewVehicle().getStartLocation(), newAct.getLocation())
                                    + getDistance(newAct.getLocation(),newAct.getLocation())
                                    - getDistance(context.getNewVehicle().getStartLocation(), nextAct.getLocation());
            else
                additionalDistance = getDistance(prevAct.getLocation(), newAct.getLocation())
                                    + getDistance(newAct.getLocation(), nextAct.getLocation())
                                    - getDistance(prevAct.getLocation(), nextAct.getLocation());

            Double routeDistance = stateManager.getRouteState(context.getRoute(), context.getNewVehicle(), distanceStateId, Double.class);
            if (routeDistance == null) {
                routeDistance = 0.;
            }
            double newRouteDistance = routeDistance + additionalDistance + pickupDistance;
            if (newRouteDistance > maxDistance) return ConstraintsStatus.NOT_FULFILLED;
            else return ConstraintsStatus.FULFILLED;
        }

        double getDistance(Location from, Location to) {
            return this.costsMatrix.getDistance(from.getId(), to.getId());
        }

    }

    public static void main(String[] args) {

        //route length 618
        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(vrpBuilder).read("input/pickups_and_deliveries_solomon_r101_withoutTWs.xml");
        //builds a matrix based on euclidean distances; t_ij = euclidean(i,j) / 2; d_ij = euclidean(i,j);
        VehicleRoutingTransportCostsMatrix costMatrix = createMatrix(vrpBuilder);
        vrpBuilder.setRoutingCost(costMatrix);
        VehicleRoutingProblem vrp = vrpBuilder.build();


        StateManager stateManager = new StateManager(vrp); //head of development - upcoming release (v1.4)

        StateId distanceStateId = stateManager.createStateId("distance"); //head of development - upcoming release (v1.4)
        stateManager.addStateUpdater(new DistanceUpdater(distanceStateId, stateManager, costMatrix));

        ConstraintManager constraintManager = new ConstraintManager(vrp, stateManager);
        constraintManager.addConstraint(new DistanceConstraint(120., distanceStateId, stateManager, costMatrix), ConstraintManager.Priority.CRITICAL);

        VehicleRoutingAlgorithm vra = Jsprit.Builder.newInstance(vrp).setStateAndConstraintManager(stateManager,constraintManager)
            .buildAlgorithm();
//        vra.setMaxIterations(250); //v1.3.1
        vra.setMaxIterations(250); //head of development - upcoming release (v1.4)

        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();

        SolutionPrinter.print(vrp, Solutions.bestOf(solutions), SolutionPrinter.Print.VERBOSE);

        new Plotter(vrp, Solutions.bestOf(solutions)).plot("output/plot", "plot");
    }

    private static VehicleRoutingTransportCostsMatrix createMatrix(VehicleRoutingProblem.Builder vrpBuilder) {
        VehicleRoutingTransportCostsMatrix.Builder matrixBuilder = VehicleRoutingTransportCostsMatrix.Builder.newInstance(true);
        for (String from : vrpBuilder.getLocationMap().keySet()) {
            for (String to : vrpBuilder.getLocationMap().keySet()) {
                Coordinate fromCoord = vrpBuilder.getLocationMap().get(from);
                Coordinate toCoord = vrpBuilder.getLocationMap().get(to);
                double distance = EuclideanDistanceCalculator.calculateDistance(fromCoord, toCoord);
                matrixBuilder.addTransportDistance(from, to, distance);
                matrixBuilder.addTransportTime(from, to, (distance / 2.));
            }
        }
        return matrixBuilder.build();
    }


}
