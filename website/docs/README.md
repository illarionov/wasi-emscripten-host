---
sidebar_label: 'Overview'
sidebar_position: 1
---

# Wasi Emscripten Host

This Kotlin Multiplatform library provides an experimental implementation of WebAssembly host functions from the
[Emscripten JS][Emscripten] environment, and some functions defined in the WASI Preview 1 specification.  
It is designed to work with JVM/Multiplatform WebAssembly runtimes such as [GraalVM][GraalWasm], [Chicory], and [Chasm].

The primary goal is to run WebAssembly binaries compiled with Emscripten in a JVM environment, without the need for a
browser or JavaScript.

Currently, the library implements a limited subset of host functions needed to run a single-threaded 
version of SQLite compiled with Emscripten. It may also work in other cases.
The WASI/Emscripten filesystem is partially implemented.

## Usage

Refer to the sections on integration with runtimes:

* [Usage with GraalVM](Integration/GraalVM.md)
* [Usage with Chicory](Integration/Chicory.md)
* [Usage with Chasm](Integration/Chasm)

## Releases

The latest release is available on [Maven Central].

```kotlin
repositories {
    mavenCentral()
}
```

Snapshot versions may be published to an own repository:

```kotlin
pluginManagement {
    repositories {
        maven {
            url = uri("https://maven.pixnews.ru")
            mavenContent {
                includeGroup("at.released.weh")
            }
        }
    }
}
```

## Status and Future Plans

The project is likely to be frozen. However, if there is interest or demand, we may implement the full set of
WASI functions. 

## Related projects

Here are some related projects:

* [wasm-sqlite-driver-binary] SQLite binaries, the primary target for this library.
* [wasm-sqlite-open-helper] Implementation of SQLiteDriver and SupportSQLiteOpenHelper.Factory based on SQLite 
compiled for Wasm. This is a project where this library is used.

[Emscripten]: https://emscripten.org/
[Chasm]: https://github.com/CharlieTap/chasm
[Chicory]: https://github.com/dylibso/chicory
[GraalWasm]: https://www.graalvm.org/latest/reference-manual/wasm/
[Maven Central]: https://central.sonatype.com/artifact/at.released.weh/bindings-graalvm240
[wasm-sqlite-driver-binary]: https://github.com/illarionov/wasm-sqlite-driver-binary
[wasm-sqlite-open-helper]: https://github.com/illarionov/wasm-sqlite-open-helper
