/*******************************************************************************
 * Copyright (c) 2014 Stefan Schroeder.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     Stefan Schroeder - initial API and implementation
 ******************************************************************************/
package jsprit.core.problem.solution;

import jsprit.core.problem.job.Job;
import jsprit.core.problem.solution.route.VehicleRoute;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class VehicleRoutingProblemSolutionTest {

    @Test
    public void whenCreatingSolutionWithTwoRoutes_solutionShouldContainTheseRoutes() {
        VehicleRoute r1 = mock(VehicleRoute.class);
        VehicleRoute r2 = mock(VehicleRoute.class);

        VehicleRoutingProblemSolution sol = new VehicleRoutingProblemSolution(Arrays.asList(r1, r2), 0.0);
        assertEquals(2, sol.getRoutes().size());
    }

    @Test
    public void whenSettingSolutionCostsTo10_solutionCostsShouldBe10() {
        VehicleRoutingProblemSolution sol = new VehicleRoutingProblemSolution(Collections.<VehicleRoute>emptyList(), 10.0);
        assertEquals(10.0, sol.getCost(), 0.01);
    }

    @Test
    public void whenCreatingSolWithCostsOf10AndSettingCostsAfterwardsTo20_solutionCostsShouldBe20() {
        VehicleRoutingProblemSolution sol = new VehicleRoutingProblemSolution(Collections.<VehicleRoute>emptyList(), 10.0);
        sol.setCost(20.0);
        assertEquals(20.0, sol.getCost(), 0.01);
    }

    @Test
    public void sizeOfBadJobsShouldBeCorrect() {
        Job badJob = mock(Job.class);
        List<Job> badJobs = new ArrayList<Job>();
        badJobs.add(badJob);
        VehicleRoutingProblemSolution sol = new VehicleRoutingProblemSolution(Collections.<VehicleRoute>emptyList(), badJobs, 10.0);
        assertEquals(1, sol.getUnassignedJobs().size());
    }

    @Test
    public void sizeOfBadJobsShouldBeCorrect_2() {
        Job badJob = mock(Job.class);
        List<Job> badJobs = new ArrayList<Job>();
        badJobs.add(badJob);
        VehicleRoutingProblemSolution sol = new VehicleRoutingProblemSolution(Collections.<VehicleRoute>emptyList(), 10.0);
        sol.getUnassignedJobs().addAll(badJobs);
        assertEquals(1, sol.getUnassignedJobs().size());
    }

    @Test
    public void badJobsShouldBeCorrect() {
        Job badJob = mock(Job.class);
        List<Job> badJobs = new ArrayList<Job>();
        badJobs.add(badJob);
        VehicleRoutingProblemSolution sol = new VehicleRoutingProblemSolution(Collections.<VehicleRoute>emptyList(), badJobs, 10.0);
        assertEquals(badJob, sol.getUnassignedJobs().iterator().next());
    }

    @Test
    public void badJobsShouldBeCorrect_2() {
        Job badJob = mock(Job.class);
        List<Job> badJobs = new ArrayList<Job>();
        badJobs.add(badJob);
        VehicleRoutingProblemSolution sol = new VehicleRoutingProblemSolution(Collections.<VehicleRoute>emptyList(), 10.0);
        sol.getUnassignedJobs().addAll(badJobs);
        assertEquals(badJob, sol.getUnassignedJobs().iterator().next());
    }

}
