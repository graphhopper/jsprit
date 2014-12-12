WHATS NEW
==========
------------------------------
<b>2014-12-12</b> new release **v1.5**

jsprit made a big step towards analysing and designing your own ruin and recreation strategies.

First, it is now possible to record and view each and every algorithm step which allows you to visualize the search, e.g. neighborhoods
and the emergence of new structures. It is as easy as importing the corresponding classes from jsprit-analysis and coding the following lines:

<pre><code>AlgorithmEventsRecorder recorder = new AlgorithmEventsRecorder(vrp,"output/events.dgs.gz"); 
recorder.setRecordingRange(0,100); //lets you specify the iterations you want to record
vehicleRoutingAlgorithm.addListener(recorder);
</code></pre>

The output consists of many redundant stuff, thus it is best suited for zipping. If you do not want to zip, omit 'gz'. To view
the events, do the following after running the algorithm:

<pre><code>AlgorithmEventsViewer viewer = new AlgorithmEventsViewer();
viewer.setRuinDelay(8); //lets you slow down ruin vis (in ms)
viewer.setRecreationDelay(4); //lets you slow down recreation vis (in ms)
viewer.display("output/events.dgs.gz");
</code></pre>

It is not only beautiful, but it allows you to immediately analyze your own strategies which brings us to the second improvement.

With <code>AbstractRuinStrategy</code> and <code>AbstractInsertionStrategy</code> it is easy to implement your own strategies. The 
abstract classes do all the non-intuitive stuff (such as informing listeners that a job has been inserted etc.) for you and you can focus on your strategies.

With the <code>PrettyAlgorithmBuilder</code> you can finally build your algorithm, basically just by adding your strategies. All
other things that need to be done to wire up the components of your algorithm does the builder for you.

A third improvement in this regards is that you can switch on and off certain strategies or just change their weights in the course of the search (which allow you to adapt your strategies e.g. 
according to its recorded success). You do that by informing the strategy manager to change a strategy's weight much like this:

<pre><code>vra.getSearchStrategyManager().informStrategyWeightChanged(strategyId, 0.); //which is basically switching off the strategy
</code></pre>

This comes with two changes. Strategy probabilities are now strategy weights (and do not need to be smaller than 1) and the probability of choosing a strategy is simply a function of
the strategy's weight and all other weights (i.e. prob(i) = weight(i) / sumOfAllWeights). The second change is that strategies
now require a unique id. Latter might break your code if (and only if) you already build your algorithm from scratch. This [example](https://github.com/jsprit/jsprit/blob/master/jsprit-examples/src/main/java/jsprit/examples/BuildAlgorithmFromScratch.java)
illustrates a few of the outlined features.

Another new feature which is worth to mention is a new InsertionStrategy called [RegretInsertion](https://github.com/jsprit/jsprit/blob/master/jsprit-core/src/main/java/jsprit/core/algorithm/recreate/RegretInsertion.java). It is much less myopic than BestInsertion since it scores all jobs before inserting them.
The one with the highest score will be inserted first. The scoring function is based on opportunity costs, i.e. it compares the best insertion alternative with the second best.
If the difference between both is high, the job will be preferred, i.e. it will be inserted earlier than later. The scoring function comes with default parameter. However, you
can replace params or overwrite the whole scoring function with your own (which currently means that you need to build your algorithm from scratch).
Note, that this strategy will have a significant impact on the computational performance. If you run it with the same no. of iterations as you run BestInsertion, you are 
 likely to wait forever. However, since it is a bit more 'intelligent' you do not need as many iterations. It is also recommended to use the [concurrent mode](https://github.com/jsprit/jsprit/blob/master/jsprit-core/src/main/java/jsprit/core/algorithm/recreate/RegretInsertionConcurrent.java) since RegretInsertion
  is best suited for concurrent calculation.

Last, you can use spherical coordinates (i.e. longitude and latitude) since [GreatCircleCosts](https://github.com/jsprit/jsprit/blob/master/jsprit-core/src/main/java/jsprit/core/util/GreatCircleCosts.java) knows how to calculate great circle distance and time. It approximates distance
 based on the [Haversine formula](http://en.wikipedia.org/wiki/Haversine_formula). If you want to approximate real networks, you can set a detour factor and a speed value.


------------------------------

<b>2014-10-14</b> new release **v1.4.2**

It has a new feature to analyse your VehicleRoutingProblemSolution. 

jsprit.core.analysis.SolutionAnalszer provides a way to easily calculate statistics for your solution such as load after activity, load before activity, picked load on route, delivered load on route, load at beginning of route, load at end, waiting time on route and total waiting time in solution, total transport time and distance and many more. Additionally, you can check whether jsprit's default constraints are violated or not. This might be important if you change your solution ex-post by removing a job etc.. Look at this example to see a (incomplete) list of what can be calculated out-of-the-box now: [VRPWithBackhauls - SolutionAnalyser](https://github.com/jsprit/jsprit/blob/master/jsprit-examples/src/main/java/jsprit/examples/VRPWithBackhaulsExample2.java)

<strong>Note that this feature is sponsored by [Open Door Logistics](http://www.opendoorlogistics.com).</strong>


------------------------------

<b>2014-09-25</b> new release **v1.4.1**

It contains a fix of a critical bug (see [#134](https://github.com/jsprit/jsprit/issues/134)).

------------------------------

<b>2014-09-08</b> new release **v1.4**

v1.4 breaks your code! Look at [changelog](https://github.com/jsprit/jsprit/blob/master/CHANGELOG.md) to migrate from v1.3.1 to v1.4.

<b> SKILLS </b>

Skills can now be included easily (see for example https://github.com/jsprit/jsprit/blob/master/jsprit-examples/src/main/java/jsprit/examples/SolomonWithSkillsExample.java).
It lets you assign hard requirements to jobs and vehicles/drivers. For example, your vehicle requires a loading bridge to unload freight at customer A or 
a technician requires a screwdriver, a hammer and an electric drill to serve customer A. 
You assign latter skills to your customer as follows:

<pre><code>Service service = Service.Builder.newInstance(serviceId).addRequiredSkill("screwdriver")
    .addRequiredSkill("hammer").addRequiredSkill("electric-drill"). ... .build();
</code></pre>

Assign these skills to your technician as well:
 
<pre><code>VehicleImpl skilled_technician = VehicleImpl.Builder.newInstance(technicianId).addSkill("screwdriver")
    .addSkill("hammer").addSkill("electric-drill"). ... .build();
</code></pre> 


Note that you can add an arbitrary number of skills to jobs and vehicles.

To enable the algorithm to consider skills, you need to use <code>core.algorithm.VehicleRoutingAlgorithmBuilder</code> as follows:

<pre><code>
VehicleRoutingAlgorithmBuilder vraBuilder = new VehicleRoutingAlgorithmBuilder(skillProblem,yourConfig);
vraBuilder.addCoreConstraints();
vraBuilder.addDefaultCostCalculators();
           
StateManager stateManager = new StateManager(skillProblem);
<b>stateManager.updateSkillStates();</b>

ConstraintManager constraintManager = new ConstraintManager(skillProblem,stateManager);
<b>constraintManager.addSkillsConstraint();</b>

VehicleRoutingAlgorithm vra = vraBuilder.build();
</code></pre>



<b> UNASSIGNED JOB LIST</b>

A solution can now consists of assigned and unassigned jobs. There are various reasons for unassigned jobs, e.g. 
demand exceeds available capacity, the job cannot be served within driver's operation time or the job is just too costly to 
serve it with your own fleet.
 
Note that jsprit uses a "soft" approach to deal with unassigned jobs, i.e. each unassigned job will be penalyzed in the objective function 
(see default objective https://github.com/jsprit/jsprit/blob/master/jsprit-core/src/main/java/jsprit/core/algorithm/VariablePlusFixedSolutionCostCalculatorFactory.java [line 55]). 
If you omit penalyzing them, you probably end up with a solution consisting solely of unassigned jobs (the less the better in terms of total costs). 
This, however, easily enables you to define objective functions that maximizes profits.

<b>Thus, if you already use your own custom objective function, you need to manually adapt it and add penalties for unassigned jobs.</b>

<b> LIFO and FIFO CONSTRAINTS </b>

You can now retrieve additional information about related activities from JobInsertionContext (see https://github.com/jsprit/jsprit/issues/127).

If one deals with shipments then two activities will be inserted: pickupShipment and deliverShipment.

If you implement core.problem.constraint.SoftActivityContraint and core.problem.constraint.HardActivityConstraint and thus

<code>public double getCosts(JobInsertionContext iFacts, TourActivity prevAct, TourActivity newAct, TourActivity nextAct, double prevActDepTime);</code>

and

<code>public ConstraintsStatus fulfilled(JobInsertionContext iFacts, TourActivity prevAct, TourActivity newAct, TourActivity nextAct, double prevActDepTime);</code>
 
you can now retrieve additional information from iFacts. If newAct is "deliverShipment" then

<code>iFacts.getRelatedActivityContext();</code>

provides arrivalTime, endTime and potentialInsertionIndex of the related "pickupShipment" (see javadoc of ActivityContext).

This allows you to easily implement LIFO and FIFO constraints (since you now know where the pickup activity will be inserted).



------------------------------

<b>2014-08-20</b> jsprit has a mailing list (https://groups.google.com/group/jsprit-mailing-list)

------------------------------

<b>2014-08-15</b> [YourKit](http://www.yourkit.com/home/) supports jsprit with its full-featured Java Profiler. 

------------------------------

<b>2014-08-10</b> jsprit uses Travis for continuous integration (https://travis-ci.org/jsprit/jsprit) 

