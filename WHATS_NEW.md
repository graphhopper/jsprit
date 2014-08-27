WHATS NEW
==========
**v1.4.0** ? in preparation

<b> SKILLS </b>

Skills can now be included easily (see for example https://github.com/jsprit/jsprit/blob/master/jsprit-examples/src/main/java/jsprit/examples/SolomonWithSkillsExample.java).
It lets you assign hard requirements to jobs and vehicles/drivers. For example, a technician requires a screwdriver to serve customer A or
your vehicle requires a loading bridge to unload freight at customer B etc.. You can add an arbitrary number of skills to jobs and vehicles.

To enable the algorithm to consider skills, you need to use <code>core.algorithm.VehicleRoutingAlgorithmBuilder</code> as follows:

<pre><code>
VehicleRoutingAlgorithmBuilder vraBuilder = new VehicleRoutingAlgorithmBuilder(skillProblem,yourConfig);
vraBuilder.addCoreConstraints();
vraBuilder.addDefaultCostCalculators();
           
StateManager stateManager = new StateManager(skillProblem);
stateManager.updateSkillStates();

ConstraintManager constraintManager = new ConstraintManager(skillProblem,stateManager);
constraintManager.addSkillsConstraint();

VehicleRoutingAlgorithm vra = vraBuilder.build();
</code></pre>



<b> UNASSIGNED JOB LIST</b>

- new feature: unassigned job list 

<b> STATEMANAGER </b>

- StateFactory.createId(String name) moved to core.algorithm.state.StateManager.createStateId(String name)
- StateFactory moved from core.problem.solution.route.state.StateFactory to core.algorithm.state.InternalStates
- StateId moved from core.problem.route.state.StateFactory.StateId to core.algorithm.state.StateId
- StateFactory.createId(String name) is not accessible anymore
- constructor new StateManager(VehicleRoutingTransportCosts costs) does not exist anymore, but is new StateManager(VehicleRoutingProblem vrp)
- StateManager.addDefault... methods do not exists anymore. Client must now decide what to do when state does not exist.
- deprecated core.problem.VehicleRoutingProblem.Builder.addVehicle(Vehicle v) and added core.problem.VehicleRoutingProblem.Builder.addVehicle(AbstractVehicle v)
- deprecated core.problem.VehicleRoutingProblem.Builder.addJob(Job j) and added core.problem.VehicleRoutingProblem.Builder.addJob(AbstractJob j)

<b> LOGGER </b>
- migrated from log4j1x to log4j2
