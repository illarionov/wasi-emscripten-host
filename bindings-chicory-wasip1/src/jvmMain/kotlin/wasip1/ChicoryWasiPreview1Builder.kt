/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.chicory.wasip1

import at.released.weh.bindings.chicory.host.module.wasi.createWasiPreview1HostFunctions
import at.released.weh.bindings.chicory.memory.ChicoryMemoryProvider
import at.released.weh.bindings.chicory.memory.DefaultChicoryMemoryProvider
import at.released.weh.bindings.chicory.memory.chicoryWasiMemoryReaderProvider
import at.released.weh.bindings.chicory.memory.chicoryWasiMemoryWriterProvider
import at.released.weh.common.api.WasiEmscriptenHostDsl
import at.released.weh.host.EmbedderHost
import at.released.weh.host.EmbedderHostBuilder
import at.released.weh.wasm.core.WasmModules.WASI_SNAPSHOT_PREVIEW1_MODULE_NAME
import com.dylibso.chicory.runtime.HostFunction

/**
 * WASI Preview 1 host function installer.
 *
 * Sets up WebAssembly host imports that provide WASI Preview 1 implementations.
 *
 * Usage example:
 *
 * ```kotlin
 * // Prepare WASI host imports
 * val builder = ChicoryWasiPreview1Builder()
 * val wasiFunctions: List<HostFunction> = builder.build()
 * val hostImports = HostImports(
 *     /* functions = */ wasiFunctions.toTypedArray(),
 *     /* globals = */ arrayOf<HostGlobal>(),
 *     /* memory = */ memory,
 *     /* tables = */ arrayOf<HostTable>(),
 * )
 *
 * // Setup Chicory Module
 * val module = Module
 *     .builder("helloworld.wasm")
 *     .withHostImports(hostImports)
 *     .withInitialize(true)
 *     .withStart(false)
 *     .build()
 *
 * // Instantiate the WebAssembly module
 * val instance = module.instantiate()
 *
 * // Use module
 * ```
 */
@WasiEmscriptenHostDsl
public class ChicoryWasiPreview1Builder {
    /**
     * Implementation of a host object that provides access from the WebAssembly to external host resources.
     */
    @set:JvmSynthetic
    public var host: EmbedderHost? = null

    /**
     * Sets memory provider used for all operations. For multi-memory scenarios.
     */
    @set:JvmSynthetic
    public var memoryProvider: ChicoryMemoryProvider? = null

    /**
     * Sets the host object that provides access from the WebAssembly to external host resources.
     */
    public fun setHost(host: EmbedderHost?): ChicoryWasiPreview1Builder = apply {
        this.host = host
    }

    /**
     * Sets memory provider used for all operations. For multi-memory scenarios.
     */
    public fun setMemoryProvider(memoryProvider: ChicoryMemoryProvider): ChicoryWasiPreview1Builder = apply {
        this.memoryProvider = memoryProvider
    }

    @JvmOverloads
    public fun build(
        moduleName: String = WASI_SNAPSHOT_PREVIEW1_MODULE_NAME,
    ): List<HostFunction> {
        val host = host ?: EmbedderHostBuilder().build()
        val memoryProvider = memoryProvider ?: DefaultChicoryMemoryProvider
        val wasiMemoryReaderProvider = chicoryWasiMemoryReaderProvider(memoryProvider, host.fileSystem)
        val wasiMemoryWriterProvider = chicoryWasiMemoryWriterProvider(memoryProvider, host.fileSystem)

        return createWasiPreview1HostFunctions(
            host = host,
            memoryProvider = memoryProvider,
            wasiMemoryReaderProvider = wasiMemoryReaderProvider,
            wasiMemoryWriterProvider = wasiMemoryWriterProvider,
            moduleName = moduleName,
        ) + createCustomChicoryFunctions()
    }

    public companion object {
        @JvmSynthetic
        public operator fun invoke(
            block: ChicoryWasiPreview1Builder.() -> Unit = {},
        ): ChicoryWasiPreview1Builder {
            return ChicoryWasiPreview1Builder().apply(block)
        }
    }
}
