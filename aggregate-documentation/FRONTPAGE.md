# Wasi-emscripten-host

Kotlin Multiplatform implementation of WebAssembly host functions from WASI Preview 1 system interface and from
[Emscripten] environment, that can be used with WebAssembly runtimes: [GraalVM][GraalWasm], [Chicory], or [Chasm].

Implemented a limited subset of Emscripten host functions needed to run a single-threaded version of SQLite compiled
with Emscripten on a JVM/Kotlin Multiplatform runtime. It may also work in some other cases too.
The WASI/Emscripten filesystem is also (partially) covered.

Source: [https://github.com/illarionov/wasi-emscripten-host](https://github.com/illarionov/wasi-emscripten-host)

[Emscripten]: https://emscripten.org/
[Chasm]: https://github.com/CharlieTap/chasm
[Chicory]: https://github.com/dylibso/chicory
[GraalWasm]: https://www.graalvm.org/latest/reference-manual/wasm/
