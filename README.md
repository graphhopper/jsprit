jsprit
======
jsprit is a java based, open source toolkit for solving a set of well-known <a href="http://neo.lcc.uma.es/vrp/vehicle-routing-problem/" target="_blank">vehicle routing problems</a> (VRP). 
It is lightweight and easy-to-use, and based on heuristics currently solving 
- <a href="http://neo.lcc.uma.es/vrp/vrp-flavors/capacitated-vrp/" target="_blank">Capacitated VRP</a>
- <a href="http://neo.lcc.uma.es/vrp/vrp-flavors/multiple-depot-vrp/" target="_blank">Multiple Depot VRP</a>
- <a href="http://neo.lcc.uma.es/vrp/vrp-flavors/vrp-with-time-windows" target="_blank">VRP with Time Windows</a>
- <a href="http://neo.lcc.uma.es/vrp/vrp-flavors/vrp-with-backhauls/" target="_blank">VRP with Backhauls</a>
- <a href="http://neo.lcc.uma.es/vrp/vrp-flavors/vrp-with-pick-up-and-delivering/" target="_blank">VRP with Pickups and Deliveries (all Pickups and Deliveries are Depot-bound)</a>
- VRP with Heterogeneous Fleet
- Time-dependent VRP
- Various combination of these types

Setting up the problem, modifying the algorithms and visualising the discovered solutions is as easy and handy as 
reading classical VRP instances to benchmark your algorithm.

Additionally, jsprit can be used along with <a href="http://www.matsim.org" target="blank_">MATSim</a> 
to solve the above problem-types in real networks (i.e. without preprocessing transport times and costs). A variety of least cost path algorithms such as Dijkstra and A*
can be used, and a dynamic and interactive visualiser greatly enhances the analysis.

##In Development
- continues improvement of code, handling and performance
- stable API to easily build algorithms from scratch

##Documentation

Please visit [jsprit-wiki](https://github.com/jsprit/jsprit/wiki) to learn more.

##License
This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

For more information, please visit [GNU Public License](http://opensource.org/licenses/GPL-2.0).

##Getting started in 2 seconds

[Add the latest snapshot to your pom](https://github.com/jsprit/jsprit/wiki/Add-latest-snapshot-to-your-pom).

##About
The jsprit-project is created and maintained by Stefan Schröder. It is motivated by two issues. 

First, there is an almost endless list of papers and algorithms to tackle vehicle routing problems, **BUT** there are (as far as I know) only a [very few open source implementations](https://github.com/jsprit/jsprit/wiki/Other-Projects) of one of these thousands algorithms. 

Second, it is motivated by my PhD-project at [KIT](http://www.kit.edu/english/index.php) where I apply vehicle routing algorithms to solve behavioural models of freight agents to assess (freight) transport policy measures. 

It is mainly inspired by my research group at [KIT-ECON](http://netze.econ.kit.edu/21.php), and by an awesome open-source project called [MATSim](www.matsim.org) and its developers.

If you have questions or if you use jsprit, it would be great you give feedback and let me know your experience:

Email: jsprit.vehicle.routing@gmail.com

[![](https://cruel-carlota.pagodabox.com/ba53806a8cc8ff439c1a51d152245dee "githalytics.com")](http://githalytics.com/jsprit/jsprit)
