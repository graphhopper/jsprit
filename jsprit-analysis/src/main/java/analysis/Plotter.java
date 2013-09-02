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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.geom.Ellipse2D;
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
import org.jfree.chart.annotations.XYShapeAnnotation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.XYItemLabelGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import util.Coordinate;
import util.Locations;
import basics.Delivery;
import basics.Job;
import basics.Pickup;
import basics.Service;
import basics.VehicleRoutingProblem;
import basics.VehicleRoutingProblemSolution;
import basics.route.TourActivity;
import basics.route.Vehicle;
import basics.route.VehicleRoute;

public class Plotter {
	
	private static class NoLocationFoundException extends Exception{

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
	}
	
	private static Logger log = Logger.getLogger(SolutionPlotter.class);
	
	
	public static enum Label {
		ID, SIZE, NO_LABEL
	}
	
	private boolean showFirstActivity = true;
	
	private Label label = Label.SIZE;
	
	private VehicleRoutingProblem vrp;
	
	private VehicleRoutingProblemSolution solution;
	
	private boolean plotSolutionAsWell = false;
	
	public void setShowFirstActivity(boolean show){
		showFirstActivity = show;
	}
	
	public void setLabel(Label label){
		this.label = label;
	}

	public Plotter(VehicleRoutingProblem vrp) {
		super();
		this.vrp = vrp;
	}

	public Plotter(VehicleRoutingProblem vrp, VehicleRoutingProblemSolution solution) {
		super();
		this.vrp = vrp;
		this.solution = solution;
		plotSolutionAsWell = true;
	}
	
	public void plot(String pngFileName, String plotTitle){
		if(plotSolutionAsWell){
			plotSolutionAsPNG(vrp, solution, pngFileName, plotTitle);
		}
		else{
			plotVrpAsPNG(vrp, pngFileName, plotTitle);
		}
	}
	
	private void plotVrpAsPNG(VehicleRoutingProblem vrp, String pngFile, String title){
		log.info("plot routes to " + pngFile);
		XYSeriesCollection problem;
		Map<XYDataItem,String> labels = new HashMap<XYDataItem, String>();
		try {
			problem = makeVrpSeries(vrp, labels);
		} catch (NoLocationFoundException e) {
			log.warn("cannot plot vrp, since coord is missing");
			return;	
		}
		XYPlot plot = createPlot(problem, labels);
		JFreeChart chart = new JFreeChart(title, plot);
		save(chart,pngFile);
	}
	
	private void plotSolutionAsPNG(VehicleRoutingProblem vrp, VehicleRoutingProblemSolution solution, String pngFile, String title){
		log.info("plot solution to " + pngFile);
		XYSeriesCollection problem;
		XYSeriesCollection solutionColl;
		Map<XYDataItem,String> labels = new HashMap<XYDataItem, String>();
		try {
			problem = makeVrpSeries(vrp, labels);
			solutionColl = makeSolutionSeries(vrp, solution);
		} catch (NoLocationFoundException e) {
			log.warn("cannot plot vrp, since coord is missing");
			return;	
		}
		XYPlot plot = createPlot(problem, solutionColl, labels);
		JFreeChart chart = new JFreeChart(title, plot);
		save(chart,pngFile);
		
	}
	
	private static XYPlot createPlot(final XYSeriesCollection problem, final Map<XYDataItem, String> labels) {
		XYPlot plot = new XYPlot();
		plot.setBackgroundPaint(Color.LIGHT_GRAY);
		plot.setRangeGridlinePaint(Color.WHITE);
		plot.setDomainGridlinePaint(Color.WHITE);
		
		XYItemRenderer problemRenderer = new XYLineAndShapeRenderer(false, true);   // Shapes only
		problemRenderer.setBaseItemLabelGenerator(new XYItemLabelGenerator() {
			
			@Override
			public String generateLabel(XYDataset arg0, int arg1, int arg2) {
				XYDataItem item = problem.getSeries(arg1).getDataItem(arg2);
				return labels.get(item);
			}
		});
		problemRenderer.setBaseItemLabelsVisible(true);
		problemRenderer.setBaseItemLabelPaint(Color.BLACK);
		
		NumberAxis xAxis = new NumberAxis();		
		xAxis.setRangeWithMargins(problem.getDomainBounds(true));
		
		NumberAxis yAxis = new NumberAxis();
		yAxis.setRangeWithMargins(problem.getRangeBounds(false));
		
		plot.setDataset(0, problem);
		plot.setRenderer(0, problemRenderer);
		plot.setDomainAxis(0, xAxis);
		plot.setRangeAxis(0, yAxis);
		
		return plot;
	}

	private XYPlot createPlot(final XYSeriesCollection problem, XYSeriesCollection solutionColl, final Map<XYDataItem, String> labels) {
		XYPlot plot = new XYPlot();
		plot.setBackgroundPaint(Color.LIGHT_GRAY);
		plot.setRangeGridlinePaint(Color.WHITE);
		plot.setDomainGridlinePaint(Color.WHITE);
		
		XYItemRenderer problemRenderer = new XYLineAndShapeRenderer(false, true);   // Shapes only
		problemRenderer.setBaseItemLabelGenerator(new XYItemLabelGenerator() {
			
			@Override
			public String generateLabel(XYDataset arg0, int arg1, int arg2) {
				XYDataItem item = problem.getSeries(arg1).getDataItem(arg2);
				return labels.get(item);
			}
		});
		problemRenderer.setBaseItemLabelsVisible(true);
		problemRenderer.setBaseItemLabelPaint(Color.BLACK);

		
		NumberAxis xAxis = new NumberAxis();		
		xAxis.setRangeWithMargins(problem.getDomainBounds(true));
		
		NumberAxis yAxis = new NumberAxis();
		yAxis.setRangeWithMargins(problem.getRangeBounds(true));
		
		plot.setDataset(0, problem);
		plot.setRenderer(0, problemRenderer);
		plot.setDomainAxis(0, xAxis);
		plot.setRangeAxis(0, yAxis);
		
		
		XYItemRenderer solutionRenderer = new XYLineAndShapeRenderer(true, false);   // Lines only
		if(showFirstActivity){
			for(int i=0;i<solutionColl.getSeriesCount();i++){
				XYSeries s = solutionColl.getSeries(i);
				XYDataItem firstCustomer = s.getDataItem(1);
				solutionRenderer.addAnnotation(new XYShapeAnnotation( new Ellipse2D.Double(firstCustomer.getXValue()-0.7, firstCustomer.getYValue()-0.7, 1.5, 1.5), new BasicStroke(1.0f), Color.RED));
			}
		}
		plot.setDataset(1, solutionColl);
		plot.setRenderer(1, solutionRenderer);
		plot.setDomainAxis(1, xAxis);
		plot.setRangeAxis(1, yAxis);
		
		return plot;
	}

	private void save(JFreeChart chart, String pngFile) {
		try {
			ChartUtilities.saveChartAsPNG(new File(pngFile), chart, 1000, 600);
		} catch (IOException e) {
			log.error("cannot plot");
			log.error(e);
			e.printStackTrace();	
		}
	}

	private XYSeriesCollection makeSolutionSeries(VehicleRoutingProblem vrp, VehicleRoutingProblemSolution solution) throws NoLocationFoundException{
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
	
	private XYSeriesCollection makeSolutionSeries(Collection<VehicleRoute> routes, Locations locations){
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
	
	private XYSeriesCollection makeVrpSeries(Collection<Vehicle> vehicles, Collection<Job> services, Map<XYDataItem, String> labels) throws NoLocationFoundException{
		XYSeriesCollection coll = new XYSeriesCollection();
		XYSeries vehicleSeries = new XYSeries("depot", false, true);
		for(Vehicle v : vehicles){
			Coordinate coord = v.getCoord();
			if(coord == null) throw new NoLocationFoundException();
			vehicleSeries.add(coord.getX(),coord.getY());	
		}
		coll.addSeries(vehicleSeries);
		
		XYSeries serviceSeries = new XYSeries("service", false, true);
		XYSeries pickupSeries = new XYSeries("pickup", false, true);
		XYSeries deliverySeries = new XYSeries("delivery", false, true);
		for(Job job : services){
			if(job instanceof Pickup){
				Pickup service = (Pickup)job;
				Coordinate coord = service.getCoord();
				XYDataItem dataItem = new XYDataItem(coord.getX(), coord.getY());
				pickupSeries.add(dataItem);
				addLabel(labels, service, dataItem);
			}
			else if(job instanceof Delivery){
				Delivery service = (Delivery)job;
				Coordinate coord = service.getCoord();
				XYDataItem dataItem = new XYDataItem(coord.getX(), coord.getY());
				deliverySeries.add(dataItem);
				addLabel(labels, service, dataItem);
			}
			else if(job instanceof Service){
				Service service = (Service)job;
				Coordinate coord = service.getCoord();
				XYDataItem dataItem = new XYDataItem(coord.getX(), coord.getY());
				serviceSeries.add(dataItem);
				addLabel(labels, service, dataItem);
			}
			else{
				throw new IllegalStateException("job instanceof " + job.getClass().toString() + ". this is not supported.");
			}
			
		}
		if(!serviceSeries.isEmpty()) coll.addSeries(serviceSeries);
		if(!pickupSeries.isEmpty()) coll.addSeries(pickupSeries);
		if(!deliverySeries.isEmpty()) coll.addSeries(deliverySeries);
		return coll;
	}

	private void addLabel(Map<XYDataItem, String> labels, Service service, XYDataItem dataItem) {
		if(this.label.equals(Label.SIZE)){
			labels.put(dataItem, String.valueOf(service.getCapacityDemand()));
		}
		else if(this.label.equals(Label.ID)){
			labels.put(dataItem, String.valueOf(service.getId()));
		}
	}
	
	private XYSeriesCollection makeVrpSeries(Collection<VehicleRoute> routes, Map<XYDataItem, String> labels) throws NoLocationFoundException{
		Set<Vehicle> vehicles = new HashSet<Vehicle>();
		Set<Job> jobs = new HashSet<Job>();
		for(VehicleRoute route : routes){
			vehicles.add(route.getVehicle());
			jobs.addAll(route.getTourActivities().getJobs());
		}
		return makeVrpSeries(vehicles, jobs, labels);
	}
	
	private XYSeriesCollection makeVrpSeries(VehicleRoutingProblem vrp, Map<XYDataItem, String> labels) throws NoLocationFoundException{
		return makeVrpSeries(vrp.getVehicles(), vrp.getJobs().values(), labels);
	}
	
	private Locations retrieveLocations(VehicleRoutingProblem vrp) throws NoLocationFoundException {
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
