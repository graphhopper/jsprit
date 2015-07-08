package jsprit.core.algorithm.state;

import jsprit.core.problem.solution.route.activity.TimeWindow;
import junit.framework.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;

/**
 * Created by schroeder on 08/07/15.
 */
public class GetLatestArrivalTimeTest {

    @Test
    public void whenSingleTW_itShouldReturnCorrectTime(){
        TimeWindow tw = TimeWindow.newInstance(0,10);
        Assert.assertEquals(10.,getLatestArrivalTime(Arrays.asList(tw),20));
    }

    @Test
    public void whenSingleTW2_itShouldReturnCorrectTime(){
        TimeWindow tw = TimeWindow.newInstance(0,10);
        Assert.assertEquals(8.,getLatestArrivalTime(Arrays.asList(tw),8));
    }

    @Test
    public void whenSingleTW3_itShouldReturnCorrectTime(){
        TimeWindow tw = TimeWindow.newInstance(2,10);
        Assert.assertEquals(1.,getLatestArrivalTime(Arrays.asList(tw),1));
    }

    @Test
    public void whenMultipleTW_itShouldReturnCorrectTime(){
        TimeWindow tw = TimeWindow.newInstance(2,10);
        TimeWindow tw2 = TimeWindow.newInstance(20,30);
        Assert.assertEquals(30.,getLatestArrivalTime(Arrays.asList(tw,tw2),40));
    }

    @Test
    public void whenMultipleTW2_itShouldReturnCorrectTime(){
        TimeWindow tw = TimeWindow.newInstance(2,10);
        TimeWindow tw2 = TimeWindow.newInstance(20,30);
        Assert.assertEquals(25.,getLatestArrivalTime(Arrays.asList(tw,tw2),25));
    }

    @Test
    public void whenMultipleTW3_itShouldReturnCorrectTime(){
        TimeWindow tw = TimeWindow.newInstance(2,10);
        TimeWindow tw2 = TimeWindow.newInstance(20,30);
        Assert.assertEquals(10.,getLatestArrivalTime(Arrays.asList(tw,tw2),19));
    }

    @Test
    public void whenMultipleTW4_itShouldReturnCorrectTime(){
        TimeWindow tw = TimeWindow.newInstance(2,10);
        TimeWindow tw2 = TimeWindow.newInstance(20,30);
        Assert.assertEquals(8.,getLatestArrivalTime(Arrays.asList(tw,tw2),8));
    }

    @Test
    public void whenMultipleTW5_itShouldReturnCorrectTime(){
        TimeWindow tw = TimeWindow.newInstance(2,10);
        TimeWindow tw2 = TimeWindow.newInstance(20,30);
        Assert.assertEquals(1.,getLatestArrivalTime(Arrays.asList(tw,tw2),1));
    }

    @Test
    public void whenSingleTW_ActivityStartTime_shouldReturnCorrectTime(){
        TimeWindow tw = TimeWindow.newInstance(2,10);
        Assert.assertEquals(2.,getActivityStartTime(Arrays.asList(tw),1));
    }

    @Test
    public void whenSingleTW2_ActivityStartTime_shouldReturnCorrectTime(){
        TimeWindow tw = TimeWindow.newInstance(2,10);
        Assert.assertEquals(3.,getActivityStartTime(Arrays.asList(tw),3));
    }

    @Test
    public void whenSingleTW3_ActivityStartTime_shouldReturnCorrectTime(){
        TimeWindow tw = TimeWindow.newInstance(2,10);
        Assert.assertEquals(11.,getActivityStartTime(Arrays.asList(tw),11));
    }

    @Test
    public void whenMultipleTW1_ActivityStartTime_shouldReturnCorrectTime(){
        TimeWindow tw = TimeWindow.newInstance(2,10);
        TimeWindow tw2 = TimeWindow.newInstance(20,30);
        Assert.assertEquals(2.,getActivityStartTime(Arrays.asList(tw,tw2),1));
    }

    @Test
    public void whenMultipleTW2_ActivityStartTime_shouldReturnCorrectTime(){
        TimeWindow tw = TimeWindow.newInstance(2,10);
        TimeWindow tw2 = TimeWindow.newInstance(20,30);
        Assert.assertEquals(3.,getActivityStartTime(Arrays.asList(tw,tw2),3));
    }

    @Test
    public void whenMultipleTW3_ActivityStartTime_shouldReturnCorrectTime(){
        TimeWindow tw = TimeWindow.newInstance(2,10);
        TimeWindow tw2 = TimeWindow.newInstance(20,30);
        Assert.assertEquals(20.,getActivityStartTime(Arrays.asList(tw,tw2),11));
    }

    @Test
    public void whenMultipleTW4_ActivityStartTime_shouldReturnCorrectTime(){
        TimeWindow tw = TimeWindow.newInstance(2,10);
        TimeWindow tw2 = TimeWindow.newInstance(20,30);
        Assert.assertEquals(21.,getActivityStartTime(Arrays.asList(tw,tw2),21));
    }

    @Test
    public void whenMultipleTW5_ActivityStartTime_shouldReturnCorrectTime(){
        TimeWindow tw = TimeWindow.newInstance(2,10);
        TimeWindow tw2 = TimeWindow.newInstance(20,30);
        Assert.assertEquals(31.,getActivityStartTime(Arrays.asList(tw,tw2),31));
    }

    private double getLatestArrivalTime(Collection<TimeWindow> timeWindows, double potentialLatestArrivalTimeAtCurrAct) {
        TimeWindow last = null;
        for(TimeWindow tw : timeWindows){
            if(tw.getStart() <= potentialLatestArrivalTimeAtCurrAct && tw.getEnd() >= potentialLatestArrivalTimeAtCurrAct){
                return potentialLatestArrivalTimeAtCurrAct;
            }
            else if(tw.getStart() > potentialLatestArrivalTimeAtCurrAct){
                if(last == null){
                    return potentialLatestArrivalTimeAtCurrAct;
                }
                else return last.getEnd();
            }
            last = tw;
        }
        return last.getEnd();
    }

    private double getActivityStartTime(Collection<TimeWindow> timeWindows, double arrivalTime) {
        boolean next = false;
        for(TimeWindow tw : timeWindows){
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
