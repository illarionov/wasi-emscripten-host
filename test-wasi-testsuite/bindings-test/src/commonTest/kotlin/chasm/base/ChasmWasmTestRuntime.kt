/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.wasi.bindings.test.chasm.base

import at.released.weh.bindings.chasm.exception.ProcExitException
import at.released.weh.bindings.chasm.wasip1.ChasmWasiPreview1Builder
import at.released.weh.host.EmbedderHost
import at.released.weh.wasi.bindings.test.runner.WasiTestsuiteArguments
import at.released.weh.wasi.bindings.test.runner.WasmTestRuntime
import io.github.charlietap.chasm.embedding.instance
import io.github.charlietap.chasm.embedding.invoke
import io.github.charlietap.chasm.embedding.module
import io.github.charlietap.chasm.embedding.shapes.Instance
import io.github.charlietap.chasm.embedding.shapes.Store
import io.github.charlietap.chasm.embedding.shapes.flatMap
import io.github.charlietap.chasm.embedding.shapes.fold
import io.github.charlietap.chasm.embedding.store
import kotlinx.io.files.Path

object ChasmWasmTestRuntime : WasmTestRuntime {
    private val HOST_FUNCTION_ERROR_PATTERN = """HostFunctionError\(error=(\d+)\)""".toRegex()

    override fun runTest(
        wasmFile: ByteArray,
        host: EmbedderHost,
        arguments: WasiTestsuiteArguments,
        rootDir: Path,
    ): Int {
        val store: Store = store()
        val instance = setupInstance(store, wasmFile, host)

        val exitCode: Int = try {
            invoke(store, instance, "_start").fold(
                onSuccess = { 0 },
                onError = { executionError ->
                    HOST_FUNCTION_ERROR_PATTERN.matchEntire(executionError.error)?.let { match ->
                        match.groups[1]!!.value.toIntOrNull()
                    } ?: -1
                },
            )
        } catch (pre: ProcExitException) {
            pre.exitCode
        }
        return exitCode
    }

    private fun setupInstance(
        store: Store,
        wasmFile: ByteArray,
        host: EmbedderHost,
    ): Instance {
        val hostImports = ChasmWasiPreview1Builder(store) {
            this.host = host
        }.build()

        val instance: Instance = module(
            bytes = wasmFile,
        ).flatMap { module ->
            instance(store, module, hostImports)
        }.fold(
            onSuccess = { it },
            onError = { throw WasmException("Can node instantiate WebAssembly binary: $it") },
        )
        return instance
    }

    class WasmException(message: String) : RuntimeException(message)

    class Factory : WasmTestRuntime.Factory {
        override fun invoke(): WasmTestRuntime = ChasmWasmTestRuntime
        override fun close() = Unit
    }
}
