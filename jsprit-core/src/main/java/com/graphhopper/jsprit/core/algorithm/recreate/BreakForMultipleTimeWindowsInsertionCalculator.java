package com.graphhopper.jsprit.core.algorithm.recreate;

import com.graphhopper.jsprit.core.problem.JobActivityFactory;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.constraint.*;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingActivityCosts;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import com.graphhopper.jsprit.core.problem.driver.Driver;
import com.graphhopper.jsprit.core.problem.job.BreakForMultipleTimeWindows;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.misc.JobInsertionContext;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.BreakForMultipleTimeWindowsActivity;
import com.graphhopper.jsprit.core.problem.solution.route.activity.End;
import com.graphhopper.jsprit.core.problem.solution.route.activity.Start;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;

final class BreakForMultipleTimeWindowsInsertionCalculator implements JobInsertionCostsCalculator {
    private static final Logger logger = LoggerFactory.getLogger(BreakInsertionCalculator.class);

    private ConstraintManager constraintManager;

    private VehicleRoutingTransportCosts transportCosts;

    private final VehicleRoutingActivityCosts activityCosts;

    private ActivityInsertionCostsCalculator additionalTransportCostsCalculator;

    private JobActivityFactory activityFactory;

    private AdditionalAccessEgressCalculator additionalAccessEgressCalculator;

    public BreakForMultipleTimeWindowsInsertionCalculator(VehicleRoutingTransportCosts routingCosts, VehicleRoutingActivityCosts activityCosts, ActivityInsertionCostsCalculator additionalTransportCostsCalculator, ConstraintManager constraintManager, JobActivityFactory jobActivityFactory) {
        this.transportCosts = routingCosts;
        this.activityCosts = activityCosts;
        this.constraintManager = constraintManager;
        this.additionalTransportCostsCalculator = additionalTransportCostsCalculator;
        additionalAccessEgressCalculator = new AdditionalAccessEgressCalculator(routingCosts);
        this.activityFactory = jobActivityFactory;

        logger.debug("initialise " + this);
    }

    @Override
    public String toString() {
        return "[name=calculatesServiceInsertion]";
    }

    /**
     * Calculates the marginal cost of inserting job i locally. This is based on the
     * assumption that cost changes can entirely covered by only looking at the predecessor i-1 and its successor i+1.
     */
    @Override
    public InsertionData getInsertionData(final VehicleRoute currentRoute, final Job jobToInsert, final Vehicle newVehicle, double newVehicleDepartureTime, final Driver newDriver, final double bestKnownCosts) {
        BreakForMultipleTimeWindows breakToInsert = (BreakForMultipleTimeWindows) jobToInsert;
        JobInsertionContext insertionContext = new JobInsertionContext(currentRoute, jobToInsert, newVehicle, newDriver, newVehicleDepartureTime);
        int insertionIndex = InsertionData.NO_INDEX;

        BreakForMultipleTimeWindowsActivity breakAct2Insert = (BreakForMultipleTimeWindowsActivity) activityFactory.createActivities(breakToInsert).get(0);
        insertionContext.getAssociatedActivities().add(breakAct2Insert);

        /*
        check hard constraints at route level
         */
        if (!constraintManager.fulfilled(insertionContext)) {
            return InsertionData.createEmptyInsertionData();
        }

        /*
        check soft constraints at route level
         */
        double additionalICostsAtRouteLevel = constraintManager.getCosts(insertionContext);

        double bestCost = bestKnownCosts;
        additionalICostsAtRouteLevel += additionalAccessEgressCalculator.getCosts(insertionContext);

		/*
        generate new start and end for new vehicle
         */
        Start start = new Start(newVehicle.getStartLocation(), newVehicle.getEarliestDeparture(), Double.MAX_VALUE);
        start.setEndTime(newVehicleDepartureTime);
        End end = new End(newVehicle.getEndLocation(), 0.0, newVehicle.getLatestArrival());

        Location bestLocation = null;

        TourActivity prevAct = start;
        double prevActStartTime = newVehicleDepartureTime;
        int actIndex = 0;
        Iterator<TourActivity> activityIterator = currentRoute.getActivities().iterator();
        boolean tourEnd = false;
        while (!tourEnd) {
            TourActivity nextAct;
            if (activityIterator.hasNext()) nextAct = activityIterator.next();
            else {
                nextAct = end;
                tourEnd = true;
            }
            boolean breakThis = true;
            final Location location = Location.Builder.newInstance().setId(breakAct2Insert.getJob().getLocation().getId()).setCoordinate(prevAct.getLocation().getCoordinate()).build();
            breakAct2Insert.setLocation(location);
            breakAct2Insert.setTheoreticalEarliestOperationStartTime(breakToInsert.getTimeWindow().getStart());
            breakAct2Insert.setTheoreticalLatestOperationStartTime(breakToInsert.getTimeWindow().getEnd());
            HardActivityConstraint.ConstraintsStatus status = constraintManager.fulfilled(insertionContext, prevAct, breakAct2Insert, nextAct, prevActStartTime);
            if (status.equals(HardActivityConstraint.ConstraintsStatus.FULFILLED)) {
                //from job2insert induced costs at activity level
                double additionalICostsAtActLevel = constraintManager.getCosts(insertionContext, prevAct, breakAct2Insert, nextAct, prevActStartTime);
                double additionalTransportationCosts = additionalTransportCostsCalculator.getCosts(insertionContext, prevAct, nextAct, breakAct2Insert, prevActStartTime);
                if (additionalICostsAtRouteLevel + additionalICostsAtActLevel + additionalTransportationCosts < bestCost) {
                    bestCost = additionalICostsAtRouteLevel + additionalICostsAtActLevel + additionalTransportationCosts;
                    insertionIndex = actIndex;
                    bestLocation = location;
                }
                breakThis = false;
            } else if (status.equals(HardActivityConstraint.ConstraintsStatus.NOT_FULFILLED)) {
                breakThis = false;
            }

            double nextActArrTime = prevActStartTime + transportCosts.getTransportTime(prevAct.getLocation(), nextAct.getLocation(), prevActStartTime, newDriver, newVehicle);
            prevActStartTime = Math.max(nextActArrTime, nextAct.getTheoreticalEarliestOperationStartTime()) + activityCosts.getActivityDuration(prevAct, nextAct,nextActArrTime,newDriver,newVehicle);
            prevAct = nextAct;
            actIndex++;
            if (breakThis) break;
        }
        if (insertionIndex == InsertionData.NO_INDEX) {
            return InsertionData.createEmptyInsertionData();
        }
        InsertionData insertionData = new InsertionData(bestCost, InsertionData.NO_INDEX, insertionIndex, newVehicle, newDriver);
        breakAct2Insert.setLocation(bestLocation);
        insertionData.getEvents().add(new InsertBreak(currentRoute, newVehicle, breakAct2Insert, insertionIndex));
        insertionData.getEvents().add(new SwitchVehicle(currentRoute, newVehicle, newVehicleDepartureTime));
        insertionData.setVehicleDepartureTime(newVehicleDepartureTime);
        return insertionData;
    }
}
