package com.graphhopper.jsprit.core.problem.job;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.graphhopper.jsprit.core.problem.solution.route.activity.JobActivity;

/**
 * DAG (Directed Acyclic Graph) based activity list implementation.
 * <p>
 * The inserted activities and their relations create the dependency graph.
 * </p>
 *
 * @author balage
 *
 */
public class GraphJobActivityList extends AbstractListBackedJobActivityList {

    // Directly added dependencies
    protected Map<JobActivity, Set<JobActivity>> dependencies = new HashMap<>();

    // Cached transitive dependencies
    protected Map<JobActivity, Set<JobActivity>> transitivePrecedingDependencyCache = new HashMap<>();
    protected Map<JobActivity, Set<JobActivity>> transitiveSubsequentDependencyCache = new HashMap<>();

    public GraphJobActivityList(AbstractJob job) {
        super(job);
    }

    @Override
    public void addActivity(JobActivity activity) {
        super.addActivity(activity);
        dependencies.put(activity, new HashSet<JobActivity>());
        transitivePrecedingDependencyCache.put(activity, new HashSet<JobActivity>());
        transitiveSubsequentDependencyCache.put(activity, new HashSet<JobActivity>());
    }

    /**
     * Adds a dependency between two activities. If the activities not in the list, they are also added.
     *
     * @param priorActivity
     *            The prior activity.
     * @param subsequentActivity
     *            The subsequent activity.
     * @throws IllegalArgumentException
     *             If the activities can't be added (see {@linkplain #addActivity(JobActivity)}) or if the new
     *             dependency would create a cycle in the dependency graph.
     */
    public void addDependency(JobActivity priorActivity, JobActivity subsequentActivity) {
        // Add activities if not added yet
        if (!_activities.contains(priorActivity)) {
            addActivity(priorActivity);
        }
        if (!_activities.contains(subsequentActivity)) {
            addActivity(subsequentActivity);
        }
        // Check if dependency already in there
        if (dependencies.get(priorActivity).contains(subsequentActivity)) {
            return;
        }

        // Check if the new dependency would create a cycle
        if (transitiveSubsequentDependencyCache.get(subsequentActivity).contains(priorActivity)) {
            throw new IllegalArgumentException("Dependency between '" + priorActivity + "' and '" + subsequentActivity + "' would create a cycle.");
        }

        // Add new dependency
        dependencies.get(priorActivity).add(subsequentActivity);

        // Update cache
        // === Subsequent =======
        // The new subsequent abilities are the subsequent and its subsesequent abilities
        Set<JobActivity> subsequentActivitiesToAdd = new HashSet<>(transitiveSubsequentDependencyCache.get(subsequentActivity));
        subsequentActivitiesToAdd.add(subsequentActivity);
        // The abilities to add the new ones to: the prior and its prior abilities
        Set<JobActivity> subsequentActivitiesToUpdate = new HashSet<>(transitivePrecedingDependencyCache.get(priorActivity));
        subsequentActivitiesToUpdate.add(priorActivity);

        // === Preceding =======
        // The new prior abilities are the prior and its trasitive prior abilities
        Set<JobActivity> priorActivitiesToAdd = new HashSet<>(transitivePrecedingDependencyCache.get(priorActivity));
        priorActivitiesToAdd.add(priorActivity);
        // The abilities to add the new ones to: the subsequent and its transitively subsequent abilities
        Set<JobActivity> priorActivitiesToUpdate = new HashSet<>(transitiveSubsequentDependencyCache.get(subsequentActivity));
        priorActivitiesToUpdate.add(subsequentActivity);

        // Do the updates
        subsequentActivitiesToUpdate.forEach(a -> transitiveSubsequentDependencyCache.get(a).addAll(subsequentActivitiesToAdd));
        priorActivitiesToUpdate.forEach(a -> transitivePrecedingDependencyCache.get(a).addAll(priorActivitiesToAdd));
    }


    @Override
    public Set<JobActivity> getPreceding(JobActivity activity) {
        if (!_activities.contains(activity)) {
            throw new IllegalArgumentException("Activity '" + activity + "' is not in the list.");
        }

        return Collections.unmodifiableSet(transitivePrecedingDependencyCache.get(activity));
    }

    @Override
    public Set<JobActivity> getSubsequent(JobActivity activity) {
        if (!_activities.contains(activity)) {
            throw new IllegalArgumentException("Activity '" + activity + "' is not in the list.");
        }

        return Collections.unmodifiableSet(transitiveSubsequentDependencyCache.get(activity));
    }

    /**
     * Just for presentation purposes. It is too verbose for toString.
     */
    public void printDetailed() {
        StringBuilder sb = new StringBuilder();
        sb.append("------------------------------\n");
        sb.append("DIRECT DEPENDENCIES\n");
        sb.append(dependencies.entrySet().stream()
                .flatMap(en -> en.getValue().stream().map(sa -> en.getKey().getName() + " -> " + sa.getName()))
                .sorted()
                .collect(Collectors.joining("\n")));
        sb.append("\nTRANSITIVE PRECEDING DEPENDENCIES\n");
        sb.append(transitivePrecedingDependencyCache.entrySet().stream()
                .sorted((e1, e2) -> e1.getKey().getName().compareTo(e2.getKey().getName()))
                .map(en -> en.getKey().getName() + ": " + en.getValue().stream()
                        .map(pa -> pa.getName())
                        .collect(Collectors.joining(", ")))
                .collect(Collectors.joining("\n")));
        sb.append("\nTRANSITIVE SUBSEQUENT DEPENDENCIES\n");
        sb.append(transitiveSubsequentDependencyCache.entrySet().stream()
                .sorted((e1, e2) -> e1.getKey().getName().compareTo(e2.getKey().getName()))
                .map(en -> en.getKey().getName() + ": " + en.getValue().stream()
                        .map(pa -> pa.getName())
                        .sorted()
                        .collect(Collectors.joining(", ")))
                .collect(Collectors.joining("\n")));
        sb.append("\nTOPOLOGICAL ORDERINGS\n");
        sb.append(getPossibleOrderings().stream()
                .sorted((l1, l2) -> {
                    for (int i = 0; i < l1.size(); i++) {
                        if (l1.get(i).equals(l2.get(i))) {
                            continue;
                        }
                        return l1.get(i).getName().compareTo(l2.get(i).getName());
                    }
                    return 0;
                })
                .map(e -> e.stream().map(a -> a.getName()).collect(Collectors.joining(", ")))
                .collect(Collectors.joining("\n")));

        System.out.println(sb.toString());
    }

    @Override
    public Set<List<JobActivity>> getPossibleOrderings() {
        Set<List<JobActivity>> orderings = new HashSet<>();
        boolean visited[] = new boolean[_activities.size()];
        Deque<JobActivity> partialOrder = new ArrayDeque<>();
        int indegree[] = new int[_activities.size()];
        for (int i = 0; i < _activities.size(); i++) {
            JobActivity act = _activities.get(i);
            indegree[i] = (int) dependencies.entrySet().stream()
                    .flatMap(en -> en.getValue().stream())
                    .filter(a -> a.equals(act))
                    .count();
        }
        allTopologicalSort(orderings, partialOrder, visited, indegree);
        return orderings;
    }

    /**
     * Recursive function for collection all possible topological orderings.
     *
     * <p>
     * <i>Migrated from the original C++ source of
     * <a href="http://www.geeksforgeeks.org/all-topological-sorts-of-a-directed-acyclic-graph/">Utkarsh Trivedi</a>
     * .</i>
     * </p>
     *
     * @param orderings
     *            The list of found orderings.
     * @param partialOrder
     *            The partial ordering under construction.
     * @param visited
     *            Markers on the already visited nodes.
     * @param indegree
     *            Dependency level of the nodes.
     */
    private void allTopologicalSort(Set<List<JobActivity>> orderings, Deque<JobActivity> partialOrder, boolean[] visited, int[] indegree) {
        boolean flag = false;
        for (int i = 0; i < _activities.size(); i++) {
            JobActivity act = _activities.get(i);
            if (indegree[i] == 0 && !visited[i]) {
                dependencies.get(act).forEach(ra -> indegree[indexOf(ra)]--);
                partialOrder.addLast(act);
                visited[i] = true;
                allTopologicalSort(orderings, partialOrder, visited, indegree);

                visited[i] = false;
                partialOrder.removeLast();
                dependencies.get(act).forEach(ra -> indegree[indexOf(ra)]++);
                flag = true;
            }
        }

        if (!flag) {
            orderings.add(new ArrayList<>(partialOrder));
        }
    }

}
