/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.graalvm240.host.pthread

import at.released.weh.host.base.WasmPtr
import at.released.weh.host.base.binding.IndirectFunctionBindingProvider
import at.released.weh.host.base.function.IndirectFunctionTableIndex
import at.released.weh.host.emscripten.export.pthread.EmscriptenPthread
import at.released.weh.host.emscripten.export.pthread.EmscriptenPthreadInternal
import at.released.weh.host.include.StructPthread

internal class ManagedPthread(
    name: String,
    override var pthreadPtr: WasmPtr<StructPthread>?,
    private val startRoutine: Int,
    private val arg: WasmPtr<Unit>,
    stateListener: StateListener,
    emscriptenPthread: EmscriptenPthread,
    emscriptenPthreadInternal: EmscriptenPthreadInternal,
    threadInitializer: ManagedThreadInitializer,
    private val indirectBindingProvider: IndirectFunctionBindingProvider,
) : ManagedThreadBase(
    name = name,
    emscriptenPthread = emscriptenPthread,
    pthreadInternal = emscriptenPthreadInternal,
    threadInitializer = threadInitializer,
    stateListener = stateListener,
) {
    override fun managedRun() {
        invokeStartRoutine()
    }

    private fun invokeStartRoutine() {
        indirectBindingProvider.getFunctionBinding(
            IndirectFunctionTableIndex(startRoutine),
        )?.executeForInt(arg.addr) ?: error("Indirect function `$startRoutine` not registered")
    }
}
