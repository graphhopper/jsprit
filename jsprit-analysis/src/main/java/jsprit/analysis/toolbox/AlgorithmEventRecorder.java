/*******************************************************************************
 * Copyright (C) 2014  Stefan Schroeder
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

import jsprit.core.algorithm.listener.AlgorithmEndsListener;
import jsprit.core.algorithm.listener.IterationStartsListener;
import jsprit.core.algorithm.recreate.InsertionData;
import jsprit.core.algorithm.recreate.listener.BeforeJobInsertionListener;
import jsprit.core.algorithm.recreate.listener.InsertionEndsListener;
import jsprit.core.algorithm.recreate.listener.InsertionStartsListener;
import jsprit.core.algorithm.ruin.listener.RuinListener;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.job.Job;
import jsprit.core.problem.job.Service;
import jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.solution.route.activity.TourActivity;
import jsprit.core.problem.vehicle.Vehicle;
import jsprit.core.problem.vehicle.VehicleImpl;
import jsprit.core.util.Solutions;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.stream.file.FileSinkDGS;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;


public class AlgorithmEventRecorder implements RuinListener, IterationStartsListener, InsertionStartsListener, BeforeJobInsertionListener, InsertionEndsListener, AlgorithmEndsListener {



    public static enum RecordPolicy {
        RECORD_AND_WRITE
    }

    public static final int BEFORE_RUIN_RENDER_SOLUTION = 2;

    public static final int RUIN = 0;

    public static final int RECREATE = 1;

    public static final int CLEAR_SOLUTION = 3;

    public static final int RENDER_FINAL_SOLUTION = 4;

    private FileWriter writer;

    private Graph graph;

    private FileSinkDGS fileSink;

    private int start_recording_at = 0;

    private int end_recording_at = Integer.MAX_VALUE;

    private int currentIteration = 0;

    public AlgorithmEventRecorder(VehicleRoutingProblem vrp, File outfile) {
        graph = new MultiGraph("g");
        try {
            writer = new FileWriter(outfile);
            fileSink = new FileSinkDGS();
            fileSink.begin(writer);
            graph.addSink(fileSink);
        } catch (IOException e) {
            e.printStackTrace();
        }
        initialiseGraph(vrp);
    }

    public void setRecordingRange(int startIteration, int endIteration){
        this.start_recording_at = startIteration;
        this.end_recording_at = endIteration;
    }

    public AlgorithmEventRecorder(VehicleRoutingProblem vrp, VehicleRoutingProblemSolution initialSolution, File outfile) {
//        this.outfile = outfile;
//        this.vrp = vrp;
//        graph = new MultiGraph("g");
//        try {
//            writer = new FileWriter(outfile);
//            writeHead();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        initialiseGraph(vrp,initialSolution);
    }

    private void initialiseGraph(VehicleRoutingProblem vrp, VehicleRoutingProblemSolution initialSolution) {

    }

    @Override
    public void ruinStarts(Collection<VehicleRoute> routes) {
        if(!record()) return;
        fileSink.stepBegins(graph.getId(),0,BEFORE_RUIN_RENDER_SOLUTION);
        recordRoutes(routes);
        fileSink.stepBegins(graph.getId(),0,RUIN);
    }

    private void recordRoutes(Collection<VehicleRoute> routes) {
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

    private boolean record() {
        return currentIteration >= start_recording_at && currentIteration <= end_recording_at;
    }

    @Override
    public void ruinEnds(Collection<VehicleRoute> routes, Collection<Job> unassignedJobs) {

    }

    @Override
    public void removed(Job job, VehicleRoute fromRoute) {
        if(!record()) return;
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
    }

    private String makeEdgeId(Node from, Node to) {
        return from.getId() + "_" + to.getId();
    }

    @Override
    public void informAlgorithmEnds(VehicleRoutingProblem problem, Collection<VehicleRoutingProblemSolution> solutions) {
        VehicleRoutingProblemSolution solution = Solutions.bestOf(solutions);
        fileSink.stepBegins(graph.getId(),0,BEFORE_RUIN_RENDER_SOLUTION);
        recordRoutes(solution.getRoutes());
        try {
            fileSink.end();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void informIterationStarts(int i, VehicleRoutingProblem problem, Collection<VehicleRoutingProblemSolution> solutions) {
        currentIteration = i;
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
        String startId = makeStartId(vehicle);
        Node node = graph.addNode(startId);
        node.addAttribute("x",vehicle.getStartLocationCoordinate().getX());
        node.addAttribute("y",vehicle.getStartLocationCoordinate().getY());
        node.addAttribute("ui.class","depot");

        String endId = makeEndId(vehicle);
        if(!startId.equals(endId)){
            Node endNode = graph.addNode(endId);
            endNode.addAttribute("x",vehicle.getEndLocationCoordinate().getX());
            endNode.addAttribute("y",vehicle.getEndLocationCoordinate().getY());
            endNode.addAttribute("ui.class","depot");
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
        Node serviceNode = graph.addNode(service.getId());
        serviceNode.addAttribute("x", service.getCoord().getX());
        serviceNode.addAttribute("y", service.getCoord().getY());
    }

    @Override
    public void informInsertionEnds(Collection<VehicleRoute> vehicleRoutes) {
        if(!record()) return;
        fileSink.stepBegins(graph.getId(),0,CLEAR_SOLUTION);
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
        if(!record()) return;
        markInserted(job);
        boolean vehicleSwitch = false;
        if(!(route.getVehicle() instanceof VehicleImpl.NoVehicle)) {
            if (!route.getVehicle().getId().equals(data.getSelectedVehicle().getId())) {
                vehicleSwitch = true;
            }
        }
        if(vehicleSwitch && !route.getActivities().isEmpty()){
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
    }

    private void removeEdge(String edgeId) {
        graph.removeEdge(edgeId);
    }

    private boolean isFirst(InsertionData data, VehicleRoute route) {
        return data.getDeliveryInsertionIndex() == 0;
    }

    private boolean isLast(InsertionData data, VehicleRoute route) {
        return data.getDeliveryInsertionIndex() == route.getActivities().size();
    }

    private void addEdge(String edgeId, String fromNode, String toNode)  {
        graph.addEdge(edgeId,fromNode,toNode,true);
    }

    @Override
    public void informInsertionStarts(Collection<VehicleRoute> vehicleRoutes, Collection<Job> unassignedJobs) {
        if(!record()) return;
        fileSink.stepBegins(graph.getId(),0,RECREATE);
    }
}
