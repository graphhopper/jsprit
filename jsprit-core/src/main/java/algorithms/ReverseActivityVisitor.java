package algorithms;

import basics.route.TourActivity;
import basics.route.VehicleRoute;

public interface ReverseActivityVisitor {
	
	public void begin(VehicleRoute route);
	
	public void visit(TourActivity activity);
	
	public void finish();

}
