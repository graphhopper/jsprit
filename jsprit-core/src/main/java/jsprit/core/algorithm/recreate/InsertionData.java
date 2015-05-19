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

import jsprit.core.problem.driver.Driver;
import jsprit.core.problem.vehicle.Vehicle;

import java.util.ArrayList;
import java.util.List;

/**
 * Data object that collects insertion information. It collects insertionCosts, insertionIndeces, vehicle and driver to be employed
 * and departureTime of vehicle at vehicle's start location (e.g. depot).
 * 
 * @author stefan
 *
 */
public class InsertionData {
	
	public static class NoInsertionFound extends InsertionData{
		
		public NoInsertionFound() {
			super(Double.MAX_VALUE, NO_INDEX, NO_INDEX, null, null);
		}

	}
	
	private static InsertionData noInsertion = new NoInsertionFound();
	
	/**
	 * Returns an instance of InsertionData that represents an EmptyInsertionData (which might indicate
	 * that no insertion has been found). It is internally instantiated as follows:<br>
	 * <code>new InsertionData(Double.MAX_VALUE, NO_INDEX, NO_INDEX, null, null);</code><br>
	 * where NO_INDEX=-1. 
	 * 
	 * @return
	 */
	public static InsertionData createEmptyInsertionData(){
		return noInsertion;
	}
	
	static int NO_INDEX = -1;

	private final double insertionCost;
	
	private final int pickupInsertionIndex;
	
	private final int deliveryInsertionIndex;
	
	private final Vehicle selectedVehicle;
	
	private final Driver selectedDriver;
	
	private double departureTime;
	
	private double additionalTime;

	private List<Event> events = new ArrayList<Event>();

	List<Event> getEvents(){
		return events;
	}

	/**
	 * @return the additionalTime
	 */
	public double getAdditionalTime() {
		return additionalTime;
	}

	/**
	 * @param additionalTime the additionalTime to set
	 */
	public void setAdditionalTime(double additionalTime) {
		this.additionalTime = additionalTime;
	}

	public InsertionData(double insertionCost, int pickupInsertionIndex, int deliveryInsertionIndex, Vehicle vehicle, Driver driver){
		this.insertionCost = insertionCost;
		this.pickupInsertionIndex = pickupInsertionIndex;
		this.deliveryInsertionIndex = deliveryInsertionIndex;
		this.selectedVehicle = vehicle;
		this.selectedDriver = driver;
	}
	
	@Override
	public String toString() {
		return "[iCost="+insertionCost+"][pickupIndex="+pickupInsertionIndex+"][deliveryIndex="+deliveryInsertionIndex+"][depTime="+departureTime+"][vehicle="+selectedVehicle+"][driver="+selectedDriver+"]";
	}
	
	/**
	 * Returns insertionIndex of deliveryActivity. If no insertionPosition is found, it returns NO_INDEX (=-1). 
	 * 
	 * @return
	 */
	public int getDeliveryInsertionIndex(){
		return deliveryInsertionIndex;
	}
	
	/**
	 * Returns insertionIndex of pickkupActivity. If no insertionPosition is found, it returns NO_INDEX (=-1).
	 * 
	 * @return
	 */
	public int getPickupInsertionIndex(){
		return pickupInsertionIndex;
	}
	
	/**
	 * Returns insertion costs (which might be the additional costs of inserting the corresponding job).
	 * 
	 * @return
	 */
	public double getInsertionCost() {
		return insertionCost;
	}

	/**
	 * Returns the vehicle to be employed.
	 * 
	 * @return
	 */
	public Vehicle getSelectedVehicle() {
		return selectedVehicle;
	}
	
	/**
	 * Returns the vehicle to be employed.
	 * 
	 * @return
	 */
	public Driver getSelectedDriver(){
		return selectedDriver;
	}

	/**
	 * @return the departureTime
	 */
	public double getVehicleDepartureTime() {
		return departureTime;
	}

	/**
	 * @param departureTime the departureTime to set
	 */
	public void setVehicleDepartureTime(double departureTime) {
		this.departureTime = departureTime;
	}
	
	
	
}
