package jsprit.instance.reader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.job.Job;
import jsprit.core.problem.job.Service;
import jsprit.core.problem.vehicle.PenaltyVehicleType;
import jsprit.core.problem.vehicle.Vehicle;
import jsprit.core.util.Coordinate;
import jsprit.instance.reader.VrphGoldenReader.VrphType;

import org.junit.Test;

public class GoldenReaderTest {
	
	@Test
	public void whenReadingInstance_itShouldReadCorrectNuOfVehicles(){
		VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
		new VrphGoldenReader(vrpBuilder, VrphType.HVRPD)
			.read(this.getClass().getClassLoader().getResource("cn_13mix.txt").getPath());
		VehicleRoutingProblem vrp = vrpBuilder.build();
		int nuOfVehicles = 0;
		for(Vehicle v : vrp.getVehicles()){
			if(!(v.getType() instanceof PenaltyVehicleType)){
				nuOfVehicles++;
			}
		}
		assertEquals(17,nuOfVehicles);
	}
	
	@Test
	public void whenReadingInstance_itShouldReadCorrectNuOfType1Vehicles(){
		VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
		new VrphGoldenReader(vrpBuilder, VrphType.HVRPD)
			.read(this.getClass().getClassLoader().getResource("cn_13mix.txt").getPath());
		VehicleRoutingProblem vrp = vrpBuilder.build();
		int nuOfType1Vehicles = 0;
		for(Vehicle v : vrp.getVehicles()){
			if(v.getType().getTypeId().equals("type_1") && !(v.getType() instanceof PenaltyVehicleType) ){
				nuOfType1Vehicles++;
			}
		}
		assertEquals(4,nuOfType1Vehicles);
	}
	
	@Test
	public void whenReadingInstance_theSumOfType1VehicleShouldHvTheCorrectCapacity(){
		VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
		new VrphGoldenReader(vrpBuilder, VrphType.HVRPD)
			.read(this.getClass().getClassLoader().getResource("cn_13mix.txt").getPath());
		VehicleRoutingProblem vrp = vrpBuilder.build();
		int sumOfType1Cap = 0;
		for(Vehicle v : vrp.getVehicles()){
			if(v.getType().getTypeId().equals("type_1") && !(v.getType() instanceof PenaltyVehicleType) ){
				sumOfType1Cap+=v.getType().getCapacityDimensions().get(0);
			}
		}
		assertEquals(80,sumOfType1Cap);
	}

	@Test
	public void whenReadingInstance_itShouldReadCorrectNuOfType2Vehicles(){
		VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
		new VrphGoldenReader(vrpBuilder, VrphType.HVRPD)
			.read(this.getClass().getClassLoader().getResource("cn_13mix.txt").getPath());
		VehicleRoutingProblem vrp = vrpBuilder.build();
		int nuOfType1Vehicles = 0;
		for(Vehicle v : vrp.getVehicles()){
			if(v.getType().getTypeId().equals("type_2")  && !(v.getType() instanceof PenaltyVehicleType) ){
				nuOfType1Vehicles++;
			}
		}
		assertEquals(2,nuOfType1Vehicles);
	}
	
	@Test
	public void whenReadingInstance_theSumOfType2VehicleShouldHvTheCorrectCapacity(){
		VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
		new VrphGoldenReader(vrpBuilder, VrphType.HVRPD)
			.read(this.getClass().getClassLoader().getResource("cn_13mix.txt").getPath());
		VehicleRoutingProblem vrp = vrpBuilder.build();
		int sumOfType1Cap = 0;
		for(Vehicle v : vrp.getVehicles()){
			if(v.getType().getTypeId().equals("type_2")  && !(v.getType() instanceof PenaltyVehicleType) ){
				sumOfType1Cap+=v.getType().getCapacityDimensions().get(0);
			}
		}
		assertEquals(60,sumOfType1Cap);
	}
	
	@Test
	public void whenReadingInstance_itShouldReadCorrectNuOfType3Vehicles(){
		VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
		new VrphGoldenReader(vrpBuilder, VrphType.HVRPD)
			.read(this.getClass().getClassLoader().getResource("cn_13mix.txt").getPath());
		VehicleRoutingProblem vrp = vrpBuilder.build();
		int nuOfType1Vehicles = 0;
		for(Vehicle v : vrp.getVehicles()){
			if(v.getType().getTypeId().equals("type_3") && !(v.getType() instanceof PenaltyVehicleType) ){
				nuOfType1Vehicles++;
			}
		}
		assertEquals(4,nuOfType1Vehicles);
	}
	
	@Test
	public void whenReadingInstance_theSumOfType3VehicleShouldHvTheCorrectCapacity(){
		VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
		new VrphGoldenReader(vrpBuilder, VrphType.HVRPD)
			.read(this.getClass().getClassLoader().getResource("cn_13mix.txt").getPath());
		VehicleRoutingProblem vrp = vrpBuilder.build();
		int sumOfType1Cap = 0;
		for(Vehicle v : vrp.getVehicles()){
			if(v.getType().getTypeId().equals("type_3") && !(v.getType() instanceof PenaltyVehicleType) ){
				sumOfType1Cap+=v.getType().getCapacityDimensions().get(0);
			}
		}
		assertEquals(160,sumOfType1Cap);
	}
	
	@Test
	public void whenReadingInstance_itShouldReadCorrectNuOfType4Vehicles(){
		VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
		new VrphGoldenReader(vrpBuilder, VrphType.HVRPD)
			.read(this.getClass().getClassLoader().getResource("cn_13mix.txt").getPath());
		VehicleRoutingProblem vrp = vrpBuilder.build();
		int nuOfType1Vehicles = 0;
		for(Vehicle v : vrp.getVehicles()){
			if(v.getType().getTypeId().equals("type_4") && !(v.getType() instanceof PenaltyVehicleType) ){
				nuOfType1Vehicles++;
			}
		}
		assertEquals(4,nuOfType1Vehicles);
	}
	
	@Test
	public void whenReadingInstance_theSumOfType4VehicleShouldHvTheCorrectCapacity(){
		VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
		new VrphGoldenReader(vrpBuilder, VrphType.HVRPD)
			.read(this.getClass().getClassLoader().getResource("cn_13mix.txt").getPath());
		VehicleRoutingProblem vrp = vrpBuilder.build();
		int sumOfType1Cap = 0;
		for(Vehicle v : vrp.getVehicles()){
			if(v.getType().getTypeId().equals("type_4") && !(v.getType() instanceof PenaltyVehicleType) ){
				sumOfType1Cap+=v.getType().getCapacityDimensions().get(0);
			}
		}
		assertEquals(280,sumOfType1Cap);
	}
	
	@Test
	public void whenReadingInstance_itShouldReadCorrectNuOfType5Vehicles(){
		VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
		new VrphGoldenReader(vrpBuilder, VrphType.HVRPD)
			.read(this.getClass().getClassLoader().getResource("cn_13mix.txt").getPath());
		VehicleRoutingProblem vrp = vrpBuilder.build();
		int nuOfType1Vehicles = 0;
		for(Vehicle v : vrp.getVehicles()){
			if(v.getType().getTypeId().equals("type_5") && !(v.getType() instanceof PenaltyVehicleType) ){
				nuOfType1Vehicles++;
			}
		}
		assertEquals(2,nuOfType1Vehicles);
	}
	
	@Test
	public void whenReadingInstance_theSumOfType5VehicleShouldHvTheCorrectCapacity(){
		VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
		new VrphGoldenReader(vrpBuilder, VrphType.HVRPD)
			.read(this.getClass().getClassLoader().getResource("cn_13mix.txt").getPath());
		VehicleRoutingProblem vrp = vrpBuilder.build();
		int sumOfType1Cap = 0;
		for(Vehicle v : vrp.getVehicles()){
			if(v.getType().getTypeId().equals("type_5") && !(v.getType() instanceof PenaltyVehicleType) ){
				sumOfType1Cap+=v.getType().getCapacityDimensions().get(0);
			}
		}
		assertEquals(240,sumOfType1Cap);
	}
	
	@Test
	public void whenReadingInstance_itShouldReadCorrectNuOfType6Vehicles(){
		VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
		new VrphGoldenReader(vrpBuilder, VrphType.HVRPD)
			.read(this.getClass().getClassLoader().getResource("cn_13mix.txt").getPath());
		VehicleRoutingProblem vrp = vrpBuilder.build();
		int nuOfType1Vehicles = 0;
		for(Vehicle v : vrp.getVehicles()){
			if(v.getType().getTypeId().equals("type_6") && !(v.getType() instanceof PenaltyVehicleType) ){
				nuOfType1Vehicles++;
			}
		}
		assertEquals(1,nuOfType1Vehicles);
	}
	
	@Test
	public void whenReadingInstance_theSumOfType6VehicleShouldHvTheCorrectCapacity(){
		VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
		new VrphGoldenReader(vrpBuilder, VrphType.HVRPD)
			.read(this.getClass().getClassLoader().getResource("cn_13mix.txt").getPath());
		VehicleRoutingProblem vrp = vrpBuilder.build();
		int sumOfType1Cap = 0;
		for(Vehicle v : vrp.getVehicles()){
			if(v.getType().getTypeId().equals("type_6") && !(v.getType() instanceof PenaltyVehicleType) ){
				sumOfType1Cap+=v.getType().getCapacityDimensions().get(0);
			}
		}
		assertEquals(200,sumOfType1Cap);
	}
	
	@Test
	public void whenReadingInstance_vehicleShouldHvTheCorrectCoord(){
		VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
		new VrphGoldenReader(vrpBuilder, VrphType.HVRPD)
			.read(this.getClass().getClassLoader().getResource("cn_13mix.txt").getPath());
		VehicleRoutingProblem vrp = vrpBuilder.build();
		for(Vehicle v : vrp.getVehicles()){
			if(v.getStartLocationCoordinate().getX() != 40.0){
				assertFalse(true);
			}
			if(v.getStartLocationCoordinate().getY() != 40.0){
				assertFalse(true);
			}
		}
		assertTrue(true);
	}
	
	@Test
	public void whenReadingInstance_service1MustHaveCorrectDemand(){
		VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
		new VrphGoldenReader(vrpBuilder, VrphType.HVRPD)
			.read(this.getClass().getClassLoader().getResource("cn_13mix.txt").getPath());
		VehicleRoutingProblem vrp = vrpBuilder.build();
		Job job = getJob("1",vrp);
		assertEquals(18,job.getSize().get(0));
	}
	
	@Test
	public void whenReadingInstance_service1MustHaveCorrectCoordinate(){
		VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
		new VrphGoldenReader(vrpBuilder, VrphType.HVRPD)
			.read(this.getClass().getClassLoader().getResource("cn_13mix.txt").getPath());
		VehicleRoutingProblem vrp = vrpBuilder.build();
		Coordinate coord = getCoord("1",vrp);
		assertEquals(22.0,coord.getX(),0.01);
		assertEquals(22.0,coord.getY(),0.01);
	}
	
	@Test
	public void whenReadingInstance_service15MustHaveCorrectCoordinate(){
		VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
		new VrphGoldenReader(vrpBuilder, VrphType.HVRPD)
			.read(this.getClass().getClassLoader().getResource("cn_13mix.txt").getPath());
		VehicleRoutingProblem vrp = vrpBuilder.build();
		Coordinate coord = getCoord("15",vrp);
		assertEquals(62.0,coord.getX(),0.01);
		assertEquals(24.0,coord.getY(),0.01);
	}
	
	
	
	@Test
	public void whenReadingInstance_service50MustHaveCorrectCoordinate(){
		VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
		new VrphGoldenReader(vrpBuilder, VrphType.HVRPD)
			.read(this.getClass().getClassLoader().getResource("cn_13mix.txt").getPath());
		VehicleRoutingProblem vrp = vrpBuilder.build();
		Coordinate coord = getCoord("50",vrp);
		assertEquals(15.0,coord.getX(),0.01);
		assertEquals(56.0,coord.getY(),0.01);
	}
	
	private Coordinate getCoord(String string, VehicleRoutingProblem vrp) {
		Job j = getJob(string,vrp);
		return ((Service)j).getCoord();
	}

	@Test
	public void whenReadingInstance_service4MustHaveCorrectDemand(){
		VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
		new VrphGoldenReader(vrpBuilder, VrphType.HVRPD)
			.read(this.getClass().getClassLoader().getResource("cn_13mix.txt").getPath());
		VehicleRoutingProblem vrp = vrpBuilder.build();
		Job job = getJob("4",vrp);
		assertEquals(30,job.getSize().get(0));
	}
	
	@Test
	public void whenReadingInstance_service50MustHaveCorrectDemand(){
		VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
		new VrphGoldenReader(vrpBuilder, VrphType.HVRPD)
			.read(this.getClass().getClassLoader().getResource("cn_13mix.txt").getPath());
		VehicleRoutingProblem vrp = vrpBuilder.build();
		Job job = getJob("50",vrp);
		assertEquals(22,job.getSize().get(0));
	}

	private Job getJob(String string, VehicleRoutingProblem vrp) {
		for(Job j : vrp.getJobs().values()){
			if(j.getId().equals(string)){
				return j;
			}
		}
		return null;
	}
	
	
}
