/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.graalvm241

import at.released.weh.bindings.graalvm241.exports.GraalvmDynamicMemoryExports
import at.released.weh.bindings.graalvm241.exports.GraalvmEmscriptenMainExports
import at.released.weh.bindings.graalvm241.exports.GraalvmEmscriptenPthreadExports
import at.released.weh.bindings.graalvm241.exports.GraalvmEmscriptenStackExports
import at.released.weh.bindings.graalvm241.exports.GraalvmIndirectFunctionProvider
import at.released.weh.bindings.graalvm241.ext.withWasmContext
import at.released.weh.bindings.graalvm241.host.GraalvmEmscriptenRuntime
import at.released.weh.bindings.graalvm241.host.memory.GraalvmWasmHostMemoryAdapter
import at.released.weh.bindings.graalvm241.host.memory.SharedMemoryWaiterListStore
import at.released.weh.bindings.graalvm241.host.pthread.GraalvmPthreadManager
import at.released.weh.bindings.graalvm241.host.pthread.ManagedThreadInitializer
import at.released.weh.bindings.graalvm241.host.pthread.threadfactory.GraalvmManagedThreadFactory
import at.released.weh.common.api.InternalWasiEmscriptenHostApi
import at.released.weh.common.api.Logger
import at.released.weh.emcripten.runtime.export.memory.DynamicMemory
import at.released.weh.emcripten.runtime.export.pthread.EmscriptenPthread
import at.released.weh.emcripten.runtime.export.pthread.EmscriptenPthreadInternal
import at.released.weh.host.EmbedderHost
import at.released.weh.host.base.function.IndirectFunctionTableIndex
import at.released.weh.host.include.StructPthread
import at.released.weh.wasm.core.IntWasmPtr
import at.released.weh.wasm.core.WasmPtr
import at.released.weh.wasm.core.memory.Memory
import org.graalvm.polyglot.Context
import org.graalvm.polyglot.Value
import java.util.concurrent.ThreadFactory

public class GraalvmEmscriptenEnvironment internal constructor(
    private val graalContext: Context,
    private val mainModuleName: String,
    private val envModuleName: String,
    private val isMainThread: Boolean = true,
    private val host: EmbedderHost,
    private val sharedMemoryWaiters: SharedMemoryWaiterListStore,
    rootLogger: Logger,
) : AutoCloseable {
    private val logger = rootLogger.withTag("GraalvmEmscriptenEnvironment")

    @InternalWasiEmscriptenHostApi
    public val threadLocalGraalContext: ThreadLocal<Context> = ThreadLocal<Context>().also {
        it.set(graalContext)
    }

    // TODO: rewrite
    @InternalWasiEmscriptenHostApi
    @Volatile
    public var managedThreadInitializer: ManagedThreadInitializer = object : ManagedThreadInitializer {
        override fun initThreadLocalGraalvmAgent() {
            error("Not implemented")
        }

        override fun destroyThreadLocalGraalvmAgent() {
            error("Not implemented")
        }

        override fun initWorkerThread(@IntWasmPtr(StructPthread::class) threadPtr: WasmPtr) {
            error("Not implemented")
        }
    }
    private val lazyManagedThreadInitializer: ManagedThreadInitializer = object : ManagedThreadInitializer {
        override fun initThreadLocalGraalvmAgent() {
            managedThreadInitializer.initThreadLocalGraalvmAgent()
        }

        override fun destroyThreadLocalGraalvmAgent() {
            managedThreadInitializer.destroyThreadLocalGraalvmAgent()
        }

        override fun initWorkerThread(@IntWasmPtr(StructPthread::class) threadPtr: WasmPtr) {
            managedThreadInitializer.initWorkerThread(threadPtr)
        }
    }

    @InternalWasiEmscriptenHostApi
    public var externalManagedThreadStartRoutine: IndirectFunctionTableIndex = IndirectFunctionTableIndex(-1)

    private val localGraalContext: Context
        get() {
            val threadContext = threadLocalGraalContext.get()
            if (threadContext != null) {
                return threadContext
            } else {
                logger.e {
                    "Graal Wasm environment is not initialized for current thread ${Thread.currentThread().name} " +
                            "and cannot be used to call WASM functions"
                }
                threadLocalGraalContext.set(graalContext)
                return graalContext
            }
        }
    private val mainBindings: Value
        get() = requireNotNull(localGraalContext.getBindings("wasm").getMember(mainModuleName)) {
            "module `$mainModuleName` not loaded"
        }

    private val emscriptenMainExports = GraalvmEmscriptenMainExports(::mainBindings)
    private val stackExports = GraalvmEmscriptenStackExports(::mainBindings)
    private val pthreadExports = GraalvmEmscriptenPthreadExports(::mainBindings)
    private val dynamicMemoryExports = GraalvmDynamicMemoryExports(::mainBindings)
    private val indirectFunctionBindingProvider = GraalvmIndirectFunctionProvider(::mainBindings)
    private val dynamicMemory = DynamicMemory(dynamicMemoryExports)

    @InternalWasiEmscriptenHostApi
    public val memory: Memory = GraalvmWasmHostMemoryAdapter(
        memoryProvider = {
            localGraalContext.withWasmContext {
                it.memories().memory(0)
            }
        },
        node = null,
    )
    private val emscriptenPthread = EmscriptenPthread(pthreadExports, dynamicMemory, memory)
    private val emscriptenPthreadInternal = EmscriptenPthreadInternal(pthreadExports)

    @InternalWasiEmscriptenHostApi
    public val pthreadManager: GraalvmPthreadManager = GraalvmPthreadManager(
        emscriptenPthreadInternal = emscriptenPthreadInternal,
        emscriptenPthread = emscriptenPthread,
        dynamicMemory = dynamicMemory,
        memory = memory,
        indirectFunctionBindingProvider = indirectFunctionBindingProvider,
        managedThreadInitializer = lazyManagedThreadInitializer,
        externalManagedThreadStartRoutine = { externalManagedThreadStartRoutine },
        rootLogger = logger,
    )

    @InternalWasiEmscriptenHostApi
    public val managedThreadFactory: ThreadFactory = GraalvmManagedThreadFactory(
        emscriptenPthread = emscriptenPthread,
        emscriptenPthreadInternal = emscriptenPthreadInternal,
        pthreadManager = pthreadManager,
        managedThreadInitializer = lazyManagedThreadInitializer,
        rootLogger = rootLogger,
    )

    @InternalWasiEmscriptenHostApi
    public val emscriptenRuntime: GraalvmEmscriptenRuntime = GraalvmEmscriptenRuntime.multithreadedRuntime(
        mainExports = emscriptenMainExports,
        stackExports = stackExports,
        emscriptenPthread = emscriptenPthread,
        emscriptenPthreadInternal = emscriptenPthreadInternal,
        memory = memory,
        logger = rootLogger,
    )

    public fun getWorkerThreadInstaller(
        workerThreadContext: Context,
    ): GraalvmEmscriptenWorkerThreadInstaller = GraalvmEmscriptenWorkerThreadInstaller(
        rootContext = graalContext,
        workerThreadContext = workerThreadContext,
        host = host,
        memoryWaiters = sharedMemoryWaiters,
        parentEnvModuleName = envModuleName,
        pthreadManager = pthreadManager,
        emscriptenStack = emscriptenRuntime.stack,
        parentMainModuleName = mainModuleName,
    )

    override fun close() {
        logger.v { "close()" }
        check(isMainThread) {
            "close() should be called from the main thread"
        }
        // Before closing we need to wait for all child threads to complete otherwise they may use already destroyed
        // shared memory
        pthreadManager.joinThreads()

        threadLocalGraalContext.remove()
        graalContext.close()
    }
}
