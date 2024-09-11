/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("CommentWrapping")

package at.released.weh.sample.chicory.gradle.app

import at.released.weh.bindings.chicory.ChicoryHostFunctionInstaller
import at.released.weh.bindings.chicory.ChicoryHostFunctionInstaller.ChicoryEmscriptenInstaller
import com.dylibso.chicory.runtime.HostFunction
import com.dylibso.chicory.runtime.HostGlobal
import com.dylibso.chicory.runtime.HostImports
import com.dylibso.chicory.runtime.HostMemory
import com.dylibso.chicory.runtime.HostTable
import com.dylibso.chicory.runtime.Memory
import com.dylibso.chicory.runtime.Module
import com.dylibso.chicory.wasm.types.MemoryLimits
import com.dylibso.chicory.wasm.types.Value

// You can use `wasm-objdump -x helloworld.wasm -j Memory` to get the memory limits declared in the WebAssembly binary.
const val INITIAL_MEMORY_SIZE_PAGES = 258

fun main() {
    // Prepare Host memory
    val memory = HostMemory(
        /* moduleName = */ "env",
        /* fieldName = */ "memory",
        /* memory = */
        Memory(
            MemoryLimits(INITIAL_MEMORY_SIZE_PAGES),
        ),
    )

    // Prepare WASI and Emscripten host imports
    val installer = ChicoryHostFunctionInstaller(
        memory = memory.memory(),
    )
    val wasiFunctions: List<HostFunction> = installer.setupWasiPreview1HostFunctions()
    val emscriptenInstaller: ChicoryEmscriptenInstaller = installer.setupEmscriptenFunctions()
    val hostImports = HostImports(
        /* functions = */ (emscriptenInstaller.emscriptenFunctions + wasiFunctions).toTypedArray(),
        /* globals = */ arrayOf<HostGlobal>(),
        /* memory = */ memory,
        /* tables = */ arrayOf<HostTable>(),
    )

    // Setup Chicory Module
    val module = Module
        .builder("helloworld.wasm")
        .withHostImports(hostImports)
        .withInitialize(true)
        .withStart(false)
        .build()

    // Instantiate the WebAssembly module
    val instance = module.instantiate()

    // Finalize initialization after module instantiation
    val emscriptenRuntime = emscriptenInstaller.finalize(instance)

    // Initialize Emscripten runtime environment
    emscriptenRuntime.initMainThread()

    // Execute code
    instance.export("main").apply(
        /* argc */ Value.i32(0),
        /* argv */ Value.i32(0),
    )[0].asInt()
}
