/*******************************************************************************
 * Copyright (C) 2013  Stefan Schroeder
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * 
 * Contributors:
 *     Stefan Schroeder - initial API and implementation
 ******************************************************************************/
package basics.route;



public class DefaultVehicleRouteCostCalculator implements VehicleRouteCostCalculator {

	private double tpCosts = 0.0;
	private double actCosts = 0.0;
	private double vehicleCosts = 0.0;
	private double driverCosts = 0.0;
	private double other = 0.0;
	
	public DefaultVehicleRouteCostCalculator(){}
	
	private DefaultVehicleRouteCostCalculator(DefaultVehicleRouteCostCalculator costCalc){
		this.tpCosts=costCalc.getTpCosts();
		this.actCosts = costCalc.getActCosts();
		this.driverCosts = costCalc.getDriverCosts();
		this.other = costCalc.getOther();
		this.vehicleCosts = costCalc.getVehicleCosts();
	}
	
	public void addTransportCost(double tpCost) {
		this.tpCosts+=tpCost;
	}
	
	public void addActivityCost(double actCost){
		this.actCosts+=actCost;
	}
	
	public void price(Vehicle vehicle){
		if(vehicle != null){
			VehicleType type = vehicle.getType();
			if(type != null){
				this.vehicleCosts = type.getVehicleCostParams().fix;
			}
		}
	}
	
	public void price(Driver driver){
		
	}

	@Override
	public void finish() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void reset() {
		tpCosts = 0.0;
		actCosts = 0.0;
		vehicleCosts = 0.0;
		driverCosts = 0.0;
		other = 0.0;
	}

	@Override
	public void addOtherCost(double cost) {
		this.other = cost;
		
	}

	@Override
	public double getCosts() {
		return tpCosts + actCosts + vehicleCosts + driverCosts + other;
	}

	/**
	 * @return the tpCosts
	 */
	public double getTpCosts() {
		return tpCosts;
	}

	/**
	 * @return the actCosts
	 */
	public double getActCosts() {
		return actCosts;
	}

	/**
	 * @return the vehicleCosts
	 */
	public double getVehicleCosts() {
		return vehicleCosts;
	}

	/**
	 * @return the driverCosts
	 */
	public double getDriverCosts() {
		return driverCosts;
	}

	/**
	 * @return the other
	 */
	public double getOther() {
		return other;
	}

	@Override
	public VehicleRouteCostCalculator duplicate() {
		return new DefaultVehicleRouteCostCalculator(this);
	}
	
	
	
}
