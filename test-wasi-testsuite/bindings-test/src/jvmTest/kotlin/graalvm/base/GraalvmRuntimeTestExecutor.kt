/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.wasi.bindings.test.graalvm.base

import at.released.weh.bindings.graalvm241.GraalvmHostFunctionInstaller
import at.released.weh.bindings.graalvm241.MemorySource
import at.released.weh.host.EmbedderHost
import at.released.weh.wasi.bindings.test.runner.RuntimeTestExecutor
import at.released.weh.wasi.bindings.test.runner.WasiTestsuiteArguments
import kotlinx.io.files.Path
import org.graalvm.polyglot.Context
import org.graalvm.polyglot.Engine
import org.graalvm.polyglot.Source
import org.graalvm.polyglot.io.ByteSequence

class GraalvmRuntimeTestExecutor(
    private val engine: Engine,
) : RuntimeTestExecutor {
    override fun runTest(
        wasmFile: ByteArray,
        host: EmbedderHost,
        arguments: WasiTestsuiteArguments,
        rootDir: Path,
    ): Int {
        val source = Source.newBuilder("wasm", ByteSequence.create(wasmFile), "testproc").build()

        val context: Context = Context.newBuilder()
            .engine(engine)
            .build()
        context.use {
            context.initialize("wasm")

            val installer = GraalvmHostFunctionInstaller(context) {
                this.host = host
            }
            installer.setupWasiPreview1Module(
                // XXX remove
                memory = MemorySource.ExportedMemory(),
            )

            context.eval(source)

            val startFunc = context
                .getBindings("wasm")
                .getMember("testproc")
                .getMember("_start")

            startFunc.execute()
            return 0
        }
    }

    class Factory : RuntimeTestExecutor.Factory {
        private val engine = Engine.newBuilder("wasm").build()
        override fun invoke(): RuntimeTestExecutor = GraalvmRuntimeTestExecutor(engine)
        override fun close() = engine.close()
    }
}
