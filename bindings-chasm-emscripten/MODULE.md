# Module bindings-chasm-emscripten

Implementation of Emscripten host functions for the [Chasm] JVM WebAssembly runtime integration.

[<img alt="Maven Central Version" src="https://img.shields.io/maven-central/v/at.released.weh/bindings-chasm-emscripten?style=flat-square">](https://central.sonatype.com/artifact/at.released.weh/bindings-chasm/overview)

## Usage

Use [ChasmEmscriptenHostBuilder](https://weh.released.at/api/bindings-chasm-emscripten/at.released.weh.bindings.chasm/-chasm-emscripten-host-builder/index.html)
to set up host functions.

```kotlin
import at.released.weh.bindings.chasm.ChasmEmscriptenHostBuilder
import at.released.weh.host.EmbedderHost
import io.github.charlietap.chasm.embedding.instance
import io.github.charlietap.chasm.embedding.invoke
import io.github.charlietap.chasm.embedding.module
import io.github.charlietap.chasm.embedding.shapes.Import
import io.github.charlietap.chasm.embedding.shapes.Store
import io.github.charlietap.chasm.embedding.shapes.Value.Number.I32
import io.github.charlietap.chasm.embedding.shapes.flatMap
import io.github.charlietap.chasm.embedding.shapes.fold
import io.github.charlietap.chasm.embedding.store

// Create Host and run code
EmbedderHost {
    fileSystem {
        unrestricted = true
    }
}.use(::executeCode)

private fun executeCode(embedderHost: EmbedderHost) {
    val store: Store = store()

    // Prepare WASI and Emscripten host imports
    val chasmBuilder = ChasmEmscriptenHostBuilder(store) {
        this.host = embedderHost
    }
    val wasiHostFunctions = chasmBuilder.setupWasiPreview1HostFunctions()
    val emscriptenInstaller = chasmBuilder.setupEmscriptenFunctions()

    val hostImports: List<Import> = buildList {
        addAll(emscriptenInstaller.emscriptenFunctions)
        addAll(wasiHostFunctions)
    }

    // Instantiate the WebAssembly module
    val instance = module(wasmBinary).flatMap { module ->
        instance(store, module, hostImports)
    }.fold(
        onSuccess = { it },
        onError = { error("Can node instantiate WebAssembly binary: $it") },
    )

    // Finalize initialization after module instantiation
    val emscriptenRuntime = emscriptenInstaller.finalize(instance)

    // Initialize Emscripten runtime environment
    emscriptenRuntime.initMainThread()

    // Execute code
    invoke(
        store = store,
        instance = instance,
        name = "main",
        args = listOf(I32(0), I32(0)),
    ).fold(
        onSuccess = { it },
        onError = { error("main() failed") },
    )
}
```

[Chasm]: https://github.com/CharlieTap/chasm
