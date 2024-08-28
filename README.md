# Wasi-emscripten-host

Kotlin Multiplatform implementation of WebAssembly host functions from WASI Preview 1 system interface and from
[Emscripten] environment, that can be used with WebAssembly runtimes: [GraalVM][GraalWasm], [Chicory], or [Chasm].

Implemented a limited subset of Emscripten host functions needed to run a single-threaded version of SQLite compiled
with Emscripten on a JVM/Kotlin Multiplatform runtime. It may also work in some other cases too.
The WASI/Emscripten filesystem is also (partially) covered.

See the related projects: 
* [wasm-sqlite-driver-binary] for binaries SQLite which are the main targets
* [wasm-sqlite-open-helper] where this library is used

## Contributing

Any type of contributions are welcome. Please see the [contribution guide](CONTRIBUTING.md).

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

[Emscripten]: https://emscripten.org/
[Chasm]: https://github.com/CharlieTap/chasm
[Chicory]: https://github.com/dylibso/chicory
[GraalWasm]: https://www.graalvm.org/latest/reference-manual/wasm/
[wasm-sqlite-driver-binary]: https://github.com/illarionov/wasm-sqlite-driver-binary
[wasm-sqlite-open-helper]: https://github.com/illarionov/wasm-sqlite-open-helper
