The entire code of more advanced examples dealing with VRP with backhauls can be found here:
- <a href="https://github.com/jsprit/jsprit/blob/v1.4/jsprit-examples/src/main/java/jsprit/examples/VRPWithBackhaulsExample2.java" target="blank_">Example 1</a> which is an adopted problem from Christophides vrpnc1 with random pickups and deliveries and a vehicle capacity of 50
- <a href="https://github.com/jsprit/jsprit/blob/v1.4/jsprit-examples/src/main/java/jsprit/examples/VRPWithBackhaulsExample.java" target="blank_">Example 2</a> which is an adopted problem from Solomon R101 with random pickups and deliveries with time-windows yielding to the results below. Additionally, the impact of the backhaul-constraint and time-windows are illustrated.

R101 without time-windows and without backhaul-constraint:
<img src="https://github.com/jsprit/misc-rep/raw/master/wiki-images/pd_solomon_r101_withoutTWs_solution.png">

The first customer in a route is marked with a red circle to indicate the orientation of the route. The labels represent the size of the pickup and delivery respectively. 

R101 without time-windows and <strong>with</strong> backhaul-constraint:
<img src="https://github.com/jsprit/misc-rep/raw/master/wiki-images/vrpwbh_solomon_r101_withoutTWs_solution.png">

R101 <strong>with</strong> time-windows and <strong>with</strong> backhaul-constraint:
<img src="https://github.com/jsprit/misc-rep/raw/master/wiki-images/vrpwbh_solomon_r101_solution.png">

