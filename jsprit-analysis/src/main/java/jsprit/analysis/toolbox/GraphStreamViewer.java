package jsprit.analysis.toolbox;

import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.job.Job;
import jsprit.core.problem.job.Service;
import jsprit.core.problem.job.Shipment;
import jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.solution.route.activity.TourActivity;
import jsprit.core.problem.solution.route.activity.TourActivity.JobActivity;
import jsprit.core.problem.vehicle.Vehicle;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.ui.swingViewer.Viewer;

public class GraphStreamViewer {

	 protected static String styleSheet =
		        "node {" +
		        "	size: 7px, 7px;" +
		        "   fill-color: rgb(255,204,0);" +
		        "	text-alignment: at-right;" +
		        " 	stroke-mode: plain;" + 
		        "	stroke-color: black;" +
		        "	stroke-width: 1.0;" +
		         "}" +
		         "node.pickup {" +
		         " 	fill-color: rgb(255,204,0);" +
		         "}" +
		         "node.delivery {" +
		         " 	fill-color: rgb(0,102,153);" +
		         "}" +
		        "node.depot {" +
		         " 	fill-color: red;" +
		         "	size: 10px, 10px;"  +
		         " 	shape: box;" +
		         "}" +
		         "edge {" +
		         "	fill-color: black;" +
		         "	arrow-size: 6px,3px;" +
		         "}" +
		         "edge.shipment {" +
		         "	fill-color: grey;" +
		         "	arrow-size: 6px,3px;" +
		         "}" ;
	
	
	private static class View {
		private long renderDelay = 0;
		private Label label = Label.NO_LABEL;
		private boolean renderShipments = false;
		private boolean enableAutoDisplay = false;
		private BoundingBox boundingBox;
		
		/**
		 * @param boundingBox the boundingBox to set
		 */
		public void setBoundingBox(BoundingBox boundingBox) {
			this.boundingBox = boundingBox;
		}

		private VehicleRoutingProblem vrp;
		private VehicleRoutingProblemSolution solution;
		
		public View(VehicleRoutingProblem vrp) {
			super();
			this.vrp = vrp;
		}
		
		public View(VehicleRoutingProblem vrp, VehicleRoutingProblemSolution solution) {
			super();
			this.vrp = vrp;
			this.solution = solution;
		}
		
		public void setEnableAutoDisplay(boolean enableAutoDisplay) {
			this.enableAutoDisplay = enableAutoDisplay;
		}

		public void setRenderDelay(long renderDelay) {
			this.renderDelay = renderDelay;
		}

		public void setLabel(Label label) {
			this.label = label;
		}

		public void setRenderShipments(boolean renderShipments) {
			this.renderShipments = renderShipments;
		}
		
	}
	
	
	 
//	public static void display(VehicleRoutingProblem vrp, int renderDelay_in_ms) {
//		View builder = new View(vrp);
//		builder.setRenderDelay(renderDelay_in_ms);
//		display();
//	}

	
	 
//	public static void display(VehicleRoutingProblem vrp) {
//		display();
//	}
	
//	public static void display(VehicleRoutingProblem vrp, VehicleRoutingProblemSolution solution, int renderDelay_in_ms, boolean enableAutoLayout) {
//		View view = new View(vrp,solution);
//		view.setEnableAutoDisplay(enableAutoLayout);
//		view.setRenderDelay(renderDelay_in_ms);
//		display();
//	}
//	
//	public static void display(VehicleRoutingProblem vrp, VehicleRoutingProblemSolution solution, int renderDelay_in_ms) {
//		View view = new View(vrp,solution);
//		view.setRenderDelay(renderDelay_in_ms);
//		display();
//	}
//	
//	public static void display(VehicleRoutingProblem vrp, VehicleRoutingProblemSolution solution) {
//		display();
//	}
	
	public static enum Label {
		NO_LABEL, ID
	}
	
	private static class BoundingBox {
		final double minX;
		final double minY;
		final double maxX;
		final double maxY;
		
		public BoundingBox(double minX, double minY, double maxX, double maxY) {
			super();
			this.minX = minX;
			this.minY = minY;
			this.maxX = maxX;
			this.maxY = maxY;
		}
		
	}
	
	private Label label = Label.NO_LABEL;
	
	private long renderDelay_in_ms = 0;
	
	private boolean enableAutoLayout = false;
	
	private boolean renderShipments = false;
	
	private BoundingBox boundingBox;

	private VehicleRoutingProblem vrp;
	
	private VehicleRoutingProblemSolution solution;
	
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
		this.enableAutoLayout = enableAutoLayout;
		return this;
	}
	
	public GraphStreamViewer setRenderShipments(boolean renderShipments){
		this.renderShipments = renderShipments;
		return this;
	}
	
//	public GraphStreamViewer setBoundingBox(double minX, double minY, double maxX, double maxY){
//		boundingBox = new BoundingBox(minX,minY,maxX,maxY);
//		return this;
//	}

//	public void display(){
//		display();
////		View view = new View(vrp,solution);
////		view.setEnableAutoDisplay(enableAutoLayout);
////		view.setLabel(label);
////		view.setRenderDelay(renderDelay_in_ms);
////		view.setRenderShipments(renderShipments);
////		view.setBoundingBox(boundingBox);
////		display(view);
//	}

	public void display(){
			System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
		    Graph g = new MultiGraph("g");
		    g.addAttribute("ui.quality");
		    g.addAttribute("ui.antialias");
		    g.addAttribute("ui.stylesheet", styleSheet);
			
		    Viewer viewer = g.display();
		    if(!enableAutoLayout) viewer.disableAutoLayout();
		    
		    
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
		    		renderRoute(g,route,routeId,renderDelay_in_ms);
		    		sleep(renderDelay_in_ms);
		    		routeId++;
		    	}
		    }
		    
	//	    if(view.boundingBox != null){
	////	    	viewer.getDefaultView().getCamera().setViewPercent(0.5);
	//	    	System.out.println("metric="+viewer.getDefaultView().getCamera().getMetrics());
	////	    	viewer.getDefaultView().getCamera().setViewCenter(15000, 50000, 0);
	////	    	viewer.getDefaultView().getCamera().setViewPercent(0.5);
	//	    	viewer.getDefaultView().getCamera().setBounds(10000,40000, 0, 20000, 60000, 0);
	//	    	System.out.println("metric="+viewer.getDefaultView().getCamera().getMetrics());
	////	    	viewer.getDefaultView().se
	//	    	viewer.getDefaultView().display(viewer.getGraphicGraph(), true);
	////	    	viewer.getDefaultView().getCamera().setViewPercent(0.5);
	//	    			
	//	    }
		
		}

	//	public static void display(VehicleRoutingProblem vrp, int renderDelay_in_ms) {
	//		View builder = new View(vrp);
	//		builder.setRenderDelay(renderDelay_in_ms);
	//		display();
	//	}
	
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
		n.addAttribute("x", vehicle.getCoord().getX());
		n.addAttribute("y", vehicle.getCoord().getY());
		n.setAttribute("ui.class", "depot");
	}

	//	public static void display(VehicleRoutingProblem vrp) {
	//		display();
	//	}
		
	//	public static void display(VehicleRoutingProblem vrp, VehicleRoutingProblemSolution solution, int renderDelay_in_ms, boolean enableAutoLayout) {
	//		View view = new View(vrp,solution);
	//		view.setEnableAutoDisplay(enableAutoLayout);
	//		view.setRenderDelay(renderDelay_in_ms);
	//		display();
	//	}
	//	
	//	public static void display(VehicleRoutingProblem vrp, VehicleRoutingProblemSolution solution, int renderDelay_in_ms) {
	//		View view = new View(vrp,solution);
	//		view.setRenderDelay(renderDelay_in_ms);
	//		display();
	//	}
	//	
	//	public static void display(VehicleRoutingProblem vrp, VehicleRoutingProblemSolution solution) {
	//		display();
	//	}
		
	private void renderRoute(Graph g, VehicleRoute route, int routeId, long renderDelay_in_ms) {
		int vehicle_edgeId = 1;
		String prevIdentifier = makeId(route.getVehicle().getId(),route.getVehicle().getLocationId());
		for(TourActivity act : route.getActivities()){
			String currIdentifier = makeId(((JobActivity)act).getJob().getId(),act.getLocationId());
			g.addEdge(makeEdgeId(routeId,vehicle_edgeId), prevIdentifier, currIdentifier, true);
			prevIdentifier = currIdentifier;
			vehicle_edgeId++;
			sleep(renderDelay_in_ms);
		}
		if(route.getVehicle().isReturnToDepot()){
			g.addEdge(makeEdgeId(routeId,vehicle_edgeId), prevIdentifier, makeId(route.getVehicle().getId(),route.getVehicle().getLocationId()), true);
		}
	}

	private String makeEdgeId(int routeId, int vehicle_edgeId) {
		return Integer.valueOf(routeId).toString() + "." + Integer.valueOf(vehicle_edgeId).toString();
	}

//	public void saveAsPNG(String filename){
//		
//	}
}
