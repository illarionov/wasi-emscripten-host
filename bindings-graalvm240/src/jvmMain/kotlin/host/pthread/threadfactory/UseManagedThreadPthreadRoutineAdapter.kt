/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.graalvm240.host.pthread.threadfactory

import at.released.weh.bindings.graalvm240.ext.getArgAsWasmPtr
import at.released.weh.bindings.graalvm240.host.module.BaseWasmNode
import at.released.weh.bindings.graalvm240.host.pthread.threadfactory.UseManagedThreadPthreadRoutineAdapter.UseManagedThreadPthreadRoutineFunctionHandle
import at.released.weh.host.EmbedderHost
import at.released.weh.host.base.WasmPtr
import at.released.weh.host.base.function.HostFunctionHandle
import com.oracle.truffle.api.CompilerDirectives
import com.oracle.truffle.api.frame.VirtualFrame
import org.graalvm.wasm.WasmContext
import org.graalvm.wasm.WasmInstance
import org.graalvm.wasm.WasmLanguage
import org.graalvm.wasm.WasmModule

internal class UseManagedThreadPthreadRoutineAdapter(
    language: WasmLanguage,
    module: WasmModule,
    host: EmbedderHost,
) : BaseWasmNode<UseManagedThreadPthreadRoutineFunctionHandle>(
    language,
    module,
    UseManagedThreadPthreadRoutineFunctionHandle(host),
) {
    override fun executeWithContext(frame: VirtualFrame, context: WasmContext, wasmInstance: WasmInstance): Int {
        val args = frame.arguments
        return handle.execute(args.getArgAsWasmPtr(0))
    }

    internal class UseManagedThreadPthreadRoutineFunctionHandle(
        host: EmbedderHost,
    ) : HostFunctionHandle(ExternalManagedThreadOrchestrator.USE_MANAGED_THREAD_PTHREAD_ROUTINE_FUNCTION, host) {
        @CompilerDirectives.TruffleBoundary
        fun execute(arg: WasmPtr<Unit>): Int {
            logger.v { "Managed thread start_routine called with arg $arg. Do nothing." }
            return 0
        }
    }
}