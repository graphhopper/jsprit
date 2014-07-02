package jsprit.core.util;

import jsprit.core.problem.cost.VehicleRoutingTransportCosts;

public class CostFactory {

    /**
     * Return manhattanCosts.
     * <p>
     * This retrieves coordinates from locationIds. LocationId has to be locId="{x},{y}". For example,
     * locId="10,10" is interpreted such that x=10 and y=10.
     *
     * @return manhattanCosts
     */
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

    /**
     * Return euclideanCosts.
     * <p>
     * This retrieves coordinates from locationIds. LocationId has to be locId="{x},{y}". For example,
     * locId="10,10" is interpreted such that x=10 and y=10.
     *
     * @return euclidean
     */
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
