package jsprit.core.problem.vehicle;

import java.util.Collection;

/**
 * Factory that creates a finite fleetmanager.
 *
 */

public class FiniteFleetManagerFactory implements VehicleFleetManagerFactory{

	private Collection<Vehicle> vehicles;
	
        /**
         * Constucts the factory.
         */
	public FiniteFleetManagerFactory(Collection<Vehicle> vehicles) {
		super();
		this.vehicles = vehicles;
	}

        /**
         * Creates the finite fleetmanager.
         */
	@Override
	public VehicleFleetManager createFleetManager() {
		return new VehicleFleetManagerImpl(vehicles);
	}

}
