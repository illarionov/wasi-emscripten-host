# Module aggregate-distribution

Auxiliary module for aggregating and packing a snapshot of a local maven repository for publishing on Github Releases.

### Tasks

```kotlin
./gradlew aggregate-distribution:foldDistribution
```

This command packs a snapshot of the local maven repository into `aggregate-distribution/build/distribution`.
