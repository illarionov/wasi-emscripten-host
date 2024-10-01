/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.wasi.bindings.test.chasm.base

import at.released.weh.bindings.chasm.ChasmHostFunctionInstaller
import at.released.weh.host.EmbedderHost
import at.released.weh.wasi.bindings.test.runner.RuntimeTestExecutor
import at.released.weh.wasi.bindings.test.runner.WasiTestsuiteArguments
import io.github.charlietap.chasm.embedding.instance
import io.github.charlietap.chasm.embedding.invoke
import io.github.charlietap.chasm.embedding.module
import io.github.charlietap.chasm.embedding.shapes.Import
import io.github.charlietap.chasm.embedding.shapes.Instance
import io.github.charlietap.chasm.embedding.shapes.Store
import io.github.charlietap.chasm.embedding.shapes.Value.Number.I32
import io.github.charlietap.chasm.embedding.shapes.flatMap
import io.github.charlietap.chasm.embedding.shapes.fold
import io.github.charlietap.chasm.embedding.store
import kotlinx.io.files.Path

object ChasmRuntimeTestExecutor : RuntimeTestExecutor {
    override fun runTest(
        wasmFile: ByteArray,
        host: EmbedderHost,
        arguments: WasiTestsuiteArguments,
        rootDir: Path,
    ): Int {
        val store: Store = store()
        val instance = setupInstance(store, wasmFile, host)
        val exitCode = invoke(store, instance, "_start").fold(
            onSuccess = { (it[0] as I32).value },
            onError = { error("Chasm error: $it") },
        )
        return exitCode
    }

    private fun setupInstance(
        store: Store,
        wasmFile: ByteArray,
        host: EmbedderHost,
    ): Instance {
        val chasmInstaller = ChasmHostFunctionInstaller(store) {
            this.host = host
        }
        val wasiHostFunctions = chasmInstaller.setupWasiPreview1HostFunctions()
        val hostImports: List<Import> = buildList {
            addAll(wasiHostFunctions)
        }

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

    class Factory : RuntimeTestExecutor.Factory {
        override fun invoke(): RuntimeTestExecutor = ChasmRuntimeTestExecutor
        override fun close() = Unit
    }
}
