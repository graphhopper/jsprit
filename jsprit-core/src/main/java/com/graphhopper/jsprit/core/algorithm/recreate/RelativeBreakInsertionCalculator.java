//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.graphhopper.jsprit.core.algorithm.recreate;

import com.graphhopper.jsprit.core.algorithm.utils.RelativeBreaksUtils;
import com.graphhopper.jsprit.core.problem.JobActivityFactory;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.Location.Builder;
import com.graphhopper.jsprit.core.problem.constraint.ConstraintManager;
import com.graphhopper.jsprit.core.problem.constraint.HardActivityConstraint.ConstraintsStatus;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingActivityCosts;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import com.graphhopper.jsprit.core.problem.driver.Driver;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.job.RelativeBreak;
import com.graphhopper.jsprit.core.problem.misc.ActivityContext;
import com.graphhopper.jsprit.core.problem.misc.JobInsertionContext;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.*;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.util.Coordinate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;

import static com.graphhopper.jsprit.core.algorithm.utils.RelativeBreaksUtils.getUpdatedTimeWindowForRelativeBreak;

final class RelativeBreakInsertionCalculator extends AbstractInsertionCalculator {
    public static final Coordinate INACTIVE_COORDINATE = new Coordinate(0, 0);

    private static final Logger logger = LoggerFactory.getLogger(RelativeBreakInsertionCalculator.class);
    private ConstraintManager constraintManager;
    private VehicleRoutingTransportCosts transportCosts;
    private final VehicleRoutingActivityCosts activityCosts;
    private ActivityInsertionCostsCalculator additionalTransportCostsCalculator;
    private JobActivityFactory activityFactory;
    private AdditionalAccessEgressCalculator additionalAccessEgressCalculator;

    public RelativeBreakInsertionCalculator(VehicleRoutingTransportCosts routingCosts, VehicleRoutingActivityCosts activityCosts, ActivityInsertionCostsCalculator additionalTransportCostsCalculator, ConstraintManager constraintManager, JobActivityFactory jobActivityFactory) {
        this.transportCosts = routingCosts;
        this.activityCosts = activityCosts;
        this.constraintManager = constraintManager;
        this.additionalTransportCostsCalculator = additionalTransportCostsCalculator;
        this.additionalAccessEgressCalculator = new AdditionalAccessEgressCalculator(routingCosts);
        this.activityFactory = jobActivityFactory;
        logger.debug("initialise " + this);
    }

    public String toString() {
        return "[name=calculatesServiceInsertion]";
    }


    public InsertionData getInsertionData(VehicleRoute currentRoute, Job jobToInsert, Vehicle newVehicle, double newVehicleDepartureTime, Driver newDriver, double bestKnownCosts) {
        RelativeBreak breakToInsert = (RelativeBreak)jobToInsert;

        if(!RelativeBreaksUtils.minRouteLengthAchieved(currentRoute, breakToInsert, transportCosts)){
            return InsertionData.createEmptyInsertionData();
        }

        JobInsertionContext insertionContext = new JobInsertionContext(currentRoute, jobToInsert, newVehicle, newDriver, newVehicleDepartureTime);
        int insertionIndex = InsertionData.NO_INDEX;
        RelativeBreakActivity breakAct2Insert = (RelativeBreakActivity)this.activityFactory.createActivities(breakToInsert).get(0);
        insertionContext.getAssociatedActivities().add(breakAct2Insert);

        if (!this.constraintManager.fulfilled(insertionContext)) {
            return InsertionData.createEmptyInsertionData();
        } else {

            TimeWindow tw = getUpdatedTimeWindowForRelativeBreak(currentRoute, breakToInsert, transportCosts);
            breakAct2Insert.setTheoreticalEarliestOperationStartTime(tw.getStart());
            breakAct2Insert.setTheoreticalLatestOperationStartTime(tw.getEnd());

            double additionalICostsAtRouteLevel = this.constraintManager.getCosts(insertionContext);
            double bestCost = bestKnownCosts;
            additionalICostsAtRouteLevel += this.additionalAccessEgressCalculator.getCosts(insertionContext);
            Start start = new Start(newVehicle.getStartLocation(), newVehicle.getEarliestDeparture(), 1.7976931348623157E308D);
            start.setEndTime(newVehicleDepartureTime);
            End end = new End(newVehicle.getEndLocation(), 0.0D, newVehicle.getLatestArrival());
            Location bestLocation = null;
            TourActivity prevAct = start;
            double prevActStartTime = newVehicleDepartureTime;
            int actIndex = 0;
            Iterator<TourActivity> activityIterator = currentRoute.getActivities().iterator();
            boolean tourEnd = false;

            // if threshold > 0 , break can not be first in route. skip first insertion index (index 0)
            if(breakToInsert.getThreshold() > 0) {
                if(currentRoute.getActivities().size() == 0)
                    return InsertionData.createEmptyInsertionData();
                prevAct =  activityIterator.next();
                actIndex++;

            }


            while(!tourEnd) {
                TourActivity nextAct;

                if (activityIterator.hasNext()) {
                    nextAct = activityIterator.next();


                } else {
                    nextAct = end;
                    tourEnd = true;
                }

                TourActivity next = nextAct;
                boolean breakThis = true;

                Location location = breakAct2Insert.getJob().getLocation();
                if(INACTIVE_COORDINATE.equals(location.getCoordinate())) {
                    Location locationToUpdate = Builder.newInstance().setId(breakAct2Insert.getJob().getLocation().getId()).setCoordinate((prevAct).getLocation().getCoordinate()).build();
                    breakAct2Insert.setLocation(locationToUpdate);
                }

                /*
                if (nextAct instanceof RelativeBreakActivity) {
                    next = this.getRelativeBreakCopyWithUpdatedLocation(location, nextAct);
                }
                */

                ActivityContext activityContext = new ActivityContext();
                activityContext.setInsertionIndex(actIndex);
                insertionContext.setActivityContext(activityContext);
                ConstraintsStatus status = this.constraintManager.fulfilled(insertionContext, prevAct, breakAct2Insert, next, prevActStartTime);
                if(!status.equals(ConstraintsStatus.FULFILLED)) {
                    status = this.constraintManager.fulfilled(insertionContext, prevAct, breakAct2Insert, next, prevActStartTime);
                }
                double additionalICostsAtActLevel;
                if (status.equals(ConstraintsStatus.FULFILLED)) {
                    additionalICostsAtActLevel = this.constraintManager.getCosts(insertionContext, prevAct, breakAct2Insert, next, prevActStartTime);
                    double additionalTransportationCosts = this.additionalTransportCostsCalculator.getCosts(insertionContext, prevAct, next, breakAct2Insert, prevActStartTime);
                    if (additionalICostsAtRouteLevel + additionalICostsAtActLevel + additionalTransportationCosts < bestCost) {
                        bestCost = additionalICostsAtRouteLevel + additionalICostsAtActLevel + additionalTransportationCosts;
                        insertionIndex = actIndex;
                        bestLocation = location;
                    }

                    breakThis = false;
                } else if (status.equals(ConstraintsStatus.NOT_FULFILLED)) {
                    breakThis = false;
                }

                additionalICostsAtActLevel = prevActStartTime + this.transportCosts.getTransportTime((prevAct).getLocation(), (next).getLocation(), prevActStartTime, newDriver, newVehicle);
                prevActStartTime = Math.max(additionalICostsAtActLevel, (next).getTheoreticalEarliestOperationStartTime()) + this.activityCosts.getActivityDuration(prevAct, next, additionalICostsAtActLevel, newDriver, newVehicle);
                prevAct = nextAct;
                ++actIndex;
                if (breakThis) {
                    break;
                }
            }

            if (insertionIndex == InsertionData.NO_INDEX) {
                return InsertionData.createEmptyInsertionData();
            } else {
                InsertionData insertionData = new InsertionData(bestCost, InsertionData.NO_INDEX, insertionIndex, newVehicle, newDriver);
                breakAct2Insert.setLocation(bestLocation);
                insertionData.getEvents().add(new InsertBreak(currentRoute, newVehicle, breakAct2Insert, insertionIndex));
                insertionData.getEvents().add(new SwitchVehicle(currentRoute, newVehicle, newVehicleDepartureTime));
                insertionData.setVehicleDepartureTime(newVehicleDepartureTime);
                return insertionData;
            }
        }
    }
}
