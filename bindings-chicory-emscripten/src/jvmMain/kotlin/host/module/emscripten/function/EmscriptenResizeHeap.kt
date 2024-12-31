/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.chicory.host.module.emscripten.function

import at.released.weh.common.api.Logger
import at.released.weh.emcripten.runtime.function.EmscriptenResizeHeapFunctionHandle.Companion.calculateNewSizePages
import at.released.weh.host.EmbedderHost
import at.released.weh.wasi.preview1.type.Errno
import at.released.weh.wasm.core.memory.Pages
import com.dylibso.chicory.runtime.Instance
import com.dylibso.chicory.runtime.WasmFunctionHandle

internal class EmscriptenResizeHeap(host: EmbedderHost) : WasmFunctionHandle {
    private val logger: Logger = host.rootLogger.withTag("wasm-func:emscripten_resize_heap")

    override fun apply(instance: Instance, vararg args: Long): LongArray {
        val memory = instance.memory()
        val requestedSize = args[0]

        val newSizePages = calculateNewSizePages(
            requestedSize,
            Pages(memory.pages().toLong()),
            Pages(memory.maximumPages().toLong()),
        )

        logger.v {
            "emscripten_resize_heap($requestedSize). " +
                    "Requested: ${newSizePages.inBytes} bytes ($newSizePages pages)"
        }

        val prevPages = memory.grow((newSizePages.count - memory.pages()).toInt())
        if (prevPages < 0) {
            logger.e {
                "Cannot enlarge memory, requested $newSizePages pages, but the limit is " +
                        "${memory.maximumPages()} pages!"
            }
            return LongArray(1) { -Errno.NOMEM.code.toLong() }
        }
        return LongArray(1) { 1 }
    }
}
