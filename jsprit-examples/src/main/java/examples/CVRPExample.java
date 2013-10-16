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
package examples;

import readers.ChristofidesReader;
import basics.VehicleRoutingProblem;
import basics.io.VrpXMLWriter;

public class CVRPExample {
	
	public static void main(String[] args) {
		VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
		new ChristofidesReader(builder).read("input/vrpnc1.txt");
		VehicleRoutingProblem vrp = builder.build();
		new VrpXMLWriter(vrp).write("input/vrpnc1-jsprit.xml");
	}

}
