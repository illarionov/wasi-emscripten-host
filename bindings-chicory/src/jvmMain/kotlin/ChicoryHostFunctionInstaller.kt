/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.chicory

import at.released.weh.bindings.chicory.exports.ChicoryEmscriptenMainExports
import at.released.weh.bindings.chicory.exports.ChicoryEmscriptenStackExports
import at.released.weh.bindings.chicory.host.memory.ChicoryMemoryAdapter
import at.released.weh.bindings.chicory.host.memory.ChicoryWasiMemoryReader
import at.released.weh.bindings.chicory.host.memory.ChicoryWasiMemoryWriter
import at.released.weh.bindings.chicory.host.module.emscripten.EmscriptenEnvFunctionsBuilder
import at.released.weh.bindings.chicory.host.module.wasi.WasiSnapshotPreview1ModuleBuilder
import at.released.weh.common.api.WasiEmscriptenHostDsl
import at.released.weh.host.EmbedderHost
import at.released.weh.host.base.WasmModules.ENV_MODULE_NAME
import at.released.weh.host.base.WasmModules.WASI_SNAPSHOT_PREVIEW1_MODULE_NAME
import at.released.weh.host.base.memory.Memory
import at.released.weh.host.base.memory.WasiMemoryReader
import at.released.weh.host.base.memory.WasiMemoryWriter
import at.released.weh.host.emscripten.export.EmscriptenRuntime
import at.released.weh.host.emscripten.export.stack.EmscriptenStack
import com.dylibso.chicory.runtime.HostFunction
import com.dylibso.chicory.runtime.Instance
import com.dylibso.chicory.runtime.Memory as ChicoryMemory

public class ChicoryHostFunctionInstaller private constructor(
    private val host: EmbedderHost,
    chicoryMemory: ChicoryMemory,
) {
    private val memoryAdapter: ChicoryMemoryAdapter = ChicoryMemoryAdapter(chicoryMemory)

    @JvmOverloads
    public fun setupWasiPreview1HostFunctions(
        moduleName: String = WASI_SNAPSHOT_PREVIEW1_MODULE_NAME,
    ): List<HostFunction> {
        val wasiMemoryReader: WasiMemoryReader = ChicoryWasiMemoryReader.createOrDefault(
            memoryAdapter,
            host.fileSystem,
        )
        val wasiMemoryWriter: WasiMemoryWriter = ChicoryWasiMemoryWriter.createOrDefault(
            memoryAdapter,
            host.fileSystem,
        )
        return WasiSnapshotPreview1ModuleBuilder(
            memory = memoryAdapter,
            wasiMemoryReader = wasiMemoryReader,
            wasiMemoryWriter = wasiMemoryWriter,
            host = host,
        ).asChicoryHostFunctions(
            moduleName = moduleName,
        )
    }

    @JvmOverloads
    public fun setupEmscriptenFunctions(
        moduleName: String = ENV_MODULE_NAME,
    ): ChicoryEmscriptenInstaller {
        return ChicoryEmscriptenInstaller(
            host = host,
            memoryAdapter = memoryAdapter,
        ).apply {
            setupEmscriptenFunctions(moduleName)
        }
    }

    public class ChicoryEmscriptenInstaller internal constructor(
        private val host: EmbedderHost,
        private val memoryAdapter: Memory,
    ) {
        public var emscriptenFunctions: List<HostFunction> = emptyList()
            private set
        private var _emscriptenStack: EmscriptenStack? = null
        private val emscriptenStack: EmscriptenStack
            get() = _emscriptenStack ?: error("Emscripten instantiation is not finalized")

        internal fun setupEmscriptenFunctions(
            moduleName: String,
        ) {
            emscriptenFunctions = EmscriptenEnvFunctionsBuilder(
                memory = memoryAdapter,
                host = host,
                stackBindingsRef = ::emscriptenStack,
            ).asChicoryHostFunctions(
                moduleName = moduleName,
            )
        }

        public fun finalize(instance: Instance): EmscriptenRuntime {
            val emscriptenRuntime = EmscriptenRuntime.emscriptenSingleThreadedRuntime(
                mainExports = ChicoryEmscriptenMainExports(instance),
                stackExports = ChicoryEmscriptenStackExports(instance),
                memory = memoryAdapter,
                logger = host.rootLogger,
            )
            _emscriptenStack = emscriptenRuntime.stack
            return emscriptenRuntime
        }
    }

    @WasiEmscriptenHostDsl
    public class Builder(
        public val memory: ChicoryMemory,
    ) {
        /**
         * Implementation of a host object that provides access from the WebAssembly to external host resources.
         */
        @set:JvmSynthetic
        public var host: EmbedderHost? = null

        /**
         * Sets the host object that provides access from the WebAssembly to external host resources.
         */
        public fun setHost(host: EmbedderHost?): Builder = apply {
            this.host = host
        }

        public fun build(): ChicoryHostFunctionInstaller {
            val host = host ?: EmbedderHost.Builder().build()
            return ChicoryHostFunctionInstaller(host, memory)
        }
    }

    public companion object {
        @JvmSynthetic
        public operator fun invoke(
            memory: ChicoryMemory,
            block: Builder.() -> Unit = {},
        ): ChicoryHostFunctionInstaller {
            return Builder(memory).apply(block).build()
        }
    }
}
