package basics.route;

import basics.route.VehicleTypeImpl.VehicleCostParams;


public interface VehicleType {

	public String getTypeId();

	public int getCapacity();
	
	public double getMaxVelocity();

	public VehicleCostParams getVehicleCostParams();

}