/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.sample.chasm.gradle.app

import at.released.weh.bindings.chasm.ChasmEmscriptenHostBuilder
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
    val store: Store = store()

    // Prepare WASI and Emscripten host imports
    val chasmBuilder = ChasmEmscriptenHostBuilder(store)
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
