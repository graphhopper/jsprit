package jsprit.core.algorithm.state;

import jsprit.core.problem.solution.route.activity.TimeWindow;
import junit.framework.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

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
    public void whenMultiple3TW1_itShouldReturnCorrectTime(){
        TimeWindow tw = TimeWindow.newInstance(2,10);
        TimeWindow tw2 = TimeWindow.newInstance(20,30);
        TimeWindow tw3 = TimeWindow.newInstance(40,50);
        Assert.assertEquals(30.,getLatestArrivalTime(Arrays.asList(tw,tw2,tw3),35));
    }

    @Test
    public void whenMultiple3TW2_itShouldReturnCorrectTime(){
        TimeWindow tw = TimeWindow.newInstance(2,10);
        TimeWindow tw2 = TimeWindow.newInstance(20,30);
        TimeWindow tw3 = TimeWindow.newInstance(40,50);
        Assert.assertEquals(50.,getLatestArrivalTime(Arrays.asList(tw,tw2,tw3),55));
    }

    @Test
    public void whenMultiple3TW3_itShouldReturnCorrectTime(){
        TimeWindow tw = TimeWindow.newInstance(2,10);
        TimeWindow tw2 = TimeWindow.newInstance(20,30);
        TimeWindow tw3 = TimeWindow.newInstance(40,50);
        Assert.assertEquals(45.,getLatestArrivalTime(Arrays.asList(tw,tw2,tw3),45));
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

    @Test
    public void whenMultiple3TW1_ActivityStartTime_shouldReturnCorrectTime(){
        TimeWindow tw = TimeWindow.newInstance(2,10);
        TimeWindow tw2 = TimeWindow.newInstance(20,30);
        TimeWindow tw3 = TimeWindow.newInstance(40,80);
        Assert.assertEquals(40.,getActivityStartTime(Arrays.asList(tw,tw2,tw3),31));
    }

    @Test
    public void whenMultiple3TW2_ActivityStartTime_shouldReturnCorrectTime(){
        TimeWindow tw = TimeWindow.newInstance(2,10);
        TimeWindow tw2 = TimeWindow.newInstance(20,30);
        TimeWindow tw3 = TimeWindow.newInstance(40,80);
        Assert.assertEquals(90.,getActivityStartTime(Arrays.asList(tw,tw2,tw3),90));
    }

    @Test
    public void whenMultiple4TW1_ActivityStartTime_shouldReturnCorrectTime(){
        TimeWindow tw = TimeWindow.newInstance(2,10);
        TimeWindow tw2 = TimeWindow.newInstance(20,30);
        TimeWindow tw3 = TimeWindow.newInstance(40,80);
        TimeWindow tw4 = TimeWindow.newInstance(140,180);
        Assert.assertEquals(140.,getActivityStartTime(Arrays.asList(tw,tw2,tw3,tw4),130));
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

    private double getActivityStartTime(List<TimeWindow> timeWindows, double arrivalTime) {
        TimeWindow last = null;
        for(int i=timeWindows.size()-1; i >= 0; i--){
            TimeWindow tw = timeWindows.get(i);
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

    @Test
    public void singleTWshouldWork(){
        TimeWindow tw = TimeWindow.newInstance(2,10);
        Assert.assertEquals(tw,getNextTimeWindow(11, Arrays.asList(tw)));
    }

    @Test
    public void multipleTWshouldWork(){
        TimeWindow tw = TimeWindow.newInstance(2,10);
        TimeWindow tw1 = TimeWindow.newInstance(20,30);
        Assert.assertEquals(tw1,getNextTimeWindow(19,Arrays.asList(tw,tw1)));
    }

    @Test
    public void multipleTW2shouldWork(){
        TimeWindow tw = TimeWindow.newInstance(2,10);
        TimeWindow tw1 = TimeWindow.newInstance(20,30);
        TimeWindow tw2 = TimeWindow.newInstance(40,50);
        Assert.assertEquals(tw2,getNextTimeWindow(31,Arrays.asList(tw,tw1,tw2)));
    }

    private TimeWindow getNextTimeWindow(double actArrTime, Collection<TimeWindow> timeWindows) {
        for(TimeWindow tw : timeWindows){
            if(actArrTime >= tw.getStart() && actArrTime <= tw.getEnd()) return tw;
            else if(actArrTime < tw.getStart()){
                return tw;
            }
        }
        return null;
    }

}
