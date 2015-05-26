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
        timeWindows.add(timeWindow);
    }

    public Collection<TimeWindow> getTimeWindows() {
        return Collections.unmodifiableCollection(timeWindows);
    }

    @Override
    public Collection<TimeWindow> getTimeWindows(double time) {
        return Collections.unmodifiableCollection(timeWindows);
    }

}
