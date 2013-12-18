package jsprit.core.algorithm.ruin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import jsprit.core.algorithm.ruin.RuinRadial.JobNeighborhoodsImplWithCapRestriction;
import jsprit.core.algorithm.ruin.distance.EuclideanServiceDistance;
import jsprit.core.algorithm.ruin.distance.JobDistance;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.job.Job;
import jsprit.core.problem.job.Service;
import jsprit.core.util.Coordinate;

import org.junit.Before;
import org.junit.Test;


public class JobNeighborhoodsWithCapRestrictionImplTest {

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
	public void doBefore(){
		VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
		target = Service.Builder.newInstance("s1", 1).setCoord(Coordinate.newInstance(0, 5)).build();
		s2 = Service.Builder.newInstance("s2", 1).setCoord(Coordinate.newInstance(0, 4)).build();
		s3 = Service.Builder.newInstance("s3", 1).setCoord(Coordinate.newInstance(0, 3)).build();
		s4 = Service.Builder.newInstance("s4", 1).setCoord(Coordinate.newInstance(0, 2)).build();
		
		s5 = Service.Builder.newInstance("s5", 1).setCoord(Coordinate.newInstance(0, 6)).build();
		s6 = Service.Builder.newInstance("s6", 1).setCoord(Coordinate.newInstance(0, 7)).build();
		s7 = Service.Builder.newInstance("s7", 1).setCoord(Coordinate.newInstance(0, 8)).build();
		
		vrp = builder.addJob(target).addJob(s2).addJob(s3).addJob(s4).addJob(s5).addJob(s6).addJob(s7).build();
		
		jobDistance = new EuclideanServiceDistance();
	}
	
	@Test
	public void whenRequestingNeighborhoodOfTargetJob_nNeighborsShouldBeTwo(){
		JobNeighborhoodsImplWithCapRestriction jn = new JobNeighborhoodsImplWithCapRestriction(vrp, jobDistance, 2);
		jn.initialise();
		Iterator<Job> iter = jn.getNearestNeighborsIterator(2, target);
		List<Service> services = new ArrayList<Service>();
		while(iter.hasNext()){
			services.add((Service) iter.next());
		}
		assertEquals(2,services.size());
	}
	
	@Test
	public void whenRequestingNeighborhoodOfTargetJob_s2ShouldBeNeighbor(){
		JobNeighborhoodsImplWithCapRestriction jn = new JobNeighborhoodsImplWithCapRestriction(vrp, jobDistance, 2);
		jn.initialise();
		Iterator<Job> iter = jn.getNearestNeighborsIterator(2, target);
		List<Service> services = new ArrayList<Service>();
		while(iter.hasNext()){
			services.add((Service) iter.next());
		}
		assertTrue(services.contains(s2));
	}
	
	@Test
	public void whenRequestingNeighborhoodOfTargetJob_s4ShouldBeNeighbor(){
		JobNeighborhoodsImplWithCapRestriction jn = new JobNeighborhoodsImplWithCapRestriction(vrp, jobDistance, 2);
		jn.initialise();
		Iterator<Job> iter = jn.getNearestNeighborsIterator(2, target);
		List<Service> services = new ArrayList<Service>();
		while(iter.hasNext()){
			services.add((Service) iter.next());
		}
		assertTrue(services.contains(s5));
	}
	
	@Test
	public void whenRequestingNeighborhoodOfTargetJob_sizeShouldBe4(){
		JobNeighborhoodsImplWithCapRestriction jn = new JobNeighborhoodsImplWithCapRestriction(vrp, jobDistance, 4);
		jn.initialise();
		Iterator<Job> iter = jn.getNearestNeighborsIterator(4, target);
		List<Service> services = new ArrayList<Service>();
		while(iter.hasNext()){
			services.add((Service) iter.next());
		}
		assertEquals(4,services.size());
	}
	
	@Test
	public void whenRequestingMoreNeighborsThanExisting_itShouldReturnMaxNeighbors(){
		JobNeighborhoodsImplWithCapRestriction jn = new JobNeighborhoodsImplWithCapRestriction(vrp, jobDistance, 2);
		jn.initialise();
		Iterator<Job> iter = jn.getNearestNeighborsIterator(100, target);
		List<Service> services = new ArrayList<Service>();
		while(iter.hasNext()){
			services.add((Service) iter.next());
		}
		assertEquals(2,services.size());
	}

}
