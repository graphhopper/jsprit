package jsprit.core.problem.solution.route.activity;

/**
 * Created by schroeder on 08/07/15.
 */
public class ActivityStartAsSoonAsArrived implements ActivityStartStrategy {

    @Override
    public double getActivityStartTime(TourActivity activity, double arrivalTime) {
        return arrivalTime;
    }
}
