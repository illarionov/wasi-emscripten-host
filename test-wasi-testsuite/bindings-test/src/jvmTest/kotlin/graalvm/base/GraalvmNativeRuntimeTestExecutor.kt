/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.wasi.bindings.test.graalvm.base

import assertk.assertThat
import assertk.assertions.isEqualTo
import at.released.weh.host.EmbedderHost
import at.released.weh.wasi.bindings.test.runner.RuntimeTestExecutor
import at.released.weh.wasi.bindings.test.runner.WasiTestsuiteArguments
import kotlinx.io.files.Path
import org.graalvm.polyglot.Context
import org.graalvm.polyglot.Engine
import org.graalvm.polyglot.PolyglotException
import org.graalvm.polyglot.Source
import org.graalvm.polyglot.io.ByteSequence
import org.graalvm.polyglot.io.IOAccess
import java.io.ByteArrayOutputStream
import kotlin.text.Charsets.UTF_8
import java.nio.file.Path as JvmPath

class GraalvmNativeRuntimeTestExecutor(
    private val engine: Engine,
) : RuntimeTestExecutor {
    override fun runTest(
        wasmFile: ByteArray,
        host: EmbedderHost,
        arguments: WasiTestsuiteArguments,
        rootDir: Path,
    ): Int {
        val stdOut = ByteArrayOutputStream()
        val stdErr = ByteArrayOutputStream()

        val source = Source.newBuilder("wasm", ByteSequence.create(wasmFile), "testproc").build()

        val preopenedDirs = arguments.dirs.joinToString(",") { virtualPath ->
            val hostPath = Path(rootDir, virtualPath).toString()
            "$virtualPath::$hostPath"
        }

        val context: Context = Context.newBuilder()
            .engine(engine)
            .option("wasm.Builtins", "wasi_snapshot_preview1")
            .option("wasm.WasiMapDirs", preopenedDirs)
            .arguments("wasm", (listOf("testproc") + arguments.args).toTypedArray())
            .environment(arguments.env)
            .currentWorkingDirectory(JvmPath.of(rootDir.toString()))
            .err(stdErr)
            .out(stdOut)
            .allowIO(IOAccess.ALL)
            .build()

        val exitCode = context.use {
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
            exitCode
        }

        assertThat(stdOut.toByteArray().toString(UTF_8)).isEqualTo(arguments.stdout)
        assertThat(stdErr.toByteArray().toString(UTF_8)).isEqualTo(arguments.stderr)

        return exitCode
    }

    class Factory : RuntimeTestExecutor.Factory {
        private val engine = Engine.newBuilder("wasm").build()
        override fun invoke(): RuntimeTestExecutor = GraalvmNativeRuntimeTestExecutor(engine)
        override fun close() = engine.close()
    }
}
