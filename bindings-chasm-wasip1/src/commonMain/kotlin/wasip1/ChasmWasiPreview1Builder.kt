/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.chasm.wasip1

import at.released.weh.bindings.chasm.dsl.ChasmHostFunctionDsl
import at.released.weh.bindings.chasm.memory.ChasmMemoryAdapter
import at.released.weh.bindings.chasm.module.wasi.createWasiPreview1HostFunctions
import at.released.weh.host.EmbedderHost
import at.released.weh.host.EmbedderHostBuilder
import at.released.weh.wasi.preview1.memory.DefaultWasiMemoryReader
import at.released.weh.wasi.preview1.memory.DefaultWasiMemoryWriter
import at.released.weh.wasm.core.WasmModules
import io.github.charlietap.chasm.embedding.shapes.Import
import io.github.charlietap.chasm.embedding.shapes.Memory
import io.github.charlietap.chasm.embedding.shapes.Store

/**
 * WASI Preview 1 host function installer.
 *
 * Sets up WebAssembly host imports that provide the Emscripten env and WASI Preview 1 implementations.
 *
 * To create a new instance, use [ChasmWasiPreview1Builder()][Companion.invoke].
 *
 * Usage example:
 *
 * ```kotlin
 * // Prepare WASI host imports
 * val wasiImports: List<Import> = ChasmWasiPreview1Builder(store) {
 *     host = embedderHost
 * }.build()
 * ```
 */
public class ChasmWasiPreview1Builder private constructor(
    private val store: Store,
    private val memoryProvider: (Store.() -> Memory)?,
    private val host: EmbedderHost,
) {
    public fun build(
        moduleName: String = WasmModules.WASI_SNAPSHOT_PREVIEW1_MODULE_NAME,
    ): List<Import> {
        val memory = ChasmMemoryAdapter(store, memoryProvider)
        val wasiMemoryReader = DefaultWasiMemoryReader(memory, host.fileSystem)
        val wasiMemoryWriter = DefaultWasiMemoryWriter(memory, host.fileSystem)
        return createWasiPreview1HostFunctions(
            store = store,
            memory = memory,
            wasiMemoryReader = wasiMemoryReader,
            wasiMemoryWriter = wasiMemoryWriter,
            host = host,
            moduleName = moduleName,
        ) + createCustomWasiPreview1HostFunctions(store, moduleName)
    }

    public companion object {
        public operator fun invoke(
            store: Store,
            block: ChasmHostFunctionDsl.() -> Unit = {},
        ): ChasmWasiPreview1Builder {
            val config = ChasmHostFunctionDsl().apply(block)
            return ChasmWasiPreview1Builder(
                store = store,
                memoryProvider = config.memoryProvider,
                host = config.host ?: EmbedderHostBuilder().build(),
            )
        }
    }
}
