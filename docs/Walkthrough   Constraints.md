It is assumed that you know the basics of the [applied algorithm](Meta-Heuristic.md). In the recreation-step each
removed job is re-inserted into the ruined solution again (one after another). To actually insert a job, the algorithm
calculates its "best" insertion position. This implies also to check the feasibility of the insertion step
which is in turn dependent on constraints (such as capacity constraints).

## jsprit knows hard and soft constraints. 

- Hard constraints must be met and cannot be broken
- soft constraints are always fulfilled but uses penalties to express "good" and "bad" insertions.

jsprit comes with built-in or default constraints (such as capacity and time-window constraints) and allows you to add
custom constraints. However, you can also disable the default constraints. To add custom constraints use
<code>core.algorithm.VehicleRoutingAlgorithmBuilder</code> to build an algorithm instead of
<code>core.algorithm.io.VehicleRoutingAlgorithms</code>.

Note that

<pre><code>VehicleRoutingAlgorithmBuilder vraBuilder = new VehicleRoutingAlgorithmBuilder(routingProblem, "yourAlgorithmConfig");
vraBuilder.addCoreConstraints();
vraBuilder.addDefaultCostCalculators();
VehicleRoutingAlgorithm vra = vraBuilder.build();
</code></pre>

is equivalent to

<pre><code>VehicleRoutingAlgorithm vra = VehicleRoutingAlgorithms.readAndCreateAlgorithm(routingProblem, "yourAlgorithmConfig");;
</code></pre>

To add custom constraints add the following lines:
<pre><code>VehicleRoutingAlgorithmBuilder vraBuilder = new VehicleRoutingAlgorithmBuilder(routingProblem, "yourAlgorithmConfig");
vraBuilder.addCoreConstraints();
vraBuilder.addDefaultCostCalculators();

//add custom constraint
StateManager stateManager = new StateManager(routingProblem.getTransportCosts());
ConstraintManager constraintManager = new ConstraintManager(routingProblem, stateManager);
constraintManager.addConstraint(customConstraint);
vraBuilder.setStateAndConstraintManager(stateManager, constraintManager);

VehicleRoutingAlgorithm vra = vraBuilder.build();
</code></pre>


## Hard constraints

There are hard constraints at two different levels: at route and activity level.

A route is basically a sequence of activities. Each route has a start- and an end-activity, and in between other activities of type <code>core.problem.solution.route.activity.TourActivity</code>.

### <code>core.problem.constraint.HardRouteConstraint</code>
A HardRouteConstraint indicates whether a specified job can be inserted into an existing route (along with a specified vehicle). To define it you need to implement the HardRouteConstraint-interface:

<pre><code>HardRouteConstraint constraint = new HardRouteConstraint(){

    @Override
    public boolean fulfilled(JobInsertionContext iContext) {
        return true;
	};

};
</code></pre>

The JobInsertionContext tells you the context of the insertion step, i.e. the specified job (<code>iContext.getJob()</code>) to be inserted into
a specified route (<code>iContext.getRoute()</code>) as well as the vehicle
that should be employed on that route (<code>iContext.getNewVehicle()</code>).

#### Example:
Assume a vehicle with id="1" is not allowed to serve a job with id="job1" (since it is for example too big to access the customer location). Such a constraint can be easily defined as follows:

<pre><code>final Job jobWithAccessConstraint = routingProblem.getJobs().get("job1");
HardRouteConstraint accessConstraint = new HardRouteConstraint() {

    @Override
    public boolean fulfilled(JobInsertionContext iContext) {
        if(iContext.getNewVehicle().getId().equals("1")){
            if(iContext.getJob()==jobWithAccessConstraint || iContext.getRoute().getTourActivities().servesJob(jobWithAccessConstraint)){
                return false;
            }
        }
        return true;
    }

};

constraintManager.addConstraint(accessConstraint);
</code></pre>

<strong>Note</strong> that if vehicleSwitches are not allowed, you are done with the first term <code>iContext.getJob()==jobWithAccessConstraint</code> in the above if-clause. If they are, you need to add the second term <code>iContext.getRoute().getTourActivities().servesJob(jobWithAccessConstraint</code> as well to prohibit the vehicle (id=1) to take over a route that already contains job1.

### core.problem.constraint.HardActivityConstraint
to be documented ... (as long as it is not yet documented it might be helpful to see how the [service backhaul constraint](https://github.com/jsprit/jsprit/blob/master/jsprit-core/src/main/java/jsprit/core/problem/constraint/ServiceDeliveriesFirstConstraint.java) is implemented which you can add to the constraintManager much like the above access constraint)

## Soft constraints
to be documented ... (as long as it is not yet documented the following examples might be helpful http://stackoverflow.com/questions/24447451/related-jobs-in-jsprit)
