# Migration Guide: jsprit 1.x to 2.0

This guide helps you upgrade from jsprit 1.x to version 2.0.

## Prerequisites

### Java 21 Required

jsprit 2.0 requires **Java 21** (was Java 8). Update your build configuration:

**Maven:**
```xml
<properties>
    <maven.compiler.source>21</maven.compiler.source>
    <maven.compiler.target>21</maven.compiler.target>
</properties>
```

**Gradle:**
```groovy
java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}
```

### JUnit 5 for Tests

If you have tests extending jsprit test classes:

```xml
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter-api</artifactId>
    <version>5.10.2</version>
    <scope>test</scope>
</dependency>
```

---

## Breaking Changes

### Index Management

**Why this changed:** Previously, indices were stored on Job and Vehicle objects. This caused problems when you needed to solve variations of a problem - for example, testing different fleet configurations or job subsets. Reusing Job objects across multiple VRPs led to index conflicts, forcing you to recreate all objects from scratch for each variation.

**What's new:** Each VRP now manages its own indices internally. When you call `build()`, indices are assigned automatically (0 to n-1). The same Job or Vehicle objects can be safely reused across multiple VRP instances.

**Example - solving problem variations:**
```java
// Define jobs once
Service job1 = Service.Builder.newInstance("job1").setLocation(loc1).build();
Service job2 = Service.Builder.newInstance("job2").setLocation(loc2).build();

// Create different problem variations reusing the same jobs
VehicleRoutingProblem vrp1 = VehicleRoutingProblem.Builder.newInstance()
    .addJob(job1).addJob(job2)
    .addVehicle(smallFleet)
    .build();

VehicleRoutingProblem vrp2 = VehicleRoutingProblem.Builder.newInstance()
    .addJob(job1).addJob(job2)
    .addVehicle(largeFleet)  // Different fleet
    .build();

// Solve both independently - no index conflicts
```

**Retrieving indices:** Use the VRP instance to get indices:
```java
int jobIndex = vrp.getJobIndex(job);
int vehicleIndex = vrp.getVehicleIndex(vehicle);
```

**Impact:** For most users, this is transparent. `Job.getIndex()` and `Vehicle.getIndex()` are deprecated but still work (returning the index from the last VRP they were added to).

---

## New Features

### Independent Operator Selection (Recommended)

The new API allows selecting ruin and insertion operators independently with weights:

**Before (1.x):**
```java
VehicleRoutingAlgorithm algorithm = Jsprit.createAlgorithm(vrp);
```

**After (2.0) - Basic:**
```java
VehicleRoutingAlgorithm algorithm = Jsprit.createAlgorithm(vrp);
// Works exactly as before - no changes required
```

**After (2.0) - With Custom Operators:**
```java
VehicleRoutingAlgorithm algorithm = Jsprit.Builder.newInstance(vrp)
    // Ruin operators with weights
    .addRuinOperator(0.3, Ruin.radial(0.3, 0.5, 10, 70))
    .addRuinOperator(0.3, Ruin.random(0.3, 0.5, 10, 70))
    .addRuinOperator(0.2, Ruin.worst())
    .addRuinOperator(0.2, Ruin.kruskalCluster())

    // Insertion operators with weights
    .addInsertionOperator(0.7, Insertion.regretFast())
    .addInsertionOperator(0.3, Insertion.best())

    .buildAlgorithm();
```

### Available Ruin Strategies

| Factory Method | Description |
|---------------|-------------|
| `Ruin.random()` | Random job removal |
| `Ruin.random(minFrac, maxFrac, minBound, maxBound)` | Random with bounded share |
| `Ruin.radial()` | Remove nearby jobs |
| `Ruin.radial(minFrac, maxFrac, minBound, maxBound)` | Radial with bounded share |
| `Ruin.worst()` | Remove jobs with highest removal benefit |
| `Ruin.worst(minFrac, maxFrac, minBound, maxBound)` | Worst with bounded share |
| `Ruin.cluster()` | DBSCAN-based cluster removal |
| `Ruin.cluster(minFrac, maxFrac, minBound, maxBound)` | Cluster with bounded share |
| `Ruin.kruskalCluster()` | MST-based cluster removal |
| `Ruin.string()` | Remove sequences from routes |
| `Ruin.string(minJobs, maxJobs, minStrings, maxStrings)` | String with parameters |
| `Ruin.timeRelated()` | Remove jobs with similar time windows |
| `Ruin.timeRelated(minFrac, maxFrac, minBound, maxBound)` | Time-related with bounded share |

**Bounded fractions** clamp the share to a range:
```java
// Remove 30-50% of jobs, but at least 10 and at most 70
Ruin.random(0.3, 0.5, 10, 70)
```

### Available Insertion Strategies

| Factory Method | Description |
|---------------|-------------|
| `Insertion.regretFast()` | Fast regret-2 insertion (default) |
| `Insertion.regretFast(k, spatialFilterK, affectedJobTracking)` | Configurable regret-k |
| `Insertion.regret()` | Standard regret-2 |
| `Insertion.regret(k)` | Standard regret-k |
| `Insertion.best()` | Greedy best insertion |
| `Insertion.cheapest()` | Cheapest insertion |
| `Insertion.positionRegret()` | Position-based regret (experimental) |

### Algorithm Event System

Monitor algorithm execution with the new unified event system.

**Setup requires the AlgorithmEventAdapter:**
```java
VehicleRoutingAlgorithm algorithm = Jsprit.createAlgorithm(vrp);

// Create and register the event adapter
AlgorithmEventAdapter adapter = new AlgorithmEventAdapter(algorithm);
algorithm.addListener(adapter);

// Now add event listeners for unified events
algorithm.addEventListener(event -> {
    switch (event) {
        case IterationStarted e ->
            System.out.println("Iteration " + e.iteration());
        case StrategyExecuted e ->
            System.out.println("Strategy: " + e.strategyId());
        case JobInserted e ->
            System.out.println("Inserted: " + e.job().getId() + " into " + e.routeId());
        case JobRemoved e ->
            System.out.println("Removed: " + e.job().getId());
        default -> {}
    }
});
```

**Available events:**
- `IterationStarted`, `IterationCompleted`
- `StrategySelected`, `StrategyExecuted`
- `RuinStarted`, `RuinCompleted`, `JobRemoved`
- `RecreateStarted`, `RecreateCompleted`, `JobInserted`, `JobUnassigned`
- `InsertionEvaluated`, `AcceptanceDecision`

### SolutionSpec for Initial Solutions

Declaratively specify initial solutions:

```java
SolutionSpec spec = SolutionSpec.builder()
    .addRoute("vehicle1", "job1", "job2", "job3")
    .addRoute("vehicle2", "job4", "job5")
    .build();

VehicleRoutingAlgorithm algorithm = Jsprit.Builder.newInstance(vrp)
    .addInitialSolution(spec)
    .buildAlgorithm();
```

### Job Type Enum

Replace `instanceof` checks with the new `Job.Type` enum:

**Before:**
```java
if (job instanceof Service) {
    // handle service
} else if (job instanceof Shipment) {
    // handle shipment
}
```

**After:**
```java
switch (job.getType()) {
    case SERVICE -> handleService((Service) job);
    case SHIPMENT -> handleShipment((Shipment) job);
    case PICKUP -> handlePickup((Pickup) job);
    case DELIVERY -> handleDelivery((Delivery) job);
}
```

---

## Deprecations

### Replace Deprecated Classes

| Deprecated | Replacement |
|-----------|-------------|
| `InsertionBuilder` | `InsertionStrategyBuilder` |
| `RuinRadialMultipleCenters` | `Ruin.radial()` |
| `ServiceInsertionOnRouteLevelCalculator` | Use standard insertion calculators |
| `RouteLevelActivityInsertionCostsEstimator` | Use standard cost estimators |
| `CalculationUtils` | Inline calculations or use `SolutionAnalyser` |

### Fixed Costs Configuration

**Before:**
```java
Jsprit.Builder.newInstance(vrp)
    .considerFixedCosts(true)
    .buildAlgorithm();
```

**After:**
```java
// Fixed costs are now handled through the objective function
// The default objective already considers fixed costs
Jsprit.Builder.newInstance(vrp)
    .buildAlgorithm();
```

---

## Experimental Features

These features are available but may change in future releases:

### Spatial Filtering

Limits route evaluation to nearby routes:

```java
Insertion.regretFast(2, 5, false)  // k=2, filter to 5 nearest routes
```

### Affected-Job Tracking

Only recalculates insertion costs for jobs affected by last insertion:

```java
Insertion.regretFast(2, 0, true)  // Enable affected-job tracking
```

### Position-Based Regret

More accurate regret calculation considering all positions:

```java
Insertion.positionRegret()
Insertion.positionRegret(3, 3)  // k=3, expand top 3 routes
```

---

## Common Migration Scenarios

### Scenario 1: Simple Algorithm Creation

No changes required:
```java
// This still works exactly as before
VehicleRoutingAlgorithm algorithm = Jsprit.createAlgorithm(vrp);
VehicleRoutingProblemSolution solution = Solutions.bestOf(algorithm.searchSolutions());
```

### Scenario 2: Custom Strategy Weights

**Before (1.x with SearchStrategyModule):**
```java
// Complex configuration with coupled strategies
```

**After (2.0):**
```java
VehicleRoutingAlgorithm algorithm = Jsprit.Builder.newInstance(vrp)
    .addRuinOperator(0.4, Ruin.radial())
    .addRuinOperator(0.4, Ruin.random())
    .addRuinOperator(0.2, Ruin.worst())
    .addInsertionOperator(0.7, Insertion.regretFast())
    .addInsertionOperator(0.3, Insertion.best())
    .buildAlgorithm();
```

### Scenario 3: Monitoring Algorithm Progress

**Before (1.x):**
```java
algorithm.addListener(new IterationStartsListener() {
    @Override
    public void informIterationStarts(int iteration, ...) {
        System.out.println("Iteration " + iteration);
    }
});
```

**After (2.0) - Still works, plus new events:**
```java
// Old listeners still work
algorithm.addListener(new IterationStartsListener() { ... });

// New unified event system (requires adapter setup)
AlgorithmEventAdapter adapter = new AlgorithmEventAdapter(algorithm);
algorithm.addListener(adapter);
algorithm.addEventListener(event -> {
    if (event instanceof StrategyExecuted e) {
        System.out.println("Used: " + e.strategyId());
    }
});
```

---

## Getting Help

- [GitHub Issues](https://github.com/graphhopper/jsprit/issues)
- [API Documentation](https://github.com/graphhopper/jsprit)
- [Examples](https://github.com/graphhopper/jsprit/tree/master/jsprit-examples)
