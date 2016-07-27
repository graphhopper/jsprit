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


import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.job.Shipment;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.DeliveryActivity;
import com.graphhopper.jsprit.core.problem.solution.route.activity.PickupActivity;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity.JobActivity;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.util.Time;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.ui.swingViewer.ViewPanel;
import org.graphstream.ui.view.View;
import org.graphstream.ui.view.Viewer;

import javax.swing.*;
import java.awt.*;


public class GraphStreamViewer {

    public static class StyleSheets {

        public static String BLUE_FOREST =
            "graph { fill-color: #141F2E; }" +
                "node {" +
                "	size: 7px, 7px;" +
                "   fill-color: #A0FFA0;" +
                "	text-alignment: at-right;" +
                " 	stroke-mode: plain;" +
                "	stroke-color: #999;" +
                "	stroke-width: 1.0;" +
                "	text-font: couriernew;" +
                " 	text-offset: 2,-5;" +
                "	text-size: 8;" +
                "}" +
                "node.pickup {" +
                " 	fill-color: #6CC644;" +
                "}" +
                "node.delivery {" +
                " 	fill-color: #f93;" +
                "}" +
                "node.pickupInRoute {" +
                "	fill-color: #6CC644;" +
                " 	stroke-mode: plain;" +
                "	stroke-color: #333;" +
                "   stroke-width: 2.0;" +
                "}" +
                "node.deliveryInRoute {" +
                " 	fill-color: #f93;" +
                " 	stroke-mode: plain;" +
                "	stroke-color: #333;" +
                "   stroke-width: 2.0;" +
                "}" +
                "node.depot {" +
                " 	fill-color: #BD2C00;" +
                "	size: 10px, 10px;" +
                " 	shape: box;" +
                "}" +
                "node.removed {" +
                " 	fill-color: #FF8080;" +
                "	size: 10px, 10px;" +
                " 	stroke-mode: plain;" +
                "	stroke-color: #CCF;" +
                "   stroke-width: 2.0;" +
                "   shadow-mode: gradient-radial;" +
                "   shadow-width: 10px; shadow-color: #EEF, #000; shadow-offset: 0px;" +
                "}" +

                "edge {" +
                "	fill-color: #D3D3D3;" +
                "	arrow-size: 6px,3px;" +
                "}" +
//                    "edge.inserted {" +
//                    "	fill-color: #A0FFA0;" +
//                    "	arrow-size: 6px,3px;" +
//                    "   shadow-mode: gradient-radial;" +
//                    "   shadow-width: 10px; shadow-color: #EEF, #000; shadow-offset: 0px;" +
//                    "}" +
//                    "edge.removed {" +
//                    "	fill-color: #FF0000;" +
//                    "	arrow-size: 6px,3px;" +
//                    "   shadow-mode: gradient-radial;" +
//                    "   shadow-width: 10px; shadow-color: #EEF, #000; shadow-offset: 0px;" +
//                    "}" +
                "edge.shipment {" +
                "	fill-color: #999;" +
                "	arrow-size: 6px,3px;" +
                "}";


        @SuppressWarnings("UnusedDeclaration")
        public static String SIMPLE_WHITE =
            "node {" +
                "	size: 10px, 10px;" +
                "   fill-color: #6CC644;" +
                "	text-alignment: at-right;" +
                " 	stroke-mode: plain;" +
                "	stroke-color: #999;" +
                "	stroke-width: 1.0;" +
                "	text-font: couriernew;" +
                " 	text-offset: 2,-5;" +
                "	text-size: 8;" +
                "}" +
                "node.pickup {" +
                " 	fill-color: #6CC644;" +
                "}" +
                "node.delivery {" +
                " 	fill-color: #f93;" +
                "}" +
                "node.pickupInRoute {" +
                "	fill-color: #6CC644;" +
                " 	stroke-mode: plain;" +
                "	stroke-color: #333;" +
                "   stroke-width: 2.0;" +
                "}" +
                "node.deliveryInRoute {" +
                " 	fill-color: #f93;" +
                " 	stroke-mode: plain;" +
                "	stroke-color: #333;" +
                "   stroke-width: 2.0;" +
                "}" +
                "node.depot {" +
                " 	fill-color: #BD2C00;" +
                "	size: 10px, 10px;" +
                " 	shape: box;" +
                "}" +
                "node.removed {" +
                " 	fill-color: #BD2C00;" +
                "	size: 10px, 10px;" +
                " 	stroke-mode: plain;" +
                "	stroke-color: #333;" +
                "   stroke-width: 2.0;" +
                "}" +

                "edge {" +
                "	fill-color: #333;" +
                "	arrow-size: 6px,3px;" +
                "}" +
                "edge.shipment {" +
                "	fill-color: #999;" +
                "	arrow-size: 6px,3px;" +
                "}";

    }

    public static Graph createMultiGraph(String name, String style) {
        Graph g = new MultiGraph(name);
        g.addAttribute("ui.quality");
        g.addAttribute("ui.antialias");
        g.addAttribute("ui.stylesheet", style);
        return g;
    }

    public static ViewPanel createEmbeddedView(Graph graph, double scaling) {
        Viewer viewer = new Viewer(graph, Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
        ViewPanel view = viewer.addDefaultView(false);
        view.setPreferredSize(new Dimension((int) (698 * scaling), (int) (440 * scaling)));
        return view;
    }

    public static String STYLESHEET =
        "node {" +
            "	size: 10px, 10px;" +
            "   fill-color: #6CC644;" +
            "	text-alignment: at-right;" +
            " 	stroke-mode: plain;" +
            "	stroke-color: #999;" +
            "	stroke-width: 1.0;" +
            "	text-font: couriernew;" +
            " 	text-offset: 2,-5;" +
            "	text-size: 8;" +
            "}" +
            "node.pickup {" +
            " 	fill-color: #6CC644;" +
            "}" +
            "node.delivery {" +
            " 	fill-color: #f93;" +
            "}" +
            "node.pickupInRoute {" +
            "	fill-color: #6CC644;" +
            " 	stroke-mode: plain;" +
            "	stroke-color: #333;" +
            "   stroke-width: 2.0;" +
            "}" +
            "node.deliveryInRoute {" +
            " 	fill-color: #f93;" +
            " 	stroke-mode: plain;" +
            "	stroke-color: #333;" +
            "   stroke-width: 2.0;" +
            "}" +
            "node.depot {" +
            " 	fill-color: #BD2C00;" +
            "	size: 10px, 10px;" +
            " 	shape: box;" +
            "}" +
            "node.removed {" +
            " 	fill-color: #BD2C00;" +
            "	size: 10px, 10px;" +
            " 	stroke-mode: plain;" +
            "	stroke-color: #333;" +
            "   stroke-width: 2.0;" +
            "}" +

            "edge {" +
            "	fill-color: #333;" +
            "	arrow-size: 6px,3px;" +
            "}" +
            "edge.shipment {" +
            "	fill-color: #999;" +
            "	arrow-size: 6px,3px;" +
            "}";

    public static enum Label {
        NO_LABEL, ID, JOB_NAME, ARRIVAL_TIME, DEPARTURE_TIME, ACTIVITY
    }

    private static class Center {
        final double x;
        final double y;

        public Center(double x, double y) {
            super();
            this.x = x;
            this.y = y;
        }

    }

    private Label label = Label.NO_LABEL;

    private long renderDelay_in_ms = 0;

    private boolean renderShipments = false;

    private Center center;

    private VehicleRoutingProblem vrp;

    private VehicleRoutingProblemSolution solution;

    private double zoomFactor;

    private double scaling = 1.0;


    public GraphStreamViewer(VehicleRoutingProblem vrp) {
        super();
        this.vrp = vrp;
    }

    public GraphStreamViewer(VehicleRoutingProblem vrp, VehicleRoutingProblemSolution solution) {
        super();
        this.vrp = vrp;
        this.solution = solution;
    }

    public GraphStreamViewer labelWith(Label label) {
        this.label = label;
        return this;
    }

    public GraphStreamViewer setRenderDelay(long ms) {
        this.renderDelay_in_ms = ms;
        return this;
    }

    public GraphStreamViewer setRenderShipments(boolean renderShipments) {
        this.renderShipments = renderShipments;
        return this;
    }

    public GraphStreamViewer setGraphStreamFrameScalingFactor(double factor) {
        this.scaling = factor;
        return this;
    }

    /**
     * Sets the camera-view. Center describes the center-focus of the camera and zoomFactor its
     * zoomFactor.
     * <p>
     * <p>a zoomFactor < 1 zooms in and > 1 out.
     *
     * @param centerX    x coordinate of center
     * @param centerY    y coordinate of center
     * @param zoomFactor zoom factor
     * @return the viewer
     */
    public GraphStreamViewer setCameraView(double centerX, double centerY, double zoomFactor) {
        center = new Center(centerX, centerY);
        this.zoomFactor = zoomFactor;
        return this;
    }

    public void display() {
        System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");

        Graph g = createMultiGraph("g");

        ViewPanel view = createEmbeddedView(g, scaling);

        createJFrame(view, scaling);

        render(g, view);
    }

    private JFrame createJFrame(ViewPanel view, double scaling) {
        JFrame jframe = new JFrame();
        JPanel basicPanel = new JPanel();
        basicPanel.setLayout(new BoxLayout(basicPanel, BoxLayout.Y_AXIS));

        //result-panel
        JPanel resultPanel = createResultPanel();
        //graphstream-panel


        JPanel graphStreamPanel = new JPanel();
        graphStreamPanel.setPreferredSize(new Dimension((int) (800 * scaling), (int) (460 * scaling)));
        graphStreamPanel.setBackground(Color.WHITE);

        JPanel graphStreamBackPanel = new JPanel();
        graphStreamBackPanel.setPreferredSize(new Dimension((int) (700 * scaling), (int) (450 * scaling)));
        graphStreamBackPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
        graphStreamBackPanel.setBackground(Color.WHITE);

        graphStreamBackPanel.add(view);
        graphStreamPanel.add(graphStreamBackPanel);

        //setup basicPanel
        basicPanel.add(resultPanel);
        basicPanel.add(graphStreamPanel);
//		basicPanel.add(legendPanel);

        //put it together
        jframe.add(basicPanel);

        //conf jframe
        jframe.setSize((int) (800 * scaling), (int) (580 * scaling));
        jframe.setLocationRelativeTo(null);
        jframe.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        jframe.setVisible(true);
        jframe.pack();
        jframe.setTitle("jsprit - GraphStream");
        return jframe;
    }

    private Graph createMultiGraph(String name) {
        return GraphStreamViewer.createMultiGraph(name, STYLESHEET);
    }

    private void render(Graph g, ViewPanel view) {
        if (center != null) {
            view.resizeFrame(view.getWidth(), view.getHeight());
            alignCamera(view);
        }

        for (Vehicle vehicle : vrp.getVehicles()) {
            renderVehicle(g, vehicle, label);
            sleep(renderDelay_in_ms);
        }

        for (Job j : vrp.getJobs().values()) {
            if (j instanceof Service) {
                renderService(g, (Service) j, label);
            } else if (j instanceof Shipment) {
                renderShipment(g, (Shipment) j, label, renderShipments);
            }
            sleep(renderDelay_in_ms);
        }

        if (solution != null) {
            int routeId = 1;
            for (VehicleRoute route : solution.getRoutes()) {
                renderRoute(g, route, routeId, renderDelay_in_ms, label);
                sleep(renderDelay_in_ms);
                routeId++;
            }
        }

    }

    private void alignCamera(View view) {
        view.getCamera().setViewCenter(center.x, center.y, 0);
        view.getCamera().setViewPercent(zoomFactor);
    }

    private JLabel createEmptyLabel() {
        JLabel emptyLabel1 = new JLabel();
        emptyLabel1.setPreferredSize(new Dimension((int) (40 * scaling), (int) (25 * scaling)));
        return emptyLabel1;
    }

    private JPanel createResultPanel() {
        int width = 800;
        int height = 50;

        JPanel panel = new JPanel();
        panel.setPreferredSize(new Dimension((int) (width * scaling), (int) (height * scaling)));
        panel.setBackground(Color.WHITE);

        JPanel subpanel = new JPanel();
        subpanel.setLayout(new FlowLayout());
        subpanel.setPreferredSize(new Dimension((int) (700 * scaling), (int) (40 * scaling)));
        subpanel.setBackground(Color.WHITE);
        subpanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));

        Font font = Font.decode("couriernew");

        JLabel jobs = new JLabel("jobs");
        jobs.setFont(font);
        jobs.setPreferredSize(new Dimension((int) (40 * scaling), (int) (25 * scaling)));

        int noJobs = 0;
        if (this.vrp != null) noJobs = this.vrp.getJobs().values().size();

        JFormattedTextField nJobs = new JFormattedTextField(noJobs);
        nJobs.setFont(font);
        nJobs.setEditable(false);
        nJobs.setBorder(BorderFactory.createEmptyBorder());
        nJobs.setBackground(new Color(230, 230, 230));

        JLabel costs = new JLabel("costs");
        costs.setFont(font);
        costs.setPreferredSize(new Dimension((int) (40 * scaling), (int) (25 * scaling)));

        JFormattedTextField costsVal = new JFormattedTextField(getSolutionCosts());
        costsVal.setFont(font);
        costsVal.setEditable(false);
        costsVal.setBorder(BorderFactory.createEmptyBorder());
        costsVal.setBackground(new Color(230, 230, 230));

        JLabel vehicles = new JLabel("routes");
        vehicles.setFont(font);
        vehicles.setPreferredSize(new Dimension((int) (40 * scaling), (int) (25 * scaling)));
//        vehicles.setForeground(Color.DARK_GRAY);

        JFormattedTextField vehVal = new JFormattedTextField(getNoRoutes());
        vehVal.setFont(font);
        vehVal.setEditable(false);
        vehVal.setBorder(BorderFactory.createEmptyBorder());
//        vehVal.setForeground(Color.DARK_GRAY);
        vehVal.setBackground(new Color(230, 230, 230));

        //platzhalter
        JLabel placeholder1 = new JLabel();
        placeholder1.setPreferredSize(new Dimension((int) (60 * scaling), (int) (25 * scaling)));

        JLabel emptyLabel1 = createEmptyLabel();

        subpanel.add(jobs);
        subpanel.add(nJobs);

        subpanel.add(emptyLabel1);

        subpanel.add(costs);
        subpanel.add(costsVal);

        JLabel emptyLabel2 = createEmptyLabel();
        subpanel.add(emptyLabel2);

        subpanel.add(vehicles);
        subpanel.add(vehVal);

        panel.add(subpanel);

        return panel;
    }

    private Integer getNoRoutes() {
        if (solution != null) return solution.getRoutes().size();
        return 0;
    }

    private Double getSolutionCosts() {
        if (solution != null) return solution.getCost();
        return 0.0;
    }

    private void renderShipment(Graph g, Shipment shipment, Label label, boolean renderShipments) {

        Node n1 = g.addNode(makeId(shipment.getId(), shipment.getPickupLocation().getId()));
        if (label.equals(Label.ID)) n1.addAttribute("ui.label", shipment.getId());
        n1.addAttribute("x", shipment.getPickupLocation().getCoordinate().getX());
        n1.addAttribute("y", shipment.getPickupLocation().getCoordinate().getY());
        n1.setAttribute("ui.class", "pickup");

        Node n2 = g.addNode(makeId(shipment.getId(), shipment.getDeliveryLocation().getId()));
        if (label.equals(Label.ID)) n2.addAttribute("ui.label", shipment.getId());
        n2.addAttribute("x", shipment.getDeliveryLocation().getCoordinate().getX());
        n2.addAttribute("y", shipment.getDeliveryLocation().getCoordinate().getY());
        n2.setAttribute("ui.class", "delivery");

        if (renderShipments) {
            Edge s = g.addEdge(shipment.getId(), makeId(shipment.getId(), shipment.getPickupLocation().getId()),
                makeId(shipment.getId(), shipment.getDeliveryLocation().getId()), true);
            s.addAttribute("ui.class", "shipment");
        }

    }

    private void sleep(long renderDelay_in_ms2) {
        try {
            Thread.sleep(renderDelay_in_ms2);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void renderService(Graph g, Service service, Label label) {
        Node n = g.addNode(makeId(service.getId(), service.getLocation().getId()));
        if (label.equals(Label.ID)) n.addAttribute("ui.label", service.getId());
        n.addAttribute("x", service.getLocation().getCoordinate().getX());
        n.addAttribute("y", service.getLocation().getCoordinate().getY());
        if (service.getType().equals("pickup")) n.setAttribute("ui.class", "pickup");
        if (service.getType().equals("delivery")) n.setAttribute("ui.class", "delivery");
    }

    private String makeId(String id, String locationId) {
        return id + "_" + locationId;
    }

    private void renderVehicle(Graph g, Vehicle vehicle, Label label) {
        String nodeId = makeId(vehicle.getId(), vehicle.getStartLocation().getId());
        Node vehicleStart = g.addNode(nodeId);
        if (label.equals(Label.ID)) vehicleStart.addAttribute("ui.label", "depot");
//		if(label.equals(Label.ACTIVITY)) n.addAttribute("ui.label", "start");
        vehicleStart.addAttribute("x", vehicle.getStartLocation().getCoordinate().getX());
        vehicleStart.addAttribute("y", vehicle.getStartLocation().getCoordinate().getY());
        vehicleStart.setAttribute("ui.class", "depot");

        if (!vehicle.getStartLocation().getId().equals(vehicle.getEndLocation().getId())) {
            Node vehicleEnd = g.addNode(makeId(vehicle.getId(), vehicle.getEndLocation().getId()));
            if (label.equals(Label.ID)) vehicleEnd.addAttribute("ui.label", "depot");
            vehicleEnd.addAttribute("x", vehicle.getEndLocation().getCoordinate().getX());
            vehicleEnd.addAttribute("y", vehicle.getEndLocation().getCoordinate().getY());
            vehicleEnd.setAttribute("ui.class", "depot");

        }
    }

    private void renderRoute(Graph g, VehicleRoute route, int routeId, long renderDelay_in_ms, Label label) {
        int vehicle_edgeId = 1;
        String prevIdentifier = makeId(route.getVehicle().getId(), route.getVehicle().getStartLocation().getId());
        if (label.equals(Label.ACTIVITY) || label.equals(Label.JOB_NAME)) {
            Node n = g.getNode(prevIdentifier);
            n.addAttribute("ui.label", "start");
        }
        for (TourActivity act : route.getActivities()) {
            Job job = ((JobActivity) act).getJob();
            String currIdentifier = makeId(job.getId(), act.getLocation().getId());
            if (label.equals(Label.ACTIVITY)) {
                Node actNode = g.getNode(currIdentifier);
                actNode.addAttribute("ui.label", act.getName());
            } else if (label.equals(Label.JOB_NAME)) {
                Node actNode = g.getNode(currIdentifier);
                actNode.addAttribute("ui.label", job.getName());
            } else if (label.equals(Label.ARRIVAL_TIME)) {
                Node actNode = g.getNode(currIdentifier);
                actNode.addAttribute("ui.label", Time.parseSecondsToTime(act.getArrTime()));
            } else if (label.equals(Label.DEPARTURE_TIME)) {
                Node actNode = g.getNode(currIdentifier);
                actNode.addAttribute("ui.label", Time.parseSecondsToTime(act.getEndTime()));
            }
            g.addEdge(makeEdgeId(routeId, vehicle_edgeId), prevIdentifier, currIdentifier, true);
            if (act instanceof PickupActivity) g.getNode(currIdentifier).addAttribute("ui.class", "pickupInRoute");
            else if (act instanceof DeliveryActivity)
                g.getNode(currIdentifier).addAttribute("ui.class", "deliveryInRoute");
            prevIdentifier = currIdentifier;
            vehicle_edgeId++;
            sleep(renderDelay_in_ms);
        }
        if (route.getVehicle().isReturnToDepot()) {
            String lastIdentifier = makeId(route.getVehicle().getId(), route.getVehicle().getEndLocation().getId());
            g.addEdge(makeEdgeId(routeId, vehicle_edgeId), prevIdentifier, lastIdentifier, true);
        }
    }

    private String makeEdgeId(int routeId, int vehicle_edgeId) {
        return Integer.valueOf(routeId).toString() + "." + Integer.valueOf(vehicle_edgeId).toString();
    }

    //	public void saveAsPNG(String filename){
    //
    //	}
}
