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
package algorithms;

import basics.route.Driver;
import basics.route.Vehicle;



class InsertionData {
	
	static class NoInsertionFound extends InsertionData{
		
		public NoInsertionFound() {
			super(Double.MAX_VALUE, NO_INDEX, NO_INDEX, null, null);
		}

	}
	
	private static InsertionData noInsertion = new NoInsertionFound();
	
	public static InsertionData noInsertionFound(){
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
		return "[iCost="+insertionCost+"][iIndex="+deliveryInsertionIndex+"][depTime="+departureTime+"][vehicle="+selectedVehicle+"][driver="+selectedDriver+"]";
	}
	
	public int getDeliveryInsertionIndex(){
		return deliveryInsertionIndex;
	}
	
	public int getPickupInsertionIndex(){
		return pickupInsertionIndex;
	}
	
	public double getInsertionCost() {
		return insertionCost;
	}

	public Vehicle getSelectedVehicle() {
		return selectedVehicle;
	}
	
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
