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
package analysis;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.Range;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import util.Coordinate;
import util.Locations;

import basics.Job;
import basics.Service;
import basics.VehicleRoutingProblem;
import basics.VehicleRoutingProblemSolution;
import basics.route.TourActivity;
import basics.route.Vehicle;
import basics.route.VehicleRoute;


/**
 * A plotter to plot vehicle-routing-solution and routes respectively.
 * 
 * @author stefan schroeder
 *
 */
public class SolutionPlotter {
	
	private static Logger log = Logger.getLogger(SolutionPlotter.class);
	
	/**
	 * Plots the solution to pngFile, with title.
	 * 
	 * <p>This can only plot if vehicles and jobs have locationIds and coordinates (@see Coordinate). Otherwise a warning message is logged
	 * and method returns but does not plot.
	 * 
	 * @param vrp
	 * @param solution
	 * @param pngFile target path with filename.
	 * @see VehicleRoutingProblem, VehicleRoutingProblemSolution 
	 */
	public static void plotSolutionAsPNG(VehicleRoutingProblem vrp, VehicleRoutingProblemSolution solution, String pngFile, String plotTitle){
		final Map<String,Coordinate> locs = new HashMap<String, Coordinate>();
		boolean locationsRetrieved = retrieveLocations(locs,vrp);
		if(!locationsRetrieved) return;
		Locations locations = new Locations(){

			@Override
			public Coordinate getCoord(String id) {
				return locs.get(id);
			}
			
		};
		plotRoutesAsPNG(solution.getRoutes(), locations, pngFile, plotTitle);
	}
	
	
	/**
	 * Plots the a collection of routes to pngFile.
	 * 
	 * 
	 * @param routes
	 * @param locations indicating the locations for the tour-activities.
	 * @param pngFile target path with filename.
	 * @param plotTitle 
	 * @see VehicleRoute
	 */
	public static void plotRoutesAsPNG(Collection<VehicleRoute> routes, Locations locations, String pngFile, String plotTitle) {
		log.info("plot routes to " + pngFile);
		XYSeriesCollection coll = new XYSeriesCollection();
		int counter = 1;
		double maxX = 0;
		double minX = Double.MAX_VALUE;
		double maxY = 0;
		double minY = Double.MAX_VALUE;
		for(VehicleRoute route : routes){
			if(route.isEmpty()) continue;
			XYSeries series = new XYSeries(counter, false, true);
			
			Coordinate startCoord = locations.getCoord(route.getStart().getLocationId());
			series.add(startCoord.getX(), startCoord.getY());
			if(startCoord.getX() > maxX) maxX = startCoord.getX();
			if(startCoord.getY() > maxY) maxY = startCoord.getY();
			if(startCoord.getX() < minX) minX = startCoord.getX();
			if(startCoord.getY() < minY) minY = startCoord.getY();
			
			for(TourActivity act : route.getTourActivities().getActivities()){
				Coordinate coord = locations.getCoord(act.getLocationId());
				series.add(coord.getX(), coord.getY());
				if(coord.getX() > maxX) maxX = coord.getX();
				if(coord.getY() > maxY) maxY = coord.getY();
				if(coord.getX() < minX) minX = coord.getX();
				if(coord.getY() < minY) minY = coord.getY();
			}
			
			Coordinate endCoord = locations.getCoord(route.getEnd().getLocationId());
			series.add(endCoord.getX(), endCoord.getY());
			if(endCoord.getX() > maxX) maxX = endCoord.getX();
			if(endCoord.getY() > maxY) maxY = endCoord.getY();
			if(endCoord.getX() < minX) minX = endCoord.getX();
			if(endCoord.getY() < minY) minY = endCoord.getY();
			
			coll.addSeries(series);
			counter++;
		}
		JFreeChart chart = ChartFactory.createXYLineChart(plotTitle,"x-coordinate", "y-coordinate",coll, PlotOrientation.VERTICAL,true,true,false);
		
		XYPlot plot = chart.getXYPlot();
		
		XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer();
		renderer.setBaseShapesVisible(true);
		
		NumberAxis xAxis = (NumberAxis) plot.getDomainAxis();
		xAxis.setTickUnit(new NumberTickUnit(10));
//		Range rangeX = new Range(minX - 0.05*minX, maxX+0.05*maxX);
		Range rangeX = new Range(minX, maxX);
		xAxis.setRange(rangeX);
		
		NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();
//		yAxis.setTickUnit(new NumberTickUnit(10));
//		Range rangeY = new Range(minY-0.05*minY,maxY + 0.05*maxY);
		Range rangeY = new Range(minY,maxY);
		yAxis.setRange(rangeY);
		try {
			ChartUtilities.saveChartAsPNG(new File(pngFile), chart, 1000, 600);
		} catch (IOException e) {
			log.error("cannot plot");
			log.error(e);
			e.printStackTrace();
			
		}
	}

	private static boolean retrieveLocations(Map<String, Coordinate> locs, VehicleRoutingProblem vrp) {
		for(Vehicle v : vrp.getVehicles()){
			String locationId = v.getLocationId();
			if(locationId == null){ 
				log.warn("cannot plot solution, since vehicle " + v + " has no locationId.");
				return false;
			}
			Coordinate coord = v.getCoord();
			if(coord == null){ 
				log.warn("cannot plot solution, since vehicle " + v + " has no location-coordinate.");
				return false;
			}
			locs.put(locationId, coord);
		}
		for(Job j : vrp.getJobs().values()){
			if(j instanceof Service){
				String locationId = ((Service) j).getLocationId();
				if(locationId == null){ 
					log.warn("cannot plot solution, since job " + j + " has no locationId.");
					return false;
				}
				Coordinate coord = ((Service) j).getCoord();
				if(coord == null){ 
					log.warn("cannot plot solution, since job " + j + " has no location-coordinate.");
					return false;
				}
				locs.put(locationId, coord);
			}
			else{
				throw new IllegalStateException("job is not a service. this is not supported yet.");
			}
		}
		return true;
	}


}
