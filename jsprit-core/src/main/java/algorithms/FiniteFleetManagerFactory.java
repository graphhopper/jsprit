package algorithms;

import java.util.Collection;

import basics.route.Vehicle;

public class FiniteFleetManagerFactory implements VehicleFleetManagerFactory{

	private Collection<Vehicle> vehicles;
	
	public FiniteFleetManagerFactory(Collection<Vehicle> vehicles) {
		super();
		this.vehicles = vehicles;
	}

	@Override
	public VehicleFleetManager createFleetManager() {
		return new VehicleFleetManagerImpl(vehicles);
	}

}
