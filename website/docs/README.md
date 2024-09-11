---
sidebar_label: 'Overview'
sidebar_position: 1
---

# Wasi Emscripten Host

Kotlin Multiplatform implementation of WebAssembly host functions from the [Emscripten] environment and of the 
WASI Preview 1 system interface, that can be used with WebAssembly runtimes: [GraalVM][GraalWasm], [Chicory], or [Chasm].

Implemented a limited subset of Emscripten host functions needed to run a single-threaded version of SQLite compiled
with Emscripten on a JVM/Kotlin Multiplatform runtime. It may also work in some other cases too.
The WASI/Emscripten filesystem is also (partially) covered.

See the related projects:

* [wasm-sqlite-driver-binary] for binaries SQLite which are the main targets
* [wasm-sqlite-open-helper] where this library is used

[Emscripten]: https://emscripten.org/
[Chasm]: https://github.com/CharlieTap/chasm
[Chicory]: https://github.com/dylibso/chicory
[GraalWasm]: https://www.graalvm.org/latest/reference-manual/wasm/
[wasm-sqlite-driver-binary]: https://github.com/illarionov/wasm-sqlite-driver-binary
[wasm-sqlite-open-helper]: https://github.com/illarionov/wasm-sqlite-open-helper
