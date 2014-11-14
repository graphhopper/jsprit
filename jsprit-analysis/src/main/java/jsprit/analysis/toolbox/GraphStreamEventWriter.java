package jsprit.analysis.toolbox;

import jsprit.core.algorithm.listener.AlgorithmEndsListener;
import jsprit.core.algorithm.recreate.InsertionData;
import jsprit.core.algorithm.recreate.listener.BeforeJobInsertionListener;
import jsprit.core.algorithm.recreate.listener.InsertionEndsListener;
import jsprit.core.algorithm.ruin.listener.RuinListener;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.job.Job;
import jsprit.core.problem.job.Service;
import jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.solution.route.activity.TourActivity;
import jsprit.core.problem.vehicle.Vehicle;
import jsprit.core.problem.vehicle.VehicleImpl;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by stefan on 14.11.14.
 */
public class GraphStreamEventWriter implements RuinListener, BeforeJobInsertionListener, InsertionEndsListener, AlgorithmEndsListener {


    static class Edges {

        String inEdgeId;

        String outEdgeId;

        Edges(String inEdgeId, String outEdgeId) {
            this.inEdgeId = inEdgeId;
            this.outEdgeId = outEdgeId;
        }
    }

    private File outfile;

    private FileWriter writer;

    private Map<String,Edges> in_out_edges = new HashMap<String,Edges>();

    private Graph graph;

    private VehicleRoutingProblem vrp;

    private boolean notInitialized = true;

    public GraphStreamEventWriter(VehicleRoutingProblem vrp, File outfile) {
        this.outfile = outfile;
        this.vrp = vrp;
        graph = new MultiGraph("g");
        try {
            writer = new FileWriter(outfile);
            writeHead();
        } catch (IOException e) {
            e.printStackTrace();
        }
        initialiseGraph(vrp);
    }

    public GraphStreamEventWriter(VehicleRoutingProblem vrp, VehicleRoutingProblemSolution initialSolution, File outfile) {
        this.outfile = outfile;
        this.vrp = vrp;
        graph = new MultiGraph("g");
        try {
            writer = new FileWriter(outfile);
            writeHead();
        } catch (IOException e) {
            e.printStackTrace();
        }
        initialiseGraph(vrp,initialSolution);
    }

    private void initialiseGraph(VehicleRoutingProblem vrp, VehicleRoutingProblemSolution initialSolution) {

    }

    @Override
    public void ruinStarts(Collection<VehicleRoute> routes) {
        for(VehicleRoute route : routes){
            String prevNode = makeStartId(route.getVehicle());
            for(TourActivity act : route.getActivities()){
                String actNode = ((TourActivity.JobActivity)act).getJob().getId();
                addEdge(prevNode+"_"+actNode,prevNode,actNode);
                prevNode = actNode;
            }
            String lastNode = makeEndId(route.getVehicle());
            addEdge(prevNode+"_"+lastNode,prevNode,lastNode);
        }
    }

    private void removeNode(Node node) {
        graph.removeNode(node);
        String eventString = "dn " + node.getId() + "\n";
        System.out.print(eventString);
        try {
            writer.write(eventString);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void ruinEnds(Collection<VehicleRoute> routes, Collection<Job> unassignedJobs) {

    }

    @Override
    public void removed(Job job, VehicleRoute fromRoute) {
        System.out.println("remove job " + job.getId());
        String nodeId = job.getId();
        Node node = graph.getNode(nodeId);

        markRemoved(node);

        Edge entering = node.getEnteringEdge(0);
        removeEdge(entering.getId());
        Edge leaving = node.getLeavingEdge(0);
        removeEdge((leaving.getId()));
        Node from = entering.getNode0();
        Node to = leaving.getNode1();
        if(!fromRoute.getActivities().isEmpty()){
            addEdge(makeEdgeId(from,to),from.getId(),to.getId());
        }

    }

    private void markRemoved(Node node) {
        node.setAttribute("ui.class","removed");
        try {
            writer.write("cn " + node.getId() + " ui.class:removed\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String makeEdgeId(Node from, Node to) {
        return from.getId() + "_" + to.getId();
    }

    @Override
    public void informAlgorithmEnds(VehicleRoutingProblem problem, Collection<VehicleRoutingProblemSolution> solutions) {
        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeHead() throws IOException {
        writer.write("DGS004\n");
        writer.write("null 0 0\n");
    }

    private void initialiseGraph(VehicleRoutingProblem problem) {
        for(Vehicle vehicle : problem.getVehicles()){
            addVehicle(vehicle);
        }
        for(Job job : problem.getJobs().values()){
            Service service = (Service)job;
            addService(service);
        }

    }

    private void addVehicle(Vehicle vehicle) {
        try {
            String startId = makeStartId(vehicle);
            String startNodeEventString = "an " + startId + " x:" + vehicle.getStartLocationCoordinate().getX() + " y:"
                    + vehicle.getStartLocationCoordinate().getY() + " ui.class:depot\n";
            System.out.print(startNodeEventString);
            writer.write(startNodeEventString);
            graph.addNode(startId);
            String endId = makeEndId(vehicle);
            if(!startId.equals(endId)){
                String endNodeEventString = "an " + endId + " x:" + vehicle.getEndLocationCoordinate().getX() + " y:"
                        + vehicle.getEndLocationCoordinate().getY() + " ui.class:depot\n";
                System.out.print(endNodeEventString);
                writer.write( endNodeEventString);
                graph.addNode(endId);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String makeStartId(Vehicle vehicle) {
        return vehicle.getId() + "_start";
    }

    private String makeEndId(Vehicle vehicle) {
        if(vehicle.getStartLocationId().equals(vehicle.getEndLocationId())) return makeStartId(vehicle);
        return vehicle.getId() + "_end";
    }

    private void addService(Service service) {
        try {
            String eventString = "an " + service.getId() + " x:" + service.getCoord().getX() + " y:" + service.getCoord().getY() + "\n";
            System.out.print(eventString);
            writer.write(eventString);
            graph.addNode(service.getId());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void informInsertionEnds(Collection<VehicleRoute> vehicleRoutes) {
        for(VehicleRoute route : vehicleRoutes){
            String prevNode = makeStartId(route.getVehicle());
            for(TourActivity act : route.getActivities()){
                String actNode = ((TourActivity.JobActivity)act).getJob().getId();
                removeEdge(prevNode + "_" + actNode);
                prevNode = actNode;
            }
            String lastNode = makeEndId(route.getVehicle());
            removeEdge(prevNode + "_" + lastNode);
        }
    }

    @Override
    public void informBeforeJobInsertion(Job job, InsertionData data, VehicleRoute route) {
        System.out.println("insert job " + job.getId());
        markInserted(job);
        boolean vehicleSwitch = false;
        if(!(route.getVehicle() instanceof VehicleImpl.NoVehicle)) {
            if (!route.getVehicle().getId().equals(data.getSelectedVehicle().getId())) {
                vehicleSwitch = true;
            }
        }
        if(vehicleSwitch && !route.getActivities().isEmpty()){
            System.out.println("switch vehicle " + route.getVehicle().getId() + " --> " + data.getSelectedVehicle().getId());
            String oldStart = makeStartId(route.getVehicle());
            String firstAct = ((TourActivity.JobActivity)route.getActivities().get(0)).getJob().getId();
            String oldEnd = makeEndId(route.getVehicle());
            String lastAct = ((TourActivity.JobActivity)route.getActivities().get(route.getActivities().size()-1)).getJob().getId();
            removeEdge(oldStart + "_" + firstAct);
            removeEdge(lastAct + "_" + oldEnd);
            String newStart = makeStartId(data.getSelectedVehicle());
            String newEnd = makeEndId(data.getSelectedVehicle());
            addEdge(newStart + "_" + firstAct,newStart,firstAct);
            addEdge(lastAct + "_" + newEnd, lastAct,newEnd);
        }
        String node_i;

        if(isFirst(data,route)) {
            node_i = makeStartId(data.getSelectedVehicle());
        }
        else {
            node_i = ((TourActivity.JobActivity)route.getActivities().get(data.getDeliveryInsertionIndex()-1)).getJob().getId();
        }
        String node_k = job.getId();
        String edgeId_1 =  node_i + "_" + node_k;
        String node_j;
        if(isLast(data,route)) {
            node_j = makeEndId(data.getSelectedVehicle());
        }
        else {
            node_j = ((TourActivity.JobActivity)route.getActivities().get(data.getDeliveryInsertionIndex())).getJob().getId();
        }
        String edgeId_2 = node_k + "_" + node_j;

        addEdge(edgeId_1, node_i, node_k);
        addEdge(edgeId_2, node_k, node_j);
        if(!route.getActivities().isEmpty()){
             removeEdge(node_i + "_" + node_j);
        }
    }

    private void markInserted(Job job) {
        graph.getNode(job.getId()).removeAttribute("ui.class");
        try {
            writer.write("cn " + job.getId() + " -ui.class\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void removeEdge(String edgeId) {
        try {
            String eventString = "de " + edgeId + "\n";
            System.out.print(eventString);
            writer.write(eventString);
            graph.removeEdge(edgeId);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean isFirst(InsertionData data, VehicleRoute route) {
        return data.getDeliveryInsertionIndex() == 0;
    }

    private boolean isLast(InsertionData data, VehicleRoute route) {
        return data.getDeliveryInsertionIndex() == route.getActivities().size();
    }

    private void addEdge(String edgeId, String fromNode, String toNode)  {
        try {
            String eventString = "ae " + edgeId + " " + fromNode + " > " + toNode + "\n";
            System.out.print(eventString);
            writer.write(eventString);
            graph.addEdge(edgeId,fromNode,toNode,true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
