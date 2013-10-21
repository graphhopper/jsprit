package algorithms;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import util.Coordinate;
import util.Locations;
import util.ManhattanCosts;
import algorithms.HardConstraints.HardActivityLevelConstraint;
import algorithms.HardConstraints.HardRouteLevelConstraint;
import basics.Shipment;
import basics.costs.VehicleRoutingActivityCosts;
import basics.costs.VehicleRoutingTransportCosts;
import basics.route.Driver;
import basics.route.TourActivity;
import basics.route.Vehicle;
import basics.route.VehicleImpl;
import basics.route.VehicleRoute;
import basics.route.VehicleType;
import basics.route.VehicleTypeImpl;

public class ShipmentInsertionCalculatorTest {
	
	VehicleRoutingTransportCosts routingCosts;
	
	VehicleRoutingActivityCosts activityCosts = new VehicleRoutingActivityCosts(){

		@Override
		public double getActivityCost(TourActivity tourAct, double arrivalTime,Driver driver, Vehicle vehicle) {
			return 0;
		}
		
	};
	
	HardActivityLevelConstraint hardActivityLevelConstraint = new HardActivityLevelConstraint() {
		
		@Override
		public boolean fulfilled(InsertionContext iFacts, TourActivity prevAct,TourActivity newAct, TourActivity nextAct, double prevActDepTime) {
			return true;
		}
	};
	
	HardRouteLevelConstraint hardRouteLevelConstraint = new HardRouteLevelConstraint(){

		@Override
		public boolean fulfilled(InsertionContext insertionContext) {
			return true;
		}
		
	};
	
	ActivityInsertionCostsCalculator activityInsertionCostsCalculator;
	
	ShipmentInsertionCalculator insertionCalculator;
	
	Vehicle vehicle;
	
	@Before
	public void doBefore(){
		Locations locations = new Locations(){

			@Override
			public Coordinate getCoord(String id) {
				//assume: locationId="x,y"
				String[] splitted = id.split(",");
				return Coordinate.newInstance(Double.parseDouble(splitted[0]), 
						Double.parseDouble(splitted[1]));
			}
			
		};
		routingCosts = new ManhattanCosts(locations);
		VehicleType type = VehicleTypeImpl.Builder.newInstance("t", 1).setCostPerDistance(1).build();
		vehicle = VehicleImpl.Builder.newInstance("v").setLocationId("0,0").setType(type).build();
		activityInsertionCostsCalculator = new LocalActivityInsertionCostsCalculator(routingCosts, activityCosts, hardActivityLevelConstraint);
		createInsertionCalculator(hardRouteLevelConstraint);
	}

	private void createInsertionCalculator(HardRouteLevelConstraint hardRouteLevelConstraint) {
		insertionCalculator = new ShipmentInsertionCalculator(routingCosts, activityInsertionCostsCalculator, hardRouteLevelConstraint);
	}
	
	@Test
	public void whenCalculatingInsertionCostsOfShipment_itShouldReturnCorrectCostValue(){
		Shipment shipment = Shipment.Builder.newInstance("s", 1).setPickupLocation("0,10").setDeliveryLocation("10,0").build();
		VehicleRoute route = VehicleRoute.emptyRoute();
		
		InsertionData iData = insertionCalculator.calculate(route, shipment, vehicle, 0.0, null, Double.MAX_VALUE);
		assertEquals(40.0,iData.getInsertionCost(),0.05);
	}
	
	@Test
	public void whenCalculatingInsertionIntoExistingRoute_itShouldReturnCorrectCosts(){
		Shipment shipment = Shipment.Builder.newInstance("s", 1).setPickupLocation("0,10").setDeliveryLocation("10,0").build();
		Shipment shipment2 = Shipment.Builder.newInstance("s2", 1).setPickupLocation("10,10").setDeliveryLocation("0,0").build();
		VehicleRoute route = VehicleRoute.emptyRoute();
		new Inserter(new InsertionListeners()).insertJob(shipment, new InsertionData(0,0,0,vehicle,null), route);
		
		InsertionData iData = insertionCalculator.calculate(route, shipment2, vehicle, 0.0, null, Double.MAX_VALUE);
		assertEquals(0.0,iData.getInsertionCost(),0.05);
		assertEquals(1,iData.getPickupInsertionIndex());
		assertEquals(2,iData.getDeliveryInsertionIndex());
	}
	
	@Test
	public void whenInsertingShipmentInRouteWithNotEnoughCapacity_itShouldReturnNoInsertion(){
		Shipment shipment = Shipment.Builder.newInstance("s", 1).setPickupLocation("0,10").setDeliveryLocation("10,0").build();
		Shipment shipment2 = Shipment.Builder.newInstance("s2", 1).setPickupLocation("10,10").setDeliveryLocation("0,0").build();
		VehicleRoute route = VehicleRoute.emptyRoute();
		new Inserter(new InsertionListeners()).insertJob(shipment, new InsertionData(0,0,0,vehicle,null), route);
		createInsertionCalculator(new HardRouteLevelConstraint() {
			
			@Override
			public boolean fulfilled(InsertionContext insertionContext) {
				return false;
			}
			
		});
		InsertionData iData = insertionCalculator.calculate(route, shipment2, vehicle, 0.0, null, Double.MAX_VALUE);
		assertEquals(InsertionData.noInsertionFound(),iData);
		
	}
	
	
	@Test
	public void whenInsertingThirdShipment_itShouldCalcCorrectVal(){
		Shipment shipment = Shipment.Builder.newInstance("s", 1).setPickupLocation("0,10").setDeliveryLocation("10,0").build();
		Shipment shipment2 = Shipment.Builder.newInstance("s2", 1).setPickupLocation("10,10").setDeliveryLocation("0,0").build();
		Shipment shipment3 = Shipment.Builder.newInstance("s3", 1).setPickupLocation("0,0").setDeliveryLocation("9,10").build();
		
		VehicleRoute route = VehicleRoute.emptyRoute();
		Inserter inserter = new Inserter(new InsertionListeners());
		inserter.insertJob(shipment, new InsertionData(0,0,0,vehicle,null), route);
		inserter.insertJob(shipment2, new InsertionData(0,1,2,vehicle,null),route);
		
		InsertionData iData = insertionCalculator.calculate(route, shipment3, vehicle, 0.0, null, Double.MAX_VALUE);
		assertEquals(0.0,iData.getInsertionCost(),0.05);
		assertEquals(0,iData.getPickupInsertionIndex());
		assertEquals(1,iData.getDeliveryInsertionIndex());
	}
	
	@Test
	public void whenInsertingThirdShipment_itShouldCalcCorrectVal2(){
		Shipment shipment = Shipment.Builder.newInstance("s", 1).setPickupLocation("0,10").setDeliveryLocation("10,0").build();
		Shipment shipment2 = Shipment.Builder.newInstance("s2", 1).setPickupLocation("10,10").setDeliveryLocation("0,0").build();
		Shipment shipment3 = Shipment.Builder.newInstance("s3", 1).setPickupLocation("0,0").setDeliveryLocation("9,9").build();
		
		VehicleRoute route = VehicleRoute.emptyRoute();
		Inserter inserter = new Inserter(new InsertionListeners());
		inserter.insertJob(shipment, new InsertionData(0,0,0,vehicle,null), route);
		inserter.insertJob(shipment2, new InsertionData(0,1,2,vehicle,null),route);
		
		InsertionData iData = insertionCalculator.calculate(route, shipment3, vehicle, 0.0, null, Double.MAX_VALUE);
		assertEquals(2.0,iData.getInsertionCost(),0.05);
		assertEquals(0,iData.getPickupInsertionIndex());
		assertEquals(1,iData.getDeliveryInsertionIndex());
	}
	
}
