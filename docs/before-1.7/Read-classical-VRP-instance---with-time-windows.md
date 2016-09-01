This example covers
- reading a classical VRP instance (here the Solomon instance C101), 
- plotting the problem,
- reading and running a predefined algorithm,
- plotting the solution.

Make sure, your pom is prepared (see [Add the latest snapshot to your pom](https://github.com/jsprit/jsprit/wiki/Add-latest-snapshot-to-your-pom)). Additionally, create an output folder in your project directory. Either do it manually or add the following lines to your code (even this obfuscates the code-example a bit):
<pre><code>File dir = new File("output");
// if the directory does not exist, create it
if (!dir.exists()){
	System.out.println("creating directory ./output");
	boolean result = dir.mkdir();  
	if(result) System.out.println("./output created");  
}
</code></pre>

Download the solomon problem instance (for instance [here](http://neo.lcc.uma.es/vrp/vrp-instances/capacitated-vrp-with-time-windows-instances/)) or download [C101_solomon.txt](https://github.com/jsprit/jsprit/tree/master/jsprit-examples/input). It is assumed you put the instance file into a folder called 'input'.

Read and build the problem:
<pre><code>/*
 * define problem-builder first
 */
VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();

/*
 * Read solomon instance with SolomonReader
 * Note that the reader assigns fixed costs of 100 to each vehicle used (even the original problem does not
 * exhibit any fixed cost components). Total costs should indicate then
 * nuOfVehicles * 100 + variable costs
 */
new SolomonReader(vrpBuilder).read("input/C101_solomon.txt");

/*
 * Build the problem. By default, transport costs are calculated as Euclidean distances.
 */
VehicleRoutingProblem vrp = vrpBuilder.build();
</code></pre>

Plot the problem to see how it looks like:
<pre><code>SolutionPlotter.plotVrpAsPNG(vrp, "output/solomon_C101.png", "C101");</code></pre>

It looks like [this](https://github.com/jsprit/misc-rep/raw/master/wiki-images/solomon_C101.png).

To solve it, define an algorithm. Here, it comes out-of-the-box. The SchrimpfFactory creates an algo which is an implemenation of [Schrimpf et al.](http://www.sciencedirect.com/science/article/pii/S0021999199964136). In this configuration, it is best suited to solve the VRP with time windows.

<pre><code>/*
* get the algorithm out-of-the-box. 
*/
VehicleRoutingAlgorithm algorithm = new SchrimpfFactory().createAlgorithm(problem);		

/*
* and search a solution which returns a collection of solution (here only one solution is in the collection)
*/
Collection<VehicleRoutingProblemSolution> solutions = algorithm.searchSolutions();
	
/*
 * use helper to get the best 
 */
VehicleRoutingProblemSolution bestSolution = Solutions.bestOf(solutions);
</code></pre>

Plot the solution now to analyse how it looks like:
<pre><code>SolutionPlotter.plotSolutionAsPNG(vrp, "output/solomon_C101_solution.png", "C101");</code></pre>

It looks like [this](https://github.com/jsprit/misc-rep/raw/master/wiki-images/solomon_C101_solution.png).

Get the entire code of this example [here](https://github.com/jsprit/jsprit/blob/v1.6/jsprit-examples/src/main/java/jsprit/examples/SolomonExample.java).
