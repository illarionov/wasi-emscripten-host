/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("EXTENSION_FUNCTION_SAME_SIGNATURE", "COMMENTED_OUT_CODE")

package at.released.weh.bindings.chasm.module.emscripten.function

import at.released.weh.bindings.chasm.ext.asInt
import at.released.weh.bindings.chasm.memory.ChasmMemoryAdapter
import at.released.weh.bindings.chasm.module.emscripten.HostFunctionProvider
import at.released.weh.common.api.Logger
import at.released.weh.emcripten.runtime.function.EmscriptenResizeHeapFunctionHandle.Companion.calculateNewSizePages
import at.released.weh.host.EmbedderHost
import at.released.weh.wasi.preview1.type.Errno.NOMEM
import at.released.weh.wasm.core.memory.Pages
import at.released.weh.wasm.core.memory.WASM_MEMORY_32_MAX_PAGES
import at.released.weh.wasm.core.memory.WASM_MEMORY_PAGE_SIZE
import io.github.charlietap.chasm.embedding.memory.sizeMemory
import io.github.charlietap.chasm.embedding.shapes.HostFunction
import io.github.charlietap.chasm.embedding.shapes.Store
import io.github.charlietap.chasm.embedding.shapes.getOrNull
import io.github.charlietap.chasm.executor.runtime.value.ExecutionValue
import io.github.charlietap.chasm.executor.runtime.value.NumberValue.I32

internal class EmscriptenResizeHeap(
    host: EmbedderHost,
    private val memory: ChasmMemoryAdapter,
) : HostFunctionProvider {
    private val logger: Logger = host.rootLogger.withTag("wasm-func:emscripten_resize_heap")
    override val function: HostFunction = { resizeHeap(this.store, it) }

    private fun resizeHeap(
        store: Store,
        args: List<ExecutionValue>,
    ): List<ExecutionValue> {
        val requestedSize = args[0].asInt().toLong()

        val chasmMemorySize = sizeMemory(store, memory.memoryInstance).getOrNull() ?: return listOf(I32(-NOMEM.code))

        val oldPages = Pages(chasmMemorySize / WASM_MEMORY_PAGE_SIZE)
        val maxPages = WASM_MEMORY_32_MAX_PAGES
        val newSizePages = calculateNewSizePages(requestedSize, oldPages, maxPages)

        logger.v {
            "emscripten_resize_heap($requestedSize). " +
                    "Requested: ${newSizePages.inBytes} bytes ($newSizePages pages)"
        }

        // TODO: broken, need to be fixed
        // val prevPages = memory.grow((newSizePages.count - oldPages.count).toInt())
        val prevPages = -1
        if (prevPages < 0) {
            logger.e {
                "Cannot enlarge memory, requested $newSizePages pages, but the limit is " +
                        "$maxPages pages!"
            }
            return listOf(I32(-NOMEM.code))
        }
        return listOf(I32(1))
    }
}
