# Module bindings-chicory-emscripten

Implementation of Emscripten host functions for the [Chicory] JVM WebAssembly runtime integration.

[<img alt="Maven Central Version" src="https://img.shields.io/maven-central/v/at.released.weh/bindings-chicory-emscripten?style=flat-square">](https://central.sonatype.com/artifact/at.released.weh/bindings-chicory/overview)

* Targets: *JVM*, *Android API 31+*

## Usage

Use [ChicoryEmscriptenHostInstaller](https://weh.released.at/api/bindings-chicory/at.released.weh.bindings.chicory/-chicory-emscripten-host-installer/index.html)
to set up host functions.

```kotlin
import at.released.weh.bindings.chicory.ChicoryEmscriptenHostInstaller
import at.released.weh.bindings.chicory.ChicoryEmscriptenHostInstaller.ChicoryEmscriptenSetupFinalizer
import at.released.weh.host.EmbedderHost
import com.dylibso.chicory.runtime.HostFunction
import com.dylibso.chicory.runtime.ImportValues
import com.dylibso.chicory.runtime.Instance
import com.dylibso.chicory.wasm.Parser

// Create Host and run code
EmbedderHost {
    fileSystem {
        unrestricted = true
    }
}.use(::executeCode)

private fun executeCode(embedderHost: EmbedderHost) {
    // Prepare WASI and Emscripten host imports
    val installer = ChicoryEmscriptenHostInstaller {
        host = embedderHost
    }

    val wasiFunctions: List<HostFunction> = installer.setupWasiPreview1HostFunctions()
    val emscriptenFinalizer: ChicoryEmscriptenSetupFinalizer = installer.setupEmscriptenFunctions()

    val hostImports = ImportValues.builder()
        .withFunctions(emscriptenFinalizer.emscriptenFunctions + wasiFunctions)
        .build()

    // Load WebAssembly binary
    val wasmModule = Parser.parse(/* … */)

    // Instantiate the WebAssembly module
    val instance = Instance
        .builder(wasmModule)
        .withImportValues(hostImports)
        .withInitialize(true)
        .withStart(false)
        .build()

    // Finalize initialization after module instantiation
    val emscriptenRuntime = emscriptenFinalizer.finalize(instance)

    // Initialize Emscripten runtime environment
    emscriptenRuntime.initMainThread()

    // Execute code
    instance.export("main").apply(
        /* argc */ 0,
        /* argv */ 0,
    )[0]
}
```

[Chicory]: https://github.com/dylibso/chicory
