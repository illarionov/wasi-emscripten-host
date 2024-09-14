# Wasi-emscripten-host

Wasi-emscripten-host is a Kotlin Multiplatform library that implements the WebAssembly host functions of the 
[Emscripten] environment, and some functions defined in the WASI Preview 1 specification.

It is designed to execute WebAssembly binaries compiled using the Emscripten toolchain on JVM/Multiplatform WebAssembly 
runtimes such as [GraalVM][GraalWasm], [Chicory], and [Chasm].

For more information, visit the project website: [weh.released.at](https://weh.released.at)

[Emscripten]: https://emscripten.org/
[Chasm]: https://github.com/CharlieTap/chasm
[Chicory]: https://github.com/dylibso/chicory
[GraalWasm]: https://www.graalvm.org/latest/reference-manual/wasm/

## License

These services are licensed under Apache 2.0 License. Authors and contributors are listed in the
[Authors](AUTHORS) file.

```
Copyright 2024 wasi-emscripten-host project authors and contributors.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
