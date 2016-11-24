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
import com.graphhopper.jsprit.core.algorithm.recreate.InsertActivity;
import com.graphhopper.jsprit.core.algorithm.recreate.InsertionData;
import com.graphhopper.jsprit.core.algorithm.recreate.listener.BeforeJobInsertionListener;
import com.graphhopper.jsprit.core.algorithm.recreate.listener.InsertionEndsListener;
import com.graphhopper.jsprit.core.algorithm.recreate.listener.InsertionStartsListener;
import com.graphhopper.jsprit.core.algorithm.ruin.listener.RuinListener;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.*;
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
import java.util.zip.GZIPOutputStream;

/**
 * Writes out what happens when algorithm searches (in graphstream dgs-file).
 */
public class AlgorithmEventsRecorder
        implements RuinListener, IterationStartsListener, InsertionStartsListener, BeforeJobInsertionListener, InsertionEndsListener, AlgorithmEndsListener {

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
        start_recording_at = startIteration;
        end_recording_at = endIteration;
    }

    @Override
    public void ruinStarts(Collection<VehicleRoute> routes) {
        if (!record()) {
            return;
        }
        fileSink.stepBegins(graph.getId(), 0, BEFORE_RUIN_RENDER_SOLUTION);
        markAllNodesAsInserted();
        addRoutes(routes);
        fileSink.stepBegins(graph.getId(), 0, RUIN);
    }

    private void markAllNodesAsInserted() {
        vrp.getJobs().values().forEach(this::markInserted);
    }

    private void addRoutes(Collection<VehicleRoute> routes) {
        for (VehicleRoute route : routes) {
            String prevNode = makeStartId(route.getVehicle());
            for (TourActivity act : route.getActivities()) {
                JobActivity jobActivity = (JobActivity) act;
                String actNodeId = makeNodeId(jobActivity);
                addEdge(makeEdgeId(prevNode, actNodeId), prevNode, actNodeId);
                prevNode = actNodeId;
            }
            if (route.getVehicle().isReturnToDepot()) {
                String lastNode = makeEndId(route.getVehicle());
                addEdge(makeEdgeId(prevNode, lastNode), prevNode, lastNode);
            }

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
        if (!record()) {
            return;
        }
        VehicleRoute copy = VehicleRoute.copyOf(fromRoute);
        for (JobActivity act : job.getActivityList().getAll()) {
            removeActivity(act, copy);
            boolean removed = copy.getTourActivities().removeActivity(act);
        }
    }

    private void removeActivity(JobActivity act, VehicleRoute fromRoute) {
        removeNodeAndBelongingEdges(makeNodeId(act), fromRoute);
    }


    private Edge getLeavingEdge(String toNodeId) {
        Collection<Edge> edges = graph.getNode(toNodeId).getLeavingEdgeSet();
        if (edges.size() == 1) {
            return edges.iterator().next();
        } else {
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
        if (enteringEdges.size() == 1) {
            return enteringEdges.iterator().next();
        } else {
            for (Edge e : enteringEdges) {
                if (e.getId().startsWith("shipment")) {
                    continue;
                }
                return e;
            }
        }
        return null;
    }


    private void removeNodeAndBelongingEdges(String nodeId, VehicleRoute fromRoute) {
        Node node = graph.getNode(nodeId);
        markRemoved(node);
        Edge entering = getEnteringEdge(nodeId);
        boolean enteringIsNotNull = entering != null;
        if (enteringIsNotNull) {
            removeEdge(entering.getId());
        }
        if (node.getLeavingEdgeSet().isEmpty()) {
            if (fromRoute.getVehicle().isReturnToDepot()) {
                throw new IllegalStateException("leaving edge is missing");
            }
            return;
        }
        Edge leaving = getLeavingEdge(nodeId);
        boolean leavingIsNotNull = leaving != null;
        if (leavingIsNotNull) {
            removeEdge((leaving.getId()));
        }
        if (enteringIsNotNull && leavingIsNotNull) {
            Node from = entering.getNode0();
            Node to = leaving.getNode1();
            if (!fromRoute.getActivities().isEmpty()) {
                addEdge(makeEdgeId(from, to), from.getId(), to.getId());
            }
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
            if (gzipOs != null) {
                gzipOs.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void informIterationStarts(int i, VehicleRoutingProblem problem, Collection<VehicleRoutingProblemSolution> solutions) {
        currentIteration = i;
    }

    private void initialiseGraph(VehicleRoutingProblem problem) {
        problem.getVehicles().forEach(this::addVehicle);
        problem.getJobs().values().forEach(this::addJob);
    }

    private void addJob(Job job) {
        for (JobActivity act : job.getActivityList().getAll()) {
            Node node = addNode(makeNodeId(act), act.getLocation().getCoordinate());
            setActTypeAttribute(node, act);
        }
    }

    private String makeNodeId(JobActivity act) {
        return act.getJob().getId() + "_" + act.getIndex();
    }

    private void setActTypeAttribute(Node node, JobActivity act) {
        if (act instanceof PickupActivity) {
            node.addAttribute("ui.class", "pickup");
        } else if (act instanceof DeliveryActivity) {
            node.addAttribute("ui.class", "delivery");
        } else if (act instanceof ExchangeActivity) {
            node.addAttribute("ui.class", "exchange");
        }
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
        if (vehicle.getStartLocation().getId().equals(vehicle.getEndLocation().getId())) {
            return makeStartId(vehicle);
        }
        return vehicle.getId() + "_end";
    }

    private Node addNode(String nodeId, Coordinate coordinate) {
        Node node = graph.addNode(nodeId);
        node.addAttribute("x", coordinate.getX());
        node.addAttribute("y", coordinate.getY());
        return node;
    }

    @Override
    public void informInsertionEnds(Collection<VehicleRoute> vehicleRoutes) {
        if (!record()) {
            return;
        }
        fileSink.stepBegins(graph.getId(), 0, CLEAR_SOLUTION);
        removeRoutes(vehicleRoutes);
    }

    private void removeRoutes(Collection<VehicleRoute> vehicleRoutes) {
        for (VehicleRoute route : vehicleRoutes) {
            String prevNodeId = makeStartId(route.getVehicle());
            for (TourActivity act : route.getActivities()) {
                JobActivity jobActivity = (JobActivity) act;
                String actNodeId = makeNodeId(jobActivity);
                removeEdge(makeEdgeId(prevNodeId, actNodeId));
                prevNodeId = actNodeId;
            }
            if (route.getVehicle().isReturnToDepot()) {
                String lastNodeId = makeEndId(route.getVehicle());
                removeEdge(makeEdgeId(prevNodeId, lastNodeId));
            }
        }
    }

    @Override
    public void informBeforeJobInsertion(Job job, InsertionData data, VehicleRoute route) {
        if (!record()) {
            return;
        }
        markInserted(job);
        handleVehicleSwitch(data, route);
        insertJob(data, route);
    }

    private void insertJob(InsertionData data, VehicleRoute route) {
        VehicleRoute copy = VehicleRoute.copyOf(route);
        for (InsertActivity activity : data.getUnmodifiableEventsByType(InsertActivity.class)) {
            insertNodeIntoExistingRoute(makeNodeId((JobActivity) activity.getActivity()), activity.getIndex(), data, copy);
            copy.getTourActivities().addActivity(activity.getIndex(), activity.getActivity());
        }
    }


    private void insertNodeIntoExistingRoute(String nodeId, int insertionIndex, InsertionData data, VehicleRoute route) {
        String fromNodeId;
        if (isFirst(insertionIndex)) {
            fromNodeId = makeStartId(data.getSelectedVehicle());
        } else {
            JobActivity jobActivity = (JobActivity) route.getActivities().get(insertionIndex - 1);
            fromNodeId = makeNodeId(jobActivity);
        }
        String firstEdgeId = makeEdgeId(fromNodeId, nodeId);
        String toNodeId;
        if (isLast(insertionIndex, route)) {
            toNodeId = makeEndId(data.getSelectedVehicle());
        } else {
            JobActivity jobActivity = (JobActivity) route.getActivities().get(insertionIndex);
            toNodeId = makeNodeId(jobActivity);
        }
        String secondEdgeId = makeEdgeId(nodeId, toNodeId);
        addEdge(firstEdgeId, fromNodeId, nodeId);
        if (!(isLast(insertionIndex, route) && !data.getSelectedVehicle().isReturnToDepot())) {
            addEdge(secondEdgeId, nodeId, toNodeId);
            if (!route.getActivities().isEmpty()) {
                removeEdge(makeEdgeId(fromNodeId, toNodeId));
            }
        }
    }

    private String makeEdgeId(String fromNodeId, String toNodeId) {
        return fromNodeId + "_" + toNodeId;
    }

    private void handleVehicleSwitch(InsertionData data, VehicleRoute route) {
        boolean vehicleSwitch = false;
        if (!(route.getVehicle() instanceof VehicleImpl.NoVehicle)) {
            if (!route.getVehicle().getId().equals(data.getSelectedVehicle().getId())) {
                vehicleSwitch = true;
            }
        }
        if (vehicleSwitch && !route.getActivities().isEmpty()) {
            String oldStartId = makeStartId(route.getVehicle());
            String firstActNodeId = makeNodeId((JobActivity) route.getActivities().get(0));
            String oldEndNodeId = makeEndId(route.getVehicle());
            String lastActNodeId = makeNodeId((JobActivity) route.getActivities().get(route.getActivities().size() - 1));
            removeEdge(makeEdgeId(oldStartId, firstActNodeId));
            if (route.getVehicle().isReturnToDepot()) {
                removeEdge(makeEdgeId(lastActNodeId, oldEndNodeId));
            }
            String newStartNodeId = makeStartId(data.getSelectedVehicle());
            String newEndNodeId = makeEndId(data.getSelectedVehicle());
            addEdge(makeEdgeId(newStartNodeId, firstActNodeId), newStartNodeId, firstActNodeId);
            if (data.getSelectedVehicle().isReturnToDepot()) {
                addEdge(makeEdgeId(lastActNodeId, newEndNodeId), lastActNodeId, newEndNodeId);
            }
        }
    }

    private void markInserted(Job job) {
        for (JobActivity act : job.getActivityList().getAll()) {
            setActTypeAttribute(graph.getNode(makeNodeId(act)), act);
        }
    }

    private void removeEdge(String edgeId) {
        markEdgeRemoved(edgeId);
        graph.removeEdge(edgeId);
    }

    private void markEdgeRemoved(String edgeId) {
        Edge edge = graph.getEdge(edgeId);
        if (edge == null) return;
        edge.addAttribute("ui.class", "removed");
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
        if (!record()) {
            return;
        }
        fileSink.stepBegins(graph.getId(), 0, RECREATE);
    }
}
