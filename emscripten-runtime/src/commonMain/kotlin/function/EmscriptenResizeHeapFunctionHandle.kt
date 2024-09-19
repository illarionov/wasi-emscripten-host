/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.emcripten.runtime.function

import at.released.weh.emcripten.runtime.EmscriptenHostFunction.EMSCRIPTEN_RESIZE_HEAP
import at.released.weh.host.EmbedderHost
import at.released.weh.wasm.core.memory.Pages
import at.released.weh.wasm.core.memory.WASM_MEMORY_PAGE_SIZE

public class EmscriptenResizeHeapFunctionHandle(
    host: EmbedderHost,
) : EmscriptenHostFunctionHandle(EMSCRIPTEN_RESIZE_HEAP, host) {
    @Suppress("MagicNumber")
    public companion object {
        private const val OVER_GROWN_HEAP_SIZE_MAX_ADD = 96 * 1024 * 1024

        public fun calculateNewSizePages(
            requestedSizeBytes: Long,
            memoryPages: Pages,
            memoryMaxPages: Pages,
        ): Pages {
            check(requestedSizeBytes > memoryPages.inBytes)

            val oldSize = memoryPages.inBytes
            val overGrownHeapSize = minOf(
                oldSize + (oldSize / 5),
                requestedSizeBytes + OVER_GROWN_HEAP_SIZE_MAX_ADD,
            ).coerceAtLeast(requestedSizeBytes)
            val newPages = ((overGrownHeapSize + WASM_MEMORY_PAGE_SIZE - 1) / WASM_MEMORY_PAGE_SIZE)
                .coerceAtMost(memoryMaxPages.count)
            return Pages(newPages)
        }
    }
}
