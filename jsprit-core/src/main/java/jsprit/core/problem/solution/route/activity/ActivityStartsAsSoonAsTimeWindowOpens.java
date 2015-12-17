package jsprit.core.problem.solution.route.activity;

/**
 * Created by schroeder on 08/07/15.
 */
public class ActivityStartsAsSoonAsTimeWindowOpens implements ActivityStartStrategy {

    @Override
    public double getActivityStartTime(TourActivity activity, double arrivalTime) {
        return Math.max(activity.getTheoreticalEarliestOperationStartTime(),arrivalTime);
    }

}
