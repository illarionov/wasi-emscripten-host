/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.chasm.module.emscripten.function

import at.released.weh.bindings.chasm.ext.asInt
import at.released.weh.bindings.chasm.memory.ChasmMemoryAdapter
import at.released.weh.common.api.Logger
import at.released.weh.filesystem.model.Errno.NOMEM
import at.released.weh.host.EmbedderHost
import at.released.weh.host.base.memory.Pages
import at.released.weh.host.base.memory.WASM_MEMORY_32_MAX_PAGES
import at.released.weh.host.emscripten.function.EmscriptenResizeHeapFunctionHandle.Companion.calculateNewSizePages
import io.github.charlietap.chasm.embedding.shapes.HostFunction
import io.github.charlietap.chasm.embedding.shapes.Value

internal class EmscriptenResizeHeap(
    host: EmbedderHost,
    private val memory: ChasmMemoryAdapter,
) : HostFunction {
    private val logger: Logger = host.rootLogger.withTag("wasm-func:emscripten_resize_heap")

    override fun invoke(args: List<Value>): List<Value> {
        val requestedSize = args[0].asInt().toLong()

        val chasmMemoryLimits = memory.limits
        val oldPages = Pages(chasmMemoryLimits.min.toLong())
        val maxPages = chasmMemoryLimits.max?.toLong()?.let(::Pages) ?: WASM_MEMORY_32_MAX_PAGES
        val newSizePages = calculateNewSizePages(requestedSize, oldPages, maxPages)

        logger.v {
            "emscripten_resize_heap($requestedSize). " +
                    "Requested: ${newSizePages.inBytes} bytes ($newSizePages pages)"
        }

        val prevPages = memory.grow((newSizePages.count - oldPages.count).toInt())
        if (prevPages < 0) {
            logger.e {
                "Cannot enlarge memory, requested $newSizePages pages, but the limit is " +
                        "$maxPages pages!"
            }
            return listOf(Value.Number.I32(-NOMEM.code))
        }
        return listOf(Value.Number.I32(1))
    }
}
