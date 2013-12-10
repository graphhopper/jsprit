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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.geom.Ellipse2D;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.job.Delivery;
import jsprit.core.problem.job.Job;
import jsprit.core.problem.job.Pickup;
import jsprit.core.problem.job.Service;
import jsprit.core.problem.job.Shipment;
import jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.solution.route.activity.TourActivity;
import jsprit.core.problem.vehicle.Vehicle;
import jsprit.core.util.Coordinate;
import jsprit.core.util.Locations;

import org.apache.log4j.Logger;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.LegendItemSource;
import org.jfree.chart.annotations.XYShapeAnnotation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.XYItemLabelGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.Range;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleEdge;


public class Plotter {
	
	private static class BoundingBox {
		double minX;
		double minY;
		double maxX;
		double maxY;
		public BoundingBox(double minX, double minY, double maxX, double maxY) {
			super();
			this.minX = minX;
			this.minY = minY;
			this.maxX = maxX;
			this.maxY = maxY;
		}
		
	}
	
//	private static class NoLocationFoundException extends Exception{
//
//		/**
//		 * 
//		 */
//		private static final long serialVersionUID = 1L;
//		
//	}
	
	private static Logger log = Logger.getLogger(SolutionPlotter.class);
	
	
	public static enum Label {
		ID, SIZE, NO_LABEL
	}
	
	private boolean showFirstActivity = true;
	
	private Label label = Label.SIZE;
	
	private VehicleRoutingProblem vrp;
	
	private boolean plotSolutionAsWell = false;

	private boolean plotShipments = true;

	private Collection<VehicleRoute> routes;
	
	private BoundingBox boundingBox = null;
	
	public void setShowFirstActivity(boolean show){
		showFirstActivity = show;
	}
	
	public void setLabel(Label label){
		this.label = label;
	}
	
	public void setBoundingBox(double minX, double minY, double maxX, double maxY){
		boundingBox = new BoundingBox(minX,minY,maxX,maxY);
	}

	public Plotter(VehicleRoutingProblem vrp) {
		super();
		this.vrp = vrp;
	}

	public Plotter(VehicleRoutingProblem vrp, VehicleRoutingProblemSolution solution) {
		super();
		this.vrp = vrp;
		this.routes = solution.getRoutes();
		plotSolutionAsWell = true;
	}
	
	public Plotter(VehicleRoutingProblem vrp, Collection<VehicleRoute> routes) {
		super();
		this.vrp = vrp;
		this.routes = routes;
		plotSolutionAsWell = true;
	}

	public void plot(String pngFileName, String plotTitle){
		String filename = pngFileName;
		if(!pngFileName.endsWith(".png")) filename += ".png";
		if(plotSolutionAsWell){
			plotSolutionAsPNG(vrp, routes, filename, plotTitle);
		}
		else{
			plotVrpAsPNG(vrp, filename, plotTitle);
		}
	}
	
	private void plotVrpAsPNG(VehicleRoutingProblem vrp, String pngFile, String title){
		log.info("plot routes to " + pngFile);
		XYSeriesCollection problem;
		final XYSeriesCollection shipments;
		Map<XYDataItem,String> labels = new HashMap<XYDataItem, String>();
		try {
			problem = makeVrpSeries(vrp, labels);
			shipments = makeShipmentSeries(vrp.getJobs().values(), null);
		} catch (NoLocationFoundException e) {
			log.warn("cannot plot vrp, since coord is missing");
			return;	
		}
		final XYPlot plot = createProblemPlot(problem, shipments, labels);
		LegendItemSource lis = new LegendItemSource() {

			@Override
			public LegendItemCollection getLegendItems() {
				LegendItemCollection lic = new LegendItemCollection();
				lic.addAll(plot.getRenderer(0).getLegendItems());
				if(!shipments.getSeries().isEmpty()){
					lic.add(plot.getRenderer(1).getLegendItem(1, 0));
				}
				return lic;
			}
		};
		
		JFreeChart chart = new JFreeChart(title, plot);
		chart.removeLegend();
		LegendTitle legend = new LegendTitle(lis);
		legend.setPosition(RectangleEdge.BOTTOM);
		chart.addLegend(legend);
		save(chart,pngFile);
	}
	
	private void plotSolutionAsPNG(VehicleRoutingProblem vrp, Collection<VehicleRoute> routes, String pngFile, String title){
		log.info("plot solution to " + pngFile);
		XYSeriesCollection problem;
		XYSeriesCollection solutionColl;
		final XYSeriesCollection shipments;
		Map<XYDataItem,String> labels = new HashMap<XYDataItem, String>();
		try {
			problem = makeVrpSeries(vrp, labels);
			shipments = makeShipmentSeries(vrp.getJobs().values(), null);
			solutionColl = makeSolutionSeries(vrp, routes);
		} catch (NoLocationFoundException e) {
			log.warn("cannot plot vrp, since coord is missing");
			return;	
		}
		final XYPlot plot = createProblemSolutionPlot(problem, shipments, solutionColl, labels);
		JFreeChart chart = new JFreeChart(title, plot);
		LegendItemSource lis = new LegendItemSource() {

			@Override
			public LegendItemCollection getLegendItems() {
				LegendItemCollection lic = new LegendItemCollection();
				lic.addAll(plot.getRenderer(0).getLegendItems());
				lic.addAll(plot.getRenderer(2).getLegendItems());
				if(!shipments.getSeries().isEmpty()){
					lic.add(plot.getRenderer(1).getLegendItem(1, 0));
				}
				return lic;
			}
		};

		chart.removeLegend();
		LegendTitle legend = new LegendTitle(lis);
		legend.setPosition(RectangleEdge.BOTTOM);
		chart.addLegend(legend);
		
		save(chart,pngFile);
		
	}
	
	private XYPlot createProblemPlot(final XYSeriesCollection problem, XYSeriesCollection shipments, final Map<XYDataItem, String> labels) {
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
		
		xAxis.setRangeWithMargins(getDomainRange(problem));
		
		NumberAxis yAxis = new NumberAxis();
		yAxis.setRangeWithMargins(getRange(problem));
		
		plot.setDataset(0, problem);
		plot.setRenderer(0, problemRenderer);
		plot.setDomainAxis(0, xAxis);
		plot.setRangeAxis(0, yAxis);
		
		XYItemRenderer shipmentsRenderer = new XYLineAndShapeRenderer(true, false);   // Shapes only
		for(int i=0;i<shipments.getSeriesCount();i++){
			shipmentsRenderer.setSeriesPaint(i, Color.BLUE);
			shipmentsRenderer.setSeriesStroke(i, new BasicStroke(
			        1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
			        1.0f, new float[] {6.0f, 6.0f}, 0.0f
			    ));
		}
//		shipmentsRenderer.getLegendItems().
		plot.setDataset(1, shipments);
		plot.setRenderer(1, shipmentsRenderer);
//		plot.setDomainAxis(1, xAxis);
//		plot.setRangeAxis(1, yAxis);
		
//		plot.addl
		return plot;
	}

	private Range getRange(final XYSeriesCollection seriesCol) {
		if(this.boundingBox==null) return seriesCol.getRangeBounds(false);
		else return new Range(boundingBox.minY, boundingBox.maxY);
	}

	private Range getDomainRange(final XYSeriesCollection seriesCol) {
		if(this.boundingBox == null) return seriesCol.getDomainBounds(true);
		else return new Range(boundingBox.minX, boundingBox.maxX);
	}

	private XYPlot createProblemSolutionPlot(final XYSeriesCollection problem, XYSeriesCollection shipments, XYSeriesCollection solutionColl, final Map<XYDataItem, String> labels) {
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
		xAxis.setRangeWithMargins(getDomainRange(solutionColl));
		
		NumberAxis yAxis = new NumberAxis();
		yAxis.setRangeWithMargins(getRange(solutionColl));
		
		plot.setDataset(0, problem);
		plot.setRenderer(0, problemRenderer);
		plot.setDomainAxis(0, xAxis);
		plot.setRangeAxis(0, yAxis);
//		plot.mapDatasetToDomainAxis(0, 0);

		
		XYItemRenderer shipmentsRenderer = new XYLineAndShapeRenderer(true, false);   // Shapes only
		for(int i=0;i<shipments.getSeriesCount();i++){
			shipmentsRenderer.setSeriesPaint(i, Color.BLUE);
			shipmentsRenderer.setSeriesStroke(i, new BasicStroke(
			        1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
			        1.0f, new float[] {6.0f, 6.0f}, 0.0f
			    ));
		}
		plot.setDataset(1, shipments);
		plot.setRenderer(1, shipmentsRenderer);
//		plot.setDomainAxis(1, xAxis);
//		plot.setRangeAxis(1, yAxis);
		
		XYItemRenderer solutionRenderer = new XYLineAndShapeRenderer(true, false);   // Lines only
		if(showFirstActivity){
			for(int i=0;i<solutionColl.getSeriesCount();i++){
				XYSeries s = solutionColl.getSeries(i);
				XYDataItem firstCustomer = s.getDataItem(1);
				solutionRenderer.addAnnotation(new XYShapeAnnotation( new Ellipse2D.Double(firstCustomer.getXValue()-0.7, firstCustomer.getYValue()-0.7, 1.5, 1.5), new BasicStroke(1.0f), Color.RED));
			}
		}
		plot.setDataset(2, solutionColl);
		plot.setRenderer(2, solutionRenderer);
//		plot.setDomainAxis(2, xAxis);
//		plot.setRangeAxis(2, yAxis);
		
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

	private XYSeriesCollection makeSolutionSeries(VehicleRoutingProblem vrp, Collection<VehicleRoute> routes) throws NoLocationFoundException{
		Locations locations = retrieveLocations(vrp);
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
	
	private XYSeriesCollection makeShipmentSeries(Collection<Job> jobs, Map<XYDataItem, String> labels) throws NoLocationFoundException{
		XYSeriesCollection coll = new XYSeriesCollection();
		if(!plotShipments) return coll;
		int sCounter = 1;
		String ship = "shipment";
		boolean first = true;
		for(Job job : jobs){
			if(!(job instanceof Shipment)){
				continue;
			}
			Shipment shipment = (Shipment)job;
			XYSeries shipmentSeries;
			if(first){
				first = false;
				shipmentSeries = new XYSeries(ship, false, true);
			}
			else{
				shipmentSeries = new XYSeries(sCounter, false, true);
				sCounter++;
			}
			shipmentSeries.add(shipment.getPickupCoord().getX(), shipment.getPickupCoord().getY());
			shipmentSeries.add(shipment.getDeliveryCoord().getX(), shipment.getDeliveryCoord().getY());
			coll.addSeries(shipmentSeries);
		}
		return coll;
	}
	
	private XYSeriesCollection makeVrpSeries(Collection<Vehicle> vehicles, Collection<Job> jobs, Map<XYDataItem, String> labels) throws NoLocationFoundException{
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
		for(Job job : jobs){
			if(job instanceof Shipment){
				Shipment s = (Shipment)job;
				XYDataItem dataItem = new XYDataItem(s.getPickupCoord().getX(), s.getPickupCoord().getY());
				pickupSeries.add(dataItem);
				addLabel(labels, s, dataItem);
				
				XYDataItem dataItem2 = new XYDataItem(s.getDeliveryCoord().getX(), s.getDeliveryCoord().getY());
				deliverySeries.add(dataItem2);
				addLabel(labels, s, dataItem2);
			}
			else if(job instanceof Pickup){
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

	private void addLabel(Map<XYDataItem, String> labels, Job job, XYDataItem dataItem) {
		if(this.label.equals(Label.SIZE)){
			labels.put(dataItem, String.valueOf(job.getCapacityDemand()));
		}
		else if(this.label.equals(Label.ID)){
			labels.put(dataItem, String.valueOf(job.getId()));
		}
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
			else if(j instanceof Shipment){
				{
					String locationId = ((Shipment) j).getPickupLocation();
					if(locationId == null) throw new NoLocationFoundException();
					Coordinate coord = ((Shipment) j).getPickupCoord();
					if(coord == null) throw new NoLocationFoundException();
					locs.put(locationId, coord);
				}
				{
					String locationId = ((Shipment) j).getDeliveryLocation();
					if(locationId == null) throw new NoLocationFoundException();
					Coordinate coord = ((Shipment) j).getDeliveryCoord();
					if(coord == null) throw new NoLocationFoundException();
					locs.put(locationId, coord);	
				}
			}
			else{
				throw new IllegalStateException("job is neither a service nor a shipment. this is not supported yet.");
			}
		}
		return new Locations() {
			
			@Override
			public Coordinate getCoord(String id) {
				return locs.get(id);
			}
		};
	}

	public void plotShipments(boolean plotShipments) {
		this.plotShipments  = plotShipments;
	}

}
