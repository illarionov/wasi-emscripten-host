---
sidebar_label: 'Chasm'
sidebar_position: 3
description: 'Implementation of Emscripten host functions for Chasm'
---

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

# Chasm Integration

[Chasm] is an experimental WebAssembly runtime built on Kotlin Multiplatform.
It supports Android API 26+, JVM JDK 17+, and a variety of multiplatform targets.

The runtime is actively developed, with frequent changes to its public interfaces.
Additionally, we rely on some internal APIs.
Therefore, this integration is compatible with version [0.9.0][Chasm_version] of Chasm.

## Installation

Add the required dependencies:

```kotlin
sourceSets {
    commonMain.dependencies {
        implementation("io.github.charlietap.chasm:chasm:0.9.0")
        implementation("at.released.weh:bindings-chasm:0.1-alpha01")
    }
}
```

## Usage

Below is an example demonstrating the execution of **helloworld.wasm**, prepared
in the "[Emscripten Example](../Emscripten#example)".

```kotlin
import at.released.weh.bindings.chasm.ChasmHostFunctionInstaller
import io.github.charlietap.chasm.embedding.instance
import io.github.charlietap.chasm.embedding.invoke
import io.github.charlietap.chasm.embedding.memory
import io.github.charlietap.chasm.embedding.module
import io.github.charlietap.chasm.embedding.shapes.Import
import io.github.charlietap.chasm.embedding.shapes.Limits
import io.github.charlietap.chasm.embedding.shapes.Memory
import io.github.charlietap.chasm.embedding.shapes.MemoryType
import io.github.charlietap.chasm.embedding.shapes.Store
import io.github.charlietap.chasm.embedding.shapes.Value.Number.I32
import io.github.charlietap.chasm.embedding.shapes.flatMap
import io.github.charlietap.chasm.embedding.shapes.fold
import io.github.charlietap.chasm.embedding.store

// You can use `wasm-objdump -x helloworld.wasm -j Memory` to get the memory limits 
// declared in the WebAssembly binary.
const val INITIAL_MEMORY_SIZE_PAGES = 258U

fun main() {
    val store: Store = store()

    // Prepare Host memory
    val memoryType = MemoryType(
        Limits(
            min = INITIAL_MEMORY_SIZE_PAGES,
            max = INITIAL_MEMORY_SIZE_PAGES,
        ),
    )
    val memory: Memory = memory(store, memoryType)

    // Prepare WASI and Emscripten host imports
    val chasmInstaller = ChasmHostFunctionInstaller(store) {
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

    // Load WebAssembly binary
    val helloWorldBytes = checkNotNull(
        Thread.currentThread().contextClassLoader.getResource("helloworld.wasm")
    )
        .openStream()
        .use { it.readAllBytes() }

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

[Chasm]: https://github.com/CharlieTap/chasm
[Chasm_version]: https://github.com/CharlieTap/chasm/releases/tag/0.9.0
