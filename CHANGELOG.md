Change-log
==========
**v1.1.1** @ 2014-01-31
<em>jsprit-core:</em>
- fixed bug: [#80]()
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
