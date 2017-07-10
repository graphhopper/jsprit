jsprit
======
[![Build Status](https://travis-ci.org/graphhopper/jsprit.svg?branch=master)](https://travis-ci.org/graphhopper/jsprit)

jsprit is a java based, open source toolkit for solving rich [Traveling Salesman Problems(TSP)](http://en.wikipedia.org/wiki/Travelling_salesman_problem") and [Vehicle Routing Problems(VRP)](http://neo.lcc.uma.es/vrp/vehicle-routing-problem/).
It is lightweight, flexible and easy-to-use, and based on a single all-purpose [meta-heuristic](../docs/Meta-Heuristic.md) currently solving

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
reading classical VRP instances to benchmark your algorithm. It is fit for change and extension due to a modular design and a comprehensive set of unit and integration-tests. [More features ...](https://github.com/graphhopper/jsprit/blob/master/docs/Features.textile)

## Getting Started with Documentation
Please visit [docs](https://github.com/graphhopper/jsprit/blob/master/docs/Home.md) to learn more.The best way to get to know jsprit is by looking at [code examples](https://github.com/graphhopper/jsprit/tree/master/jsprit-examples/src/main/java/com/graphhopper/jsprit/examples).

## Modules and Dependencies
Please read [Notice.md](https://github.com/graphhopper/jsprit/blob/master/NOTICE.md) to get to know the direct dependencies of each module.

## Whats New
jsprit develops fast. Look [here](https://github.com/jsprit/jsprit/blob/master/WHATS_NEW.md) to get to know whats new.

## License
This software is released under [Apache License v2](https://www.apache.org/licenses/LICENSE-2.0).

## Contribution

Any contribution is welcome. Feel free to improve jsprit and make pull requests. If you want to contribute to jsprit (which would be great), fork the project and build your fork, make changes, run your and jsprit's test cases and make a pull request (see [help.github.contribute](https://help.github.com/articles/fork-a-repo) or [stackoverflow.contribute](http://stackoverflow.com/questions/4384776/how-do-i-contribute-to-others-code-in-github) for details).

See who has contributed [here](https://github.com/jsprit/jsprit/blob/master/CONTRIBUTORS.md).

## Acknowledgement
Developing this would be much more difficult without the help of [these companies](https://github.com/graphhopper/jsprit/blob/master/docs/Acknowledgement.md).

## Contact

#### Mailing List:
In the [Graphhopper Forum ](https://discuss.graphhopper.com/) ([Also you can see the old mailing list](https://groups.google.com/group/jsprit-mailing-list)) you can discuss jsprit related issues and you will probably get answers to your questions.

#### Stackoverflow:
You can also use [stackoverflow](http://stackoverflow.com/questions/tagged/jsprit) to discuss your issues. Tag it with <em>jsprit</em> then it is easier to keep track of your topic.

#### Issue Tracker:
For bugs, feature requests or similar use the [issue tracker](https://github.com/jsprit/jsprit/issues).

#### Email:
If you cannot get help in the mailing list or you just do not want to discuss your topic publicly, [contact us via mail](https://graphhopper.com/#contact)


## About
The jsprit-project has been created by [Stefan Schröder](https://github.com/oblonski) and is maintained by [GraphHopper](https://graphhopper.com/). It is motivated by two issues.

First, you can find vehicle routing problems **everywhere** in the world of distributing and moving things and people. This probably explains why there is an almost endless list of papers and algorithms to tackle these problems. However, there are only [very few open source implementations](https://github.com/graphhopper/jsprit/blob/master/docs/Other-Projects.md) and even fewer projects that can deal with real world problems that usually have many side-constraints.

Second, it is motivated by my PhD-project at [KIT](http://www.kit.edu/english/index.php) where I apply vehicle routing algorithms to solve behavioural models of freight agents to assess (freight) transport policy measures.

It is mainly inspired by my research group at [KIT-ECON](http://netze.econ.kit.edu/21.php), and by a great open-source project called [MATSim](http://www.matsim.org) and its developers.

