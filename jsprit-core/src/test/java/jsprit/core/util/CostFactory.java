package jsprit.core.util;

import jsprit.core.problem.cost.VehicleRoutingTransportCosts;

public class CostFactory {
	
	public static VehicleRoutingTransportCosts createManhattanCosts(){
		Locations locations = new Locations(){

			@Override
			public Coordinate getCoord(String id) {
				//assume: locationId="x,y"
				String[] splitted = id.split(",");
				return Coordinate.newInstance(Double.parseDouble(splitted[0]), 
						Double.parseDouble(splitted[1]));
			}
			
		};
		return new ManhattanCosts(locations);
	}
	
	public static VehicleRoutingTransportCosts createEuclideanCosts(){
		Locations locations = new Locations(){

			@Override
			public Coordinate getCoord(String id) {
				//assume: locationId="x,y"
				String[] splitted = id.split(",");
				return Coordinate.newInstance(Double.parseDouble(splitted[0]), 
						Double.parseDouble(splitted[1]));
			}
			
		};
		return new CrowFlyCosts(locations);
	}
}
