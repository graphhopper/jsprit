# Migration guide

The new version of the Jsprit library made deep changes on the key structure. Although we made effort to keep as much as of these changes under the hood, there are some code breaking changes in the new version.

This guide helps you to migrate your code to the new version. The first part of the guide helps you make your code run again as small an effort and as quick as it is possible, without migrating to the new data structure. The second part gives you some hints, how to move your code and your data structure to meet the new version. Although you can get a running code without this migration, the legacy classes backing this partial solution are deprecated and are going to be removed in future versions.

## Chapter 1: The quick guide to make your code run again

To help the third party developers to quickly and painlessly migrate to the new version, the current version of the library contains several legacy classes. They has the same (or as close to the original as it was possible to achieve) API as the ones in the previous version. These classes are now marked as deprecated and mostly facades over the new structure. 

> **Although by completing these migration steps, you are likely to get a running code, keep in mind that these legacy implementations are in the library only temporally and are going to be removed in some future version.**

### Using the new structure for own job implementations

If you defined your own implementation of Job, you may run into a few incompatibilities. Before these problems are solved, you have to choose which path you take.

If the reason of your own job implementation is to create jobs with more activities, the new `CustomJob` may render your class deprecated and you can now use the `CustomJob` instead. It brings benefits of being general, and you don't have to create the constraints to keep the activities together (on the same route and either all of them or none). 

If your reason to extends any of the old job types was to add user data to it, it is now better to use the `userData` field of the `CustomJob`.

If, after taking account all the above, you still can't avoid to use your implementation, you have to be aware the structural changes and make your implementation compatible with it. Because there is no defined way how your implementation extends the API, it is impossible to give a step by step guide. However, here are the most important changes your implementation must follow:

- The activities now have fixed set of types (Service, Pickup, Exchange, Delivery) and you have to map your activities to these.
- Some of the parameters which was on job level are moved to activity level, because they are associated to them: time windows, operation times, size requirements and changes, location.
- The AbstractJob abstract class is extended with some new abstract methods which should be implemented in your class.
- The builder mechanism is made inheritance friendly and it is recommended to migrate your one to it. (See the JavaDoc for details!)



## Chapter 2: Prepare for the future

In this step, we give you guided help how to completely get rid of the legacy classes and move

### Capacity to SizeDimension

The `Capacity` class is renamed to `SizeDimension`. For backward compatibility, the `SizeDimension` class extends the now deprecated `Capacity` class. This let you use the `Capacity` class as variable type anywhere  the value is read out. Also, the `Capacity.Builder` creates a `SizeDimension` class under the hood, so when a `Capacity` object is created it is really a `SizeDimension`.

This makes this rename transparent as far as code correctness goes. However, the `Capacity` class may be removed in the future, so it is strongly recommended to rename all references to `Capacity` to `SizeDimension`.

### Using CustomJob instead of legacy Job types

The old job types (`Service`, `Pickup`, `Delivery`, `Shipment`) are obsolete. However, they can easily be replaced with the new `CustomJob`, by using its Builder methods. 

#### Transforming single-activity jobs

The `Service`, `Pickup`, `Delivery` jobs contain only one activity. They can be replaced by the corresponding addXXX() methods (XXX stands for the name of the old job) in `CustomJob.Builder`. 

These methods comes with four different flavors: 

```
addService(Location location)
addService(Location location, SizeDimension size)
addService(Location location, SizeDimension size, double operationTime)
addService(Location location, SizeDimension size, double operationTime, TimeWindow tw)
```

These methods let's you create jobs with a location, size, operation time and a single time window.

**<u>Example 1:</u>**

If you have a Service declaration:

```java
Service s1 = new Service.Builder("s1").setLocation(Location.newInstance(10, 0)).build();
```

In this example, only the location is set, so you can replace it to the following code snippet:

```java
 CustomJob s1 = new CustomJob.Builder("s1")
            .addService(Location.newInstance(10, 0))
            .build();
```

**<u>Example 2:</u>**

When you have to set the time window, but neither the size or the operation time, there are common defaults for these values to use. This code

```java
Service service = new Service.Builder("s").setLocation(Location.newInstance(20, 0))
                        .setTimeWindow(TimeWindow.newInstance(40, 50)).build();
```

may be converted to 

```java
    CustomJob service = new CustomJob.Builder("s")
            .addService(Location.newInstance(20, 0), SizeDimension.EMPTY, 0,
                TimeWindow.newInstance(40, 50))
            .build();
```
**<u>Example 3:</u>**

When you need even more than these convenient methods offer (more time windows, name the activities, skills), you have to do some additional work. First you have to create a `BuilderActivityInfo`:

```
BuilderActivityInfo activityInfo = new BuilderActivityInfo(ActivityType.SERVICE,
                Location.newInstance(20, 0));
```

Then set the required values on it: 

```java
activityInfo.withName("activity name");
activityInfo.withOperationTime(10);
activityInfo.withSize(SizeDimension.Builder.newInstance()
		.addDimension(0, 1)
		.addDimension(1, 2)
		.build());
activityInfo.withTimeWindows(TimeWindow.newInstance(40, 50), TimeWindow.newInstance(70, 80));
```

Finally, you can configure the CustomJob.Builder and create the job:

```java
CustomJob.Builder customJobBuilder = new CustomJob.Builder("id");
        customJobBuilder.addActivity(activityInfo)
        .addAllRequiredSkills(Skills.Builder.newInstance().addSkill("skill").build())
        .setPriority(5)
        .build();
```