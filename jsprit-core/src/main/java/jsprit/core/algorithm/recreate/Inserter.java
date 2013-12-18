/*******************************************************************************
 * Copyright (C) 2013  Stefan Schroeder
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either 
 * version 3.0 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package jsprit.core.algorithm.recreate;

import jsprit.core.algorithm.recreate.InsertionData.NoInsertionFound;
import jsprit.core.algorithm.recreate.listener.InsertionListeners;
import jsprit.core.problem.job.Job;
import jsprit.core.problem.job.Service;
import jsprit.core.problem.job.Shipment;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.solution.route.activity.DefaultShipmentActivityFactory;
import jsprit.core.problem.solution.route.activity.DefaultTourActivityFactory;
import jsprit.core.problem.solution.route.activity.TourActivity;
import jsprit.core.problem.solution.route.activity.TourActivityFactory;
import jsprit.core.problem.solution.route.activity.TourShipmentActivityFactory;

class Inserter {
	
	interface JobInsertionHandler {
		void handleJobInsertion(Job job, InsertionData iData, VehicleRoute route);
		
		void setNextHandler(JobInsertionHandler handler);
	}
	
	class JobExceptionHandler implements JobInsertionHandler{

		@Override
		public void handleJobInsertion(Job job, InsertionData iData,VehicleRoute route) {
			throw new IllegalStateException("job insertion is not supported. Do not know job type.");
		}

		@Override
		public void setNextHandler(JobInsertionHandler handler) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	class ServiceInsertionHandler implements JobInsertionHandler{

		private TourActivityFactory activityFactory = new DefaultTourActivityFactory();
		
		private JobInsertionHandler delegator = new JobExceptionHandler();
		
		@Override
		public void handleJobInsertion(Job job, InsertionData iData, VehicleRoute route) {
			if(job instanceof Service){
				if(!iData.getSelectedVehicle().isReturnToDepot()){
					if(iData.getDeliveryInsertionIndex()>=route.getTourActivities().getActivities().size()){
						setEndLocation(route,(Service)job);
					}
				}
				route.getTourActivities().addActivity(iData.getDeliveryInsertionIndex(), this.activityFactory.createActivity((Service)job));
				route.setDepartureTime(iData.getVehicleDepartureTime());
			}
			else delegator.handleJobInsertion(job, iData, route);
		}
		
		private void setEndLocation(VehicleRoute route, Service service) {
			route.getEnd().setLocationId(service.getLocationId());
		}
		
		public void setNextHandler(JobInsertionHandler jobInsertionHandler){
			this.delegator = jobInsertionHandler;
		}
		
	}
	
	class ShipmentInsertionHandler implements JobInsertionHandler {
		
		private TourShipmentActivityFactory activityFactory = new DefaultShipmentActivityFactory();
		
		private JobInsertionHandler delegator = new JobExceptionHandler();
		
		@Override
		public void handleJobInsertion(Job job, InsertionData iData, VehicleRoute route) {
			if(job instanceof Shipment){
				TourActivity pickupShipment = this.activityFactory.createPickup((Shipment)job);
				TourActivity deliverShipment = this.activityFactory.createDelivery((Shipment)job);
				if(!iData.getSelectedVehicle().isReturnToDepot()){
					if(iData.getDeliveryInsertionIndex()>=route.getActivities().size()){
						setEndLocation(route,(Shipment)job);
					}
				}
				route.getTourActivities().addActivity(iData.getDeliveryInsertionIndex(), deliverShipment);
				route.getTourActivities().addActivity(iData.getPickupInsertionIndex(), pickupShipment);
				route.setDepartureTime(iData.getVehicleDepartureTime());
			}
			else delegator.handleJobInsertion(job, iData, route);
		}
		
		private void setEndLocation(VehicleRoute route, Shipment shipment) {
			route.getEnd().setLocationId(shipment.getDeliveryLocation());
		}

		public void setNextHandler(JobInsertionHandler jobInsertionHandler){
			this.delegator = jobInsertionHandler;
		}
		
	}

	private InsertionListeners insertionListeners;
	
	private JobInsertionHandler jobInsertionHandler;
	
	public Inserter(InsertionListeners insertionListeners) {
		this.insertionListeners = insertionListeners;
		new DefaultTourActivityFactory();
		jobInsertionHandler = new ServiceInsertionHandler();
		jobInsertionHandler.setNextHandler(new ShipmentInsertionHandler());
	}

	public void insertJob(Job job, InsertionData insertionData, VehicleRoute vehicleRoute){
		insertionListeners.informBeforeJobInsertion(job, insertionData, vehicleRoute);
		
		if(insertionData == null || (insertionData instanceof NoInsertionFound)) throw new IllegalStateException("insertionData null. cannot insert job.");
		if(job == null) throw new IllegalStateException("cannot insert null-job");
		if(!(vehicleRoute.getVehicle().getId().toString().equals(insertionData.getSelectedVehicle().getId().toString()))){
			insertionListeners.informVehicleSwitched(vehicleRoute, vehicleRoute.getVehicle(), insertionData.getSelectedVehicle());
//			log.debug("vehicle switched from " + vehicleRoute.getVehicle().getId() + " to " + insertionData.getSelectedVehicle().getId());
			vehicleRoute.setVehicle(insertionData.getSelectedVehicle(), insertionData.getVehicleDepartureTime());
		}
		jobInsertionHandler.handleJobInsertion(job, insertionData, vehicleRoute);
		insertionListeners.informJobInserted(job, vehicleRoute, insertionData.getInsertionCost(), insertionData.getAdditionalTime());
	}
}
