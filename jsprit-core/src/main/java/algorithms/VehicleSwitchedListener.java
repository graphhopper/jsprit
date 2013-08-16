package algorithms;

import basics.algo.InsertionListener;
import basics.route.Vehicle;
import basics.route.VehicleRoute;

interface VehicleSwitchedListener extends InsertionListener{
	
	public void vehicleSwitched(VehicleRoute vehicleRoute, Vehicle oldVehicle, Vehicle newVehicle);

}
