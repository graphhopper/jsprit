WHATS NEW
==========
**v1.4.0** ? in preparation

<b> SKILLS </b>

- new feature: skills can now be included easily (see for example https://github.com/jsprit/jsprit/blob/master/jsprit-examples/src/main/java/jsprit/examples/SolomonWithSkillsExample.java)

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
