# Migration checklist

### Task 1: Capacity to SizeDimension

**<u>Nature:</u>** 

`Capacity` became deprecated but kept, it is fully compatible with the `SizeDimension` class, which is a subclass of `Capacity`.

**<u>Decision:</u>** 

Make `Capacity` *deprecated*. Keep both classes.

**<u>State:</u>**

Ready.

**<u>Migration task:</u>**

None required. Optionally, but strongly recommended to replace all `Capacity` references by `SizeDimension`. (Only a search and replace.)

### Task 2: The Job interface

**<u>Nature:</u>** 

The `Job` interface was extended. This would break any third party implementation of this interface (or the extension of the `AbstractJob` abstract class).

**<u>Decision:</u>** 

There are several ways to overcome on this compatibility conflict:

1. Keep it as it is and force the implementators to extend its implementation or to migrate to `CustomJob`.
2. Restore the original interface and create an extended one as a descendant of this. The `AbstractJob` is the only one implementing this interface, so it can simply implement the extended one.

Option 1 is a compatibility breaker, but doesn't pollute the code unnecessary and urges the users to switch to new job structure. With Option 2, we are keeping compatibility, but introducing an unnecessary level in the class hierarchy and finding a good name for the extended interface would be hard, because this is only created for the sake of compatibility.

**<u>State:</u>**

(!) Waiting for decision.

**<u>Migration task:</u>**

*Write down detailed description of all mandatory and optional migration tasks.*

### Task 3: The old Job types

**<u>Nature:</u>** 

There are old job types (`Service`, `Pickup`, `Delivery` and `Shipment`) with deprecated methods. With the new `CustomJob` and the special functions (`addService`, `addPickup`, etc.) of its builder, these old job types are redundant and they are only special cases for the more general `CustomJob`. However, they are the central building classes for Jsprit, so keeping and supporting them in some way is vital for backward compatibility.

**<u>Decision:</u>** 

There are several ways to overcome on this compatibility conflict:

1. Remove these classes and guide the users to migrate to the methods in `CustomJob.Builder`.
2. There are already implementations for these classes in new version, but they are not compatible with the old once. Help the users to migrate to these classes.
3. Create façade classes. These classes offer the same interface as the old ones, but extends `CustomJob` and delegates the calls to the new structure.

The first lefts the least of the garbage in the code base, but seems to be too drastic and puts too much migration work on the old users. The second is the way how now the new version works, so here mostly documentation tasks are necessary. The third one would provide an *almost* 100% compatibility, but for a cost of one more redirection, which may have (although possibly minor) performance impact on Jsprit. The *almost* is there, because there may be a few method name collisions where both `CustomJob` and the old classes have the same method, but with different behavior.

My proposal is the 3rd one.

**<u>State:</u>**

(!) Waiting for decision.

**<u>Migration task:</u>**

*Write down detailed description of all mandatory and optional migration tasks.*





----

### Task X: 

**<u>Nature:</u>** 

*Write down the nature of the change and how it affects the compatibility.*

**<u>Decision:</u>** 

*Write down how we plan to solve the conflict.* 

**<u>State:</u>**

*Write down the state of the task.*

**<u>Migration task:</u>**

*Write down detailed description of all mandatory and optional migration tasks.*