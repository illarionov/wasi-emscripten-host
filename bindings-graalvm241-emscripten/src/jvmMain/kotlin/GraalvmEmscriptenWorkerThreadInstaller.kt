/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.graalvm241

import at.released.weh.bindings.graalvm241.host.module.emscripten.EmscriptenEnvModuleBuilder
import at.released.weh.bindings.graalvm241.host.pthread.GraalvmPthreadManager
import at.released.weh.bindings.graalvm241.memory.MemorySource.ExportedExternalMemory
import at.released.weh.bindings.graalvm241.memory.MemorySource.ImportedMemory
import at.released.weh.bindings.graalvm241.memory.SharedMemoryWaiterListStore
import at.released.weh.bindings.graalvm241.wasip1.GraalvmWasiPreview1Builder
import at.released.weh.emcripten.runtime.export.stack.EmscriptenStack
import at.released.weh.host.EmbedderHost
import at.released.weh.wasm.core.WasmModules.ENV_MODULE_NAME
import at.released.weh.wasm.core.WasmModules.WASI_SNAPSHOT_PREVIEW1_MODULE_NAME
import org.graalvm.polyglot.Context

public class GraalvmEmscriptenWorkerThreadInstaller internal constructor(
    private val rootContext: Context,
    private val workerThreadContext: Context,
    private val host: EmbedderHost,
    private val memoryWaiters: SharedMemoryWaiterListStore,
    private val pthreadManager: GraalvmPthreadManager,
    private val emscriptenStack: EmscriptenStack,
    private val parentEnvModuleName: String = ENV_MODULE_NAME,
    private val parentMainModuleName: String,
) {
    @JvmOverloads
    public fun setupWasiPreview1Module(
        moduleName: String = WASI_SNAPSHOT_PREVIEW1_MODULE_NAME,
        memory: ImportedMemory? = ImportedMemory(moduleName = ENV_MODULE_NAME),
    ) {
        GraalvmWasiPreview1Builder {
            this.host = host
            this.memorySource = memory
        }.build(workerThreadContext, moduleName)
    }

    @JvmOverloads
    public fun setupEmscriptenFunctions(
        sourceMemoryIndex: Int = 0,
    ): GraalvmEmscriptenEnvironment {
        val memorySource = ExportedExternalMemory(
            sourceContext = rootContext,
            sourceMemoryIndex = sourceMemoryIndex,
        )
        EmscriptenEnvModuleBuilder(
            host = host,
            pthreadRef = ::pthreadManager,
            emscriptenStackRef = ::emscriptenStack,
            memoryWaiters = memoryWaiters,
        ).setupModule(
            graalContext = workerThreadContext,
            moduleName = parentEnvModuleName,
            memorySource = memorySource,
        )

        return GraalvmEmscriptenEnvironment(
            graalContext = workerThreadContext,
            mainModuleName = parentMainModuleName,
            isMainThread = false,
            rootLogger = host.rootLogger,
            envModuleName = parentEnvModuleName,
            sharedMemoryWaiters = memoryWaiters,
            host = host,
        )
    }
}
