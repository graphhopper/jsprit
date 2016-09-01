#### Configure and create algorithm
##### Per xml
Structure of overall algorithm config:

![configBuildingBlock](https://github.com/jsprit/misc-rep/raw/master/wiki-images/config1.png)

Structure of overall (search) strategy:

![overallStrategyBuildingBlock](https://github.com/jsprit/misc-rep/raw/master/wiki-images/config2.png)

Structure of individual search strategies and modules:

![searchStrategyBuildingBlock](https://github.com/jsprit/misc-rep/raw/master/wiki-images/config3.png)

<pre><code>VehicleRoutingProblem problem = ...
String myAlgorithmConfigXmlFilname = ...
VehicleRoutingAlgorithm vra = VehicleRoutingAlgorithms.readAndCreateAlgorithm(problem, myAlgorithmConfigXmlFilename);</code></pre>

##### In code

<pre><code>AlgorithmConfig myAlgorithmConfig = new AlgorithmConfig();
XMLConfiguration xmlConfig = myAlgorithmConfig.getXMLConfiguration();
xmlConfig.setProperty("iterations", 2000);
xmlConfig.setProperty("construction.insertion[@name]","bestInsertion");
		
xmlConfig.setProperty("strategy.memory", 1);
String searchStrategy = "strategy.searchStrategies.searchStrategy";
		
xmlConfig.setProperty(searchStrategy + "(0).selector[@name]","selectBest");
xmlConfig.setProperty(searchStrategy + "(0).acceptor[@name]","schrimpfAcceptance");
xmlConfig.setProperty(searchStrategy + "(0).acceptor.alpha","0.4");
xmlConfig.setProperty(searchStrategy + "(0).acceptor.warmup","100");

xmlConfig.setProperty(searchStrategy + "(0).modules.module(0)[@name]","ruin_and_recreate");
xmlConfig.setProperty(searchStrategy + "(0).modules.module(0).ruin[@name]","randomRuin");
xmlConfig.setProperty(searchStrategy + "(0).modules.module(0).ruin.share","0.5");
xmlConfig.setProperty(searchStrategy + "(0).modules.module(0).insertion[@name]","bestInsertion");
xmlConfig.setProperty(searchStrategy + "(0).probability","0.5");
		
xmlConfig.setProperty(searchStrategy + "(1).selector[@name]","selectBest");
xmlConfig.setProperty(searchStrategy + "(1).acceptor[@name]","schrimpfAcceptance");
xmlConfig.setProperty(searchStrategy + "(1).modules.module(0)[@name]","ruin_and_recreate");
xmlConfig.setProperty(searchStrategy + "(1).modules.module(0).ruin[@name]","radialRuin");
xmlConfig.setProperty(searchStrategy + "(1).modules.module(0).ruin.share","0.3");
xmlConfig.setProperty(searchStrategy + "(1).modules.module(0).insertion[@name]","bestInsertion");
xmlConfig.setProperty(searchStrategy + "(1).probability","0.5");

VehicleRoutingAlgorithm vra = VehicleRoutingAlgorithms.createAlgorithm(problem,myAlgorithmConfig);
</code></pre>

#### Search solution
<pre><code>Collection&lt;VehicleRoutingProblemSolution&gt; solutions = vra.searchSolutions();</code></pre>

#### Overwrite iterations in code
<pre><code>vra.setMaxIterations(nOfIterations);</code></pre>

#### Premature termination
##### Time
xml
<pre><code>&lt;prematureBreak basedOn="time"&gt;
    &lt;time&gt;2.&lt;/time&gt;
&lt;/prematureBreak&gt;
</code></pre>
in-code
<pre><code>TimeTermination prematureTermination = new TimeTermination(2.); //in seconds
vra.setPrematureAlgorithmTermination(prematureTermination);
vra.addListener(prematureTermination);
</code></pre>
##### Iterations

xml
<pre><code>&lt;prematureBreak basedOn="iterations"&gt;
    &lt;iterations&gt;200&lt;/iterations&gt;
&lt;/prematureBreak&gt;
</code></pre>

in-code
<pre><code>vra.setPrematureAlgorithmTermination(new IterationWithoutImprovementTermination(200));
</code></pre>

##### Variation Coefficient
xml
<pre><code>&lt;prematureBreak basedOn="variationCoefficient"&gt;
    &lt;threshold&gt;0.01&lt;/threshold&gt;
    &lt;iterations&gt;50&lt;/iterations&gt;
&lt;/prematureBreak&gt;
</code></pre>
in-code
<pre><code>VariationCoefficientTermination prematureTermination = new VariationCoefficientTermination(50, 0.01);
vra.setPrematureAlgorithmTermination(prematureTermination);
vra.addListener(prematureTermination);
</code></pre>

#### Add initial solution
<pre><code>vra.addInitialSolution(initialSolution);</code></pre>

#### Add your own code, or listen to the algorithm
The following figure shows the various entry points for your implementation.

![algorithmListeners](https://github.com/jsprit/misc-rep/raw/master/wiki-images/algorithm_v3.png)

core.algorithm.recreate.listener.InsertionListener in turn is parent to InsertionStartsListener, InsertionEndsListener, JobInsertedListener and VehicleSwitchedListener.


Implement one or many listener(s) and register your implementation in the algorithm like that:
<pre><code>IterationStartsListener iterationStartPrinter = new IterationStartsListener() {
			
        @Override
        public void informIterationStarts(int i, VehicleRoutingProblem problem, Collection<VehicleRoutingProblemSolution> solutions) {
              System.out.println("iteration " + i + " starts");
        }
			
};
vra.addListener(iterationStartPrinter);
</code></pre>
Note that - depending on the listener - your implementation should be fast, otherwise it can significantly influence the performance of the algorithm. 

#### Customize your algorithm with <code>core.algorithm.VehicleRoutingAlgorithmBuilder</code>
To use the builder, you need to use 1.2.1-SNAPSHOT version.
<pre><code>VehicleRoutingAlgorithmBuilder algorithmBuilder = new VehicleRoutingAlgorithmBuilder(problem, "myAlgorithmConfig.xml");</code></pre>

The basic structure of the meta-heuristic is still configured in the xml-file (myAlgorithmConfig.xml) or in AlgorithmConfig.java. However, you can set your own objective function and define your own constraints.

##### Set custom objective function
By default, the sum of fixed and variable transportation costs are minimized. Thus if you do not set your own objective, the algorithm grabs the costs-parameters you defined in core.problem.vehicle.VehicleTypeImpl.java and calculates total transportation costs accordingly. If you want to define your own, make it like this

<pre><code>algorithmBuilder.setObjectiveFunction(objectiveFunction);
</code></pre>

Note that if you set your own objective function, your insertion heuristic should insert the jobs according this function. Therefore you need to find appropriate soft constraints to penalyze bad and/or reward good insertion moves.

##### Use default cost-calculators
<pre><code>algorithmBuilder.addDefaultCostCalculators();
</code></pre>

This adds the default objective function and its corresponding insertion costs calculation to penalyze bad and/or reward good insertion moves.

##### Add default (core) constraints and updater
<pre><code>algorithmBuilder.addCoreConstraints();
</code></pre>

This basically adds capacity and time-window constraints.

##### Set your own <code>core.algorithm.state.StateManager</code>
<pre><code>algorithmBuilder.setStateManager(stateManager);
</code></pre>

##### Set your own <code>core.problem.constraint.ConstraintManager</code>
<pre><code>algorithmBuilder.setStateAndConstraintManager(stateManager, constraintManager);
</code></pre>

If you want to add your own constraints you need to define your own stateManager as well.

##### Build the algorithm
<pre><code>VehicleRoutingAlgorithm vra = algorithmBuilder.build();
</code></pre>

Note that 

<pre><code>VehicleRoutingAlgorithm vra = VehicleRoutingAlgorithms.readAndCreateAlgorithm(routingProblem, "yourAlgorithmConfig");
</code></pre>

is equivalent to 

<pre><code>VehicleRoutingAlgorithmBuilder vraBuilder = new VehicleRoutingAlgorithmBuilder(routingProblem, "yourAlgorithmConfig");
vraBuilder.addCoreConstraints();
vraBuilder.addDefaultCostCalculators();
VehicleRoutingAlgorithm vra = vraBuilder.build();
</code></pre>

which is in turn equivalent to 

<pre><code>VehicleRoutingAlgorithmBuilder vraBuilder = new VehicleRoutingAlgorithmBuilder(routingProblem, "yourAlgorithmConfig");

StateManager stateManager = new StateManager(problem.getTransportCosts());
stateManager.updateLoadStates();
stateManager.updateTimeWindowStates();

ConstraintManager constraintManager = new ConstraintManager(problem, stateManager);
constraintManager.addLoadConstraint();
constraintManager.addTimeWindowConstraint();

vraBuilder.setStateAndConstraintManager(stateManager, constraintManager);

vraBuilder.addDefaultCostCalculators();
VehicleRoutingAlgorithm vra = vraBuilder.build();
</code></pre>
