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
