/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.wasi.bindings.test.graalvm.base

import at.released.weh.bindings.graalvm241.wasip1.GraalvmWasiPreview1Builder
import at.released.weh.host.EmbedderHost
import at.released.weh.wasi.bindings.test.runner.WasiTestsuiteArguments
import at.released.weh.wasi.bindings.test.runner.WasmTestRuntime
import kotlinx.io.files.Path
import org.graalvm.polyglot.Context
import org.graalvm.polyglot.Engine
import org.graalvm.polyglot.PolyglotException
import org.graalvm.polyglot.Source
import org.graalvm.polyglot.io.ByteSequence

class GraalvmWasmTestRuntime(
    private val engine: Engine,
) : WasmTestRuntime {
    override val hasOwnStdioTests: Boolean = false

    override fun runTest(
        wasmFile: ByteArray,
        host: EmbedderHost,
        arguments: WasiTestsuiteArguments,
        rootDir: Path,
    ): Int {
        val source = Source.newBuilder("wasm", ByteSequence.create(wasmFile), "testproc").build()

        Context.newBuilder().engine(engine).build().use { context ->
            context.initialize("wasm")

            GraalvmWasiPreview1Builder {
                this.host = host
            }.build(context)

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

    class Factory : WasmTestRuntime.Factory {
        private val engine = Engine.newBuilder("wasm").build()
        override fun invoke(): WasmTestRuntime = GraalvmWasmTestRuntime(engine)
        override fun close() = engine.close()
    }
}
