/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.graalvm241.host.pthread

import at.released.weh.bindings.graalvm241.host.pthread.ManagedThreadBase.State.ATTACHING
import at.released.weh.bindings.graalvm241.host.pthread.ManagedThreadBase.State.DESTROYED
import at.released.weh.bindings.graalvm241.host.pthread.ManagedThreadBase.State.DESTROYING
import at.released.weh.bindings.graalvm241.host.pthread.ManagedThreadBase.State.DETACHING
import at.released.weh.bindings.graalvm241.host.pthread.ManagedThreadBase.State.LOADING
import at.released.weh.bindings.graalvm241.host.pthread.ManagedThreadBase.State.NOT_STARTED
import at.released.weh.bindings.graalvm241.host.pthread.ManagedThreadBase.State.RUNNING
import at.released.weh.bindings.graalvm241.host.pthread.ManagedThreadBase.StateListener
import at.released.weh.host.emscripten.export.pthread.EmscriptenPthread
import at.released.weh.host.emscripten.export.pthread.EmscriptenPthreadInternal
import at.released.weh.host.include.StructPthread
import at.released.weh.wasm.core.IntWasmPtr
import at.released.weh.wasm.core.WasmPtr

internal abstract class ManagedThreadBase(
    name: String,
    private val emscriptenPthread: EmscriptenPthread,
    private val pthreadInternal: EmscriptenPthreadInternal,
    private val threadInitializer: ManagedThreadInitializer,
    private val stateListener: StateListener = StateListener { _, _, _ -> },
) : Thread(name) {
    @IntWasmPtr(StructPthread::class)
    abstract var pthreadPtr: WasmPtr?
    private val stateLock = Any()
    private var wasmAgentLoaded: Boolean = false
    private var pthreadAttached: Boolean = false

    @Volatile
    public var state: State = NOT_STARTED
        private set(newState) {
            synchronized(stateLock) {
                field = newState
            }
            stateListener.onNewState(this, pthreadPtr, newState)
        }

    override fun run() {
        checkNotNull(pthreadPtr) {
            "pthreadPtr is not set"
        }

        state = LOADING
        try {
            loadWasmAgent()

            state = ATTACHING
            attachPthread()

            state = RUNNING
            managedRun()
        } finally {
            state = DETACHING
            try {
                detachPthread()
            } finally {
                state = DESTROYING
                destroyThreadPtr()
                unloadWasmAgent()

                state = DESTROYED
            }
        }
    }

    abstract fun managedRun()

    private fun loadWasmAgent() {
        threadInitializer.initThreadLocalGraalvmAgent()
        wasmAgentLoaded = true
    }

    private fun unloadWasmAgent() {
        threadInitializer.destroyThreadLocalGraalvmAgent()
        wasmAgentLoaded = false
    }

    private fun attachPthread() {
        val ptr = requireNotNull(pthreadPtr)
        threadInitializer.initWorkerThread(ptr)

        check(emscriptenPthread.pthreadSelf() == ptr.toULong()) {
            "pthreadSelf is not $ptr"
        }

        pthreadAttached = true
    }

    private fun detachPthread() {
        if (!pthreadAttached) {
            return
        }
        pthreadAttached = false
        pthreadInternal.emscriptenThreadExit(-1)
    }

    private fun destroyThreadPtr() {
        val ptr = requireNotNull(pthreadPtr)
        pthreadInternal.emscriptenThreadFreeData(ptr)
        pthreadPtr = null
    }

    public enum class State {
        NOT_STARTED,
        LOADING,
        ATTACHING,
        RUNNING,
        DETACHING,
        DESTROYING,
        DESTROYED,
    }

    internal fun interface StateListener {
        fun onNewState(
            thread: ManagedThreadBase,
            @IntWasmPtr(StructPthread::class) pthreadPtr: WasmPtr?,
            newState: State,
        )
    }
}
