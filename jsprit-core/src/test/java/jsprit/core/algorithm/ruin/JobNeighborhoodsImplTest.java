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
package jsprit.core.algorithm.ruin;

import jsprit.core.algorithm.ruin.distance.EuclideanServiceDistance;
import jsprit.core.algorithm.ruin.distance.JobDistance;
import jsprit.core.problem.Location;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.job.Job;
import jsprit.core.problem.job.Service;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class JobNeighborhoodsImplTest {

    VehicleRoutingProblem vrp;

    JobDistance jobDistance;

    Service target;
    Service s2;
    Service s3;
    Service s4;
    Service s5;
    Service s6;
    Service s7;

    @Before
    public void doBefore() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
        target = Service.Builder.newInstance("s1").addSizeDimension(0, 1).setLocation(Location.newInstance(0, 5)).build();
        s2 = Service.Builder.newInstance("s2").addSizeDimension(0, 1).setLocation(Location.newInstance(0, 4)).build();
        s3 = Service.Builder.newInstance("s3").addSizeDimension(0, 1).setLocation(Location.newInstance(0, 3)).build();
        s4 = Service.Builder.newInstance("s4").addSizeDimension(0, 1).setLocation(Location.newInstance(0, 2)).build();

        s5 = Service.Builder.newInstance("s5").addSizeDimension(0, 1).setLocation(Location.newInstance(0, 6)).build();
        s6 = Service.Builder.newInstance("s6").addSizeDimension(0, 1).setLocation(Location.newInstance(0, 7)).build();
        s7 = Service.Builder.newInstance("s7").addSizeDimension(0, 1).setLocation(Location.newInstance(0, 8)).build();

        vrp = builder.addJob(target).addJob(s2).addJob(s3).addJob(s4).addJob(s5).addJob(s6).addJob(s7).build();

        jobDistance = new EuclideanServiceDistance();
    }

    @Test
    public void whenRequestingNeighborhoodOfTargetJob_nNeighborsShouldBeTwo() {
        JobNeighborhoodsImpl jn = new JobNeighborhoodsImpl(vrp, jobDistance);
        jn.initialise();
        Iterator<Job> iter = jn.getNearestNeighborsIterator(2, target);
        List<Service> services = new ArrayList<Service>();
        while (iter.hasNext()) {
            services.add((Service) iter.next());
        }
        assertEquals(2, services.size());
    }

    @Test
    public void whenRequestingNeighborhoodOfTargetJob_s2ShouldBeNeighbor() {
        JobNeighborhoodsImpl jn = new JobNeighborhoodsImpl(vrp, jobDistance);
        jn.initialise();
        Iterator<Job> iter = jn.getNearestNeighborsIterator(2, target);
        List<Service> services = new ArrayList<Service>();
        while (iter.hasNext()) {
            services.add((Service) iter.next());
        }
        assertTrue(services.contains(s2));
    }

    @Test
    public void whenRequestingNeighborhoodOfTargetJob_s4ShouldBeNeighbor() {
        JobNeighborhoodsImpl jn = new JobNeighborhoodsImpl(vrp, jobDistance);
        jn.initialise();
        Iterator<Job> iter = jn.getNearestNeighborsIterator(2, target);
        List<Service> services = new ArrayList<Service>();
        while (iter.hasNext()) {
            services.add((Service) iter.next());
        }
        assertTrue(services.contains(s5));
    }

    @Test
    public void whenRequestingNeighborhoodOfTargetJob_sizeShouldBe4() {
        JobNeighborhoodsImpl jn = new JobNeighborhoodsImpl(vrp, jobDistance);
        jn.initialise();
        Iterator<Job> iter = jn.getNearestNeighborsIterator(4, target);
        List<Service> services = new ArrayList<Service>();
        while (iter.hasNext()) {
            services.add((Service) iter.next());
        }
        assertEquals(4, services.size());
    }

    @Test
    public void whenRequestingMoreNeighborsThanExisting_itShouldReturnMaxNeighbors() {
        JobNeighborhoodsImpl jn = new JobNeighborhoodsImpl(vrp, jobDistance);
        jn.initialise();
        Iterator<Job> iter = jn.getNearestNeighborsIterator(100, target);
        List<Service> services = new ArrayList<Service>();
        while (iter.hasNext()) {
            services.add((Service) iter.next());
        }
        assertEquals(6, services.size());
    }

}
