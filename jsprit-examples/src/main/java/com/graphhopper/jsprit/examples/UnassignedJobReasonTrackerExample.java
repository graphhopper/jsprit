package com.graphhopper.jsprit.examples;

import com.graphhopper.jsprit.analysis.toolbox.GraphStreamViewer;
import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.box.Jsprit;
import com.graphhopper.jsprit.core.algorithm.state.StateManager;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.constraint.ConstraintManager;
import com.graphhopper.jsprit.core.problem.constraint.HardRouteConstraint;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.job.Shipment;
import com.graphhopper.jsprit.core.problem.misc.JobInsertionContext;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl.Builder;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleType;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import com.graphhopper.jsprit.core.util.FailedConstraintInfo;
import com.graphhopper.jsprit.core.util.Solutions;
import com.graphhopper.jsprit.core.util.UnassignedJobReasonTracker;

import java.util.Collection;

public class UnassignedJobReasonTrackerExample {


    public static void main(String[] args) {
        final int WEIGHT_INDEX = 0;
        VehicleType vehicleType1 = VehicleTypeImpl.Builder.newInstance("vehicleType1").addCapacityDimension(WEIGHT_INDEX, 2).build();
        VehicleType vehicleType2 = VehicleTypeImpl.Builder.newInstance("vehicleType2").addCapacityDimension(WEIGHT_INDEX, 4).build();

        // vehicles
        VehicleImpl v1 = Builder.newInstance("vehicle1").setStartLocation(Location.newInstance(0, 0)).setType(vehicleType1).build();
        VehicleImpl v2 = Builder.newInstance("vehicle2").setStartLocation(Location.newInstance(0, 10)).setType(vehicleType2).build();

        // jobs
        Shipment shipment1 = Shipment.Builder.newInstance("shipment1").addSizeDimension(WEIGHT_INDEX, 1)
            .setPickupLocation(Location.newInstance(1, 0))
            .setDeliveryLocation(Location.newInstance(3, 0))
            .setPickupTimeWindow(new TimeWindow(0, 3))
            .setDeliveryTimeWindow(new TimeWindow(2, 7))
            .build();
        Shipment shipment2 = Shipment.Builder.newInstance("shipment2").addSizeDimension(WEIGHT_INDEX, 1)
            .setPickupLocation(Location.newInstance(1, 10))
            .setDeliveryLocation(Location.newInstance(3, 10))
            .setPickupTimeWindow(new TimeWindow(1, 3))
            .setDeliveryTimeWindow(new TimeWindow(3, 7))
            .build();

        // shipment3 requires more capacity so v1 is not be able to fulfill it, and v2 is too far away and it cannot fit into time windows
        Shipment shipment3 = Shipment.Builder.newInstance("shipment3").addSizeDimension(WEIGHT_INDEX, 3)
            .setPickupLocation(Location.newInstance(10, 0))
            .setDeliveryLocation(Location.newInstance(15, 0))
            .setPickupTimeWindow(new TimeWindow(10, 15))
            .setDeliveryTimeWindow(new TimeWindow(15, 16))
            .build();

        // shipment4 is too far away from v1 so the time windows will fail, and for v2 the custom constraint will fail
        Shipment shipment4 = Shipment.Builder.newInstance("shipment4").addSizeDimension(WEIGHT_INDEX, 1)
            .setPickupLocation(Location.newInstance(10, 10))
            .setDeliveryLocation(Location.newInstance(15, 10))
            .setPickupTimeWindow(new TimeWindow(10, 15))
            .setDeliveryTimeWindow(new TimeWindow(15, 16))
            .build();

        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        vrpBuilder.addVehicle(v1).addVehicle(v2);
        vrpBuilder.addJob(shipment1).addJob(shipment2).addJob(shipment3).addJob(shipment4);

        // prepare the algorithm
        VehicleRoutingProblem problem = vrpBuilder.build();
        StateManager stateManager = new StateManager(problem);

        ConstraintManager constraintManager = new ConstraintManager(problem, stateManager);
        DummyConstraint dummyConstraint = new DummyConstraint();
        constraintManager.addConstraint(dummyConstraint);
        VehicleRoutingAlgorithm algorithm = Jsprit.Builder.newInstance(problem)
            .setStateAndConstraintManager(stateManager, constraintManager)
            .setProperty(Jsprit.Parameter.VEHICLE_SWITCH, "false") // needed so that constraint works
            .buildAlgorithm();
        algorithm.setMaxIterations(10);

        // set the tracker
        UnassignedJobReasonTracker tracker = new UnassignedJobReasonTracker();
        tracker.put("DummyConstraint", 50, "prevent shipment4 in vehicle2");
        algorithm.addListener(tracker);

        // run the algorithm'
        Collection<VehicleRoutingProblemSolution> solutions = algorithm.searchSolutions();
        VehicleRoutingProblemSolution bestSolution = Solutions.bestOf(solutions);

        for (Job unassignedJob: bestSolution.getUnassignedJobs()) {
            Collection<FailedConstraintInfo> failedConstraints = tracker.getFailedConstraintsForJob(unassignedJob.getId());
            for (FailedConstraintInfo failedConstraint : failedConstraints) {
                System.out.println(failedConstraint.toString());
            }
        }

        new GraphStreamViewer(problem, bestSolution).labelWith(GraphStreamViewer.Label.ID).setRenderDelay(200).display();
    }

    static class DummyConstraint implements HardRouteConstraint {
        @Override
        public boolean fulfilled(JobInsertionContext insertionContext) {
            return !(insertionContext.getJob().getId().equals("shipment4") && insertionContext.getNewVehicle().getId().equals("vehicle2"));
        }
    }

}
