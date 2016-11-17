package com.graphhopper.jsprit.core.problem.job;

import com.graphhopper.jsprit.core.problem.solution.route.activity.JobActivity;
import com.graphhopper.jsprit.core.problem.solution.route.activity.ServiceActivityNEW;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GraphJobActivityListTest {

    private AbstractJob job;

    @Before
    public void beforeClass() {
        job = mock(Service.class);
    }


    private ServiceActivityNEW createActivity(AbstractJob job, String name) {
        ServiceActivityNEW act = mock(ServiceActivityNEW.class);
        when(act.getName()).thenReturn(name);
        when(act.getJob()).thenReturn(job);
        when(act.toString()).thenReturn(name);
        return act;
    }

    private Set<JobActivity> setOf(JobActivity... actA) {
        return new HashSet<>(Arrays.asList(actA));
    }


    @Test
    public void whenCreatingList_itMustGiveBackTheCorrectJob() {
        GraphJobActivityList list = new GraphJobActivityList(job);
        assertEquals(job, list.getJob());
    }

    @Test
    public void whenAddingAnActivity_itMustHaveTheCorrectJob() {
        GraphJobActivityList list = new GraphJobActivityList(job);
        ServiceActivityNEW actA = createActivity(job, "A");
        list.addActivity(actA);
        assertEquals(1, list.size());
        assertEquals(actA, list.getAll().get(0));
    }

    @Test
    public void whenAddingAnActivity_itMustInitializeTheCache() {
        GraphJobActivityList list = new GraphJobActivityList(job);
        ServiceActivityNEW actA = createActivity(job, "A");
        list.addActivity(actA);
        assertEquals(1, list.dependencies.size());
        assertEquals(setOf(actA), list.dependencies.keySet());
        assertEquals(1, list.transitivePrecedingDependencyCache.size());
        assertEquals(setOf(actA), list.transitivePrecedingDependencyCache.keySet());
        assertEquals(1, list.transitiveSubsequentDependencyCache.size());
        assertEquals(setOf(actA), list.transitiveSubsequentDependencyCache.keySet());
    }


    @Test
    public void whenAddingAnActivityTwice_itMustHaveToAddOnlyOnce() {
        GraphJobActivityList list = new GraphJobActivityList(job);
        ServiceActivityNEW actA = createActivity(job, "A");
        list.addActivity(actA);
        list.addActivity(actA);
        assertEquals(1, list.size());
        assertEquals(actA, list.getAll().get(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenAddingAnActivityWithWrongJob_itMustThrowException() {
        GraphJobActivityList list = new GraphJobActivityList(job);
        list.addActivity(createActivity(mock(Service.class), "A"));
    }

    @Test
    public void whenAddingADependency_itMustAddTheActivities() {
        GraphJobActivityList list = new GraphJobActivityList(job);
        ServiceActivityNEW actA = createActivity(job, "A");
        ServiceActivityNEW actB = createActivity(job, "B");
        list.addActivity(actA);
        list.addDependency(actA, actB);
        assertEquals(2, list.size());
        assertEquals(actA, list.getAll().get(0));
        assertEquals(actB, list.getAll().get(1));
    }

    @Test
    public void whenAddingADependency_itMustUpdateCaches() {
        GraphJobActivityList list = new GraphJobActivityList(job);
        ServiceActivityNEW actA = createActivity(job, "A");
        ServiceActivityNEW actB = createActivity(job, "B");
        list.addDependency(actA, actB);
        assertEquals(setOf(actB), list.dependencies.get(actA));
        assertEquals(setOf(actA), list.transitivePrecedingDependencyCache.get(actB));
        assertEquals(setOf(actB), list.transitiveSubsequentDependencyCache.get(actA));
    }

    @Test
    public void whenAddingASecondDependency_itMustUpdateCachesTransitively() {
        GraphJobActivityList list = new GraphJobActivityList(job);
        ServiceActivityNEW actA = createActivity(job, "A");
        ServiceActivityNEW actB = createActivity(job, "B");
        ServiceActivityNEW actC = createActivity(job, "C");
        list.addDependency(actA, actB);
        list.addDependency(actB, actC);
        assertEquals(setOf(actB), list.dependencies.get(actA));
        assertEquals(setOf(actC), list.dependencies.get(actB));
        assertEquals(setOf(actA), list.transitivePrecedingDependencyCache.get(actB));
        assertEquals(setOf(actA, actB), list.transitivePrecedingDependencyCache.get(actC));
        assertEquals(setOf(actB, actC), list.transitiveSubsequentDependencyCache.get(actA));
        assertEquals(setOf(actC), list.transitiveSubsequentDependencyCache.get(actB));
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenAddingADependencyWhichCreatesCycly_itMustThrowAnException() {
        GraphJobActivityList list = new GraphJobActivityList(job);
        ServiceActivityNEW actA = createActivity(job, "A");
        ServiceActivityNEW actB = createActivity(job, "B");
        list.addDependency(actA, actB);
        list.addDependency(actB, actA);
    }


    @Test
    public void complexTest() {
        ServiceActivityNEW actA = createActivity(job, "A");
        ServiceActivityNEW actB = createActivity(job, "B");
        ServiceActivityNEW actC = createActivity(job, "C");
        ServiceActivityNEW actD = createActivity(job, "D");
        ServiceActivityNEW actE = createActivity(job, "E");

        GraphJobActivityList list = new GraphJobActivityList(job);
        list.addDependency(actA, actB);
        list.addDependency(actA, actC);
        list.addDependency(actB, actD);
        list.addActivity(actE);
        assertEquals(setOf(), list.transitivePrecedingDependencyCache.get(actA));
        assertEquals(setOf(actA), list.transitivePrecedingDependencyCache.get(actB));
        assertEquals(setOf(actA), list.transitivePrecedingDependencyCache.get(actC));
        assertEquals(setOf(actA, actB), list.transitivePrecedingDependencyCache.get(actD));
        assertEquals(setOf(), list.transitivePrecedingDependencyCache.get(actE));
        assertEquals(setOf(actB, actC, actD), list.transitiveSubsequentDependencyCache.get(actA));
        assertEquals(setOf(actD), list.transitiveSubsequentDependencyCache.get(actB));
        assertEquals(setOf(), list.transitiveSubsequentDependencyCache.get(actC));
        assertEquals(setOf(), list.transitiveSubsequentDependencyCache.get(actD));
        assertEquals(setOf(), list.transitiveSubsequentDependencyCache.get(actE));
        list.printDetailed();

        list.addDependency(actB, actE);
        assertEquals(setOf(), list.transitivePrecedingDependencyCache.get(actA));
        assertEquals(setOf(actA), list.transitivePrecedingDependencyCache.get(actB));
        assertEquals(setOf(actA), list.transitivePrecedingDependencyCache.get(actC));
        assertEquals(setOf(actA, actB), list.transitivePrecedingDependencyCache.get(actD));
        assertEquals(setOf(actA, actB), list.transitivePrecedingDependencyCache.get(actE));
        assertEquals(setOf(actB, actC, actD, actE), list.transitiveSubsequentDependencyCache.get(actA));
        assertEquals(setOf(actD, actE), list.transitiveSubsequentDependencyCache.get(actB));
        assertEquals(setOf(), list.transitiveSubsequentDependencyCache.get(actC));
        assertEquals(setOf(), list.transitiveSubsequentDependencyCache.get(actD));
        assertEquals(setOf(), list.transitiveSubsequentDependencyCache.get(actE));
        list.printDetailed();

        list.addDependency(actC, actD);
        assertEquals(setOf(), list.transitivePrecedingDependencyCache.get(actA));
        assertEquals(setOf(actA), list.transitivePrecedingDependencyCache.get(actB));
        assertEquals(setOf(actA), list.transitivePrecedingDependencyCache.get(actC));
        assertEquals(setOf(actA, actB, actC), list.transitivePrecedingDependencyCache.get(actD));
        assertEquals(setOf(actA, actB), list.transitivePrecedingDependencyCache.get(actE));
        assertEquals(setOf(actB, actC, actD, actE), list.transitiveSubsequentDependencyCache.get(actA));
        assertEquals(setOf(actD, actE), list.transitiveSubsequentDependencyCache.get(actB));
        assertEquals(setOf(actD), list.transitiveSubsequentDependencyCache.get(actC));
        assertEquals(setOf(), list.transitiveSubsequentDependencyCache.get(actD));
        assertEquals(setOf(), list.transitiveSubsequentDependencyCache.get(actE));
        list.printDetailed();

        list.addDependency(actD, actE);
        assertEquals(setOf(), list.transitivePrecedingDependencyCache.get(actA));
        assertEquals(setOf(actA), list.transitivePrecedingDependencyCache.get(actB));
        assertEquals(setOf(actA), list.transitivePrecedingDependencyCache.get(actC));
        assertEquals(setOf(actA, actB, actC), list.transitivePrecedingDependencyCache.get(actD));
        assertEquals(setOf(actA, actB, actC, actD), list.transitivePrecedingDependencyCache.get(actE));
        assertEquals(setOf(actB, actC, actD, actE), list.transitiveSubsequentDependencyCache.get(actA));
        assertEquals(setOf(actD, actE), list.transitiveSubsequentDependencyCache.get(actB));
        assertEquals(setOf(actD, actE), list.transitiveSubsequentDependencyCache.get(actC));
        assertEquals(setOf(actE), list.transitiveSubsequentDependencyCache.get(actD));
        assertEquals(setOf(), list.transitiveSubsequentDependencyCache.get(actE));

        list.printDetailed();
    }

}
