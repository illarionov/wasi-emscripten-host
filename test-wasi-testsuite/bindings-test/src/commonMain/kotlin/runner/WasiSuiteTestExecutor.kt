/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.wasi.bindings.test.runner

import assertk.assertThat
import assertk.assertions.isEqualTo
import at.released.weh.common.api.Logger
import at.released.weh.host.CommandArgsProvider
import at.released.weh.host.EmbedderHost
import at.released.weh.host.SystemEnvProvider
import at.released.weh.test.logger.TestLogger
import at.released.weh.wasi.bindings.test.ext.copyRecursively
import at.released.weh.wasi.bindings.test.ext.setCurrentWorkingDirectory
import at.released.weh.wasi.bindings.test.ext.walkBottomUp
import kotlinx.io.IOException
import kotlinx.io.buffered
import kotlinx.io.files.FileSystem
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readByteArray
import kotlinx.io.readString
import kotlinx.serialization.json.Json

/**
 * WASI Test suite test executor.
 *
 * See [WASI Test Suite](https://github.com/WebAssembly/wasi-testsuite/blob/main/doc/specification.md)
 */
public class WasiSuiteTestExecutor(
    private val testsRoot: Path,
    private val testName: String,
    private val runtimeTestExecutor: RuntimeTestExecutor,
    private val tempRoot: Path,
    private val fileSystem: FileSystem = SystemFileSystem,
    private val logger: Logger = TestLogger(),
) {
    private val argumentsFilePath: Path get() = Path(testsRoot, "$testName.json")
    private val wasmFilePath: Path get() = Path(testsRoot, "$testName.wasm")

    public fun runTest() {
        val wasmFilename = readWasm()
        val arguments = readArguments()
        val host = setupHost(arguments)
        prepareTempRoot(arguments.dirs)

        fileSystem.setCurrentWorkingDirectory(tempRoot)
        val exitCode = try {
            runtimeTestExecutor.runTest(
                wasmFile = wasmFilename,
                host = host,
                arguments = arguments,
                rootDir = tempRoot,
            )
        } finally {
            cleanup()
        }
        assertThat(exitCode).isEqualTo(arguments.exitCode)
        // XXX: check stdout
        // XXX: check stderr
    }

    private fun readArguments(): WasiTestsuiteArguments {
        if (!fileSystem.exists(argumentsFilePath)) {
            return WasiTestsuiteArguments()
        }

        return Json.decodeFromString(
            fileSystem.source(argumentsFilePath).buffered().readString(),
        )
    }

    private fun readWasm(): ByteArray = fileSystem.source(wasmFilePath).buffered().readByteArray()

    private fun prepareTempRoot(
        dirs: List<String>,
    ) {
        for (dir in dirs) {
            val srcDir = Path(testsRoot, dir)
            val dstDir = Path(tempRoot, dir)
            fileSystem.copyRecursively(srcDir, dstDir)
        }
    }

    private fun setupHost(
        arguments: WasiTestsuiteArguments,
    ): EmbedderHost {
        return EmbedderHost.Builder().apply {
            rootLogger = logger
            commandArgsProvider = CommandArgsProvider { listOf("testproc") + arguments.args }
            systemEnvProvider = SystemEnvProvider(arguments::env)
            // XXX: setup stdout / stderr / preopen directories / current working directory
        }.build()
    }

    private fun cleanup() {
        fileSystem.walkBottomUp(tempRoot)
            .filter { it != tempRoot }
            .forEach {
                try {
                    fileSystem.delete(it, mustExist = false)
                } catch (deletionFailed: IOException) {
                    // XXX: usually symlink, IGNORE for now
                    @Suppress("DEBUG_PRINT")
                    println("Can not delete `$it`: $deletionFailed")
                }
            }
    }
}
