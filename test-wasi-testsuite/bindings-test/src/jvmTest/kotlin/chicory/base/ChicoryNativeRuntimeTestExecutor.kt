/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.wasi.bindings.test.chicory.base

import assertk.assertThat
import assertk.assertions.isEqualTo
import at.released.weh.host.EmbedderHost
import at.released.weh.wasi.bindings.test.runner.RuntimeTestExecutor
import at.released.weh.wasi.bindings.test.runner.WasiTestsuiteArguments
import com.dylibso.chicory.log.SystemLogger
import com.dylibso.chicory.runtime.HostImports
import com.dylibso.chicory.runtime.Module
import com.dylibso.chicory.runtime.exceptions.WASMMachineException
import com.dylibso.chicory.wasi.WasiExitException
import com.dylibso.chicory.wasi.WasiOptions
import com.dylibso.chicory.wasi.WasiPreview1
import java.io.ByteArrayOutputStream
import java.nio.file.Path
import kotlin.text.Charsets.UTF_8

/**
 * Chicory implementation of WASI Preview 1 for reference
 */
object ChicoryNativeRuntimeTestExecutor : RuntimeTestExecutor {
    override fun runTest(
        wasmFile: ByteArray,
        host: EmbedderHost,
        arguments: WasiTestsuiteArguments,
        rootDir: kotlinx.io.files.Path,
    ): Int {
        val logger = SystemLogger()

        val stdOut = ByteArrayOutputStream()
        val stdErr = ByteArrayOutputStream()
        val options = WasiOptions.builder()
            .withStdout(stdOut)
            .withStderr(stdErr)
            .withArguments(listOf("chicorytest") + arguments.args)
            .apply {
                arguments.env.forEach { (key, value) -> withEnvironment(key, value) }
                arguments.dirs.forEach {
                    withDirectory(it, Path.of(rootDir.toString(), it))
                }
            }
            .build()

        WasiPreview1(logger, options).use { wasi ->
            val hostFunctions = wasi.toHostFunctions()
            val hostImports = HostImports(hostFunctions)

            val module = Module
                .builder(wasmFile)
                .withHostImports(hostImports)
                .withInitialize(true)
                .withStart(false)
                .build()

            // Instantiate the WebAssembly module
            val instance = module.instantiate()
            val exitCode = try {
                instance.export("_start").apply()
                0
            } catch (exit: WasiExitException) {
                exit.exitCode()
            } catch (machineException: WASMMachineException) {
                (machineException.cause as? WasiExitException)?.exitCode() ?: throw machineException
            }

            assertThat(stdOut.toByteArray().toString(UTF_8)).isEqualTo(arguments.stdout)
            assertThat(stdErr.toByteArray().toString(UTF_8)).isEqualTo(arguments.stderr)
            return exitCode
        }
    }

    class Factory : RuntimeTestExecutor.Factory {
        override fun invoke(): RuntimeTestExecutor = ChicoryNativeRuntimeTestExecutor
        override fun close() = Unit
    }
}
