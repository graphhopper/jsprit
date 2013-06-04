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
package util;

import java.util.Collection;

import basics.route.TourActivity;
import basics.route.VehicleRoute;




public class RouteUtils {

//	public static double getTransportCosts(Collection<VehicleRoute> routes) {
//		double cost = 0.0;
//		for (VehicleRoute r : routes) {
//			if(r.getTour().isEmpty()){
//				continue;
//			}
//			cost += r.getTour().tourData.transportCosts;
//		}
//		return cost;
//	}
//
//	public static double getTransportTime(Collection<VehicleRoute> routes) {
//		double time = 0.0;
//		for (VehicleRoute r : routes) {
//			if(r.getTour().isEmpty()){
//				continue;
//			}
//			time += r.getTour().tourData.transportTime;
//		}
//		return time;
//	}
	
	public static double getTotalCost(Collection<VehicleRoute> routes){
		double total = 0.0;
		for (VehicleRoute r : routes) {
			if(r.isEmpty()) {
				total += 0.0;
			}
			else total += r.getCost();
		}
		return total;
	}
	
	
	public static double getTotalServiceTime(Collection<VehicleRoute> routes){
		double total = 0.0;
		for (VehicleRoute r : routes) {
			if(r.getTourActivities().isEmpty()){
				continue;
			}
			for(TourActivity act : r.getTourActivities().getActivities()){
				total += act.getOperationTime();
			}
		}
		return total;
	}
	
	
	public static double getTotalFixCost(Collection<VehicleRoute> routes){
		double total = 0.0;
		for (VehicleRoute r : routes) {
			if(r.getTourActivities().isEmpty()){
				continue;
			}
			total += r.getVehicle().getType().vehicleCostParams.fix;
		}
		return total;
	}
	
	public static int getNuOfActiveRoutes(Collection<VehicleRoute> routes){
		int count = 0;
		for (VehicleRoute r : routes) {
			if(r.isEmpty()){
				continue;
			}
			count++;
		}
		return count;
	}
}
