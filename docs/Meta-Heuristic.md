#### The Meta-Heuristic
The idea of the meta-heuristic that is applied to solve the various vehicle routing problems 
with jsprit was developed by <a href="http://www.sciencedirect.com/science/article/pii/S0021999199964136" target="_blank">Schrimpf et al. (2000)</a>
who formulated the <em>ruin-and-recreate</em> principle. 
It is a large neighborhood search that combines elements of simulated annealing 
and threshold-accepting algorithms (Schrimpf et al. [2000, pg. 142]). 
Essentially, it works as follows: starting with an initial solution, it disintegrates 
parts of the solution leading to (i) a set of jobs that are not served by a vehicle anymore and to 
(ii) a partial solution containing all other jobs. Thus, this step is called <em>ruin</em> step. 
Based on the partial solution (ii) all jobs from (i) are re-integrated again, which is therefore referred 
to as <em>recreation</em> yielding to a new solution. If the new solution has a certain quality, 
it is accepted as new best solution, whereupon a new <em>ruin-and-recreate</em> iteration starts. 
These steps are repeated over and over again until a certain termination criterion is met 
(e.g. computation time, #iterations, etc.).

We extended the core algorithm described by [Schrimpf et al. (2000)](http://www.sciencedirect.com/science/article/pii/S0021999199964136) with strategies inspired by the great work of
[Pisinger and Ropke (2007)](http://www.sciencedirect.com/science/article/pii/S0305054805003023).

<b>Why this approach?</b>
* it is best suited for complex problems that have many constraints and a discontinue solution space (Schrimpf et al. [2000, pg. 142]),
* it is an all-purpose meta-heuristic that can be used to solve a number of classical VRP types,
* it can be computed concurrently in an intuitive way, 
* basic search strategies (or local moves) can be easily varied to small and large moves according to the complexity of the problem,
* it can generate whole new neighborhood structures,
* the number of search strategies can be kept low and thus 
* it is appealing simple in structure and comparably easy to understand and 
* there is a clear distinction between ruin and recreate which - we think - makes constraint checking much easier.
