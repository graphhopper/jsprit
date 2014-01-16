package jsprit.core.problem.vehicle;

import java.util.Arrays;

import static org.mockito.Mockito.*;
import org.junit.Test;

public class FiniteVehicleFleetManagerFactoryTest {
	
	@Test
	public void whenFiniteVehicleManagerIsCreated_itShouldReturnCorrectManager(){
		VehicleFleetManager vfm = new FiniteFleetManagerFactory(Arrays.asList(mock(Vehicle.class))).createFleetManager();
		
	}

}
