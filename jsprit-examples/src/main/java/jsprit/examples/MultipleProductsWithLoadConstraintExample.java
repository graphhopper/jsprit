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

import jsprit.analysis.toolbox.GraphStreamViewer;
import jsprit.core.algorithm.VehicleRoutingAlgorithm;
import jsprit.core.algorithm.box.Jsprit;
import jsprit.core.algorithm.state.InternalStates;
import jsprit.core.algorithm.state.StateManager;
import jsprit.core.problem.Capacity;
import jsprit.core.problem.Location;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.constraint.ConstraintManager;
import jsprit.core.problem.constraint.HardActivityConstraint;
import jsprit.core.problem.job.Shipment;
import jsprit.core.problem.misc.JobInsertionContext;
import jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import jsprit.core.problem.solution.route.activity.TourActivity;
import jsprit.core.problem.vehicle.VehicleImpl;
import jsprit.core.problem.vehicle.VehicleType;
import jsprit.core.problem.vehicle.VehicleTypeImpl;
import jsprit.core.reporting.SolutionPrinter;
import jsprit.core.util.Coordinate;
import jsprit.core.util.Solutions;

import java.util.Collection;

//import jsprit.core.problem.constraint.HardActivityStateLevelConstraint; //v1.3.1

//import jsprit.core.problem.solution.route.state.StateFactory;  //v1.3.1


/**
 * Illustrates a VRP with multiple products.
 *
 * It has the hard requirements that no two different products can be transported in the same vehicle at the same time.
 * This might be important if products require different temperatures. For example, if a vehicle transports
 * apples then no bananas can be loaded (and the other way around). Once all apples have been unloaded, bananas can
 * be loaded.
 *
 * See also the discussion here: https://groups.google.com/forum/#!topic/jsprit-mailing-list/2JQqY4loC0U
 */
public class MultipleProductsWithLoadConstraintExample {

    static final int BANANAS_DIMENSION_INDEX = 0;

    static final int APPLES_DIMENSION_INDEX = 1;

//    static class BananasFirst implements HardActivityStateLevelConstraint { //v1.3.1
    static class BananasFirst implements HardActivityConstraint {

        @Override
        public ConstraintsStatus fulfilled(JobInsertionContext jobInsertionContext, TourActivity prevActivity, TourActivity newActivity, TourActivity nextActivity, double departureTimeAtPrevActivity) {
            if(isBananaPickup(newActivity) && isApplePickup(prevActivity)) return ConstraintsStatus.NOT_FULFILLED_BREAK;
            if(isBananaPickup(nextActivity) && isApplePickup(newActivity)) return ConstraintsStatus.NOT_FULFILLED;
            return ConstraintsStatus.FULFILLED;
        }

        private boolean isApplePickup(TourActivity act){
            return act.getSize().get(APPLES_DIMENSION_INDEX) > 0;
        }

        private boolean isBananaPickup(TourActivity act){
            return act.getSize().get(BANANAS_DIMENSION_INDEX) > 0;
        }
    }

    //static class NoBananasANDApplesConstraint implements HardActivityStateLevelConstraint { //v1.3.1
    static class NoBananasANDApplesConstraint implements HardActivityConstraint {

        private StateManager stateManager;

        NoBananasANDApplesConstraint(StateManager stateManager) {
            this.stateManager = stateManager;
        }

        @Override
        public ConstraintsStatus fulfilled(JobInsertionContext jobInsertionContext, TourActivity prevAct, TourActivity newAct, TourActivity nextAct, double departureTimeAtPrevAct) {
            Capacity loadAtPrevAct = getLoadAtPreviousAct(prevAct);

            if( isPickup(newAct) ){
                if( ( isApplePickup(newAct) && hasBananasInVehicle(loadAtPrevAct) ) ||
                        ( isBananaPickup(newAct) && hasApplesInVehicle(loadAtPrevAct) ) ){
                    return ConstraintsStatus.NOT_FULFILLED;
                }
                if( ( isApplePickup(newAct) && isBananaPickup(nextAct) ) ||
                        ( isBananaPickup(newAct) && isApplePickup(nextAct) ) ){
                    return ConstraintsStatus.NOT_FULFILLED;
                }
                return ConstraintsStatus.FULFILLED;
            }

            if( isDelivery(newAct) ){
                if( ( isAppleDelivery(newAct) && hasBananasInVehicle(loadAtPrevAct) ) ||
                        ( isBananaDelivery(newAct) && hasApplesInVehicle(loadAtPrevAct) ) ){
                    return ConstraintsStatus.NOT_FULFILLED_BREAK; // if so constraint is broken forever -> break here
                }
                return ConstraintsStatus.FULFILLED;
            }
            throw new IllegalStateException("can only constraint shipments");
        }

        private boolean hasApplesInVehicle(Capacity loadAtPrevAct) {
            return loadAtPrevAct.get(APPLES_DIMENSION_INDEX) > 0;
        }

        private boolean hasBananasInVehicle(Capacity loadAtPrevAct){
            return loadAtPrevAct.get(BANANAS_DIMENSION_INDEX) > 0;
        }

        private boolean isBananaPickup(TourActivity act){
            return act.getSize().get(BANANAS_DIMENSION_INDEX) > 0;
        }

        private boolean isBananaDelivery(TourActivity act){
            return act.getSize().get(BANANAS_DIMENSION_INDEX) < 0;
        }

        private boolean isApplePickup(TourActivity act){
            return act.getSize().get(APPLES_DIMENSION_INDEX) > 0;
        }

        private boolean isAppleDelivery(TourActivity act){
            return act.getSize().get(APPLES_DIMENSION_INDEX) < 0;
        }

        private boolean isPickup(TourActivity newAct) {
            return newAct.getName().equals("pickupShipment");
        }

        private boolean isDelivery(TourActivity newAct) {
            return newAct.getName().equals("deliverShipment");
        }

        private Capacity getLoadAtPreviousAct(TourActivity prevAct) {
//            Capacity prevLoad = stateManager.getActivityState(prevAct, StateFactory.LOAD, Capacity.class); //v1.3.1
            Capacity prevLoad = stateManager.getActivityState(prevAct, InternalStates.LOAD, Capacity.class); //1.3.2-SNAPSHOT & upcoming release v1.4
            if(prevLoad != null) return prevLoad;
            else return Capacity.Builder.newInstance().build();
        }
    }



    public static void main(String[] args) {


        VehicleType type = VehicleTypeImpl.Builder.newInstance("type").addCapacityDimension(BANANAS_DIMENSION_INDEX,10)
                .addCapacityDimension(APPLES_DIMENSION_INDEX, 20).build();
        VehicleImpl vehicle = VehicleImpl.Builder.newInstance("vehicle").setStartLocation(loc(Coordinate.newInstance(0, 0)))
                .setType(type).build();

        Shipment bananas = Shipment.Builder.newInstance("bananas_1").addSizeDimension(BANANAS_DIMENSION_INDEX,1)
                .setPickupLocation(loc(Coordinate.newInstance(1, 8))).setDeliveryLocation(loc(Coordinate.newInstance(10, 8))).build();

        Shipment bananas_2 = Shipment.Builder.newInstance("bananas_2").addSizeDimension(BANANAS_DIMENSION_INDEX,1)
                .setPickupLocation(loc(Coordinate.newInstance(2, 8))).setDeliveryLocation(loc(Coordinate.newInstance(11, 8))).build();

        Shipment bananas_3 = Shipment.Builder.newInstance("bananas_3").addSizeDimension(BANANAS_DIMENSION_INDEX,1)
                .setPickupLocation(loc(Coordinate.newInstance(3, 8))).setDeliveryLocation(loc(Coordinate.newInstance(12, 8))).build();

        Shipment apples = Shipment.Builder.newInstance("apples_1").addSizeDimension(APPLES_DIMENSION_INDEX,1)
                .setPickupLocation(loc(Coordinate.newInstance(1, 6))).setDeliveryLocation(loc(Coordinate.newInstance(10, 12))).build();

        Shipment apples_2 = Shipment.Builder.newInstance("apples_2").addSizeDimension(APPLES_DIMENSION_INDEX,1)
                .setPickupLocation(loc(Coordinate.newInstance(1, 5))).setDeliveryLocation(loc(Coordinate.newInstance(10, 11))).build();

        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().setFleetSize(VehicleRoutingProblem.FleetSize.INFINITE)
                .addVehicle(vehicle)
                .addJob(bananas).addJob(apples).addJob(bananas_2).addJob(bananas_3).addJob(apples_2).build();

        StateManager stateManager = new StateManager(vrp);
        ConstraintManager constraintManager = new ConstraintManager(vrp,stateManager);
        constraintManager.addConstraint(new NoBananasANDApplesConstraint(stateManager), ConstraintManager.Priority.CRITICAL);
//        constraintManager.addConstraint(new BananasFirst(),ConstraintManager.Priority.CRITICAL);

        VehicleRoutingAlgorithm vra = Jsprit.Builder.newInstance(vrp).setStateAndConstraintManager(stateManager,constraintManager)
                .buildAlgorithm();

        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();

        SolutionPrinter.print(vrp, Solutions.bestOf(solutions), SolutionPrinter.Print.VERBOSE);

        new GraphStreamViewer(vrp, Solutions.bestOf(solutions)).labelWith(GraphStreamViewer.Label.ID).setRenderShipments(true).display();

    }

    private static Location loc(Coordinate coordinate) {
        return Location.Builder.newInstance().setCoordinate(coordinate).build();
    }
}
