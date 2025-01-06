/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.sample.graalvm.gradle.app

import at.released.weh.bindings.graalvm241.GraalvmHostFunctionInstaller
import at.released.weh.common.api.InternalWasiEmscriptenHostApi
import org.graalvm.polyglot.Context
import org.graalvm.polyglot.Source

internal object App

const val HELLO_WORLD_MODULE_NAME: String = "helloworld"

fun main() {
    // Prepare Source
    val source = Source.newBuilder("wasm", App::class.java.getResource("helloworld.wasm"))
        .name(HELLO_WORLD_MODULE_NAME)
        .build()

    // Setup Polyglot Context
    val context: Context = Context.newBuilder().build()
    context.use {
        // Context must be initialized before installing modules
        context.initialize("wasm")

        // Setup modules
        val installer = GraalvmHostFunctionInstaller(context)
        installer.setupWasiPreview1Module()
        val emscriptenInstaller = installer.setupEmscriptenFunctions()

        // Evaluate the WebAssembly module
        context.eval(source)

        // Finish initialization after module instantiation
        emscriptenInstaller.finalize(HELLO_WORLD_MODULE_NAME).use { emscriptenEnv ->
            // Initialize Emscripten runtime environment
            emscriptenEnv.emscriptenRuntime.initMainThread()

            run(context)
        }
    }
}

private fun run(
    context: Context,
) {
    val mainFunction = context.getBindings("wasm").getMember(HELLO_WORLD_MODULE_NAME).getMember("main")
    mainFunction.execute(
        /* argc */
        0,
        /* argv */
        0,
    ).asInt()
}
