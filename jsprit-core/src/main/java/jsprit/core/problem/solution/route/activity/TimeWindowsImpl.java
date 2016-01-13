package jsprit.core.problem.solution.route.activity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Created by schroeder on 26/05/15.
 */
public class TimeWindowsImpl implements TimeWindows {

    private Collection<TimeWindow> timeWindows = new ArrayList<TimeWindow>();

    public void add(TimeWindow timeWindow){
        for(TimeWindow tw : timeWindows){
            if(timeWindow.getStart() > tw.getStart() && timeWindow.getStart() < tw.getEnd()){
                throw new IllegalStateException("time-windows cannot overlap each other. overlap: " + tw + ", " + timeWindow);
            }
            if(timeWindow.getEnd() > tw.getStart() && timeWindow.getEnd() < tw.getEnd()){
                throw new IllegalStateException("time-windows cannot overlap each other. overlap: " + tw + ", " + timeWindow);
            }
            if(timeWindow.getStart() <= tw.getStart() && timeWindow.getEnd() >= tw.getEnd()){
                throw new IllegalStateException("time-windows cannot overlap each other. overlap: " + tw + ", " + timeWindow);
            }
        }
        timeWindows.add(timeWindow);
    }

    public Collection<TimeWindow> getTimeWindows() {
        return Collections.unmodifiableCollection(timeWindows);
    }

}
