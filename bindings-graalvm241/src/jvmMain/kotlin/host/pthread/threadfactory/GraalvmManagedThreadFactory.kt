/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.graalvm241.host.pthread.threadfactory

import at.released.weh.bindings.graalvm241.host.pthread.GraalvmPthreadManager
import at.released.weh.bindings.graalvm241.host.pthread.ManagedThreadBase
import at.released.weh.bindings.graalvm241.host.pthread.ManagedThreadBase.State.DESTROYING
import at.released.weh.bindings.graalvm241.host.pthread.ManagedThreadInitializer
import at.released.weh.common.api.Logger
import at.released.weh.emcripten.runtime.export.pthread.EmscriptenPthread
import at.released.weh.emcripten.runtime.export.pthread.EmscriptenPthreadInternal
import at.released.weh.host.include.StructPthread
import at.released.weh.wasm.core.IntWasmPtr
import at.released.weh.wasm.core.WasmPtr
import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger

internal class GraalvmManagedThreadFactory(
    private val emscriptenPthread: EmscriptenPthread,
    private val emscriptenPthreadInternal: EmscriptenPthreadInternal,
    private val pthreadManager: GraalvmPthreadManager,
    private val managedThreadInitializer: ManagedThreadInitializer,
    rootLogger: Logger,
) : ThreadFactory {
    private val logger: Logger = rootLogger.withTag("GraalvmManagedThreadFactory")
    private val threadNumber = AtomicInteger(1)

    override fun newThread(runnable: Runnable): Thread {
        val name = "graalvm-embedder-thread-${threadNumber.getAndDecrement()}"
        val threadStateListener = ManagedThreadBase.StateListener { thread, ptr, newState ->
            logger.v { "Thread $ptr: $newState" }
            when (newState) {
                DESTROYING -> if (ptr != null) {
                    pthreadManager.unregisterManagedThread(ptr, thread)
                }
                else -> {}
            }
        }

        val thread = object : ManagedThreadBase(
            name = name,
            emscriptenPthread = emscriptenPthread,
            pthreadInternal = emscriptenPthreadInternal,
            threadInitializer = managedThreadInitializer,
            stateListener = threadStateListener,
        ) {
            @IntWasmPtr(StructPthread::class)
            override var pthreadPtr: WasmPtr? = null

            override fun managedRun() = runnable.run()
        }
        if (thread.isDaemon) {
            thread.isDaemon = false
        }
        thread.setUncaughtExceptionHandler { terminatedThread, throwable ->
            val ptr = (terminatedThread as ManagedThreadBase).pthreadPtr
            logger.e(throwable) { "Uncaught exception in Managed Thread ${terminatedThread.name} " }
            if (ptr != null) {
                pthreadManager.unregisterManagedThread(ptr, thread)
            }

            throw IllegalStateException("Internal exception", throwable)
        }

        // XXX: Wasm pthread leaks if thread not started
        val ptr = pthreadManager.createWasmPthreadForThread(thread)
        thread.pthreadPtr = ptr.toInt()

        return thread
    }
}
