/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.graalvm241.host.module.emscripten.function

import at.released.weh.bindings.graalvm241.ext.getArgAsInt
import at.released.weh.bindings.graalvm241.host.module.emscripten.BaseEmscriptenWasmNode
import at.released.weh.bindings.graalvm241.host.module.emscripten.function.EmscriptenResizeHeap.ResizeHeapHandle
import at.released.weh.emcripten.runtime.EmscriptenHostFunction.EMSCRIPTEN_RESIZE_HEAP
import at.released.weh.emcripten.runtime.function.EmscriptenHostFunctionHandle
import at.released.weh.emcripten.runtime.function.EmscriptenResizeHeapFunctionHandle.Companion.calculateNewSizePages
import at.released.weh.host.EmbedderHost
import at.released.weh.wasi.preview1.type.Errno.NOMEM
import at.released.weh.wasm.core.memory.Pages
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary
import com.oracle.truffle.api.frame.VirtualFrame
import org.graalvm.wasm.WasmContext
import org.graalvm.wasm.WasmInstance
import org.graalvm.wasm.WasmLanguage
import org.graalvm.wasm.WasmModule
import org.graalvm.wasm.memory.WasmMemory

internal class EmscriptenResizeHeap(
    language: WasmLanguage,
    module: WasmModule,
    host: EmbedderHost,
) : BaseEmscriptenWasmNode<ResizeHeapHandle>(language, module, ResizeHeapHandle(host)) {
    override fun executeWithContext(frame: VirtualFrame, context: WasmContext, instance: WasmInstance): Any {
        return handle.execute(
            memory(frame),
            frame.arguments.getArgAsInt(0).toLong(),
        )
    }

    internal class ResizeHeapHandle(
        host: EmbedderHost,
    ) : EmscriptenHostFunctionHandle(EMSCRIPTEN_RESIZE_HEAP, host) {
        @TruffleBoundary
        fun execute(
            memory: WasmMemory,
            requestedSize: Long,
        ): Int {
            val currentPages = Pages(memory.size())
            val declaredMaxPages = Pages(memory.declaredMaxSize())
            val newSizePages = calculateNewSizePages(requestedSize, currentPages, declaredMaxPages)

            logger.v {
                "emscripten_resize_heap($requestedSize). " +
                        "Requested: ${newSizePages.inBytes} bytes ($newSizePages pages)"
            }

            val memoryAdded = memory.grow(newSizePages.count - currentPages.count)
            return if (memoryAdded < 0) {
                return -NOMEM.code
            } else {
                1
            }
        }
    }
}
