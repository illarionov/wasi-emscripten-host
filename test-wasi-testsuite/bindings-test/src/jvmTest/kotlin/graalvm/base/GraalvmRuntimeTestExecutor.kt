/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.wasi.bindings.test.graalvm.base

import at.released.weh.bindings.graalvm241.GraalvmHostFunctionInstaller
import at.released.weh.host.EmbedderHost
import at.released.weh.wasi.bindings.test.runner.RuntimeTestExecutor
import at.released.weh.wasi.bindings.test.runner.WasiTestsuiteArguments
import kotlinx.io.files.Path
import org.graalvm.polyglot.Context
import org.graalvm.polyglot.Engine
import org.graalvm.polyglot.PolyglotException
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
            installer.setupWasiPreview1Module()

            context.eval(source)

            val startFunc = context
                .getBindings("wasm")
                .getMember("testproc")
                .getMember("_start")

            val exitCode = try {
                startFunc.execute()
                0
            } catch (pex: PolyglotException) {
                if (pex.isExit) {
                    pex.exitStatus
                } else {
                    throw pex
                }
            }
            return exitCode
        }
    }

    class Factory : RuntimeTestExecutor.Factory {
        private val engine = Engine.newBuilder("wasm").build()
        override fun invoke(): RuntimeTestExecutor = GraalvmRuntimeTestExecutor(engine)
        override fun close() = engine.close()
    }
}
