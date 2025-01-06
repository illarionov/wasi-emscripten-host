/*
 * Copyright 2024-2025, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("CommentWrapping")

package at.released.weh.sample.chicory.gradle.app

import at.released.weh.bindings.chicory.ChicoryEmscriptenHostInstaller
import at.released.weh.bindings.chicory.ChicoryEmscriptenHostInstaller.ChicoryEmscriptenSetupFinalizer
import at.released.weh.host.EmbedderHost
import com.dylibso.chicory.runtime.HostFunction
import com.dylibso.chicory.runtime.ImportValues
import com.dylibso.chicory.runtime.Instance
import com.dylibso.chicory.wasm.Parser

fun main() {
    // Create Host and run code
    EmbedderHost {
        fileSystem {
            unrestricted = true
        }
    }.use(::executeCode)
}

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
    val wasmModule = Thread.currentThread().contextClassLoader.getResourceAsStream("helloworld.wasm")
        .use(Parser::parse)

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
