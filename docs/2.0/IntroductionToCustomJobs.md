# Introduction to Custom Jobs

In the previous version of Jsprit, users had the choice to pick one of the four, predefined job types: Services, Pickups, Deliveries or Shipments. Life is much more complex and these limited palette of basic ingredients often proved to be not enough, forcing the users to make complex constraints. 
Internally, the jobs were broken down into steps called *activities*, but the parameters of these activities were configured and stored in the Job.

Jsprit 2 uses a completely different approach. Even if you are experienced with Jsprit, it is vital to understand the new concept.

What is a job? A sequence of tasks (*activities*) someone has to fulfill. In Jsprit 2, a `Job` is nothing more than a container which contains one or more `Activities`. The algorithm will ensure that these activities

* are executed on the same route
* either all or none of them is included in the solution
* their order is kept as they were defined.

The activities became the main building blocks of the problems. They have their own 

* capacity impact (size dimension change)
* time windows
* operation time
* location.

There are for type of abilities, differentiated by their impact on vehicle load:

| Type     | Capacity impact                          |
| -------- | ---------------------------------------- |
| Service  | No change in vehicle load.               |
| Pickup   | Some space is allocated in one or more dimensions. |
| Delivery | Some space is freed in one or more dimensions. |
| Exchange | Some dimensions may be increased other may be reduced: a combination of pickup and delivery. |

> *Note: This is only a convenient partitioning, because each other types could be interpreted as a special case of exchange.*

On the other hand, the user has the freedom to build up Jobs from these activities, combining any kind and any number of them. 

**<u>Example:</u>**

A classical case when there is a shipment from the warehouse to the customer, but when delivering the order, the vehicle has to pick up some backhaul cargo at the same time and take it back to the warehouse. (Such as empty crates.)

Earlier this required to define two shipments and ensure that (1) both jobs are done by the same vehicle; (2) the delivery of the first job is immediately preceding the pickup of the second. These rules had to be ensured by complex hard constraints and even the ruin strategy had to be altered.

With Jsprit 2, one can simply define a new `CustomJob` with three activities:

```java
new CustomJob.Builder(id)
  		.addPickup(...)
  		.addExchange(...)
  		.addDelivery(...)
  		.build();
```

All constraints a taken care by the algorithm.

> For convenience and backward compatibility, the old Job types (Service, Pickup, Delivery and Shipment) are kept, but most of their methods are deprecated. Because the vast structural change of the Job/Activity architecture, there are some legacy code breaking changes. See XXX for migration guide!