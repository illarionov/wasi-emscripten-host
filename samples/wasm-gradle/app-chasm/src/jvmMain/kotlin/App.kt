/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.sample.chasm.gradle.app

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

// You can use `wasm-objdump -x helloworld.wasm -j Memory` to get the memory limits declared in the WebAssembly binary.
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
    val helloWorldBytes = checkNotNull(Thread.currentThread().contextClassLoader.getResource("helloworld.wasm"))
        .openStream()
        .use {
            it.readAllBytes()
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
