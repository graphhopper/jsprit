package jsprit.core.algorithm.state;

import jsprit.core.problem.solution.route.activity.TimeWindow;
import jsprit.core.problem.solution.route.activity.TourActivity;

/**
 * Created by schroeder on 08/07/15.
 */
public class ActivityStartsAsSoonAsNextTimeWindowOpens implements ActivityStartStrategy {

    @Override
    public double getActivityStartTime(TourActivity activity, double arrivalTime) {
        boolean next = false;
        for(TimeWindow tw : activity.getTimeWindows()){
            if(next){
                return Math.max(tw.getStart(),arrivalTime);
            }
            if(tw.getStart() <= arrivalTime && tw.getEnd() >= arrivalTime){
                return arrivalTime;
            }
            else if(tw.getEnd() < arrivalTime){
                next = true;
            }
            else if(tw.getStart() > arrivalTime){
                return tw.getStart();
            }
        }
        return arrivalTime;
    }

}
