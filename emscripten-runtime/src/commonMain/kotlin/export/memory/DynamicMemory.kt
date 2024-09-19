/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.emcripten.runtime.export.memory

import at.released.weh.common.api.InternalWasiEmscriptenHostApi
import at.released.weh.wasm.core.IntWasmPtr
import at.released.weh.wasm.core.WasmPtr
import at.released.weh.wasm.core.WasmPtrUtil.ptrIsNull

@InternalWasiEmscriptenHostApi
public class DynamicMemory(
    public val exports: DynamicMemoryExports,
) {
    @IntWasmPtr
    public fun allocOrThrow(len: UInt): WasmPtr {
        check(len > 0U)
        val mem: WasmPtr = requireNotNull(exports.malloc) {
            functionNotExported("malloc")
        }.executeForPtr(len.toInt())

        if (ptrIsNull(mem)) {
            throw OutOfMemoryException()
        }

        return mem
    }

    public fun free(@IntWasmPtr ptr: WasmPtr) {
        requireNotNull(exports.free) {
            functionNotExported("free")
        }.executeVoid(ptr)
    }

    private fun functionNotExported(function: String): String = "$function function is not exported. " +
            "Recompile application with \"_malloc\" and \"_free\" in EXPORTED_FUNCTIONS"

    public class OutOfMemoryException : RuntimeException()
}

@InternalWasiEmscriptenHostApi
public fun DynamicMemory.freeSilent(@IntWasmPtr value: WasmPtr): Result<Unit> = kotlin.runCatching {
    free(value)
}
