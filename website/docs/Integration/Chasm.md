---
sidebar_label: 'Chasm'
sidebar_position: 1
description: 'Implementation of WASI Preview 1 and Emscripten host functions for Chasm'
---

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

# Chasm Integration

[Chasm] is an experimental WebAssembly runtime built on Kotlin Multiplatform.
It supports Android API 26+, JVM JDK 17+, and a variety of multiplatform targets.

This integration is compatible with version [0.9.4][Chasm_version] of Chasm.

## Wasi Preview 1 Bindings Integration

Check [WASI Preview 1](../WASIP1) to see the current limitations of the WASI P1 implementation.

### Installation

Add the required dependencies:

```kotlin
sourceSets {
    commonMain.dependencies {
        implementation("io.github.charlietap.chasm:chasm:0.9.4")
        implementation("at.released.weh:bindings-chasm-wasip1:0.2")
    }
}
```

### Usage

Below is an example demonstrating the execution of **helloworld.wasm**, build using Emscripten with the `STANDALONE_WASM` flag.

```kotlin
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
import java.io.InputStream

fun main() {
    // Load WebAssembly binary
    val helloWorldBytes = checkNotNull(
        Thread.currentThread().contextClassLoader.getResource("helloworld_wasi.wasm"),
    ).openStream().use(InputStream::readAllBytes)

    // Create Host and run code
    EmbedderHost {
        fileSystem {
            addPreopenedDirectory(".", "/data")
        }
    }.use {
        executeCode(it, helloWorldBytes)
    }
}

fun executeCode(embedderHost: EmbedderHost, wasmBinary: ByteArray): Int {
    val store: Store = store()

    // Prepare WASI  host imports
    val wasiImports: List<Import> = ChasmWasiPreview1Builder(store) {
        host = embedderHost
    }.build()

    // Instantiate the WebAssembly module
    val instance = module(wasmBinary)
        .flatMap { module -> instance(store, module, wasiImports) }
        .fold(
            onSuccess = { it },
            onError = { throw WasmException("Can node instantiate WebAssembly binary: $it") },
        )

    // Execute code
    invoke(store, instance, "_start").fold(
        onSuccess = { "Success" },
        onError = { executionError -> executionError.error },
    )

    return 0
}

class WasmException(message: String) : RuntimeException(message)
```

## Emscripten bindings integration

### Installation

Add the required dependencies:

```kotlin
sourceSets {
    commonMain.dependencies {
        implementation("io.github.charlietap.chasm:chasm:0.9.4")
        implementation("at.released.weh:bindings-chasm-emscripten:0.2")
    }
}
```

### Usage

Below is an example demonstrating the execution of **helloworld.wasm**, prepared
in the "[Emscripten Example](../Emscripten#example)".

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
import java.io.InputStream

fun main() {
    // Create Host and run code
    EmbedderHost {
        fileSystem {
            unrestricted = true
        }
    }.use(::executeCode)
}

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

    // Load WebAssembly binary
    val helloWorldBytes = checkNotNull(Thread.currentThread().contextClassLoader.getResource("helloworld.wasm"))
        .openStream()
        .use(InputStream::readAllBytes)

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
    invoke(
        store = store,
        instance = instance,
        name = "main",
        args = listOf(
            // argc
            I32(0),
            // argv
            I32(0),
        ),
    ).fold(
        onSuccess = { it },
        onError = { throw WasmException("main() failed") },
    )
}

class WasmException(message: String) : RuntimeException(message)
```

## Other samples

* https://github.com/illarionov/wasi-emscripten-host/tree/main/samples  
  This directory in the the source repository contains more examples of using the library.
* https://github.com/illarionov/wehdemo  
  This example showcases how to execute a Kotlin/Wasm-WASI binary in a Kotlin Multiplatform project.

[Chasm]: https://github.com/CharlieTap/chasm
[Chasm_version]: https://github.com/CharlieTap/chasm/releases/tag/0.9.4
[Samples]: https://github.com/illarionov/wasi-emscripten-host/tree/main/samples
