/*******************************************************************************
 * Copyright (c) 2011 Stefan Schroeder.
 * eMail: stefan.schroeder@kit.edu
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan Schroeder - initial API and implementation
 ******************************************************************************/
package util;



public class EuclideanDistanceCalculator {

	public static double calculateDistance(Coordinate coord1, Coordinate coord2) {
		double xDiff = coord1.getX() - coord2.getX();
		double yDiff = coord1.getY() - coord2.getY();
		return Math.sqrt((xDiff * xDiff) + (yDiff * yDiff));
	}

}
