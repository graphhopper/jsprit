package jsprit.core.algorithm.recreate;

import static org.junit.Assert.assertTrue;
import jsprit.core.algorithm.state.StateManager;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.constraint.ConstraintManager;
import jsprit.core.problem.job.Delivery;
import jsprit.core.problem.job.Shipment;
import jsprit.core.problem.vehicle.InfiniteFleetManagerFactory;
import jsprit.core.problem.vehicle.Vehicle;
import jsprit.core.problem.vehicle.VehicleFleetManager;
import jsprit.core.problem.vehicle.VehicleImpl;
import jsprit.core.problem.vehicle.VehicleImpl.Builder;
import jsprit.core.problem.vehicle.VehicleType;
import jsprit.core.problem.vehicle.VehicleTypeImpl;
import jsprit.core.util.Coordinate;

import org.junit.Test;


public class TestMixedServiceAndShipmentsProblemOnRouteLevel {
	
	
	
	

	
	@Test(expected=UnsupportedOperationException.class)
	public void whenHavingShipmentsAndServicesInOneProblem_andInsertionShouldBeMadeOnRouteLevel_throwException(){
		/* get a vehicle type-builder and build a type with the typeId "vehicleType" and a capacity of 2
		 */
		VehicleTypeImpl.Builder vehicleTypeBuilder = VehicleTypeImpl.Builder.newInstance("vehicleType", 2);
		VehicleType vehicleType = vehicleTypeBuilder.build();
		
		/*
		 * get a vehicle-builder and build a vehicle located at (10,10) with type "vehicleType"
		 */
		Builder vehicleBuilder = VehicleImpl.Builder.newInstance("vehicle");
		vehicleBuilder.setLocationCoord(Coordinate.newInstance(10, 10));
		vehicleBuilder.setType(vehicleType);
		Vehicle vehicle = vehicleBuilder.build();
		
		/*
		 * build shipments at the required locations, each with a capacity-demand of 1.
		 * 4 shipments
		 * 1: (5,7)->(6,9)
		 * 2: (5,13)->(6,11)
		 * 3: (15,7)->(14,9)
		 * 4: (15,13)->(14,11)
		 */
		
		Shipment shipment1 = Shipment.Builder.newInstance("1", 1).setPickupCoord(Coordinate.newInstance(5, 7)).setDeliveryCoord(Coordinate.newInstance(6, 9)).build();
		Shipment shipment2 = Shipment.Builder.newInstance("2", 1).setPickupCoord(Coordinate.newInstance(5, 13)).setDeliveryCoord(Coordinate.newInstance(6, 11)).build();
		
		Shipment shipment3 = Shipment.Builder.newInstance("3", 1).setPickupCoord(Coordinate.newInstance(15, 7)).setDeliveryCoord(Coordinate.newInstance(14, 9)).build();
		Shipment shipment4 = Shipment.Builder.newInstance("4", 1).setPickupCoord(Coordinate.newInstance(15, 13)).setDeliveryCoord(Coordinate.newInstance(14, 11)).build();
		
		/*
		 * build deliveries, (implicitly picked up in the depot)
		 * 1: (4,8)
		 * 2: (4,12)
		 * 3: (16,8)
		 * 4: (16,12)
		 */
		Delivery delivery1 = (Delivery) Delivery.Builder.newInstance("5", 1).setCoord(Coordinate.newInstance(4, 8)).build();
		Delivery delivery2 = (Delivery) Delivery.Builder.newInstance("6", 1).setCoord(Coordinate.newInstance(4, 12)).build();
		Delivery delivery3 = (Delivery) Delivery.Builder.newInstance("7", 1).setCoord(Coordinate.newInstance(16, 8)).build();
		Delivery delivery4 = (Delivery) Delivery.Builder.newInstance("8", 1).setCoord(Coordinate.newInstance(16, 12)).build();
		
		VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
		vrpBuilder.addVehicle(vehicle);
		vrpBuilder.addJob(shipment1).addJob(shipment2).addJob(shipment3).addJob(shipment4)
			.addJob(delivery1).addJob(delivery2).addJob(delivery3).addJob(delivery4).build();
		
		VehicleRoutingProblem vrp = vrpBuilder.build();
		
		final StateManager stateManager = new StateManager(vrp);
		

		ConstraintManager constraintManager = new ConstraintManager(vrp,stateManager);
		constraintManager.addLoadConstraint();
		constraintManager.addTimeWindowConstraint();
		
		VehicleFleetManager fleetManager = new InfiniteFleetManagerFactory(vrp.getVehicles()).createFleetManager();

		BestInsertionBuilder bestIBuilder = new BestInsertionBuilder(vrp, fleetManager, stateManager,constraintManager);
		bestIBuilder.setRouteLevel(2, 2);
		@SuppressWarnings("unused")
		InsertionStrategy bestInsertion = bestIBuilder.build();
		
	}
	
	@Test
	public void whenHavingOnlyServicesInOneProblem_andInsertionShouldBeMadeOnRouteLevel_itShouldAssertTrue(){
		/* get a vehicle type-builder and build a type with the typeId "vehicleType" and a capacity of 2
		 */
		VehicleTypeImpl.Builder vehicleTypeBuilder = VehicleTypeImpl.Builder.newInstance("vehicleType", 2);
		VehicleType vehicleType = vehicleTypeBuilder.build();
		
		/*
		 * get a vehicle-builder and build a vehicle located at (10,10) with type "vehicleType"
		 */
		Builder vehicleBuilder = VehicleImpl.Builder.newInstance("vehicle");
		vehicleBuilder.setLocationCoord(Coordinate.newInstance(10, 10));
		vehicleBuilder.setType(vehicleType);
		Vehicle vehicle = vehicleBuilder.build();
		
		/*
		 * build shipments at the required locations, each with a capacity-demand of 1.
		 * 4 shipments
		 * 1: (5,7)->(6,9)
		 * 2: (5,13)->(6,11)
		 * 3: (15,7)->(14,9)
		 * 4: (15,13)->(14,11)
		 */
//		
//		Shipment shipment1 = Shipment.Builder.newInstance("1", 1).setPickupCoord(Coordinate.newInstance(5, 7)).setDeliveryCoord(Coordinate.newInstance(6, 9)).build();
//		Shipment shipment2 = Shipment.Builder.newInstance("2", 1).setPickupCoord(Coordinate.newInstance(5, 13)).setDeliveryCoord(Coordinate.newInstance(6, 11)).build();
//		
//		Shipment shipment3 = Shipment.Builder.newInstance("3", 1).setPickupCoord(Coordinate.newInstance(15, 7)).setDeliveryCoord(Coordinate.newInstance(14, 9)).build();
//		Shipment shipment4 = Shipment.Builder.newInstance("4", 1).setPickupCoord(Coordinate.newInstance(15, 13)).setDeliveryCoord(Coordinate.newInstance(14, 11)).build();
//		
		/*
		 * build deliveries, (implicitly picked up in the depot)
		 * 1: (4,8)
		 * 2: (4,12)
		 * 3: (16,8)
		 * 4: (16,12)
		 */
		Delivery delivery1 = (Delivery) Delivery.Builder.newInstance("5", 1).setCoord(Coordinate.newInstance(4, 8)).build();
		Delivery delivery2 = (Delivery) Delivery.Builder.newInstance("6", 1).setCoord(Coordinate.newInstance(4, 12)).build();
		Delivery delivery3 = (Delivery) Delivery.Builder.newInstance("7", 1).setCoord(Coordinate.newInstance(16, 8)).build();
		Delivery delivery4 = (Delivery) Delivery.Builder.newInstance("8", 1).setCoord(Coordinate.newInstance(16, 12)).build();
		
		VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
		vrpBuilder.addVehicle(vehicle)
//		vrpBuilder.addJob(shipment1).addJob(shipment2).addJob(shipment3).addJob(shipment4)
			.addJob(delivery1).addJob(delivery2).addJob(delivery3).addJob(delivery4).build();
		
		VehicleRoutingProblem vrp = vrpBuilder.build();
		
		final StateManager stateManager = new StateManager(vrp);

		ConstraintManager constraintManager = new ConstraintManager(vrp,stateManager);
		constraintManager.addLoadConstraint();
		constraintManager.addTimeWindowConstraint();
		
		VehicleFleetManager fleetManager = new InfiniteFleetManagerFactory(vrp.getVehicles()).createFleetManager();

		BestInsertionBuilder bestIBuilder = new BestInsertionBuilder(vrp, fleetManager, stateManager,constraintManager);
		bestIBuilder.setRouteLevel(2, 2);
		@SuppressWarnings("unused")
		InsertionStrategy bestInsertion = bestIBuilder.build();
		
		assertTrue(true);
		
	}

}
