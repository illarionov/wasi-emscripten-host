/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.chasm

import at.released.weh.bindings.chasm.dsl.ChasmHostFunctionInstallerDsl
import at.released.weh.bindings.chasm.exports.ChasmEmscriptenMainExports
import at.released.weh.bindings.chasm.exports.ChasmEmscriptenStackExports
import at.released.weh.bindings.chasm.memory.ChasmMemoryAdapter
import at.released.weh.bindings.chasm.module.emscripten.createEmscriptenHostFunctions
import at.released.weh.bindings.chasm.module.wasi.createWasiPreview1HostFunctions
import at.released.weh.common.api.Logger
import at.released.weh.host.EmbedderHost
import at.released.weh.host.base.WasmModules.ENV_MODULE_NAME
import at.released.weh.host.base.WasmModules.WASI_SNAPSHOT_PREVIEW1_MODULE_NAME
import at.released.weh.host.emscripten.export.EmscriptenRuntime
import at.released.weh.host.emscripten.export.stack.EmscriptenStack
import at.released.weh.wasi.preview1.memory.DefaultWasiMemoryReader
import at.released.weh.wasi.preview1.memory.DefaultWasiMemoryWriter
import io.github.charlietap.chasm.embedding.shapes.Import
import io.github.charlietap.chasm.embedding.shapes.Memory
import io.github.charlietap.chasm.embedding.shapes.Store
import io.github.charlietap.chasm.embedding.shapes.Instance as ChasmInstance

public class ChasmHostFunctionInstaller private constructor(
    private val store: Store,
    memoryProvider: (Store.() -> Memory)?,
    private val host: EmbedderHost,
) {
    private val memory = ChasmMemoryAdapter(store, memoryProvider)

    public fun setupWasiPreview1HostFunctions(
        moduleName: String = WASI_SNAPSHOT_PREVIEW1_MODULE_NAME,
    ): List<Import> {
        val wasiMemoryReader = DefaultWasiMemoryReader(memory, host.fileSystem)
        val wasiMemoryWriter = DefaultWasiMemoryWriter(memory, host.fileSystem)
        return createWasiPreview1HostFunctions(
            store = store,
            memory = memory,
            wasiMemoryReader = wasiMemoryReader,
            wasiMemoryWriter = wasiMemoryWriter,
            host = host,
            moduleName = moduleName,
        )
    }

    public fun setupEmscriptenFunctions(
        moduleName: String = ENV_MODULE_NAME,
    ): ChasmEmscriptenInstaller {
        return ChasmEmscriptenInstaller(store, memory, host.rootLogger).apply {
            setupEmscriptenFunctions(host, moduleName)
        }
    }

    public class ChasmEmscriptenInstaller internal constructor(
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
            val emscriptenRuntime = EmscriptenRuntime.emscriptenSingleThreadedRuntime(
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
            block: ChasmHostFunctionInstallerDsl.() -> Unit = {},
        ): ChasmHostFunctionInstaller {
            val config = ChasmHostFunctionInstallerDsl().apply(block)
            return ChasmHostFunctionInstaller(
                store = store,
                memoryProvider = config.memoryProvider,
                host = config.host ?: EmbedderHost.Builder().build(),
            )
        }
    }
}
