/*
 * Licensed to GraphHopper GmbH under one or more contributor
 * license agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * GraphHopper GmbH licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.graphhopper.jsprit.analysis.toolbox;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.SizeDimension;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.End;
import com.graphhopper.jsprit.core.problem.solution.route.activity.JobActivity;
import com.graphhopper.jsprit.core.problem.solution.route.activity.Start;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.util.Coordinate;
import org.jfree.chart.*;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.Range;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.util.ShapeUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.io.File;
import java.io.IOException;
import java.util.*;


/**
 * Visualizes problem and solution.
 * <p>Note that every item to be rendered need to have coordinates.
 *
 * @author schroeder
 */
public class Plotter {

    private final static Color START_COLOR = Color.RED;
    private final static Color END_COLOR = Color.RED;
    private final static Color PICKUP_COLOR = Color.GREEN;
    private final static Color DELIVERY_COLOR = Color.BLUE;
    private final static Color SERVICE_COLOR = Color.BLUE;

    private final static Shape ELLIPSE = new Ellipse2D.Double(-3, -3, 6, 6);


    private static class MyActivityRenderer extends XYLineAndShapeRenderer {

        /**
         *
         */
        private static final long serialVersionUID = 1L;

        private XYSeriesCollection seriesCollection;

        private Map<XYDataItem, Activity> activities;

        private Set<XYDataItem> firstActivities;

        public MyActivityRenderer(XYSeriesCollection seriesCollection, Map<XYDataItem, Activity> activities, Set<XYDataItem> firstActivities) {
            super(false, true);
            this.seriesCollection = seriesCollection;
            this.activities = activities;
            this.firstActivities = firstActivities;
            super.setSeriesOutlinePaint(0, Color.DARK_GRAY);
            super.setUseOutlinePaint(true);
        }

        @Override
        public Shape getItemShape(int seriesIndex, int itemIndex) {
            XYDataItem dataItem = seriesCollection.getSeries(seriesIndex).getDataItem(itemIndex);
            if (firstActivities.contains(dataItem)) {
                return ShapeUtilities.createUpTriangle(4.0f);
            }
            return ELLIPSE;
        }

        @Override
        public Paint getItemOutlinePaint(int seriesIndex, int itemIndex) {
            XYDataItem dataItem = seriesCollection.getSeries(seriesIndex).getDataItem(itemIndex);
            if (firstActivities.contains(dataItem)) {
                return Color.BLACK;
            }
            return super.getItemOutlinePaint(seriesIndex, itemIndex);
        }

        @Override
        public Paint getItemPaint(int seriesIndex, int itemIndex) {
            XYDataItem dataItem = seriesCollection.getSeries(seriesIndex).getDataItem(itemIndex);
            Activity activity = activities.get(dataItem);
            if (activity.equals(Activity.PICKUP)) return PICKUP_COLOR;
            if (activity.equals(Activity.DELIVERY)) return DELIVERY_COLOR;
            if (activity.equals(Activity.SERVICE)) return SERVICE_COLOR;
            if (activity.equals(Activity.START)) return START_COLOR;
            if (activity.equals(Activity.END)) return END_COLOR;
            throw new IllegalStateException("activity at " + dataItem.toString() + " cannot be assigned to a color");
        }

    }

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

    private enum Activity {
        START, END, PICKUP, DELIVERY, SERVICE, EXCHANGE;

    }


    private static Logger log = LoggerFactory.getLogger(Plotter.class);

    /**
     * Label to label ID (=jobId), SIZE (=jobSize=jobCapacityDimensions)
     *
     * @author schroeder
     */
    public enum Label {
        ID, SIZE, @SuppressWarnings("UnusedDeclaration")NO_LABEL
    }

    private Label label = Label.SIZE;

    private VehicleRoutingProblem vrp;

    private Collection<VehicleRoute> routes;

    private BoundingBox boundingBox = null;

    private Map<XYDataItem, Activity> activitiesByDataItem = new HashMap<>();

    private Map<XYDataItem, String> labelsByDataItem = new HashMap<>();

    private Set<XYDataItem> firstActivities = new HashSet<>();

    private double scalingFactor = 1.;

    private boolean invert = false;

    private boolean plotShipments = false;

    /**
     * Constructs Plotter with problem. Thus only the problem can be rendered.
     *
     * @param vrp the routing problem
     */
    public Plotter(VehicleRoutingProblem vrp) {
        this.vrp = vrp;
    }

    /**
     * Constructs Plotter with problem and solution to render them both.
     *
     * @param vrp      the routing problem
     * @param solution the solution
     */
    public Plotter(VehicleRoutingProblem vrp, VehicleRoutingProblemSolution solution) {
        this(vrp, solution.getRoutes());
    }

    /**
     * Constructs Plotter with problem and routes to render individual routes.
     *
     * @param vrp    the routing problem
     * @param routes routes
     */
    public Plotter(VehicleRoutingProblem vrp, Collection<VehicleRoute> routes) {
        super();
        this.vrp = vrp;
        this.routes = routes;
    }

    @SuppressWarnings("UnusedDeclaration")
    public Plotter setScalingFactor(double scalingFactor) {
        this.scalingFactor = scalingFactor;
        return this;
    }

    /**
     * Sets a label.
     *
     * @param label of jobs
     * @return plotter
     */
    public Plotter setLabel(Label label) {
        this.label = label;
        return this;
    }

    public Plotter invertCoordinates(boolean invert) {
        this.invert = invert;
        return this;
    }

    /**
     * Sets a bounding box to zoom in to certain areas.
     *
     * @param minX lower left x
     * @param minY lower left y
     * @param maxX upper right x
     * @param maxY upper right y
     * @return
     */
    @SuppressWarnings("UnusedDeclaration")
    public Plotter setBoundingBox(double minX, double minY, double maxX, double maxY) {
        boundingBox = new BoundingBox(minX, minY, maxX, maxY);
        return this;
    }

    /**
     * Flag that indicates whether shipments should be rendered as well.
     *
     * @param plotShipments flag to plot shipment
     * @return the plotter
     */
    @Deprecated
    public Plotter plotShipments(boolean plotShipments) {
        this.plotShipments = plotShipments;
        return this;
    }

    public Plotter plotJobRelations(boolean plotJobRelations) {
        this.plotShipments = plotJobRelations;
        return this;
    }

    /**
     * Plots problem and/or solution/routes.
     *
     * @param pngFileName - path and filename
     * @param plotTitle   - title that appears on top of image
     */
    public void plot(String pngFileName, String plotTitle) {
        String filename = pngFileName;
        if (!pngFileName.endsWith(".png")) filename += ".png";
        plot(vrp, routes, filename, plotTitle);
    }

    private void plot(VehicleRoutingProblem vrp, final Collection<VehicleRoute> routes, String pngFile, String title) {
        log.info("plot to {}", pngFile);
        XYSeriesCollection problem;
        XYSeriesCollection solution = null;
        XYSeries activities;
        try {
            activities = retrieveActivities(vrp);
            problem = new XYSeriesCollection(activities);
            if (routes != null && !routes.isEmpty()) {
                solution = makeSolutionSeries(vrp, routes);
            }
        } catch (NoLocationFoundException e) {
            log.warn("cannot plot vrp, since coordinate is missing");
            return;
        }
        final XYPlot plot = createPlot(problem, solution);
        JFreeChart chart = new JFreeChart(title, plot);

        LegendTitle legend = createLegend(routes, plot);
        chart.removeLegend();
        chart.addLegend(legend);

        save(chart, pngFile);

    }

    private LegendTitle createLegend(final Collection<VehicleRoute> routes, final XYPlot plot) {
        LegendItemCollection lic = new LegendItemCollection();
        LegendItem vehLoc = new LegendItem("vehLoc", Color.RED);
        vehLoc.setShape(ELLIPSE);
        vehLoc.setShapeVisible(true);
        lic.add(vehLoc);

        LegendItem serviceActItem = new LegendItem("serviceAct", Color.BLUE);
        serviceActItem.setShape(ELLIPSE);
        serviceActItem.setShapeVisible(true);
        lic.add(serviceActItem);

        LegendItem pickupActItem = new LegendItem("pickupAct", Color.GREEN);
        pickupActItem.setShape(ELLIPSE);
        pickupActItem.setShapeVisible(true);
        lic.add(pickupActItem);

        LegendItem deliveryActItem = new LegendItem("deliveryAct", Color.BLUE);
        deliveryActItem.setShape(ELLIPSE);
        deliveryActItem.setShapeVisible(true);
        lic.add(deliveryActItem);

        LegendItem exchangeActItem = new LegendItem("exchangeAct", Color.ORANGE);
        exchangeActItem.setShape(ELLIPSE);
        exchangeActItem.setShapeVisible(true);
        lic.add(exchangeActItem);

        if (routes != null && !routes.isEmpty()) {
            LegendItem item = new LegendItem("firstActivity", Color.BLACK);
            Shape upTriangle = ShapeUtilities.createUpTriangle(3.0f);
            item.setShape(upTriangle);
            item.setOutlinePaint(Color.BLACK);

            item.setLine(upTriangle);
            item.setLinePaint(Color.BLACK);
            item.setShapeVisible(true);

            lic.add(item);
            lic.addAll(plot.getRenderer(2).getLegendItems());
        }

        LegendItemSource source = () -> lic;
        return new LegendTitle(source);

    }


    private XYItemRenderer getJobRenderer(XYSeriesCollection shipments) {
        XYItemRenderer shipmentsRenderer = new XYLineAndShapeRenderer(true, false);   // Shapes only
        for (int i = 0; i < shipments.getSeriesCount(); i++) {
            shipmentsRenderer.setSeriesPaint(i, Color.DARK_GRAY);
            shipmentsRenderer.setSeriesStroke(i, new BasicStroke(
                1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                1.f, new float[]{4.0f, 4.0f}, 0.0f
            ));
        }
        return shipmentsRenderer;
    }

    private MyActivityRenderer getProblemRenderer(final XYSeriesCollection problem) {
        MyActivityRenderer problemRenderer = new MyActivityRenderer(problem, activitiesByDataItem, firstActivities);
        problemRenderer.setBaseItemLabelGenerator((arg0, arg1, arg2) -> {
            XYDataItem item = problem.getSeries(arg1).getDataItem(arg2);
            return labelsByDataItem.get(item);
        });
        problemRenderer.setBaseItemLabelsVisible(true);
        problemRenderer.setBaseItemLabelPaint(Color.BLACK);

        return problemRenderer;
    }

    private Range getRange(final XYSeriesCollection seriesCol) {
        if (this.boundingBox == null) {
            Range rangeBounds = seriesCol.getRangeBounds(true);
            if (rangeBounds.getLength() == 0d) {
                rangeBounds = new Range(rangeBounds.getLowerBound(), rangeBounds.getLowerBound() + 10);
            }
            return rangeBounds;
        }
        else return new Range(boundingBox.minY, boundingBox.maxY);
    }

    private Range getDomainRange(final XYSeriesCollection seriesCol) {
        if (this.boundingBox == null) return seriesCol.getDomainBounds(true);
        else return new Range(boundingBox.minX, boundingBox.maxX);
    }

    private XYPlot createPlot(final XYSeriesCollection problem, XYSeriesCollection solution) {
        XYPlot plot = new XYPlot();
        plot.setBackgroundPaint(Color.LIGHT_GRAY);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);

        XYLineAndShapeRenderer problemRenderer = getProblemRenderer(problem);
        plot.setDataset(0, problem);
        plot.setRenderer(0, problemRenderer);

        if (plotShipments) {
            XYSeriesCollection jobSeriesCollections = makeJobSeries(vrp.getJobs().values());
            XYItemRenderer jobRenderer = getJobRenderer(jobSeriesCollections);
            plot.setDataset(1, jobSeriesCollections);
            plot.setRenderer(1, jobRenderer);
        }

        if (solution != null) {
            XYItemRenderer solutionRenderer = getRouteRenderer(solution);
            plot.setDataset(2, solution);
            plot.setRenderer(2, solutionRenderer);
        }

        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();

        if (boundingBox == null) {
            xAxis.setRangeWithMargins(getDomainRange(problem));
            yAxis.setRangeWithMargins(getRange(problem));
        } else {
            xAxis.setRangeWithMargins(new Range(boundingBox.minX, boundingBox.maxX));
            yAxis.setRangeWithMargins(new Range(boundingBox.minY, boundingBox.maxY));
        }

        plot.setDomainAxis(xAxis);
        plot.setRangeAxis(yAxis);

        return plot;
    }

    private XYSeriesCollection makeJobSeries(Collection<Job> jobs) {
        XYSeriesCollection coll = new XYSeriesCollection();
        if (!plotShipments) return coll;
        int sCounter = 1;
        String ship = "job";
        boolean first = true;
        for (Job job : jobs) {
            if (job.getActivityList().size() == 1) {
                continue;
            }
            XYSeries jobSeries;
            if (first) {
                first = false;
                jobSeries = new XYSeries(ship, false, true);
            } else {
                jobSeries = new XYSeries(sCounter, false, true);
                sCounter++;
            }
            for (JobActivity act : job.getActivityList().getAll()) {
                jobSeries.add(act.getLocation().getCoordinate().getX() * scalingFactor, act.getLocation().getCoordinate().getY() * scalingFactor);
            }
            coll.addSeries(jobSeries);
        }
        return coll;
    }

    private XYItemRenderer getRouteRenderer(XYSeriesCollection solutionColl) {
        XYItemRenderer solutionRenderer = new XYLineAndShapeRenderer(true, false);   // Lines only
        for (int i = 0; i < solutionColl.getSeriesCount(); i++) {
            XYSeries s = solutionColl.getSeries(i);
            XYDataItem firstCustomer = s.getDataItem(1);
            firstActivities.add(firstCustomer);
        }
        return solutionRenderer;
    }

    private void save(JFreeChart chart, String pngFile) {
        try {
            ChartUtilities.saveChartAsPNG(new File(pngFile), chart, 1000, 600);
        } catch (IOException e) {
            log.error("cannot plot");
            log.error(e.toString());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private XYSeriesCollection makeSolutionSeries(VehicleRoutingProblem vrp, Collection<VehicleRoute> routes) throws NoLocationFoundException {
        Map<String, Coordinate> coordinates = makeMap(vrp.getAllLocations());
        XYSeriesCollection coll = new XYSeriesCollection();
        int counter = 1;
        for (VehicleRoute route : routes) {
            if (route.isEmpty()) continue;
            XYSeries series = new XYSeries(counter, false, true);
            Coordinate startCoordinate = getCoordinate(coordinates.get(route.getStart().getLocation().getId()));
            series.add(startCoordinate.getX() * scalingFactor, startCoordinate.getY() * scalingFactor);
            for (TourActivity act : route.getTourActivities().getActivities()) {
                Coordinate coordinate = getCoordinate(coordinates.get(act.getLocation().getId()));
                series.add(coordinate.getX() * scalingFactor, coordinate.getY() * scalingFactor);
            }
            Coordinate endCoordinate = getCoordinate(coordinates.get(route.getEnd().getLocation().getId()));
            series.add(endCoordinate.getX() * scalingFactor, endCoordinate.getY() * scalingFactor);
            coll.addSeries(series);
            counter++;
        }
        return coll;
    }

    private Map<String, Coordinate> makeMap(Collection<Location> allLocations) {
        Map<String, Coordinate> coords = new HashMap<>();
        for (Location l : allLocations) coords.put(l.getId(), l.getCoordinate());
        return coords;
    }


    private void addJob(XYSeries activities, Job job) {
        for (JobActivity act : job.getActivityList().getAll()) {
            Coordinate coordinate = getCoordinate(act.getLocation().getCoordinate());
            XYDataItem dataItem = new XYDataItem(coordinate.getX() * scalingFactor, coordinate.getY() * scalingFactor);
            activities.add(dataItem);
            addLabel(act, dataItem);
            markItem(dataItem, act);
        }

    }

    private void addLabel(JobActivity jobAct, XYDataItem dataItem) {
        if (this.label.equals(Label.SIZE)) {
            labelsByDataItem.put(dataItem, getSizeString(jobAct));
        } else if (this.label.equals(Label.ID)) {
            labelsByDataItem.put(dataItem, String.valueOf(jobAct.getJob().getId()));
        }
    }

    private String getSizeString(JobActivity act) {
        StringBuilder builder = new StringBuilder();
        builder.append("(");
        boolean firstDim = true;
        for (int i = 0; i < act.getLoadChange().getNuOfDimensions(); i++) {
            if (firstDim) {
                builder.append(String.valueOf(act.getLoadChange().get(i)));
                firstDim = false;
            } else {
                builder.append(",");
                builder.append(String.valueOf(act.getLoadChange().get(i)));
            }
        }
        builder.append(")");
        return builder.toString();
    }

    private Coordinate getCoordinate(Coordinate coordinate) {
        if (invert) {
            return Coordinate.newInstance(coordinate.getY(), coordinate.getX());
        }
        return coordinate;
    }

    private XYSeries retrieveActivities(VehicleRoutingProblem vrp) throws NoLocationFoundException {
        XYSeries activities = new XYSeries("activities", false, true);
        for (Vehicle v : vrp.getVehicles()) {
            Coordinate startCoordinate = getCoordinate(v.getStartLocation().getCoordinate());
            if (startCoordinate == null) throw new NoLocationFoundException();
            XYDataItem item = new XYDataItem(startCoordinate.getX() * scalingFactor, startCoordinate.getY() * scalingFactor);
            markItem(item, new Start(v.getStartLocation(), v.getEarliestDeparture(), v.getLatestArrival()));
            activities.add(item);

            if (!v.getStartLocation().getId().equals(v.getEndLocation().getId())) {
                Coordinate end_coordinate = getCoordinate(v.getEndLocation().getCoordinate());
                if (end_coordinate == null) throw new NoLocationFoundException();
                XYDataItem end_item = new XYDataItem(end_coordinate.getX() * scalingFactor, end_coordinate.getY() * scalingFactor);
                markItem(end_item, new End(v.getEndLocation(), v.getEarliestDeparture(), v.getLatestArrival()));
                activities.add(end_item);
            }
        }
        for (Job job : vrp.getJobs().values()) {
            addJob(activities, job);
        }
        for (VehicleRoute r : vrp.getInitialVehicleRoutes()) {
            for (Job job : r.getTourActivities().getJobs()) {
                addJob(activities, job);
            }
        }
        return activities;
    }

    private void markItem(XYDataItem item, TourActivity activity) {
        Activity activityEnum;
        if (activity instanceof Start) activityEnum = Activity.START;
        else if (activity instanceof End) activityEnum = Activity.END;
        else if (activity.getLoadChange().sign().equals(SizeDimension.SizeDimensionSign.POSITIVE)) {
            activityEnum = Activity.PICKUP;
        } else if (activity.getLoadChange().sign().equals(SizeDimension.SizeDimensionSign.NEGATIVE)) {
            activityEnum = Activity.DELIVERY;
        } else if (activity.getLoadChange().sign().equals(SizeDimension.SizeDimensionSign.MIXED)) {
            activityEnum = Activity.EXCHANGE;
        } else activityEnum = Activity.SERVICE;
        activitiesByDataItem.put(item, activityEnum);
    }


}
