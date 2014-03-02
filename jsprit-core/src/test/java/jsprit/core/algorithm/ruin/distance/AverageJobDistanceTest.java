package jsprit.core.algorithm.ruin.distance;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import jsprit.core.algorithm.ruin.distance.AvgServiceAndShipmentDistance;
import jsprit.core.problem.job.Service;
import jsprit.core.problem.job.Shipment;
import jsprit.core.util.Coordinate;
import jsprit.core.util.CrowFlyCosts;
import jsprit.core.util.Locations;

import org.junit.Before;
import org.junit.Test;


public class AverageJobDistanceTest {

	
	private CrowFlyCosts routingCosts;

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
		routingCosts = new CrowFlyCosts(locations);

	}
	
	@Test
	public void distanceOfTwoEqualShipmentsShouldBeSmallerThanAnyOtherDistance(){
		Shipment s1 = Shipment.Builder.newInstance("s1").addSizeDimension(0, 1).setPickupLocation("0,0").setDeliveryLocation("10,10").build();
		Shipment s2 = Shipment.Builder.newInstance("s2").addSizeDimension(0, 1).setPickupLocation("0,0").setDeliveryLocation("10,10").build();
		
		double dist = new AvgServiceAndShipmentDistance(routingCosts).getDistance(s1, s2);
		
		for(int i=0;i<10;i++){
			for(int j=0;j<10;j++){
				Shipment other1 = Shipment.Builder.newInstance("s1").addSizeDimension(0, 1).setPickupLocation("0,0").setDeliveryLocation(i+","+j).build();
				Shipment other2 = Shipment.Builder.newInstance("s2").addSizeDimension(0, 1).setPickupLocation("0,0").setDeliveryLocation("10,10").build();
				double dist2 = new AvgServiceAndShipmentDistance(routingCosts).getDistance(other1, other2);
				System.out.println("("+i+","+j+"), dist=" + dist + ", dist2=" + dist2);
				assertTrue(dist<=dist2+dist2*0.001);
			}
		}
	}
	
	
	
	@Test
	public void whenServicesHaveSameLocation_distanceShouldBeZero(){
		Service s1 = Service.Builder.newInstance("s1").addSizeDimension(0, 1).setLocationId("10,0").build();
		Service s2 = Service.Builder.newInstance("s2").addSizeDimension(0, 1).setLocationId("10,0").build();
		
		double dist = new AvgServiceAndShipmentDistance(routingCosts).getDistance(s1, s2);
		assertEquals(0.0,dist,0.01);
	}
}
