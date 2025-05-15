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
package com.graphhopper.jsprit.core.reporting;

import com.graphhopper.jsprit.core.problem.Capacity;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.DeliveryActivity;
import com.graphhopper.jsprit.core.problem.solution.route.activity.PickupActivity;
import com.graphhopper.jsprit.core.problem.solution.route.activity.ServiceActivity;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity.JobActivity;
import com.graphhopper.jsprit.core.util.VehicleIndexComparator;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Enhanced printer to print the details of a vehicle-routing-problem solution.
 * <p>
 * Supports all job types including EnRoutePickup and EnRouteDelivery.
 *
 * @author stefan schroeder
 * @author [your name]
 */
public class SolutionPrinter {

    // Wrapping System.out into a PrintWriter
    private static final PrintWriter SYSTEM_OUT_AS_PRINT_WRITER = new PrintWriter(System.out);

    /**
     * Enum to indicate verbose-level.
     */
    public enum Print {
        CONCISE,       // Basic information
        VERBOSE,       // Detailed information
        DETAILED       // Most comprehensive output including capacity tracking
    }

    /**
     * Class to store job counts by type
     */
    private static class JobCounts {
        int nServices;
        int nShipments;
        int nBreaks;
        int nEnRoutePickups;
        int nEnRouteDeliveries;
        int nOthers;

        public JobCounts() {
            this.nServices = 0;
            this.nShipments = 0;
            this.nBreaks = 0;
            this.nEnRoutePickups = 0;
            this.nEnRouteDeliveries = 0;
            this.nOthers = 0;
        }

        public int getTotal() {
            return nServices + nShipments + nBreaks + nEnRoutePickups + nEnRouteDeliveries + nOthers;
        }
    }

    /**
     * Prints costs and #vehicles to stdout.
     *
     * @param solution the solution to be printed
     */
    public static void print(VehicleRoutingProblemSolution solution) {
        print(SYSTEM_OUT_AS_PRINT_WRITER, solution);
        SYSTEM_OUT_AS_PRINT_WRITER.flush();
    }

    /**
     * Prints costs and #vehicles to the given writer
     *
     * @param out      the destination writer
     * @param solution the solution to be printed
     */
    public static void print(PrintWriter out, VehicleRoutingProblemSolution solution) {
        out.println("[costs=" + solution.getCost() + "]");
        out.println("[#vehicles=" + solution.getRoutes().size() + "]");
    }

    /**
     * Prints solution details to stdout.
     *
     * @param problem  the routing problem
     * @param solution the solution to be printed
     * @param print    the verbosity level
     */
    public static void print(VehicleRoutingProblem problem, VehicleRoutingProblemSolution solution, Print print) {
        print(SYSTEM_OUT_AS_PRINT_WRITER, problem, solution, print);
        SYSTEM_OUT_AS_PRINT_WRITER.flush();
    }

    /**
     * Prints solution details to the given writer.
     *
     * @param out      the destination writer
     * @param problem  the routing problem
     * @param solution the solution to be printed
     * @param print    the verbosity level
     */
    public static void print(PrintWriter out, VehicleRoutingProblem problem, VehicleRoutingProblemSolution solution, Print print) {
        // Print problem summary
        printProblemSummary(out, problem);

        // Print solution summary
        printSolutionSummary(out, solution);

        // Print detailed information if requested
        if (print == Print.VERBOSE || print == Print.DETAILED) {
            printRouteDetails(out, problem, solution, print == Print.DETAILED);
        }
    }

    /**
     * Prints a summary of the problem.
     *
     * @param out     the destination writer
     * @param problem the problem to summarize
     */
    private static void printProblemSummary(PrintWriter out, VehicleRoutingProblem problem) {
        String leftAlign = "| %-20s | %-15s |%n";

        out.format("+--------------------------------------+%n");
        out.printf("| PROBLEM SUMMARY                      |%n");
        out.format("+----------------------+---------------+%n");
        out.printf("| Metric               | Value         |%n");
        out.format("+----------------------+---------------+%n");

        JobCounts jobs = countJobsByType(problem);
        out.format(leftAlign, "Total Jobs", jobs.getTotal());
        out.format(leftAlign, "Services", jobs.nServices);
        out.format(leftAlign, "Shipments", jobs.nShipments);
        out.format(leftAlign, "Breaks", jobs.nBreaks);
        out.format(leftAlign, "EnRoutePickups", jobs.nEnRoutePickups);
        out.format(leftAlign, "EnRouteDeliveries", jobs.nEnRouteDeliveries);
        if (jobs.nOthers > 0) {
            out.format(leftAlign, "Other Job Types", jobs.nOthers);
        }
        out.format(leftAlign, "Fleet Size", problem.getFleetSize().toString());
        out.format(leftAlign, "Vehicle Types", problem.getTypes().size());
        out.format("+--------------------------------------+%n");
        out.println();
    }

    /**
     * Prints a summary of the solution.
     *
     * @param out      the destination writer
     * @param solution the solution to summarize
     */
    private static void printSolutionSummary(PrintWriter out, VehicleRoutingProblemSolution solution) {
        String leftAlign = "| %-20s | %-30s |%n";

        out.format("+------------------------------------------------------+%n");
        out.printf("| SOLUTION SUMMARY                                      |%n");
        out.format("+----------------------+--------------------------------+%n");
        out.printf("| Metric               | Value                          |%n");
        out.format("+----------------------+--------------------------------+%n");
        out.format(leftAlign, "Total Cost", String.format("%.2f", solution.getCost()));
        out.format(leftAlign, "Vehicles Used", solution.getRoutes().size());
        out.format(leftAlign, "Unassigned Jobs", solution.getUnassignedJobs().size());

        // Count activities by type
        Map<String, Integer> activityCounts = countActivitiesByType(solution);
        for (Map.Entry<String, Integer> entry : activityCounts.entrySet()) {
            out.format(leftAlign, entry.getKey(), entry.getValue());
        }

        out.format("+------------------------------------------------------+%n");
        out.println();
    }

    /**
     * Prints detailed information about each route.
     *
     * @param out           the destination writer
     * @param problem       the routing problem
     * @param solution      the solution to be printed
     * @param trackCapacity whether to track and show capacity changes
     */
    private static void printRouteDetails(PrintWriter out, VehicleRoutingProblem problem,
                                          VehicleRoutingProblemSolution solution, boolean trackCapacity) {
        // Define table format based on whether we're tracking capacity
        String header;
        String headerSeparator;
        String rowFormat;

        if (trackCapacity) {
            header = "| %-5s | %-15s | %-20s | %-20s | %-8s | %-8s | %-10s | %-25s |%n";
            headerSeparator = "+-------+-----------------+----------------------+----------------------+----------+----------+------------+---------------------------+%n";
            rowFormat = "| %-5s | %-15s | %-20s | %-20s | %-8s | %-8s | %-10.2f | %-25s |%n";

            out.format(headerSeparator);
            out.printf("| ROUTE DETAILS (With Capacity Tracking)                                                                                       |%n");
            out.format(headerSeparator);
            out.printf(header, "Route", "Vehicle", "Activity", "Job ID", "ArrTime", "EndTime", "Cost", "Load");
        } else {
            header = "| %-5s | %-15s | %-20s | %-20s | %-8s | %-8s | %-10s |%n";
            headerSeparator = "+-------+-----------------+----------------------+----------------------+----------+----------+------------+%n";
            rowFormat = "| %-5s | %-15s | %-20s | %-20s | %-8s | %-8s | %-10.2f |%n";

            out.format(headerSeparator);
            out.printf("| ROUTE DETAILS                                                                |%n");
            out.format(headerSeparator);
            out.printf(header, "Route", "Vehicle", "Activity", "Job ID", "ArrTime", "EndTime", "Cost");
        }

        out.format(headerSeparator);

        // Sort routes for consistent output
        List<VehicleRoute> sortedRoutes = new ArrayList<>(solution.getRoutes());
        sortedRoutes.sort(new VehicleIndexComparator());

        int routeIdx = 1;
        for (VehicleRoute route : sortedRoutes) {
            // Initialize tracking variables
            double routeCost = 0;
            TourActivity prevAct = route.getStart();

            // Initialize capacity tracking
            Capacity currentCapacity = getLoadAtBeginning(route); // Start with zero capacity

            // Print route start
            if (trackCapacity) {
                out.printf(rowFormat, routeIdx, route.getVehicle().getId(),
                    route.getStart().getName(), "-",
                    "START", formatTime(route.getStart().getEndTime()),
                    routeCost, formatCapacity(currentCapacity));
            } else {
                out.printf(rowFormat, routeIdx, route.getVehicle().getId(),
                    route.getStart().getName(), "-",
                    "START", formatTime(route.getStart().getEndTime()),
                    routeCost);
            }

            // Print each activity
            for (TourActivity act : route.getActivities()) {
                // Get job ID
                String jobId = getJobId(act);

                // Calculate cost of this activity
                double actCost = calculateActivityCost(problem, prevAct, act, route);
                routeCost += actCost;

                // Update capacity if tracking
                if (trackCapacity) {
                    currentCapacity = updateCapacity(currentCapacity, act);

                    out.printf(rowFormat, routeIdx, route.getVehicle().getId(),
                        act.getName(), jobId,
                        formatTime(act.getArrTime()), formatTime(act.getEndTime()),
                        routeCost, formatCapacity(currentCapacity));
                } else {
                    out.printf(rowFormat, routeIdx, route.getVehicle().getId(),
                        act.getName(), jobId,
                        formatTime(act.getArrTime()), formatTime(act.getEndTime()),
                        routeCost);
                }

                prevAct = act;
            }

            // Print route end
            double endCost = calculateActivityCost(problem, prevAct, route.getEnd(), route);
            routeCost += endCost;

            if (trackCapacity) {
                out.printf(rowFormat, routeIdx, route.getVehicle().getId(),
                    route.getEnd().getName(), "-",
                    formatTime(route.getEnd().getArrTime()), "END",
                    routeCost, formatCapacity(currentCapacity));
            } else {
                out.printf(rowFormat, routeIdx, route.getVehicle().getId(),
                    route.getEnd().getName(), "-",
                    formatTime(route.getEnd().getArrTime()), "END",
                    routeCost);
            }

            out.format(headerSeparator);
            routeIdx++;
        }

        // Print unassigned jobs
        printUnassignedJobs(out, solution);
    }

    private static Capacity getLoadAtBeginning(VehicleRoute route) {
        Capacity current = Capacity.Builder.newInstance().build();
        for (Job j : route.getTourActivities().getJobs()) {
            if (j.isPickedUpAtVehicleStart()) {
                current = Capacity.addup(current, j.getSize());
            }
        }
        return current;
    }

    /**
     * Prints unassigned jobs if any exist.
     *
     * @param out      the destination writer
     * @param solution the solution containing unassigned jobs
     */
    private static void printUnassignedJobs(PrintWriter out, VehicleRoutingProblemSolution solution) {
        if (!solution.getUnassignedJobs().isEmpty()) {
            out.format("+------------------+----------------------+%n");
            out.format("| UNASSIGNED JOBS                         |%n");
            out.format("+------------------+----------------------+%n");
            out.format("| %-16s | %-20s |%n", "Job ID", "Job Type");
            out.format("+------------------+----------------------+%n");

            for (Job job : solution.getUnassignedJobs()) {
                out.format("| %-16s | %-20s |%n", job.getId(), getJobTypeName(job));
            }

            out.format("+------------------+----------------------+%n");
        }
    }

    /**
     * Counts activities by type in the given solution.
     *
     * @param solution the solution to analyze
     * @return a map of activity types to counts
     */
    private static Map<String, Integer> countActivitiesByType(VehicleRoutingProblemSolution solution) {
        Map<String, Integer> counts = new TreeMap<>();  // TreeMap for sorted keys

        for (VehicleRoute route : solution.getRoutes()) {
            for (TourActivity act : route.getActivities()) {
                String actType = act.getName();
                counts.put(actType, counts.getOrDefault(actType, 0) + 1);
            }
        }

        return counts;
    }

    /**
     * Counts jobs by type in the given problem.
     *
     * @param problem the problem to analyze
     * @return job counts by type
     */
    private static JobCounts countJobsByType(VehicleRoutingProblem problem) {
        JobCounts counts = new JobCounts();

        for (Job job : problem.getJobs().values()) {
            if (job.getJobType().isService()) {
                counts.nServices++;
            } else if (job.getJobType().isShipment()) {
                counts.nShipments++;
            } else if (job.getJobType().isBreak()) {
                counts.nBreaks++;
            } else {
                // Check for EnRoutePickup and EnRouteDelivery using class name
                // This approach avoids direct dependencies on these classes if not in classpath
                String jobClassName = job.getClass().getSimpleName();
                if ("EnRoutePickup".equals(jobClassName)) {
                    counts.nEnRoutePickups++;
                } else if ("EnRouteDelivery".equals(jobClassName)) {
                    counts.nEnRouteDeliveries++;
                } else {
                    counts.nOthers++;
                }
            }
        }

        return counts;
    }

    /**
     * Gets the job ID from an activity.
     *
     * @param act the activity
     * @return the job ID or "-" if no job is associated
     */
    private static String getJobId(TourActivity act) {
        if (act instanceof JobActivity) {
            return ((JobActivity) act).getJob().getId();
        }
        return "-";
    }

    /**
     * Gets a descriptive name for the job type.
     *
     * @param job the job
     * @return a descriptive name for the job type
     */
    private static String getJobTypeName(Job job) {
        if (job.getJobType().isService()) {
            return "Service";
        } else if (job.getJobType().isShipment()) {
            return "Shipment";
        } else if (job.getJobType().isBreak()) {
            return "Break";
        } else {
            // Check for EnRoutePickup and EnRouteDelivery using class name
            String jobClassName = job.getClass().getSimpleName();
            if ("EnRoutePickup".equals(jobClassName)) {
                return "EnRoutePickup";
            } else if ("EnRouteDelivery".equals(jobClassName)) {
                return "EnRouteDelivery";
            } else {
                return jobClassName;
            }
        }
    }

    /**
     * Calculates the cost of an activity.
     *
     * @param problem the routing problem
     * @param prevAct the previous activity
     * @param act     the current activity
     * @param route   the route
     * @return the cost of the activity
     */
    private static double calculateActivityCost(VehicleRoutingProblem problem, TourActivity prevAct,
                                                TourActivity act, VehicleRoute route) {
        double transportCost = problem.getTransportCosts().getTransportCost(
            prevAct.getLocation(), act.getLocation(), prevAct.getEndTime(),
            route.getDriver(), route.getVehicle());

        double activityCost = problem.getActivityCosts().getActivityCost(
            act, act.getArrTime(), route.getDriver(), route.getVehicle());

        return transportCost + activityCost;
    }

    /**
     * Updates capacity tracking based on activity type.
     *
     * @param currentCapacity the current capacity state
     * @param act             the activity to process
     * @return the updated capacity
     */
    private static Capacity updateCapacity(Capacity currentCapacity, TourActivity act) {
        if (!(act instanceof JobActivity)) {
            return currentCapacity;
        }

        Job job = ((JobActivity) act).getJob();
        Capacity jobCapacity = job.getSize();
        Capacity newCapacity = Capacity.copyOf(currentCapacity);

        String actName = act.getName().toLowerCase();
        String actClassName = act.getClass().getSimpleName();

        // Handle different activity types for capacity tracking
        if (act instanceof PickupActivity || actClassName.contains("EnRoutePickupActivity")) {
            // Pickup activities increase capacity
            newCapacity = Capacity.addup(newCapacity, jobCapacity);
        } else if (act instanceof DeliveryActivity) {
            // Standard delivery decreases capacity
            newCapacity = Capacity.subtract(newCapacity, jobCapacity);
        } else if (actClassName.contains("EnRouteDeliveryActivity")) {
            // EnRouteDelivery adds its capacity (which is typically negative)
            newCapacity = Capacity.addup(newCapacity, jobCapacity);
        } else if (act instanceof ServiceActivity) {
            // Service is typically pickup, so add capacity
            newCapacity = Capacity.addup(newCapacity, jobCapacity);
        }

        return newCapacity;
    }

    /**
     * Formats capacity to a readable string.
     *
     * @param capacity the capacity object
     * @return formatted capacity string
     */
    private static String formatCapacity(Capacity capacity) {
        if (capacity == null || capacity.isZero()) {
            return "[0]";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < capacity.getNuOfDimensions(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(capacity.get(i));
        }

        return sb.append("]").toString();
    }

    /**
     * Formats a time value to a readable string.
     *
     * @param time the time value
     * @return formatted time string
     */
    private static String formatTime(double time) {
        if (time == Double.MAX_VALUE || time == -Double.MAX_VALUE) {
            return "-";
        }
        return String.format("%.0f", time);
    }
}
