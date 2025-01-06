/*
 * Copyright 2024-2025, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("CommentWrapping")

package at.released.weh.sample.chicory.gradle.app

import at.released.weh.bindings.chicory.exception.ProcExitException
import at.released.weh.bindings.chicory.wasip1.ChicoryWasiPreview1Builder
import at.released.weh.host.EmbedderHost
import com.dylibso.chicory.runtime.HostFunction
import com.dylibso.chicory.runtime.ImportValues
import com.dylibso.chicory.runtime.Instance
import com.dylibso.chicory.wasm.Parser
import com.dylibso.chicory.wasm.WasmModule
import kotlin.system.exitProcess

fun main() {
    // Load WebAssembly binary
    val wasmModule = Thread.currentThread().contextClassLoader.getResourceAsStream("helloworld_wasi.wasm")
        .use(Parser::parse)

    // Create Host and run code
    EmbedderHost {
        fileSystem {
            addPreopenedDirectory(".", "/data")
        }
    }.use { executeCode(it, wasmModule) }
}

private fun executeCode(embedderHost: EmbedderHost, wasmModule: WasmModule) {
    // Prepare WASI and Emscripten host imports
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
        if (pre.exitCode != 0) {
            exitProcess(pre.exitCode)
        }
    }
}
