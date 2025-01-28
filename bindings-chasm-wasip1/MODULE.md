# Module bindings-chasm-wasip1

Implementation of WASI Preview 1 host functions for [Chasm] WebAssembly runtime.

[<img alt="Maven Central Version" src="https://img.shields.io/maven-central/v/at.released.weh/bindings-chasm-wasip1?style=flat-square">](https://central.sonatype.com/artifact/at.released.weh/bindings-chasm-wasip1/overview)

## Usage

Use [ChasmWasiPreview1Builder](https://weh.released.at/api/bindings-chasm-wasip1/at.released.weh.bindings.chasm.wasip1/-chasm-wasi-preview1-builder/index.html)
to set up host functions.

```kotlin
import at.released.weh.bindings.chasm.exception.ProcExitException
import at.released.weh.bindings.chasm.wasip1.ChasmWasiPreview1Builder
import at.released.weh.host.EmbedderHost
import io.github.charlietap.chasm.embedding.instance
import io.github.charlietap.chasm.embedding.invoke
import io.github.charlietap.chasm.embedding.module
import io.github.charlietap.chasm.embedding.shapes.Import
import io.github.charlietap.chasm.embedding.shapes.Store
import io.github.charlietap.chasm.embedding.shapes.flatMap
import io.github.charlietap.chasm.embedding.shapes.fold
import io.github.charlietap.chasm.embedding.store

// Create Host and run code
EmbedderHost {
    fileSystem {
        addPreopenedDirectory(".", "/data")
    }
}.use {
    executeCode(it, wasmBinary)
}

fun executeCode(embedderHost: EmbedderHost, wasmBinary: ByteArray): Int {
    val store: Store = store()

    // Prepare WASI host imports
    val wasiImports: List<Import> = ChasmWasiPreview1Builder(store) {
        host = embedderHost
    }.build()

    // Instantiate the WebAssembly module
    val instance = module(wasmBinary)
        .flatMap { module -> instance(store, module, wasiImports) }
        .fold(
            onSuccess = { it },
            onError = { error("Can node instantiate WebAssembly binary: $it") },
        )

    // Execute code
    try {
        invoke(store, instance, "_start").fold(
            onSuccess = { it },
            onError = { error("main() failed") },
        )
    } catch (pre: ProcExitException) {
        return pre.exitCode
    }
    return 0
}
```

[Chasm]: https://github.com/CharlieTap/chasm
