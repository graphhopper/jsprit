# Migration guide

The new version of the Jsprit library made deep changes on the key structure. Although we made effort to keep as much as of these changes under the hood, there are some code breaking changes in the new version.

This guide helps you to migrate your code to the new version. The first part of the guide helps you make your code run again as small an effort and as quick as it is possible, without migrating to the new data structure. The second part gives you some hints, how to move your code and your data structure to meet the new version. Although you can get a running code without this migration, the legacy classes backing this partial solution are deprecated and are going to be removed in future versions.

## Chapter 1: The quick guide to make your code run again

To help the third party developers to quickly and painlessly migrate to the new version, the current version of the library contains several legacy classes. They has the same (or as close to the original as it was possible to achieve) API as the ones in the previous version. These classes are now marked as deprecated and mostly facades over the new structure. 

> **Although by completing these migration steps, you are likely to get a running code, keep in mind that these legacy implementations are in the library only temporally and are going to be removed in some future version.**

### Chapter 2: Prepare for the future

In this step, we give you guided help how to completely get rid of the legacy classes and move

### Capacity to SizeDimension

The `Capacity` class is renamed to `SizeDimension`. For backward compatibility, the `SizeDimension` class extends the now deprecated `Capacity` class. This let you use the `Capacity` class as variable type anywhere  the value is read out. Also, the `Capacity.Builder` creates a `SizeDimension` class under the hood, so when a `Capacity` object is created it is really a `SizeDimension`.

This makes this rename transparent as far as code correctness goes. However, the `Capacity` class may be removed in the future, so it is strongly recommended to rename all references to `Capacity` to `SizeDimension`.