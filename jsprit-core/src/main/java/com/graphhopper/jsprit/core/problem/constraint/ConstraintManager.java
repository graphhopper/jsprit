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
package com.graphhopper.jsprit.core.problem.constraint;

import com.graphhopper.jsprit.core.algorithm.recreate.InsertionCostBreakdown;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.misc.JobInsertionContext;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.problem.solution.route.state.RouteAndActivityStateGetter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Manager that manage hard- and soft constraints, both on route and activity level.
 *
 * @author schroeder
 */
public class ConstraintManager implements HardActivityConstraint, HardRouteConstraint, SoftActivityConstraint, SoftRouteConstraint {


    /**
     * The tier of a hard activity constraint. A position is checked tier by tier, CRITICAL first, and the first tier
     * with a failure decides:
     * <ul>
     * <li>in CRITICAL and HIGH every constraint of the tier is asked (so all failures are known for the unassigned
     * reasons), unless one returns NOT_FULFILLED_BREAK, which ends the check at once; the tier fails with NOT_FULFILLED
     * if any constraint returned it,</li>
     * <li>LOW stops at the first failing constraint,</li>
     * <li>a later tier is only reached when the earlier ones passed.</li>
     * </ul>
     * Within a tier the constraints run in registration order; expensive checks therefore belong behind cheap ones and
     * in a later tier than constraints that fail often. The recorded failures (see
     * {@link #fulfilled(JobInsertionContext, TourActivity, TourActivity, TourActivity, double, Collection, List)}) follow
     * this protocol: a NOT_FULFILLED_BREAK is recorded alone, even if constraints before it in the same tier failed.
     */
    public enum Priority {
        CRITICAL, HIGH, LOW
    }

    private static final Logger log = LoggerFactory.getLogger(ConstraintManager.class);

    private HardActivityLevelConstraintManager actLevelConstraintManager = new HardActivityLevelConstraintManager();

    private HardRouteLevelConstraintManager hardRouteConstraintManager = new HardRouteLevelConstraintManager();

    private SoftActivityConstraintManager softActivityConstraintManager = new SoftActivityConstraintManager();

    private SoftRouteConstraintManager softRouteConstraintManager = new SoftRouteConstraintManager();

    private VehicleRoutingProblem vrp;

    private RouteAndActivityStateGetter stateManager;

    private boolean loadConstraintsSet = false;

    private boolean timeWindowConstraintsSet = false;

    private boolean skillconstraintSet = false;

    private final JobDependencies jobDependencies;

    public ConstraintManager(VehicleRoutingProblem vrp, RouteAndActivityStateGetter stateManager) {
        this.vrp = vrp;
        this.stateManager = stateManager;
        jobDependencies = new JobDependencies(vrp);
    }

    public ConstraintManager(VehicleRoutingProblem vrp, RouteAndActivityStateGetter stateManager, Collection<Constraint> constraints) {
        this.vrp = vrp;
        this.stateManager = stateManager;
        jobDependencies = new JobDependencies(vrp);
        resolveConstraints(constraints);
    }

    public boolean hasHardActivityConstraints() {
        return actLevelConstraintManager.hasHardActivityConstraints();
    }

    public List<HardRouteConstraint> getHardRouteConstraints() {
        return hardRouteConstraintManager.getConstraints();
    }

    public List<HardActivityConstraint> getCriticalHardActivityConstraints() {
        return actLevelConstraintManager.getCriticalConstraints();
    }

    public List<HardActivityConstraint> getHighPrioHardActivityConstraints() {
        return actLevelConstraintManager.getHighPrioConstraints();
    }

    public List<HardActivityConstraint> getLowPrioHardActivityConstraints() {
        return actLevelConstraintManager.getLowPrioConstraints();
    }

    public DependencyType[] getDependencyTypes() {
        return jobDependencies.getDependencyTypes();
    }

    public void setDependencyType(String jobId, DependencyType dependencyType) {
        jobDependencies.setDependencyType(jobId, dependencyType);
    }

    public DependencyType getDependencyType(String jobId) {
        return jobDependencies.getDependencyType(jobId);
    }

    private void resolveConstraints(Collection<Constraint> constraints) {
        for (Constraint c : constraints) {
            boolean constraintTypeKnown = false;
            if (c instanceof HardActivityConstraint) {
                actLevelConstraintManager.addConstraint((HardActivityConstraint) c, Priority.HIGH);
                constraintTypeKnown = true;
            }
            if (c instanceof HardRouteConstraint) {
                hardRouteConstraintManager.addConstraint((HardRouteConstraint) c);
                constraintTypeKnown = true;
            }
            if (c instanceof SoftRouteConstraint) {
                softRouteConstraintManager.addConstraint((SoftRouteConstraint) c);
                constraintTypeKnown = true;
            }
            if (c instanceof SoftActivityConstraint) {
                softActivityConstraintManager.addConstraint((SoftActivityConstraint) c);
                constraintTypeKnown = true;
            }
            if (!constraintTypeKnown) {
                log.warn("constraint {} implements none of HardActivityConstraint, HardRouteConstraint, SoftActivityConstraint, SoftRouteConstraint and is ignored", c);
            }
        }

    }

    public void addTimeWindowConstraint() {
        if (!timeWindowConstraintsSet) {
            addConstraint(new VehicleDependentTimeWindowConstraints(stateManager, vrp.getTransportCosts(), vrp.getActivityCosts()), Priority.HIGH);
            timeWindowConstraintsSet = true;
        }
    }


    public void addLoadConstraint() {
        if (!loadConstraintsSet) {
            addConstraint(new PickupAndDeliverShipmentLoadActivityLevelConstraint(stateManager), Priority.CRITICAL);
            addConstraint(new ServiceLoadRouteLevelConstraint(stateManager));
            addConstraint(new ServiceLoadActivityLevelConstraint(stateManager), Priority.LOW);
            loadConstraintsSet = true;
        }
    }

    public void addSkillsConstraint() {
        if (!skillconstraintSet) {
            addConstraint(new HardSkillConstraint(stateManager));
            skillconstraintSet = true;
        }
    }

    public void addConstraint(HardActivityConstraint actLevelConstraint, Priority priority) {
        actLevelConstraintManager.addConstraint(actLevelConstraint, priority);
    }

    public void addConstraint(HardRouteConstraint routeLevelConstraint) {
        hardRouteConstraintManager.addConstraint(routeLevelConstraint);
    }

    public void addConstraint(SoftActivityConstraint softActivityConstraint) {
        softActivityConstraintManager.addConstraint(softActivityConstraint);
    }

    public void addConstraint(SoftRouteConstraint softRouteConstraint) {
        softRouteConstraintManager.addConstraint(softRouteConstraint);
    }

    @Override
    public boolean fulfilled(JobInsertionContext insertionContext) {
        return hardRouteConstraintManager.fulfilled(insertionContext);
    }

    @Override
    public ConstraintsStatus fulfilled(JobInsertionContext iFacts, TourActivity prevAct, TourActivity newAct, TourActivity nextAct, double prevActDepTime) {
        return actLevelConstraintManager.fulfilled(iFacts, prevAct, newAct, nextAct, prevActDepTime, null, null);
    }

    /**
     * Like {@link #fulfilled(JobInsertionContext, TourActivity, TourActivity, TourActivity, double)}, and adds the
     * constraints that made the position infeasible to {@code failedConstraints} (see {@link Priority} for which ones
     * these are). {@code failedInTier} is a scratch list the caller keeps across positions so that no list is allocated
     * per position; it may be null.
     */
    public ConstraintsStatus fulfilled(JobInsertionContext iFacts, TourActivity prevAct, TourActivity newAct, TourActivity nextAct, double prevActDepTime,
                                       Collection<HardConstraint> failedConstraints, List<HardConstraint> failedInTier) {
        return actLevelConstraintManager.fulfilled(iFacts, prevAct, newAct, nextAct, prevActDepTime, failedConstraints, failedInTier);
    }

    public Collection<Constraint> getConstraints() {
        List<Constraint> constraints = new ArrayList<Constraint>();
        constraints.addAll(actLevelConstraintManager.getAllConstraints());
        constraints.addAll(hardRouteConstraintManager.getConstraints());
        constraints.addAll(softActivityConstraintManager.getConstraints());
        constraints.addAll(softRouteConstraintManager.getConstraints());
        return Collections.unmodifiableCollection(constraints);
    }

    /**
     * Get all registered soft route constraints.
     * These influence insertion decisions at the route level (e.g., which route to insert into).
     */
    public Collection<SoftRouteConstraint> getSoftRouteConstraints() {
        return softRouteConstraintManager.getConstraints();
    }

    /**
     * Get all registered soft activity constraints.
     * These influence insertion decisions at the activity level (e.g., where in the route to insert).
     */
    public Collection<SoftActivityConstraint> getSoftActivityConstraints() {
        return softActivityConstraintManager.getConstraints();
    }

    /**
     * Get route-level soft constraint costs with breakdown by constraint.
     *
     * @param context The job insertion context
     * @return Breakdown of costs per constraint
     */
    public InsertionCostBreakdown getRouteCostsBreakdown(JobInsertionContext context) {
        return softRouteConstraintManager.getCostsBreakdown(context);
    }

    /**
     * A scratch array for {@link #getActivityCosts(JobInsertionContext, TourActivity, TourActivity, TourActivity, double, double[])}:
     * one slot per soft activity constraint. Allocate it once per insertion evaluation (per job and route), not per position;
     * the calculators are shared between threads, so it must not be a field of theirs.
     */
    public double[] newActivityCostsScratch() {
        return new double[softActivityConstraintManager.size()];
    }

    /**
     * Soft activity costs of one position, evaluated exactly once per constraint, with the single values recorded in
     * {@code scratch} so that {@link #activityBreakdownOf(double[])} can build the breakdown later for the position that
     * wins, instead of building a map with concatenated names for every feasible position.
     */
    public double getActivityCosts(JobInsertionContext context, TourActivity prevAct, TourActivity newAct, TourActivity nextAct, double depTime, double[] scratch) {
        return softActivityConstraintManager.getCosts(context, prevAct, newAct, nextAct, depTime, scratch);
    }

    public InsertionCostBreakdown activityBreakdownOf(double[] scratch) {
        return softActivityConstraintManager.breakdownOf(scratch);
    }

    @Override
    public double getCosts(JobInsertionContext insertionContext) {
        return softRouteConstraintManager.getCosts(insertionContext);
    }

    @Override
    public double getCosts(JobInsertionContext iFacts, TourActivity prevAct, TourActivity newAct, TourActivity nextAct, double prevActDepTime) {
        return softActivityConstraintManager.getCosts(iFacts, prevAct, newAct, nextAct, prevActDepTime);
    }


}
