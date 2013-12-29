package jsprit.core.problem.vehicle;

import java.util.Collection;

/**
 * Factory that constructs a finite fleet manager.
 *
 */

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
