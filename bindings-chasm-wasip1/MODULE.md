# Module bindings-chasm-wasip1

Wasi Preview 1 host functions for [Chasm] WebAssembly runtime.

[<img alt="Maven Central Version" src="https://img.shields.io/maven-central/v/at.released.weh/bindings-chasm-wasip1?style=flat-square">](https://central.sonatype.com/artifact/at.released.weh/bindings-chasm/overview)

## Usage

Use [ChasmWasiPreview1Installer](http://localhost:63342/wasi-emscripten-host/aggregate-documentation/build/dokka/html/bindings-chasm/at.released.weh.bindings.chasm/-chasm-host-function-installer/index.html)
to set up host functions.

```kotlin
const val INITIAL_MEMORY_SIZE_PAGES = 258U

val store: Store = store()

// Prepare Host memory
val memoryType = MemoryType(
    Limits(
        min = INITIAL_MEMORY_SIZE_PAGES,
        max = INITIAL_MEMORY_SIZE_PAGES,
    ),
)
val memory: Memory = memory(store, memoryType)

// Prepare WASI host imports
val chasmInstaller = ChasmWasiPreview1Installer(store) {
    memoryProvider = { memory }
}
val wasiHostFunctions = chasmInstaller.setupWasiPreview1HostFunctions()
val emscriptenInstaller = chasmInstaller.setupEmscriptenFunctions()

val hostImports: List<Import> = buildList {
    Import(
        moduleName = "env",
        entityName = "memory",
        value = memory,
    )
    addAll(emscriptenInstaller.emscriptenFunctions)
    addAll(wasiHostFunctions)
}

// Instantiate the WebAssembly module
val instance = module(
    bytes = helloWorldBytes,
).flatMap { module ->
    instance(store, module, hostImports)
}.fold(
    onSuccess = { it },
    onError = { throw WasmException("Can node instantiate WebAssembly binary: $it") },
)

// Finalize initialization after module instantiation
val emscriptenRuntime = emscriptenInstaller.finalize(instance)

// Initialize Emscripten runtime environment
emscriptenRuntime.initMainThread()

// Execute code
```

[Chasm]: https://github.com/CharlieTap/chasm
