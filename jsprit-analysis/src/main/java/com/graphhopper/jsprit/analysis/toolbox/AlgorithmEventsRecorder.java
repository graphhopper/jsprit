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

import com.graphhopper.jsprit.core.algorithm.listener.AlgorithmEndsListener;
import com.graphhopper.jsprit.core.algorithm.listener.IterationStartsListener;
import com.graphhopper.jsprit.core.algorithm.recreate.InsertionData;
import com.graphhopper.jsprit.core.algorithm.recreate.listener.BeforeJobInsertionListener;
import com.graphhopper.jsprit.core.algorithm.recreate.listener.InsertionEndsListener;
import com.graphhopper.jsprit.core.algorithm.recreate.listener.InsertionStartsListener;
import com.graphhopper.jsprit.core.algorithm.ruin.listener.RuinListener;
import com.graphhopper.jsprit.core.problem.AbstractActivity;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Delivery;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.job.Shipment;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.util.Coordinate;
import com.graphhopper.jsprit.core.util.Solutions;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.stream.file.FileSinkDGS;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.zip.GZIPOutputStream;

/**
 * Writes out what happens when algorithm searches (in graphstream dgs-file).
 */
public class AlgorithmEventsRecorder implements RuinListener, IterationStartsListener, InsertionStartsListener, BeforeJobInsertionListener, InsertionEndsListener, AlgorithmEndsListener {

    private boolean renderShipments = false;

    public static final int BEFORE_RUIN_RENDER_SOLUTION = 2;

    public static final int RUIN = 0;

    public static final int RECREATE = 1;

    public static final int CLEAR_SOLUTION = 3;


    private Graph graph;

    private FileSinkDGS fileSink;

    private FileOutputStream fos;

    private GZIPOutputStream gzipOs;

    private int start_recording_at = 0;

    private int end_recording_at = Integer.MAX_VALUE;

    private int currentIteration = 0;

    private VehicleRoutingProblem vrp;

    public AlgorithmEventsRecorder(VehicleRoutingProblem vrp, String dgsFileLocation) {
        this.vrp = vrp;
        graph = new MultiGraph("g");
        try {
            File dgsFile = new File(dgsFileLocation);
            fos = new FileOutputStream(dgsFile);
            fileSink = new FileSinkDGS();
            if (dgsFile.getName().endsWith("gz")) {
                gzipOs = new GZIPOutputStream(fos);
                fileSink.begin(gzipOs);
            } else {
                fileSink.begin(fos);
            }
            graph.addSink(fileSink);
        } catch (IOException e) {
            e.printStackTrace();
        }
        initialiseGraph(vrp);
    }

    public void setRecordingRange(int startIteration, int endIteration) {
        this.start_recording_at = startIteration;
        this.end_recording_at = endIteration;
    }

    @Override
    public void ruinStarts(Collection<VehicleRoute> routes) {
        if (!record()) return;
        fileSink.stepBegins(graph.getId(), 0, BEFORE_RUIN_RENDER_SOLUTION);
        markAllNodesAsInserted();
        addRoutes(routes);
        fileSink.stepBegins(graph.getId(), 0, RUIN);
    }

    private void markAllNodesAsInserted() {
        for (Job j : vrp.getJobs().values()) {
            markInserted(j);
        }
    }

    private void addRoutes(Collection<VehicleRoute> routes) {
        for (VehicleRoute route : routes) {
            String prevNode = makeStartId(route.getVehicle());
            for (TourActivity act : route.getActivities()) {
                String actNodeId = getNodeId(act);
                addEdge(prevNode + "_" + actNodeId, prevNode, actNodeId);
                prevNode = actNodeId;
            }
            if (route.getVehicle().isReturnToDepot()) {
                String lastNode = makeEndId(route.getVehicle());
                addEdge(prevNode + "_" + lastNode, prevNode, lastNode);
            }

        }
    }

    private String getNodeId(TourActivity act) {
        String nodeId = null;
        if (act instanceof TourActivity.JobActivity) {
            Job job = ((TourActivity.JobActivity) act).getJob();
            if (job instanceof Service) {
                nodeId = job.getId();
            } else if (job instanceof Shipment) {
                if (act.getName().equals("pickupShipment")) nodeId = getFromNodeId((Shipment) job);
                else nodeId = getToNodeId((Shipment) job);
            }
        }
        return nodeId;
    }

    private boolean record() {
        return currentIteration >= start_recording_at && currentIteration <= end_recording_at;
    }

    @Override
    public void ruinEnds(Collection<VehicleRoute> routes, Collection<Job> unassignedJobs) {

    }

    @Override
    public void removed(Job job, VehicleRoute fromRoute) {
        if (!record()) return;
        if (job instanceof Service) removeService(job, fromRoute);
        else if (job instanceof Shipment) removeShipment(job, fromRoute);
    }

    private void removeShipment(Job job, VehicleRoute fromRoute) {
        Shipment shipment = (Shipment) job;
        String fromNodeId = getFromNodeId(shipment);
        String toNodeId = getToNodeId(shipment);
//        removeNodeAndBelongingEdges(fromNodeId,fromRoute);
//        removeNodeAndBelongingEdges(toNodeId,fromRoute);

        Edge enteringToNode = getEnteringEdge(toNodeId);
        if (enteringToNode.getNode0().getId().equals(fromNodeId)) {
            markRemoved(graph.getNode(fromNodeId));
            markRemoved(graph.getNode(toNodeId));
            // i -> from -> to -> j: rem(i,from), rem(from,to), rem(to,j), add(i,j)
            Edge enteringFromNode = getEnteringEdge(fromNodeId);
            removeEdge(enteringFromNode.getId());
            removeEdge(enteringToNode.getId());
            if (graph.getNode(toNodeId).getLeavingEdgeSet().isEmpty()) {
                if (fromRoute.getVehicle().isReturnToDepot())
                    throw new IllegalStateException("leaving edge is missing");
                return;
            }

            Edge leavingToNode = getLeavingEdge(toNodeId);
            removeEdge(leavingToNode.getId());
            Node from = enteringFromNode.getNode0();
            Node to = leavingToNode.getNode1();
            if (!fromRoute.getActivities().isEmpty()) {
                addEdge(makeEdgeId(from, to), from.getId(), to.getId());
            }
        } else {
            removeNodeAndBelongingEdges(fromNodeId, fromRoute);
            removeNodeAndBelongingEdges(toNodeId, fromRoute);
        }
    }

    private Edge getLeavingEdge(String toNodeId) {
        Collection<Edge> edges = graph.getNode(toNodeId).getLeavingEdgeSet();
        if (edges.size() == 1) return edges.iterator().next();
        else {
            for (Edge e : edges) {
                if (e.getId().startsWith("shipment")) {
                    continue;
                }
                return e;
            }
        }
        return null;
    }

    private Edge getEnteringEdge(String toNodeId) {
        Collection<Edge> enteringEdges = graph.getNode(toNodeId).getEnteringEdgeSet();
        if (enteringEdges.size() == 1) return enteringEdges.iterator().next();
        else {
            for (Edge e : enteringEdges) {
                if (e.getId().startsWith("shipment")) {
                    continue;
                }
                return e;
            }
        }
        return null;
    }

    private String getToNodeId(Shipment shipment) {
        return shipment.getId() + "_delivery";
    }

    private String getFromNodeId(Shipment shipment) {
        return shipment.getId() + "_pickup";
    }

    private void removeService(Job job, VehicleRoute fromRoute) {
        String nodeId = job.getId();
        removeNodeAndBelongingEdges(nodeId, fromRoute);
    }

    private void removeNodeAndBelongingEdges(String nodeId, VehicleRoute fromRoute) {
        Node node = graph.getNode(nodeId);
        markRemoved(node);
        Edge entering = getEnteringEdge(nodeId);
        removeEdge(entering.getId());

        if (node.getLeavingEdgeSet().isEmpty()) {
            if (fromRoute.getVehicle().isReturnToDepot()) throw new IllegalStateException("leaving edge is missing");
            return;
        }

        Edge leaving = getLeavingEdge(nodeId);
        removeEdge((leaving.getId()));
        Node from = entering.getNode0();
        Node to = leaving.getNode1();
        if (!fromRoute.getActivities().isEmpty()) {
            addEdge(makeEdgeId(from, to), from.getId(), to.getId());
        }
    }

    private void markRemoved(Node node) {
        node.setAttribute("ui.class", "removed");
    }

    private String makeEdgeId(Node from, Node to) {
        return from.getId() + "_" + to.getId();
    }

    @Override
    public void informAlgorithmEnds(VehicleRoutingProblem problem, Collection<VehicleRoutingProblemSolution> solutions) {
        VehicleRoutingProblemSolution solution = Solutions.bestOf(solutions);
        fileSink.stepBegins(graph.getId(), 0, BEFORE_RUIN_RENDER_SOLUTION);
        addRoutes(solution.getRoutes());
        finish();
    }

    private void finish() {
        try {
            fileSink.end();
            fos.close();
            if (gzipOs != null) gzipOs.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void informIterationStarts(int i, VehicleRoutingProblem problem, Collection<VehicleRoutingProblemSolution> solutions) {
        currentIteration = i;
    }

    private void initialiseGraph(VehicleRoutingProblem problem) {
        for (Vehicle vehicle : problem.getVehicles()) {
            addVehicle(vehicle);
        }
        for (Job job : problem.getJobs().values()) {
            addJob(job);
        }
    }

    private void addJob(Job job) {
        if (job instanceof Service) {
            Service service = (Service) job;
            addNode(service.getId(), service.getLocation().getCoordinate());
            markService(service);
        } else if (job instanceof Shipment) {
            Shipment shipment = (Shipment) job;
            String fromNodeId = getFromNodeId(shipment);
            addNode(fromNodeId, shipment.getPickupLocation().getCoordinate());
            String toNodeId = getToNodeId(shipment);
            addNode(toNodeId, shipment.getDeliveryLocation().getCoordinate());
            markShipment(shipment);
            if (renderShipments) {
                Edge e = graph.addEdge("shipment_" + fromNodeId + "_" + toNodeId, fromNodeId, toNodeId, true);
                e.addAttribute("ui.class", "shipment");
            }
        }
    }

    private void markShipment(Shipment shipment) {
        markPickup(getFromNodeId(shipment));
        markDelivery(getToNodeId(shipment));
    }

    private void markService(Service service) {
        if (service instanceof Delivery) {
            markDelivery(service.getId());
        } else {
            markPickup(service.getId());
        }
    }

    private void markPickup(String id) {
        graph.getNode(id).addAttribute("ui.class", "pickup");
    }

    private void markDelivery(String id) {
        graph.getNode(id).addAttribute("ui.class", "delivery");
    }

    private void addVehicle(Vehicle vehicle) {
        String startId = makeStartId(vehicle);
        Node node = graph.addNode(startId);
        node.addAttribute("x", vehicle.getStartLocation().getCoordinate().getX());
        node.addAttribute("y", vehicle.getStartLocation().getCoordinate().getY());
        node.addAttribute("ui.class", "depot");

        String endId = makeEndId(vehicle);
        if (!startId.equals(endId)) {
            Node endNode = graph.addNode(endId);
            endNode.addAttribute("x", vehicle.getEndLocation().getCoordinate().getX());
            endNode.addAttribute("y", vehicle.getEndLocation().getCoordinate().getY());
            endNode.addAttribute("ui.class", "depot");
        }
    }

    private String makeStartId(Vehicle vehicle) {
        return vehicle.getId() + "_start";
    }

    private String makeEndId(Vehicle vehicle) {
        if (vehicle.getStartLocation().getId().equals(vehicle.getEndLocation().getId())) return makeStartId(vehicle);
        return vehicle.getId() + "_end";
    }

    private void addNode(String nodeId, Coordinate nodeCoord) {
        Node node = graph.addNode(nodeId);
        node.addAttribute("x", nodeCoord.getX());
        node.addAttribute("y", nodeCoord.getY());
    }

    @Override
    public void informInsertionEnds(Collection<VehicleRoute> vehicleRoutes) {
        if (!record()) return;
        fileSink.stepBegins(graph.getId(), 0, CLEAR_SOLUTION);
        removeRoutes(vehicleRoutes);
    }

    private void removeRoutes(Collection<VehicleRoute> vehicleRoutes) {
        for (VehicleRoute route : vehicleRoutes) {
            String prevNode = makeStartId(route.getVehicle());
            for (TourActivity act : route.getActivities()) {
                String actNode = getNodeId(act);
                removeEdge(prevNode + "_" + actNode);
                prevNode = actNode;
            }
            if (route.getVehicle().isReturnToDepot()) {
                String lastNode = makeEndId(route.getVehicle());
                removeEdge(prevNode + "_" + lastNode);
            }
        }
    }

    @Override
    public void informBeforeJobInsertion(Job job, InsertionData data, VehicleRoute route) {
        if (!record()) return;
        markInserted(job);
        handleVehicleSwitch(data, route);
        insertJob(job, data, route);
    }

    private void insertJob(Job job, InsertionData data, VehicleRoute route) {
        if (job instanceof Service) insertService(job, data, route);
        else if (job instanceof Shipment) insertShipment(job, data, route);
    }

    private void insertShipment(Job job, InsertionData data, VehicleRoute route) {
        String fromNodeId = getFromNodeId((Shipment) job);
        String toNodeId = getToNodeId((Shipment) job);
        insertNode(toNodeId, data.getDeliveryInsertionIndex(), data, route);

        List<AbstractActivity> del = vrp.getActivities(job);
        VehicleRoute copied = VehicleRoute.copyOf(route);
        copied.getTourActivities().addActivity(data.getDeliveryInsertionIndex(), del.get(1));

        insertNode(fromNodeId, data.getPickupInsertionIndex(), data, copied);
    }

    private void insertService(Job job, InsertionData data, VehicleRoute route) {
        insertNode(job.getId(), data.getDeliveryInsertionIndex(), data, route);
    }

    private void insertNode(String nodeId, int insertionIndex, InsertionData data, VehicleRoute route) {
//        VehicleRoute copied = VehicleRoute.copyOf(route);

        String node_i;

        if (isFirst(insertionIndex)) {
            node_i = makeStartId(data.getSelectedVehicle());
        } else {
            TourActivity.JobActivity jobActivity = (TourActivity.JobActivity) route.getActivities().get(insertionIndex - 1);
            node_i = getNodeId(jobActivity);
        }
        String edgeId_1 = node_i + "_" + nodeId;
        String node_j;
        if (isLast(insertionIndex, route)) {
            node_j = makeEndId(data.getSelectedVehicle());
        } else {
            TourActivity.JobActivity jobActivity = (TourActivity.JobActivity) route.getActivities().get(insertionIndex);
            node_j = getNodeId(jobActivity);
        }
        String edgeId_2 = nodeId + "_" + node_j;

        addEdge(edgeId_1, node_i, nodeId);

        if (!(isLast(insertionIndex, route) && !data.getSelectedVehicle().isReturnToDepot())) {
            addEdge(edgeId_2, nodeId, node_j);
            if (!route.getActivities().isEmpty()) {
                removeEdge(node_i + "_" + node_j);
            }
        }
    }

    private void handleVehicleSwitch(InsertionData data, VehicleRoute route) {
        boolean vehicleSwitch = false;
        if (!(route.getVehicle() instanceof VehicleImpl.NoVehicle)) {
            if (!route.getVehicle().getId().equals(data.getSelectedVehicle().getId())) {
                vehicleSwitch = true;
            }
        }
        if (vehicleSwitch && !route.getActivities().isEmpty()) {
            String oldStart = makeStartId(route.getVehicle());
            String firstAct = ((TourActivity.JobActivity) route.getActivities().get(0)).getJob().getId();
            String oldEnd = makeEndId(route.getVehicle());
            String lastAct = ((TourActivity.JobActivity) route.getActivities().get(route.getActivities().size() - 1)).getJob().getId();
            removeEdge(oldStart + "_" + firstAct);

            if (route.getVehicle().isReturnToDepot()) {
                removeEdge(lastAct + "_" + oldEnd);
            }

            String newStart = makeStartId(data.getSelectedVehicle());
            String newEnd = makeEndId(data.getSelectedVehicle());
            addEdge(newStart + "_" + firstAct, newStart, firstAct);

            if (data.getSelectedVehicle().isReturnToDepot()) {
                addEdge(lastAct + "_" + newEnd, lastAct, newEnd);
            }
        }
    }

    private void markInserted(Job job) {
        if (job instanceof Service) {
            markService((Service) job);
        } else {
            markShipment((Shipment) job);
        }
    }

    private void removeEdge(String edgeId) {
        markEdgeRemoved(edgeId);
        graph.removeEdge(edgeId);
    }

    private void markEdgeRemoved(String edgeId) {
        graph.getEdge(edgeId).addAttribute("ui.class", "removed");
    }

    private boolean isFirst(int index) {
        return index == 0;
    }

    private boolean isLast(int index, VehicleRoute route) {
        return index == route.getActivities().size();
    }

    private void addEdge(String edgeId, String fromNode, String toNode) {
        graph.addEdge(edgeId, fromNode, toNode, true);
        markEdgeInserted(edgeId);
    }

    private void markEdgeInserted(String edgeId) {
        graph.getEdge(edgeId).addAttribute("ui.class", "inserted");
        graph.getEdge(edgeId).removeAttribute("ui.class");
    }

    @Override
    public void informInsertionStarts(Collection<VehicleRoute> vehicleRoutes, Collection<Job> unassignedJobs) {
        if (!record()) return;
        fileSink.stepBegins(graph.getId(), 0, RECREATE);
    }
}
