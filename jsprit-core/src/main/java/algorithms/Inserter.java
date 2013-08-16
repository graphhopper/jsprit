package algorithms;

import algorithms.InsertionData.NoInsertionFound;
import basics.Job;
import basics.Service;
import basics.route.ServiceActivity;
import basics.route.VehicleRoute;

class Inserter {

	private InsertionListeners insertionListeners;
	
	public Inserter(InsertionListeners insertionListeners) {
		this.insertionListeners = insertionListeners;
	}

	public void insertJob(Job job, InsertionData insertionData, VehicleRoute vehicleRoute){
		insertionListeners.informBeforeJobInsertion(job, insertionData, vehicleRoute);
		
		if(insertionData == null || (insertionData instanceof NoInsertionFound)) throw new IllegalStateException("insertionData null. cannot insert job.");
		if(job == null) throw new IllegalStateException("cannot insert null-job");
		if(!(vehicleRoute.getVehicle().getId().toString().equals(insertionData.getSelectedVehicle().getId().toString()))){
			insertionListeners.informVehicleSwitched(vehicleRoute, vehicleRoute.getVehicle(), insertionData.getSelectedVehicle());
			vehicleRoute.setVehicle(insertionData.getSelectedVehicle(), insertionData.getVehicleDepartureTime());
		}
//		if(vehicleRoute.getDepartureTime() != vehicleRoute.g)
		if(job instanceof Service) {
			vehicleRoute.getTourActivities().addActivity(insertionData.getDeliveryInsertionIndex(), ServiceActivity.newInstance((Service)job));
			vehicleRoute.setDepartureTime(insertionData.getVehicleDepartureTime());
		}
		else throw new IllegalStateException("neither service nor shipment. this is not supported.");
		
		insertionListeners.informJobInserted(job, vehicleRoute);
//		updateTour(vehicleRoute);
	}
}
