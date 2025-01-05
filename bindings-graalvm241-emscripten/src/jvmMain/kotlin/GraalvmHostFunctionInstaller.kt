/*
 * Copyright 2024-2025, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.graalvm241

import at.released.weh.bindings.graalvm241.GraalvmHostFunctionInstaller.Builder
import at.released.weh.bindings.graalvm241.GraalvmHostFunctionInstaller.Companion
import at.released.weh.bindings.graalvm241.host.module.emscripten.EmscriptenEnvModuleBuilder
import at.released.weh.bindings.graalvm241.host.pthread.GraalvmPthreadManager
import at.released.weh.bindings.graalvm241.memory.MemorySource
import at.released.weh.bindings.graalvm241.memory.MemorySource.ExportedMemory
import at.released.weh.bindings.graalvm241.memory.MemorySource.ImportedMemory
import at.released.weh.bindings.graalvm241.memory.MemorySpec
import at.released.weh.bindings.graalvm241.memory.SharedMemoryWaiterListStore
import at.released.weh.bindings.graalvm241.wasip1.GraalvmWasiPreview1Builder
import at.released.weh.common.api.WasiEmscriptenHostDsl
import at.released.weh.emcripten.runtime.export.stack.EmscriptenStack
import at.released.weh.host.EmbedderHost
import at.released.weh.host.EmbedderHostBuilder
import at.released.weh.wasm.core.WasmModules.ENV_MODULE_NAME
import at.released.weh.wasm.core.WasmModules.WASI_SNAPSHOT_PREVIEW1_MODULE_NAME
import at.released.weh.wasm.core.memory.Pages
import at.released.weh.wasm.core.memory.WASM_MEMORY_PAGE_SIZE
import org.graalvm.polyglot.Context

/**
 * Emscripten / WASI Preview 1 module installer.
 *
 * This class is responsible for installing Emscripten and WASI Preview 1 host functions.
 * It prepares the environment and installs WebAssembly modules with the Emscripten env host functions and
 * WASI Preview 1 host functions into the GraalVM context.
 *
 * To create a new instance, use either [GraalvmHostFunctionInstaller()][Companion.invoke] or [Builder].
 *
 * Usage example:
 *
 * ```kotlin
 * // Setup modules
 * val installer = GraalvmHostFunctionInstaller(context) {
 *    ...
 * }
 * installer.setupWasiPreview1Module()
 * val emscriptenInstaller = installer.setupEmscriptenFunctions()
 *
 * // Evaluate the WebAssembly module
 * context.eval(source)
 *
 * // Finish initialization after module instantiation
 * emscriptenInstaller.finalize(HELLO_WORLD_MODULE_NAME).use { emscriptenEnv ->
 *     // Initialize Emscripten runtime environment
 *     emscriptenEnv.emscriptenRuntime.initMainThread()
 *
 *     // Execute code
 *     run(context)
 * }
 * ```
 */
public class GraalvmHostFunctionInstaller private constructor(
    private val host: EmbedderHost,
    private val graalWasmContext: Context,
) {
    private val memoryWaiters = SharedMemoryWaiterListStore()

    @JvmOverloads
    public fun setupWasiPreview1Module(
        moduleName: String = WASI_SNAPSHOT_PREVIEW1_MODULE_NAME,
        memory: ImportedMemory? = ImportedMemory(),
    ) {
        GraalvmWasiPreview1Builder {
            this.host = this@GraalvmHostFunctionInstaller.host
            this.memorySource = memory
        }.build(graalWasmContext, moduleName)
    }

    @JvmOverloads
    public fun setupEmscriptenFunctions(
        moduleName: String = ENV_MODULE_NAME,
        memory: MemorySource = ExportedMemory(spec = DEFAULT_MEMORY_SPEC),
    ): GraalvmEmscriptenFinalizer {
        return GraalvmEmscriptenFinalizer(
            envModuleName = moduleName,
            host = host,
            memoryWaiters = memoryWaiters,
            graalWasmContext = graalWasmContext,
        ).apply {
            setup(memory)
        }
    }

    public class GraalvmEmscriptenFinalizer internal constructor(
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
            val host = host ?: EmbedderHostBuilder().build()
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
