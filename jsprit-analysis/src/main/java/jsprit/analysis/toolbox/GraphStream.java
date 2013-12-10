package jsprit.analysis.toolbox;

import java.util.HashMap;
import java.util.Map;

import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.job.Job;
import jsprit.core.problem.job.Service;
import jsprit.core.problem.job.Shipment;
import jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.solution.route.activity.TourActivity;
import jsprit.core.problem.solution.route.activity.TourActivity.JobActivity;
import jsprit.core.problem.vehicle.Vehicle;
import jsprit.core.util.Coordinate;
import jsprit.core.util.Locations;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.DefaultGraph;
import org.graphstream.ui.swingViewer.Viewer;

public class GraphStream {

	 protected static String styleSheet =
		        "node {" +
		        "	size: 10px, 10px;" +
		        "   fill-color: orange;" +
		        "	text-alignment: at-right;" +
		        " 	stroke-mode: plain;" + 
		        "	stroke-color: black;" +
		        
		         "}" +
		        "node.depot {" +
		         " 	fill-color: red;" +
		         "	size: 10px, 10px;"  +
		         " 	shape: box;" +
		         "}" +
		         "edge {" +
		         "	fill-color: black;" +
		         "}" ;
	
	public static void display(VehicleRoutingProblem vrp, int renderDelay_in_ms) {
		System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
	    Graph g = new DefaultGraph("g");
	    g.addAttribute("ui.quality");
	    g.addAttribute("ui.antialias");
	    g.addAttribute("ui.stylesheet", styleSheet);
		
	    Viewer viewer = g.display();
	    viewer.disableAutoLayout();
	    
	    for(Vehicle vehicle : vrp.getVehicles()){
			renderVehicle(g, vehicle);
			sleep(renderDelay_in_ms);
		}
	    
	    for(Job j : vrp.getJobs().values()){
	    	sleep(renderDelay_in_ms);
	    	if(j instanceof Service){
	    		renderService(g, j);
	    	}
	    }
	}

	private static void sleep(int renderDelay_in_ms) {
		try {
			Thread.sleep(renderDelay_in_ms);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		};
		
	}

	private static void renderService(Graph g, Job j) {
		Node n = g.addNode(j.getId());
		n.addAttribute("ui.label", j.getId());
		n.addAttribute("x", ((Service) j).getCoord().getX());
		n.addAttribute("y", ((Service) j).getCoord().getY());
	}

	private static void renderVehicle(Graph g, Vehicle vehicle) {
		Node n = g.addNode(vehicle.getId());
		n.addAttribute("ui.label", "depot");
		n.addAttribute("x", vehicle.getCoord().getX());
		n.addAttribute("y", vehicle.getCoord().getY());
		n.setAttribute("ui.class", "depot");
	}
	 
	public static void display(VehicleRoutingProblem vrp) {
		display(vrp,0);
	}

	public static void display(VehicleRoutingProblem vrp, VehicleRoutingProblemSolution solution, int renderDelay_in_ms, boolean enableAutoLayout) {
		System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
	    Graph g = new DefaultGraph("g");
	    g.addAttribute("ui.quality");
	    g.addAttribute("ui.antialias");
	    g.addAttribute("ui.stylesheet", styleSheet);
		
	    Viewer viewer = g.display();
	    if(!enableAutoLayout) viewer.disableAutoLayout();
	    
	    for(Vehicle vehicle : vrp.getVehicles()){
	    	renderVehicle(g,vehicle);
	    	sleep(renderDelay_in_ms);
	    }
	    
	    for(Job j : vrp.getJobs().values()){
	    	if(j instanceof Service){
	    		renderService(g,(Service)j);
	    	}
	    	sleep(renderDelay_in_ms);
	    }
	    
	    int routeId = 1;
	    for(VehicleRoute route : solution.getRoutes()){
	    	renderRoute(g,route,routeId,renderDelay_in_ms);
	    	sleep(renderDelay_in_ms);
	    	routeId++;
	    }
		
	}
	
	public static void display(VehicleRoutingProblem vrp, VehicleRoutingProblemSolution solution, int renderDelay_in_ms) {
		display(vrp,solution,renderDelay_in_ms,false);
	}
	
	public static void display(VehicleRoutingProblem vrp, VehicleRoutingProblemSolution solution) {
		display(vrp,solution,0,false);
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

	private static void renderRoute(Graph g, VehicleRoute route, int routeId, int renderDelay_in_ms) {
		int vehicle_edgeId = 1;
		String prevIdentifier = route.getVehicle().getId();
		for(TourActivity act : route.getActivities()){
			String currIdentifier = ((JobActivity)act).getJob().getId();
			g.addEdge(makeEdgeId(routeId,vehicle_edgeId), prevIdentifier, currIdentifier, true);
			prevIdentifier = currIdentifier;
			vehicle_edgeId++;
			sleep(renderDelay_in_ms);
		}
		if(route.getVehicle().isReturnToDepot()){
			g.addEdge(makeEdgeId(routeId,vehicle_edgeId), prevIdentifier, route.getVehicle().getId(), true);
		}
	}

	private static String makeEdgeId(int routeId, int vehicle_edgeId) {
		return Integer.valueOf(routeId).toString() + "." + Integer.valueOf(vehicle_edgeId).toString();
	}

}
