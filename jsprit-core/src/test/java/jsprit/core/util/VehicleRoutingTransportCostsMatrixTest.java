package jsprit.core.util;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jsprit.core.problem.vehicle.Vehicle;
import jsprit.core.problem.vehicle.VehicleType;
import jsprit.core.problem.vehicle.VehicleTypeImpl;

import org.junit.Test;

public class VehicleRoutingTransportCostsMatrixTest {
	
	@Test
	public void whenAddingDistanceToSymmetricMatrix_itShouldReturnCorrectValues(){
		VehicleRoutingTransportCostsMatrix.Builder matrixBuilder = VehicleRoutingTransportCostsMatrix.Builder.newInstance(true);
		matrixBuilder.addTransportDistance("1", "2", 2.);
		VehicleRoutingTransportCostsMatrix matrix = matrixBuilder.build();
		assertEquals(2.,matrix.getTransportCost("1", "2", 0.0, null, null),0.1);
		assertEquals(2.,matrix.getDistance("1", "2"),0.1);
		assertEquals(2.,matrix.getTransportCost("2", "1", 0.0, null, null),0.1);
		assertEquals(2.,matrix.getDistance("2", "1"),0.1);
	}
	
	@Test
	public void whenAddingDistanceToSymmetricMatrixUsingStringAsKey_itShouldReturnCorrectValues(){
		VehicleRoutingTransportCostsMatrix.Builder matrixBuilder = VehicleRoutingTransportCostsMatrix.Builder.newInstance(true);
		matrixBuilder.addTransportDistance("from", "to", 2.);
		VehicleRoutingTransportCostsMatrix matrix = matrixBuilder.build();
		assertEquals(2.,matrix.getTransportCost("from", "to", 0.0, null, null),0.1);
		assertEquals(2.,matrix.getDistance("from", "to"),0.1);
		assertEquals(2.,matrix.getTransportCost("to", "from", 0.0, null, null),0.1);
		assertEquals(2.,matrix.getDistance("from", "to"),0.1);
	}
	
	@Test
	public void whenAddingDistanceToSymmetricMatrixWhereKeyAlreadyExists_itShouldOverrideValues(){
		VehicleRoutingTransportCostsMatrix.Builder matrixBuilder = VehicleRoutingTransportCostsMatrix.Builder.newInstance(true);
		matrixBuilder.addTransportDistance("from", "to", 2.);
		//overide
		matrixBuilder.addTransportDistance("from", "to", 4.);
		VehicleRoutingTransportCostsMatrix matrix = matrixBuilder.build();
		
		assertEquals(4.,matrix.getTransportCost("from", "to", 0.0, null, null),0.1);
		assertEquals(4.,matrix.getDistance("from", "to"),0.1);
		assertEquals(4.,matrix.getTransportCost("to", "from", 0.0, null, null),0.1);
		assertEquals(4.,matrix.getDistance("from", "to"),0.1);
	}
	
	@Test
	public void whenAddingDistanceToSymmetricMatrixWhereReverseKeyAlreadyExists_itShouldOverrideValues(){
		VehicleRoutingTransportCostsMatrix.Builder matrixBuilder = VehicleRoutingTransportCostsMatrix.Builder.newInstance(true);
		matrixBuilder.addTransportDistance("from", "to", 2.);
		//overide
		matrixBuilder.addTransportDistance("to", "from", 4.);
		VehicleRoutingTransportCostsMatrix matrix = matrixBuilder.build();
		
		assertEquals(4.,matrix.getTransportCost("from", "to", 0.0, null, null),0.1);
		assertEquals(4.,matrix.getDistance("from", "to"),0.1);
		assertEquals(4.,matrix.getTransportCost("to", "from", 0.0, null, null),0.1);
		assertEquals(4.,matrix.getDistance("from", "to"),0.1);
	}
	
	@Test
	public void whenAddingDistanceToAsymmetricMatrix_itShouldReturnCorrectValues(){
		VehicleRoutingTransportCostsMatrix.Builder matrixBuilder = VehicleRoutingTransportCostsMatrix.Builder.newInstance(false);
		matrixBuilder.addTransportDistance("1", "2", 2.);
		VehicleRoutingTransportCostsMatrix matrix = matrixBuilder.build();
		assertEquals(2.,matrix.getTransportCost("1", "2", 0.0, null, null),0.1);
	}
	
	@Test(expected=IllegalStateException.class)
	public void whenRequestingRelationThatDoesNotExist_itShouldThrowException(){
		VehicleRoutingTransportCostsMatrix.Builder matrixBuilder = VehicleRoutingTransportCostsMatrix.Builder.newInstance(false);
		matrixBuilder.addTransportDistance("1", "2", 2.);
		VehicleRoutingTransportCostsMatrix matrix = matrixBuilder.build();
		matrix.getTransportCost("2", "1", 0.0, null, null);
	}
	
	@Test
	public void whenAddingDistanceToAsymmetricMatrixUsingStringAsKey_itShouldReturnCorrectValues(){
		VehicleRoutingTransportCostsMatrix.Builder matrixBuilder = VehicleRoutingTransportCostsMatrix.Builder.newInstance(false);
		matrixBuilder.addTransportDistance("from", "to", 2.);
		matrixBuilder.addTransportDistance("to", "from", 4.);
		VehicleRoutingTransportCostsMatrix matrix = matrixBuilder.build();
		assertEquals(2.,matrix.getTransportCost("from", "to", 0.0, null, null),0.1);
		assertEquals(4.,matrix.getTransportCost("to", "from", 0.0, null, null),0.1);
	}
	
	@Test
	public void whenAddingTimeToSymmetricMatrix_itShouldReturnCorrectValues(){
		VehicleRoutingTransportCostsMatrix.Builder matrixBuilder = VehicleRoutingTransportCostsMatrix.Builder.newInstance(true);
		matrixBuilder.addTransportTime("1", "2", 2.);
		VehicleRoutingTransportCostsMatrix matrix = matrixBuilder.build();
		assertEquals(2.,matrix.getTransportTime("1", "2", 0.0, null, null),0.1);
		assertEquals(2.,matrix.getTransportTime("2", "1", 0.0, null, null),0.1);
	}
	
	@Test
	public void whenAddingTimeToSymmetricMatrixUsingStringAsKey_itShouldReturnCorrectValues(){
		VehicleRoutingTransportCostsMatrix.Builder matrixBuilder = VehicleRoutingTransportCostsMatrix.Builder.newInstance(true);
		matrixBuilder.addTransportTime("from", "to", 2.);
		VehicleRoutingTransportCostsMatrix matrix = matrixBuilder.build();
		assertEquals(2.,matrix.getTransportTime("from", "to", 0.0, null, null),0.1);
		assertEquals(2.,matrix.getTransportTime("to", "from", 0.0, null, null),0.1);
	}
	
	@Test
	public void whenAddingTimeToAsymmetricMatrix_itShouldReturnCorrectValues(){
		VehicleRoutingTransportCostsMatrix.Builder matrixBuilder = VehicleRoutingTransportCostsMatrix.Builder.newInstance(false);
		matrixBuilder.addTransportTime("1", "2", 2.);
		VehicleRoutingTransportCostsMatrix matrix = matrixBuilder.build();
		assertEquals(2.,matrix.getTransportTime("1", "2", 0.0, null, null),0.1);
	}
	
	@Test(expected=IllegalStateException.class)
	public void whenRequestingTimeOfRelationThatDoesNotExist_itShouldThrowException(){
		VehicleRoutingTransportCostsMatrix.Builder matrixBuilder = VehicleRoutingTransportCostsMatrix.Builder.newInstance(false);
		matrixBuilder.addTransportTime("1", "2", 2.);
		VehicleRoutingTransportCostsMatrix matrix = matrixBuilder.build();
		matrix.getTransportTime("2", "1", 0.0, null, null);
	}
	
	@Test
	public void whenAddingTimeToAsymmetricMatrixUsingStringAsKey_itShouldReturnCorrectValues(){
		VehicleRoutingTransportCostsMatrix.Builder matrixBuilder = VehicleRoutingTransportCostsMatrix.Builder.newInstance(true);
		matrixBuilder.addTransportTime("from", "to", 2.);
		matrixBuilder.addTransportTime("to", "from", 4.);
		VehicleRoutingTransportCostsMatrix matrix = matrixBuilder.build();
		assertEquals(2.,matrix.getTransportTime("from", "to", 0.0, null, null),0.1);
		assertEquals(4.,matrix.getTransportTime("to", "from", 0.0, null, null),0.1);
	}
	
	@Test
	public void whenAddingTimeToAsymmetricMatrixUsingStringAsKey_itShouldReturnCorrectCostValues(){
		VehicleType type = VehicleTypeImpl.Builder.newInstance("t").setCostPerDistance(0.).setCostPerTime(1.).build();
		Vehicle vehicle = mock(Vehicle.class);
		when(vehicle.getType()).thenReturn(type);
		VehicleRoutingTransportCostsMatrix.Builder matrixBuilder = VehicleRoutingTransportCostsMatrix.Builder.newInstance(true);
		matrixBuilder.addTransportTime("from", "to", 2.);
		matrixBuilder.addTransportTime("to", "from", 4.);
		VehicleRoutingTransportCostsMatrix matrix = matrixBuilder.build();
//		assertEquals(2.,matrix.getTransportTime("from", "to", 0.0, null, null),0.1);
//		assertEquals(4.,matrix.getTransportTime("to", "from", 0.0, null, null),0.1);
		assertEquals(2.,matrix.getTransportCost("from", "to", 0.0, null, vehicle),0.1);
		assertEquals(4.,matrix.getTransportCost("to", "from", 0.0, null, vehicle),0.1);
	}

}
