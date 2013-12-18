package jsprit.analysis.toolbox;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.job.Job;
import jsprit.core.problem.job.Service;
import jsprit.core.problem.job.Shipment;
import jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.solution.route.activity.DeliveryActivity;
import jsprit.core.problem.solution.route.activity.PickupActivity;
import jsprit.core.problem.solution.route.activity.TourActivity;
import jsprit.core.problem.solution.route.activity.TourActivity.JobActivity;
import jsprit.core.problem.vehicle.Vehicle;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.ui.swingViewer.View;
import org.graphstream.ui.swingViewer.Viewer;



public class GraphStreamViewer {

	protected static String styleSheet =
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
					"	size: 10px, 10px;"  +
					" 	shape: box;" +
					"}" +
					
					"edge {" +
					"	fill-color: #333;" +
					"	arrow-size: 6px,3px;" +
					"}" +
					"edge.shipment {" +
					"	fill-color: #999;" +
					"	arrow-size: 6px,3px;" +
					"}" ;

	public static enum Label {
		NO_LABEL, ID, ACTIVITY
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

	public GraphStreamViewer labelWith(Label label){
		this.label=label;
		return this;
	}

	public GraphStreamViewer setRenderDelay(long ms){
		this.renderDelay_in_ms=ms;
		return this;
	}

	public GraphStreamViewer setEnableAutoLayout(boolean enableAutoLayout) {
		return this;
	}

	public GraphStreamViewer setRenderShipments(boolean renderShipments){
		this.renderShipments = renderShipments;
		return this;
	}
	
	public GraphStreamViewer setGraphStreamFrameScalingFactor(double factor){
		this.scaling=factor;
		return this;
	}

	/**
	 * Sets the camera-view. Center describes the center-focus of the camera and zoomFactor its 
	 * zoomFactor.
	 * 
	 * <p>a zoomFactor < 1 zooms in and > 1 out.
	 * 
	 * @param centerX
	 * @param centerY
	 * @param zoomFactor
	 * @return
	 */
	public GraphStreamViewer setCameraView(double centerX, double centerY, double zoomFactor){
		center = new Center(centerX,centerY);
		this.zoomFactor = zoomFactor; 
		return this;
	}
	
	public void display(){
		System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
		
		JFrame jframe = new JFrame();
		
		JPanel basicPanel = new JPanel();
		basicPanel.setLayout(new BoxLayout(basicPanel, BoxLayout.Y_AXIS));
		
		//result-panel
		JPanel resultPanel = createResultPanel();
		//graphstream-panel
		Graph g = new MultiGraph("g");
		g.addAttribute("ui.quality");
		g.addAttribute("ui.antialias");
		g.addAttribute("ui.stylesheet", styleSheet);

		JPanel graphStreamPanel = new JPanel();
		graphStreamPanel.setPreferredSize(new Dimension((int)(800*scaling),(int)(460*scaling)));
		graphStreamPanel.setBackground(Color.WHITE);
		
		JPanel graphStreamBackPanel = new JPanel();
		graphStreamBackPanel.setPreferredSize(new Dimension((int)(700*scaling),(int)(450*scaling)));
		graphStreamBackPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
		graphStreamBackPanel.setBackground(Color.WHITE);
		
		Viewer viewer = new Viewer(g,Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD); 
		View view = viewer.addDefaultView(false);
		view.setPreferredSize(new Dimension((int)(698*scaling),(int)(440*scaling)));
			
		graphStreamBackPanel.add(view);	
		graphStreamPanel.add(graphStreamBackPanel);
		
		//setup basicPanel
		basicPanel.add(resultPanel);
		basicPanel.add(graphStreamPanel);
//		basicPanel.add(legendPanel);
		
		//put it together
		jframe.add(basicPanel);
		
		//conf jframe
		jframe.setSize((int)(800*scaling),(int)(580*scaling));
		jframe.setLocationRelativeTo(null);
		jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jframe.setVisible(true);
		jframe.pack();
		jframe.setTitle("jsprit - GraphStream");
		
		//start rendering graph
		render(g,view);

	}

	private void render(Graph g, View view) {
		if(center != null){
			view.resizeFrame(view.getWidth(), view.getHeight());
			view.getCamera().setViewCenter(center.x, center.y, 0);
			view.getCamera().setViewPercent(zoomFactor);
		}

		for(Vehicle vehicle : vrp.getVehicles()){
			renderVehicle(g,vehicle,label);
			sleep(renderDelay_in_ms);
		}

		for(Job j : vrp.getJobs().values()){
			if(j instanceof Service){
				renderService(g,(Service)j,label);
			}
			else if(j instanceof Shipment){
				renderShipment(g,(Shipment)j,label,renderShipments);
			}
			sleep(renderDelay_in_ms);
		}

		if(solution != null){
			int routeId = 1;
			for(VehicleRoute route : solution.getRoutes()){
				renderRoute(g,route,routeId,renderDelay_in_ms,label);
				sleep(renderDelay_in_ms);
				routeId++;
			}
		}
		
	}

	private JLabel createEmptyLabel() {
		JLabel emptyLabel1 = new JLabel();
        emptyLabel1.setPreferredSize(new Dimension((int)(40*scaling),(int)(25*scaling)));
		return emptyLabel1;
	}

	private JPanel createResultPanel() {
		int width = 800;
		int height = 50;
		
		JPanel panel = new JPanel();
		panel.setPreferredSize(new Dimension((int)(width*scaling),(int)(height*scaling)));
        panel.setBackground(Color.WHITE);
        
        JPanel subpanel = new JPanel();
        subpanel.setLayout(new FlowLayout());
        subpanel.setPreferredSize(new Dimension((int)(700*scaling),(int)(40*scaling)));
        subpanel.setBackground(Color.WHITE);
        subpanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY,1));
        
        Font font = Font.decode("couriernew");

        JLabel jobs = new JLabel(new String("jobs"));
        jobs.setFont(font);
        jobs.setPreferredSize(new Dimension((int)(40*scaling),(int)(25*scaling)));

        JFormattedTextField nJobs = new JFormattedTextField(this.vrp.getJobs().values().size());
        nJobs.setFont(font);
        nJobs.setEditable(false);
        nJobs.setBorder(BorderFactory.createEmptyBorder());
        nJobs.setBackground(new Color(230,230,230));
        
        JLabel costs = new JLabel(new String("costs"));
        costs.setFont(font);
        costs.setPreferredSize(new Dimension((int)(40*scaling),(int)(25*scaling)));

        JFormattedTextField costsVal = new JFormattedTextField(new Double(getSolutionCosts()));
        costsVal.setFont(font);
        costsVal.setEditable(false);
        costsVal.setBorder(BorderFactory.createEmptyBorder());
        costsVal.setBackground(new Color(230,230,230));
        
        JLabel vehicles = new JLabel(new String("routes"));
        vehicles.setFont(font);
        vehicles.setPreferredSize(new Dimension((int)(40*scaling),(int)(25*scaling)));
//        vehicles.setForeground(Color.DARK_GRAY);
        
        JFormattedTextField vehVal = new JFormattedTextField(getNuRoutes());
        vehVal.setFont(font);
        vehVal.setEditable(false);
        vehVal.setBorder(BorderFactory.createEmptyBorder());
//        vehVal.setForeground(Color.DARK_GRAY);
        vehVal.setBackground(new Color(230,230,230));
        
        //platzhalter
        JLabel placeholder1 = new JLabel();
        placeholder1.setPreferredSize(new Dimension((int)(60*scaling),(int)(25*scaling)));
        
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

	private Integer getNuRoutes() {
		if(solution!=null) return Integer.valueOf(solution.getRoutes().size());
		return 0;
	}

	private Double getSolutionCosts() {
		if(solution!=null) return Double.valueOf(solution.getCost());
		return 0.0;
	}

	private void renderShipment(Graph g, Shipment shipment, Label label, boolean renderShipments) {

		Node n1 = g.addNode(makeId(shipment.getId(),shipment.getPickupLocation()));
		if(label.equals(Label.ID)) n1.addAttribute("ui.label", shipment.getId());
		n1.addAttribute("x", shipment.getPickupCoord().getX());
		n1.addAttribute("y", shipment.getPickupCoord().getY());
		n1.setAttribute("ui.class", "pickup");

		Node n2 = g.addNode(makeId(shipment.getId(),shipment.getDeliveryLocation()));
		if(label.equals(Label.ID)) n2.addAttribute("ui.label", shipment.getId());
		n2.addAttribute("x", shipment.getDeliveryCoord().getX());
		n2.addAttribute("y", shipment.getDeliveryCoord().getY());
		n2.setAttribute("ui.class", "delivery");

		if(renderShipments){
			Edge s = g.addEdge(shipment.getId(), makeId(shipment.getId(),shipment.getPickupLocation()),
					makeId(shipment.getId(),shipment.getDeliveryLocation()), true);
			s.addAttribute("ui.class", "shipment");
		}

	}

	private void sleep(long renderDelay_in_ms2) {
		try {
			Thread.sleep(renderDelay_in_ms2);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		};

	}

	private void renderService(Graph g, Service service, Label label) {
		Node n = g.addNode(makeId(service.getId(),service.getLocationId()));
		if(label.equals(Label.ID)) n.addAttribute("ui.label", service.getId());
		n.addAttribute("x", service.getCoord().getX());
		n.addAttribute("y", service.getCoord().getY());
		if(service.getType().equals("pickup")) n.setAttribute("ui.class", "pickup");
		if(service.getType().equals("delivery")) n.setAttribute("ui.class", "delivery");
	}

	private String makeId(String id, String locationId) {
		return new StringBuffer().append(id).append("_").append(locationId).toString();
	}

	private void renderVehicle(Graph g, Vehicle vehicle, Label label) {
		Node n = g.addNode(makeId(vehicle.getId(),vehicle.getLocationId()));
		if(label.equals(Label.ID)) n.addAttribute("ui.label", "depot");
//		if(label.equals(Label.ACTIVITY)) n.addAttribute("ui.label", "start");
		n.addAttribute("x", vehicle.getCoord().getX());
		n.addAttribute("y", vehicle.getCoord().getY());
		n.setAttribute("ui.class", "depot");
	}

	private void renderRoute(Graph g, VehicleRoute route, int routeId, long renderDelay_in_ms, Label label) {
		int vehicle_edgeId = 1;
		String prevIdentifier = makeId(route.getVehicle().getId(),route.getVehicle().getLocationId());
		if(label.equals(Label.ACTIVITY)){
			Node n = g.getNode(prevIdentifier);
			n.addAttribute("ui.label", "start");
		}
		for(TourActivity act : route.getActivities()){
			String currIdentifier = makeId(((JobActivity)act).getJob().getId(),act.getLocationId());
			if(label.equals(Label.ACTIVITY)){
				Node actNode = g.getNode(currIdentifier);
				actNode.addAttribute("ui.label", act.getName());
			}
			g.addEdge(makeEdgeId(routeId,vehicle_edgeId), prevIdentifier, currIdentifier, true);
			if(act instanceof PickupActivity) g.getNode(currIdentifier).addAttribute("ui.class", "pickupInRoute");
			else if (act instanceof DeliveryActivity) g.getNode(currIdentifier).addAttribute("ui.class", "deliveryInRoute");
			prevIdentifier = currIdentifier;
			vehicle_edgeId++;
			sleep(renderDelay_in_ms);
		}
		if(route.getVehicle().isReturnToDepot()){
			String lastIdentifier = makeId(route.getVehicle().getId(),route.getVehicle().getLocationId());
			g.addEdge(makeEdgeId(routeId,vehicle_edgeId), prevIdentifier, lastIdentifier, true);
		}
	}

	private String makeEdgeId(int routeId, int vehicle_edgeId) {
		return Integer.valueOf(routeId).toString() + "." + Integer.valueOf(vehicle_edgeId).toString();
	}

	//	public void saveAsPNG(String filename){
	//		
	//	}
}
