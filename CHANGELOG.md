Change-log
==========
**v0.0.5** @ 2013-11-22

- more memory-efficient RadialRuin (see issue #53)


**v0.0.4** @ 2013-10-17

- A number of internal improvements
- License change from GPLv2 to LGPLv3
- Add premature algorithm termination: PrematureAlgorithmBreaker.java and its implementations
- SearchStrategy.java: public SearchStrategy(SolutionSelector,SolutionAcceptor) --> public SearchStratgy(SolutionSelector,SolutionAcceptor,SolutionCostCalculator)
- SearchStrategy.java: public boolean run(...) --> public DiscoveredSolution run(...)
- VehicleImpl.VehicleType.Builder --> VehicleTypeImpl.Builder
- VehicleImpl.VehicleBuilder --> VehicleImpl.Builder

**v0.0.3** @ 2013-06-04

- Bug fix - access resources in jar

**v0.0.2** @ 2013-06-03

- Bug fix - access resources in jar

**v0.0.1** @ 2013-06-02
