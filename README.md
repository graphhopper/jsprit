# jsprit

jsprit is a java based, open source toolkit for solving rich [Traveling Salesman Problems (TSP)](http://en.wikipedia.org/wiki/Travelling_salesman_problem) and [Vehicle Routing Problems (VRP)](https://en.wikipedia.org/wiki/Vehicle_routing_problem).

It is lightweight, flexible and easy-to-use, and based on a single all-purpose [meta-heuristic](docs/Meta-Heuristic.md) currently solving:

- Capacitated VRP
- Multiple Depot VRP
- VRP with Time Windows
- VRP with Backhauls
- VRP with Pickups and Deliveries
- VRP with Heterogeneous Fleet
- Time-dependent VRP
- Traveling Salesman Problem
- Dial-a-Ride Problem
- Various combinations of these types

Setting up the problem, defining additional constraints, modifying the algorithms and visualising the discovered solutions is straightforward. It is designed for change and extension with a modular architecture and comprehensive test coverage. [More features...](https://github.com/graphhopper/jsprit/blob/master/docs/Features.textile)

The jsprit-project is maintained by [GraphHopper](https://graphhopper.com/).

## Requirements

- **Java 21** or higher

## Installation

**Maven:**
```xml
<dependency>
    <groupId>com.graphhopper</groupId>
    <artifactId>jsprit-core</artifactId>
    <version>2.0.0</version>
</dependency>
```

**Gradle:**
```groovy
implementation 'com.graphhopper:jsprit-core:2.0.0'
```

## Quick Start

```java
// Define vehicles
VehicleType vehicleType = VehicleTypeImpl.Builder.newInstance("type")
    .addCapacityDimension(0, 10)
    .build();

VehicleImpl vehicle = VehicleImpl.Builder.newInstance("vehicle")
    .setStartLocation(Location.newInstance(0, 0))
    .setType(vehicleType)
    .build();

// Define jobs
Service job1 = Service.Builder.newInstance("job1")
    .addSizeDimension(0, 1)
    .setLocation(Location.newInstance(5, 7))
    .build();

Service job2 = Service.Builder.newInstance("job2")
    .addSizeDimension(0, 2)
    .setLocation(Location.newInstance(3, 4))
    .build();

// Build the problem
VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance()
    .addVehicle(vehicle)
    .addJob(job1)
    .addJob(job2)
    .build();

// Solve
VehicleRoutingAlgorithm algorithm = Jsprit.createAlgorithm(vrp);
Collection<VehicleRoutingProblemSolution> solutions = algorithm.searchSolutions();
VehicleRoutingProblemSolution best = Solutions.bestOf(solutions);
```

## What's New in 2.0

**Independent Operator Selection** - Configure ruin and insertion strategies separately:
```java
VehicleRoutingAlgorithm algorithm = Jsprit.Builder.newInstance(vrp)
    .addRuinOperator(0.4, Ruin.radial())
    .addRuinOperator(0.4, Ruin.random())
    .addRuinOperator(0.2, Ruin.worst())
    .addInsertionOperator(0.7, Insertion.regretFast())
    .addInsertionOperator(0.3, Insertion.best())
    .buildAlgorithm();
```

**Algorithm Event System** - Monitor algorithm execution:
```java
AlgorithmEventAdapter adapter = new AlgorithmEventAdapter(algorithm);
algorithm.addListener(adapter);
algorithm.addEventListener(event -> {
    if (event instanceof StrategyExecuted e) {
        System.out.println("Strategy: " + e.strategyId());
    }
});
```

**Other improvements:**
- Regret-k insertion (configurable k)
- MST-based cluster ruin strategy
- Declarative initial solutions with SolutionSpec
- Job.Type enum for cleaner type handling

See [CHANGELOG.md](CHANGELOG.md) for full details.

Upgrading from 1.x? See [MIGRATION.md](MIGRATION.md).

## Documentation

Please visit [docs](https://github.com/graphhopper/jsprit/blob/master/docs/Home.md) to learn more.

## Modules and Dependencies

Please read [NOTICE.md](https://github.com/graphhopper/jsprit/blob/master/NOTICE.md) to get to know the direct dependencies of each module.

## License

This software is released under [Apache License v2](https://www.apache.org/licenses/LICENSE-2.0).

## Contribution

Any contribution is welcome. Feel free to improve jsprit and make pull requests. If you want to contribute to jsprit, fork the project and build your fork, make changes, run tests and make a pull request (see [GitHub help](https://help.github.com/articles/fork-a-repo) for details).

See who has contributed [here](https://github.com/graphhopper/jsprit/graphs/contributors).

## Contact

**Forum:** In the [GraphHopper Forum](https://discuss.graphhopper.com/) you can discuss jsprit related issues and get answers to your questions.

**Issue Tracker:** For bugs and feature requests, use the [issue tracker](https://github.com/graphhopper/jsprit/issues).

**Email:** If you prefer private communication, [contact us via mail](https://graphhopper.com/#contact).
