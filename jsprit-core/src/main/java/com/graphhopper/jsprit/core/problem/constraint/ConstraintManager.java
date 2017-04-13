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

import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Job;
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


    public static enum Priority {
        CRITICAL, HIGH, LOW
    }

    private static Logger log = LoggerFactory.getLogger(ConstraintManager.class);

    private HardActivityLevelConstraintManager actLevelConstraintManager = new HardActivityLevelConstraintManager();

    private HardRouteLevelConstraintManager hardRouteConstraintManager = new HardRouteLevelConstraintManager();

    private SoftActivityConstraintManager softActivityConstraintManager = new SoftActivityConstraintManager();

    private SoftRouteConstraintManager softRouteConstraintManager = new SoftRouteConstraintManager();

    private VehicleRoutingProblem vrp;

    private RouteAndActivityStateGetter stateManager;

    private boolean loadConstraintsSet = false;

    private boolean timeWindowConstraintsSet = false;

    private boolean skillconstraintSet = false;

    private final DependencyType[] dependencyTypes;

    public ConstraintManager(VehicleRoutingProblem vrp, RouteAndActivityStateGetter stateManager) {
        this.vrp = vrp;
        this.stateManager = stateManager;
        dependencyTypes = new DependencyType[vrp.getJobs().size() + 1];
    }

    public ConstraintManager(VehicleRoutingProblem vrp, RouteAndActivityStateGetter stateManager, Collection<Constraint> constraints) {
        this.vrp = vrp;
        this.stateManager = stateManager;
        dependencyTypes = new DependencyType[vrp.getJobs().size() + 1];
        resolveConstraints(constraints);
    }

    public Collection<HardRouteConstraint> getHardRouteConstraints() {
        return hardRouteConstraintManager.getConstraints();
    }

    public Collection<HardActivityConstraint> getCriticalHardActivityConstraints() {
        return actLevelConstraintManager.getCriticalConstraints();
    }

    public Collection<HardActivityConstraint> getHighPrioHardActivityConstraints() {
        return actLevelConstraintManager.getHighPrioConstraints();
    }

    public Collection<HardActivityConstraint> getLowPrioHardActivityConstraints() {
        return actLevelConstraintManager.getLowPrioConstraints();
    }
//    public Collection<HardActivityConstraint> getHardActivityConstraints() {
//        return actLevelConstraintManager.g;
//    }

    public DependencyType[] getDependencyTypes() {
        return dependencyTypes;
    }

    public void setDependencyType(String jobId, DependencyType dependencyType){
        Job job = vrp.getJobs().get(jobId);
        if(job != null) {
            dependencyTypes[job.getIndex()] = dependencyType;
        }
    }

    public DependencyType getDependencyType(String jobId){
        Job job = vrp.getJobs().get(jobId);
        if(job != null){
            return dependencyTypes[job.getIndex()];
        }
        return DependencyType.NO_TYPE;
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
                log.warn("constraint " + c + " unknown thus ignores the constraint. currently, a constraint must implement either HardActivityStateLevelConstraint or HardRouteStateLevelConstraint");
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

//	public void add

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
        return actLevelConstraintManager.fulfilled(iFacts, prevAct, newAct, nextAct, prevActDepTime);
    }

    public Collection<Constraint> getConstraints() {
        List<Constraint> constraints = new ArrayList<Constraint>();
        constraints.addAll(actLevelConstraintManager.getAllConstraints());
        constraints.addAll(hardRouteConstraintManager.getConstraints());
        constraints.addAll(softActivityConstraintManager.getConstraints());
        constraints.addAll(softRouteConstraintManager.getConstraints());
        return Collections.unmodifiableCollection(constraints);
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
