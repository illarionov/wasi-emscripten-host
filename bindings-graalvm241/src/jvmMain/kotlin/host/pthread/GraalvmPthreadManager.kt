/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.graalvm241.host.pthread

import at.released.weh.bindings.graalvm241.host.pthread.ManagedThreadBase.State.DESTROYING
import at.released.weh.bindings.graalvm241.host.pthread.threadfactory.ExternalManagedThreadOrchestrator
import at.released.weh.common.api.Logger
import at.released.weh.emcripten.runtime.export.IndirectFunctionTableIndex
import at.released.weh.emcripten.runtime.export.memory.DynamicMemory
import at.released.weh.emcripten.runtime.export.pthread.EmscriptenPthread
import at.released.weh.emcripten.runtime.export.pthread.EmscriptenPthreadInternal
import at.released.weh.emcripten.runtime.export.pthread.PthreadManager
import at.released.weh.host.include.StructPthread
import at.released.weh.host.include.pthread_t
import at.released.weh.wasm.core.IntWasmPtr
import at.released.weh.wasm.core.WasmPtr
import at.released.weh.wasm.core.memory.Memory
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

public class GraalvmPthreadManager(
    memory: Memory,
    dynamicMemory: DynamicMemory,
    private val externalManagedThreadStartRoutine: () -> IndirectFunctionTableIndex,
    private val managedThreadInitializer: ManagedThreadInitializer,
    private val indirectFunctionBindingProvider: IndirectFunctionBindingProvider,
    private val emscriptenPthreadInternal: EmscriptenPthreadInternal,
    private val emscriptenPthread: EmscriptenPthread,

    @Suppress("DEPRECATION")
    private val mainThreadId: Long = Thread.currentThread().id,
    rootLogger: Logger,
) : PthreadManager(
    emscriptenPthreadInternal,
    {
        @Suppress("DEPRECATION")
        mainThreadId == Thread.currentThread().id
    },
) {
    private val logger: Logger = rootLogger.withTag("GraalvmPthreadManager")
    private val lock = ReentrantLock()
    private val threads = NativeThreadRegistry()
    private val threadNumber = AtomicInteger(1)
    private val externalThreadOrchestrator = ExternalManagedThreadOrchestrator(
        pthread = emscriptenPthread,
        dynamicMemory = dynamicMemory,
        memory = memory,
        externalManagedThreadStartRoutine = externalManagedThreadStartRoutine,
        rootLogger = rootLogger,
    )

    public fun createWasmPthreadForThread(thread: Thread): pthread_t =
        externalThreadOrchestrator.createWasmPthreadForThread(thread)

    /**
     * Called from `__pthread_create_js` to reuse an existing Web Worker or spawn a new one
     */
    public fun spawnThread(
        @IntWasmPtr(StructPthread::class) pthreadPtr: WasmPtr,
        @IntWasmPtr(Int::class) attr: WasmPtr,
        startRoutine: Int,
        @IntWasmPtr() arg: WasmPtr,
    ): Int {
        logger.v { "spawnThread($pthreadPtr, $attr, $startRoutine, $arg)" }
        return if (startRoutine != externalManagedThreadStartRoutine().funcId) {
            spawnManagedThread(pthreadPtr, startRoutine, arg)
        } else {
            lock.withLock {
                val thread = externalThreadOrchestrator.joinExternalThread(arg)
                threads.register(pthreadPtr, thread)
                0
            }
        }
    }

    private fun spawnManagedThread(
        @IntWasmPtr(StructPthread::class) pthreadPtr: WasmPtr,
        startRoutine: Int,
        @IntWasmPtr arg: WasmPtr,
    ): Int {
        val name = "graalvm-pthread-${threadNumber.getAndDecrement()}"
        val thread = ManagedPthread(
            name = name,
            pthreadPtr = pthreadPtr,
            startRoutine = startRoutine,
            arg = arg,
            threadInitializer = managedThreadInitializer,
            indirectBindingProvider = indirectFunctionBindingProvider,
            emscriptenPthread = emscriptenPthread,
            emscriptenPthreadInternal = emscriptenPthreadInternal,
            stateListener = { thread, ptr, newState ->
                logger.v { "Thread $ptr: $newState" }

                when (newState) {
                    DESTROYING -> unregisterManagedThread(ptr!!, thread)
                    else -> {}
                }
            },
        )
        thread.setUncaughtExceptionHandler { terminatedThread, throwable ->
            val ptr = (terminatedThread as ManagedPthread).pthreadPtr
            logger.i(throwable) { "Uncaught exception in Pthread $ptr ${terminatedThread.name} " }
            if (ptr != null) {
                unregisterManagedThread(ptr, thread, throwable)
            }
            throw throwable
        }

        lock.withLock {
            threads.register(pthreadPtr, thread)
        }
        thread.start()

        return 0
    }

    public fun unregisterManagedThread(
        @IntWasmPtr(StructPthread::class) pthreadPtr: WasmPtr,
        thread: Thread,
        throwable: Throwable? = null,
    ): Unit = lock.withLock {
        threads.unregister(pthreadPtr, thread, throwable)
    }

    public fun joinThreads(
        maxTimeout: Duration = MAX_JOIN_THREADS_TIMEOUT,
    ) {
        val deadline = System.nanoTime() + maxTimeout.inWholeNanoseconds
        val threads = lock.withLock {
            threads.getAllThreads()
        }
        logger.v { "joinThreads(): waiting fot ${threads.count()} threads" }

        var stuck: Thread? = null
        for (thread in threads) {
            val waitTime = java.time.Duration.ofNanos(deadline - System.nanoTime())
            val terminated = thread.join(waitTime)
            if (!terminated) {
                stuck = thread
                break
            }
        }
        if (stuck != null) {
            logger.e { "joinThreads(): timeout on waiting for thread ${stuck.name}" }
        }
    }

    private class NativeThreadRegistry {
        private val threads: MutableMap<WasmPtr, Thread> = mutableMapOf()

        fun register(
            @IntWasmPtr(StructPthread::class) ptr: WasmPtr,
            thread: Thread,
        ) {
            val old = threads.getOrPut(ptr) { thread }
            check(old == thread) {
                "Another thread already registered for $ptr"
            }
        }

        fun unregister(
            @IntWasmPtr(StructPthread::class) ptr: WasmPtr,
            terminatedThread: Thread,
            error: Throwable? = null,
        ) {
            val oldThread = threads.remove(ptr)
            @Suppress("UseCheckOrError")
            if (oldThread != terminatedThread) {
                throw IllegalStateException("Removed wrong thread $oldThread").also {
                    if (error != null) {
                        it.addSuppressed(error)
                    }
                }
            }
        }

        fun getAllThreads(): List<Thread> = threads.values.toList()
    }

    private companion object {
        private val MAX_JOIN_THREADS_TIMEOUT = 5.seconds
    }
}
