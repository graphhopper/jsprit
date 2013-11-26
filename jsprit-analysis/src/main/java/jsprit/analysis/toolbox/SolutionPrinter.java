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
package jsprit.analysis.toolbox;

import jsprit.core.problem.solution.VehicleRoutingProblemSolution;

/**
 * Printer to print the details of a vehicle-routing-problem solution.
 * 
 * @author stefan schroeder
 *
 */
public class SolutionPrinter {
	
	/**
	 * Enum to indicate verbose-level.
	 * 
	 * <p> Print.CONCISE and Print.VERBOSE are available.
	 * 
	 * @author stefan schroeder
	 *
	 */
	public enum Print {
		
		CONCISE,VERBOSE 
	}
	
	/**
	 * Prints costs and #vehicles to stdout (System.out.println).
	 * 
	 * @param solution
	 */
	public static void print(VehicleRoutingProblemSolution solution){
		System.out.println("[costs="+solution.getCost() + "]");
		System.out.println("[#vehicles="+solution.getRoutes().size() + "]");
		
	}
	
//	/**
//	 * Prints the details of the solution according to a print-level, i.e. Print.CONCISE or PRINT.VERBOSE.
//	 * 
//	 * <p>CONCISE prints total-costs and #vehicles.
//	 * <p>VERBOSE prints the route-details additionally. If the DefaultVehicleRouteCostCalculator (which is the standard-calculator) 
//	 * is used in VehicleRoute, then route-costs are differentiated further between transport, activity, vehicle, driver and other-costs.
//	 * 
//	 * @param solution
//	 * @param level
//	 * 
//	 * @deprecated is not going to work anymore
//	 */
//	@Deprecated
//	public static void print(VehicleRoutingProblemSolution solution, Print level){
//		if(level.equals(Print.CONCISE)){
//			print(solution);
//		}
//		else{
//			print(solution);
//			System.out.println("routes");
//			int routeCount = 1;
//			for(VehicleRoute route : solution.getRoutes()){
//				System.out.println("[route="+routeCount+"][departureTime="+route.getStart().getEndTime()+"[total=" + route.getCost() +  "]");
//				if(route.getVehicleRouteCostCalculator() instanceof DefaultVehicleRouteCostCalculator){
//					DefaultVehicleRouteCostCalculator defaultCalc = (DefaultVehicleRouteCostCalculator) route.getVehicleRouteCostCalculator();
//					System.out.println("[transport=" + defaultCalc.getTpCosts() + "][activity=" + defaultCalc.getActCosts() + 
//							"][vehicle=" + defaultCalc.getVehicleCosts() + "][driver=" + defaultCalc.getDriverCosts() + "][other=" + defaultCalc.getOther() + "]");
//				}
//				routeCount++;
//			}
//		}
//		
//		
//	}
	
}
