package jsprit.core.algorithm.termination;


import jsprit.core.algorithm.SearchStrategy;
import junit.framework.Assert;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class IterationsWithoutImprovementTest {

    @Test
    public void itShouldTerminateAfter100() {
        IterationWithoutImprovementTermination termination = new IterationWithoutImprovementTermination(100);
        SearchStrategy.DiscoveredSolution discoveredSolution = mock(SearchStrategy.DiscoveredSolution.class);
        when(discoveredSolution.isAccepted()).thenReturn(false);
        int terminatedAfter = 0;
        for (int i = 0; i < 200; i++) {
            boolean terminate = termination.isPrematureBreak(discoveredSolution);
            if (terminate) {
                terminatedAfter = i;
                break;
            }
        }
        Assert.assertEquals(100, terminatedAfter);
    }

    @Test
    public void itShouldTerminateAfter1() {
        IterationWithoutImprovementTermination termination = new IterationWithoutImprovementTermination(1);
        SearchStrategy.DiscoveredSolution discoveredSolution = mock(SearchStrategy.DiscoveredSolution.class);
        when(discoveredSolution.isAccepted()).thenReturn(false);
        int terminatedAfter = 0;
        for (int i = 0; i < 200; i++) {
            boolean terminate = termination.isPrematureBreak(discoveredSolution);
            if (terminate) {
                terminatedAfter = i;
                break;
            }
        }
        Assert.assertEquals(1, terminatedAfter);
    }

    @Test
    public void itShouldTerminateAfter150() {
        IterationWithoutImprovementTermination termination = new IterationWithoutImprovementTermination(100);
        SearchStrategy.DiscoveredSolution discoveredSolution = mock(SearchStrategy.DiscoveredSolution.class);
        int terminatedAfter = 0;
        for (int i = 0; i < 200; i++) {
            when(discoveredSolution.isAccepted()).thenReturn(false);
            if (i == 49) when(discoveredSolution.isAccepted()).thenReturn(true);
            boolean terminate = termination.isPrematureBreak(discoveredSolution);
            if (terminate) {
                terminatedAfter = i;
                break;
            }
        }
        Assert.assertEquals(150, terminatedAfter);
    }
}
