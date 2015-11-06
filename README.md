jsprit
======
[![Build Status](https://travis-ci.org/jsprit/jsprit.svg?branch=master)](https://travis-ci.org/jsprit/jsprit)

jsprit is a java based, open source toolkit for solving rich <a href="http://en.wikipedia.org/wiki/Travelling_salesman_problem" target="_blank">traveling salesman</a> (TSP) and <a href="http://neo.lcc.uma.es/vrp/vehicle-routing-problem/" target="_blank">vehicle routing problems</a> (VRP). 
It is lightweight, flexible and easy-to-use, and based on a single all-purpose <a href="https://github.com/jsprit/jsprit/wiki/Meta-Heuristic" target="_blank">meta-heuristic</a> currently solving 
- Capacitated VRP
- Multiple Depot VRP
- VRP with Time Windows
- VRP with Backhauls
- VRP with Pickups and Deliveries
- VRP with Heterogeneous Fleet
- Time-dependent VRP
- Traveling Salesman Problem
- Dial-a-Ride Problem
- Various combination of these types

Setting up the problem, defining additional constraints, modifying the algorithms and visualising the discovered solutions is as easy and handy as 
reading classical VRP instances to benchmark your algorithm. It is fit for change and extension due to a modular design and a comprehensive set of unit and integration-tests. [More features ...](https://github.com/jsprit/jsprit/wiki/features)

##[Whats New](https://github.com/jsprit/jsprit/blob/master/WHATS_NEW.md)

##Documentation

Please visit [jsprit-wiki](https://github.com/jsprit/jsprit/wiki) to learn more.

##License
This software is released under [LGPL](http://opensource.org/licenses/LGPL-3.0).

##[Get Started](https://github.com/jsprit/jsprit/wiki/Getting-Started)

##[Contributors](https://github.com/jsprit/jsprit/graphs/contributors)
[Pierre-David Bélanger](https://github.com/pierredavidbelanger)<br>
[Abraham Gausachs](https://github.com/agausachs)<br>
[Subhamay Das](http://www.linkedin.com/profile/view?id=10203174)<br>
[Philip Welch](http://www.opendoorlogistics.com)

##[Acknowledgement](https://github.com/jsprit/jsprit/wiki/Acknowledgement)

##Contact

####Mailing List: 
In the [mailing list](https://discuss.graphhopper.com/) ([old mailing list](https://groups.google.com/group/jsprit-mailing-list)) you can discuss jsprit related issues and you will probably get answers to your questions.

####Stackoverflow:
You can also use [stackoverflow](http://stackoverflow.com/questions/tagged/jsprit) to discuss your issues. Tag it with <em>jsprit</em> then it is easier to keep track of your topic.

####Issue Tracker:
For bugs, feature requests or similar use the [issue tracker](https://github.com/jsprit/jsprit/issues).

####Email: 
If you cannot get help in the mailing list or you just do not want to discuss your topic publicly, send an email to:

info@jspr.it


##About
The jsprit-project is created and maintained by [Stefan Schröder](https://github.com/oblonski). It is motivated by two issues. 

First, you can find vehicle routing problems **everywhere** in the world of distributing and moving things and people. This probably explains why there is an almost endless list of papers and algorithms to tackle these problems. However there are only [very few open source implementations](https://github.com/jsprit/jsprit/wiki/Other-Projects) and even fewer projects that can deal with real world problems that usually have many side-constraints.

Second, it is motivated by my PhD-project at [KIT](http://www.kit.edu/english/index.php) where I apply vehicle routing algorithms to solve behavioural models of freight agents to assess (freight) transport policy measures. 

It is mainly inspired by my research group at [KIT-ECON](http://netze.econ.kit.edu/21.php), and by a great open-source project called [MATSim](http://www.matsim.org) and its developers.

