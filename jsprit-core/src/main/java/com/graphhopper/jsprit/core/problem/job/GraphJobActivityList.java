package com.graphhopper.jsprit.core.problem.job;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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
public class GraphJobActivityList extends SequentialJobActivityList {

    protected Map<JobActivity, Set<JobActivity>> dependencies = new HashMap<>();

    protected Map<JobActivity, Set<JobActivity>> transitivePrecedingDependencyCache = new HashMap<>();
    protected Map<JobActivity, Set<JobActivity>> transitiveSubsequentDependencyCache = new HashMap<>();

    public GraphJobActivityList(AbstractJob job) {
        super(job);
    }

    @Override
    public void addActivity(JobActivity activity) {
        validateActivity(activity);
        if (_activities.contains(activity)) {
            return;
        }
        _activities.add(activity);
        dependencies.put(activity, new HashSet<JobActivity>());
        transitivePrecedingDependencyCache.put(activity, new HashSet<JobActivity>());
        transitiveSubsequentDependencyCache.put(activity, new HashSet<JobActivity>());
    }

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
            throw new IllegalArgumentException("Dependency between '"+priorActivity+"' and '"+subsequentActivity+"' would create a cycle.");
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

    public void printDetailed() {
        StringBuilder sb = new StringBuilder();
        sb.append("DIRECT DEPENDENCIES\n");
        sb.append(dependencies.entrySet().stream()
                .flatMap(en -> en.getValue().stream().map(sa -> en.getKey().getName() + " -> " + sa.getName()))
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

        System.out.println(sb.toString());
    }




}
