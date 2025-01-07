---
sidebar_label: 'Overview'
sidebar_position: 1
---

# WASI-Emscripten-Host

This Kotlin Multiplatform library provides an implementation of the of the host functions specified in [WASI Preview 1],
as well as an experimental implementation of the [Emscripten JS][Emscripten] environment host functions.  
It is designed to work with JVM/Multiplatform WebAssembly runtimes such as [GraalWASM][GraalWasm], [Chicory], and [Chasm].

The primary goal is to run WebAssembly binaries compiled for WASM WASI or for Emscripten in a JVM environment, 
without the need for a browser or JavaScript.

The library currently implements all non-deprecated functions of the WASI Preview 1 specification
and mostly passes the [WASI Testsuite]. For more details and specifics of the WASI Preview 1 implementation, refer 
to the  the [WASI Preview 1 Implementation](WASIP1.md).

A limited set of Emscripten environment functions is also implemented, sufficient to run a single-threaded 
version of SQLite compiled with Emscripten. It may also work in other cases. The description of the Emscripten
implementation and an example of compiling a C++ application to work with this library can be found on the
 [Emscripten environment](Emscripten.md).

Supported Kotlin targets: _JVM_ (based on NIO), _macosArm64_, macosX64_, _iosArm64_, _iosX64_, _iosSimulatorArm64_, _linuxX64_, _linuxArm64_, _mingwX64_.

## Usage

Refer to the sections on integration with runtimes:

* [Usage with Chasm](Integration/Chasm)
* [Usage with Chicory](Integration/Chicory.md)
* [Usage with GraalVM](Integration/GraalVM.md)

## Releases

The latest release is available on [Maven Central].

```kotlin
repositories {
    mavenCentral()
}
```

[Emscripten]: https://emscripten.org/
[Chasm]: https://github.com/CharlieTap/chasm
[Chicory]: https://github.com/dylibso/chicory
[GraalWasm]: https://www.graalvm.org/latest/reference-manual/wasm/
[Maven Central]: https://central.sonatype.com/artifact/at.released.weh/bindings-chasm-wasip1
[WASI Preview 1]: https://wasi.dev/
[WASI Testsuite]: https://github.com/WebAssembly/wasi-testsuite
