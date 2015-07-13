package jsprit.core.algorithm.state;

import jsprit.core.problem.solution.route.activity.TimeWindow;
import jsprit.core.problem.solution.route.activity.TourActivity;

/**
 * Created by schroeder on 08/07/15.
 */
public class ActivityStartsAsSoonAsNextTimeWindowOpens implements ActivityStartStrategy {

    @Override
    public double getActivityStartTime(TourActivity activity, double arrivalTime) {
        TimeWindow last = null;
        for(int i=activity.getTimeWindows().size()-1; i >= 0; i--){
            TimeWindow tw = activity.getTimeWindows().get(i);
            if(tw.getStart() <= arrivalTime && tw.getEnd() >= arrivalTime){
                return arrivalTime;
            }
            else if(arrivalTime > tw.getEnd()){
                if(last != null) return last.getStart();
                else return arrivalTime;
            }
            last = tw;
        }
        return Math.max(arrivalTime,last.getStart());
    }

}
