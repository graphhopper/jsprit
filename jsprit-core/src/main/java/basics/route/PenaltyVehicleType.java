package basics.route;

import basics.route.VehicleTypeImpl.VehicleCostParams;

public class PenaltyVehicleType implements VehicleType{

	private VehicleType type;
	
	public PenaltyVehicleType(VehicleType type) {
		super();
		this.type = type;
	}

	@Override
	public String getTypeId() {
		return type.getTypeId();
	}

	@Override
	public int getCapacity() {
		return type.getCapacity();
	}

	@Override
	public VehicleCostParams getVehicleCostParams() {
		return type.getVehicleCostParams();
	}

	@Override
	public double getMaxVelocity() {
		return type.getMaxVelocity();
	}

	

}
