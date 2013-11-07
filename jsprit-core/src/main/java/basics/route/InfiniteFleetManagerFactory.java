package basics.route;

import java.util.Collection;


public class InfiniteFleetManagerFactory implements VehicleFleetManagerFactory{

	private Collection<Vehicle> vehicles;
	
	public InfiniteFleetManagerFactory(Collection<Vehicle> vehicles) {
		super();
		this.vehicles = vehicles;
	}

	@Override
	public VehicleFleetManager createFleetManager() {
		return new InfiniteVehicles(vehicles);
	}

}
