
/*******************************************************************************
 * Copyright (C) 2014  Stefan Schroeder
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

package jsprit.examples;


import jsprit.analysis.toolbox.Plotter;
import jsprit.core.algorithm.VehicleRoutingAlgorithm;
import jsprit.core.algorithm.VehicleRoutingAlgorithmBuilder;
import jsprit.core.algorithm.state.StateId;
import jsprit.core.algorithm.state.StateManager;
import jsprit.core.algorithm.state.StateUpdater;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.constraint.ConstraintManager;
import jsprit.core.problem.constraint.HardActivityConstraint;
import jsprit.core.problem.io.VrpXMLReader;
import jsprit.core.problem.misc.JobInsertionContext;
import jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.solution.route.activity.ActivityVisitor;
import jsprit.core.problem.solution.route.activity.TourActivity;
import jsprit.core.reporting.SolutionPrinter;
import jsprit.core.util.Coordinate;
import jsprit.core.util.EuclideanDistanceCalculator;
import jsprit.core.util.Solutions;
import jsprit.core.util.VehicleRoutingTransportCostsMatrix;

import java.util.Collection;

//import jsprit.core.problem.solution.route.state.StateFactory; //v1.3.1

public class AdditionalDistanceConstraintExample {

    static class DistanceUpdater implements StateUpdater, ActivityVisitor {

        private final StateManager stateManager;

        private final VehicleRoutingTransportCostsMatrix costMatrix;

//        private final StateFactory.StateId distanceStateId;    //v1.3.1
        private final StateId distanceStateId; //head of development - upcoming release

        private VehicleRoute vehicleRoute;

        private double distance = 0.;

        private TourActivity prevAct;

//        public DistanceUpdater(StateFactory.StateId distanceStateId, StateManager stateManager, VehicleRoutingTransportCostsMatrix costMatrix) { //v1.3.1
            public DistanceUpdater(StateId distanceStateId, StateManager stateManager, VehicleRoutingTransportCostsMatrix transportCosts) { //head of development - upcoming release (v1.4)
            this.costMatrix = transportCosts;
            this.stateManager = stateManager;
            this.distanceStateId = distanceStateId;
        }

        @Override
        public void begin(VehicleRoute vehicleRoute) {
            distance = 0.;
            prevAct = vehicleRoute.getStart();
            this.vehicleRoute = vehicleRoute;
        }

        @Override
        public void visit(TourActivity tourActivity) {
            distance += getDistance(prevAct,tourActivity);
            prevAct = tourActivity;
        }

        @Override
        public void finish() {
            distance += getDistance(prevAct,vehicleRoute.getEnd());
//            stateManager.putTypedRouteState(vehicleRoute,distanceStateId,Double.class,distance); //v1.3.1
            stateManager.putRouteState(vehicleRoute, distanceStateId, distance); //head of development - upcoming release (v1.4)
        }

        double getDistance(TourActivity from, TourActivity to){
            return costMatrix.getDistance(from.getLocation().getId(), to.getLocation().getId());
        }
    }

    static class DistanceConstraint implements HardActivityConstraint {

        private final StateManager stateManager;

        private final VehicleRoutingTransportCostsMatrix costsMatrix;

        private final double maxDistance;

//        private final StateFactory.StateId distanceStateId; //v1.3.1
        private final StateId distanceStateId; //head of development - upcoming release (v1.4)

//        DistanceConstraint(double maxDistance, StateFactory.StateId distanceStateId, StateManager stateManager, VehicleRoutingTransportCostsMatrix costsMatrix) { //v1.3.1
        DistanceConstraint(double maxDistance, StateId distanceStateId, StateManager stateManager, VehicleRoutingTransportCostsMatrix transportCosts) { //head of development - upcoming release (v1.4)
            this.costsMatrix = transportCosts;
            this.maxDistance = maxDistance;
            this.stateManager = stateManager;
            this.distanceStateId = distanceStateId;
        }

        @Override
        public ConstraintsStatus fulfilled(JobInsertionContext context, TourActivity prevAct, TourActivity newAct, TourActivity nextAct, double v) {
            double additionalDistance = getDistance(prevAct,newAct) + getDistance(newAct,nextAct) - getDistance(prevAct,nextAct);
            Double routeDistance = stateManager.getRouteState(context.getRoute(), distanceStateId, Double.class);
            if(routeDistance == null) routeDistance = 0.;
            double newRouteDistance = routeDistance + additionalDistance;
            if(newRouteDistance > maxDistance) {
                return ConstraintsStatus.NOT_FULFILLED;
            }
            else return ConstraintsStatus.FULFILLED;
        }

        double getDistance(TourActivity from, TourActivity to){
            return costsMatrix.getDistance(from.getLocation().getId(), to.getLocation().getId());
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

        VehicleRoutingAlgorithmBuilder vraBuilder = new VehicleRoutingAlgorithmBuilder(vrp,"input/algorithmConfig_solomon.xml");

//        StateManager stateManager = new StateManager(vrp.getTransportCosts());  //v1.3.1
        StateManager stateManager = new StateManager(vrp); //head of development - upcoming release (v1.4)

//        StateFactory.StateId distanceStateId = StateFactory.createId("distance"); //v1.3.1
        StateId distanceStateId = stateManager.createStateId("distance"); //head of development - upcoming release (v1.4)
        stateManager.addStateUpdater(new DistanceUpdater(distanceStateId, stateManager, costMatrix));
//        stateManager.updateLoadStates();

        ConstraintManager constraintManager = new ConstraintManager(vrp,stateManager);
        constraintManager.addConstraint(new DistanceConstraint(120.,distanceStateId,stateManager,costMatrix), ConstraintManager.Priority.CRITICAL);
//        constraintManager.addLoadConstraint();

//        vraBuilder.addCoreConstraints();
        vraBuilder.addDefaultCostCalculators();
        vraBuilder.setStateAndConstraintManager(stateManager,constraintManager);

        VehicleRoutingAlgorithm vra = vraBuilder.build();
//        vra.setMaxIterations(250); //v1.3.1
        vra.setMaxIterations(250); //head of development - upcoming release (v1.4)

        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();

        SolutionPrinter.print(vrp, Solutions.bestOf(solutions), SolutionPrinter.Print.VERBOSE);

        new Plotter(vrp, Solutions.bestOf(solutions)).plot("output/plot","plot");
    }

    private static VehicleRoutingTransportCostsMatrix createMatrix(VehicleRoutingProblem.Builder vrpBuilder) {
        VehicleRoutingTransportCostsMatrix.Builder matrixBuilder = VehicleRoutingTransportCostsMatrix.Builder.newInstance(true);
        for(String from : vrpBuilder.getLocationMap().keySet()){
            for(String to : vrpBuilder.getLocationMap().keySet()){
                Coordinate fromCoord = vrpBuilder.getLocationMap().get(from);
                Coordinate toCoord = vrpBuilder.getLocationMap().get(to);
                double distance = EuclideanDistanceCalculator.calculateDistance(fromCoord, toCoord);
                matrixBuilder.addTransportDistance(from,to,distance);
                matrixBuilder.addTransportTime(from,to,(distance / 2.));
            }
        }
        return matrixBuilder.build();
    }


}
