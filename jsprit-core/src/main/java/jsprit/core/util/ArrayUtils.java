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
package jsprit.core.util;

import java.util.Collection;
import java.util.List;

public class ArrayUtils {

	public static double[] toPrimitiveArray(List<Double> list){
		double[] arr = new double[list.size()];
		for(int i=0;i<list.size();i++) arr[i]=list.get(i);
		return arr;
	}

	public static double[] toPrimitiveArray(Collection<Double> collection){
		double[] arr = new double[collection.size()];
		int i=0;
		for(Double d : collection){
			arr[i]=d;
			i++;
		}
		return arr;
	}
	
}
