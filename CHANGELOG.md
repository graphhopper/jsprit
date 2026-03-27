# Changelog

All notable changes to jsprit will be documented in this file.

## [2.0.0] - 2026-03-27

### Breaking Changes

- **Java 21 required** - Minimum Java version is now 21 (was 8)
- **JUnit 5** - Test framework upgraded from JUnit 4 to JUnit 5
- **Index management** - Job and Vehicle indices are now managed by VRP, not mutable on objects
  - `Job.setIndex()` and `Vehicle.setIndex()` are deprecated
  - Indices are assigned automatically when building VRP

### Added

#### New API: Independent Operator Selection
Select ruin and insertion operators independently with weights:
```java
Jsprit.Builder.newInstance(vrp)
    .addRuinOperator(0.3, Ruin.radial(0.3, 0.5, 10, 70))
    .addRuinOperator(0.3, Ruin.random(0.3, 0.5, 10, 70))
    .addRuinOperator(0.2, Ruin.kruskalCluster())
    .addInsertionOperator(0.7, Insertion.regretFast())
    .addInsertionOperator(0.3, Insertion.best())
    .buildAlgorithm();
```

#### Ruin Strategies
- `Ruin.random()` - Random job removal
- `Ruin.radial()` - Remove nearby jobs
- `Ruin.worst()` - Remove jobs with highest removal benefit
- `Ruin.cluster()` - DBSCAN-based cluster removal
- `Ruin.kruskalCluster()` - MST-based cluster removal (new)
- `Ruin.string()` - Remove sequences from routes
- `Ruin.timeRelated()` - Remove jobs with similar time windows
- All ruin factories support bounded fractions: `Ruin.random(0.3, 0.5, 10, 70)`
  (30-50% of jobs, clamped to [10, 70])

#### Insertion Strategies
- `Insertion.regretFast()` - Fast regret-2 insertion
- `Insertion.regret()` - Standard regret without optimizations
- `Insertion.best()` - Greedy best insertion
- `Insertion.cheapest()` - Cheapest insertion
- `Insertion.positionRegret()` - Position-based regret (experimental)

#### Algorithm Event System
New observability system for monitoring algorithm execution:
```java
algorithm.addListener(new AlgorithmEventAdapter() {
    @Override
    public void onEvent(AlgorithmEvent event) {
        switch (event) {
            case IterationStarted e -> log("Iteration " + e.iteration());
            case StrategyExecuted e -> log("Strategy: " + e.strategyId());
            case JobInserted e -> log("Inserted: " + e.jobId());
            // ... more events
        }
    }
});
```

Events: `IterationStarted`, `IterationCompleted`, `StrategySelected`, `StrategyExecuted`,
`RuinStarted`, `RuinCompleted`, `JobRemoved`, `RecreateStarted`, `RecreateCompleted`,
`JobInserted`, `JobUnassigned`, `InsertionEvaluated`, `AcceptanceDecision`

#### SolutionSpec for Declarative Initial Solutions
```java
SolutionSpec spec = SolutionSpec.builder()
    .addRoute("vehicle1", "job1", "job2", "job3")
    .addRoute("vehicle2", "job4", "job5")
    .build();

Jsprit.Builder.newInstance(vrp)
    .addInitialSolution(spec)
    .buildAlgorithm();
```

#### Other Additions
- `Job.Type` enum to replace instanceof checks
- Setup time support in activity duration calculations
- Soft constraints now accessible via API
- Cost breakdown in `AcceptanceDecision` event
- En-route pickup and delivery support

### Changed

- Regret insertion expanded to regret-k (configurable k value)
- Improved performance through route indexing with Trove maps
- RuinRadial now operates on activities in current solution
- Optimized array operations in StateManager
- Better memory footprint for insertion calculations

### Fixed

- Time window bug in certain edge cases
- Vehicle switch feasibility check
- Removal bug in open deliveries
- Empty queue handling in insertion
- MaxDistance relatedness calculation in RuinTimeRelated
- Total operation time calculation in SolutionAnalyser
- DBSCAN fallback when returning empty clusters

### Deprecated

- `considerFixedCosts()` methods - use objective function instead
- `Job.getIndex()` / `Job.setIndex()` - indices managed by VRP
- `Vehicle.getIndex()` / `Vehicle.setIndex()` - indices managed by VRP
- `CalculationUtils` class
- `RuinRadialMultipleCenters` - use `Ruin.radial()` instead
- `InsertionBuilder` - use `InsertionStrategyBuilder` instead
- `ServiceInsertionOnRouteLevelCalculator`
- `RouteLevelActivityInsertionCostsEstimator`

### Experimental Features

These features are available but may change in future releases:
- Spatial filtering in regret insertion (`Insertion.regretFast(k, spatialFilterK, tracking)`)
- Affected-job tracking optimization
- Position-based regret insertion (`Insertion.positionRegret()`)

---

## [1.8] - 2020-04-01

Last stable release before 2.0. See [GitHub releases](https://github.com/graphhopper/jsprit/releases/tag/v1.8).

---

## Previous Releases

**v1.7.3** @ 2019-04-10

**v1.7.2** @ 2017-06-08
- see [Whats new](https://github.com/graphhopper/jsprit/blob/master/WHATS_NEW.md)

**v1.7.1** @ 2017-05-11
- see [Whats new](https://github.com/graphhopper/jsprit/blob/master/WHATS_NEW.md)

**v1.7** @ 2017-01-12
- see [Whats new](https://github.com/graphhopper/jsprit/blob/master/WHATS_NEW.md)

**v1.6.2** @ 2016-02-02
- driver breaks
- multiple time windows
- consideration of waiting times
- fast regret
- a number of internal improvements

**v1.6.1** @ 2015-08-10

**v1.6** @ 2015-03-12

**v1.5** @ 2014-12-12

**v1.4.2** @ 2014-10-14

**v1.4.1** @ 2014-09-25

**v1.4** @ 2014-09-08

**v1.3.1** @ 2014-06-14

**v1.3.0** @ 2014-05-19

**v1.2.0** @ 2014-03-06

**v1.1.1** @ 2014-01-31

**v1.1.0** @ 2014-01-27

**v1.0.0** @ 2013-11-26

For detailed changelogs of older versions, see [GitHub releases](https://github.com/graphhopper/jsprit/releases).
