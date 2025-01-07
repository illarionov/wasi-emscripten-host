/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.sample.graalvm.gradle.app

import at.released.weh.bindings.graalvm241.wasip1.GraalvmWasiPreview1Builder
import at.released.weh.host.EmbedderHost
import org.graalvm.polyglot.Context
import org.graalvm.polyglot.PolyglotException
import org.graalvm.polyglot.Source

internal object App

const val HELLO_WORLD_MODULE_NAME: String = "helloworld"

fun main() {
    // Create Host and run code
    EmbedderHost {
        fileSystem {
            addPreopenedDirectory(".", "/data")
        }
    }.use(::executeCode)
}

private fun executeCode(embedderHost: EmbedderHost) {
    // Prepare Source
    val source = Source.newBuilder("wasm", App::class.java.getResource("helloworld_wasi.wasm"))
        .name(HELLO_WORLD_MODULE_NAME)
        .build()

    // Setup Polyglot Context
    val context: Context = Context.newBuilder().build()
    context.use {
        // Context must be initialized before installing modules
        context.initialize("wasm")

        // Setup WASI Preview 1 module
        GraalvmWasiPreview1Builder {
            host = embedderHost
        }.build(context)

        // Evaluate the WebAssembly module
        context.eval(source)

        // Run code
        val startFunction = context.getBindings("wasm").getMember(HELLO_WORLD_MODULE_NAME).getMember("_start")

        try {
            startFunction.execute()
        } catch (re: PolyglotException) {
            if (re.message?.startsWith("Program exited with status code") == false) {
                throw re
            }
            Unit
        }
    }
}
