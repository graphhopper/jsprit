Change-log
==========

**v1.7.2** @ 2017-06-08
- see [Whats new](https://github.com/graphhopper/jsprit/blob/master/WHATS_NEW.md)

**v1.7.1** @ 2017-05-11
- see [Whats new](https://github.com/graphhopper/jsprit/blob/master/WHATS_NEW.md)

**v1.7** @ 2017-01-12
- see [Whats new](https://github.com/graphhopper/jsprit/blob/master/WHATS_NEW.md)

**v1.6.2** @ 2016-02-02

new features:
- driver breaks
- multiple time windows
- consideration of waiting times
- fast regret
- a number of internal improvements

- [detailed changelog ](https://rawgit.com/jsprit/misc-rep/master/changelog_1.6.1_1.6.2.html)

**v1.6.1** @ 2015-08-10
- feature [#165](https://github.com/jsprit/jsprit/issues/156)
- feature [#169](https://github.com/jsprit/jsprit/issues/159)
- bugfix [#154](https://github.com/jsprit/jsprit/issues/154)
- bugfix [#155](https://github.com/jsprit/jsprit/issues/155)
- bugfix [#158](https://github.com/jsprit/jsprit/issues/158)
- bugfix [#164](https://github.com/jsprit/jsprit/issues/164)
- bugfix [#165](https://github.com/jsprit/jsprit/issues/165)

- [detailed changelog ](https://rawgit.com/jsprit/misc-rep/master/changelog_1.6_to_1.6.1.html)


**v1.6** @ 2015-03-12
<b>! Break change !</b>

new features: see [Whats new](https://github.com/jsprit/jsprit/blob/master/WHATS_NEW.md)

- [detailed changelog ](https://github.com/jsprit/misc-rep/blob/master/changelog_1.5_to_1.6.txt)

**v1.5** @ 2014-12-12
<b>! Break change !</b>

It only breaks your code if you already build your algorithm from scratch since strategies now need to have unique ids. just add a unique id to the SearchStrategy constructor.

new features: see [Whats new](https://github.com/jsprit/jsprit/blob/master/WHATS_NEW.md)

- [detailed changelog](https://github.com/jsprit/misc-rep/blob/master/changelog_1.4.2_to_1.5.txt)

**v1.4.2** @ 2014-10-14

new feature: core.analysis.SolutionAnalyser

**v1.4.1** @ 2014-09-25
- bugfix [#131](https://github.com/jsprit/jsprit/issues/131)
- bugfix [#134](https://github.com/jsprit/jsprit/issues/134)

**v1.4** @ 2014-09-08

<b>! Break change !</b>

- [detailed changelog](https://github.com/jsprit/misc-rep/blob/master/changelog_1.3.1_to_1.4.txt)
- migrated from log4j1x to log4j2 (if you do not use Maven, see how the dependencies changed: [https://github.com/jsprit/jsprit/wiki/jsprit-core](https://github.com/jsprit/jsprit/wiki/jsprit-core)
- new feature: skills can now be included easily (see for example https://github.com/jsprit/jsprit/blob/master/jsprit-examples/src/main/java/jsprit/examples/SolomonWithSkillsExample.java)
- new feature: unassigned job list
- countless improvements of javadocs

<em>jsprit-core</em>
- renaming of core.problem.constraint.HardActivityStateLevelConstraint into HardActivityConstraint
- renaming of core.problem.constraint.HardRouteStateLevelConstraint into HardRouteConstraint
- StateFactory.createId(String name) moved to core.algorithm.state.StateManager.createStateId(String name)
- StateFactory moved from core.problem.solution.route.state.StateFactory to core.algorithm.state.InternalStates
- StateId moved from core.problem.route.state.StateFactory.StateId to core.algorithm.state.StateId
- StateFactory.createId(String name) is not accessible anymore
- constructor new StateManager(VehicleRoutingTransportCosts costs) does not exist anymore, but is new StateManager(VehicleRoutingProblem vrp)
- StateManager.addDefault... methods do not exists anymore. Client must now decide what to do when state does not exist.
- deprecated core.problem.VehicleRoutingProblem.Builder.addVehicle(Vehicle v) and added core.problem.VehicleRoutingProblem.Builder.addVehicle(AbstractVehicle v)
- deprecated core.problem.VehicleRoutingProblem.Builder.addJob(Job j) and added core.problem.VehicleRoutingProblem.Builder.addJob(AbstractJob j)
- <b>this [example](https://github.com/jsprit/jsprit/blob/master/jsprit-examples/src/main/java/jsprit/examples/MultipleProductsWithLoadConstraintExample.java) might make migrating from v1.3.1 to v1.4 easier (since both versions are implemented - v1.3.1 was commented out) </b>

- bugfix [#107](https://github.com/jsprit/jsprit/issues/107)
- bugfix [#109](https://github.com/jsprit/jsprit/issues/109)
- bugfix [#111](https://github.com/jsprit/jsprit/issues/111)
- bugfix [#112](https://github.com/jsprit/jsprit/issues/112)
- bugfix [#114](https://github.com/jsprit/jsprit/issues/114)
- bugfix [#126](https://github.com/jsprit/jsprit/issues/126)
- bugfix [#128](https://github.com/jsprit/jsprit/issues/128)

**v1.3.1** @ 2014-06-14

<em>jsprit-core</em>
- bugfix [#104](https://github.com/jsprit/jsprit/issues/104)
- bugfix [#105](https://github.com/jsprit/jsprit/issues/105)

**v1.3.0** @ 2014-05-19
- [detailed changelog](https://github.com/jsprit/misc-rep/raw/master/changelog_1.2.0_to_1.3.0.txt)
- removed deprecated code (that had been deprecated before v1.2.0) which is definitely a break change (see details above)

<em>jsprit-analysis</em>
- redesigned analysis.toolbox.Plotter - it now always shows first activity as small triangle (no annotation that does not scale anymore)
- added analysis.toolbox.ComputationalLaboratory to conduct various sensitivity studies concurrently
- added analysis.toolbox.XYLineChartBuilder to simplify chart creation of n XYLines
- bugfix [#59](https://github.com/jsprit/jsprit/issues/59)

<em>jsprit-core</em>
- added a number of unit- and integration-test to better guarantee/protect functionality
- added feature: algorithm maximizes/minimizes whatever constraints and custom objective suggest, i.e.constraints and custom objective function can now be easily defined
and considered by the meta-heuristic [#57](https://github.com/jsprit/jsprit/issues/57), [#72](https://github.com/jsprit/jsprit/issues/72)
- added feature: initial loads can now be defined [#87](https://github.com/jsprit/jsprit/issues/87)
- bugfix [#84](https://github.com/jsprit/jsprit/issues/84)
- bugfix [#91](https://github.com/jsprit/jsprit/issues/91)
- bugfix [#92](https://github.com/jsprit/jsprit/issues/92)
- bugfix [#95](https://github.com/jsprit/jsprit/issues/95)
- bugfix [#96](https://github.com/jsprit/jsprit/issues/96)
- bugfix [#98](https://github.com/jsprit/jsprit/issues/98)




**v1.2.0** @ 2014-03-06

- [detailed changelog](https://github.com/jsprit/misc-rep/raw/master/changelog_1.1.1_to_1.2.0.txt) (containing added and deprecated methods and classes)

<em>jsprit-core:</em>
- added feature: multiple capacity dimensions ([#55](https://github.com/jsprit/jsprit/issues/55))
- added feature: different start and end locations of routes ([#74](https://github.com/jsprit/jsprit/issues/74))
- added a number of unit-tests
- reworked StateManager to deal with any state-object
- VrpXMLReader$ServiceBuilderFactory: Parameter 3 of 'public Service$Builder createBuilder(java.lang.String, java.lang.String, int)' has changed its type to java.lang.Integer
- Job: Method 'public Capacity getSize()' has been added to an interface <b>[potential Break Change]</b>
- Service$Builder: Removed field demand
- ServiceActivity: Removed field capacityDemand
- TourActivity: Method 'public Capacity getSize()'  has been added to an interface <b>[potential Break Change]</b>
- RouteAndActivityStateGetter: Method 'public java.lang.Object getActivityState(TourActivity, StateFactory$StateId, java.lang.Class)' has been added to an interface <b>[potential Break Change]</b>
- RouteAndActivityStateGetter: Method 'public java.lang.Object getRouteState(VehicleRoute, StateFactory$StateId, java.lang.Class)' has been added to an interface <b>[potential Break Change]</b>
- Vehicle: Method 'public Coordinate getEndLocationCoordinate()' has been added to an interface <b>[potential Break Change]</b>
- Vehicle: Method 'public java.lang.String getEndLocationId()' has been added to an interface <b>[potential Break Change]</b>
- Vehicle: Method 'public Coordinate getStartLocationCoordinate()' has been added to an interface <b>[potential Break Change]</b>
- Vehicle: Method 'public java.lang.String getStartLocationId()' has been added to an interface <b>[potential Break Change]</b>
- VehicleFleetManager: Method 'public java.util.Collection getAvailableVehicles(Vehicle)' has been added to an interface <b>[potential Break Change]</b>
- VehicleType: Method 'public Capacity getCapacityDimensions()' has been added to an interface <b>[potential Break Change]</b>
- jsprit.core.util.VrpVerifier: Class jsprit.core.util.VrpVerifier removed <b>[potential Break Change]</b>



**v1.1.1** @ 2014-01-31

<em>jsprit-core:</em>
- fixed bug: [#80](https://github.com/jsprit/jsprit/issues/80)
- added new package reporting with basic reporting

**v1.1.0** @ 2014-01-27

- [detailed changelog](https://github.com/jsprit/misc-rep/raw/master/changelog_1.0.0_to_1.1.0.txt)

<em>jsprit-core:</em>
- added javadocs (VehicleRoutingProblem and classes in package vehicle. and job.)
- added unit-tests (for classes in package vehicle., job. and io.)
- deprecated methods in VehicleRoutingProblem, VehicleTypeImpl, VehicleImpl
- added func in VehicleRoutingProblem.Builder (.addPenaltyVehicle(...) methods)
- added feature: open-routes ([#54](https://github.com/jsprit/jsprit/issues/54))
- added func in VehicleImpl and VehicleImpl.Builder (.setReturnToDepot(...), isReturnToDepot())
- added feature: prohibit vehicles to take over entire route ([#70](https://github.com/jsprit/jsprit/issues/70))
- fixed bug: [#58](https://github.com/jsprit/jsprit/issues/58),[#76](https://github.com/jsprit/jsprit/issues/76)-[#79](https://github.com/jsprit/jsprit/issues/79)
- added abstract class AbstractForwardVehicleRoutingCosts
- inspected and removed all warnings
- visibility of methods activity.Start.get/setCoordinate(...) decreased from public to package <b>[potential Break Change]</b>
- visibility of methods activity.End.get/setCoordinate(...) decreased from public to package <b>[potential Break Change]</b>
- method isReturnToDepot() has been added to interface Vehicle <b>[potential Break Change]</b>
- visibility of constructor VehicleImpl.Builder decreased from public to private <b>[potential Break Change]</b>

<em>jsprit-analysis:</em>
- added GraphStreamViewer
- inspected and removed all warnings

<em>jsprit-example:</em>
- added BicycleMessenger
- enriched examples with GraphStreamViewer
- inspected and removed all warnings

<em>jsprit-instance:</em>
- added VrphGoldenReader (plus instances to bechmark VRPH)
- inspected and removed all warnings



**v1.0.0** @ 2013-11-26 (break change)

- re-organized API
- new package names: jsprit.&lt;module&gt;.&lt;folder(s)&gt;
- most of the breaks can be fixed by (re-)organizing imports
- however the following breaks have to be fixed manually:
- SolutionPrinter.<del>print(solution, Print.VERBOSE)</del> --> use .print(solution) instead
- VehicleRoute: <del>getCosts()</del>, <del>getCostCalculator()</del>
- TimeBreaker --> TimeTermination, VariationCoefficentBreaker --> ...Termination, Iteration...Breaker --> ...Termination
- VehicleRoutingAlgorithm: setPrematureBreaker(...) --> setPrematureTermination(...)
- util.<del>Counter</del>
- [detailed changelog](https://github.com/jsprit/misc-rep/raw/master/changelog_0.0.5_to_1.0.0.txt)

**v0.0.5** @ 2013-11-22

- more memory-efficient RadialRuin (issue #53)
- bug fix (issue #51)

**v0.0.4** @ 2013-10-17

- a number of internal improvements
- license change from GPLv2 to LGPLv3
- add premature algorithm termination: PrematureAlgorithmBreaker.java and its implementations
- searchStrategy.java: public SearchStrategy(SolutionSelector,SolutionAcceptor) --> public SearchStratgy(SolutionSelector,SolutionAcceptor,SolutionCostCalculator)
- searchStrategy.java: public boolean run(...) --> public DiscoveredSolution run(...)
- vehicleImpl.VehicleType.Builder --> VehicleTypeImpl.Builder
- vehicleImpl.VehicleBuilder --> VehicleImpl.Builder

**v0.0.3** @ 2013-06-04

- bug fix - access resources in jar

**v0.0.2** @ 2013-06-03

- bug fix - access resources in jar

**v0.0.1** @ 2013-06-02
