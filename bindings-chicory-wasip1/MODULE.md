# Module bindings-chicory-wasip1

Implementation of WASI Preview 1 host functions for the [Chicory] JVM WebAssembly runtime.

[<img alt="Maven Central Version" src="https://img.shields.io/maven-central/v/at.released.weh/bindings-chicory-wasip1?style=flat-square">](https://central.sonatype.com/artifact/at.released.weh/bindings-chicory-wasip1/overview)

* Targets: *JVM*, *Android API 31+*

## Usage

Use [ChasmWasiPreview1Builder](https://weh.released.at/api/bindings-chicory-wasip1/at.released.weh.bindings.chicory.wasip1/-chicory-wasi-preview1-builder/index.html)
to set up host functions.

```kotlin
import at.released.weh.bindings.chicory.exception.ProcExitException
import at.released.weh.bindings.chicory.wasip1.ChicoryWasiPreview1Builder
import at.released.weh.host.EmbedderHost
import com.dylibso.chicory.runtime.HostFunction
import com.dylibso.chicory.runtime.ImportValues
import com.dylibso.chicory.runtime.Instance
import com.dylibso.chicory.wasm.Parser
import com.dylibso.chicory.wasm.WasmModule

// Create Host and run code
EmbedderHost {
    fileSystem {
        addPreopenedDirectory(".", "/data")
    }
}.use { executeCode(it) }

private fun executeCode(embedderHost: EmbedderHost) {
    // Prepare WASI Preview 1 host imports
    val wasiImports: List<HostFunction> = ChicoryWasiPreview1Builder {
        host = embedderHost
    }.build()
    val hostImports = ImportValues.builder().withFunctions(wasiImports).build()

    // Instantiate the WebAssembly module
    val instance = Instance
        .builder(wasmModule)
        .withImportValues(hostImports)
        .withInitialize(true)
        .withStart(false)
        .build()

    // Execute code
    try {
        instance.export("_start").apply()
    } catch (pre: ProcExitException) {
        // Handle exception depending on pre.exitCode
    }
}
```

[Chicory]: https://github.com/dylibso/chicory
