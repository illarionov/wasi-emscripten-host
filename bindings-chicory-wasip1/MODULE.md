# Module bindings-chicory

[Chicory] JVM WebAssembly runtime integration.

[<img alt="Maven Central Version" src="https://img.shields.io/maven-central/v/at.released.weh/bindings-chicory?style=flat-square">](https://central.sonatype.com/artifact/at.released.weh/bindings-chicory/overview)

* Targets: *JVM*, *Android API 31+*

## Usage

Use [ChasmHostFunctionInstaller](https://weh.released.at/api/bindings-chicory/at.released.weh.bindings.chicory/-chicory-host-function-installer/index.html)
to set up host functions.

```kotlin
// Prepare Host memory
val memory = HostMemory(
    /* moduleName = */ "env",
    /* fieldName = */ "memory",
    /* memory = */
    Memory(
        MemoryLimits(INITIAL_MEMORY_SIZE_PAGES),
    ),
)

// Prepare WASI and Emscripten host imports
val installer = ChicoryHostFunctionInstaller(
    memory = memory.memory(),
)
val wasiFunctions: List<HostFunction> = installer.setupWasiPreview1HostFunctions()
val emscriptenInstaller: ChicoryEmscriptenInstaller = installer.setupEmscriptenFunctions()
val hostImports = HostImports(
    /* functions = */ (emscriptenInstaller.emscriptenFunctions + wasiFunctions).toTypedArray(),
    /* globals = */ arrayOf<HostGlobal>(),
    /* memory = */ memory,
    /* tables = */ arrayOf<HostTable>(),
)

// Build Chicory Module
val module = Module
    .builder("helloworld.wasm")
    .withHostImports(hostImports)
    .withInitialize(true)
    .withStart(false)
    .build()

// Instantiate the WebAssembly module
val instance = module.instantiate()

// Finalize initialization after module instantiation
val emscriptenRuntime = emscriptenInstaller.finalize(instance)

// Initialize Emscripten runtime environment
emscriptenRuntime.initMainThread()

```

[Chicory]: https://github.com/dylibso/chicory
