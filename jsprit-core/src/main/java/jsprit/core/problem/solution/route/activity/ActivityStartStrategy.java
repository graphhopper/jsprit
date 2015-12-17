package jsprit.core.problem.solution.route.activity;

/**
 * Created by schroeder on 08/07/15.
 */
public interface ActivityStartStrategy {

    public double getActivityStartTime(TourActivity activity, double arrivalTime);

}
