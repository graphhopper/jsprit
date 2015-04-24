/*******************************************************************************
 * Copyright (C) 2014  Stefan Schroeder
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
import jsprit.core.problem.AbstractActivity;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.job.Job;
import jsprit.core.problem.job.Service;
import jsprit.core.problem.job.Shipment;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.solution.route.activity.*;

import java.util.List;

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

		}
		
	}
	
	class ServiceInsertionHandler implements JobInsertionHandler{

		private TourActivityFactory activityFactory = new DefaultTourActivityFactory();
		
		private JobInsertionHandler delegator = new JobExceptionHandler();

        private VehicleRoutingProblem vehicleRoutingProblem;

        public ServiceInsertionHandler(VehicleRoutingProblem vehicleRoutingProblem) {
            this.vehicleRoutingProblem = vehicleRoutingProblem;
        }

        @Override
		public void handleJobInsertion(Job job, InsertionData iData, VehicleRoute route) {
			if(job instanceof Service){
				route.setVehicleAndDepartureTime(iData.getSelectedVehicle(),iData.getVehicleDepartureTime());
				if(!iData.getSelectedVehicle().isReturnToDepot()){
					if(iData.getDeliveryInsertionIndex()>=route.getTourActivities().getActivities().size()){
						setEndLocation(route,(Service)job);
					}
				}
                TourActivity activity = vehicleRoutingProblem.copyAndGetActivities(job).get(0);
                route.getTourActivities().addActivity(iData.getDeliveryInsertionIndex(), activity);
			}
			else delegator.handleJobInsertion(job, iData, route);
		}
		
		private void setEndLocation(VehicleRoute route, Service service) {
			route.getEnd().setLocation(service.getLocation());
		}
		
		public void setNextHandler(JobInsertionHandler jobInsertionHandler){
			this.delegator = jobInsertionHandler;
		}
		
	}
	
	class ShipmentInsertionHandler implements JobInsertionHandler {

        private final VehicleRoutingProblem vehicleRoutingProblem;

        private TourShipmentActivityFactory activityFactory = new DefaultShipmentActivityFactory();
		
		private JobInsertionHandler delegator = new JobExceptionHandler();

        public ShipmentInsertionHandler(VehicleRoutingProblem vehicleRoutingProblem) {
            this.vehicleRoutingProblem = vehicleRoutingProblem;
        }

        @Override
		public void handleJobInsertion(Job job, InsertionData iData, VehicleRoute route) {
			if(job instanceof Shipment){
                List<AbstractActivity> acts = vehicleRoutingProblem.copyAndGetActivities(job);
				TourActivity pickupShipment = acts.get(0);
				TourActivity deliverShipment = acts.get(1);
				route.setVehicleAndDepartureTime(iData.getSelectedVehicle(),iData.getVehicleDepartureTime());
				if(!iData.getSelectedVehicle().isReturnToDepot()){
					if(iData.getDeliveryInsertionIndex()>=route.getActivities().size()){
						setEndLocation(route,(Shipment)job);
					}
				}
				route.getTourActivities().addActivity(iData.getDeliveryInsertionIndex(), deliverShipment);
				route.getTourActivities().addActivity(iData.getPickupInsertionIndex(), pickupShipment);
			}
			else delegator.handleJobInsertion(job, iData, route);
		}
		
		private void setEndLocation(VehicleRoute route, Shipment shipment) {
			route.getEnd().setLocation(shipment.getDeliveryLocation());
		}

		public void setNextHandler(JobInsertionHandler jobInsertionHandler){
			this.delegator = jobInsertionHandler;
		}
		
	}

	private InsertionListeners insertionListeners;
	
	private JobInsertionHandler jobInsertionHandler;

    private VehicleRoutingProblem vehicleRoutingProblem;
	
	public Inserter(InsertionListeners insertionListeners, VehicleRoutingProblem vehicleRoutingProblem) {
		this.insertionListeners = insertionListeners;
		new DefaultTourActivityFactory();
		jobInsertionHandler = new ServiceInsertionHandler(vehicleRoutingProblem);
		jobInsertionHandler.setNextHandler(new ShipmentInsertionHandler(vehicleRoutingProblem));
	}

	public void insertJob(Job job, InsertionData insertionData, VehicleRoute vehicleRoute){
		insertionListeners.informBeforeJobInsertion(job, insertionData, vehicleRoute);
		
		if(insertionData == null || (insertionData instanceof NoInsertionFound)) throw new IllegalStateException("insertionData null. cannot insert job.");
		if(job == null) throw new IllegalStateException("cannot insert null-job");
		if(!(vehicleRoute.getVehicle().getId().equals(insertionData.getSelectedVehicle().getId()))){
			insertionListeners.informVehicleSwitched(vehicleRoute, vehicleRoute.getVehicle(), insertionData.getSelectedVehicle());
			vehicleRoute.setVehicleAndDepartureTime(insertionData.getSelectedVehicle(), insertionData.getVehicleDepartureTime());
		}
		jobInsertionHandler.handleJobInsertion(job, insertionData, vehicleRoute);

		insertionListeners.informJobInserted(job, vehicleRoute, insertionData.getInsertionCost(), insertionData.getAdditionalTime());
	}
}
