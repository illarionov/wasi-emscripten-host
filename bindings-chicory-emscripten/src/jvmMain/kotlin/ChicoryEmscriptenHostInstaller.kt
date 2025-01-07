/*
 * Copyright 2024-2025, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.chicory

import at.released.weh.bindings.chicory.ChicoryEmscriptenHostInstaller.Builder
import at.released.weh.bindings.chicory.ChicoryEmscriptenHostInstaller.Companion
import at.released.weh.bindings.chicory.exports.ChicoryEmscriptenMainExports
import at.released.weh.bindings.chicory.exports.ChicoryEmscriptenStackExports
import at.released.weh.bindings.chicory.host.module.emscripten.EmscriptenEnvFunctionsBuilder
import at.released.weh.bindings.chicory.memory.ChicoryMemoryAdapter
import at.released.weh.bindings.chicory.memory.ChicoryMemoryProvider
import at.released.weh.bindings.chicory.wasip1.ChicoryWasiPreview1Builder
import at.released.weh.common.api.WasiEmscriptenHostDsl
import at.released.weh.emcripten.runtime.export.DefaultEmscriptenRuntime
import at.released.weh.emcripten.runtime.export.EmscriptenRuntime
import at.released.weh.emcripten.runtime.export.stack.EmscriptenStack
import at.released.weh.host.EmbedderHost
import at.released.weh.host.EmbedderHostBuilder
import at.released.weh.wasm.core.WasmModules.ENV_MODULE_NAME
import at.released.weh.wasm.core.WasmModules.WASI_SNAPSHOT_PREVIEW1_MODULE_NAME
import com.dylibso.chicory.runtime.HostFunction
import com.dylibso.chicory.runtime.Instance

/**
 * Emscripten / WASI Preview 1 host function installer.
 *
 * Sets up WebAssembly host imports that provide the Emscripten env and WASI Preview 1 implementations.
 *
 * To create a new instance, use either [ChicoryEmscriptenHostInstaller {}][Companion.invoke] or [Builder].
 *
 * Usage example:
 *
 * ```kotlin
 * import at.released.weh.host.EmbedderHost
 * import at.released.weh.bindings.chicory.ChicoryEmscriptenHostInstaller
 * import at.released.weh.bindings.chicory.ChicoryEmscriptenHostInstaller.ChicoryEmscriptenSetupFinalizer
 * import com.dylibso.chicory.runtime.HostFunction
 * import com.dylibso.chicory.runtime.ImportValues
 * import com.dylibso.chicory.runtime.Instance
 *
 * // Create Host and run code
 * val embedderHost = EmbedderHost {
 *     fileSystem {
 *         unrestricted = true
 *     }
 * }
 *
 * // Prepare WASI and Emscripten host imports
 * val installer = ChicoryEmscriptenHostInstaller {
 *     host = embedderHost
 * }
 *
 * val wasiFunctions: List<HostFunction> = installer.setupWasiPreview1HostFunctions()
 * val emscriptenFinalizer: ChicoryEmscriptenSetupFinalizer = installer.setupEmscriptenFunctions()
 *
 * val hostImports = ImportValues.builder()
 *     .withFunctions(emscriptenFinalizer.emscriptenFunctions + wasiFunctions)
 *     .build()
 *
 * // Instantiate the WebAssembly module
 * val instance = Instance
 *     .builder("helloworld.wasm")
 *     .withImportValues(hostImports)
 *     .withInitialize(true)
 *     .withStart(false)
 *     .build()
 *
 * // Finalize initialization after module instantiation
 * val emscriptenRuntime = emscriptenFinalizer.finalize(instance)
 *
 * // Initialize Emscripten runtime environment
 * emscriptenRuntime.initMainThread()
 *
 * // Execute code
 * instance.export("main").apply(
 *     /* argc */ 0,
 *     /* argv */ 0,
 * )[0]
 *
 * ```
 */
public class ChicoryEmscriptenHostInstaller private constructor(
    private val host: EmbedderHost,
    chicoryMemoryProvider: ChicoryMemoryProvider?,
) {
    private val memoryProvider = chicoryMemoryProvider ?: DefaultChicoryMemoryProvider

    private object DefaultChicoryMemoryProvider : ChicoryMemoryProvider {
        override fun get(instance: Instance): ChicoryMemoryAdapter = ChicoryMemoryAdapter(instance.memory())
    }

    @JvmOverloads
    public fun setupWasiPreview1HostFunctions(
        moduleName: String = WASI_SNAPSHOT_PREVIEW1_MODULE_NAME,
    ): List<@JvmSuppressWildcards HostFunction> = ChicoryWasiPreview1Builder {
        this.host = this@ChicoryEmscriptenHostInstaller.host
        this.memoryProvider = this@ChicoryEmscriptenHostInstaller.memoryProvider
    }.build(moduleName)

    @JvmOverloads
    public fun setupEmscriptenFunctions(
        moduleName: String = ENV_MODULE_NAME,
    ): ChicoryEmscriptenSetupFinalizer {
        return ChicoryEmscriptenSetupFinalizer(
            host = host,
            memoryProvider = memoryProvider,
        ).apply {
            setupEmscriptenFunctions(moduleName)
        }
    }

    @WasiEmscriptenHostDsl
    public class Builder {
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
        public fun setHost(host: EmbedderHost?): Builder = apply {
            this.host = host
        }

        /**
         * Sets memory provider used for all operations. For multi-memory scenarios.
         */
        public fun setMemoryProvider(memoryProvider: ChicoryMemoryProvider): Builder = apply {
            this.memoryProvider = memoryProvider
        }

        public fun build(): ChicoryEmscriptenHostInstaller {
            return ChicoryEmscriptenHostInstaller(
                host ?: EmbedderHostBuilder().build(),
                memoryProvider,
            )
        }
    }

    public class ChicoryEmscriptenSetupFinalizer internal constructor(
        private val host: EmbedderHost,
        private val memoryProvider: ChicoryMemoryProvider,
    ) {
        public var emscriptenFunctions: List<@JvmSuppressWildcards HostFunction> = emptyList()
            private set
        private var _emscriptenStack: EmscriptenStack? = null
        private val emscriptenStack: EmscriptenStack
            get() = _emscriptenStack ?: error("Emscripten instantiation is not finalized")

        internal fun setupEmscriptenFunctions(
            moduleName: String,
        ) {
            emscriptenFunctions = EmscriptenEnvFunctionsBuilder(
                memoryProvider = memoryProvider,
                host = host,
                stackBindingsRef = ::emscriptenStack,
            ).asChicoryHostFunctions(
                moduleName = moduleName,
            )
        }

        public fun finalize(instance: Instance): EmscriptenRuntime {
            val emscriptenRuntime = DefaultEmscriptenRuntime.emscriptenSingleThreadedRuntime(
                mainExports = ChicoryEmscriptenMainExports(instance),
                stackExports = ChicoryEmscriptenStackExports(instance),
                memory = memoryProvider.get(instance),
                logger = host.rootLogger,
            )
            _emscriptenStack = emscriptenRuntime.stack
            return emscriptenRuntime
        }
    }

    public companion object {
        @JvmSynthetic
        public operator fun invoke(
            block: Builder.() -> Unit = {},
        ): ChicoryEmscriptenHostInstaller {
            return Builder().apply(block).build()
        }
    }
}
