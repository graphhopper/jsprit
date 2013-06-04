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
package basics;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import algorithms.TestVehicleFleetManager;
import basics.algo.SearchStrategyManagerTest;
import basics.algo.SearchStrategyTest;
import basics.io.VrpReaderV2Test;
import basics.io.VrpWriterV2Test;
import basics.io.VrpWriterV3Test;
import basics.route.ServiceActTest;
import basics.route.TestTour;
import basics.route.TestVehicleRoute;




@RunWith(Suite.class)
@Suite.SuiteClasses({
	SearchStrategyManagerTest.class,
	SearchStrategyTest.class,
	TestTour.class,
	TestVehicleFleetManager.class,
	TestVehicleRoute.class,
    ServiceActTest.class,
    ServiceTest.class,
    VehicleRoutingProblemBuilderTest.class,
    VrpReaderV2Test.class,
    VrpWriterV2Test.class,
    VrpWriterV3Test.class
	
})
public class BasicsSuite {}
