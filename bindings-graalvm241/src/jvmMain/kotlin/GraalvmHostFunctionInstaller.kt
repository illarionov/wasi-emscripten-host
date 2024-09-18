/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.graalvm241

import at.released.weh.bindings.graalvm241.MemorySource.ExportedMemory
import at.released.weh.bindings.graalvm241.MemorySource.ImportedMemory
import at.released.weh.bindings.graalvm241.host.memory.SharedMemoryWaiterListStore
import at.released.weh.bindings.graalvm241.host.module.emscripten.EmscriptenEnvModuleBuilder
import at.released.weh.bindings.graalvm241.host.module.wasi.WasiSnapshotPreview1ModuleBuilder
import at.released.weh.bindings.graalvm241.host.pthread.GraalvmPthreadManager
import at.released.weh.common.api.WasiEmscriptenHostDsl
import at.released.weh.host.EmbedderHost
import at.released.weh.host.base.WasmModules.ENV_MODULE_NAME
import at.released.weh.host.base.WasmModules.WASI_SNAPSHOT_PREVIEW1_MODULE_NAME
import at.released.weh.host.base.memory.Pages
import at.released.weh.host.base.memory.WASM_MEMORY_PAGE_SIZE
import at.released.weh.host.emscripten.export.stack.EmscriptenStack
import org.graalvm.polyglot.Context

public class GraalvmHostFunctionInstaller private constructor(
    private val host: EmbedderHost,
    private val graalWasmContext: Context,
) {
    private val memoryWaiters = SharedMemoryWaiterListStore()

    @JvmOverloads
    public fun setupWasiPreview1Module(
        moduleName: String = WASI_SNAPSHOT_PREVIEW1_MODULE_NAME,
        memory: MemorySource? = ImportedMemory(),
    ) {
        WasiSnapshotPreview1ModuleBuilder.setupModule(
            graalContext = graalWasmContext,
            host = host,
            moduleName = moduleName,
            memory = memory,
            memoryWaiters = memoryWaiters,
        )
    }

    @JvmOverloads
    public fun setupEmscriptenFunctions(
        moduleName: String = ENV_MODULE_NAME,
        memory: MemorySource = ExportedMemory(spec = DEFAULT_MEMORY_SPEC),
    ): GraalvmEmscriptenInstaller {
        return GraalvmEmscriptenInstaller(
            envModuleName = moduleName,
            host = host,
            memoryWaiters = memoryWaiters,
            graalWasmContext = graalWasmContext,
        ).apply {
            setup(memory)
        }
    }

    public class GraalvmEmscriptenInstaller internal constructor(
        private val envModuleName: String,
        private val graalWasmContext: Context,
        private val host: EmbedderHost,
        private val memoryWaiters: SharedMemoryWaiterListStore,
    ) {
        private var _emscriptenStack: EmscriptenStack? = null
        private val emscriptenStack: EmscriptenStack
            get() = _emscriptenStack ?: error(
                "emscriptenStack is not available. Emscripten instantiation is not finalized",
            )

        private var _pthreadManager: GraalvmPthreadManager? = null
        private val pthreadManager: GraalvmPthreadManager
            get() = _pthreadManager ?: error(
                "pthreadManager is not available. Emscripten instantiation is not finalized",
            )

        internal fun setup(
            memorySource: MemorySource,
        ) {
            EmscriptenEnvModuleBuilder(
                host = host,
                pthreadRef = ::pthreadManager,
                emscriptenStackRef = ::emscriptenStack,
                memoryWaiters = memoryWaiters,
            ).setupModule(
                graalContext = graalWasmContext,
                moduleName = envModuleName,
                memorySource = memorySource,
            )
        }

        public fun finalize(
            mainModuleName: String,
        ): GraalvmEmscriptenEnvironment {
            val runtime = GraalvmEmscriptenEnvironment(
                graalContext = graalWasmContext,
                mainModuleName = mainModuleName,
                envModuleName = envModuleName,
                isMainThread = true,
                rootLogger = host.rootLogger,
                host = host,
                sharedMemoryWaiters = memoryWaiters,
            )
            _emscriptenStack = runtime.emscriptenRuntime.stack
            _pthreadManager = runtime.pthreadManager
            return runtime
        }
    }

    @WasiEmscriptenHostDsl
    public class Builder(
        public val wasmContext: Context,
    ) {
        /**
         * Implementation of a host object that provides access from the WebAssembly to external host resources.
         */
        @set:JvmSynthetic
        public var host: EmbedderHost? = null

        /**
         * Sets implementation of a host object that provides access from the WebAssembly to external host resources.
         */
        public fun setHost(host: EmbedderHost?): Builder = apply {
            this.host = host
        }

        public fun build(): GraalvmHostFunctionInstaller {
            val host = host ?: EmbedderHost.Builder().build()
            return GraalvmHostFunctionInstaller(host, wasmContext)
        }
    }

    public companion object {
        internal val DEFAULT_MEMORY_SPEC: MemorySpec = MemorySpec {
            minSize = Pages(50331648L / WASM_MEMORY_PAGE_SIZE)
            shared = false
            useUnsafe = false
        }

        @JvmSynthetic
        public operator fun invoke(
            wasmContext: Context,
            block: Builder.() -> Unit = {},
        ): GraalvmHostFunctionInstaller {
            return Builder(wasmContext).apply(block).build()
        }
    }
}
