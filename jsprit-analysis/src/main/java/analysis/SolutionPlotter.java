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

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
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
	
	private static class NoLocationFoundException extends Exception{

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
	}
	
	private static Logger log = Logger.getLogger(SolutionPlotter.class);
	

	/**
	 * Plots the {@link VehicleRoutingProblem} to png-file.

	 * @param vrp
	 * @param pngFile target path with filename.
	 * @see VehicleRoutingProblem, VehicleRoutingProblemSolution 
	 */
	public static void plotVrpAsPNG(VehicleRoutingProblem vrp, String pngFile, String title){
		log.info("plot routes to " + pngFile);
		XYSeriesCollection problem;
		try {
			problem = makeVrpSeries(vrp);
		} catch (NoLocationFoundException e) {
			log.warn("cannot plot vrp, since coord is missing");
			return;	
		}
		XYPlot plot = createPlot(problem);
		JFreeChart chart = new JFreeChart(title, plot);
		save(chart,pngFile);
	}

	/**
	 * Retrieves the problem from routes, and plots it along with the routes to pngFile.
	 * 
	 * @param routes
	 * @param locations indicating the locations for the tour-activities.
	 * @param pngFile target path with filename.
	 * @param plotTitle 
	 * @see VehicleRoute
	 */
	public static void plotRoutesAsPNG(Collection<VehicleRoute> routes, Locations locations, String pngFile, String title) {
		log.info("plot routes to " + pngFile);
		XYSeriesCollection problem;
		try {
			problem = makeVrpSeries(routes);
		} catch (NoLocationFoundException e) {
			log.warn("cannot plot vrp, since coord is missing");
			return;	
		}
		XYSeriesCollection solutionColl = makeSolutionSeries(routes,locations);
		XYPlot plot = createPlot(problem, solutionColl);
		JFreeChart chart = new JFreeChart(title, plot);
		save(chart,pngFile);
	}
	
	/**
	 * Plots problem and solution to pngFile.
	 * 
	 * <p>This can only plot if vehicles and jobs have locationIds and coordinates (@see Coordinate). Otherwise a warning message is logged
	 * and method returns but does not plot.
	 * 
	 * @param vrp
	 * @param solution
	 * @param pngFile target path with filename.
	 * @see VehicleRoutingProblem, VehicleRoutingProblemSolution 
	 */
	public static void plotSolutionAsPNG(VehicleRoutingProblem vrp, VehicleRoutingProblemSolution solution, String pngFile, String title){
		log.info("plot solution to " + pngFile);
		XYSeriesCollection problem;
		XYSeriesCollection solutionColl;
		try {
			problem = makeVrpSeries(vrp);
			solutionColl = makeSolutionSeries(vrp, solution);
		} catch (NoLocationFoundException e) {
			log.warn("cannot plot vrp, since coord is missing");
			return;	
		}
		XYPlot plot = createPlot(problem, solutionColl);
		JFreeChart chart = new JFreeChart(title, plot);
		save(chart,pngFile);
		
	}
	
	

	private static XYPlot createPlot(XYSeriesCollection problem) {
		XYPlot plot = new XYPlot();
		plot.setBackgroundPaint(Color.LIGHT_GRAY);
		plot.setRangeGridlinePaint(Color.WHITE);
		plot.setDomainGridlinePaint(Color.WHITE);
		
		XYItemRenderer problemRenderer = new XYLineAndShapeRenderer(false, true);   // Shapes only
		NumberAxis xAxis = new NumberAxis();		
		Range xRange = problem.getRangeBounds(false);
		Range rangeX = Range.scale(xRange, 1.01);
		xAxis.setRange(rangeX);
		
		NumberAxis yAxis = new NumberAxis();
		Range yRange = problem.getRangeBounds(true);
		Range rangeY = Range.scale(yRange, 1.01);
		yAxis.setRange(rangeY);
		
		plot.setDataset(0, problem);
		plot.setRenderer(0, problemRenderer);
		plot.setDomainAxis(0, xAxis);
		plot.setRangeAxis(0, yAxis);
		return plot;
	}

	private static XYPlot createPlot(XYSeriesCollection problem, XYSeriesCollection solutionColl) {
		XYPlot plot = new XYPlot();
		plot.setBackgroundPaint(Color.LIGHT_GRAY);
		plot.setRangeGridlinePaint(Color.WHITE);
		plot.setDomainGridlinePaint(Color.WHITE);
		
		XYItemRenderer problemRenderer = new XYLineAndShapeRenderer(false, true);   // Shapes only
		NumberAxis xAxis = new NumberAxis();		
		Range xRange = problem.getRangeBounds(false);
		Range rangeX = Range.scale(xRange, 1.01);
		xAxis.setRange(rangeX);
		
		NumberAxis yAxis = new NumberAxis();
		Range yRange = problem.getRangeBounds(true);
		Range rangeY = Range.scale(yRange, 1.01);
		yAxis.setRange(rangeY);
		
		plot.setDataset(0, problem);
		plot.setRenderer(0, problemRenderer);
		plot.setDomainAxis(0, xAxis);
		plot.setRangeAxis(0, yAxis);
		
		XYItemRenderer solutionRenderer = new XYLineAndShapeRenderer(true, false);   // Lines only
		plot.setDataset(1, solutionColl);
		plot.setRenderer(1, solutionRenderer);
		plot.setDomainAxis(1, xAxis);
		plot.setRangeAxis(1, yAxis);
		return plot;
	}

	private static void save(JFreeChart chart, String pngFile) {
		try {
			ChartUtilities.saveChartAsPNG(new File(pngFile), chart, 1000, 600);
		} catch (IOException e) {
			log.error("cannot plot");
			log.error(e);
			e.printStackTrace();	
		}
	}

	private static XYSeriesCollection makeSolutionSeries(VehicleRoutingProblem vrp, VehicleRoutingProblemSolution solution) throws NoLocationFoundException{
		Locations locations = retrieveLocations(vrp);
		XYSeriesCollection coll = new XYSeriesCollection();
		int counter = 1;
		for(VehicleRoute route : solution.getRoutes()){
			if(route.isEmpty()) continue;
			XYSeries series = new XYSeries(counter, false, true);
			
			Coordinate startCoord = locations.getCoord(route.getStart().getLocationId());
			series.add(startCoord.getX(), startCoord.getY());
			
			for(TourActivity act : route.getTourActivities().getActivities()){
				Coordinate coord = locations.getCoord(act.getLocationId());
				series.add(coord.getX(), coord.getY());
			}
			
			Coordinate endCoord = locations.getCoord(route.getEnd().getLocationId());
			series.add(endCoord.getX(), endCoord.getY());
			
			coll.addSeries(series);
			counter++;
		}
		return coll;
	}
	
	private static XYSeriesCollection makeSolutionSeries(Collection<VehicleRoute> routes, Locations locations){
		XYSeriesCollection coll = new XYSeriesCollection();
		int counter = 1;
		for(VehicleRoute route : routes){
			if(route.isEmpty()) continue;
			XYSeries series = new XYSeries(counter, false, true);
			
			Coordinate startCoord = locations.getCoord(route.getStart().getLocationId());
			series.add(startCoord.getX(), startCoord.getY());
			
			for(TourActivity act : route.getTourActivities().getActivities()){
				Coordinate coord = locations.getCoord(act.getLocationId());
				series.add(coord.getX(), coord.getY());
			}
			
			Coordinate endCoord = locations.getCoord(route.getEnd().getLocationId());
			series.add(endCoord.getX(), endCoord.getY());
			
			coll.addSeries(series);
			counter++;
		}
		return coll;
	}
	
	private static XYSeriesCollection makeVrpSeries(Collection<Vehicle> vehicles, Collection<Job> services) throws NoLocationFoundException{
		XYSeriesCollection coll = new XYSeriesCollection();
		XYSeries vehicleSeries = new XYSeries("depot", false, true);
		for(Vehicle v : vehicles){
			Coordinate coord = v.getCoord();
			if(coord == null) throw new NoLocationFoundException();
			vehicleSeries.add(coord.getX(),coord.getY());	
		}
		coll.addSeries(vehicleSeries);
		
		XYSeries jobSeries = new XYSeries("service", false, true);
		for(Job job : services){
			Service service = (Service)job;
			Coordinate coord = service.getCoord();
			jobSeries.add(coord.getX(), coord.getY());
		}
		coll.addSeries(jobSeries);
		return coll;
	}
	
	private static XYSeriesCollection makeVrpSeries(Collection<VehicleRoute> routes) throws NoLocationFoundException{
		Set<Vehicle> vehicles = new HashSet<Vehicle>();
		Set<Job> jobs = new HashSet<Job>();
		for(VehicleRoute route : routes){
			vehicles.add(route.getVehicle());
			jobs.addAll(route.getTourActivities().getJobs());
		}
		return makeVrpSeries(vehicles, jobs);
	}
	
	private static XYSeriesCollection makeVrpSeries(VehicleRoutingProblem vrp) throws NoLocationFoundException{
		return makeVrpSeries(vrp.getVehicles(), vrp.getJobs().values());
	}
	
	private static Locations retrieveLocations(VehicleRoutingProblem vrp) throws NoLocationFoundException {
		final Map<String, Coordinate> locs = new HashMap<String, Coordinate>();
		for(Vehicle v : vrp.getVehicles()){
			String locationId = v.getLocationId();
			if(locationId == null) throw new NoLocationFoundException();
			Coordinate coord = v.getCoord();
			if(coord == null) throw new NoLocationFoundException();
			locs.put(locationId, coord);
		}
		for(Job j : vrp.getJobs().values()){
			if(j instanceof Service){
				String locationId = ((Service) j).getLocationId();
				if(locationId == null) throw new NoLocationFoundException();
				Coordinate coord = ((Service) j).getCoord();
				if(coord == null) throw new NoLocationFoundException();
				locs.put(locationId, coord);
			}
			else{
				throw new IllegalStateException("job is not a service. this is not supported yet.");
			}
		}
		return new Locations() {
			
			@Override
			public Coordinate getCoord(String id) {
				return locs.get(id);
			}
		};
	}
	
}
