package jsprit.core.algorithm.recreate;

import static org.mockito.Mockito.mock;
import jsprit.core.problem.cost.VehicleRoutingActivityCosts;
import jsprit.core.problem.cost.VehicleRoutingTransportCosts;

import org.junit.Before;
import org.junit.Test;

public class TestLocalActivityInsertionCostsCalculator {

	VehicleRoutingTransportCosts tpCosts;
	
	VehicleRoutingActivityCosts actCosts;
	
	LocalActivityInsertionCostsCalculator calc;
	
	@Before
	public void doBefore(){
		tpCosts = mock(VehicleRoutingTransportCosts.class);
		actCosts = mock(VehicleRoutingActivityCosts.class);
		calc = new LocalActivityInsertionCostsCalculator(tpCosts, actCosts);
	}
	
	@Test
	public void whenInsertingActBetweenTwoRouteActs_itCalcsMarginalTpCosts(){
		VehicleRoutingTransportCosts tpCosts = mock(VehicleRoutingTransportCosts.class);
		VehicleRoutingActivityCosts actCosts = mock(VehicleRoutingActivityCosts.class);
		
		LocalActivityInsertionCostsCalculator calc = new LocalActivityInsertionCostsCalculator(tpCosts, actCosts);
	}

	@Test
	public void whenInsertingActBeforeEndANDRouteIsOpen_itReturnsTransportCostsToLastAct(){
		
	}
	
	@Test
	public void whenInsertingActBeforeEndANDRouteIsClosed_itReturnsMarginalTpCosts(){
		
	}
}
