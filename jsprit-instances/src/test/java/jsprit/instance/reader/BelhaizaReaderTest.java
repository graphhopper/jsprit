/*******************************************************************************
 * Copyright (C) 2013  Stefan Schroeder
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package jsprit.instance.reader;

import jsprit.core.algorithm.VehicleRoutingAlgorithm;
import jsprit.core.algorithm.box.Jsprit;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.VehicleRoutingProblem.FleetSize;
import jsprit.core.problem.job.Service;
import jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import jsprit.core.problem.solution.route.activity.TimeWindow;
import jsprit.core.problem.vehicle.Vehicle;
import jsprit.core.reporting.SolutionPrinter;
import jsprit.core.util.Solutions;
import org.junit.Test;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;


public class BelhaizaReaderTest {

	@Test
	public void whenReadingBelhaizaInstance_nuOfCustomersIsCorrect(){
		VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
		new BelhaizaReader(builder).read(getPath());
		VehicleRoutingProblem vrp = builder.build();
		assertEquals(100,vrp.getJobs().values().size());
	}

	private String getPath() {
		URL resource = getClass().getClassLoader().getResource("cm101.txt");
		if(resource == null) throw new IllegalStateException("file C101_solomon.txt does not exist");
		return resource.getPath();
	}

	@Test
	public void whenReadingBelhaizaInstance_fleetSizeIsInfinite(){
		VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
		new BelhaizaReader(builder).read(getPath());
		VehicleRoutingProblem vrp = builder.build();
		assertEquals(FleetSize.INFINITE,vrp.getFleetSize());
	}

	@Test
	public void whenReadingBelhaizaInstance_vehicleCapacitiesAreCorrect(){
		VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
		new BelhaizaReader(builder).read(getPath());
		VehicleRoutingProblem vrp = builder.build();
		for(Vehicle v : vrp.getVehicles()){
			assertEquals(200,v.getType().getCapacityDimensions().get(0));
		}
	}

	@Test
	public void whenReadingBelhaizaInstance_vehicleLocationsAreCorrect_and_correspondToDepotLocation(){
		VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
		new BelhaizaReader(builder).read(getPath());
		VehicleRoutingProblem vrp = builder.build();
		for(Vehicle v : vrp.getVehicles()){
			assertEquals(40.0,v.getStartLocation().getCoordinate().getX(),0.01);
			assertEquals(50.0,v.getStartLocation().getCoordinate().getY(),0.01);
		}
	}

	@Test
	public void whenReadingBelhaizaInstance_demandOfCustomerOneIsCorrect(){
		VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
		new BelhaizaReader(builder).read(getPath());
		VehicleRoutingProblem vrp = builder.build();
		assertEquals(10,vrp.getJobs().get("1").getSize().get(0));
	}

	@Test
	public void whenReadingBelhaizaInstance_serviceDurationOfCustomerTwoIsCorrect(){
		VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
		new BelhaizaReader(builder).read(getPath());
		VehicleRoutingProblem vrp = builder.build();
		assertEquals(90,((Service)vrp.getJobs().get("2")).getServiceDuration(),0.1);
	}

	@Test
	public void noTimeWindowsShouldBeCorrect(){
		VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
		new BelhaizaReader(builder).read(getPath());
		VehicleRoutingProblem vrp = builder.build();
		assertEquals(5,((Service)vrp.getJobs().get("1")).getTimeWindows().size());
	}

	@Test
	public void noTimeWindowsShouldBeCorrect2(){
		VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
		new BelhaizaReader(builder).read(getPath());
		VehicleRoutingProblem vrp = builder.build();
		assertEquals(10,((Service)vrp.getJobs().get("2")).getTimeWindows().size());
	}

	@Test
	public void firstTimeWindowShouldBeCorrect(){
		VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
		new BelhaizaReader(builder).read(getPath());
		VehicleRoutingProblem vrp = builder.build();
		assertEquals(20.,((Service)vrp.getJobs().get("1")).getTimeWindows().iterator().next().getStart(),0.1);
		assertEquals(31.,((Service)vrp.getJobs().get("1")).getTimeWindows().iterator().next().getEnd(),0.1);
	}

	@Test
	public void secondTimeWindowShouldBeCorrect(){
		VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
		new BelhaizaReader(builder).read(getPath());
		VehicleRoutingProblem vrp = builder.build();
		List<TimeWindow> timeWindows = new ArrayList<TimeWindow>(((Service)vrp.getJobs().get("1")).getTimeWindows());
		assertEquals(118.,timeWindows.get(1).getStart(),0.1);
		assertEquals(148.,timeWindows.get(1).getEnd(),0.1);
	}

	@Test
	public void thirdTimeWindowShouldBeCorrect(){
		VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
		new BelhaizaReader(builder).read(getPath());
		VehicleRoutingProblem vrp = builder.build();
		List<TimeWindow> timeWindows = new ArrayList<TimeWindow>(((Service)vrp.getJobs().get("1")).getTimeWindows());
		assertEquals(235.,timeWindows.get(2).getStart(),0.1);
		assertEquals(258.,timeWindows.get(2).getEnd(),0.1);
	}

	@Test
	public void fourthTimeWindowShouldBeCorrect(){
		VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
		new BelhaizaReader(builder).read(getPath());
		VehicleRoutingProblem vrp = builder.build();
		List<TimeWindow> timeWindows = new ArrayList<TimeWindow>(((Service)vrp.getJobs().get("1")).getTimeWindows());
		assertEquals(343.,timeWindows.get(3).getStart(),0.1);
		assertEquals(355.,timeWindows.get(3).getEnd(),0.1);
	}

	@Test
	public void fifthTimeWindowShouldBeCorrect(){
		VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
		new BelhaizaReader(builder).read(getPath());
		VehicleRoutingProblem vrp = builder.build();
		List<TimeWindow> timeWindows = new ArrayList<TimeWindow>(((Service)vrp.getJobs().get("1")).getTimeWindows());
		assertEquals(441.,timeWindows.get(4).getStart(),0.1);
		assertEquals(457.,timeWindows.get(4).getEnd(),0.1);
	}

	@Test
	public void testAlgo(){
		VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
		new BelhaizaReader(builder).read(getPath());
		builder.setFleetSize(FleetSize.FINITE);
		VehicleRoutingProblem vrp = builder.build();

//		VehicleRoutingAlgorithm vra = Jsprit.createAlgorithm(vrp);

//		VehicleRoutingAlgorithm algorithm = new SchrimpfFactory().createAlgorithm(vrp);

		Jsprit.Builder vraBuilder = Jsprit.Builder.newInstance(vrp);
		vraBuilder.setProperty(Jsprit.Strategy.CLUSTER_REGRET, "0.25");
		vraBuilder.setProperty(Jsprit.Strategy.RADIAL_REGRET, "0.25");
		vraBuilder.setProperty(Jsprit.Strategy.RANDOM_REGRET, "0.");
		vraBuilder.setProperty(Jsprit.Strategy.WORST_REGRET, "0.25");
		vraBuilder.setProperty(Jsprit.Parameter.THRESHOLD_INI, "0.05");
		VehicleRoutingAlgorithm algorithm = vraBuilder.buildAlgorithm();
		algorithm.setMaxIterations(5000);
//		VariationCoefficientTermination variation_coefficient = new VariationCoefficientTermination(200, 0.005);
//		algorithm.setPrematureAlgorithmTermination(variation_coefficient);
//		algorithm.addListener(variation_coefficient);

//		vra.setMaxIterations(5000);
		VehicleRoutingProblemSolution solution = Solutions.bestOf(algorithm.searchSolutions());

		SolutionPrinter.print(vrp,solution, SolutionPrinter.Print.VERBOSE);
	}

}
