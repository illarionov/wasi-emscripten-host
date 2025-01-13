/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.sample.chasm.gradle.app

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
