package jsprit.core.problem.solution.route.activity;

import org.junit.Test;

/**
 * Created by schroeder on 18/12/15.
 */
public class TimeWindowsImplTest {

    @Test(expected = IllegalStateException.class)
    public void overlappingTW_shouldThrowException(){
        TimeWindowsImpl tws = new TimeWindowsImpl();
        tws.add(TimeWindow.newInstance(50, 100));
        tws.add(TimeWindow.newInstance(90,150));
    }

    @Test(expected = IllegalStateException.class)
    public void overlappingTW2_shouldThrowException(){
        TimeWindowsImpl tws = new TimeWindowsImpl();
        tws.add(TimeWindow.newInstance(50, 100));
        tws.add(TimeWindow.newInstance(40,150));
    }

    @Test(expected = IllegalStateException.class)
    public void overlappingTW3_shouldThrowException(){
        TimeWindowsImpl tws = new TimeWindowsImpl();
        tws.add(TimeWindow.newInstance(50, 100));
        tws.add(TimeWindow.newInstance(50, 100));
    }
}
