/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.chasm

import at.released.weh.bindings.chasm.dsl.ChasmHostFunctionDsl
import at.released.weh.bindings.chasm.exports.ChasmEmscriptenMainExports
import at.released.weh.bindings.chasm.exports.ChasmEmscriptenStackExports
import at.released.weh.bindings.chasm.memory.ChasmMemoryAdapter
import at.released.weh.bindings.chasm.module.emscripten.createEmscriptenHostFunctions
import at.released.weh.bindings.chasm.wasip1.ChasmWasiPreview1Builder
import at.released.weh.common.api.Logger
import at.released.weh.emcripten.runtime.export.DefaultEmscriptenRuntime
import at.released.weh.emcripten.runtime.export.EmscriptenRuntime
import at.released.weh.emcripten.runtime.export.stack.EmscriptenStack
import at.released.weh.host.EmbedderHost
import at.released.weh.wasm.core.WasmModules.ENV_MODULE_NAME
import at.released.weh.wasm.core.WasmModules.WASI_SNAPSHOT_PREVIEW1_MODULE_NAME
import io.github.charlietap.chasm.embedding.shapes.Import
import io.github.charlietap.chasm.embedding.shapes.Memory
import io.github.charlietap.chasm.embedding.shapes.Store
import io.github.charlietap.chasm.embedding.shapes.Instance as ChasmInstance

/**
 * Emscripten / WASI Preview 1 host function installer.
 *
 * Sets up WebAssembly host imports that provide the Emscripten env and WASI Preview 1 implementations.
 *
 * To create a new instance, use [Companion.invoke].
 *
 * Usage example:
 *
 * ```kotlin
 * // Prepare Host memory
 * val memory: Memory = memory(store, memoryType)
 *
 * // Prepare WASI and Emscripten host imports
 * val chasmHostBuilder = ChasmEmscriptenHostBuilder(store) {
 *     memoryProvider = { memory }
 * }
 * val wasiHostFunctions = chasmHostBuilder.setupWasiPreview1HostFunctions()
 * val emscriptenFinalizer = chasmHostBuilder.setupEmscriptenFunctions()
 * ```
 */
public class ChasmEmscriptenHostBuilder private constructor(
    private val store: Store,
    private val memoryProvider: (Store.() -> Memory)?,
    private val host: EmbedderHost,
) {
    private val memory = ChasmMemoryAdapter(store, memoryProvider)

    public fun setupWasiPreview1HostFunctions(
        moduleName: String = WASI_SNAPSHOT_PREVIEW1_MODULE_NAME,
    ): List<Import> = ChasmWasiPreview1Builder(store) {
        this.host = this@ChasmEmscriptenHostBuilder.host
        this.memoryProvider = this@ChasmEmscriptenHostBuilder.memoryProvider
    }.build(moduleName)

    public fun setupEmscriptenFunctions(
        moduleName: String = ENV_MODULE_NAME,
    ): ChasmEmscriptenSetupFinalizer {
        return ChasmEmscriptenSetupFinalizer(store, memory, host.rootLogger).apply {
            setupEmscriptenFunctions(host, moduleName)
        }
    }

    public class ChasmEmscriptenSetupFinalizer internal constructor(
        private val store: Store,
        private val memory: ChasmMemoryAdapter,
        private val rootLogger: Logger,
    ) {
        public var emscriptenFunctions: List<Import> = emptyList()
            private set

        private var _emscriptenStack: EmscriptenStack? = null
        private val emscriptenStack: EmscriptenStack
            get() = _emscriptenStack ?: error("Emscripten instantiation is not finalized")

        internal fun setupEmscriptenFunctions(
            host: EmbedderHost,
            moduleName: String,
        ) {
            emscriptenFunctions = createEmscriptenHostFunctions(
                store = store,
                memory = memory,
                host = host,
                emscriptenStackRef = ::emscriptenStack,
                moduleName = moduleName,
            )
        }

        public fun finalize(instance: ChasmInstance): EmscriptenRuntime {
            val emscriptenRuntime = DefaultEmscriptenRuntime.emscriptenSingleThreadedRuntime(
                mainExports = ChasmEmscriptenMainExports(store, instance),
                stackExports = ChasmEmscriptenStackExports(store, instance),
                memory = memory,
                logger = rootLogger,
            )
            _emscriptenStack = emscriptenRuntime.stack
            return emscriptenRuntime
        }
    }

    public companion object {
        public operator fun invoke(
            store: Store,
            block: ChasmHostFunctionDsl.() -> Unit = {},
        ): ChasmEmscriptenHostBuilder {
            val config = ChasmHostFunctionDsl().apply(block)
            return ChasmEmscriptenHostBuilder(
                store = store,
                memoryProvider = config.memoryProvider,
                host = config.host ?: EmbedderHost.Builder().build(),
            )
        }
    }
}
